package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.ClientStats
import app.rork.un30xjmm47ctw8vs4cms0.ui.agoLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.InitialsAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.customerMatches
import app.rork.un30xjmm47ctw8vs4cms0.ui.customerSubtitle
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd

private enum class ClientSort(val label: String) { FREQUENT("Más compras"), RECENT("Recientes"), SPENT("Más gastan"), OWING("Deben") }

/** Customer base: everyone who bought with their cédula, how much and how often. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(vm: AppViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(ClientSort.FREQUENT) }

    val clients = ledger.clients
    val buyers = clients.count { it.purchases > 0 }
    val frequent = clients.count { it.frequent }
    val returning = clients.count { it.visits >= 2 }
    val list = remember(clients, query, sort) {
        val q = query.trim()
        clients
            .filter { customerMatches(it.customer.name, it.customer.cedula, it.customer.phone, q) }
            .filter { sort != ClientSort.OWING || it.owedCents > 0 }
            .sortedWith(
                when (sort) {
                    ClientSort.FREQUENT -> compareByDescending<ClientStats> { it.purchases }.thenByDescending { it.lastPurchase ?: 0 }
                    ClientSort.RECENT -> compareByDescending { it.lastPurchase ?: it.customer.createdAt }
                    ClientSort.SPENT -> compareByDescending { it.spentCents }
                    ClientSort.OWING -> compareByDescending { it.owedCents }
                }
            )
    }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FvCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Clientes registrados", style = MaterialTheme.typography.titleSmall, color = FV.Muted, modifier = Modifier.weight(1f))
                        IconBadge(Icons.Filled.People, FV.Green, FV.GreenTint)
                    }
                    Text("${clients.size}", style = MoneyLarge, color = FV.Ink)
                    Text("Sin duplicados: una cédula, un cliente.", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    Spacer(Modifier.size(12.dp))
                    HorizontalDivider(color = FV.Hairline)
                    Spacer(Modifier.size(12.dp))
                    Row {
                        Stat("$buyers", "compraron", Modifier.weight(1f))
                        Stat("$returning", "volvieron", Modifier.weight(1f))
                        Stat("$frequent", "frecuentes", Modifier.weight(1f), FV.Green)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar por nombre, cédula o teléfono") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") } },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ClientSort.entries.toList()) { s ->
                        FilterChip(selected = sort == s, onClick = { sort = s }, label = { Text(s.label) })
                    }
                }
            }
            if (list.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.People,
                        if (clients.isEmpty()) "Todavía no hay clientes" else "Sin resultados",
                        if (clients.isEmpty()) "Cada venta guarda al cliente con su cédula. Aquí verás cuánto y cada cuánto compra."
                        else "Prueba con otro nombre, cédula o teléfono.",
                    )
                }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                        Column {
                            list.forEachIndexed { i, c ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 80.dp))
                                ClientRow(c) { onOpen(c.customer.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier, color: androidx.compose.ui.graphics.Color = FV.Ink) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = FV.Muted)
    }
}

@Composable
private fun ClientRow(c: ClientStats, onClick: () -> Unit) {
    ClickableRow(onClick = onClick) {
        InitialsAvatar(c.customer.name)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    c.customer.name, style = MaterialTheme.typography.titleMedium, color = FV.Ink,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
                )
                if (c.frequent) {
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = FV.GreenTint) {
                        Text("Frecuente", style = MaterialTheme.typography.labelSmall, color = FV.GreenDark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
            Text(customerSubtitle(c.customer.cedula, c.customer.phone), style = MaterialTheme.typography.bodySmall, color = FV.Muted, maxLines = 1)
            Text(
                if (c.purchases == 0) "Sin compras todavía"
                else listOfNotNull(
                    "${c.purchases} ${if (c.purchases == 1) "compra" else "compras"} · ${usd(c.spentCents)}",
                    c.lastPurchase?.let { "última ${agoLabel(it)}" },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Row {
                Text(c.frequencyLabel, style = MaterialTheme.typography.bodySmall, color = FV.Green)
                if (c.owedCents > 0) {
                    Text("  ·  ", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    Text("Debe ${usd(c.owedCents)}", style = MaterialTheme.typography.bodySmall, color = FV.AmberText)
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = FV.Muted)
    }
}
