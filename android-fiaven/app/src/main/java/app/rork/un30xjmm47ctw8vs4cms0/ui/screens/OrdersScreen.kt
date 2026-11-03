package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Order
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.DAY_MS
import app.rork.un30xjmm47ctw8vs4cms0.ui.centsToInput
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseCents
import app.rork.un30xjmm47ctw8vs4cms0.ui.startOfToday
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class OrderTab(val label: String, val status: String) {
    PENDING("Pendientes", "pending"), DELIVERED("Entregados", "delivered"), CANCELLED("Cancelados", "cancelled")
}

private fun dueLabel(dueAt: Long): String {
    val today = startOfToday()
    val days = Math.floorDiv(dueAt - today, DAY_MS).toInt()
    return when {
        days < -1 -> "Atrasado ${-days} días"
        days == -1 -> "Era para ayer"
        days == 0 -> "Para hoy"
        days == 1 -> "Para mañana"
        days < 7 -> "Para el " + SimpleDateFormat("EEEE d", Locale("es")).format(Date(dueAt))
        else -> "Para el " + SimpleDateFormat("d 'de' MMMM", Locale("es")).format(Date(dueAt))
    }
}

private fun localDayStart(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun utcToLocalDayStart(utc: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
    return Calendar.getInstance().apply {
        clear(); set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Orders customers asked for, with the day they must be ready. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(OrderTab.PENDING) }
    var editing by remember { mutableStateOf<Order?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Order?>(null) }

    val today = startOfToday()
    val shown = data.orders.filter { it.status == tab.status }.let { l ->
        if (tab == OrderTab.PENDING) l.sortedBy { it.dueAt } else l.sortedByDescending { it.updatedAt }
    }
    val groups: List<Pair<String, List<Order>>> = if (tab != OrderTab.PENDING) listOf("" to shown) else listOf(
        "Atrasados" to shown.filter { it.dueAt < today },
        "Hoy" to shown.filter { it.dueAt in today until today + DAY_MS },
        "Mañana" to shown.filter { it.dueAt in today + DAY_MS until today + 2 * DAY_MS },
        "Próximos" to shown.filter { it.dueAt >= today + 2 * DAY_MS },
    ).filter { it.second.isNotEmpty() }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Pedidos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { creating = true },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nuevo pedido") },
                containerColor = FV.Green, contentColor = FV.Surface,
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    OrderTab.entries.forEachIndexed { i, t ->
                        val n = data.orders.count { it.status == t.status }
                        SegmentedButton(
                            selected = tab == t, onClick = { tab = t },
                            shape = SegmentedButtonDefaults.itemShape(i, OrderTab.entries.size),
                        ) { Text(if (t == OrderTab.PENDING && n > 0) "${t.label} · $n" else t.label, maxLines = 1) }
                    }
                }
            }
            if (shown.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Event,
                        when (tab) { OrderTab.PENDING -> "No hay pedidos pendientes"; OrderTab.DELIVERED -> "Aún no hay entregados"; else -> "Sin cancelados" },
                        if (tab == OrderTab.PENDING) "Anota lo que te encargan con la fecha de entrega y te avisaremos en Caja cuando toque." else "Aquí verás el historial.",
                    )
                }
            }
            groups.forEach { (title, orders) ->
                if (title.isNotEmpty()) item(key = "h:$title") {
                    SectionHeader("$title · ${orders.size}")
                }
                items(orders, key = { it.id }) { o ->
                    OrderCard(
                        o,
                        onClick = { editing = o },
                        onDeliver = { vm.setOrderStatus(o, "delivered") },
                        onWhatsApp = o.customerPhone.takeIf { it.isNotBlank() }?.let { phone ->
                            {
                                val digits = phone.filter { it.isDigit() }.let { if (it.startsWith("0") && it.length == 11) "58" + it.drop(1) else it }
                                val text = "Hola ${o.customerName}, tu pedido (${o.description}) " +
                                    (if (o.status == "pending") "está listo." else "") +
                                    (if (o.totalCents - o.depositCents > 0) " Resta por pagar ${usd(o.totalCents - o.depositCents)}." else "")
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits?text=" + Uri.encode(text))))
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (creating || editing != null) {
        OrderSheet(
            order = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { name, phone, desc, total, deposit, due, note ->
                vm.saveOrder(editing?.id, name, phone, desc, total, deposit, due, note)
                creating = false; editing = null
            },
            onStatus = editing?.let { o -> { s: String -> vm.setOrderStatus(o, s); editing = null } },
            onDelete = editing?.let { o -> { confirmDelete = o; editing = null } },
        )
    }

    confirmDelete?.let { o ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = FV.Surface,
            title = { Text("¿Borrar el pedido?") },
            text = { Text("Se borrará el pedido de ${o.customerName} en todos los teléfonos.", color = FV.Muted) },
            confirmButton = { TextButton(onClick = { vm.deleteOrder(o); confirmDelete = null }) { Text("Borrar", color = FV.Danger) } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun OrderCard(o: Order, onClick: () -> Unit, onDeliver: () -> Unit, onWhatsApp: (() -> Unit)?) {
    val haptic = LocalHapticFeedback.current
    val today = startOfToday()
    val pending = o.status == "pending"
    val late = pending && o.dueAt < today
    val isToday = pending && o.dueAt in today until today + DAY_MS
    val accent: Color = when { late -> FV.Danger; isToday -> FV.AmberText; else -> FV.Muted }
    val rest = (o.totalCents - o.depositCents).coerceAtLeast(0)
    FvCard(
        onClick = onClick,
        container = when { late -> FV.DangerTint; isToday -> FV.AmberTint; else -> FV.Surface },
        border = when { late -> FV.DangerSoft; isToday -> FV.AmberSoft; else -> FV.Hairline },
        contentPadding = PaddingValues(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Event, null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                when (o.status) {
                    "delivered" -> "Entregado"
                    "cancelled" -> "Cancelado"
                    else -> dueLabel(o.dueAt)
                },
                style = MaterialTheme.typography.labelLarge, color = accent, modifier = Modifier.weight(1f),
            )
            Text(app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate(o.dueAt), style = MaterialTheme.typography.labelMedium, color = FV.Muted)
        }
        Spacer(Modifier.height(8.dp))
        Text(o.customerName, style = MaterialTheme.typography.titleMedium, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(o.description, style = MaterialTheme.typography.bodyMedium, color = FV.Ink, maxLines = 3, overflow = TextOverflow.Ellipsis)
        if (o.note.isNotBlank()) Text(o.note, style = MaterialTheme.typography.bodySmall, color = FV.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (o.totalCents > 0) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total ${usd(o.totalCents)}", style = MaterialTheme.typography.bodyMedium, color = FV.Ink, modifier = Modifier.weight(1f))
                if (o.depositCents > 0) Text("Abonó ${usd(o.depositCents)}", style = MaterialTheme.typography.bodySmall, color = FV.Green)
            }
            if (rest > 0) Text("Resta ${usd(rest)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = FV.Ink)
        }
        if (pending) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onDeliver() },
                    shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Entregado")
                }
                if (onWhatsApp != null) {
                    OutlinedButton(onClick = onWhatsApp, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Avisar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderSheet(
    order: Order?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Long, Long, Long, String) -> Unit,
    onStatus: ((String) -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(order?.customerName.orEmpty()) }
    var phone by remember { mutableStateOf(order?.customerPhone.orEmpty()) }
    var description by remember { mutableStateOf(order?.description.orEmpty()) }
    var total by remember { mutableStateOf(order?.totalCents?.takeIf { it > 0 }?.let { centsToInput(it) }.orEmpty()) }
    var deposit by remember { mutableStateOf(order?.depositCents?.takeIf { it > 0 }?.let { centsToInput(it) }.orEmpty()) }
    var note by remember { mutableStateOf(order?.note.orEmpty()) }
    var due by remember { mutableStateOf(order?.dueAt ?: (startOfToday() + DAY_MS)) }
    var pickDate by remember { mutableStateOf(false) }
    val totalCents = if (total.isBlank()) 0L else parseCents(total)
    val depositCents = if (deposit.isBlank()) 0L else parseCents(deposit)
    val valid = name.isNotBlank() && description.isNotBlank() && totalCents != null && depositCents != null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = FV.Surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (order == null) "Nuevo pedido" else "Editar pedido", style = MaterialTheme.typography.titleLarge, color = FV.Ink)
            OutlinedTextField(
                name, { name = it.take(60) }, label = { Text("Cliente") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                phone, { phone = it.filter { c -> c.isDigit() || c in "+ -" }.take(20) }, label = { Text("Teléfono (opcional)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                description, { description = it.take(300) }, label = { Text("¿Qué pidió?") }, minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
            Surface(
                onClick = { pickDate = true }, shape = RoundedCornerShape(16.dp), color = FV.Surface,
                border = BorderStroke(1.dp, FV.Outline), modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = FV.GreenDark)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Fecha de entrega", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                        Text(dueLabel(due), color = FV.Ink, fontWeight = FontWeight.SemiBold)
                    }
                    Text(app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate(due), color = FV.Muted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Hoy" to 0, "Mañana" to 1, "En 3 días" to 3, "En 1 semana" to 7).forEach { (l, d) ->
                    SuggestionChip(onClick = { due = startOfToday() + d * DAY_MS }, label = { Text(l) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    total, { total = it.take(12) }, label = { Text("Total $") }, singleLine = true, isError = totalCents == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    deposit, { deposit = it.take(12) }, label = { Text("Abonó $") }, singleLine = true, isError = depositCents == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                )
            }
            if (totalCents != null && depositCents != null && totalCents > 0) {
                val rest = (totalCents - depositCents).coerceAtLeast(0)
                Text(if (rest > 0) "Resta por cobrar ${usd(rest)}" else "Pagado completo", color = if (rest > 0) FV.Ink else FV.Green)
            }
            OutlinedTextField(
                note, { note = it.take(200) }, label = { Text("Nota (opcional)") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
            Button(
                enabled = valid,
                onClick = { onSave(name, phone, description, totalCents ?: 0, depositCents ?: 0, localDayStart(due), note) },
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(54.dp),
            ) { Text(if (order == null) "Guardar pedido" else "Guardar cambios") }
            if (order != null && onStatus != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (order.status != "pending") {
                        OutlinedButton(onClick = { onStatus("pending") }, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) { Text("Volver a pendiente") }
                    } else {
                        OutlinedButton(onClick = { onStatus("cancelled") }, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) { Text("Cancelar pedido") }
                    }
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) { Text("Borrar", color = FV.Danger) }
                    }
                }
            }
        }
    }

    if (pickDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = localToUtcDay(due))
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { due = utcToLocalDayStart(it) }
                    pickDate = false
                }) { Text("Listo") }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text("Cancelar") } },
        ) { DatePicker(state = state) }
    }
}
