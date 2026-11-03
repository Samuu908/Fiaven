package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Expense
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.bsAmount
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.PayMethodChips
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseDecimal
import app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

val EXPENSE_CATEGORIES = listOf(
    "Mercancía", "Servicios", "Alquiler", "Sueldos", "Transporte", "Comida", "Mantenimiento", "Publicidad", "Otros",
)

fun expenseIcon(category: String): ImageVector = when (category) {
    "Mercancía" -> Icons.Filled.Inventory2
    "Servicios" -> Icons.Filled.Bolt
    "Alquiler" -> Icons.Filled.Home
    "Sueldos" -> Icons.Filled.Groups
    "Transporte" -> Icons.Filled.DirectionsCar
    "Comida" -> Icons.Filled.Restaurant
    "Mantenimiento" -> Icons.Filled.Build
    "Publicidad" -> Icons.Filled.Campaign
    else -> Icons.Filled.MoreHoriz
}

private fun monthStart(offset: Int): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    add(Calendar.MONTH, offset)
}.timeInMillis

private fun monthLabel(start: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale("es")).format(Date(start)).replaceFirstChar { it.uppercase() }

/** Money going out (rent, services, merchandise…) and, for the owner, what is left after it. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpensesScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var adding by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Expense?>(null) }

    val from = monthStart(monthOffset)
    val to = monthStart(monthOffset + 1)
    val monthExpenses = remember(data.expenses, from) {
        data.expenses.filter { !it.voided && it.createdAt in from until to }.sortedByDescending { it.createdAt }
    }
    val spent = monthExpenses.sumOf { it.amountCents }
    val byCategory = remember(monthExpenses) {
        monthExpenses.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amountCents } }.toList().sortedByDescending { it.second }
    }
    val list = if (filter == null) monthExpenses else monthExpenses.filter { it.category == filter }

    // Owner-only: what came in vs. what went out.
    val income = remember(data.sales, data.payments, from) {
        data.sales.filter { !it.voided && !it.isFiado && it.createdAt in from until to }.sumOf { it.totalCents } +
            data.payments.filter { !it.voided && it.createdAt in from until to }.sumOf { it.amountCents }
    }
    val suppliersPaid = remember(data.supplierPayments, from) {
        data.supplierPayments.filter { !it.voided && it.createdAt in from until to }.sumOf { it.amountCents }
    }
    val net = income - spent - suppliersPaid

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Gastos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { adding = true },
                icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Anotar gasto") },
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
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { monthOffset-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Mes anterior") }
                    Text(
                        monthLabel(from), style = MaterialTheme.typography.titleMedium, color = FV.Ink,
                        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                    IconButton(onClick = { monthOffset++ }, enabled = monthOffset < 0) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Mes siguiente")
                    }
                }
            }

            item {
                FvCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Gastado en el mes", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
                        IconBadge(Icons.Filled.TrendingDown, FV.Danger, FV.DangerTint)
                    }
                    Text(usd(spent), style = MoneyLarge, color = FV.Ink)
                    if (ledger.effectiveRate > 0) Text(bs(spent, ledger.effectiveRate), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    if (byCategory.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        byCategory.take(5).forEach { (cat, cents) ->
                            CategoryBar(cat, cents, spent)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (isOwner) {
                item {
                    FvCard {
                        Text("Gasto vs. ganancia", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                        Spacer(Modifier.height(10.dp))
                        MoneyLine("Entró a caja (contado + abonos)", income, FV.Green, "+")
                        MoneyLine("Gastos", spent, FV.Danger, "−")
                        if (suppliersPaid > 0) MoneyLine("Pagos a proveedores", suppliersPaid, FV.Danger, "−")
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = FV.Hairline)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (net >= 0) "Te queda" else "Te falta", style = MaterialTheme.typography.titleMedium, color = FV.Ink, modifier = Modifier.weight(1f))
                            Text(usd(kotlin.math.abs(net)), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = if (net >= 0) FV.Green else FV.Danger)
                        }
                        Text(
                            if (net >= 0) "Lo que entró menos lo que salió este mes." else "Este mes salió más dinero del que entró.",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                    }
                }
            }

            if (byCategory.size > 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("Todos") }) }
                        items(byCategory.map { it.first }) { c ->
                            FilterChip(selected = filter == c, onClick = { filter = if (filter == c) null else c }, label = { Text(c) })
                        }
                    }
                }
            }

            item { SectionHeader("Movimientos del mes") }

            if (list.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Receipt, "Sin gastos este mes",
                        "Anota lo que pagas: luz, alquiler, mercancía, sueldos… Así sabrás cuánto te queda de verdad.",
                    )
                }
            } else {
                item {
                    FvCard(contentPadding = PaddingValues(0.dp)) {
                        list.forEachIndexed { i, e ->
                            if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 72.dp))
                            ClickableRow({ selected = e }) {
                                IconBadge(expenseIcon(e.category), FV.Muted, FV.Neutral)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        e.description.ifBlank { e.category }, style = MaterialTheme.typography.bodyLarge, color = FV.Ink,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        listOf(e.category, shortDate(e.createdAt), e.method.ifBlank { null }).filterNotNull().joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall, color = FV.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("−" + usd(e.amountCents), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = FV.Ink)
                                    if (e.currency == "BS") Text(bsAmount(e.originalAmount), style = MaterialTheme.typography.labelSmall, color = FV.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        ExpenseSheet(
            rate = ledger.effectiveRate,
            onDismiss = { adding = false },
            onSave = { cents, currency, original, category, description, method, date ->
                vm.addExpense(cents, currency, original, category, description, method, date)
                adding = false
            },
        )
    }

    selected?.let { e ->
        var confirm by remember(e.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { selected = null },
            containerColor = FV.Surface,
            icon = { Icon(expenseIcon(e.category), null, tint = FV.Muted) },
            title = { Text(e.description.ifBlank { e.category }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(usd(e.amountCents), style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                    if (e.currency == "BS") Text("Pagado en bolívares: ${bsAmount(e.originalAmount)}", color = FV.Muted)
                    Text("Categoría: ${e.category}", color = FV.Ink)
                    if (e.method.isNotBlank()) Text("Método: ${e.method}", color = FV.Ink)
                    Text("Anotado por ${e.authorName} · ${shortDate(e.createdAt)}", color = FV.Muted, style = MaterialTheme.typography.bodySmall)
                    if (confirm) Text("¿Anular este gasto? Se quitará en todos los teléfonos.", color = FV.Danger)
                }
            },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Cerrar") } },
            dismissButton = {
                TextButton(onClick = { if (confirm) { vm.voidExpense(e); selected = null } else confirm = true }) {
                    Text(if (confirm) "Sí, anular" else "Anular", color = FV.Danger)
                }
            },
        )
    }
}

@Composable
private fun CategoryBar(category: String, cents: Long, total: Long) {
    val frac = if (total > 0) (cents.toFloat() / total).coerceIn(0.02f, 1f) else 0f
    Column {
        Row {
            Text(category, style = MaterialTheme.typography.bodyMedium, color = FV.Ink, modifier = Modifier.weight(1f))
            Text(usd(cents), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
        }
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(FV.Neutral)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(frac).clip(RoundedCornerShape(4.dp)).background(FV.Green))
        }
    }
}

@Composable
private fun MoneyLine(label: String, cents: Long, color: androidx.compose.ui.graphics.Color, sign: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = FV.Ink, modifier = Modifier.weight(1f))
        Text("$sign ${usd(cents)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ExpenseSheet(
    rate: Double,
    onDismiss: () -> Unit,
    onSave: (Long, String, Double, String, String, String, Long) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currency by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Otros") }
    var description by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var pickDate by remember { mutableStateOf(false) }
    val canBs = rate > 0
    val value = parseDecimal(amount)?.toDouble()
    val cents: Long? = value?.let { if (currency == "USD") (it * 100).roundToLong() else if (canBs) (it / rate * 100).roundToLong() else null }
        ?.takeIf { it > 0 }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = FV.Surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Anotar gasto", style = MaterialTheme.typography.titleLarge, color = FV.Ink)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = currency == "USD", onClick = { currency = "USD"; amount = "" }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Dólares") }
                SegmentedButton(selected = currency == "BS", enabled = canBs, onClick = { currency = "BS"; amount = "" }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Bolívares") }
            }
            OutlinedTextField(
                value = amount, onValueChange = { amount = it.take(14) },
                label = { Text(if (currency == "USD") "Monto en $" else "Monto en Bs") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
            if (cents != null && currency == "BS") Text("Equivale a ${usd(cents)}", color = FV.Green, style = MaterialTheme.typography.bodyMedium)
            if (cents != null && currency == "USD" && canBs) Text("Equivale a ${bs(cents, rate)}", color = FV.Muted, style = MaterialTheme.typography.bodyMedium)

            Text("Categoría", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EXPENSE_CATEGORIES.forEach { c ->
                    FilterChip(
                        selected = category == c, onClick = { category = c }, label = { Text(c) },
                        leadingIcon = { Icon(if (category == c) Icons.Filled.Check else expenseIcon(c), null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FV.GreenSoft, selectedLabelColor = FV.GreenDark, selectedLeadingIconColor = FV.GreenDark),
                    )
                }
            }
            OutlinedTextField(
                description, { description = it.take(80) }, label = { Text("Descripción (ej. Pago de luz)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
            Text("¿Cómo lo pagaste?", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
            PayMethodChips(method, { method = it })

            Surface(
                onClick = { pickDate = true }, shape = RoundedCornerShape(16.dp), color = FV.Surface,
                border = BorderStroke(1.dp, FV.Outline), modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = FV.Muted)
                    Spacer(Modifier.width(12.dp))
                    Text("Fecha", color = FV.Muted, modifier = Modifier.weight(1f))
                    Text(shortDate(date), color = FV.Ink, fontWeight = FontWeight.SemiBold)
                }
            }

            Button(
                enabled = cents != null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(cents ?: 0, currency, value ?: 0.0, category, description, method, date)
                },
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(54.dp),
            ) { Text(if (cents != null) "Guardar gasto · ${usd(cents)}" else "Guardar gasto") }
        }
    }

    if (pickDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = localToUtcDay(date),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= System.currentTimeMillis()
            },
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = utcDayToLocal(it) }
                    pickDate = false
                }) { Text("Listo") }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text("Cancelar") } },
        ) { DatePicker(state = state) }
    }
}
