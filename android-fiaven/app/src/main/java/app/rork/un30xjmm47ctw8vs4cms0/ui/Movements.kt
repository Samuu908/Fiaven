package app.rork.un30xjmm47ctw8vs4cms0.ui

import app.rork.un30xjmm47ctw8vs4cms0.data.AppData
import app.rork.un30xjmm47ctw8vs4cms0.data.Kinds
import app.rork.un30xjmm47ctw8vs4cms0.data.Payment
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Period(val label: String) { DIA("Día"), MES("Mes"), ANIO("Año") }

enum class MoveFilter(val label: String) { TODO("Todo"), CONTADO("Contado"), FIADO("Fiados"), ABONO("Abonos") }

private val es = Locale("es", "VE")

private fun cal(millis: Long) = Calendar.getInstance().apply { timeInMillis = millis }

/** Start of the day/month/year that contains [anchor]. */
fun periodStart(anchor: Long, period: Period): Long = cal(anchor).apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    if (period != Period.DIA) set(Calendar.DAY_OF_MONTH, 1)
    if (period == Period.ANIO) set(Calendar.MONTH, Calendar.JANUARY)
}.timeInMillis

/** Moves [start] by [steps] periods. */
fun shiftPeriod(start: Long, period: Period, steps: Int): Long = cal(start).apply {
    add(
        when (period) { Period.DIA -> Calendar.DAY_OF_MONTH; Period.MES -> Calendar.MONTH; Period.ANIO -> Calendar.YEAR },
        steps,
    )
}.timeInMillis

fun periodLabel(start: Long, period: Period): String {
    val today = periodStart(System.currentTimeMillis(), period)
    val raw = when (period) {
        Period.DIA -> when (start) {
            today -> "Hoy, " + SimpleDateFormat("d 'de' MMMM", es).format(Date(start))
            shiftPeriod(today, period, -1) -> "Ayer, " + SimpleDateFormat("d 'de' MMMM", es).format(Date(start))
            else -> SimpleDateFormat("EEEE d 'de' MMMM yyyy", es).format(Date(start))
        }
        Period.MES -> SimpleDateFormat("MMMM yyyy", es).format(Date(start))
        Period.ANIO -> SimpleDateFormat("yyyy", es).format(Date(start))
    }
    return raw.replaceFirstChar { it.uppercase() }
}

/** Bucket bounds used for the bar chart: hours blocks for a day, days for a month, months for a year. */
fun periodBuckets(start: Long, period: Period): List<Long> = when (period) {
    // 6 blocks of 4 hours
    Period.DIA -> (0..6).map { cal(start).apply { add(Calendar.HOUR_OF_DAY, it * 4) }.timeInMillis }
    Period.MES -> {
        val days = cal(start).getActualMaximum(Calendar.DAY_OF_MONTH)
        (0..days).map { cal(start).apply { add(Calendar.DAY_OF_MONTH, it) }.timeInMillis }
    }
    Period.ANIO -> (0..12).map { cal(start).apply { add(Calendar.MONTH, it) }.timeInMillis }
}

fun bucketLabel(bucketStart: Long, period: Period): String = when (period) {
    Period.DIA -> SimpleDateFormat("HH", es).format(Date(bucketStart)) + "h"
    Period.MES -> cal(bucketStart).get(Calendar.DAY_OF_MONTH).toString()
    Period.ANIO -> SimpleDateFormat("MMM", es).format(Date(bucketStart)).take(3).replace(".", "").replaceFirstChar { it.uppercase() }
}

fun dayHeader(dayStart: Long): String {
    val today = startOfToday()
    val label = when (dayStart) {
        today -> "Hoy"
        shiftPeriod(today, Period.DIA, -1) -> "Ayer"
        else -> SimpleDateFormat("EEEE d 'de' MMMM", es).format(Date(dayStart))
    }
    return label.replaceFirstChar { it.uppercase() }
}

fun saleActivity(sale: Sale, me: String?, dirty: Set<String>): ActivityItem {
    val what = when {
        sale.items.size == 1 -> sale.items[0].let { "${it.name} x${it.qty}" }
        sale.items.isEmpty() -> "Venta"
        else -> "${sale.items.size} productos"
    }
    val title = if (sale.isFiado) "Fiado · ${sale.customerName ?: "Cliente"} · $what" else "Contado · $what"
    return ActivityItem(
        "s:${sale.id}", false, title, if (sale.authorUser == me) "Tú" else sale.authorName, sale.createdAt, sale.totalCents,
        "${Kinds.SALE}:${sale.id}" in dirty, sale.isFiado,
    )
}

fun paymentActivity(p: Payment, me: String?, dirty: Set<String>): ActivityItem = ActivityItem(
    "p:${p.id}", true, "Abono · ${p.customerName}", if (p.authorUser == me) "Tú" else p.authorName, p.createdAt, p.amountCents,
    "${Kinds.PAYMENT}:${p.id}" in dirty, false,
)

/** All non-voided operations in [start, end), newest first. */
fun movementsIn(d: AppData, start: Long, end: Long, filter: MoveFilter, me: String?): List<ActivityItem> = buildList {
    if (filter != MoveFilter.ABONO) {
        for (s in d.sales) {
            if (s.voided || s.createdAt < start || s.createdAt >= end) continue
            if (filter == MoveFilter.CONTADO && s.isFiado) continue
            if (filter == MoveFilter.FIADO && !s.isFiado) continue
            add(saleActivity(s, me, d.dirtyKeys))
        }
    }
    if (filter == MoveFilter.TODO || filter == MoveFilter.ABONO) {
        for (p in d.payments) {
            if (p.voided || p.createdAt < start || p.createdAt >= end) continue
            add(paymentActivity(p, me, d.dirtyKeys))
        }
    }
}.sortedByDescending { it.createdAt }
