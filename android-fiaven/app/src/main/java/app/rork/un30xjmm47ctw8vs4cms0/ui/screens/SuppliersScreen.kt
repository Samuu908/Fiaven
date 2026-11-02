package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Kinds
import app.rork.un30xjmm47ctw8vs4cms0.data.Purchase
import app.rork.un30xjmm47ctw8vs4cms0.data.SupplierPayment
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.SupplierBalance
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.bsAmount
import app.rork.un30xjmm47ctw8vs4cms0.ui.centsToInput
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.InitialsAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.hourLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseDecimal
import app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate
import app.rork.un30xjmm47ctw8vs4cms0.ui.startOfToday
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import coil3.compose.AsyncImage
import java.io.File
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToLong

private val PAYMENT_METHODS = listOf("Efectivo", "Transferencia", "Pago móvil", "Zelle", "Otro")

private fun supplierSubtitle(rif: String, phone: String): String =
    listOf(rif, phone).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Sin RIF ni teléfono" }

// ---------------------------------------------------------------- list

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(vm: AppViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val balances by vm.supplierBalances.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val data by vm.data.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }

    val totalOwed = balances.sumOf { it.owedCents }
    val owing = balances.count { it.owedCents > 0 }
    val monthStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val paidThisMonth = data.supplierPayments.filter { !it.voided && it.createdAt >= monthStart }.sumOf { it.amountCents }
    val list = remember(balances, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) balances
        else balances.filter { it.name.lowercase().contains(q) || it.rif.lowercase().contains(q) || it.phone.contains(q) }
    }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Proveedores") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        bottomBar = {
            Surface(color = FV.Canvas) {
                Button(
                    onClick = { creating = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.navigationBarsPadding().padding(16.dp).fillMaxWidth().heightIn(min = 54.dp),
                ) {
                    Icon(Icons.Filled.LocalShipping, null); Spacer(Modifier.width(8.dp))
                    Text("Nuevo proveedor", style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FvCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total que debes", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
                        IconBadge(Icons.Filled.LocalShipping, FV.GreenDark, FV.GreenSoft)
                    }
                    Text(usd(totalOwed), style = MoneyLarge, color = FV.Ink)
                    Text(
                        "$owing ${if (owing == 1) "proveedor" else "proveedores"} · ${bs(totalOwed, ledger.effectiveRate)}",
                        style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = FV.Hairline)
                    Spacer(Modifier.height(12.dp))
                    Text("Abonado este mes", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    Text(usd(paidThisMonth), style = MaterialTheme.typography.titleMedium, color = FV.Green)
                }
            }
            if (balances.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        placeholder = { Text("Buscar por nombre o RIF") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") } },
                        singleLine = true, shape = RoundedCornerShape(28.dp), colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (list.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.LocalShipping,
                        if (balances.isEmpty()) "Sin proveedores" else "Sin resultados",
                        if (balances.isEmpty()) "Agrega a quienes te venden mercancía para llevar sus facturas, lo que les debes y cada abono con su comprobante."
                        else "Prueba con otro nombre o RIF.",
                    )
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                        Column {
                            list.forEachIndexed { i, b ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 80.dp))
                                ClickableRow(onClick = { onOpen(b.supplierId) }) {
                                    InitialsAvatar(b.name)
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(b.name, style = MaterialTheme.typography.titleMedium, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(supplierSubtitle(b.rif, b.phone), style = MaterialTheme.typography.bodyMedium, color = FV.Muted, maxLines = 1)
                                        Text(
                                            when {
                                                b.owedCents > 0 -> "Le debes ${usd(b.owedCents)}"
                                                b.creditCents > 0 -> "Saldo a favor ${usd(b.creditCents)}"
                                                else -> "Al día"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (b.owedCents > 0) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (b.owedCents > 0) FV.Ink else FV.Green,
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FV.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating) {
        SupplierDialog("Nuevo proveedor", "", "", "", "", onDismiss = { creating = false }) { n, r, p, note ->
            val id = vm.saveSupplier(null, n, p, r, note)
            creating = false
            onOpen(id)
        }
    }
}

@Composable
private fun SupplierDialog(
    title: String, name0: String, rif0: String, phone0: String, note0: String,
    onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf(name0) }
    var rif by remember { mutableStateOf(rif0) }
    var phone by remember { mutableStateOf(phone0) }
    var note by remember { mutableStateOf(note0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    name, { name = it.take(60) }, label = { Text("Nombre o empresa") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    rif, { rif = it.take(20) }, label = { Text("RIF (opcional)") }, placeholder = { Text("J-12345678-9") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    phone, { phone = it.take(20) }, label = { Text("Teléfono (opcional)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    note, { note = it.take(200) }, label = { Text("Nota (opcional)") }, placeholder = { Text("Ej.: trae los martes, cuenta Banesco…") },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), minLines = 2,
                )
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onSave(name, rif, phone, note) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

// ---------------------------------------------------------------- detail

private data class SupplierEntry(
    val key: String,
    val isPayment: Boolean,
    val title: String,
    val subtitle: String,
    val createdAt: Long,
    val amountCents: Long,
    val attachments: List<String>,
    val voided: Boolean,
    val pending: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(vm: AppViewModel, supplierId: String, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val balances by vm.supplierBalances.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isOwner = session?.isOwner == true
    val supplier = data.suppliers.find { it.id == supplierId }
    val balance = balances.find { it.supplierId == supplierId }
        ?: SupplierBalance(supplierId, supplier?.name ?: "Proveedor", supplier?.phone.orEmpty(), supplier?.rif.orEmpty(), 0, 0, 0)
    val me = session?.username

    val history = remember(data, supplierId, me) {
        val purchases = data.purchases.filter { it.supplierId == supplierId }.map { p ->
            SupplierEntry(
                "f:${p.id}", false,
                (if (p.voided) "Anulada · " else "") + if (p.invoiceNumber.isNotBlank()) "Factura Nº ${p.invoiceNumber}" else "Compra / deuda",
                "${if (p.authorUser == me) "Tú" else p.authorName} · ${shortDate(p.createdAt)}",
                p.createdAt, p.amountCents, p.attachments, p.voided, "${Kinds.PURCHASE}:${p.id}" in data.dirtyKeys,
            )
        }
        val pays = data.supplierPayments.filter { it.supplierId == supplierId }.map { p ->
            SupplierEntry(
                "a:${p.id}", true,
                (if (p.voided) "Anulado · " else "Abono") + (if (p.method.isNotBlank()) " · ${p.method}" else "") +
                    if (p.currency == "BS") " · ${bsAmount(p.originalAmount)}" else "",
                "${if (p.authorUser == me) "Tú" else p.authorName} · ${shortDate(p.createdAt)}",
                p.createdAt, p.amountCents, p.attachments, p.voided, "${Kinds.SUPPLIER_PAYMENT}:${p.id}" in data.dirtyKeys,
            )
        }
        (purchases + pays).sortedByDescending { it.createdAt }
    }

    var newPurchase by remember { mutableStateOf(false) }
    var newPayment by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text(balance.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = {
                    IconButton(onClick = { editing = true }) { Icon(Icons.Filled.Edit, "Editar proveedor") }
                    if (isOwner) {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "Más") }
                        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            DropdownMenuItem(
                                text = { Text("Eliminar proveedor", color = FV.Danger) },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = FV.Danger) },
                                onClick = { menu = false; confirmDelete = true },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        bottomBar = {
            Surface(color = FV.Canvas) {
                Row(
                    Modifier.navigationBarsPadding().padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = { newPurchase = true },
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                    ) { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Factura") }
                    Button(
                        onClick = { newPayment = true },
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1.3f).heightIn(min = 54.dp),
                    ) { Icon(Icons.Filled.Payments, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Registrar abono") }
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
                        Text(supplierSubtitle(balance.rif, balance.phone), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    }
                }
            }
            supplier?.note?.takeIf { it.isNotBlank() }?.let { note ->
                item { Text(note, style = MaterialTheme.typography.bodyMedium, color = FV.Muted) }
            }
            item {
                FvCard {
                    Text(if (balance.creditCents > 0) "Saldo a tu favor" else "Le debes", style = MaterialTheme.typography.titleSmall, color = FV.Muted)
                    Text(
                        usd(if (balance.creditCents > 0) balance.creditCents else balance.owedCents), style = MoneyLarge,
                        color = if (balance.owedCents > 0) FV.Ink else FV.Green,
                    )
                    Text(bs(balance.owedCents.coerceAtLeast(balance.creditCents), ledger.effectiveRate), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    Spacer(Modifier.size(12.dp))
                    HorizontalDivider(color = FV.Hairline)
                    Spacer(Modifier.size(12.dp))
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text("Abonado", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            Text(usd(balance.paidCents), style = MaterialTheme.typography.titleMedium, color = FV.Green)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Total comprado", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            Text(usd(balance.purchasedCents), style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                        }
                    }
                }
            }
            if (balance.phone.isNotBlank()) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${balance.phone}".toUri())) } },
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                        ) { Icon(Icons.Filled.Call, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Llamar") }
                        OutlinedButton(
                            onClick = {
                                val digits = balance.phone.filter { it.isDigit() }
                                val intl = if (digits.startsWith("0")) "58" + digits.drop(1) else digits
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/$intl".toUri())) }
                            },
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                        ) { Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("WhatsApp") }
                    }
                }
            }
            item { SectionHeader("Historial") }
            if (history.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.History, "Sin movimientos",
                        "Anota cada factura de compra y cada abono. Adjunta la foto de la factura o la captura de la transferencia como comprobante.",
                    )
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                        Column {
                            history.forEachIndexed { i, e ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 74.dp))
                                EntryRow(e) { selected = e.key }
                            }
                        }
                    }
                }
            }
        }
    }

    if (newPurchase || newPayment) {
        EntrySheet(
            vm = vm, isPayment = newPayment, balance = balance, rate = ledger.effectiveRate,
            onDismiss = { newPurchase = false; newPayment = false },
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            newPurchase = false; newPayment = false
        }
    }

    selected?.let { key ->
        val purchase = if (key.startsWith("f:")) data.purchases.find { it.id == key.drop(2) } else null
        val payment = if (key.startsWith("a:")) data.supplierPayments.find { it.id == key.drop(2) } else null
        if (purchase == null && payment == null) selected = null
        else EntryDetailSheet(
            vm, purchase, payment, canVoid = isOwner,
            onVoid = {
                purchase?.let { vm.voidPurchase(it) }
                payment?.let { vm.voidSupplierPayment(it) }
                selected = null
            },
            onDismiss = { selected = null },
        )
    }

    if (editing) {
        SupplierDialog(
            "Editar proveedor", balance.name, balance.rif, balance.phone, supplier?.note.orEmpty(),
            onDismiss = { editing = false },
        ) { n, r, p, note ->
            vm.saveSupplier(supplierId, n, p, r, note)
            editing = false
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = FV.Surface,
            title = { Text("¿Eliminar proveedor?") },
            text = {
                Text(
                    if (history.isEmpty()) "${balance.name} se quitará de la lista."
                    else "${balance.name} se quitará de la lista. Sus facturas y abonos siguen guardados en el respaldo.",
                    color = FV.Muted,
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.deleteSupplier(supplierId); onBack() }) { Text("Eliminar", color = FV.Danger) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun EntryRow(e: SupplierEntry, onClick: () -> Unit) {
    ClickableRow(onClick) {
        IconBadge(
            if (e.isPayment) Icons.Filled.Payments else Icons.AutoMirrored.Filled.ReceiptLong,
            if (e.voided) FV.Muted else if (e.isPayment) FV.GreenDark else FV.Ink,
            if (e.voided) FV.Neutral else if (e.isPayment) FV.GreenSoft else FV.Neutral,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(e.title, style = MaterialTheme.typography.bodyLarge, color = if (e.voided) FV.Muted else FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(e.subtitle, style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                if (e.attachments.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.AttachFile, null, tint = FV.GreenDark, modifier = Modifier.size(14.dp))
                    Text("${e.attachments.size}", style = MaterialTheme.typography.labelSmall, color = FV.GreenDark)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            (if (e.isPayment) "−" else "+") + usd(e.amountCents),
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
            color = when { e.voided -> FV.Muted; e.isPayment -> FV.Green; else -> FV.Ink },
        )
    }
}

// ---------------------------------------------------------------- new invoice / payment

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EntrySheet(
    vm: AppViewModel,
    isPayment: Boolean,
    balance: SupplierBalance,
    rate: Double,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currency by remember { mutableStateOf("USD") }
    var amount by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Transferencia") }
    var reference by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var pickDate by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<String>() }
    var adding by remember { mutableStateOf(false) }
    var photoError by remember { mutableStateOf(false) }
    var cameraFile by remember { mutableStateOf<File?>(null) }

    val value = parseDecimal(amount)?.toDouble()
    val canBs = rate > 0
    val cents: Long? = value?.let { if (currency == "USD") (it * 100).roundToLong() else if (canBs) (it / rate * 100).roundToLong() else null }

    fun attach(uri: Uri?) {
        if (uri == null) return
        adding = true; photoError = false
        vm.addAttachment(uri) { id ->
            adding = false
            if (id != null) attachments += id else photoError = true
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { attach(it) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = cameraFile
        if (ok && f != null) attach(Uri.fromFile(f))
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = FV.Surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (isPayment) "Abono a ${balance.name}" else "Factura de ${balance.name}", style = MaterialTheme.typography.titleLarge, color = FV.Ink)
            Text(
                if (isPayment) "Le debes ${usd(balance.owedCents)}. Adjunta la captura de la transferencia o el recibo."
                else "Anota lo que compraste a crédito y adjunta la foto de la factura.",
                style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
            )
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = currency == "USD", onClick = { currency = "USD"; amount = "" }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Dólares") }
                SegmentedButton(selected = currency == "BS", enabled = canBs, onClick = { currency = "BS"; amount = "" }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Bolívares") }
            }
            OutlinedTextField(
                value = amount, onValueChange = { amount = it.take(14) },
                label = { Text(if (currency == "USD") "Monto en $" else "Monto en Bs") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                trailingIcon = if (isPayment && balance.owedCents > 0) {
                    {
                        TextButton(onClick = {
                            amount = if (currency == "USD") centsToInput(balance.owedCents)
                            else centsToInput((balance.owedCents / 100.0 * rate * 100).roundToLong())
                        }) { Text("Todo") }
                    }
                } else null,
            )
            if (cents != null && currency == "BS") Text("Equivale a ${usd(cents)}", color = FV.Green, style = MaterialTheme.typography.bodyMedium)
            if (cents != null && currency == "USD" && canBs) Text("Equivale a ${bs(cents, rate)}", color = FV.Muted, style = MaterialTheme.typography.bodyMedium)

            if (isPayment) {
                Text("Forma de pago", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PAYMENT_METHODS.forEach { m ->
                        FilterChip(
                            selected = method == m, onClick = { method = m }, label = { Text(m) },
                            leadingIcon = if (method == m) { { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) } } else null,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FV.GreenSoft, selectedLabelColor = FV.GreenDark, selectedLeadingIconColor = FV.GreenDark),
                        )
                    }
                }
                if (method != "Efectivo") {
                    OutlinedTextField(
                        reference, { reference = it.take(40) }, label = { Text("Nº de referencia (opcional)") }, singleLine = true,
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                OutlinedTextField(
                    number, { number = it.take(30) }, label = { Text("Nº de factura (opcional)") }, singleLine = true,
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
            }

            Surface(
                onClick = { pickDate = true }, shape = RoundedCornerShape(16.dp), color = FV.Surface,
                border = BorderStroke(1.dp, FV.Outline), modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = FV.Muted)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Fecha", style = MaterialTheme.typography.labelMedium, color = FV.Muted)
                        Text(if (date >= startOfToday()) "Hoy, ${shortDate(date)}" else shortDate(date), style = MaterialTheme.typography.bodyLarge, color = FV.Ink)
                    }
                    Text("Cambiar", style = MaterialTheme.typography.labelLarge, color = FV.GreenDark)
                }
            }

            OutlinedTextField(
                note, { note = it.take(300) }, label = { Text("Nota (opcional)") },
                placeholder = { Text(if (isPayment) "Ej.: pagado a nombre de Pedro" else "Ej.: 20 sacos de cemento") },
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(), minLines = 2,
            )

            Text(if (isPayment) "Comprobante" else "Foto de la factura", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(attachments.toList(), key = { it }) { id ->
                    Box {
                        AttachmentThumb(vm, id, 88) {}
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)).clickable { attachments.remove(id) },
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Filled.Close, "Quitar", tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                }
                if (adding) {
                    item {
                        Box(Modifier.size(88.dp).clip(RoundedCornerShape(14.dp)).background(FV.Neutral), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(26.dp), color = FV.Green, strokeWidth = 3.dp)
                        }
                    }
                }
            }
            if (attachments.size < 4) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            val dir = File(context.cacheDir, "camera").apply { mkdirs() }
                            val f = File(dir, "foto_${System.currentTimeMillis()}.jpg")
                            cameraFile = f
                            val uri = FileProvider.getUriForFile(context, context.packageName + ".files", f)
                            runCatching { camera.launch(uri) }
                        },
                        enabled = !adding, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                    ) { Icon(Icons.Filled.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Cámara") }
                    OutlinedButton(
                        onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        enabled = !adding, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                    ) { Icon(Icons.Filled.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Galería") }
                }
            }
            if (photoError) Text("No se pudo leer la imagen. Prueba con otra.", color = FV.Danger, style = MaterialTheme.typography.bodySmall)

            Button(
                enabled = cents != null && cents > 0 && !adding,
                onClick = {
                    val when0 = date
                    if (isPayment) vm.addSupplierPayment(balance, cents!!, currency, value ?: 0.0, method, if (method == "Efectivo") "" else reference, note, attachments.toList(), when0)
                    else vm.addPurchase(balance, cents!!, currency, value ?: 0.0, number, note, attachments.toList(), when0)
                    onSaved()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(top = 4.dp),
            ) {
                Text(
                    if (isPayment) "Guardar abono" + (cents?.let { " · ${usd(it)}" } ?: "")
                    else "Guardar factura" + (cents?.let { " · ${usd(it)}" } ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
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

private fun localToUtcDay(local: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = local }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear(); set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Picked day → now if it's today, else noon of that day (local time). */
private fun utcDayToLocal(utc: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
    val local = Calendar.getInstance().apply {
        set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH), 12, 0, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return if (local >= startOfToday()) System.currentTimeMillis() else local
}

// ---------------------------------------------------------------- entry detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryDetailSheet(
    vm: AppViewModel,
    purchase: Purchase?,
    payment: SupplierPayment?,
    canVoid: Boolean,
    onVoid: () -> Unit,
    onDismiss: () -> Unit,
) {
    val pending by vm.pendingAttachments.collectAsStateWithLifecycle()
    var viewing by remember { mutableStateOf<String?>(null) }
    var confirmVoid by remember { mutableStateOf(false) }
    val isPayment = payment != null
    val amount = purchase?.amountCents ?: payment?.amountCents ?: 0
    val currency = purchase?.currency ?: payment?.currency ?: "USD"
    val original = purchase?.originalAmount ?: payment?.originalAmount ?: 0.0
    val rate = purchase?.rate ?: payment?.rate ?: 0.0
    val createdAt = purchase?.createdAt ?: payment?.createdAt ?: 0
    val author = purchase?.authorName ?: payment?.authorName.orEmpty()
    val note = purchase?.note ?: payment?.note.orEmpty()
    val attachments = purchase?.attachments ?: payment?.attachments.orEmpty()
    val voided = purchase?.voided ?: payment?.voided ?: false

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = FV.Surface) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    if (isPayment) Icons.Filled.Payments else Icons.AutoMirrored.Filled.ReceiptLong,
                    if (isPayment) FV.GreenDark else FV.Ink, if (isPayment) FV.GreenSoft else FV.Neutral,
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isPayment) "Abono" + (payment?.method?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")
                        else purchase?.invoiceNumber?.takeIf { it.isNotBlank() }?.let { "Factura Nº $it" } ?: "Compra / deuda",
                        style = MaterialTheme.typography.titleLarge, color = FV.Ink,
                    )
                    Text(purchase?.supplierName ?: payment?.supplierName.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                }
            }
            if (voided) {
                Surface(shape = RoundedCornerShape(12.dp), color = FV.DangerTint, border = BorderStroke(1.dp, FV.DangerSoft)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Block, null, tint = FV.Danger, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPayment) "Abono anulado: no cuenta en el saldo" else "Factura anulada: no cuenta en el saldo", color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text(usd(amount), style = MoneyLarge, color = if (voided) FV.Muted else if (isPayment) FV.Green else FV.Ink)
            if (currency == "BS") Text("Pagado en bolívares: ${bsAmount(original)} (tasa ${app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber(rate)})", color = FV.Muted)
            else if (rate > 0) Text("≈ ${bs(amount, rate)} a la tasa de ese día", color = FV.Muted, style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = FV.Hairline)
            DetailLine("Fecha", "${shortDate(createdAt)} · ${hourLabel(createdAt)}")
            DetailLine("Registrado por", author)
            payment?.reference?.takeIf { it.isNotBlank() }?.let { DetailLine("Referencia", it) }
            if (note.isNotBlank()) DetailLine("Nota", note)

            Text(if (isPayment) "Comprobantes" else "Fotos de la factura", style = MaterialTheme.typography.titleSmall, color = FV.Ink, modifier = Modifier.padding(top = 6.dp))
            if (attachments.isEmpty()) {
                Text("Sin fotos adjuntas.", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(attachments, key = { it }) { id ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AttachmentThumb(vm, id, 112) { viewing = id }
                            if (id in pending) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Filled.CloudUpload, null, tint = FV.AmberText, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("subiendo", style = MaterialTheme.typography.labelSmall, color = FV.AmberText)
                                }
                            }
                        }
                    }
                }
                Text("Toca una foto para verla completa o compartirla.", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }

            if (canVoid && !voided) {
                TextButton(onClick = { confirmVoid = true }, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Filled.Block, null, tint = FV.Danger, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isPayment) "Anular abono" else "Anular factura", color = FV.Danger)
                }
            }
        }
    }

    viewing?.let { id -> AttachmentViewer(vm, id) { viewing = null } }

    if (confirmVoid) {
        AlertDialog(
            onDismissRequest = { confirmVoid = false },
            containerColor = FV.Surface,
            title = { Text(if (isPayment) "¿Anular este abono?" else "¿Anular esta factura?") },
            text = { Text("Dejará de contar en el saldo. Queda en el historial marcado como anulado, con sus fotos.", color = FV.Muted) },
            confirmButton = { TextButton(onClick = { confirmVoid = false; onVoid() }) { Text("Anular", color = FV.Danger) } },
            dismissButton = { TextButton(onClick = { confirmVoid = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = FV.Muted, modifier = Modifier.width(120.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = FV.Ink, modifier = Modifier.weight(1f))
    }
}

// ---------------------------------------------------------------- attachments

@Composable
private fun rememberAttachment(vm: AppViewModel, id: String): Pair<File?, Boolean> {
    var file by remember(id) { mutableStateOf<File?>(null) }
    var failed by remember(id) { mutableStateOf(false) }
    LaunchedEffect(id) {
        val f = vm.loadAttachment(id)
        if (f == null) failed = true else file = f
    }
    return file to failed
}

@Composable
private fun AttachmentThumb(vm: AppViewModel, id: String, size: Int, onClick: () -> Unit) {
    val (file, failed) = rememberAttachment(vm, id)
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape(14.dp)).background(FV.Neutral)
            .border(1.dp, FV.Hairline, RoundedCornerShape(14.dp)).clickable(enabled = file != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when {
            file != null -> AsyncImage(model = file, contentDescription = "Comprobante", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            failed -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CloudOff, null, tint = FV.Muted, modifier = Modifier.size(22.dp))
                Text("Sin conexión", style = MaterialTheme.typography.labelSmall, color = FV.Muted)
            }
            else -> CircularProgressIndicator(Modifier.size(22.dp), color = FV.Green, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun AttachmentViewer(vm: AppViewModel, id: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val (file, _) = rememberAttachment(vm, id)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transform = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 6f)
        offset = if (scale == 1f) Offset.Zero else offset + pan
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (file != null) {
                AsyncImage(
                    model = file, contentDescription = "Comprobante", contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures(onDoubleTap = { if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2.5f }) }
                        .transformable(transform)
                        .graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y },
                )
            } else {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White)
            }
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar", tint = Color.White) }
                Spacer(Modifier.weight(1f))
                AnimatedVisibility(file != null) {
                    IconButton(onClick = {
                        val f = file ?: return@IconButton
                        runCatching {
                            val uri = FileProvider.getUriForFile(context, context.packageName + ".files", f)
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(send, "Compartir comprobante"))
                        }
                    }) { Icon(Icons.Filled.Share, "Compartir", tint = Color.White) }
                }
            }
        }
    }
}
