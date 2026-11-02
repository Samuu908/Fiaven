package app.rork.un30xjmm47ctw8vs4cms0.ui

import app.rork.un30xjmm47ctw8vs4cms0.data.AppData
import app.rork.un30xjmm47ctw8vs4cms0.data.Product
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** A debt older than this many days is "vencida" (shown in red, collected first). */
const val OVERDUE_DAYS = 15
const val DAY_MS = 24L * 60 * 60 * 1000

/** "yyyy-MM-dd" of the local day containing [millis]; used as the id of a cash closing. */
fun dayKey(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

fun dayStartOf(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun longDate(millis: Long): String =
    SimpleDateFormat("EEEE d 'de' MMMM yyyy", Locale("es")).format(Date(millis)).replaceFirstChar { it.uppercase() }

fun shortDate(millis: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale("es")).format(Date(millis))

fun hourLabel(millis: Long): String = SimpleDateFormat("HH:mm", Locale("es")).format(Date(millis))

/**
 * Date of the oldest fiado that abonos have not covered yet (abonos pay the oldest fiado first).
 * Null when the customer owes nothing.
 */
fun oldestUnpaidSince(fiados: List<Sale>, paidCents: Long): Long? {
    var left = paidCents
    for (s in fiados.sortedBy { it.createdAt }) {
        if (left >= s.totalCents) left -= s.totalCents
        else return s.createdAt
    }
    return null
}

fun daysSince(millis: Long, now: Long = System.currentTimeMillis()): Int =
    ((dayStartOf(now) - dayStartOf(millis)) / DAY_MS).toInt().coerceAtLeast(0)

/** Totals of one day, used by "Cierre de caja". */
data class DaySnapshot(
    val dayStart: Long,
    val cashSalesCents: Long,
    val paymentsCents: Long,
    val fiadoCents: Long,
    val salesCount: Int,
    val paymentsCount: Int,
) {
    val expectedCents: Long get() = cashSalesCents + paymentsCents
}

fun daySnapshot(d: AppData, dayStart: Long): DaySnapshot {
    val end = dayStart + DAY_MS
    val sales = d.sales.filter { !it.voided && it.createdAt >= dayStart && it.createdAt < end }
    val pays = d.payments.filter { !it.voided && it.createdAt >= dayStart && it.createdAt < end }
    return DaySnapshot(
        dayStart = dayStart,
        cashSalesCents = sales.filter { !it.isFiado }.sumOf { it.totalCents },
        paymentsCents = pays.sumOf { it.amountCents },
        fiadoCents = sales.filter { it.isFiado }.sumOf { it.totalCents },
        salesCount = sales.size,
        paymentsCount = pays.size,
    )
}

/** Units sold of each product (all time) and units sold since its stock count. */
data class StockInfo(val sold: Int, val left: Int?)

fun stockInfo(d: AppData): Map<String, StockInfo> {
    val products = d.products.associateBy { it.id }
    val soldAll = HashMap<String, Int>()
    val soldSince = HashMap<String, Int>()
    for (s in d.sales) {
        if (s.voided) continue
        for (item in s.items) {
            val id = item.productId ?: continue
            soldAll[id] = (soldAll[id] ?: 0) + item.qty
            val since = products[id]?.stockSince
            if (since != null && s.createdAt >= since) soldSince[id] = (soldSince[id] ?: 0) + item.qty
        }
    }
    return d.products.associate { p ->
        p.id to StockInfo(soldAll[p.id] ?: 0, p.stock?.let { it - (soldSince[p.id] ?: 0) })
    }
}

fun Product.matches(query: String): Boolean {
    val q = query.trim().lowercase()
    return q.isEmpty() || name.lowercase().contains(q) || (barcode != null && barcode.contains(q))
}
