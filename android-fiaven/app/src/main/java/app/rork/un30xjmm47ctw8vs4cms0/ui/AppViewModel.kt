package app.rork.un30xjmm47ctw8vs4cms0.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.rork.un30xjmm47ctw8vs4cms0.data.AppData
import app.rork.un30xjmm47ctw8vs4cms0.data.BcvState
import app.rork.un30xjmm47ctw8vs4cms0.data.Customer
import app.rork.un30xjmm47ctw8vs4cms0.data.Kinds
import app.rork.un30xjmm47ctw8vs4cms0.data.Payment
import app.rork.un30xjmm47ctw8vs4cms0.data.Product
import app.rork.un30xjmm47ctw8vs4cms0.data.RateSetting
import app.rork.un30xjmm47ctw8vs4cms0.data.Repository
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale
import app.rork.un30xjmm47ctw8vs4cms0.data.SaleItem
import app.rork.un30xjmm47ctw8vs4cms0.data.SecurityInfo
import app.rork.un30xjmm47ctw8vs4cms0.data.Credits
import app.rork.un30xjmm47ctw8vs4cms0.data.Session
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomerBalance(
    val customerId: String,
    val name: String,
    val phone: String,
    val fiadoCents: Long,
    val paidCents: Long,
    val lastActivity: Long,
) {
    val owedCents: Long get() = (fiadoCents - paidCents).coerceAtLeast(0)
}

data class ActivityItem(
    val key: String,
    val isPayment: Boolean,
    val title: String,
    val author: String,
    val createdAt: Long,
    val amountCents: Long,
    val pending: Boolean,
    val fiado: Boolean,
)

data class Ledger(
    val effectiveRate: Double = 0.0,
    val rateIsBcv: Boolean = false,
    val cashTodayCents: Long = 0,
    val cashAllCents: Long = 0,
    val fiadoOutstandingCents: Long = 0,
    val balances: List<CustomerBalance> = emptyList(),
    val activity: List<ActivityItem> = emptyList(),
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val repo = Repository.get(application).also { it.start() }

    val session: StateFlow<Session?> = repo.session
    val data: StateFlow<AppData> = repo.data
    val sync = repo.sync
    val bcv: StateFlow<BcvState> = repo.bcv

    val ledger: StateFlow<Ledger> = combine(repo.data, repo.bcv, repo.session) { d, b, s ->
        buildLedger(d, b, s)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Ledger())

    private fun buildLedger(d: AppData, b: BcvState, s: Session?): Ledger {
        val useBcv = d.rate.mode == "bcv" || d.rate.customRate <= 0.0
        val rate = if (useBcv && b.rate > 0) b.rate else if (d.rate.customRate > 0) d.rate.customRate else b.rate
        val today = startOfToday()
        val sales = d.sales.filter { !it.voided }
        val payments = d.payments.filter { !it.voided }

        val cashAll = sales.filter { !it.isFiado }.sumOf { it.totalCents } + payments.sumOf { it.amountCents }
        val cashToday = sales.filter { !it.isFiado && it.createdAt >= today }.sumOf { it.totalCents } +
            payments.filter { it.createdAt >= today }.sumOf { it.amountCents }

        val customersById = d.customers.associateBy { it.id }
        val map = LinkedHashMap<String, CustomerBalance>()
        fun base(id: String, fallbackName: String): CustomerBalance {
            val c = customersById[id]
            return map[id] ?: CustomerBalance(id, c?.name ?: fallbackName, c?.phone.orEmpty(), 0, 0, c?.createdAt ?: 0)
        }
        for (sale in sales.filter { it.isFiado && it.customerId != null }) {
            val b0 = base(sale.customerId!!, sale.customerName ?: "Cliente")
            map[sale.customerId] = b0.copy(fiadoCents = b0.fiadoCents + sale.totalCents, lastActivity = maxOf(b0.lastActivity, sale.createdAt))
        }
        for (p in payments) {
            val b0 = base(p.customerId, p.customerName)
            map[p.customerId] = b0.copy(paidCents = b0.paidCents + p.amountCents, lastActivity = maxOf(b0.lastActivity, p.createdAt))
        }
        val balances = map.values.sortedWith(compareByDescending<CustomerBalance> { it.owedCents > 0 }.thenByDescending { it.lastActivity })

        val me = s?.username
        val activity = buildList {
            for (sale in sales.take(60)) add(saleActivity(sale, me, d.dirtyKeys))
            for (p in payments.take(60)) add(paymentActivity(p, me, d.dirtyKeys))
        }.sortedByDescending { it.createdAt }.take(60)

        return Ledger(
            effectiveRate = rate,
            rateIsBcv = useBcv,
            cashTodayCents = cashToday,
            cashAllCents = cashAll,
            fiadoOutstandingCents = balances.sumOf { it.owedCents },
            balances = balances,
            activity = activity,
        )
    }

    // ---------- actions ----------

    fun login(user: String, pass: String, onError: (String) -> Unit) = viewModelScope.launch {
        try { repo.login(user, pass) } catch (e: Exception) { onError(friendly(e)) }
    }

    fun register(
        business: String, name: String, user: String, pass: String, question: String, answer: String, onError: (String) -> Unit,
    ) = viewModelScope.launch {
        try { repo.register(business, name, user, pass, question, answer) } catch (e: Exception) { onError(friendly(e)) }
    }

    // ---------- account security & creator credits ----------

    val credits = repo.credits
    val canEditCredits = repo.canEditCredits

    /** null = unknown yet (offline or loading). */
    private val _security = kotlinx.coroutines.flow.MutableStateFlow<SecurityInfo?>(null)
    val security: StateFlow<SecurityInfo?> = _security

    fun refreshAccount() = viewModelScope.launch {
        runCatching { repo.refreshCredits() }
        if (session.value != null) runCatching { repo.security() }.onSuccess { _security.value = it }
        else _security.value = null
    }

    val privacyUrl: String get() = repo.privacyUrl
    val deleteAccountUrl: String get() = repo.deleteAccountUrl

    fun recoveryQuestion(user: String, onOk: (String) -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try { onOk(repo.recoveryQuestion(user)) } catch (e: Exception) { onError(friendly(e)) }
    }

    fun recover(user: String, answer: String, newPass: String, onError: (String) -> Unit) = viewModelScope.launch {
        try { repo.recover(user, answer, newPass) } catch (e: Exception) { onError(friendly(e)) }
    }

    fun setSecurity(password: String, question: String, answer: String, onOk: () -> Unit, onError: (String) -> Unit) =
        viewModelScope.launch {
            try {
                repo.setSecurity(password, question, answer)
                _security.value = SecurityInfo(question, true)
                onOk()
            } catch (e: Exception) { onError(friendly(e)) }
        }

    fun deleteAccount(password: String, onError: (String) -> Unit) = viewModelScope.launch {
        try { repo.deleteAccount(password) } catch (e: Exception) { onError(friendly(e)) }
    }

    fun saveCredits(credits: Credits, onOk: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try { repo.saveCredits(credits); onOk() } catch (e: Exception) { onError(friendly(e)) }
    }

    init {
        viewModelScope.launch {
            var last: String? = "init"
            session.collect { s -> if (s?.token != last) { last = s?.token; _security.value = null; refreshAccount() } }
        }
    }

    fun logout() = repo.logout()

    fun friendly(e: Exception): String = when (e) {
        is app.rork.un30xjmm47ctw8vs4cms0.data.ApiException -> e.message ?: "Error"
        is java.io.IOException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
        else -> e.message ?: "Algo salió mal"
    }

    fun saveSale(
        items: List<SaleItem>,
        fiado: Boolean,
        customerName: String,
        customerPhone: String,
        existingCustomerId: String?,
    ) {
        val s = session.value ?: return
        val now = System.currentTimeMillis()
        var customerId: String? = null
        var name: String? = null
        if (fiado) {
            val existing = existingCustomerId?.let { id -> data.value.customers.find { it.id == id } }
                ?: data.value.customers.find { it.name.equals(customerName.trim(), ignoreCase = true) }
            val customer = if (existing != null) {
                if (customerPhone.isNotBlank() && customerPhone.trim() != existing.phone) existing.copy(phone = customerPhone.trim()).also { repo.saveCustomer(it) } else existing
            } else {
                Customer(repo.newId(), customerName.trim(), customerPhone.trim(), now).also { repo.saveCustomer(it) }
            }
            customerId = customer.id
            name = customer.name
        }
        repo.saveSale(
            Sale(
                id = repo.newId(),
                type = if (fiado) "fiado" else "contado",
                items = items,
                totalCents = items.sumOf { it.priceCents * it.qty },
                rate = ledger.value.effectiveRate,
                customerId = customerId,
                customerName = name,
                authorUser = s.username,
                authorName = s.displayName,
                createdAt = now,
            )
        )
    }

    fun addPayment(balance: CustomerBalance, amountCents: Long, currency: String, original: Double) {
        val s = session.value ?: return
        repo.savePayment(
            Payment(
                id = repo.newId(),
                customerId = balance.customerId,
                customerName = balance.name,
                amountCents = amountCents,
                currency = currency,
                originalAmount = original,
                rate = ledger.value.effectiveRate,
                authorUser = s.username,
                authorName = s.displayName,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    fun updateCustomer(id: String, name: String, phone: String) {
        val existing = data.value.customers.find { it.id == id }
        val c = existing?.copy(name = name.trim(), phone = phone.trim())
            ?: Customer(id, name.trim(), phone.trim(), System.currentTimeMillis())
        repo.saveCustomer(c)
    }

    fun voidSale(sale: Sale) = repo.saveSale(sale.copy(voided = true))
    fun voidPayment(payment: Payment) = repo.savePayment(payment.copy(voided = true))

    fun saveProduct(id: String?, name: String, priceCents: Long, costCents: Long?) {
        val existing = id?.let { pid -> data.value.products.find { it.id == pid } }
        repo.saveProduct(
            existing?.copy(name = name.trim(), priceCents = priceCents, costCents = costCents)
                ?: Product(repo.newId(), name.trim(), priceCents, System.currentTimeMillis(), costCents)
        )
    }

    fun deleteProduct(product: Product) = repo.deleteProduct(product)

    fun saveRate(mode: String, custom: Double) {
        val s = session.value ?: return
        repo.saveRate(RateSetting(mode = mode, customRate = custom, updatedBy = s.displayName))
    }

    fun refreshBcv() = repo.refreshBcv()
    fun syncNow() = viewModelScope.launch { repo.syncNow() }
}
