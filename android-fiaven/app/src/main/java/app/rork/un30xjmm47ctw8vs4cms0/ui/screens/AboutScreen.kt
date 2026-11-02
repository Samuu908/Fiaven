package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.CreditLink
import app.rork.un30xjmm47ctw8vs4cms0.data.Credits
import app.rork.un30xjmm47ctw8vs4cms0.data.SocialNetwork
import app.rork.un30xjmm47ctw8vs4cms0.data.cleanHandle
import app.rork.un30xjmm47ctw8vs4cms0.data.display
import app.rork.un30xjmm47ctw8vs4cms0.data.url
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.SectionHeader
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

private fun SocialNetwork.icon(): ImageVector = when (this) {
    SocialNetwork.INSTAGRAM -> Icons.Filled.PhotoCamera
    SocialNetwork.TIKTOK -> Icons.Filled.MusicNote
    SocialNetwork.FACEBOOK -> Icons.Filled.ThumbUp
    SocialNetwork.X -> Icons.Filled.AlternateEmail
    SocialNetwork.YOUTUBE -> Icons.Filled.PlayCircle
    SocialNetwork.WHATSAPP -> Icons.Filled.Phone
    SocialNetwork.WEB -> Icons.Filled.Language
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(vm: AppViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val credits by vm.credits.collectAsStateWithLifecycle()
    val canEdit by vm.canEditCredits.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    var editing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Acerca de") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = {
                    if (canEdit) IconButton(onClick = { editing = true }) { Icon(Icons.Filled.Edit, "Editar créditos") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Image(
                painterResource(app.rork.un30xjmm47ctw8vs4cms0.R.drawable.kashly_logo), "Kashly",
                modifier = Modifier.size(84.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp)),
            )
            Text("Kashly", fontSize = 36.sp, fontWeight = FontWeight.SemiBold, color = FV.GreenDark, letterSpacing = (-1).sp)
            Text("Versión ${app.rork.un30xjmm47ctw8vs4cms0.BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
            Spacer(Modifier.height(8.dp))

            FvCard(Modifier.fillMaxWidth()) {
                Text("Creado por", style = MaterialTheme.typography.labelLarge, color = FV.Muted)
                Spacer(Modifier.height(2.dp))
                Text(credits.name, style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                if (credits.links.isNotEmpty()) Spacer(Modifier.height(8.dp))
                credits.links.forEachIndexed { i, link ->
                    if (i > 0) HorizontalDivider(color = FV.Hairline)
                    val net = SocialNetwork.of(link.network)
                    ClickableRowCompact(onClick = { runCatching { uri.openUri(link.url()) } }) {
                        IconBadge(net.icon(), FV.Green, FV.GreenSoft, 38)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(net.label, style = MaterialTheme.typography.labelMedium, color = FV.Muted)
                            Text(link.display(), style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                        }
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = FV.Muted, modifier = Modifier.size(18.dp))
                    }
                }
                if (canEdit) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Solo tú ves el botón para editar. Los demás solo pueden verlo.",
                        style = MaterialTheme.typography.bodySmall, color = FV.GreenDark,
                    )
                }
            }

            SectionHeader("Legal", Modifier.fillMaxWidth())
            FvCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) {
                ClickableRow({ runCatching { uri.openUri(vm.privacyUrl) } }) {
                    Icon(Icons.Filled.Policy, null, tint = FV.Muted)
                    Spacer(Modifier.width(16.dp))
                    Text("Política de privacidad", style = MaterialTheme.typography.titleSmall, color = FV.Ink, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = FV.Muted, modifier = Modifier.size(18.dp))
                }
                HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                ClickableRow({ runCatching { uri.openUri(vm.deleteAccountUrl) } }) {
                    Icon(Icons.Filled.DeleteForever, null, tint = FV.Muted)
                    Spacer(Modifier.width(16.dp))
                    Text("Cómo eliminar tu cuenta", style = MaterialTheme.typography.titleSmall, color = FV.Ink, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = FV.Muted, modifier = Modifier.size(18.dp))
                }
            }
            Text(
                "Hecho en Venezuela para los negocios que trabajan en \$ y Bs.",
                style = MaterialTheme.typography.bodySmall, color = FV.Muted, textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }

    if (editing) CreditsDialog(vm, credits, onDismiss = { editing = false }, onSaved = { editing = false; onSaved() })
}

@Composable
private fun ClickableRowCompact(onClick: () -> Unit, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}


@Composable
private fun CreditsDialog(vm: AppViewModel, initial: Credits, onDismiss: () -> Unit, onSaved: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    val links = remember { mutableStateListOf<CreditLink>().apply { addAll(initial.links) } }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = FV.Surface,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Editar créditos") },
        text = {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it.take(60) },
                    label = { Text("Nombre que se muestra") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(),
                )
                Text("Redes", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                links.forEachIndexed { i, link -> LinkEditor(link, onChange = { links[i] = it }, onRemove = { links.removeAt(i) }) }
                if (links.size < 8) {
                    TextButton(onClick = { links.add(CreditLink("instagram", "")) }) {
                        Icon(Icons.Filled.Add, null, tint = FV.GreenDark)
                        Spacer(Modifier.width(6.dp))
                        Text("Añadir red", color = FV.GreenDark)
                    }
                }
                AnimatedVisibility(error != null) {
                    Text(error.orEmpty(), color = FV.Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = links.map { it.copy(handle = it.cleanHandle()) }.filter { it.handle.isNotBlank() }
                    if (name.trim().length < 2) { error = "Escribe un nombre"; return@Button }
                    loading = true
                    error = null
                    vm.saveCredits(Credits(name.trim(), clean), onOk = { loading = false; onSaved() }, onError = { loading = false; error = it })
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
private fun LinkEditor(link: CreditLink, onChange: (CreditLink) -> Unit, onRemove: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val net = SocialNetwork.of(link.network)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                TextButton(onClick = { menu = true }) {
                    Icon(net.icon(), null, tint = FV.GreenDark, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(net.label, color = FV.GreenDark)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = FV.Surface) {
                    SocialNetwork.entries.forEach { n ->
                        DropdownMenuItem(
                            text = { Text(n.label) },
                            leadingIcon = { Icon(n.icon(), null) },
                            onClick = { menu = false; onChange(link.copy(network = n.id)) },
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, "Quitar", tint = FV.Muted) }
        }
        OutlinedTextField(
            value = link.handle,
            onValueChange = { onChange(link.copy(handle = it.replace(" ", "").take(80))) },
            label = {
                Text(
                    when (net) {
                        SocialNetwork.WHATSAPP -> "Número con código de país"
                        SocialNetwork.WEB -> "Dirección web"
                        else -> "Usuario"
                    }
                )
            },
            prefix = { if (net.prefix.isNotEmpty()) Text(net.prefix, color = FV.Muted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = when (net) {
                    SocialNetwork.WHATSAPP -> KeyboardType.Phone
                    SocialNetwork.WEB -> KeyboardType.Uri
                    else -> KeyboardType.Ascii
                },
            ),
            shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(),
        )
    }
}

