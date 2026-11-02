package app.rork.un30xjmm47ctw8vs4cms0.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import java.util.UUID

data class AppData(
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val rate: RateSetting = RateSetting(),
    val dirtyKeys: Set<String> = emptySet(),
    val loaded: Boolean = false,
)

data class SyncState(
    val online: Boolean = true,
    val realtime: Boolean = false,
    val syncing: Boolean = false,
    val pending: Int = 0,
    val lastSyncAt: Long = 0,
)

data class BcvState(val rate: Double = 0.0, val updated: String = "", val loading: Boolean = false, val error: Boolean = false)

class Repository private constructor(private val appContext: Context) {

    companion object {
        @Volatile private var instance: Repository? = null
        fun get(context: Context): Repository =
            instance ?: synchronized(this) { instance ?: Repository(context.applicationContext).also { instance = it } }

        const val RATE_ID = "rate"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = LocalDb(appContext)
    private val api = Api()
    private val prefs = appContext.getSharedPreferences("fiaven", Context.MODE_PRIVATE)
    private val syncMutex = Mutex()

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private val _sync = MutableStateFlow(SyncState())
    val sync: StateFlow<SyncState> = _sync.asStateFlow()

    private val _bcv = MutableStateFlow(
        BcvState(rate = prefs.getFloat("bcv_rate", 0f).toDouble(), updated = prefs.getString("bcv_date", "").orEmpty())
    )
    val bcv: StateFlow<BcvState> = _bcv.asStateFlow()

    private var socket: WebSocket? = null
    private var socketJob: Job? = null
    private var loopJob: Job? = null
    private var started = false

    // ---------- lifecycle ----------

    fun start() {
        if (started) return
        started = true
        scope.launch { reload() }
        registerConnectivity()
        if (_session.value != null) startSessionWork()
        refreshBcv()
    }

    private fun startSessionWork() {
        connectSocket()
        loopJob?.cancel()
        loopJob = scope.launch {
            while (isActive) {
                syncNow()
                delay(if (_sync.value.pending > 0) 10_000 else 45_000)
            }
        }
    }

    private fun stopSessionWork() {
        loopJob?.cancel(); loopJob = null
        socketJob?.cancel(); socketJob = null
        socket?.close(1000, "logout"); socket = null
        _sync.update { it.copy(realtime = false) }
    }

    private fun registerConnectivity() {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetwork
        val caps = active?.let { cm.getNetworkCapabilities(it) }
        _sync.update { it.copy(online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) }
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _sync.update { it.copy(online = true) }
                if (_session.value != null) {
                    scope.launch { syncNow() }
                    if (socket == null) connectSocket()
                }
                if (_bcv.value.rate == 0.0) refreshBcv()
            }

            override fun onLost(network: Network) {
                _sync.update { it.copy(online = false, realtime = false) }
            }
        })
    }

    // ---------- session ----------

    private fun loadSession(): Session? {
        val token = prefs.getString("token", null) ?: return null
        return Session(
            token = token,
            username = prefs.getString("username", "").orEmpty(),
            displayName = prefs.getString("display_name", "").orEmpty(),
            role = prefs.getString("role", "worker").orEmpty(),
            businessId = prefs.getString("business_id", "").orEmpty(),
            businessName = prefs.getString("business_name", "").orEmpty(),
        )
    }

    private fun saveSession(auth: AuthResponse) {
        val previousBusiness = prefs.getString("business_id", null)
        if (previousBusiness != null && previousBusiness != auth.business.id) db.clearAll()
        prefs.edit()
            .putString("token", auth.token)
            .putString("username", auth.user.username)
            .putString("display_name", auth.user.displayName)
            .putString("role", auth.user.role)
            .putString("business_id", auth.business.id)
            .putString("business_name", auth.business.name)
            .apply()
        _session.value = loadSession()
        reload()
        startSessionWork()
    }

    suspend fun login(username: String, password: String) {
        saveSession(api.login(username.trim(), password))
    }

    suspend fun register(
        businessName: String, displayName: String, username: String, password: String, question: String, answer: String,
    ) {
        saveSession(api.register(businessName.trim(), displayName.trim(), username.trim(), password, question, normalizeAnswer(answer)))
    }

    // ---------- password recovery & account ----------

    val privacyUrl: String get() = api.privacyUrl
    val deleteAccountUrl: String get() = api.deleteAccountUrl

    suspend fun recoveryQuestion(username: String): String = api.recoveryQuestion(username.trim())

    suspend fun recover(username: String, answer: String, newPassword: String) {
        saveSession(api.recover(username.trim(), normalizeAnswer(answer), newPassword))
    }

    suspend fun security(): SecurityInfo = api.security(requireToken())

    suspend fun setSecurity(password: String, question: String, answer: String) =
        api.setSecurity(requireToken(), password, question, normalizeAnswer(answer))

    /** Deletes the account on the server (the whole business if owner) and wipes this phone. */
    suspend fun deleteAccount(password: String) {
        api.deleteAccount(requireToken(), password)
        stopSessionWork()
        db.clearAll()
        prefs.edit().remove("token").remove("username").remove("display_name").remove("role")
            .remove("business_id").remove("business_name").apply()
        _session.value = null
        reload()
    }

    // ---------- creator credits ----------

    private val _credits = MutableStateFlow(
        prefs.getString("credits", null)?.let { runCatching { AppJson.decodeFromString<Credits>(it) }.getOrNull() } ?: Credits()
    )
    val credits: StateFlow<Credits> = _credits.asStateFlow()
    private val _canEditCredits = MutableStateFlow(false)
    val canEditCredits: StateFlow<Boolean> = _canEditCredits.asStateFlow()

    suspend fun refreshCredits() {
        val r = api.credits(_session.value?.token)
        r.credits?.let { c ->
            _credits.value = c
            prefs.edit().putString("credits", AppJson.encodeToString(Credits.serializer(), c)).apply()
        }
        _canEditCredits.value = r.canEdit
    }

    suspend fun saveCredits(credits: Credits) {
        api.setCredits(requireToken(), credits)
        _credits.value = credits
        prefs.edit().putString("credits", AppJson.encodeToString(Credits.serializer(), credits)).apply()
    }

    fun logout() {
        val token = _session.value?.token
        stopSessionWork()
        prefs.edit().remove("token").remove("username").remove("display_name").remove("role")
            .remove("business_name").apply()
        _session.value = null
        if (token != null) scope.launch { api.logout(token) }
    }

    private fun expireSession() {
        stopSessionWork()
        prefs.edit().remove("token").apply()
        _session.value = null
    }

    // ---------- team ----------

    suspend fun team(): List<TeamMember> = api.team(requireToken())
    suspend fun addMember(displayName: String, username: String, password: String) =
        api.addMember(requireToken(), displayName.trim(), username.trim(), password)
    suspend fun removeMember(username: String) = api.removeMember(requireToken(), username)

    private fun requireToken(): String = _session.value?.token ?: throw ApiException("Sin sesión", 401)

    // ---------- local data ----------

    @Synchronized
    private fun reload() {
        val rows = db.allRows()
        val products = ArrayList<Product>()
        val customers = ArrayList<Customer>()
        val sales = ArrayList<Sale>()
        val payments = ArrayList<Payment>()
        var rate = RateSetting()
        val dirty = HashSet<String>()
        for (row in rows) {
            if (row.dirty) dirty += "${row.kind}:${row.id}"
            if (row.deleted) continue
            runCatching {
                when (row.kind) {
                    Kinds.PRODUCT -> products += AppJson.decodeFromString<Product>(row.data)
                    Kinds.CUSTOMER -> customers += AppJson.decodeFromString<Customer>(row.data)
                    Kinds.SALE -> sales += AppJson.decodeFromString<Sale>(row.data)
                    Kinds.PAYMENT -> payments += AppJson.decodeFromString<Payment>(row.data)
                    Kinds.SETTING -> if (row.id == RATE_ID) rate = AppJson.decodeFromString<RateSetting>(row.data)
                }
            }
        }
        _data.value = AppData(
            products = products.sortedBy { it.name.lowercase() },
            customers = customers.sortedBy { it.name.lowercase() },
            sales = sales.sortedByDescending { it.createdAt },
            payments = payments.sortedByDescending { it.createdAt },
            rate = rate,
            dirtyKeys = dirty,
            loaded = true,
        )
        _sync.update { it.copy(pending = dirty.size) }
    }

    private fun writeLocal(kind: String, id: String, json: String, deleted: Boolean = false) {
        db.putLocal(kind, id, json, deleted)
        reload()
        scope.launch { syncNow() }
    }

    fun newId(): String = UUID.randomUUID().toString()

    fun saveProduct(product: Product) =
        writeLocal(Kinds.PRODUCT, product.id, AppJson.encodeToString(Product.serializer(), product))

    fun deleteProduct(product: Product) =
        writeLocal(Kinds.PRODUCT, product.id, AppJson.encodeToString(Product.serializer(), product), deleted = true)

    fun saveCustomer(customer: Customer) =
        writeLocal(Kinds.CUSTOMER, customer.id, AppJson.encodeToString(Customer.serializer(), customer))

    fun saveSale(sale: Sale) = writeLocal(Kinds.SALE, sale.id, AppJson.encodeToString(Sale.serializer(), sale))

    fun savePayment(payment: Payment) =
        writeLocal(Kinds.PAYMENT, payment.id, AppJson.encodeToString(Payment.serializer(), payment))

    fun saveRate(setting: RateSetting) =
        writeLocal(Kinds.SETTING, RATE_ID, AppJson.encodeToString(RateSetting.serializer(), setting))

    // ---------- BCV ----------

    fun refreshBcv() {
        if (_bcv.value.loading) return
        _bcv.update { it.copy(loading = true, error = false) }
        scope.launch {
            try {
                val (rate, date) = api.fetchBcvRate()
                prefs.edit().putFloat("bcv_rate", rate.toFloat()).putString("bcv_date", date).apply()
                _bcv.value = BcvState(rate = rate, updated = date)
            } catch (e: Exception) {
                _bcv.update { it.copy(loading = false, error = true) }
            }
        }
    }

    // ---------- sync ----------

    suspend fun syncNow() {
        val session = _session.value ?: return
        syncMutex.withLock {
            _sync.update { it.copy(syncing = true) }
            try {
                var guard = 0
                while (guard++ < 50) {
                    val since = db.getMeta("cursor")?.toLongOrNull() ?: 0L
                    val dirty = db.dirtyRows(300)
                    val response = api.sync(
                        session.token,
                        SyncRequest(since, dirty.map { SyncChange(it.kind, it.id, it.data, it.updatedAt, it.deleted) })
                    )
                    db.applySync(dirty, response)
                    reload()
                    if (!response.more && db.pendingCount() == 0) break
                    if (!response.more && dirty.isEmpty()) break
                }
                _sync.update { it.copy(online = true, lastSyncAt = System.currentTimeMillis()) }
            } catch (e: ApiException) {
                if (e.status == 401) expireSession()
            } catch (e: IOException) {
                _sync.update { it.copy(online = false) }
            } catch (e: Exception) {
                // keep data locally, retry later
            } finally {
                _sync.update { it.copy(syncing = false, pending = db.pendingCount()) }
            }
        }
    }

    private fun connectSocket() {
        socketJob?.cancel()
        socketJob = scope.launch {
            var backoff = 2_000L
            while (isActive && _session.value != null) {
                val token = _session.value?.token ?: break
                val closed = kotlinx.coroutines.CompletableDeferred<Unit>()
                val ws = api.openSocket(token, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        backoff = 2_000L
                        _sync.update { it.copy(realtime = true, online = true) }
                        scope.launch { syncNow() }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        if (text.contains("changed")) scope.launch { syncNow() }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        _sync.update { it.copy(realtime = false) }
                        closed.complete(Unit)
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        _sync.update { it.copy(realtime = false) }
                        if (response?.code == 401) expireSession()
                        closed.complete(Unit)
                    }
                })
                socket = ws
                closed.await()
                socket = null
                delay(backoff)
                backoff = (backoff * 2).coerceAtMost(30_000L)
            }
        }
    }
}
