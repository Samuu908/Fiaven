package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.TeamMember
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.InitialsAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

val ROLE_OPTIONS = listOf("Dueño", "Administrador", "Gerente", "Subjefe", "Encargado", "Cajero", "Empleado")

@Composable
private fun RoleBadge(label: String, owner: Boolean) {
    Surface(shape = RoundedCornerShape(8.dp), color = if (owner) FV.GreenSoft else FV.Neutral) {
        Text(
            label, style = MaterialTheme.typography.labelMedium,
            color = if (owner) FV.GreenDark else FV.Ink,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

/** Predefined job titles plus a free "Otro…" field (max 30 characters). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolePicker(value: String, forOwner: Boolean, onChange: (String) -> Unit) {
    val options = if (forOwner) ROLE_OPTIONS else ROLE_OPTIONS.filter { it != "Dueño" }
    var custom by remember { mutableStateOf(value.isNotBlank() && value !in options) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            options.forEach { o ->
                val selected = !custom && value == o
                FilterChip(
                    selected = selected,
                    onClick = { custom = false; onChange(o) },
                    label = { Text(o) },
                    leadingIcon = if (selected) { { Icon(Icons.Filled.Check, null, Modifier.padding(0.dp)) } } else null,
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FV.GreenSoft, selectedLabelColor = FV.GreenDark, selectedLeadingIconColor = FV.GreenDark),
                )
            }
            FilterChip(
                selected = custom,
                onClick = { custom = true; if (value in options) onChange("") },
                label = { Text("Otro…") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FV.GreenSoft, selectedLabelColor = FV.GreenDark),
            )
        }
        if (custom) {
            OutlinedTextField(
                value, { onChange(it.take(30)) }, label = { Text("Escribe el rol") }, placeholder = { Text("Ej.: Almacenista") },
                singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(vm: AppViewModel, onBack: () -> Unit) {
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true
    val scope = rememberCoroutineScope()
    var members by remember { mutableStateOf<List<TeamMember>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var adding by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf<TeamMember?>(null) }
    var editingRole by remember { mutableStateOf<TeamMember?>(null) }

    LaunchedEffect(reload) {
        error = null
        try { members = vm.repo.team() } catch (e: Exception) { error = vm.friendly(e) }
    }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Equipo y accesos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FvCard {
                    Text(session?.businessName.orEmpty(), style = MaterialTheme.typography.titleLarge, color = FV.Ink)
                    Text(
                        "Conectado como ${session?.displayName} (@${session?.username}) · ${session?.roleLabel.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "Solo las personas con usuario y contraseña pueden registrar ventas en este negocio." +
                            if (isOwner) " Toca a una persona para cambiar su nombre, rol o contraseña." else "",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                    )
                }
            }
            item {
                SectionHeader("Personas con acceso") {
                    if (isOwner) TextButton(onClick = { adding = true }) {
                        Icon(Icons.Filled.PersonAdd, null); Spacer(Modifier.width(6.dp)); Text("Agregar")
                    }
                }
            }
            item {
                when {
                    error != null -> Column {
                        Text(error!!, color = FV.AmberText)
                        TextButton(onClick = { reload++ }) { Text("Reintentar") }
                    }
                    members == null -> CircularProgressIndicator(color = FV.Green)
                    else -> Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                        Column {
                            members!!.forEachIndexed { i, m ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 80.dp))
                                ClickableRow(onClick = { if (isOwner) editingRole = m }) {
                                    InitialsAvatar(m.displayName)
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(m.displayName, style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                                        Text("@${m.username}", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                                        Spacer(Modifier.height(4.dp))
                                        RoleBadge(m.roleLabel, m.role == "owner")
                                    }
                                    if (isOwner) {
                                        IconButton(onClick = { editingRole = m }) { Icon(Icons.Filled.Badge, "Cambiar rol", tint = FV.Muted) }
                                    }
                                    if (isOwner && m.role != "owner") {
                                        IconButton(onClick = { removing = m }) { Icon(Icons.Filled.Delete, "Quitar acceso", tint = FV.Danger) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = { vm.logout() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(top = 8.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null); Spacer(Modifier.width(8.dp)); Text("Cerrar sesión")
                }
            }
        }
    }

    if (adding) {
        var name by remember { mutableStateOf("") }
        var user by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("Empleado") }
        var busy by remember { mutableStateOf(false) }
        var err by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!busy) adding = false },
            containerColor = FV.Surface,
            title = { Text("Nueva persona") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(user, { user = it.trim().lowercase() }, label = { Text("Usuario") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        pass, { pass = it }, label = { Text("Contraseña (mín. 6)") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Rol", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    RolePicker(title, forOwner = false) { title = it }
                    Text("Entrégale estos datos para que entre desde su teléfono.", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    err?.let { Text(it, color = FV.Danger, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                Button(
                    enabled = !busy && name.isNotBlank() && user.length >= 3 && pass.length >= 6,
                    onClick = {
                        busy = true; err = null
                        scope.launch {
                            try { vm.repo.addMember(name.trim(), user, pass, title); adding = false; reload++ }
                            catch (e: Exception) { err = vm.friendly(e) }
                            busy = false
                        }
                    },
                ) { Text(if (busy) "Creando…" else "Crear acceso") }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text("Cancelar") } },
        )
    }

    editingRole?.let { m ->
        var title by remember(m.username) { mutableStateOf(m.roleLabel) }
        var newName by remember(m.username) { mutableStateOf(m.displayName) }
        var newPass by remember(m.username) { mutableStateOf("") }
        val isMe = m.username == session?.username
        var busy by remember(m.username) { mutableStateOf(false) }
        var err by remember(m.username) { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!busy) editingRole = null },
            containerColor = FV.Surface,
            icon = { Icon(Icons.Filled.Badge, null, tint = FV.GreenDark) },
            title = { Text("Editar a ${m.displayName}") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        newName, { newName = it.take(60) }, label = { Text("Nombre visible") }, singleLine = true,
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    )
                    if (!isMe) {
                        OutlinedTextField(
                            newPass, { newPass = it }, label = { Text("Contraseña nueva (opcional)") }, singleLine = true,
                            supportingText = { Text("Si la olvidó: escribe una nueva y entrégasela") },
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Text("Rol", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    RolePicker(title, forOwner = m.role == "owner") { title = it }
                    Text(
                        if (m.role == "owner") "Sigue teniendo control total del negocio."
                        else "El rol se muestra en el equipo. Los permisos siguen siendo los de empleado.",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                    )
                    err?.let { Text(it, color = FV.Danger, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                Button(
                    enabled = !busy && title.isNotBlank() && newName.trim().length >= 2 && (newPass.isEmpty() || newPass.length >= 6),
                    onClick = {
                        busy = true; err = null
                        val nameChanged = newName.trim() != m.displayName
                        val finish = {
                            if (nameChanged || newPass.isNotEmpty()) {
                                vm.updateMember(m.username, if (nameChanged) newName.trim() else "", newPass,
                                    onOk = { busy = false; editingRole = null; reload++ },
                                    onError = { busy = false; err = it })
                            } else { busy = false; editingRole = null; reload++ }
                        }
                        if (title != m.roleLabel) {
                            vm.setMemberTitle(m.username, title, onOk = { finish() }, onError = { busy = false; err = it })
                        } else finish()
                    },
                ) { Text(if (busy) "Guardando…" else "Guardar") }
            },
            dismissButton = { TextButton(onClick = { editingRole = null }) { Text("Cancelar") } },
        )
    }

    removing?.let { m ->
        AlertDialog(
            onDismissRequest = { removing = null },
            containerColor = FV.Surface,
            title = { Text("Quitar acceso") },
            text = { Text("${m.displayName} ya no podrá entrar ni registrar ventas.") },
            confirmButton = {
                TextButton(onClick = {
                    removing = null
                    scope.launch {
                        try { vm.repo.removeMember(m.username); reload++ } catch (e: Exception) { error = vm.friendly(e) }
                    }
                }) { Text("Quitar", color = FV.Danger) }
            },
            dismissButton = { TextButton(onClick = { removing = null }) { Text("Cancelar") } },
        )
    }
}
