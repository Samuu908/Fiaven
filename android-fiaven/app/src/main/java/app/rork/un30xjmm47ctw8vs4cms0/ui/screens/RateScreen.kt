package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseDecimal
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge

@Composable
fun RateScreen(vm: AppViewModel, onSaved: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val bcv by vm.bcv.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true

    var mode by remember(data.rate) { mutableStateOf(data.rate.mode) }
    var custom by remember(data.rate) {
        mutableStateOf(if (data.rate.customRate > 0) formatNumber(data.rate.customRate).replace(".", "") else "")
    }
    LaunchedEffect(Unit) { if (bcv.rate <= 0 && !bcv.loading) vm.refreshBcv() }

    val customValue = parseDecimal(custom)?.toDouble()
    val previewRate = if (mode == "bcv") bcv.rate else customValue ?: 0.0
    val canSave = isOwner && (mode == "bcv" && bcv.rate > 0 || mode == "custom" && (customValue ?: 0.0) > 0)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FvCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tasa oficial BCV", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
                if (bcv.loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else IconButton(onClick = { vm.refreshBcv() }) { Icon(Icons.Filled.Refresh, "Actualizar tasa BCV", tint = FV.Green) }
            }
            Text(if (bcv.rate > 0) "Bs ${formatNumber(bcv.rate)}" else "—", style = MoneyLarge, color = FV.Ink)
            Text(
                when {
                    bcv.error && bcv.rate <= 0 -> "No se pudo obtener. Revisa tu conexión."
                    bcv.error -> "Sin conexión · mostrando la última tasa guardada"
                    bcv.updated.isNotBlank() -> "Actualizada: ${bcv.updated}"
                    else -> "por cada \$1"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (bcv.error) FV.AmberText else FV.Muted,
            )
        }

        FvCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tasa que usa el negocio", style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                    Text("Se aplica en todos los teléfonos del equipo", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                }
                IconBadge(Icons.Filled.AccountBalance, FV.Green, FV.GreenSoft)
            }
            Spacer(Modifier.size(14.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = mode == "bcv", enabled = isOwner, onClick = { mode = "bcv" }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("BCV") }
                SegmentedButton(selected = mode == "custom", enabled = isOwner, onClick = { mode = "custom" }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Personalizada") }
            }
            if (mode == "custom") {
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = custom, onValueChange = { custom = it },
                    enabled = isOwner,
                    label = { Text("Bs por cada \$1") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.size(12.dp))
            Text(
                if (previewRate > 0) "\$ 1 = ${bs(100, previewRate)}   ·   \$ 10 = ${bs(1000, previewRate)}" else "Escribe una tasa para ver la conversión",
                style = MaterialTheme.typography.bodyLarge, color = FV.GreenDark,
            )
            if (data.rate.updatedBy.isNotBlank()) {
                Text("Última vez cambiada por ${data.rate.updatedBy}", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
        }

        if (isOwner) {
            Button(
                onClick = { vm.saveRate(mode, if (mode == "custom") customValue ?: 0.0 else data.rate.customRate); onSaved() },
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            ) { Text("Guardar tasa", style = MaterialTheme.typography.titleMedium) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                Icon(Icons.Filled.Lock, null, tint = FV.Muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Solo el dueño puede cambiar la tasa", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            }
        }
        Text(
            "Tasa en uso ahora: Bs ${formatNumber(ledger.effectiveRate)} por \$1. Cada venta guarda la tasa del momento.",
            style = MaterialTheme.typography.bodySmall, color = FV.Muted, modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
