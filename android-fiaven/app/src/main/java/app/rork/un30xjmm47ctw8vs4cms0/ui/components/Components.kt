package app.rork.un30xjmm47ctw8vs4cms0.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.rork.un30xjmm47ctw8vs4cms0.data.SyncState
import app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber
import app.rork.un30xjmm47ctw8vs4cms0.ui.initials
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

@Composable
fun FvCard(
    modifier: Modifier = Modifier,
    container: Color = FV.Surface,
    border: Color = FV.Hairline,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "press")
    val shape = RoundedCornerShape(20.dp)
    val mod = modifier.scale(scale)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = mod,
            shape = shape,
            color = container,
            border = BorderStroke(1.dp, border),
            interactionSource = interaction,
        ) { Column(Modifier.padding(contentPadding), content = content) }
    } else {
        Surface(modifier = mod, shape = shape, color = container, border = BorderStroke(1.dp, border)) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun RateChip(rate: Double, isBcv: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = FV.GreenSoft,
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SwapHoriz, null, tint = FV.Green, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            val label = if (isBcv) "Tasa BCV" else "Tasa personalizada"
            Text(
                if (rate > 0) "$label: Bs ${formatNumber(rate)} / \$1" else "Configura la tasa del día",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = FV.GreenDark,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun SyncBanner(sync: SyncState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val visible = !sync.online || sync.pending > 0
    AnimatedVisibility(visible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut(), modifier = modifier) {
        val offline = !sync.online
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = if (offline) FV.AmberSoft else FV.GreenTint,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (offline) Icons.Filled.CloudOff else Icons.Filled.CloudUpload, null,
                    tint = if (offline) FV.AmberText else FV.Green, modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
                val text = when {
                    offline && sync.pending > 0 -> "Sin internet · ${sync.pending} ${if (sync.pending == 1) "cambio pendiente" else "cambios pendientes"} de subir"
                    offline -> "Sin internet · trabajando sin conexión"
                    else -> "Subiendo ${sync.pending} ${if (sync.pending == 1) "cambio" else "cambios"}…"
                }
                Text(
                    text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                    color = if (offline) FV.AmberText else FV.GreenDark, modifier = Modifier.weight(1f),
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = if (offline) FV.AmberText else FV.Green)
            }
        }
    }
}

@Composable
fun SyncedLabel(sync: SyncState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val ok = sync.online && sync.pending == 0
        Icon(
            if (ok) Icons.Filled.CloudDone else Icons.Filled.CloudOff, null,
            tint = if (ok) FV.Green else FV.AmberText, modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            when {
                ok && sync.realtime -> "Sincronizado en tiempo real"
                ok -> "Sincronizado"
                else -> "Guardado en el teléfono"
            },
            style = MaterialTheme.typography.labelMedium,
            color = if (ok) FV.Green else FV.AmberText,
        )
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, background: Color, size: Int = 44) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size((size * 0.5f).dp)) }
}

@Composable
fun InitialsAvatar(name: String, size: Int = 48) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(FV.GreenSoft),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(name), style = MaterialTheme.typography.titleMedium, color = FV.GreenDark, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = FV.Ink)
        Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(icon, FV.Green, FV.GreenSoft, 64)
        Spacer(Modifier.padding(6.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = FV.Ink)
        Spacer(Modifier.padding(2.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = FV.Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun ClickableRow(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
