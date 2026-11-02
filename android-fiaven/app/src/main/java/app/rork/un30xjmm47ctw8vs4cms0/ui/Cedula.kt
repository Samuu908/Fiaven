package app.rork.un30xjmm47ctw8vs4cms0.ui

import app.rork.un30xjmm47ctw8vs4cms0.data.Customer

private val CEDULA_LETTERS = setOf('V', 'E', 'J', 'G', 'P')

/** Stores a cédula/RIF as "V-12345678" (letter + digits). Returns "" when it has no digits. */
fun normalizeCedula(input: String): String {
    val t = input.trim().uppercase()
    val digits = t.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val letter = t.firstOrNull { it.isLetter() }?.takeIf { it in CEDULA_LETTERS } ?: 'V'
    return "$letter-$digits"
}

/** "V-12345678" → "V-12.345.678". RIF-length numbers (J, G) are left without dots. */
fun prettyCedula(cedula: String): String {
    val n = normalizeCedula(cedula)
    if (n.isEmpty()) return ""
    val letter = n.first()
    val digits = n.substringAfter('-')
    if (letter == 'J' || letter == 'G' || digits.length > 8) return n
    return "$letter-" + digits.reversed().chunked(3).joinToString(".").reversed()
}

fun cedulaDigits(s: String): String = s.filter { it.isDigit() }

/** Name, cédula or phone match. Digits in the query are compared ignoring dots and dashes. */
fun customerMatches(name: String, cedula: String, phone: String, query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    if (name.lowercase().contains(q)) return true
    val qd = cedulaDigits(q)
    if (qd.length >= 3) {
        if (cedulaDigits(cedula).contains(qd)) return true
        if (cedulaDigits(phone).contains(qd)) return true
    }
    return false
}

fun Customer.matchesQuery(query: String) = customerMatches(name, cedula, phone, query)

/** Small subtitle: "V-12.345.678 · 0414-1234567". */
fun customerSubtitle(cedula: String, phone: String, empty: String = "Sin teléfono"): String =
    listOfNotNull(prettyCedula(cedula).ifBlank { null }?.let { "C.I. $it" }, phone.ifBlank { null })
        .joinToString("  ·  ").ifBlank { empty }
