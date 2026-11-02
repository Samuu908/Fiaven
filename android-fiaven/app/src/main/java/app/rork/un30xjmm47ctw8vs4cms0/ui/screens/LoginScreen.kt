package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Key
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.normalizeAnswer
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.AnswerField
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.CreatorCredit
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.PasswordField
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.QuestionPicker
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

@Composable
fun LoginScreen(vm: AppViewModel) {
    var creating by rememberSaveable { mutableStateOf(false) }
    var businessName by rememberSaveable { mutableStateOf("") }
    var ownerName by rememberSaveable { mutableStateOf("") }
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var showPass by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var question by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }
    var recovering by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val uri = LocalUriHandler.current
    val credits by vm.credits.collectAsStateWithLifecycle()

    fun submit() {
        focus.clearFocus()
        error = null
        if (creating) {
            when {
                businessName.isBlank() -> { error = "Escribe el nombre de tu negocio"; return }
                ownerName.isBlank() -> { error = "Escribe tu nombre"; return }
                user.trim().length < 3 -> { error = "El usuario debe tener al menos 3 caracteres"; return }
                pass.length < 6 -> { error = "La contraseña debe tener al menos 6 caracteres"; return }
                question.isBlank() -> { error = "Elige una pregunta de seguridad"; return }
                normalizeAnswer(answer).length < 2 -> { error = "Escribe la respuesta a tu pregunta"; return }
            }
        } else if (user.isBlank() || pass.isBlank()) { error = "Escribe tu usuario y contraseña"; return }
        loading = true
        val onError: (String) -> Unit = { loading = false; error = it }
        // Username is case-insensitive (the server normalizes it); the password is sent exactly as typed.
        if (creating) vm.register(businessName.trim(), ownerName.trim(), user.trim(), pass, question, answer, onError)
        else vm.login(user.trim(), pass, onError)
    }

    Surface(color = FV.Canvas, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(if (creating) 40.dp else 72.dp))
            IconBadge(Icons.Filled.Eco, FV.Green, FV.GreenSoft, 84)
            Spacer(Modifier.height(20.dp))
            Text("FiaVen", fontSize = 44.sp, fontWeight = FontWeight.SemiBold, color = FV.GreenDark, letterSpacing = (-1).sp)
            Spacer(Modifier.height(6.dp))
            Text(
                if (creating) "Crea tu negocio en un minuto. Serás el dueño y luego podrás dar acceso a tu equipo."
                else "Ventas, fiados y caja en $ y Bs para cualquier negocio",
                style = MaterialTheme.typography.titleMedium, color = FV.Muted, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AnimatedVisibility(creating) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Field(businessName, { businessName = it.take(60) }, "Nombre del negocio", Icons.Filled.Storefront, capitalize = true)
                        Field(ownerName, { ownerName = it.take(40) }, "Tu nombre", Icons.Filled.Badge, capitalize = true)
                    }
                }
                Field(
                    user, { user = it.replace(" ", "").take(30) },
                    if (creating) "Elige un usuario" else "Usuario", Icons.Filled.Person,
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, if (showPass) "Ocultar" else "Mostrar")
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                AnimatedVisibility(creating) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Si olvidas tu contraseña, la recuperas respondiendo esta pregunta. No necesitas correo ni teléfono.",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp),
                        )
                        QuestionPicker(question, { question = it })
                        AnswerField(answer, { answer = it })
                    }
                }
            }

            if (!creating) {
                TextButton(
                    onClick = { recovering = true; error = null },
                    enabled = !loading,
                    modifier = Modifier.align(Alignment.End),
                ) { Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelLarge, color = FV.GreenDark) }
            }

            AnimatedVisibility(error != null) {
                Text(
                    error.orEmpty(), color = FV.Danger, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp), textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(if (creating) 24.dp else 12.dp))
            Button(
                onClick = { submit() },
                enabled = !loading,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(22.dp), color = FV.Surface, strokeWidth = 2.dp)
                else Text(if (creating) "Crear mi negocio" else "Entrar", style = MaterialTheme.typography.titleMedium)
            }
            if (creating) {
                Text(
                    "Al crear tu negocio aceptas la Política de privacidad",
                    style = MaterialTheme.typography.bodySmall, color = FV.GreenDark, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        .clickable { runCatching { uri.openUri(vm.privacyUrl) } }.padding(vertical = 6.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = { creating = !creating; error = null },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (creating) "Ya tengo cuenta · Entrar" else "¿Nuevo aquí? Crea tu negocio gratis",
                    style = MaterialTheme.typography.titleSmall, color = FV.GreenDark,
                )
            }
            if (!creating) {
                Text(
                    "¿Trabajas en un negocio que ya usa FiaVen? Pide tu usuario al dueño.",
                    style = MaterialTheme.typography.bodySmall, color = FV.Muted, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                Icon(Icons.Filled.Lock, null, tint = FV.Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Tus datos se guardan en el teléfono y se sincronizan al conectarte",
                    style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                )
            }
            Spacer(Modifier.height(28.dp))
            CreatorCredit(credits)
            Spacer(Modifier.height(20.dp))
        }
    }

    if (recovering) RecoverDialog(vm, initialUser = user.trim(), onDismiss = { recovering = false })
}

/** Two steps: username → security question + answer + new password. */
@Composable
private fun RecoverDialog(vm: AppViewModel, initialUser: String, onDismiss: () -> Unit) {
    var user by rememberSaveable { mutableStateOf(initialUser) }
    var question by rememberSaveable { mutableStateOf<String?>(null) }
    var answer by rememberSaveable { mutableStateOf("") }
    var newPass by rememberSaveable { mutableStateOf("") }
    var newPass2 by rememberSaveable { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun next() {
        error = null
        if (question == null) {
            if (user.isBlank()) { error = "Escribe tu usuario"; return }
            loading = true
            vm.recoveryQuestion(user.trim(), onOk = { loading = false; question = it }, onError = { loading = false; error = it })
        } else {
            when {
                normalizeAnswer(answer).isEmpty() -> { error = "Escribe tu respuesta"; return }
                newPass.length < 6 -> { error = "La nueva contraseña debe tener al menos 6 caracteres"; return }
                newPass != newPass2 -> { error = "Las contraseñas no coinciden"; return }
            }
            loading = true
            // On success the session starts and the login screen goes away by itself.
            vm.recover(user.trim(), answer, newPass) { loading = false; error = it }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = FV.Surface,
        shape = RoundedCornerShape(24.dp),
        icon = { IconBadge(Icons.Filled.Key, FV.Green, FV.GreenSoft, 52) },
        title = { Text("Recuperar contraseña", textAlign = TextAlign.Center) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val q = question
                if (q == null) {
                    Text(
                        "Escribe tu usuario y te mostraremos tu pregunta de seguridad.",
                        style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                    )
                    Field(user, { user = it.replace(" ", "").take(30) }, "Usuario", Icons.Filled.Person)
                    Text(
                        "¿Eres trabajador y no configuraste una pregunta? Pide al dueño que te dé una contraseña nueva desde Equipo.",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                    )
                } else {
                    FvCard(container = FV.GreenSoft, border = FV.GreenSoft, contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                        Text("Tu pregunta", style = MaterialTheme.typography.labelMedium, color = FV.GreenDark)
                        Spacer(Modifier.height(4.dp))
                        Text(q, style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    }
                    AnswerField(answer, { answer = it })
                    PasswordField(newPass, { newPass = it }, "Nueva contraseña", imeAction = ImeAction.Next)
                    PasswordField(newPass2, { newPass2 = it }, "Repite la nueva contraseña")
                }
                AnimatedVisibility(error != null) {
                    Text(error.orEmpty(), color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = { next() }, enabled = !loading, shape = RoundedCornerShape(14.dp)) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = FV.Surface, strokeWidth = 2.dp)
                else Text(if (question == null) "Continuar" else "Cambiar y entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = { if (question != null && !loading) { question = null; error = null } else onDismiss() }, enabled = !loading) {
                Text(if (question != null) "Atrás" else "Cancelar", color = FV.Muted)
            }
        },
    )
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FV.Surface,
    unfocusedContainerColor = FV.Surface,
    unfocusedBorderColor = FV.Outline,
)

@Composable
private fun Field(value: String, onChange: (String) -> Unit, label: String, icon: ImageVector, capitalize: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = if (capitalize) KeyboardCapitalization.Words else KeyboardCapitalization.None,
            keyboardType = if (capitalize) KeyboardType.Text else KeyboardType.Ascii,
            imeAction = ImeAction.Next,
        ),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors(),
        modifier = Modifier.fillMaxWidth(),
    )
}
