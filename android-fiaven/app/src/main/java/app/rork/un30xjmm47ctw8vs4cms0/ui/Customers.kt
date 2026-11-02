package app.rork.un30xjmm47ctw8vs4cms0.ui

import app.rork.un30xjmm47ctw8vs4cms0.data.Customer
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale

/**
 * Phone in a comparable form: digits only, "+58 414…" and "0414…" become the same.
 * Returns "" when it is too short to identify anyone.
 */
fun phoneKey(phone: String): String {
    var d = phone.filter { it.isDigit() }
    if (d.startsWith("58") && d.length >= 12) d = "0" + d.drop(2)
    if (!d.startsWith("0") && d.length == 10) d = "0$d"
    return if (d.length >= 7) d else ""
}

private fun cedulaKey(cedula: String): String = cedulaDigits(cedula).takeIf { it.length >= 5 }.orEmpty()

/**
 * Customers that are the same person are treated as one (same cédula, or the same phone when
 * one of them has no cédula). Records stay untouched; sales and abonos of every duplicate are
 * counted under the [canonicalOf] id, so their debts add up into a single account.
 */
class CustomerGroups(
    private val canonical: Map<String, String>,
    /** canonical id → every customer record of that person. */
    val members: Map<String, List<Customer>>,
    /** canonical id → merged view (name, best phone and cédula). */
    val merged: Map<String, Customer>,
) {
    fun canonicalOf(id: String): String = canonical[id] ?: id
    fun memberIds(id: String): Set<String> =
        members[canonicalOf(id)]?.map { it.id }?.toSet() ?: setOf(id)
    val people: List<Customer> get() = merged.values.toList()

    /** The person with this cédula, if any. */
    fun byCedula(cedula: String): Customer? {
        val k = cedulaKey(cedula)
        if (k.isEmpty()) return null
        return merged.values.firstOrNull { cedulaKey(it.cedula) == k }
    }

    /** The person with this phone who has no different cédula (callers also check the name). */
    fun byPhone(phone: String, cedula: String = ""): Customer? {
        val k = phoneKey(phone)
        if (k.isEmpty()) return null
        val ck = cedulaKey(cedula)
        return merged.values.firstOrNull { c ->
            phoneKey(c.phone) == k && (ck.isEmpty() || cedulaKey(c.cedula).let { it.isEmpty() || it == ck })
        }
    }

    companion object {
        val EMPTY = CustomerGroups(emptyMap(), emptyMap(), emptyMap())

        fun of(customers: List<Customer>): CustomerGroups {
            if (customers.isEmpty()) return EMPTY
            val sorted = customers.sortedBy { it.createdAt }
            val groups = LinkedHashMap<String, MutableList<Customer>>() // group key → members
            val byCed = HashMap<String, String>()   // cédula → group key
            val byPhone = HashMap<String, String>() // phone → group key (first group seen)
            // 1) Everyone with a cédula: the cédula decides.
            for (c in sorted) {
                val ck = cedulaKey(c.cedula)
                if (ck.isEmpty()) continue
                val g = byCed.getOrPut(ck) { "c:$ck" }
                groups.getOrPut(g) { mutableListOf() }.add(c)
                phoneKey(c.phone).takeIf { it.isNotEmpty() }?.let { byPhone.putIfAbsent(it, g) }
            }
            // 2) Without cédula: join whoever already has the same phone and first name
            //    (a shared family phone alone is not enough).
            for (c in sorted) {
                if (cedulaKey(c.cedula).isNotEmpty()) continue
                val pk = phoneKey(c.phone)
                val known = pk.takeIf { it.isNotEmpty() }?.let { byPhone[it] }
                val g = when {
                    known != null && groups[known].orEmpty().any { sameFirstName(it.name, c.name) } -> known
                    pk.isNotEmpty() && known == null -> "p:$pk".also { byPhone[pk] = it }
                    else -> "id:${c.id}"
                }
                groups.getOrPut(g) { mutableListOf() }.add(c)
            }
            val canonical = HashMap<String, String>()
            val members = LinkedHashMap<String, List<Customer>>()
            val merged = LinkedHashMap<String, Customer>()
            for (list in groups.values) {
                // Oldest record with a cédula is the main one.
                val main = list.firstOrNull { cedulaKey(it.cedula).isNotEmpty() } ?: list.first()
                for (m in list) canonical[m.id] = main.id
                members[main.id] = list
                merged[main.id] = main.copy(
                    phone = main.phone.ifBlank { list.lastOrNull { it.phone.isNotBlank() }?.phone.orEmpty() },
                    cedula = main.cedula.ifBlank { list.firstOrNull { it.cedula.isNotBlank() }?.cedula.orEmpty() },
                    createdAt = list.minOf { it.createdAt },
                )
            }
            return CustomerGroups(canonical, members, merged)
        }
    }
}

/** What a customer buys and how often, counting contado and fiado sales. */
data class ClientStats(
    val customer: Customer,
    val purchases: Int,
    val spentCents: Long,
    val firstPurchase: Long?,
    val lastPurchase: Long?,
    /** Distinct days with a purchase. */
    val visits: Int,
    val purchasesLast30: Int,
    val owedCents: Long = 0,
) {
    /** Average days between visits, null with fewer than 2 visits. */
    val everyDays: Int?
        get() = if (visits >= 2 && firstPurchase != null && lastPurchase != null)
            (((lastPurchase - firstPurchase) / DAY_MS.toDouble()) / (visits - 1)).let { kotlin.math.round(it).toInt().coerceAtLeast(1) }
        else null
    val frequent: Boolean get() = purchasesLast30 >= 4

    val frequencyLabel: String
        get() {
            val e = everyDays
            return when {
                purchases == 0 -> "Sin compras todavía"
                e == null -> "Una sola visita"
                e <= 1 -> "Compra casi a diario"
                e in 6..8 -> "Compra cada semana"
                e in 13..16 -> "Compra cada 2 semanas"
                e in 28..33 -> "Compra cada mes"
                else -> "Compra cada ~$e días"
            }
        }
}

fun buildClientStats(groups: CustomerGroups, sales: List<Sale>, owed: Map<String, Long>): List<ClientStats> {
    val now = System.currentTimeMillis()
    val byPerson = sales.filter { !it.voided && it.customerId != null }.groupBy { groups.canonicalOf(it.customerId!!) }
    return groups.merged.map { (id, c) ->
        val list = byPerson[id].orEmpty()
        ClientStats(
            customer = c,
            purchases = list.size,
            spentCents = list.sumOf { it.totalCents },
            firstPurchase = list.minOfOrNull { it.createdAt },
            lastPurchase = list.maxOfOrNull { it.createdAt },
            visits = list.map { dayKey(it.createdAt) }.distinct().size,
            purchasesLast30 = list.count { now - it.createdAt <= 30 * DAY_MS },
            owedCents = owed[id] ?: 0,
        )
    }
}

/** "hoy", "ayer", "hace 5 días". */
fun agoLabel(millis: Long): String {
    val d = daysSince(millis)
    return when {
        d <= 0 -> "hoy"
        d == 1 -> "ayer"
        else -> "hace $d días"
    }
}

/** "maria perez" and "María" share the first name (accents and case ignored). */
fun sameFirstName(a: String, b: String): Boolean {
    fun first(s: String) = java.text.Normalizer.normalize(s.trim().lowercase(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").split(Regex("\\s+")).firstOrNull().orEmpty()
    val x = first(a); val y = first(b)
    return x.isNotEmpty() && x == y
}
