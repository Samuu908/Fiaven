package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Payment
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale
import app.rork.un30xjmm47ctw8vs4cms0.ui.ActivityItem
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.buildWeek
import app.rork.un30xjmm47ctw8vs4cms0.ui.startOfToday
import app.rork.un30xjmm47ctw8vs4cms0.ui.weekStart
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SyncBanner
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SyncedLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyHuge
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.timeLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import app.rork.un30xjmm47ctw8vs4cms0.ui.hourLabel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajaScreen(
    vm: AppViewModel,
    onOpenFiados: () -> Unit,
    onOpenCustomer: (String) -> Unit,
    onOpenSummary: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenMovements: () -> Unit,
    onOpenClosing: () -> Unit,
) {
    val security by vm.security.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val sync by vm.sync.collectAsStateWithLifecycle()
    val data by vm.data.collectAsStateWithLifecycle()
    val sessionState by vm.session.collectAsStateWithLifecycle()
    val week = remember(data) { buildWeek(data, weekStart()) }
    val todaySummary = remember(week) { week.days.firstOrNull { it.start == startOfToday() } }
    var allTime by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<ActivityItem?>(null) }
    val cash = if (allTime) ledger.cashAllCents else ledger.cashTodayCents
    val customersOwing = ledger.balances.count { it.owedCents > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SyncBanner(sync, onClick = { vm.syncNow() }) }

        if (security?.hasQuestion == false) {
            item(key = "protect") {
                FvCard(container = FV.AmberTint, border = FV.AmberSoft, onClick = onOpenAccount, contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.Shield, FV.AmberText, FV.Surface)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Protege tu cuenta", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                            Text(
                                "Añade una pregunta de seguridad para recuperar tu contraseña si la olvidas.",
                                style = MaterialTheme.typography.bodySmall, color = FV.AmberText,
                            )
                        }
                    }
                }
            }
        }

        item {
            FvCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Dinero en caja", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                        Text(
                            if (allTime) "Contado + abonos · todo" else "Contado + abonos · hoy",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                    }
                    IconBadge(Icons.Filled.AccountBalanceWallet, FV.Green, FV.GreenSoft)
                }
                Spacer(Modifier.height(6.dp))
                AnimatedContent(cash, label = "cash") { value ->
                    Text(usd(value), style = MoneyHuge, color = FV.Green)
                }
                Text(bs(cash, ledger.effectiveRate), style = MaterialTheme.typography.titleMedium, color = FV.Muted)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { SyncedLabel(sync) }
                    SingleChoiceSegmentedButtonRow(Modifier.width(150.dp)) {
                        SegmentedButton(
                            selected = !allTime, onClick = { allTime = false },
                            shape = SegmentedButtonDefaults.itemShape(0, 2), icon = {},
                        ) { Text("Hoy", style = MaterialTheme.typography.labelMedium) }
                        SegmentedButton(
                            selected = allTime, onClick = { allTime = true },
                            shape = SegmentedButtonDefaults.itemShape(1, 2), icon = {},
                        ) { Text("Todo", style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        }

        if (ledger.overdue.isNotEmpty()) {
            item(key = "overdue") {
                val n = ledger.overdue.size
                FvCard(container = FV.DangerTint, border = FV.DangerSoft, onClick = onOpenFiados, contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.WarningAmber, FV.Danger, FV.Surface)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "$n ${if (n == 1) "fiado vencido" else "fiados vencidos"} · ${usd(ledger.overdueCents)}",
                                style = MaterialTheme.typography.titleSmall, color = FV.Danger,
                            )
                            Text(
                                "Llevan más de 15 días sin pagarse. Cóbralos primero.",
                                style = MaterialTheme.typography.bodySmall, color = FV.Ink,
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FV.Danger)
                    }
                }
            }
        }

        val cal = java.util.Calendar.getInstance()
        val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val weekendTime = dow == java.util.Calendar.SUNDAY ||
            (dow == java.util.Calendar.SATURDAY && cal.get(java.util.Calendar.HOUR_OF_DAY) >= 17)
        if (weekendTime) {
            item(key = "weekend") {
                FvCard(onClick = onOpenSummary, contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.AutoMirrored.Filled.Send, FV.GreenDark, FV.GreenSoft)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Resumen del fin de semana", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                            Text("Envíalo por WhatsApp en un toque", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FV.Muted)
                    }
                }
            }
        }

        item(key = "closing") {
            val closing = ledger.todayClosing
            FvCard(onClick = onOpenClosing, contentPadding = PaddingValues(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        if (closing != null) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        FV.GreenDark, FV.GreenSoft,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (closing != null) "Día cerrado" else "Cerrar caja de hoy",
                            style = MaterialTheme.typography.titleSmall, color = FV.Ink,
                        )
                        Text(
                            if (closing != null) "A las ${hourLabel(closing.createdAt)} por ${closing.authorName}"
                            else "Cuenta el efectivo y deja el día listo",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FV.Muted)
                }
            }
        }

        item {
            val total = week.total
            FvCard(onClick = onOpenSummary) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Resumen semanal", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                        Text(
                            "Hoy ${usd(todaySummary?.soldCents ?: 0)} · semana ${usd(total.soldCents)}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = FV.Ink,
                        )
                        if (sessionState?.isOwner == true && total.hasCosts) {
                            Text(
                                "Dinero libre de la semana: ${usd(total.profitCents)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (total.profitCents >= 0) FV.Green else FV.Danger,
                            )
                        } else {
                            Text("Ventas por día y por semana", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                        }
                    }
                    IconBadge(Icons.Filled.Insights, FV.Green, FV.GreenSoft)
                }
            }
        }

        item {
            FvCard(container = FV.AmberTint, border = FV.Amber.copy(alpha = 0.55f), onClick = onOpenFiados) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Fiados por cobrar (aparte)", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
                    IconBadge(Icons.Filled.People, FV.AmberText, FV.AmberSoft)
                }
                Text(usd(ledger.fiadoOutstandingCents), style = MoneyLarge, color = FV.AmberText)
                Text(
                    "$customersOwing ${if (customersOwing == 1) "cliente" else "clientes"} · no cuenta en caja",
                    style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                )
            }
        }

        item {
            SectionHeader("Actividad reciente") {
                TextButton(onClick = onOpenMovements) { Text("Ver por fecha", color = FV.GreenDark) }
            }
        }

        if (ledger.activity.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.PointOfSale, "Aún no hay ventas",
                    "Toca “Nueva venta” para anotar la primera. Aparecerá en todos los teléfonos del negocio.",
                )
            }
        } else {
            item {
                FvCard(contentPadding = PaddingValues(0.dp), modifier = Modifier.animateContentSize()) {
                    ledger.activity.take(25).forEachIndexed { index, a ->
                        if (index > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 72.dp))
                        ActivityRow(a) { selected = a }
                    }
                }
            }
        }
    }

    selected?.let { item ->
        val sale = if (!item.isPayment) data.sales.find { "s:${it.id}" == item.key } else null
        val payment = if (item.isPayment) data.payments.find { "p:${it.id}" == item.key } else null
        ActivityDetailDialog(
            sale = sale, payment = payment, rate = ledger.effectiveRate,
            canVoid = sale?.voided == false || payment?.voided == false,
            onVoid = {
                sale?.let { vm.voidSale(it) }; payment?.let { vm.voidPayment(it) }; selected = null
            },
            onOpenCustomer = (sale?.customerId ?: payment?.customerId)?.let { id -> { selected = null; onOpenCustomer(id) } },
            onDismiss = { selected = null },
            businessName = vm.session.value?.businessName.orEmpty(),
        )
    }
}

@Composable
fun ActivityRow(a: ActivityItem, onClick: () -> Unit) {
    ClickableRow(onClick) {
        IconBadge(
            if (a.isPayment) Icons.Filled.Payments else Icons.Filled.ShoppingCart,
            if (a.fiado) FV.AmberText else FV.Muted,
            if (a.fiado) FV.AmberSoft else FV.Neutral,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(a.title, style = MaterialTheme.typography.bodyLarge, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${a.author} · ${timeLabel(a.createdAt)}", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (a.isPayment) "+" else "") + usd(a.amountCents),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (a.fiado) FV.AmberText else FV.Ink,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (a.pending) Icons.Filled.Schedule else Icons.Filled.CheckCircle, null,
                    tint = if (a.pending) FV.AmberText else FV.Green, modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (a.pending) "pendiente" else "sincronizado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (a.pending) FV.AmberText else FV.Green,
                )
            }
        }
    }
}

@Composable
fun ActivityDetailDialog(
    sale: Sale?,
    payment: Payment?,
    rate: Double,
    canVoid: Boolean,
    onVoid: () -> Unit,
    onOpenCustomer: (() -> Unit)?,
    onDismiss: () -> Unit,
    businessName: String = "",
) {
    var confirm by remember { mutableStateOf(false) }
    var showInvoice by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = {
            Text(
                when {
                    sale?.isFiado == true -> "Venta fiada"
                    sale != null -> "Venta de contado"
                    else -> "Abono"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (sale != null) {
                    sale.customerName?.let { Text("Cliente: $it", color = FV.Ink) }
                    sale.items.forEach {
                        Row(Modifier.fillMaxWidth()) {
                            Text("${it.qty} × ${it.name}", modifier = Modifier.weight(1f), color = FV.Ink)
                            Text(usd(it.priceCents * it.qty), color = FV.Ink)
                        }
                    }
                    HorizontalDivider(color = FV.Hairline)
                    Row(Modifier.fillMaxWidth()) {
                        Text("Total", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(usd(sale.totalCents), fontWeight = FontWeight.SemiBold)
                    }
                    Text(bs(sale.totalCents, sale.rate) + " a tasa del momento", color = FV.Muted, style = MaterialTheme.typography.bodySmall)
                    Text("Registrado por ${sale.authorName} · ${timeLabel(sale.createdAt)}", color = FV.Muted, style = MaterialTheme.typography.bodySmall)
                    sale.customerCedula?.let { Text("C.I./RIF: ${app.rork.un30xjmm47ctw8vs4cms0.ui.prettyCedula(it)}", color = FV.Muted, style = MaterialTheme.typography.bodySmall) }
                    if (!confirm) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showInvoice = true },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        ) {
                            Icon(Icons.Filled.Receipt, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ver factura / descargar foto")
                        }
                    }
                }
                if (payment != null) {
                    Text("Cliente: ${payment.customerName}", color = FV.Ink)
                    Text(usd(payment.amountCents), style = MaterialTheme.typography.headlineSmall, color = FV.Green)
                    if (payment.currency == "BS") {
                        Text("Pagó Bs ${app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber(payment.originalAmount)}", color = FV.Muted)
                    }
                    Text("Registrado por ${payment.authorName} · ${timeLabel(payment.createdAt)}", color = FV.Muted, style = MaterialTheme.typography.bodySmall)
                }
                if (confirm) {
                    Text("¿Seguro? Se quitará de la caja en todos los teléfonos.", color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            if (onOpenCustomer != null && !confirm) TextButton(onClick = onOpenCustomer) { Text("Ver cliente") }
            else TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        dismissButton = {
            if (canVoid) {
                TextButton(onClick = { if (confirm) onVoid() else confirm = true }) {
                    Text(if (confirm) "Sí, anular" else "Anular", color = FV.Danger)
                }
            }
        },
    )
    if (showInvoice && sale != null) {
        SaleInvoiceDialog(sale, businessName, onDismiss = { showInvoice = false })
    }
}
