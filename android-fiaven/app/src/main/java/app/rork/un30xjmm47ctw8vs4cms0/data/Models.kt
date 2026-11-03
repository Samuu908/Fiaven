package app.rork.un30xjmm47ctw8vs4cms0.data

import kotlinx.serialization.Serializable

object Kinds {
    const val PRODUCT = "product"
    const val CUSTOMER = "customer"
    const val SALE = "sale"
    const val PAYMENT = "payment"
    const val SETTING = "setting"
    const val NOTE = "note"
    const val CLOSING = "closing"
    const val QUOTE = "quote"
    const val SUPPLIER = "supplier"
    const val PURCHASE = "purchase"
    const val SUPPLIER_PAYMENT = "spayment"
    const val EXPENSE = "expense"
    const val ORDER = "order"
}

/** Ways a customer can pay; stored as the visible label. */
val PAY_METHODS = listOf(
    "Efectivo $", "Efectivo Bs", "Pago móvil", "Transferencia", "Binance", "Zelle", "Punto de venta", "Moneda extranjera",
)

/** Money that left the business (rent, delivery, lunch…). Amount always in USD cents. */
@Serializable
data class Expense(
    val id: String,
    val amountCents: Long,
    val currency: String = "USD",
    val originalAmount: Double = 0.0,
    val rate: Double = 0.0,
    val category: String = "Otros",
    val description: String = "",
    val method: String = "",
    val authorUser: String,
    val authorName: String,
    val createdAt: Long,
    val voided: Boolean = false,
)

/** Customer order to deliver on a date. Amounts in USD cents. */
@Serializable
data class Order(
    val id: String,
    val customerName: String,
    val customerPhone: String = "",
    val description: String,
    val totalCents: Long = 0,
    val depositCents: Long = 0,
    /** Start of the delivery day (local time). */
    val dueAt: Long,
    val status: String = "pending", // pending | delivered | cancelled
    val note: String = "",
    val authorName: String,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
)

@Serializable
data class PayQr(val id: String, val label: String, val attachmentId: String)

@Serializable
data class PagoMovil(val id: String, val bank: String, val phone: String, val cedula: String, val holder: String = "")

/** Free account (Zelle, Binance, cuenta bancaria…): label + value to copy. */
@Serializable
data class PayAccount(val id: String, val label: String, val value: String, val holder: String = "")

/** Owner-defined payment details shown to customers. */
@Serializable
data class PayData(
    val qrs: List<PayQr> = emptyList(),
    val mobile: List<PagoMovil> = emptyList(),
    val accounts: List<PayAccount> = emptyList(),
)

/** WhatsApp numbers that receive the weekly summary. */
@Serializable
data class SummarySetting(val phones: List<String> = emptyList(), val set: Boolean = false)

/** Someone the business buys from. */
@Serializable
data class Supplier(
    val id: String,
    val name: String,
    val phone: String = "",
    /** RIF stored as "J-12345678-9" (free text). */
    val rif: String = "",
    val note: String = "",
    val createdAt: Long,
)

/** A purchase invoice or debt owed to a supplier. Amount always in USD cents. */
@Serializable
data class Purchase(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val amountCents: Long,
    val currency: String = "USD", // "USD" | "BS"
    val originalAmount: Double = 0.0,
    val rate: Double = 0.0,
    val invoiceNumber: String = "",
    val note: String = "",
    /** Ids of photos (invoice) stored on the server. */
    val attachments: List<String> = emptyList(),
    val authorUser: String,
    val authorName: String,
    val createdAt: Long,
    val voided: Boolean = false,
)

/** A payment (abono) made to a supplier, with optional proof (transfer screenshot, receipt). */
@Serializable
data class SupplierPayment(
    val id: String,
    val supplierId: String,
    val supplierName: String,
    val amountCents: Long,
    val currency: String = "USD",
    val originalAmount: Double = 0.0,
    val rate: Double = 0.0,
    val method: String = "", // Efectivo, Transferencia, Pago móvil, Zelle, Otro
    val reference: String = "",
    val note: String = "",
    val attachments: List<String> = emptyList(),
    val authorUser: String,
    val authorName: String,
    val createdAt: Long,
    val voided: Boolean = false,
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val priceCents: Long,
    val createdAt: Long,
    /** Optional purchase price (USD cents), used to calculate profit. */
    val costCents: Long? = null,
    /** Barcode / EAN printed on the product, used by the scanner. */
    val barcode: String? = null,
    /** Units counted on hand at [stockSince]; null = not tracked. */
    val stock: Int? = null,
    val stockSince: Long? = null,
    /** Warn when stock reaches this many units (default 5). */
    val minStock: Int? = null,
)

/** Free-form note shared by the whole business. */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val authorName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean = false,
)

/** End-of-day cash closing. id = day key "yyyy-MM-dd". */
@Serializable
data class Closing(
    val id: String,
    val dayStart: Long,
    val cashSalesCents: Long,
    val paymentsCents: Long,
    val fiadoCents: Long,
    val salesCount: Int,
    val expectedCents: Long,
    val countedCents: Long? = null,
    val note: String = "",
    val rate: Double = 0.0,
    val authorUser: String,
    val authorName: String,
    val createdAt: Long,
)

/** Price quote (presupuesto) sent to a customer as an invoice-like image. */
@Serializable
data class Quote(
    val id: String,
    val number: Int,
    val customerName: String,
    val customerPhone: String = "",
    val customerCedula: String = "",
    val items: List<SaleItem>,
    val totalCents: Long,
    val rate: Double,
    val note: String = "",
    val validDays: Int = 7,
    val authorName: String,
    val createdAt: Long,
)

@Serializable
data class InventorySetting(val enabled: Boolean = false)

/** Business photo set by the owner; a small square JPEG as base64. Synced to every phone of the business. */
@Serializable
data class BusinessPhoto(val data: String = "", val updatedAt: Long = 0, val authorName: String = "")

val BACKUP_KINDS = setOf(
    Kinds.PRODUCT, Kinds.CUSTOMER, Kinds.SALE, Kinds.PAYMENT, Kinds.SETTING, Kinds.NOTE, Kinds.CLOSING, Kinds.QUOTE,
    Kinds.SUPPLIER, Kinds.PURCHASE, Kinds.SUPPLIER_PAYMENT, Kinds.EXPENSE, Kinds.ORDER,
)

@Serializable
data class BackupRecord(val kind: String, val id: String, val data: String, val updatedAt: Long)

/** File written by "Descargar respaldo". */
@Serializable
data class Backup(
    val app: String = "Kashly",
    val version: Int = 1,
    val businessId: String = "",
    val businessName: String = "",
    val exportedAt: Long = 0,
    val records: List<BackupRecord> = emptyList(),
)

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val phone: String = "",
    val createdAt: Long,
    /** Cédula or RIF, stored as "V-12345678". */
    val cedula: String = "",
)

@Serializable
data class SaleItem(
    val productId: String? = null,
    val name: String,
    val priceCents: Long,
    val qty: Int,
    /** Purchase price per unit at the moment of the sale, if known. */
    val costCents: Long? = null,
)

@Serializable
data class Sale(
    val id: String,
    val type: String, // "contado" | "fiado"
    val items: List<SaleItem>,
    val totalCents: Long,
    val rate: Double,
    val customerId: String? = null,
    val customerName: String? = null,
    val authorUser: String,
    val authorName: String,
    val createdAt: Long,
    val voided: Boolean = false,
    val customerCedula: String? = null,
    val customerPhone: String? = null,
    /** How it was paid ("Pago móvil", "Efectivo $ + Pago móvil"…). */
    val payMethod: String? = null,
    /** Mixed payment: what was received in each currency. */
    val paidUsdCents: Long? = null,
    val paidBsCents: Long? = null,
    /** Name printed as "Atendido por" on the invoice. */
    val attendedBy: String? = null,
) {
    val isFiado: Boolean get() = type == "fiado"
}

@Serializable
data class Payment(
    val id: String,
    val customerId: String,
    val customerName: String,
    val amountCents: Long, // always USD cents
    val currency: String, // "USD" | "BS"
    val originalAmount: Double,
    val rate: Double,
    val authorUser: String,
    val authorName: String,
    val createdAt: Long,
    val voided: Boolean = false,
    val method: String = "",
)

@Serializable
data class RateSetting(
    val mode: String = "custom", // "bcv" | "custom"
    val customRate: Double = 0.0,
    val updatedBy: String = "",
)

@Serializable
data class SessionUser(val username: String, val displayName: String, val role: String, val title: String = "")

@Serializable
data class SessionBusiness(val id: String, val name: String)

@Serializable
data class AuthResponse(val token: String, val user: SessionUser, val business: SessionBusiness)

@Serializable
data class TeamMember(val username: String, val displayName: String, val role: String, val title: String = "") {
    val roleLabel: String get() = title.ifBlank { if (role == "owner") "Dueño" else "Empleado" }
}

/** Response of GET /me. */
@Serializable
data class MeResponse(val username: String = "", val displayName: String = "", val role: String = "", val title: String = "")

@Serializable
data class TeamResponse(val users: List<TeamMember>)

@Serializable
data class SyncChange(
    val kind: String,
    val id: String,
    val data: String,
    val updatedAt: Long,
    val deleted: Boolean,
)

@Serializable
data class SyncRequest(val since: Long, val changes: List<SyncChange>)

@Serializable
data class SyncRecord(
    val kind: String,
    val id: String,
    val data: String,
    val updatedAt: Long,
    val deleted: Boolean,
    val author: String = "",
)

@Serializable
data class RecordKey(val kind: String, val id: String)

@Serializable
data class SyncResponse(
    val cursor: Long,
    val more: Boolean,
    val records: List<SyncRecord>,
    val rejected: List<RecordKey> = emptyList(),
)

@Serializable
data class ErrorResponse(val error: String = "")

@Serializable
data class QuestionResponse(val question: String = "")

@Serializable
data class SecurityInfo(val question: String? = null, val hasQuestion: Boolean = false)

/** One social network of the app creator ("Creado por ..."). */
@Serializable
data class CreditLink(val network: String, val handle: String)

@Serializable
data class Credits(
    val name: String = "Samuel Martinez",
    val links: List<CreditLink> = listOf(CreditLink("instagram", "Samuuj_m")),
)

@Serializable
data class CreditsResponse(val credits: Credits? = null, val canEdit: Boolean = false)

data class Session(
    val token: String,
    val username: String,
    val displayName: String,
    val role: String,
    val businessId: String,
    val businessName: String,
    /** Visible job title (Administrador, Gerente…); permissions still come from [role]. */
    val title: String = "",
) {
    val isOwner: Boolean get() = role == "owner"
    val roleLabel: String get() = title.ifBlank { if (isOwner) "Dueño" else "Empleado" }
}

/** A local row plus its sync flag. */
data class LocalRow(
    val kind: String,
    val id: String,
    val data: String,
    val updatedAt: Long,
    val deleted: Boolean,
    val dirty: Boolean,
)
