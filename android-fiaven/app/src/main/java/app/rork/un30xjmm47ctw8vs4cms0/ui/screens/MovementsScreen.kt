package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.ActivityItem
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.DaySummary
import app.rork.un30xjmm47ctw8vs4cms0.ui.MoveFilter
import app.rork.un30xjmm47ctw8vs4cms0.ui.Period
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.bucketLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.buildBuckets
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.dayHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.movementsIn
import app.rork.un30xjmm47ctw8vs4cms0.ui.periodBuckets
import app.rork.un30xjmm47ctw8vs4cms0.ui.periodLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.periodStart
import app.rork.un30xjmm47ctw8vs4cms0.ui.shiftPeriod
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import java.util.Calendar
import java.util.TimeZone

/** Local midnight → the UTC-midnight millis the Material date picker expects (and back). */
private fun localToPicker(local: Long): Long {
    val l = Calendar.getInstance().apply { timeInMillis = local }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun pickerToLocal(utc: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
    return Calendar.getInstance().apply {
        clear(); set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun dayOf(millis: Long): Long = periodStart(millis, Period.DIA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementsScreen(vm: AppViewModel, onBack: () -> Unit, onOpenCustomer: (String) -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var period by rememberSaveable { mutableStateOf(Period.DIA) }
    var start by rememberSaveable { mutableLongStateOf(periodStart(System.currentTimeMillis(), Period.DIA)) }
    var filter by rememberSaveable { mutableStateOf(MoveFilter.TODO) }
    var picking by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ActivityItem?>(null) }

    val end = shiftPeriod(start, period, 1)
    val isCurrent = start == periodStart(System.currentTimeMillis(), period)
    val buckets = remember(data, start, period) { buildBuckets(data, periodBuckets(start, period)) }
    val total = remember(buckets) { buckets.fold(DaySummary(start)) { acc, d -> acc + d } }
    val moves = remember(data, start, end, filter, session) { movementsIn(data, start, end, filter, session?.username) }
    val groups = remember(moves, period) {
        if (period == Period.DIA) listOf(start to moves) else moves.groupBy { dayOf(it.createdAt) }.toList()
    }

    fun setPeriod(p: Period, anchor: Long = start) {
        period = p
        start = periodStart(anchor, p)
    }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Movimientos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = { IconButton(onClick = { picking = true }) { Icon(Icons.Filled.CalendarMonth, "Elegir fecha") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "period") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Period.entries.forEachIndexed { i, p ->
                        SegmentedButton(
                            selected = period == p,
                            onClick = { setPeriod(p) },
                            shape = SegmentedButtonDefaults.itemShape(i, Period.entries.size),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = FV.GreenSoft, activeContentColor = FV.GreenDark),
                        ) { Text(p.label) }
                    }
                }
            }

            item(key = "nav") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { start = shiftPeriod(start, period, -1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior")
                    }
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { picking = true }.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedContent(periodLabel(start, period), transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "label") {
                            Text(it, style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                        }
                        if (!isCurrent) {
                            Text(
                                "Volver a hoy", style = MaterialTheme.typography.labelMedium, color = FV.GreenDark,
                                modifier = Modifier.clickable { start = periodStart(System.currentTimeMillis(), period) },
                            )
                        }
                    }
                    IconButton(onClick = { start = shiftPeriod(start, period, 1) }, enabled = !isCurrent) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Siguiente")
                    }
                }
            }

            item(key = "totals") {
                FvCard(Modifier.animateContentSize()) {
                    Text("Vendido", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                    AnimatedContent(total.soldCents, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "sold") {
                        Text(usd(it), style = MoneyLarge, color = FV.Ink)
                    }
                    Text(bs(total.soldCents, ledger.effectiveRate) + " a la tasa de hoy", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    Spacer(Modifier.height(14.dp))
                    Bars(buckets, period, onPick = { b ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (period) {
                            Period.MES -> setPeriod(Period.DIA, b)
                            Period.ANIO -> setPeriod(Period.MES, b)
                            Period.DIA -> Unit
                        }
                    })
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = FV.Hairline)
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Stat("Entró en caja", usd(total.cashInCents), FV.Green, Modifier.weight(1f))
                        Stat("Fiado", usd(total.fiadoCents), FV.AmberText, Modifier.weight(1f))
                        Stat("Abonos", usd(total.abonosCents), FV.Ink, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${total.salesCount} ${if (total.salesCount == 1) "venta" else "ventas"} en este período",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                    )
                }
            }

            item(key = "filters") {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MoveFilter.entries.forEach { f ->
                        FilterChip(
                            selected = filter == f,
                            onClick = { filter = f },
                            label = { Text(f.label) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = FV.Surface,
                                selectedContainerColor = if (f == MoveFilter.FIADO) FV.AmberSoft else FV.GreenSoft,
                                selectedLabelColor = if (f == MoveFilter.FIADO) FV.AmberText else FV.GreenDark,
                            ),
                        )
                    }
                }
            }

            if (moves.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        Icons.Filled.ReceiptLong, "Sin movimientos",
                        "No hay operaciones en este período. Cambia la fecha o el filtro.",
                    )
                }
            } else {
                items(groups, key = { it.first }) { (day, list) ->
                    Column {
                        if (period != Period.DIA) {
                            Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(dayHeader(day), style = MaterialTheme.typography.titleSmall, color = FV.Ink, modifier = Modifier.weight(1f))
                                Text(
                                    usd(list.filter { !it.isPayment }.sumOf { it.amountCents }),
                                    style = MaterialTheme.typography.labelLarge, color = FV.Muted,
                                )
                            }
                        }
                        FvCard(contentPadding = PaddingValues(0.dp)) {
                            list.forEachIndexed { i, a ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 72.dp))
                                ActivityRow(a) { selected = a }
                            }
                        }
                    }
                }
            }
        }
    }

    if (picking) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = localToPicker(start),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = pickerToLocal(utcTimeMillis) <= System.currentTimeMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { setPeriod(period, pickerToLocal(it)) }
                    picking = false
                }) { Text("Ver") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Cancelar", color = FV.Muted) } },
            colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = FV.Surface),
        ) {
            DatePicker(
                state = state,
                title = {
                    Text(
                        when (period) {
                            Period.DIA -> "Elige un día"
                            Period.MES -> "Elige cualquier día del mes"
                            Period.ANIO -> "Elige cualquier día del año"
                        },
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    )
                },
                colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = FV.Surface),
            )
        }
    }

    selected?.let { item ->
        val sale = if (!item.isPayment) data.sales.find { "s:${it.id}" == item.key } else null
        val payment = if (item.isPayment) data.payments.find { "p:${it.id}" == item.key } else null
        ActivityDetailDialog(
            sale = sale, payment = payment, rate = ledger.effectiveRate,
            canVoid = sale?.voided == false || payment?.voided == false,
            onVoid = { sale?.let { vm.voidSale(it) }; payment?.let { vm.voidPayment(it) }; selected = null },
            onOpenCustomer = (sale?.customerId ?: payment?.customerId)?.let { id -> { selected = null; onOpenCustomer(id) } },
            onDismiss = { selected = null },
            businessName = vm.session.value?.businessName.orEmpty(),
        )
    }
}

/** Stacked bars: green = contado + abonos (cash in), amber = fiado. Tap drills down. */
@Composable
private fun Bars(buckets: List<DaySummary>, period: Period, onPick: (Long) -> Unit) {
    val max = (buckets.maxOfOrNull { it.cashInCents + it.fiadoCents } ?: 0L).coerceAtLeast(1L)
    val today = periodStart(System.currentTimeMillis(), Period.DIA)
    val spacing = if (buckets.size > 20) 3.dp else 8.dp
    Row(
        Modifier.fillMaxWidth().height(92.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom,
    ) {
        buckets.forEachIndexed { i, b ->
            val cash by animateFloatAsState(b.cashInCents.toFloat() / max, label = "cash")
            val fiado by animateFloatAsState(b.fiadoCents.toFloat() / max, label = "fiado")
            val highlight = period == Period.MES && b.start == today
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).clickable(enabled = period != Period.DIA) { onPick(b.start) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.fillMaxWidth().height(70.dp), verticalArrangement = Arrangement.Bottom) {
                    val rest = 1f - cash - fiado
                    if (cash + fiado > 0f && rest > 0.001f) Spacer(Modifier.weight(rest))
                    if (fiado > 0f) Box(Modifier.fillMaxWidth().weight(fiado).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(FV.Amber))
                    if (cash > 0f) {
                        Box(
                            Modifier.fillMaxWidth().weight(cash)
                                .clip(RoundedCornerShape(topStart = if (fiado > 0f) 0.dp else 4.dp, topEnd = if (fiado > 0f) 0.dp else 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                .background(if (highlight) FV.GreenDark else FV.Green),
                        )
                    }
                    if (cash + fiado <= 0f) {
                        Box(Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(FV.Hairline))
                    }
                }
                Spacer(Modifier.height(6.dp))
                val showLabel = period != Period.MES || i == 0 || (i + 1) % 5 == 0
                Text(
                    if (showLabel) bucketLabel(b.start, period) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (highlight) FV.Green else FV.Muted,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Legend(FV.Green, "Entró en caja")
        Spacer(Modifier.width(14.dp))
        Legend(FV.Amber, "Fiado")
        if (period != Period.DIA) {
            Spacer(Modifier.weight(1f))
            Text(
                if (period == Period.MES) "Toca un día" else "Toca un mes",
                style = MaterialTheme.typography.labelSmall, color = FV.Muted,
            )
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
    Spacer(Modifier.width(6.dp))
    Text(label, style = MaterialTheme.typography.labelSmall, color = FV.Muted)
}

@Composable
private fun Stat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = FV.Muted)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}
