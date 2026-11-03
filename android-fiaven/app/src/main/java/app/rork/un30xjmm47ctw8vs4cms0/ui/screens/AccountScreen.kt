package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.SecurityQuestions
import app.rork.un30xjmm47ctw8vs4cms0.data.normalizeAnswer
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.AnswerField
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.InitialsAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.PasswordField
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.QuestionPicker
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.ui.platform.LocalContext
import app.rork.un30xjmm47ctw8vs4cms0.data.Backup
import app.rork.un30xjmm47ctw8vs4cms0.data.Kinds
import app.rork.un30xjmm47ctw8vs4cms0.ui.dayKey
import app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.BusinessAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.ThemeMode
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.ThemePrefs
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(vm: AppViewModel, onBack: () -> Unit, onOpenAbout: () -> Unit, onSecuritySaved: () -> Unit) {
    val session by vm.session.collectAsStateWithLifecycle()
    val security by vm.security.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    var editingSecurity by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf<AccountEdit?>(null) }
    val context = LocalContext.current
    var pending by remember { mutableStateOf<Backup?>(null) }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { target ->
        if (target != null) {
            val result = runCatching {
                val json = vm.exportBackup()
                context.contentResolver.openOutputStream(target)?.use { it.write(json.toByteArray()) } ?: error("No se pudo escribir")
            }
            Toast.makeText(
                context,
                if (result.isSuccess) "Respaldo guardado" else "No se pudo guardar el respaldo",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { source ->
        if (source != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(source)?.use { it.readBytes().decodeToString() } ?: error("vacío")
                vm.parseBackup(text)
            }.onSuccess { pending = it }
                .onFailure { Toast.makeText(context, it.message ?: "Este archivo no es un respaldo de Kashly", Toast.LENGTH_LONG).show() }
        }
    }
    val data by vm.data.collectAsStateWithLifecycle()
    var savingPhoto by remember { mutableStateOf(false) }
    var removingPhoto by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked ->
        if (picked != null) {
            savingPhoto = true
            vm.setBusinessPhoto(picked) { ok ->
                savingPhoto = false
                Toast.makeText(
                    context,
                    if (ok) "Foto guardada · se verá en todos los teléfonos del negocio" else "No se pudo usar esa imagen",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }
    val s = session ?: return
    val hasQuestion = security?.hasQuestion == true

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Mi cuenta") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FvCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(s.displayName.ifBlank { s.username }, 56)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.displayName.ifBlank { s.username }, style = MaterialTheme.typography.titleLarge, color = FV.Ink)
                        Text("@${s.username}", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = FV.Hairline)
                Spacer(Modifier.height(14.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Negocio", style = MaterialTheme.typography.labelMedium, color = FV.Muted)
                        Text(s.businessName, style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rol", style = MaterialTheme.typography.labelMedium, color = FV.Muted)
                        Text(s.roleLabel, style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    }
                }
            }

            SectionHeader("Datos de acceso")
            FvCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                LinkRow(Icons.Filled.Badge, "Nombre visible", s.displayName.ifBlank { "Sin nombre" }) { editMode = AccountEdit.NAME }
                HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                LinkRow(Icons.Filled.AlternateEmail, "Usuario para entrar", "@${s.username}") { editMode = AccountEdit.USERNAME }
                HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                LinkRow(Icons.Filled.Key, "Contraseña", "Cámbiala cuando quieras") { editMode = AccountEdit.PASSWORD }
            }

            SectionHeader("Foto del negocio")
            FvCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        BusinessAvatar(data.photo, s.businessName, size = 84)
                        if (savingPhoto) CircularProgressIndicator(Modifier.size(28.dp), color = FV.Green, strokeWidth = 3.dp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.businessName, style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                        Text(
                            when {
                                data.photo != null && s.isOwner -> "Todo tu equipo ve esta foto"
                                data.photo != null -> "Puesta por ${data.photo?.authorName?.ifBlank { null } ?: "el dueño"}"
                                s.isOwner -> "Añade el logo o una foto de tu negocio. Se verá en todos los teléfonos."
                                else -> "El dueño aún no ha puesto una foto del negocio."
                            },
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                    }
                }
                if (s.isOwner) {
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            enabled = !savingPhoto,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.PhotoCamera, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (data.photo != null) "Cambiar foto" else "Elegir foto")
                        }
                        if (data.photo != null) {
                            OutlinedButton(onClick = { removingPhoto = true }, enabled = !savingPhoto, shape = RoundedCornerShape(14.dp)) {
                                Text("Quitar", color = FV.Danger)
                            }
                        }
                    }
                }
            }

            SectionHeader("Apariencia")
            FvCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.Palette, FV.GreenDark, FV.GreenSoft, size = 40)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Modo oscuro", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                        Text("Solo cambia en este teléfono", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    }
                }
                Spacer(Modifier.height(12.dp))
                val options = listOf(ThemeMode.SYSTEM to "Automático", ThemeMode.LIGHT to "Claro", ThemeMode.DARK to "Oscuro")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    options.forEachIndexed { i, (mode, label) ->
                        SegmentedButton(
                            selected = ThemePrefs.mode == mode,
                            onClick = { ThemePrefs.set(context, mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                        ) { Text(label) }
                    }
                }
            }

            SectionHeader("Seguridad")
            FvCard(
                container = if (security != null && !hasQuestion) FV.AmberTint else FV.Surface,
                border = if (security != null && !hasQuestion) FV.AmberSoft else FV.Hairline,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(
                        if (hasQuestion) Icons.Filled.VerifiedUser else Icons.Filled.Shield,
                        if (hasQuestion) FV.Green else FV.AmberText,
                        if (hasQuestion) FV.GreenSoft else FV.Surface,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Pregunta de seguridad", style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                        Text(
                            when {
                                security == null -> "Conéctate a internet para ver su estado"
                                hasQuestion -> security?.question.orEmpty()
                                else -> "Aún no la tienes. Sin ella no podrás recuperar tu contraseña."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (security != null && !hasQuestion) FV.AmberText else FV.Muted,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { editingSecurity = true },
                    enabled = security != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (hasQuestion) "Cambiar pregunta" else "Configurar ahora") }
            }

            SectionHeader("Respaldo")
            FvCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                LinkRow(Icons.Filled.CloudDownload, "Descargar respaldo", "Guarda una copia de todo en tu teléfono") {
                    saveLauncher.launch("fiaven-respaldo-${dayKey(System.currentTimeMillis())}.json")
                }
                if (s.isOwner) {
                    HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                    LinkRow(Icons.Filled.CloudUpload, "Subir respaldo", "Recupera datos desde un archivo guardado") {
                        openLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    }
                }
            }

            SectionHeader("Privacidad y ayuda")
            FvCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                LinkRow(Icons.Filled.Policy, "Política de privacidad", "Qué datos guardamos y por qué") {
                    runCatching { uri.openUri(vm.privacyUrl) }
                }
                HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                LinkRow(Icons.Filled.Info, "Acerca de Kashly", "Versión, créditos y contacto", onClick = onOpenAbout)
            }

            FvCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                LinkRow(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", "Tus datos siguen guardados en la nube") { vm.logout() }
                HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                LinkRow(
                    Icons.Filled.DeleteForever, "Eliminar cuenta",
                    if (s.isOwner) "Borra tu negocio y todos sus datos" else "Borra tu usuario de este negocio",
                    tint = FV.Danger,
                ) { deleting = true }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (editingSecurity) {
        SecurityDialog(
            vm,
            current = security?.question,
            onDismiss = { editingSecurity = false },
            onSaved = { editingSecurity = false; onSecuritySaved() },
        )
    }
    if (removingPhoto) {
        AlertDialog(
            onDismissRequest = { removingPhoto = false },
            containerColor = FV.Surface,
            title = { Text("¿Quitar la foto del negocio?") },
            text = { Text("Se quitará en todos los teléfonos del negocio.", color = FV.Muted) },
            confirmButton = {
                TextButton(onClick = { vm.removeBusinessPhoto(); removingPhoto = false }) { Text("Quitar", color = FV.Danger) }
            },
            dismissButton = { TextButton(onClick = { removingPhoto = false }) { Text("Cancelar", color = FV.Muted) } },
        )
    }
    pending?.let { backup ->
        val counts = backup.records.groupingBy { it.kind }.eachCount()
        val other = backup.businessId != s.businessId
        AlertDialog(
            onDismissRequest = { pending = null },
            containerColor = FV.Surface,
            shape = RoundedCornerShape(24.dp),
            icon = { IconBadge(Icons.Filled.CloudUpload, FV.GreenDark, FV.GreenSoft, 52) },
            title = { Text("¿Subir este respaldo?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("De \"${backup.businessName}\" · ${shortDate(backup.exportedAt)}", color = FV.Ink)
                    Text(
                        listOf(
                            Kinds.SALE to "ventas", Kinds.PAYMENT to "abonos", Kinds.CUSTOMER to "clientes",
                            Kinds.PRODUCT to "productos", Kinds.NOTE to "notas", Kinds.QUOTE to "presupuestos",
                        ).joinToString(" · ") { (k, label) -> "${counts[k] ?: 0} $label" },
                        color = FV.Muted, style = MaterialTheme.typography.bodyMedium,
                    )
                    if (other) {
                        Text(
                            "Este respaldo es de otro negocio. Sus datos se mezclarán con los de \"${s.businessName}\".",
                            color = FV.Danger, style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        "No se borra nada: solo se recupera lo que falte o esté más viejo aquí.",
                        color = FV.Muted, style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val n = runCatching { vm.importBackup(backup) }.getOrDefault(-1)
                        pending = null
                        Toast.makeText(
                            context,
                            if (n < 0) "No se pudo subir el respaldo" else "Respaldo subido · $n registros recuperados",
                            Toast.LENGTH_LONG,
                        ).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Subir") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar", color = FV.Muted) } },
        )
    }
    if (deleting) DeleteAccountDialog(vm, isOwner = s.isOwner, businessName = s.businessName, onDismiss = { deleting = false })
    editMode?.let { mode ->
        AccountEditDialog(vm, mode, s.username, s.displayName, onDismiss = { editMode = null }) { msg ->
            editMode = null
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }
}

private enum class AccountEdit { NAME, USERNAME, PASSWORD }

@Composable
private fun AccountEditDialog(
    vm: AppViewModel, mode: AccountEdit, username: String, displayName: String,
    onDismiss: () -> Unit, onSaved: (String) -> Unit,
) {
    var value by remember { mutableStateOf(if (mode == AccountEdit.NAME) displayName else if (mode == AccountEdit.USERNAME) username else "") }
    var confirm by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = FV.Surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                when (mode) {
                    AccountEdit.NAME -> "Cambiar nombre"
                    AccountEdit.USERNAME -> "Cambiar usuario"
                    AccountEdit.PASSWORD -> "Cambiar contraseña"
                }
            )
        },
        text = {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (mode) {
                    AccountEdit.NAME -> {
                        Text("Es el nombre que ve tu equipo y que sale en las facturas.", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                        OutlinedTextField(
                            value = value, onValueChange = { value = it.take(60) }, label = { Text("Nombre visible") },
                            singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors(),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    AccountEdit.USERNAME -> {
                        Text("Con este usuario entras a Kashly. Tus ventas y registros se quedan contigo.", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                        OutlinedTextField(
                            value = value,
                            onValueChange = { v -> value = v.lowercase().filter { it.isLetterOrDigit() || it in "._&-" }.take(32) },
                            label = { Text("Usuario nuevo") }, prefix = { Text("@") },
                            supportingText = { Text("3 a 32 letras, números, punto, guion o &") },
                            singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    AccountEdit.PASSWORD -> {
                        Text("Tus otros teléfonos tendrán que volver a entrar con la contraseña nueva.", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                        PasswordField(value, { value = it }, "Contraseña nueva", imeAction = ImeAction.Next)
                        PasswordField(confirm, { confirm = it }, "Repite la contraseña nueva", imeAction = ImeAction.Next)
                    }
                }
                PasswordField(password, { password = it }, "Tu contraseña actual", imeAction = ImeAction.Done)
                AnimatedVisibility(error != null) {
                    Text(error.orEmpty(), color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = when {
                        mode == AccountEdit.NAME && value.trim().length < 2 -> "Escribe tu nombre"
                        mode == AccountEdit.USERNAME && !Regex("^[a-z0-9._&-]{3,32}$").matches(value.trim()) -> "Usuario no válido"
                        mode == AccountEdit.PASSWORD && value.length < 6 -> "La contraseña nueva debe tener al menos 6 caracteres"
                        mode == AccountEdit.PASSWORD && value != confirm -> "Las contraseñas no coinciden"
                        password.isBlank() -> "Escribe tu contraseña actual"
                        else -> null
                    }
                    if (error != null) return@Button
                    loading = true
                    val newUser = if (mode == AccountEdit.USERNAME) value.trim() else username
                    val newName = if (mode == AccountEdit.NAME) value.trim() else displayName
                    val newPass = if (mode == AccountEdit.PASSWORD) value else ""
                    vm.updateAccount(
                        password, newUser, newName, newPass,
                        onOk = {
                            loading = false
                            onSaved(
                                when (mode) {
                                    AccountEdit.NAME -> "Nombre actualizado"
                                    AccountEdit.USERNAME -> "Ahora entras como @$newUser"
                                    AccountEdit.PASSWORD -> "Contraseña cambiada"
                                }
                            )
                        },
                        onError = { loading = false; error = it },
                    )
                },
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = FV.Surface, strokeWidth = 2.dp)
                else Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !loading) { Text("Cancelar", color = FV.Muted) } },
    )
}

@Composable
private fun LinkRow(icon: ImageVector, title: String, subtitle: String, tint: Color = FV.Ink, onClick: () -> Unit) {
    ClickableRow(onClick) {
        Icon(icon, null, tint = if (tint == FV.Ink) FV.Muted else tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FV.Muted)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FV.Muted)
    }
}

@Composable
private fun SecurityDialog(vm: AppViewModel, current: String?, onDismiss: () -> Unit, onSaved: () -> Unit) {
    var question by remember { mutableStateOf(current?.takeIf { it in SecurityQuestions }.orEmpty()) }
    var answer by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = FV.Surface,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Pregunta de seguridad") },
        text = {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Si olvidas tu contraseña, podrás crear una nueva respondiendo esta pregunta.",
                    style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                )
                QuestionPicker(question, { question = it })
                AnswerField(answer, { answer = it })
                PasswordField(password, { password = it }, "Tu contraseña actual", imeAction = ImeAction.Done)
                AnimatedVisibility(error != null) {
                    Text(error.orEmpty(), color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = when {
                        question.isBlank() -> "Elige una pregunta"
                        normalizeAnswer(answer).length < 2 -> "Escribe tu respuesta"
                        password.isBlank() -> "Escribe tu contraseña actual"
                        else -> null
                    }
                    if (error == null) {
                        loading = true
                        vm.setSecurity(password, question, answer, onOk = { loading = false; onSaved() }, onError = { loading = false; error = it })
                    }
                },
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = FV.Surface, strokeWidth = 2.dp)
                else Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !loading) { Text("Cancelar", color = FV.Muted) } },
    )
}

@Composable
private fun DeleteAccountDialog(vm: AppViewModel, isOwner: Boolean, businessName: String, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = FV.Surface,
        shape = RoundedCornerShape(24.dp),
        icon = { IconBadge(Icons.Filled.DeleteForever, FV.Danger, FV.Danger.copy(alpha = 0.1f), 52) },
        title = { Text("¿Eliminar tu cuenta?") },
        text = {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (isOwner)
                        "Se borrarán para siempre \"$businessName\", sus ventas, fiados, productos, tasas y los accesos de tu equipo. No se puede deshacer."
                    else
                        "Se borrará tu usuario. Las ventas que registraste se quedan en el negocio. No se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium, color = FV.Ink,
                )
                PasswordField(password, { password = it }, "Escribe tu contraseña para confirmar")
                AnimatedVisibility(error != null) {
                    Text(error.orEmpty(), color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isBlank()) { error = "Escribe tu contraseña"; return@Button }
                    loading = true
                    error = null
                    vm.deleteAccount(password) { loading = false; error = it }
                },
                enabled = !loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FV.Danger, contentColor = FV.Surface),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = FV.Surface, strokeWidth = 2.dp)
                else Text("Eliminar para siempre")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !loading) { Text("Cancelar", color = FV.Muted) } },
    )
}
