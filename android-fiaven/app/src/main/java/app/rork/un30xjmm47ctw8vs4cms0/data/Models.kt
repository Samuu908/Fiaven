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
}

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
)

@Serializable
data class RateSetting(
    val mode: String = "custom", // "bcv" | "custom"
    val customRate: Double = 0.0,
    val updatedBy: String = "",
)

@Serializable
data class SessionUser(val username: String, val displayName: String, val role: String)

@Serializable
data class SessionBusiness(val id: String, val name: String)

@Serializable
data class AuthResponse(val token: String, val user: SessionUser, val business: SessionBusiness)

@Serializable
data class TeamMember(val username: String, val displayName: String, val role: String)

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
) {
    val isOwner: Boolean get() = role == "owner"
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
