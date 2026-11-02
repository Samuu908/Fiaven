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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Product
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.centsToInput
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseCents
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd

@Composable
fun CatalogScreen(vm: AppViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }

    val list = remember(data.products, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) data.products else data.products.filter { it.name.lowercase().contains(q) }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Catálogo", style = MaterialTheme.typography.titleLarge, color = FV.Ink)
                    Text(
                        "${data.products.size} ${if (data.products.size == 1) "producto" else "productos"} con precio en $",
                        style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                    )
                }
                if (isOwner) {
                    FilledTonalButton(onClick = { creating = true }, shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar")
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar producto") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") } },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (!isOwner) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, null, tint = FV.Muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Solo el dueño puede cambiar precios", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                }
            }
        }
        if (list.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.Inventory2,
                    if (data.products.isEmpty()) "Aún no hay productos" else "Sin resultados",
                    if (data.products.isEmpty()) {
                        if (isOwner) "Agrega tus productos con su precio en dólares para vender más rápido." else "El dueño todavía no ha cargado productos."
                    } else "Prueba con otro nombre.",
                )
            }
        } else {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                    Column {
                        list.forEachIndexed { i, p ->
                            if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 74.dp))
                            ClickableRow(onClick = { if (isOwner) editing = p }) {
                                IconBadge(Icons.Filled.Inventory2, FV.Green, FV.GreenTint)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        p.name, style = MaterialTheme.typography.bodyLarge, color = FV.Ink,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                                    )
                                    val cost = p.costCents
                                    if (isOwner && cost != null) {
                                        Text(
                                            "Compra ${usd(cost)} · Ganas ${usd(p.priceCents - cost)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (p.priceCents >= cost) FV.Green else FV.Danger,
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(usd(p.priceCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = FV.Ink)
                                    Text(bs(p.priceCents, ledger.effectiveRate), style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        ProductDialog(
            product = editing,
            rate = ledger.effectiveRate,
            onDismiss = { creating = false; editing = null },
            onSave = { name, cents, cost ->
                vm.saveProduct(editing?.id, name, cents, cost)
                creating = false; editing = null
            },
            onDelete = editing?.let { p -> { vm.deleteProduct(p); editing = null } },
        )
    }
}

@Composable
private fun ProductDialog(
    product: Product?,
    rate: Double,
    onDismiss: () -> Unit,
    onSave: (String, Long, Long?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(product?.name.orEmpty()) }
    var price by remember { mutableStateOf(product?.let { centsToInput(it.priceCents) }.orEmpty()) }
    var cost by remember { mutableStateOf(product?.costCents?.let { centsToInput(it) }.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }
    val cents = parseCents(price)
    val costCents = if (cost.isBlank()) null else parseCents(cost)
    val costInvalid = cost.isNotBlank() && costCents == null
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text(if (product == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    name, { name = it }, label = { Text("Nombre") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    price, { price = it }, label = { Text("Precio de venta en $") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                if (cents != null && rate > 0) Text("Equivale a ${bs(cents, rate)}", color = FV.Muted)
                OutlinedTextField(
                    cost, { cost = it }, label = { Text("Precio de compra en $ (opcional)") }, singleLine = true,
                    isError = costInvalid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                if (cents != null && costCents != null) {
                    val gain = cents - costCents
                    Text(
                        if (gain >= 0) "Ganas ${usd(gain)} por unidad" else "Pierdes ${usd(-gain)} por unidad",
                        color = if (gain >= 0) FV.Green else FV.Danger,
                    )
                } else {
                    Text(
                        "Con el precio de compra verás cuánto dinero te queda libre en el resumen.",
                        color = FV.Muted, style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (confirmDelete) Text("¿Borrar este producto? Las ventas anteriores no cambian.", color = FV.Danger)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && cents != null && cents > 0 && !costInvalid,
                onClick = { onSave(name, cents ?: 0, costCents) },
            ) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = { if (confirmDelete) onDelete() else confirmDelete = true }) {
                        Text(if (confirmDelete) "Sí, borrar" else "Borrar", color = FV.Danger)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        },
    )
}
