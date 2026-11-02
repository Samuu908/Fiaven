package app.rork.un30xjmm47ctw8vs4cms0.ui

import app.rork.un30xjmm47ctw8vs4cms0.data.AppData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Totals for one calendar day. All amounts are USD cents. */
data class DaySummary(
    val start: Long,
    val salesCount: Int = 0,
    /** Everything sold that day (contado + fiado). */
    val soldCents: Long = 0,
    val contadoCents: Long = 0,
    val fiadoCents: Long = 0,
    val abonosCents: Long = 0,
    /** Sum of purchase prices of items that have one. */
    val costCents: Long = 0,
    /** Sale value of items without a purchase price (profit unknown for those). */
    val uncostedCents: Long = 0,
) {
    /** Money actually received: contado sales + abonos. */
    val cashInCents: Long get() = contadoCents + abonosCents
    /** Free money ("ganancia") on items with a known purchase price. */
    val profitCents: Long get() = soldCents - uncostedCents - costCents
    val hasCosts: Boolean get() = soldCents - uncostedCents > 0

    operator fun plus(o: DaySummary) = DaySummary(
        start, salesCount + o.salesCount, soldCents + o.soldCents, contadoCents + o.contadoCents,
        fiadoCents + o.fiadoCents, abonosCents + o.abonosCents, costCents + o.costCents, uncostedCents + o.uncostedCents,
    )
}

data class WeekSummary(val start: Long, val days: List<DaySummary>) {
    val total: DaySummary get() = days.fold(DaySummary(start)) { acc, d -> acc + d }
}

private val esLocale = Locale("es", "VE")

/** Monday 00:00 of the week containing [millis], shifted by [offsetWeeks]. */
fun weekStart(millis: Long = System.currentTimeMillis(), offsetWeeks: Int = 0): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        val back = (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        add(Calendar.DAY_OF_MONTH, -back)
        add(Calendar.WEEK_OF_YEAR, offsetWeeks)
    }.timeInMillis

private fun addDays(start: Long, days: Int): Long =
    Calendar.getInstance().apply { timeInMillis = start; add(Calendar.DAY_OF_MONTH, days) }.timeInMillis

fun buildWeek(d: AppData, start: Long): WeekSummary =
    WeekSummary(start, buildBuckets(d, (0..7).map { addDays(start, it) }))

/** Totals per bucket; [bounds] are ascending starts with the final end appended (n buckets = n+1 bounds). */
fun buildBuckets(d: AppData, bounds: List<Long>): List<DaySummary> {
    val n = bounds.size - 1
    val days = Array(n) { DaySummary(bounds[it]) }
    fun index(t: Long): Int = if (t < bounds[0] || t >= bounds[n]) -1 else (0 until n).first { t < bounds[it + 1] }

    for (sale in d.sales) {
        if (sale.voided) continue
        val i = index(sale.createdAt)
        if (i < 0) continue
        var cost = 0L
        var uncosted = 0L
        for (item in sale.items) {
            val line = item.priceCents * item.qty
            if (item.costCents != null) cost += item.costCents * item.qty else uncosted += line
        }
        val day = days[i]
        days[i] = day.copy(
            salesCount = day.salesCount + 1,
            soldCents = day.soldCents + sale.totalCents,
            contadoCents = day.contadoCents + if (sale.isFiado) 0 else sale.totalCents,
            fiadoCents = day.fiadoCents + if (sale.isFiado) sale.totalCents else 0,
            costCents = day.costCents + cost,
            uncostedCents = day.uncostedCents + uncosted,
        )
    }
    for (p in d.payments) {
        if (p.voided) continue
        val i = index(p.createdAt)
        if (i < 0) continue
        days[i] = days[i].copy(abonosCents = days[i].abonosCents + p.amountCents)
    }
    return days.toList()
}

fun weekLabel(start: Long): String {
    val end = addDays(start, 6)
    val f = SimpleDateFormat("d MMM", esLocale)
    return "${f.format(Date(start))} – ${f.format(Date(end))}".replace(".", "")
}

fun dayName(start: Long): String =
    SimpleDateFormat("EEEE d", esLocale).format(Date(start)).replaceFirstChar { it.uppercase() }

fun dayInitial(start: Long): String =
    SimpleDateFormat("EEE", esLocale).format(Date(start)).take(1).uppercase()
