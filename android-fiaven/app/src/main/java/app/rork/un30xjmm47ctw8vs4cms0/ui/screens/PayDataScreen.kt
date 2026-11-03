package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.PagoMovil
import app.rork.un30xjmm47ctw8vs4cms0.data.PayAccount
import app.rork.un30xjmm47ctw8vs4cms0.data.PayData
import app.rork.un30xjmm47ctw8vs4cms0.data.PayQr
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.prettyCedula
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

private val VE_BANKS = listOf(
    "0102 Banco de Venezuela", "0134 Banesco", "0105 Mercantil", "0108 BBVA Provincial", "0172 Bancamiga",
    "0191 BNC", "0163 Banco del Tesoro", "0175 Bicentenario", "0174 Banplus", "0115 Banco Exterior",
    "0114 Bancaribe", "0104 Venezolano de Crédito", "0138 Banco Plaza", "0171 Banco Activo", "0137 Sofitasa",
    "0156 100% Banco", "0128 Banco Caroní", "0177 Banfanb", "0169 Mi Banco", "0151 BFC",
)

private val ACCOUNT_KINDS = listOf("Zelle", "Binance", "Transferencia", "PayPal", "Zinli", "Wally", "Efectivo")

private fun mobileText(m: PagoMovil) = buildString {
    appendLine("*Pago móvil*")
    appendLine("Banco: ${m.bank}")
    appendLine("Teléfono: ${m.phone}")
    append("C.I./RIF: ${prettyCedula(m.cedula).ifBlank { m.cedula }}")
    if (m.holder.isNotBlank()) append("\nTitular: ${m.holder}")
}

private fun accountText(a: PayAccount) = buildString {
    append("*${a.label}*\n${a.value}")
    if (a.holder.isNotBlank()) append("\nTitular: ${a.holder}")
}

private fun copy(context: Context, text: String, what: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText(what, text))
    Toast.makeText(context, "$what copiado", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    runCatching {
        val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
        context.startActivity(Intent.createChooser(send, "Enviar datos de pago"))
    }
}

private fun accountIcon(label: String) = when (label.lowercase()) {
    "binance" -> Icons.Filled.CurrencyBitcoin
    else -> Icons.Filled.AccountBalance
}

/** "Cobros": QR codes and payment details to show or send to customers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayDataScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true
    val pay = data.payData
    val context = LocalContext.current
    var viewing by remember { mutableStateOf<PayQr?>(null) }
    var editMobile by remember { mutableStateOf<PagoMovil?>(null) }
    var newMobile by remember { mutableStateOf(false) }
    var editAccount by remember { mutableStateOf<PayAccount?>(null) }
    var newAccount by remember { mutableStateOf(false) }
    var pendingQr by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }
    var removeQr by remember { mutableStateOf<PayQr?>(null) }

    fun save(p: PayData) = vm.savePayData(p)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uploading = true
            vm.addAttachment(uri) { id ->
                uploading = false
                if (id != null) pendingQr = id
                else Toast.makeText(context, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val allText = (pay.mobile.map { mobileText(it) } + pay.accounts.map { accountText(it) }).joinToString("\n\n")
    val empty = pay.qrs.isEmpty() && pay.mobile.isEmpty() && pay.accounts.isEmpty()

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Cobros") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = {
                    if (allText.isNotBlank()) IconButton(onClick = { shareText(context, (session?.businessName?.let { "$it\n\n" } ?: "") + allText) }) {
                        Icon(Icons.Filled.Share, "Compartir todo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (empty) {
                item {
                    EmptyState(
                        Icons.Filled.QrCode2, "Tus datos para cobrar",
                        if (isOwner) "Agrega tus QR, cuentas de pago móvil, Zelle o Binance. Todo el equipo podrá mostrarlos y enviarlos al cliente."
                        else "El dueño aún no ha agregado datos de pago.",
                    )
                }
            }

            // ---- QR codes
            if (pay.qrs.isNotEmpty() || isOwner) {
                item {
                    SectionHeader("Códigos QR") {
                        if (isOwner) TextButton(
                            enabled = !uploading,
                            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        ) {
                            if (uploading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = FV.Green)
                            else Text("+ Agregar QR", color = FV.GreenDark)
                        }
                    }
                }
                if (pay.qrs.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(pay.qrs, key = { it.id }) { q ->
                                FvCard(contentPadding = PaddingValues(10.dp), modifier = Modifier.width(176.dp)) {
                                    AttachmentThumb(vm, q.attachmentId, 156) { viewing = q }
                                    Spacer(Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            q.label, style = MaterialTheme.typography.titleSmall, color = FV.Ink,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                                        )
                                        if (isOwner) IconButton(onClick = { removeQr = q }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Filled.Delete, "Quitar", tint = FV.Muted, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Text("Toca para mostrar", style = MaterialTheme.typography.labelSmall, color = FV.Muted)
                                }
                            }
                        }
                    }
                }
            }

            // ---- Pago móvil
            if (pay.mobile.isNotEmpty() || isOwner) {
                item {
                    SectionHeader("Pago móvil") {
                        if (isOwner) TextButton(onClick = { newMobile = true }) { Text("+ Agregar", color = FV.GreenDark) }
                    }
                }
                items(pay.mobile, key = { it.id }) { m ->
                    FvCard(contentPadding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(Icons.Filled.PhoneAndroid, FV.GreenDark, FV.GreenSoft, 40)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.bank, style = MaterialTheme.typography.titleSmall, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (m.holder.isNotBlank()) Text(m.holder, style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            }
                            if (isOwner) IconButton(onClick = { editMobile = m }) { Icon(Icons.Filled.Edit, "Editar", tint = FV.Muted) }
                        }
                        Spacer(Modifier.height(10.dp))
                        CopyLine("Teléfono", m.phone) { copy(context, m.phone, "Teléfono") }
                        CopyLine("C.I./RIF", prettyCedula(m.cedula).ifBlank { m.cedula }) { copy(context, m.cedula.filter { it.isLetterOrDigit() }, "Cédula") }
                        Spacer(Modifier.height(10.dp))
                        ShareButtons(onCopy = { copy(context, mobileText(m).replace("*", ""), "Datos") }, onShare = { shareText(context, mobileText(m)) })
                    }
                }
            }

            // ---- Other accounts
            if (pay.accounts.isNotEmpty() || isOwner) {
                item {
                    SectionHeader("Zelle, Binance y otras") {
                        if (isOwner) TextButton(onClick = { newAccount = true }) { Text("+ Agregar", color = FV.GreenDark) }
                    }
                }
                items(pay.accounts, key = { it.id }) { a ->
                    FvCard(contentPadding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(accountIcon(a.label), FV.GreenDark, FV.GreenSoft, 40)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(a.label, style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                                if (a.holder.isNotBlank()) Text(a.holder, style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                            }
                            if (isOwner) IconButton(onClick = { editAccount = a }) { Icon(Icons.Filled.Edit, "Editar", tint = FV.Muted) }
                        }
                        Spacer(Modifier.height(10.dp))
                        CopyLine("Dato", a.value) { copy(context, a.value, a.label) }
                        Spacer(Modifier.height(10.dp))
                        ShareButtons(onCopy = { copy(context, a.value, a.label) }, onShare = { shareText(context, accountText(a)) })
                    }
                }
            }

            if (!isOwner && !empty) {
                item { Text("Solo el dueño puede cambiar estos datos.", style = MaterialTheme.typography.bodySmall, color = FV.Muted) }
            }
        }
    }

    viewing?.let { q -> AttachmentViewer(vm, q.attachmentId) { viewing = null } }

    pendingQr?.let { id ->
        LabelDialog(
            title = "Nombre del QR", hint = "Ej. Binance, Banesco, Pago móvil",
            initial = "", onDismiss = { pendingQr = null },
        ) { label ->
            save(pay.copy(qrs = pay.qrs + PayQr(vm.repo.newId(), label, id)))
            pendingQr = null
        }
    }

    removeQr?.let { q ->
        AlertDialog(
            onDismissRequest = { removeQr = null },
            containerColor = FV.Surface,
            title = { Text("¿Quitar el QR \"${q.label}\"?") },
            confirmButton = {
                TextButton(onClick = { save(pay.copy(qrs = pay.qrs.filterNot { it.id == q.id })); removeQr = null }) { Text("Quitar", color = FV.Danger) }
            },
            dismissButton = { TextButton(onClick = { removeQr = null }) { Text("Cancelar") } },
        )
    }

    if (newMobile || editMobile != null) {
        val e = editMobile
        MobileDialog(
            e,
            onDismiss = { newMobile = false; editMobile = null },
            onDelete = e?.let { m -> { save(pay.copy(mobile = pay.mobile.filterNot { it.id == m.id })); editMobile = null } },
        ) { bank, phone, ced, holder ->
            val item = PagoMovil(e?.id ?: vm.repo.newId(), bank.trim(), phone.trim(), ced.trim(), holder.trim())
            save(pay.copy(mobile = if (e == null) pay.mobile + item else pay.mobile.map { if (it.id == e.id) item else it }))
            newMobile = false; editMobile = null
        }
    }

    if (newAccount || editAccount != null) {
        val e = editAccount
        AccountDialog(
            e,
            onDismiss = { newAccount = false; editAccount = null },
            onDelete = e?.let { a -> { save(pay.copy(accounts = pay.accounts.filterNot { it.id == a.id })); editAccount = null } },
        ) { label, value, holder ->
            val item = PayAccount(e?.id ?: vm.repo.newId(), label.trim(), value.trim(), holder.trim())
            save(pay.copy(accounts = if (e == null) pay.accounts + item else pay.accounts.map { if (it.id == e.id) item else it }))
            newAccount = false; editAccount = null
        }
    }
}

@Composable
private fun CopyLine(label: String, value: String, onCopy: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = FV.Muted)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = FV.Ink)
        }
        IconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, "Copiar $label", tint = FV.GreenDark, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun ShareButtons(onCopy: () -> Unit, onShare: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onCopy, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.ContentCopy, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Copiar todo")
        }
        Button(onClick = onShare, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) {
            Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Enviar")
        }
    }
}

@Composable
private fun LabelDialog(title: String, hint: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value, { value = it.take(30) }, label = { Text("Nombre") }, placeholder = { Text(hint) }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(enabled = value.isNotBlank(), onClick = { onSave(value.trim()) }) { Text("Guardar", color = FV.GreenDark) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun MobileDialog(
    current: PagoMovil?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String, String, String) -> Unit,
) {
    var bank by remember { mutableStateOf(current?.bank.orEmpty()) }
    var phone by remember { mutableStateOf(current?.phone.orEmpty()) }
    var cedula by remember { mutableStateOf(current?.cedula.orEmpty()) }
    var holder by remember { mutableStateOf(current?.holder.orEmpty()) }
    var banks by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text(if (current == null) "Nuevo pago móvil" else "Editar pago móvil") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box {
                    OutlinedTextField(
                        bank, { bank = it.take(40) }, label = { Text("Banco") }, singleLine = true,
                        trailingIcon = { IconButton(onClick = { banks = true }) { Icon(Icons.Filled.ArrowDropDown, "Elegir banco") } },
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    )
                    DropdownMenu(expanded = banks, onDismissRequest = { banks = false }) {
                        VE_BANKS.forEach { b -> DropdownMenuItem(text = { Text(b) }, onClick = { bank = b; banks = false }) }
                    }
                }
                OutlinedTextField(
                    phone, { phone = it.filter { c -> c.isDigit() || c in "+- " }.take(16) }, label = { Text("Teléfono") }, singleLine = true,
                    placeholder = { Text("0414-1234567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    cedula, { cedula = it.take(14) }, label = { Text("Cédula o RIF") }, singleLine = true,
                    placeholder = { Text("V-12345678") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    holder, { holder = it.take(50) }, label = { Text("Titular (opcional)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                if (confirm) Text("¿Borrar esta cuenta?", color = FV.Danger)
            }
        },
        confirmButton = {
            TextButton(enabled = bank.isNotBlank() && phone.isNotBlank() && cedula.isNotBlank(), onClick = { onSave(bank, phone, cedula, holder) }) {
                Text("Guardar", color = FV.GreenDark)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { if (confirm) onDelete() else confirm = true }) { Text(if (confirm) "Sí, borrar" else "Borrar", color = FV.Danger) }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AccountDialog(
    current: PayAccount?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String, String, String) -> Unit,
) {
    var label by remember { mutableStateOf(current?.label ?: "Zelle") }
    var value by remember { mutableStateOf(current?.value.orEmpty()) }
    var holder by remember { mutableStateOf(current?.holder.orEmpty()) }
    var confirm by remember { mutableStateOf(false) }
    val hint = when (label.lowercase()) {
        "zelle" -> "Correo o teléfono de Zelle"
        "binance" -> "Correo, Pay ID o usuario"
        "transferencia" -> "Banco y número de cuenta"
        else -> "Dato para pagar"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text(if (current == null) "Nueva cuenta" else "Editar cuenta") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ACCOUNT_KINDS.forEach { k -> SuggestionChip(onClick = { label = k }, label = { Text(k, color = if (label == k) FV.GreenDark else FV.Ink) }) }
                }
                OutlinedTextField(
                    label, { label = it.take(24) }, label = { Text("Tipo") }, singleLine = true,
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value, { value = it.take(120) }, label = { Text(hint) },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    holder, { holder = it.take(50) }, label = { Text("Titular (opcional)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                if (confirm) Text("¿Borrar esta cuenta?", color = FV.Danger)
            }
        },
        confirmButton = {
            TextButton(enabled = label.isNotBlank() && value.isNotBlank(), onClick = { onSave(label, value, holder) }) { Text("Guardar", color = FV.GreenDark) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { if (confirm) onDelete() else confirm = true }) { Text(if (confirm) "Sí, borrar" else "Borrar", color = FV.Danger) }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
    )
}
