package app.rork.un30xjmm47ctw8vs4cms0.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

data class MoreItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color? = null,
    val bg: Color? = null,
    val badge: String? = null,
    val onClick: () -> Unit,
)

data class MoreSection(val title: String, val items: List<MoreItem>)

/** Scrollable "Más" sheet: replaces the old ⋮ dropdown that overflowed small screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSheet(sections: List<MoreSection>, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = FV.Canvas) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text("Más opciones", style = MaterialTheme.typography.titleLarge, color = FV.Ink, fontWeight = FontWeight.SemiBold)
            sections.forEach { section ->
                Spacer(Modifier.height(18.dp))
                Text(
                    section.title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = FV.Muted,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                section.items.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { item ->
                            MoreTile(item, Modifier.weight(1f))
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreTile(item: MoreItem, modifier: Modifier) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(dampingRatio = 0.6f), label = "tile")
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .scale(scale)
            .clip(shape)
            .background(FV.Surface)
            .border(1.dp, FV.Hairline, shape)
            .clickable(interactionSource = source, indication = androidx.compose.material3.ripple(), onClick = item.onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(item.bg ?: FV.GreenTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(item.icon, null, tint = item.tint ?: FV.GreenDark, modifier = Modifier.size(22.dp))
            }
            item.badge?.let {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(start = 28.dp)
                        .clip(CircleShape)
                        .background(FV.Amber)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                ) {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.label,
            style = MaterialTheme.typography.labelLarge,
            color = FV.Ink,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
