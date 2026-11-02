package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.InitialsAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd

@Composable
fun FiadosScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var showPaid by rememberSaveable { mutableStateOf(false) }
    val owing = ledger.balances.count { it.owedCents > 0 }
    val list = remember(ledger.balances, query, showPaid) {
        val q = query.trim().lowercase()
        ledger.balances
            .filter { showPaid || it.owedCents > 0 }
            .filter { q.isEmpty() || it.name.lowercase().contains(q) || it.phone.contains(q) }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FvCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total por cobrar", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
                    IconBadge(Icons.Filled.People, FV.AmberText, FV.AmberSoft)
                }
                Text(usd(ledger.fiadoOutstandingCents), style = MoneyLarge, color = FV.AmberText)
                Text(
                    "$owing ${if (owing == 1) "cliente" else "clientes"} · ${bs(ledger.fiadoOutstandingCents, ledger.effectiveRate)}",
                    style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar cliente") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") } },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            FilterChip(selected = showPaid, onClick = { showPaid = !showPaid }, label = { Text("Mostrar clientes al día") })
        }
        if (list.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.People,
                    if (ledger.balances.isEmpty()) "Nadie te debe" else "Sin resultados",
                    if (ledger.balances.isEmpty()) "Cuando anotes una venta como “Fiado”, el cliente aparecerá aquí con su saldo."
                    else "Prueba con otro nombre o teléfono.",
                )
            }
        } else {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = androidx.compose.foundation.BorderStroke(1.dp, FV.Hairline)) {
                    Column {
                        list.forEachIndexed { index, b ->
                            if (index > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 80.dp))
                            ClickableRow(onClick = { onOpen(b.customerId) }) {
                                InitialsAvatar(b.name)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(b.name, style = MaterialTheme.typography.titleMedium, color = FV.Ink)
                                    Text(b.phone.ifBlank { "Sin teléfono" }, style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                                    Row {
                                        Text("Abonó ${usd(b.paidCents)}", style = MaterialTheme.typography.bodyMedium, color = FV.Green)
                                        Text("  ·  ", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                                        Text(
                                            if (b.owedCents > 0) "Debe ${usd(b.owedCents)}" else "Al día",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (b.owedCents > 0) FV.AmberText else FV.Green,
                                        )
                                    }
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
