package app.rork.un30xjmm47ctw8vs4cms0.ui

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val symbols = DecimalFormatSymbols(Locale.US).apply {
    decimalSeparator = ','
    groupingSeparator = '.'
}

private val moneyFormat = DecimalFormat("#,##0.00", symbols)

fun formatNumber(value: Double): String = synchronized(moneyFormat) { moneyFormat.format(value) }

fun usd(cents: Long): String = "$ " + formatNumber(cents / 100.0)

fun bs(cents: Long, rate: Double): String = "Bs " + formatNumber(cents / 100.0 * rate)

fun bsAmount(value: Double): String = "Bs " + formatNumber(value)

/** Accepts "1,60", "1.60", "1.234,50" or "1234.5". Returns null when invalid. */
fun parseDecimal(raw: String): BigDecimal? {
    var s = raw.trim().replace(" ", "")
    if (s.isEmpty()) return null
    if (s.contains(',') && s.contains('.')) {
        s = if (s.lastIndexOf(',') > s.lastIndexOf('.')) s.replace(".", "").replace(',', '.') else s.replace(",", "")
    } else {
        s = s.replace(',', '.')
    }
    return s.toBigDecimalOrNull()?.takeIf { it.signum() >= 0 }
}

fun parseCents(raw: String): Long? =
    parseDecimal(raw)?.setScale(2, RoundingMode.HALF_UP)?.movePointRight(2)?.toLong()

fun centsToInput(cents: Long): String = formatNumber(cents / 100.0).replace(".", "")

fun timeLabel(millis: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
    val pattern = if (sameDay) "HH:mm" else "dd/MM · HH:mm"
    return SimpleDateFormat(pattern, Locale("es")).format(Date(millis))
}

fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun initials(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
