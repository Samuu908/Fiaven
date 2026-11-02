package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Closing
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.daySnapshot
import app.rork.un30xjmm47ctw8vs4cms0.ui.hourLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.longDate
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseCents
import app.rork.un30xjmm47ctw8vs4cms0.ui.startOfToday
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyHuge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosingScreen(vm: AppViewModel, onBack: () -> Unit, onClosed: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val isOwner = session?.isOwner == true
    val today = startOfToday()
    val snap = remember(data, today) { daySnapshot(data, today) }
    val closed = ledger.todayClosing
    val history = data.closings.filter { it.id != closed?.id }

    var counted by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var confirmClose by remember { mutableStateOf(false) }
    var confirmReopen by remember { mutableStateOf(false) }
    val countedCents = if (counted.isBlank()) null else parseCents(counted)

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Cierre de caja") },
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
                Text(longDate(today), style = MaterialTheme.typography.titleMedium, color = FV.Muted)
            }
            item {
                AnimatedContent(closed, label = "closing") { c ->
                    if (c != null) ClosedCard(c, isOwner, onReopen = { confirmReopen = true })
                    else FvCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Efectivo esperado hoy", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                                Text("Contado + abonos", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            }
                            IconBadge(Icons.Filled.PointOfSale, FV.Green, FV.GreenSoft)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(usd(snap.expectedCents), style = MoneyHuge, color = FV.Green)
                        Text(bs(snap.expectedCents, ledger.effectiveRate), style = MaterialTheme.typography.titleMedium, color = FV.Muted)
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = FV.Hairline)
                        Spacer(Modifier.height(10.dp))
                        TotalLine("Ventas de contado", usd(snap.cashSalesCents))
                        TotalLine("Abonos recibidos", usd(snap.paymentsCents), FV.Green)
                        TotalLine("Fiado del día (no entra en caja)", usd(snap.fiadoCents), FV.AmberText)
                        TotalLine("Movimientos", "${snap.salesCount} ventas · ${snap.paymentsCount} abonos")
                    }
                }
            }
            if (closed == null) {
                item {
                    FvCard {
                        Text("Cuenta el efectivo", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                        Text(
                            "Opcional: escribe lo que hay en la gaveta para saber si cuadra.",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            counted, { counted = it },
                            label = { Text("Efectivo contado en $") },
                            singleLine = true,
                            isError = counted.isNotBlank() && countedCents == null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp), colors = fieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        countedCents?.let { DifferenceText(it - snap.expectedCents) }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            note, { note = it },
                            label = { Text("Nota del cierre (opcional)") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            shape = RoundedCornerShape(16.dp), colors = fieldColors(),
                            modifier = Modifier.fillMaxWidth(), minLines = 2,
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { confirmClose = true },
                            enabled = counted.isBlank() || countedCents != null,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                        ) {
                            Icon(Icons.Filled.Lock, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cerrar caja de hoy", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            item { SectionHeader("Cierres anteriores") }
            if (history.isEmpty()) {
                item { EmptyState(Icons.Filled.EventAvailable, "Sin cierres todavía", "Cada día cerrado queda guardado aquí con sus totales.") }
            } else {
                items(history, key = { it.id }) { c -> HistoryCard(c) }
            }
        }
    }

    if (confirmClose) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            containerColor = FV.Surface,
            icon = { Icon(Icons.Filled.Lock, null, tint = FV.Green) },
            title = { Text("¿Cerrar la caja de hoy?") },
            text = {
                Text(
                    "Se guardará el cierre con ${usd(snap.expectedCents)} esperados. Después nadie podrá anotar ventas ni abonos hoy" +
                        if (isOwner) ", a menos que reabras la caja." else "; solo el dueño puede reabrirla."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.closeToday(countedCents, note)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    confirmClose = false
                    counted = ""; note = ""
                    onClosed()
                }) { Text("Cerrar caja") }
            },
            dismissButton = { TextButton(onClick = { confirmClose = false }) { Text("Cancelar") } },
        )
    }
    if (confirmReopen && closed != null) {
        AlertDialog(
            onDismissRequest = { confirmReopen = false },
            containerColor = FV.Surface,
            title = { Text("¿Reabrir la caja?") },
            text = { Text("Se borrará el cierre de hoy y el equipo podrá anotar ventas otra vez. Luego puedes cerrarla de nuevo.") },
            confirmButton = { TextButton(onClick = { vm.reopenDay(closed); confirmReopen = false }) { Text("Reabrir") } },
            dismissButton = { TextButton(onClick = { confirmReopen = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun ClosedCard(c: Closing, isOwner: Boolean, onReopen: () -> Unit) {
    FvCard(container = FV.GreenTint, border = FV.GreenSoft) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Filled.Lock, FV.Surface, FV.Green)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Caja cerrada", style = MaterialTheme.typography.titleMedium, color = FV.GreenDark, fontWeight = FontWeight.SemiBold)
                Text("A las ${hourLabel(c.createdAt)} por ${c.authorName}", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(usd(c.expectedCents), style = MoneyHuge, color = FV.Green)
        Text("Efectivo esperado · ${bs(c.expectedCents, c.rate)}", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = FV.GreenSoft)
        Spacer(Modifier.height(10.dp))
        ClosingLines(c)
        if (isOwner) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onReopen, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.LockOpen, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reabrir caja")
            }
        }
    }
}

@Composable
private fun ClosingLines(c: Closing) {
    TotalLine("Ventas de contado", usd(c.cashSalesCents))
    TotalLine("Abonos recibidos", usd(c.paymentsCents), FV.Green)
    TotalLine("Fiado del día", usd(c.fiadoCents), FV.AmberText)
    c.countedCents?.let {
        TotalLine("Efectivo contado", usd(it))
        DifferenceText(it - c.expectedCents)
    }
    if (c.note.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        Text("“${c.note}”", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
    }
}

@Composable
private fun HistoryCard(c: Closing) {
    FvCard(contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(longDate(c.dayStart), style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                Text("Cerró ${c.authorName} · ${hourLabel(c.createdAt)}", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
            Text(usd(c.expectedCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = FV.Green)
        }
        Spacer(Modifier.height(8.dp))
        ClosingLines(c)
    }
}

@Composable
private fun TotalLine(label: String, value: String, color: Color = FV.Ink) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = FV.Muted, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun DifferenceText(diff: Long) {
    val (text, color) = when {
        diff == 0L -> "La caja cuadra exacto" to FV.Green
        diff > 0 -> "Sobran ${usd(diff)}" to FV.Green
        else -> "Faltan ${usd(-diff)}" to FV.Danger
    }
    Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = color, modifier = Modifier.padding(top = 4.dp))
}
