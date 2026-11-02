package app.rork.un30xjmm47ctw8vs4cms0.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.rork.un30xjmm47ctw8vs4cms0.data.AppData
import app.rork.un30xjmm47ctw8vs4cms0.data.BcvState
import app.rork.un30xjmm47ctw8vs4cms0.data.Customer
import app.rork.un30xjmm47ctw8vs4cms0.data.Backup
import app.rork.un30xjmm47ctw8vs4cms0.data.Closing
import app.rork.un30xjmm47ctw8vs4cms0.data.InventorySetting
import app.rork.un30xjmm47ctw8vs4cms0.data.BusinessPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.rork.un30xjmm47ctw8vs4cms0.data.Note
import app.rork.un30xjmm47ctw8vs4cms0.data.Quote
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
import app.rork.un30xjmm47ctw8vs4cms0.data.Supplier
import app.rork.un30xjmm47ctw8vs4cms0.data.Purchase
import app.rork.un30xjmm47ctw8vs4cms0.data.SupplierPayment
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
    /** Date of the oldest fiado still unpaid. */
    val debtSince: Long? = null,
    val cedula: String = "",
) {
    val owedCents: Long get() = (fiadoCents - paidCents).coerceAtLeast(0)
    val daysOwed: Int get() = if (owedCents > 0 && debtSince != null) daysSince(debtSince) else 0
    val overdue: Boolean get() = owedCents > 0 && daysOwed > OVERDUE_DAYS
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
    val overdue: List<CustomerBalance> = emptyList(),
    val todayClosing: Closing? = null,
    val stock: Map<String, StockInfo> = emptyMap(),
) {
    val overdueCents: Long get() = overdue.sumOf { it.owedCents }
}

data class SupplierBalance(
    val supplierId: String,
    val name: String,
    val phone: String,
    val rif: String,
    val purchasedCents: Long,
    val paidCents: Long,
    val lastActivity: Long,
) {
    val owedCents: Long get() = (purchasedCents - paidCents).coerceAtLeast(0)
    val creditCents: Long get() = (paidCents - purchasedCents).coerceAtLeast(0)
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    /** Sale in progress; survives closing the sheet by accident. */
    val saleDraft = app.rork.un30xjmm47ctw8vs4cms0.ui.screens.SaleDraft()

    val repo = Repository.get(application).also { it.start() }

    val session: StateFlow<Session?> = repo.session
    val data: StateFlow<AppData> = repo.data
    val sync = repo.sync
    val bcv: StateFlow<BcvState> = repo.bcv

    val ledger: StateFlow<Ledger> = combine(repo.data, repo.bcv, repo.session) { d, b, s ->
        buildLedger(d, b, s)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Ledger())

    val supplierBalances: StateFlow<List<SupplierBalance>> = kotlinx.coroutines.flow.flow {
        repo.data.collect { emit(buildSupplierBalances(it)) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pendingAttachments = repo.pendingAttachments

    private fun buildSupplierBalances(d: AppData): List<SupplierBalance> {
        val purchases = d.purchases.filter { !it.voided }.groupBy { it.supplierId }
        val pays = d.supplierPayments.filter { !it.voided }.groupBy { it.supplierId }
        return d.suppliers.map { s ->
            val pu = purchases[s.id].orEmpty()
            val pa = pays[s.id].orEmpty()
            SupplierBalance(
                s.id, s.name, s.phone, s.rif,
                pu.sumOf { it.amountCents }, pa.sumOf { it.amountCents },
                maxOf(s.createdAt, pu.maxOfOrNull { it.createdAt } ?: 0, pa.maxOfOrNull { it.createdAt } ?: 0),
            )
        }.sortedWith(compareByDescending<SupplierBalance> { it.owedCents > 0 }.thenByDescending { it.owedCents }.thenByDescending { it.lastActivity })
    }

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
            return map[id] ?: CustomerBalance(id, c?.name ?: fallbackName, c?.phone.orEmpty(), 0, 0, c?.createdAt ?: 0, cedula = c?.cedula.orEmpty())
        }
        for (sale in sales.filter { it.isFiado && it.customerId != null }) {
            val b0 = base(sale.customerId!!, sale.customerName ?: "Cliente")
            map[sale.customerId] = b0.copy(fiadoCents = b0.fiadoCents + sale.totalCents, lastActivity = maxOf(b0.lastActivity, sale.createdAt))
        }
        for (p in payments) {
            val b0 = base(p.customerId, p.customerName)
            map[p.customerId] = b0.copy(paidCents = b0.paidCents + p.amountCents, lastActivity = maxOf(b0.lastActivity, p.createdAt))
        }
        val fiadosByCustomer = sales.filter { it.isFiado && it.customerId != null }.groupBy { it.customerId!! }
        val balances = map.values
            .map { it.copy(debtSince = oldestUnpaidSince(fiadosByCustomer[it.customerId].orEmpty(), it.paidCents)) }
            .sortedWith(
                compareByDescending<CustomerBalance> { it.overdue }
                    .thenByDescending { if (it.overdue) it.daysOwed else 0 }
                    .thenByDescending { it.owedCents > 0 }
                    .thenByDescending { it.lastActivity }
            )

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
            overdue = balances.filter { it.overdue },
            todayClosing = d.closings.find { it.id == dayKey(today) },
            stock = stockInfo(d),
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

    fun logout() { saleDraft.clear(); repo.logout() }

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
        customerCedula: String = "",
    ) {
        val s = session.value ?: return
        val now = System.currentTimeMillis()
        var customerId: String? = null
        var name: String? = null
        if (fiado) {
            val ced = normalizeCedula(customerCedula)
            val existing = existingCustomerId?.let { id -> data.value.customers.find { it.id == id } }
                ?: ced.takeIf { it.isNotEmpty() }?.let { c -> data.value.customers.find { cedulaDigits(it.cedula) == cedulaDigits(c) } }
                ?: data.value.customers.find { it.name.equals(customerName.trim(), ignoreCase = true) }
            val customer = if (existing != null) {
                val updated = existing.copy(
                    phone = customerPhone.trim().ifBlank { existing.phone },
                    cedula = ced.ifBlank { existing.cedula },
                )
                if (updated != existing) updated.also { repo.saveCustomer(it) } else existing
            } else {
                Customer(repo.newId(), customerName.trim(), customerPhone.trim(), now, cedula = ced).also { repo.saveCustomer(it) }
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

    fun updateCustomer(id: String, name: String, phone: String, cedula: String) {
        val existing = data.value.customers.find { it.id == id }
        val ced = normalizeCedula(cedula)
        val c = existing?.copy(name = name.trim(), phone = phone.trim(), cedula = ced)
            ?: Customer(id, name.trim(), phone.trim(), System.currentTimeMillis(), cedula = ced)
        repo.saveCustomer(c)
    }

    fun voidSale(sale: Sale) = repo.saveSale(sale.copy(voided = true))
    fun voidPayment(payment: Payment) = repo.savePayment(payment.copy(voided = true))

    /** [newStock] = units on hand right now (recount); null keeps the previous count. */
    fun saveProduct(id: String?, name: String, priceCents: Long, costCents: Long?, barcode: String?, newStock: Int?) {
        val existing = id?.let { pid -> data.value.products.find { it.id == pid } }
        val now = System.currentTimeMillis()
        val code = barcode?.trim()?.ifBlank { null }
        var p = existing?.copy(name = name.trim(), priceCents = priceCents, costCents = costCents, barcode = code)
            ?: Product(repo.newId(), name.trim(), priceCents, now, costCents, barcode = code)
        if (newStock != null) p = p.copy(stock = newStock, stockSince = now)
        repo.saveProduct(p)
    }

    fun productByBarcode(code: String): Product? {
        val c = code.trim()
        return data.value.products.find { it.barcode != null && it.barcode == c }
    }

    fun setInventory(enabled: Boolean) = repo.saveInventory(InventorySetting(enabled))

    // ---------- business photo (owner sets it, every phone receives it by sync) ----------

    fun setBusinessPhoto(uri: android.net.Uri, onDone: (Boolean) -> Unit) {
        val s = session.value ?: return
        if (!s.isOwner) return onDone(false)
        viewModelScope.launch {
            val encoded = withContext(Dispatchers.IO) { runCatching { encodePhoto(getApplication(), uri) }.getOrNull() }
            if (encoded == null) { onDone(false); return@launch }
            repo.savePhoto(BusinessPhoto(encoded, System.currentTimeMillis(), s.displayName.ifBlank { s.username }))
            onDone(true)
        }
    }

    fun removeBusinessPhoto() {
        val s = session.value ?: return
        if (!s.isOwner) return
        repo.savePhoto(BusinessPhoto("", System.currentTimeMillis(), s.displayName), deleted = true)
    }

    // ---------- suppliers ----------

    fun saveSupplier(id: String?, name: String, phone: String, rif: String, note: String): String {
        val existing = id?.let { sid -> data.value.suppliers.find { it.id == sid } }
        val s = existing?.copy(name = name.trim(), phone = phone.trim(), rif = rif.trim().uppercase(), note = note.trim())
            ?: Supplier(repo.newId(), name.trim(), phone.trim(), rif.trim().uppercase(), note.trim(), System.currentTimeMillis())
        repo.saveSupplier(s)
        return s.id
    }

    fun deleteSupplier(id: String) {
        val s = data.value.suppliers.find { it.id == id } ?: return
        repo.saveSupplier(s, deleted = true)
    }

    fun addPurchase(
        supplier: SupplierBalance, cents: Long, currency: String, original: Double,
        invoiceNumber: String, note: String, attachments: List<String>, createdAt: Long,
    ) {
        val s = session.value ?: return
        repo.savePurchase(
            Purchase(
                id = repo.newId(), supplierId = supplier.supplierId, supplierName = supplier.name,
                amountCents = cents, currency = currency, originalAmount = original, rate = ledger.value.effectiveRate,
                invoiceNumber = invoiceNumber.trim(), note = note.trim(), attachments = attachments,
                authorUser = s.username, authorName = s.displayName, createdAt = createdAt,
            )
        )
    }

    fun addSupplierPayment(
        supplier: SupplierBalance, cents: Long, currency: String, original: Double,
        method: String, reference: String, note: String, attachments: List<String>, createdAt: Long,
    ) {
        val s = session.value ?: return
        repo.saveSupplierPayment(
            SupplierPayment(
                id = repo.newId(), supplierId = supplier.supplierId, supplierName = supplier.name,
                amountCents = cents, currency = currency, originalAmount = original, rate = ledger.value.effectiveRate,
                method = method, reference = reference.trim(), note = note.trim(), attachments = attachments,
                authorUser = s.username, authorName = s.displayName, createdAt = createdAt,
            )
        )
    }

    fun voidPurchase(p: Purchase) = repo.savePurchase(p.copy(voided = true))
    fun voidSupplierPayment(p: SupplierPayment) = repo.saveSupplierPayment(p.copy(voided = true))

    /** Compresses the picked image, keeps it on this phone and queues the upload. */
    fun addAttachment(uri: android.net.Uri, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                runCatching { repo.addAttachment(encodeDocument(getApplication(), uri)) }.getOrNull()
            }
            onDone(id)
        }
    }

    suspend fun loadAttachment(id: String): java.io.File? = repo.loadAttachment(id)

    // ---------- team roles ----------

    fun setMemberTitle(username: String, title: String, onOk: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try { repo.setMemberTitle(username, title); onOk() } catch (e: Exception) { onError(friendly(e)) }
    }

    // ---------- cash closing ----------

    fun closeToday(countedCents: Long?, note: String) {
        val s = session.value ?: return
        val today = startOfToday()
        val snap = daySnapshot(data.value, today)
        repo.saveClosing(
            Closing(
                id = dayKey(today),
                dayStart = today,
                cashSalesCents = snap.cashSalesCents,
                paymentsCents = snap.paymentsCents,
                fiadoCents = snap.fiadoCents,
                salesCount = snap.salesCount,
                expectedCents = snap.expectedCents,
                countedCents = countedCents,
                note = note.trim(),
                rate = ledger.value.effectiveRate,
                authorUser = s.username,
                authorName = s.displayName,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    /** Owner only: removes the closing so the day accepts new sales again. */
    fun reopenDay(closing: Closing) = repo.saveClosing(closing, deleted = true)

    // ---------- notes ----------

    fun saveNote(id: String?, title: String, body: String, pinned: Boolean): String {
        val s = session.value
        val now = System.currentTimeMillis()
        val existing = id?.let { nid -> data.value.notes.find { it.id == nid } }
        val note = existing?.copy(title = title.trim(), body = body.trimEnd(), pinned = pinned, updatedAt = now)
            ?: Note(id ?: repo.newId(), title.trim(), body.trimEnd(), s?.displayName.orEmpty(), now, now, pinned)
        repo.saveNote(note)
        return note.id
    }

    fun deleteNote(note: Note) = repo.saveNote(note, deleted = true)

    // ---------- quotes ----------

    fun saveQuote(customerName: String, customerPhone: String, items: List<SaleItem>, note: String, validDays: Int, customerCedula: String = ""): Quote {
        val s = session.value
        val q = Quote(
            id = repo.newId(),
            number = (data.value.quotes.maxOfOrNull { it.number } ?: 0) + 1,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            customerCedula = normalizeCedula(customerCedula),
            items = items,
            totalCents = items.sumOf { it.priceCents * it.qty },
            rate = ledger.value.effectiveRate,
            note = note.trim(),
            validDays = validDays,
            authorName = s?.displayName.orEmpty(),
            createdAt = System.currentTimeMillis(),
        )
        repo.saveQuote(q)
        return q
    }

    fun deleteQuote(quote: Quote) = repo.saveQuote(quote, deleted = true)

    // ---------- backup ----------

    fun exportBackup(): String = repo.exportBackup()
    fun parseBackup(json: String): Backup = repo.parseBackup(json)
    fun importBackup(backup: Backup): Int = repo.importBackup(backup)

    fun deleteProduct(product: Product) = repo.deleteProduct(product)

    fun saveRate(mode: String, custom: Double) {
        val s = session.value ?: return
        repo.saveRate(RateSetting(mode = mode, customRate = custom, updatedBy = s.displayName))
    }

    fun refreshBcv() = repo.refreshBcv()
    fun syncNow() = viewModelScope.launch { repo.syncNow() }
}
