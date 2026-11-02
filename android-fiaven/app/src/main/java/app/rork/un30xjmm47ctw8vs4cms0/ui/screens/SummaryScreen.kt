package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.DaySummary
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.buildWeek
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.dayInitial
import app.rork.un30xjmm47ctw8vs4cms0.ui.dayName
import app.rork.un30xjmm47ctw8vs4cms0.ui.startOfToday
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyHuge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import app.rork.un30xjmm47ctw8vs4cms0.ui.weekLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.weekStart
import android.content.Intent
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import app.rork.un30xjmm47ctw8vs4cms0.ui.SUMMARY_RECIPIENTS
import app.rork.un30xjmm47ctw8vs4cms0.ui.WeekSummary
import app.rork.un30xjmm47ctw8vs4cms0.ui.prettyPhone
import app.rork.un30xjmm47ctw8vs4cms0.ui.summaryMessage
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true
    val haptic = LocalHapticFeedback.current
    var offset by rememberSaveable { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf<Long?>(null) }

    val start = remember(offset) { weekStart(offsetWeeks = offset) }
    val week = remember(data, start) { buildWeek(data, start) }
    val total = week.total
    val today = startOfToday()
    val rate = ledger.effectiveRate

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Resumen semanal") },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); offset-- }) {
                        Icon(Icons.Filled.ChevronLeft, "Semana anterior", tint = FV.Ink)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(weekLabel(start), style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                        Text(
                            when (offset) { 0 -> "Esta semana"; -1 -> "Semana pasada"; else -> "Lunes a domingo" },
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                    }
                    IconButton(
                        enabled = offset < 0,
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); offset++ },
                    ) { Icon(Icons.Filled.ChevronRight, "Semana siguiente", tint = if (offset < 0) FV.Ink else FV.Hairline) }
                }
            }

            item {
                FvCard {
                    Text("Vendido en la semana", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                    Text("Contado + fiado · sin anuladas", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    Spacer(Modifier.height(6.dp))
                    AnimatedContent(total.soldCents, label = "weekSold") { Text(usd(it), style = MoneyHuge, color = FV.Ink) }
                    Text(bs(total.soldCents, rate), style = MaterialTheme.typography.titleMedium, color = FV.Muted)
                    Spacer(Modifier.height(16.dp))
                    WeekBars(week.days, today, isOwner)
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = FV.Hairline)
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Stat("Entró en caja", usd(total.cashInCents), FV.Green, Modifier.weight(1f))
                        Stat("Fiado", usd(total.fiadoCents), FV.AmberText, Modifier.weight(1f))
                        Stat("Ventas", total.salesCount.toString(), FV.Ink, Modifier.weight(1f))
                    }
                }
            }

            if (isOwner) {
                item { ProfitCard(total, rate) }
            }

            item { SendSummaryCard(week, session?.businessName.orEmpty(), rate, isOwner) }

            item { SectionHeader("Día por día") }

            item {
                FvCard(contentPadding = PaddingValues(0.dp)) {
                    week.days.forEachIndexed { i, day ->
                        if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 16.dp))
                        DayRow(
                            day = day,
                            isToday = day.start == today,
                            isFuture = day.start > today,
                            isOwner = isOwner,
                            open = expanded == day.start,
                            onClick = { expanded = if (expanded == day.start) null else day.start },
                        )
                    }
                }
            }

            if (!isOwner) {
                item {
                    Text(
                        "La ganancia solo la ve el dueño del negocio.",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfitCard(total: DaySummary, rate: Double) {
    FvCard(container = FV.GreenTint, border = FV.Green.copy(alpha = 0.35f)) {
        Text("Dinero libre (ganancia)", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
        Text("Lo vendido menos lo que te costó", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
        Spacer(Modifier.height(6.dp))
        if (total.hasCosts) {
            AnimatedContent(total.profitCents, label = "weekProfit") {
                Text(
                    usd(it), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold,
                    color = if (it >= 0) FV.Green else FV.Danger,
                )
            }
            Text(bs(total.profitCents, rate), style = MaterialTheme.typography.titleMedium, color = FV.Muted)
            Spacer(Modifier.height(8.dp))
            Text(
                "Costo de lo vendido: ${usd(total.costCents)}",
                style = MaterialTheme.typography.bodyMedium, color = FV.Ink,
            )
        } else {
            Text("—", style = MaterialTheme.typography.displaySmall, color = FV.Muted)
        }
        if (total.uncostedCents > 0) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Info, null, tint = FV.AmberText, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${usd(total.uncostedCents)} vendidos no tienen precio de compra y no cuentan en la ganancia. " +
                        "Agrégalo en Catálogo para las próximas ventas.",
                    style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                )
            }
        }
    }
}

@Composable
private fun WeekBars(days: List<DaySummary>, today: Long, isOwner: Boolean) {
    val max = (days.maxOfOrNull { it.soldCents } ?: 0L).coerceAtLeast(1L)
    Row(
        Modifier.fillMaxWidth().height(96.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { d ->
            val sold by animateFloatAsState(d.soldCents.toFloat() / max, label = "bar")
            val profit by animateFloatAsState(
                if (isOwner && d.hasCosts) (d.profitCents.coerceAtLeast(0).toFloat() / max) else 0f, label = "profit",
            )
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier.fillMaxWidth().fillMaxHeight(sold.coerceAtLeast(0.03f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (d.start == today) FV.Green.copy(alpha = 0.35f) else FV.Hairline),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (profit > 0f) {
                            Box(
                                Modifier.fillMaxWidth().fillMaxHeight((profit / sold.coerceAtLeast(0.0001f)).coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(6.dp)).background(FV.Green),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    dayInitial(d.start), style = MaterialTheme.typography.labelSmall,
                    color = if (d.start == today) FV.Green else FV.Muted,
                    fontWeight = if (d.start == today) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
    if (isOwner) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Legend(FV.Hairline, "Vendido")
            Spacer(Modifier.width(14.dp))
            Legend(FV.Green, "Ganancia")
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

@Composable
private fun DayRow(day: DaySummary, isToday: Boolean, isFuture: Boolean, isOwner: Boolean, open: Boolean, onClick: () -> Unit) {
    val empty = day.salesCount == 0 && day.abonosCents == 0L
    Column {
        ClickableRow(onClick = { if (!empty) onClick() }) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dayName(day.start), style = MaterialTheme.typography.bodyLarge, color = if (isFuture) FV.Muted else FV.Ink)
                    if (isToday) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Hoy", style = MaterialTheme.typography.labelSmall, color = FV.Green,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(FV.GreenTint).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    when {
                        empty && isFuture -> "Por venir"
                        empty -> "Sin movimiento"
                        else -> "${day.salesCount} ${if (day.salesCount == 1) "venta" else "ventas"} · caja ${usd(day.cashInCents)}"
                    },
                    style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    usd(day.soldCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = if (empty) FV.Muted else FV.Ink,
                )
                if (isOwner && day.hasCosts) {
                    Text(
                        "${if (day.profitCents >= 0) "+" else "−"}${usd(kotlin.math.abs(day.profitCents))} libre",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (day.profitCents >= 0) FV.Green else FV.Danger,
                    )
                }
            }
            if (!empty) {
                Spacer(Modifier.width(4.dp))
                Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = FV.Muted)
            }
        }
        AnimatedVisibility(open, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Line("Ventas de contado", usd(day.contadoCents))
                Line("Ventas fiadas", usd(day.fiadoCents), FV.AmberText)
                Line("Abonos cobrados", usd(day.abonosCents))
                Line("Entró en caja", usd(day.cashInCents), FV.Green, bold = true)
                if (isOwner) {
                    HorizontalDivider(color = FV.Hairline)
                    Line("Costo de lo vendido", if (day.hasCosts) usd(day.costCents) else "—")
                    Line(
                        "Dinero libre", if (day.hasCosts) usd(day.profitCents) else "—",
                        if (day.profitCents >= 0) FV.Green else FV.Danger, bold = true,
                    )
                    if (day.uncostedCents > 0) {
                        Text(
                            "${usd(day.uncostedCents)} sin precio de compra",
                            style = MaterialTheme.typography.bodySmall, color = FV.AmberText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Line(label: String, value: String, color: Color = FV.Ink, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = FV.Muted, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
    }
}

/** Opens WhatsApp on [phone]'s chat with [text] already written. */
fun openWhatsAppChat(context: android.content.Context, phone: String, text: String) {
    val uri = "https://wa.me/$phone?text=${android.net.Uri.encode(text)}".toUri()
    val apps = listOf("com.whatsapp", "com.whatsapp.w4b")
    val opened = apps.any { pkg ->
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage(pkg)) }.isSuccess
    }
    if (!opened) runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}

@Composable
private fun SendSummaryCard(week: WeekSummary, businessName: String, rate: Double, isOwner: Boolean) {
    val context = LocalContext.current
    var weekend by rememberSaveable { mutableStateOf(true) }
    val days = if (weekend) week.days.takeLast(2) else week.days
    val period = if (weekend) "${dayName(days.first().start)} y ${dayName(days.last().start).replaceFirstChar { it.lowercase() }}"
    else weekLabel(week.start)
    val sold = days.sumOf { it.soldCents }

    FvCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.AutoMirrored.Filled.Send, FV.GreenDark, FV.GreenSoft)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Enviar resumen por WhatsApp", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                Text("$period · ${usd(sold)} vendido", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(selected = weekend, onClick = { weekend = true }, shape = SegmentedButtonDefaults.itemShape(0, 2)) {
                Text("Fin de semana")
            }
            SegmentedButton(selected = !weekend, onClick = { weekend = false }, shape = SegmentedButtonDefaults.itemShape(1, 2)) {
                Text("Semana completa")
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SUMMARY_RECIPIENTS.forEach { phone ->
                OutlinedButton(
                    onClick = {
                        val text = summaryMessage(
                            businessName,
                            if (weekend) "Ventas del fin de semana" else "Ventas de la semana",
                            period, days, rate, includeProfit = isOwner,
                        )
                        openWhatsAppChat(context, phone, text)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Enviar a ${prettyPhone(phone)}", color = FV.GreenDark) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Se abre WhatsApp con el mensaje listo; solo toca enviar.",
            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
        )
    }
}
