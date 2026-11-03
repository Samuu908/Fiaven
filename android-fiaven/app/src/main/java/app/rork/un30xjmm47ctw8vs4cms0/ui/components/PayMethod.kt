package app.rork.un30xjmm47ctw8vs4cms0.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.rork.un30xjmm47ctw8vs4cms0.data.PAY_METHODS
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

/** Single-choice payment method chips; tapping the selected one clears it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PayMethodChips(selected: String, onSelect: (String) -> Unit, options: List<String> = PAY_METHODS, modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { m ->
            val on = m == selected
            FilterChip(
                selected = on,
                onClick = { onSelect(if (on) "" else m) },
                label = { Text(m) },
                leadingIcon = if (on) { { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) } } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FV.GreenSoft, selectedLabelColor = FV.GreenDark, selectedLeadingIconColor = FV.GreenDark,
                ),
            )
        }
    }
}
