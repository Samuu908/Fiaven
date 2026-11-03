package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import app.rork.un30xjmm47ctw8vs4cms0.ui.customerSubtitle
import app.rork.un30xjmm47ctw8vs4cms0.ui.ClientStats
import app.rork.un30xjmm47ctw8vs4cms0.ui.agoLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.prettyCedula
import androidx.compose.ui.text.input.KeyboardCapitalization
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Kinds
import app.rork.un30xjmm47ctw8vs4cms0.ui.ActivityItem
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.CustomerBalance
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.bsAmount
import app.rork.un30xjmm47ctw8vs4cms0.ui.centsToInput
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.InitialsAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseDecimal
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import kotlin.math.roundToLong
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WarningAmber
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(vm: AppViewModel, customerId0: String, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isOwner = session?.isOwner == true

    // Duplicates of the same person open as one account.
    val customerId = ledger.groups.canonicalOf(customerId0)
    val memberIds = remember(ledger.groups, customerId) { ledger.groups.memberIds(customerId) }
    val customer = ledger.groups.merged[customerId] ?: data.customers.find { it.id == customerId }
    val stats = ledger.clients.find { it.customer.id == customerId }
    val balance = ledger.balances.find { it.customerId == customerId }
        ?: CustomerBalance(customerId, customer?.name ?: "Cliente", customer?.phone.orEmpty(), 0, 0, 0, cedula = customer?.cedula.orEmpty())

    val me = session?.username
    val history = remember(data, memberIds, me) {
        val sales = data.sales.filter { it.customerId in memberIds && !it.voided }.map { s ->
            val what = if (s.items.size == 1) "${s.items[0].name} x${s.items[0].qty}" else "${s.items.size} productos"
            ActivityItem("s:${s.id}", false, (if (s.isFiado) "Fiado · " else "Contado · ") + what,
                if (s.authorUser == me) "Tú" else s.authorName, s.createdAt, s.totalCents,
                "${Kinds.SALE}:${s.id}" in data.dirtyKeys, s.isFiado)
        }
        val pays = data.payments.filter { it.customerId in memberIds && !it.voided }.map { p ->
            ActivityItem("p:${p.id}", true,
                "Abono" + if (p.currency == "BS") " · ${bsAmount(p.originalAmount)}" else "",
                if (p.authorUser == me) "Tú" else p.authorName, p.createdAt, p.amountCents,
                "${Kinds.PAYMENT}:${p.id}" in data.dirtyKeys, false)
        }
        (sales + pays).sortedByDescending { it.createdAt }
    }

    val dayClosed = ledger.todayClosing != null
    var showPay by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text(balance.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = { IconButton(onClick = { showEdit = true }) { Icon(Icons.Filled.Edit, "Editar cliente") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        bottomBar = {
            Surface(color = FV.Canvas) {
                Button(
                    onClick = { showPay = true },
                    enabled = balance.owedCents > 0 && !dayClosed,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.navigationBarsPadding().padding(16.dp).fillMaxWidth().heightIn(min = 54.dp),
                ) {
                    Icon(if (dayClosed && balance.owedCents > 0) Icons.Filled.Lock else Icons.Filled.Payments, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            balance.owedCents <= 0 -> "Cliente al día"
                            dayClosed -> "Caja de hoy cerrada"
                            else -> "Registrar abono"
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(balance.name, 56)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(balance.name, style = MaterialTheme.typography.titleLarge, color = FV.Ink)
                        Text(customerSubtitle(balance.cedula, balance.phone), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    }
                }
            }
            if (stats != null) {
                item { ClientStatsCard(stats) }
            }
            if (memberIds.size > 1) {
                item {
                    Text(
                        "Unificado: ${memberIds.size} registros con la misma cédula o teléfono se muestran como un solo cliente.",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
            if (balance.overdue) {
                item {
                    FvCard(container = FV.DangerTint, border = FV.DangerSoft, contentPadding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Filled.WarningAmber, FV.Danger, FV.Surface)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Deuda con ${balance.daysOwed} días — prioridad de cobro", style = MaterialTheme.typography.titleSmall, color = FV.Danger)
                                balance.debtSince?.let {
                                    Text("Debe desde el ${shortDate(it)}", style = MaterialTheme.typography.bodySmall, color = FV.Ink)
                                }
                            }
                        }
                    }
                }
            }
            if (balance.fiadoCents > 0 || balance.paidCents > 0) item {
                FvCard(container = if (balance.overdue) FV.DangerTint else FV.AmberTint, border = if (balance.overdue) FV.DangerSoft else FV.AmberSoft) {
                    Text("Debe", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                    Text(
                        usd(balance.owedCents), style = MoneyLarge,
                        color = when { balance.overdue -> FV.Danger; balance.owedCents > 0 -> FV.AmberText; else -> FV.Green },
                    )
                    Text(bs(balance.owedCents, ledger.effectiveRate), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    Spacer(Modifier.size(12.dp))
                    HorizontalDivider(color = FV.AmberSoft)
                    Spacer(Modifier.size(12.dp))
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Abonado", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            Text(usd(balance.paidCents), style = MaterialTheme.typography.titleMedium, color = FV.Green)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Total fiado", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            Text(usd(balance.fiadoCents), style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                        }
                    }
                }
            }
            if (balance.phone.isNotBlank()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${balance.phone}".toUri())) }
                            },
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                        ) { Icon(Icons.Filled.Call, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Llamar") }
                        OutlinedButton(
                            onClick = {
                                val digits = balance.phone.filter { it.isDigit() }
                                val intl = if (digits.startsWith("0")) "58" + digits.drop(1) else digits
                                val msg = "Hola ${balance.name}, te escribo por tu cuenta pendiente de ${usd(balance.owedCents)} " +
                                    "(${bs(balance.owedCents, ledger.effectiveRate)}). ¡Gracias!"
                                val uri = "https://wa.me/$intl?text=${android.net.Uri.encode(msg)}".toUri()
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            },
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                        ) { Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("WhatsApp") }
                    }
                }
            }
            item { SectionHeader("Historial") }
            if (history.isEmpty()) {
                item { EmptyState(Icons.Filled.History, "Sin movimientos", "Aquí verás sus compras, fiados y abonos.") }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                        Column {
                            history.forEachIndexed { i, a ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 74.dp))
                                ActivityRow(a) { selected = a.key }
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { key ->
        val sale = if (key.startsWith("s:")) data.sales.find { it.id == key.drop(2) } else null
        val payment = if (key.startsWith("p:")) data.payments.find { it.id == key.drop(2) } else null
        ActivityDetailDialog(
            sale = sale, payment = payment, rate = ledger.effectiveRate,
            canVoid = sale?.voided == false || payment?.voided == false,
            onVoid = {
                sale?.let { vm.voidSale(it) }
                payment?.let { vm.voidPayment(it) }
                selected = null
            },
            onOpenCustomer = null,
            onDismiss = { selected = null },
            businessName = vm.session.value?.businessName.orEmpty(),
            onAttendedBy = { s, n -> vm.setAttendedBy(s, n) },
        )
    }

    if (showPay) {
        PaymentDialog(balance, ledger.effectiveRate, onDismiss = { showPay = false }) { cents, currency, original, method ->
            vm.addPayment(balance, cents, currency, original, method)
            showPay = false
        }
    }
    if (showEdit) {
        EditCustomerDialog(
            balance.name, balance.phone, balance.cedula,
            duplicateOf = { p, c -> vm.duplicateOf(customerId, p, c)?.name },
            onDismiss = { showEdit = false },
        ) { n, p, c ->
            vm.updateCustomer(customerId, n, p, c)
            showEdit = false
        }
    }
}

@Composable
private fun PaymentDialog(
    balance: CustomerBalance,
    rate: Double,
    onDismiss: () -> Unit,
    onSave: (Long, String, Double, String) -> Unit,
) {
    var currency by remember { mutableStateOf("USD") }
    var method by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val value = parseDecimal(amount)?.toDouble()
    val canBs = rate > 0
    val cents: Long? = value?.let { if (currency == "USD") (it * 100).roundToLong() else if (canBs) (it / rate * 100).roundToLong() else null }
    val tooMuch = cents != null && cents > balance.owedCents + 1

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text("Registrar abono") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${balance.name} debe ${usd(balance.owedCents)}", color = FV.Muted)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(selected = currency == "USD", onClick = { currency = "USD"; amount = "" }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Dólares") }
                    SegmentedButton(selected = currency == "BS", enabled = canBs, onClick = { currency = "BS"; amount = "" }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Bolívares") }
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text(if (currency == "USD") "Monto en $" else "Monto en Bs") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = {
                            amount = if (currency == "USD") centsToInput(balance.owedCents)
                            else centsToInput((balance.owedCents / 100.0 * rate * 100).roundToLong())
                        }) { Text("Todo") }
                    },
                )
                if (cents != null && currency == "BS") Text("Equivale a ${usd(cents)}", color = FV.Green)
                if (cents != null && currency == "USD" && canBs) Text("Equivale a ${bs(cents, rate)}", color = FV.Muted)
                if (tooMuch) Text("El abono es mayor que la deuda", color = FV.Danger, style = MaterialTheme.typography.bodySmall)
                Text("Método de pago", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                app.rork.un30xjmm47ctw8vs4cms0.ui.components.PayMethodChips(method, { method = it })
            }
        },
        confirmButton = {
            TextButton(
                enabled = cents != null && cents > 0 && !tooMuch,
                onClick = { onSave(cents!!.coerceAtMost(balance.owedCents), currency, value ?: 0.0, method) },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun EditCustomerDialog(
    name0: String, phone0: String, cedula0: String,
    duplicateOf: (String, String) -> String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf(name0) }
    var cedula by remember { mutableStateOf(prettyCedula(cedula0)) }
    var phone by remember { mutableStateOf(phone0) }
    val other = duplicateOf(phone, cedula)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text("Editar cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    cedula, { cedula = it.take(16) }, label = { Text("Cédula o RIF") }, placeholder = { Text("V-12.345.678") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    phone, { phone = it }, label = { Text("Teléfono") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                if (other != null) {
                    Surface(shape = RoundedCornerShape(16.dp), color = FV.AmberTint, border = BorderStroke(1.dp, FV.AmberSoft)) {
                        Text(
                            "Esa cédula o teléfono ya es de $other. Si guardas, ambos quedarán como un solo cliente y sus deudas se sumarán.",
                            style = MaterialTheme.typography.bodySmall, color = FV.AmberText, modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(name, phone, cedula) }) {
                Text(if (other != null) "Unir y guardar" else "Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun ClientStatsCard(stats: ClientStats) {
    FvCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Compras", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
            if (stats.frequent) {
                Surface(shape = RoundedCornerShape(50), color = FV.GreenTint) {
                    Text("Frecuente", style = MaterialTheme.typography.labelMedium, color = FV.GreenDark, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
        Spacer(Modifier.size(6.dp))
        Row {
            Column(Modifier.weight(1f)) {
                Text("${stats.purchases}", style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                Text(if (stats.purchases == 1) "compra" else "compras", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
            Column(Modifier.weight(1f)) {
                Text(usd(stats.spentCents), style = MaterialTheme.typography.headlineSmall, color = FV.Green)
                Text("en total", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
            Column(Modifier.weight(1f)) {
                Text("${stats.purchasesLast30}", style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                Text("últimos 30 días", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
        }
        Spacer(Modifier.size(10.dp))
        HorizontalDivider(color = FV.Hairline)
        Spacer(Modifier.size(10.dp))
        Text(stats.frequencyLabel, style = MaterialTheme.typography.bodyMedium, color = FV.Ink)
        Text(
            listOfNotNull(
                stats.lastPurchase?.let { "Última compra ${agoLabel(it)}" },
                stats.firstPurchase?.let { "cliente desde el ${shortDate(it)}" },
            ).joinToString(" · ").ifBlank { "Todavía no ha comprado" },
            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
        )
    }
}
