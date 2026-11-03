package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.AppData
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.DAY_MS
import app.rork.un30xjmm47ctw8vs4cms0.ui.StockInfo
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.startOfToday
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

private data class ProductStat(val key: String, val name: String, val units: Int, val cents: Long, val productId: String?)

private data class BuyTip(val name: String, val left: Int?, val perDay: Double, val daysLeft: Double?, val suggest: Int)

private data class Insights(
    val totalCents: Long,
    val salesCount: Int,
    val units: Int,
    val top: List<ProductStat>,
    val bottom: List<ProductStat>,
    val buyMore: List<BuyTip>,
    val buyLess: List<ProductStat>,
    val bestWeekday: String?,
    val bestHour: Int?,
)

/** How many days of stock to keep when suggesting purchases. */
private const val COVER_DAYS = 14

private fun buildInsights(d: AppData, days: Int, stock: Map<String, StockInfo>): Insights {
    val from = startOfToday() - (days - 1) * DAY_MS
    val sales = d.sales.filter { !it.voided && it.createdAt >= from }
    val byKey = LinkedHashMap<String, ProductStat>()
    for (s in sales) for (i in s.items) {
        val key = i.productId ?: ("n:" + i.name.trim().lowercase())
        val prev = byKey[key]
        byKey[key] = ProductStat(key, prev?.name ?: i.name, (prev?.units ?: 0) + i.qty, (prev?.cents ?: 0) + i.priceCents * i.qty, i.productId)
    }
    // Catalog products that didn't sell at all also count as "less sold".
    for (p in d.products) if (!byKey.containsKey(p.id)) byKey[p.id] = ProductStat(p.id, p.name, 0, 0, p.id)
    val catalogNames = d.products.associate { it.id to it.name }
    val stats = byKey.values.map { s -> s.productId?.let { catalogNames[it] }?.let { s.copy(name = it) } ?: s }
    val sold = stats.filter { it.units > 0 }.sortedWith(compareByDescending<ProductStat> { it.units }.thenByDescending { it.cents })
    val inCatalog = stats.filter { it.productId != null && catalogNames.containsKey(it.productId) }
    val bottom = inCatalog.sortedWith(compareBy<ProductStat> { it.units }.thenBy { it.cents }).take(5)
        .filter { b -> sold.take(5).none { it.key == b.key } }

    val buyMore = if (!d.inventory.enabled) {
        sold.take(5).map { BuyTip(it.name, null, it.units / days.toDouble(), null, ceil(it.units / days.toDouble() * COVER_DAYS).toInt()) }
    } else {
        d.products.mapNotNull { p ->
            val left = stock[p.id]?.left ?: return@mapNotNull null
            val units = byKey[p.id]?.units ?: 0
            val perDay = units / days.toDouble()
            val daysLeft = if (perDay > 0) left / perDay else null
            val min = p.minStock ?: app.rork.un30xjmm47ctw8vs4cms0.ui.DEFAULT_MIN_STOCK
            val needs = left <= min || (daysLeft != null && daysLeft < COVER_DAYS)
            if (!needs) null
            else BuyTip(p.name, left, perDay, daysLeft, (ceil(perDay * COVER_DAYS).toInt() - left).coerceAtLeast(if (left <= min) min * 2 - left else 0).coerceAtLeast(1))
        }.sortedBy { it.daysLeft ?: if ((it.left ?: 0) <= 0) -1.0 else 999.0 }.take(8)
    }
    val buyLess = if (!d.inventory.enabled) emptyList() else d.products.mapNotNull { p ->
        val left = stock[p.id]?.left ?: return@mapNotNull null
        val units = byKey[p.id]?.units ?: 0
        val perDay = units / days.toDouble()
        // Stock that would last more than 3 months at the current pace (or never sells).
        if (left > 0 && (perDay == 0.0 || left / perDay > 90)) ProductStat(p.id, p.name, units, left.toLong(), p.id) else null
    }.sortedByDescending { it.cents }.take(5)

    val weekday = sales.groupBy { Calendar.getInstance().apply { timeInMillis = it.createdAt }.get(Calendar.DAY_OF_WEEK) }
        .maxByOrNull { e -> e.value.sumOf { it.totalCents } }?.value?.first()?.createdAt
        ?.let { SimpleDateFormat("EEEE", Locale("es")).format(java.util.Date(it)) }
    val hour = sales.groupBy { Calendar.getInstance().apply { timeInMillis = it.createdAt }.get(Calendar.HOUR_OF_DAY) }
        .maxByOrNull { it.value.size }?.key

    return Insights(
        totalCents = sales.sumOf { it.totalCents }, salesCount = sales.size, units = sold.sumOf { it.units },
        top = sold.take(5), bottom = bottom, buyMore = buyMore, buyLess = buyLess,
        bestWeekday = weekday, bestHour = hour,
    )
}

/** "Modo inteligente": what sells, what doesn't and what to buy. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    var days by rememberSaveable { mutableStateOf(30) }
    val ins = remember(data, ledger.stock, days) { buildInsights(data, days, ledger.stock) }
    val maxUnits = ins.top.maxOfOrNull { it.units } ?: 1

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Modo inteligente") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf(7, 30, 90).forEachIndexed { i, d ->
                        SegmentedButton(selected = days == d, onClick = { days = d }, shape = SegmentedButtonDefaults.itemShape(i, 3)) {
                            Text("$d días")
                        }
                    }
                }
            }

            item {
                FvCard(container = FV.GreenTint, border = FV.GreenSoft) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Vendido en $days días", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                            Text(usd(ins.totalCents), style = MoneyLarge, color = FV.GreenDark)
                        }
                        IconBadge(Icons.Filled.AutoAwesome, FV.GreenDark, FV.GreenSoft)
                    }
                    Text(
                        "${ins.salesCount} ventas · ${ins.units} unidades" +
                            if (ins.salesCount > 0) " · promedio ${usd(ins.totalCents / ins.salesCount)}" else "",
                        style = MaterialTheme.typography.bodyMedium, color = FV.Ink,
                    )
                    if (ins.bestWeekday != null || ins.bestHour != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, null, tint = FV.GreenDark, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                listOfNotNull(
                                    ins.bestWeekday?.let { "Tu mejor día: $it" },
                                    ins.bestHour?.let { "más ventas cerca de las ${"%02d".format(it)}:00" },
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall, color = FV.GreenDark,
                            )
                        }
                    }
                }
            }

            if (ins.salesCount == 0 && data.products.isEmpty()) {
                item {
                    EmptyState(Icons.Filled.AutoAwesome, "Aún no hay datos", "Cuando registres ventas con productos del catálogo verás aquí qué se vende más y qué comprar.")
                }
                return@LazyColumn
            }

            item { SectionHeader("Lo que más se vende") }
            item {
                FvCard {
                    if (ins.top.isEmpty()) Text("Sin ventas en este periodo.", color = FV.Muted)
                    ins.top.forEachIndexed { i, p ->
                        if (i > 0) Spacer(Modifier.height(12.dp))
                        RankRow(i + 1, p, p.units / maxUnits.toFloat(), FV.Green)
                    }
                }
            }

            if (ins.bottom.isNotEmpty()) {
                item { SectionHeader("Lo que menos se vende") }
                item {
                    FvCard {
                        ins.bottom.forEachIndexed { i, p ->
                            if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(vertical = 10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.TrendingDown, null, tint = FV.Muted, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(p.name, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text(if (p.units == 0) "No se vendió" else "${p.units} u.", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Qué comprar") }
            item {
                FvCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.AddShoppingCart, FV.GreenDark, FV.GreenSoft, 36)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Comprar más", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                            Text(
                                if (data.inventory.enabled) "Para tener mercancía para $COVER_DAYS días, al ritmo de venta actual."
                                else "Lo que más sale. Activa el inventario en Catálogo para cálculos exactos.",
                                style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    if (ins.buyMore.isEmpty()) Text("Todo bien por ahora: no hace falta reponer nada.", color = FV.Green)
                    ins.buyMore.forEachIndexed { i, t ->
                        if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(vertical = 10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(t.name, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    buyTipLine(t),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if ((t.left ?: 1) <= 0 || (t.daysLeft ?: 99.0) < 3) FV.AmberText else FV.Muted,
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(FV.GreenSoft).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text("+${t.suggest}", style = MaterialTheme.typography.labelLarge, color = FV.GreenDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (ins.buyLess.isNotEmpty()) {
                item {
                    FvCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Filled.RemoveShoppingCart, FV.Muted, FV.Neutral, 36)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Comprar menos", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                                Text("Tienes más de lo que se vende. Espera antes de reponer.", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        ins.buyLess.forEachIndexed { i, p ->
                            if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(vertical = 10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.name, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Text("Quedan ${p.cents} · vendidas ${p.units}", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buyTipLine(t: BuyTip): String {
    val pace = if (t.perDay >= 1) "vendes ~${"%.0f".format(t.perDay)} al día" else if (t.perDay > 0) "vendes ~${"%.0f".format(t.perDay * 7)} a la semana" else "sin ventas recientes"
    return when {
        t.left == null -> pace.replaceFirstChar { it.uppercase() }
        t.left <= 0 -> "Agotado · $pace"
        t.daysLeft != null -> "Quedan ${t.left} · alcanza para ~${maxOf(1, t.daysLeft.toInt())} días"
        else -> "Quedan ${t.left} · $pace"
    }
}

@Composable
private fun RankRow(rank: Int, p: ProductStat, frac: Float, color: Color) {
    val animated by animateFloatAsState(frac.coerceIn(0.03f, 1f), tween(600), label = "bar")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(if (rank == 1) FV.Green else FV.GreenSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text("$rank", style = MaterialTheme.typography.labelLarge, color = if (rank == 1) FV.Surface else FV.GreenDark, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.name, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.TrendingUp, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("${p.units} u. · ${usd(p.cents)}", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(FV.Neutral)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(animated).clip(RoundedCornerShape(4.dp)).background(color))
            }
        }
    }
}
