package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Product
import app.rork.un30xjmm47ctw8vs4cms0.data.SaleItem
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseCents
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd

private data class CartLine(
    val key: String, val productId: String?, val name: String, val priceCents: Long, val qty: Int, val costCents: Long? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleSheet(vm: AppViewModel, onDismiss: () -> Unit, onSaved: (Boolean) -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val sync by vm.sync.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var query by remember { mutableStateOf("") }
    val cart = remember { mutableStateListOf<CartLine>() }
    var fiado by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf<String?>(null) }
    var showFree by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val filtered = remember(query, data.products) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) data.products else data.products.filter { it.name.lowercase().contains(q) }
    }
    val total = cart.sumOf { it.priceCents * it.qty }

    fun qtyOf(p: Product) = cart.find { it.key == p.id }?.qty ?: 0
    fun change(p: Product, delta: Int) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val i = cart.indexOfFirst { it.key == p.id }
        if (i < 0) { if (delta > 0) cart.add(CartLine(p.id, p.id, p.name, p.priceCents, 1, p.costCents)) }
        else {
            val q = cart[i].qty + delta
            if (q <= 0) cart.removeAt(i) else cart[i] = cart[i].copy(qty = q)
        }
        error = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FV.Canvas,
    ) {
        Column(Modifier.fillMaxHeight(0.94f).imePadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Nueva venta", style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                    Text("Agrega productos, elige el tipo y guarda la venta", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar") }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") } },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = FV.Surface,
                border = BorderStroke(1.dp, FV.Hairline),
            ) {
                if (data.products.isEmpty()) {
                    EmptyState(
                        Icons.Filled.Inventory2, "Tu catálogo está vacío",
                        "Agrega productos con su precio en la pestaña Catálogo, o usa “Monto libre”.",
                    )
                } else if (filtered.isEmpty()) {
                    EmptyState(Icons.Filled.Search, "Sin resultados", "No hay productos que coincidan con “$query”.")
                } else {
                    LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                        items(filtered, key = { it.id }) { p ->
                            val q = qtyOf(p)
                            Row(
                                Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${p.name} · ${usd(p.priceCents)}", style = MaterialTheme.typography.bodyLarge, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(bs(p.priceCents, ledger.effectiveRate), style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                                }
                                if (q > 0) {
                                    FilledTonalIconButton(onClick = { change(p, -1) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.Remove, "Quitar uno", modifier = Modifier.size(18.dp))
                                    }
                                    Text("$q", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                                FilledTonalIconButton(
                                    onClick = { change(p, 1) },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = FV.GreenSoft, contentColor = FV.Green),
                                ) { Icon(Icons.Filled.Add, "Agregar ${p.name}", modifier = Modifier.size(18.dp)) }
                            }
                            HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChip(onClick = { showFree = true }, label = { Text("Monto libre") }, leadingIcon = { Icon(Icons.Filled.Edit, null, Modifier.size(18.dp)) })
                }
                items(cart, key = { it.key }) { line ->
                    InputChip(
                        selected = true,
                        onClick = { cart.remove(line) },
                        label = { Text("${line.qty}× ${line.name}", maxLines = 1) },
                        trailingIcon = { Icon(Icons.Filled.Close, "Quitar", Modifier.size(16.dp)) },
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = !fiado, onClick = { fiado = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Contado") }
                SegmentedButton(
                    selected = fiado, onClick = { fiado = true }, shape = SegmentedButtonDefaults.itemShape(1, 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = FV.AmberSoft, activeContentColor = FV.AmberText),
                ) { Text("Fiado") }
            }

            AnimatedVisibility(fiado) {
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it; customerId = null; error = null },
                        placeholder = { Text("Cliente") },
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val suggestions = remember(customerName, data.customers, customerId) {
                        val q = customerName.trim().lowercase()
                        if (q.isEmpty() || customerId != null) emptyList()
                        else data.customers.filter { it.name.lowercase().contains(q) && !it.name.equals(customerName.trim(), true) }.take(5)
                    }
                    if (suggestions.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(suggestions, key = { it.id }) { c ->
                                SuggestionChip(
                                    onClick = { customerName = c.name; customerPhone = c.phone; customerId = c.id },
                                    label = { Text(c.name) },
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        placeholder = { Text("Teléfono") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = if (fiado) FV.AmberTint else FV.GreenTint, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(usd(total), style = MoneyLarge, color = if (fiado) FV.AmberText else FV.Green)
                        Spacer(Modifier.width(10.dp))
                        Text("· " + bs(total, ledger.effectiveRate), style = MaterialTheme.typography.titleMedium, color = FV.Muted, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    error?.let { Text(it, color = FV.Danger, style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            when {
                                cart.isEmpty() -> error = "Agrega al menos un producto"
                                fiado && customerName.isBlank() -> error = "Escribe el nombre del cliente"
                                else -> {
                                    vm.saveSale(
                                        cart.map { SaleItem(it.productId, it.name, it.priceCents, it.qty, it.costCents) },
                                        fiado, customerName, customerPhone, customerId,
                                    )
                                    onSaved(fiado)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    ) { Text(if (fiado) "Guardar fiado" else "Guardar venta", style = MaterialTheme.typography.titleMedium) }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudUpload, null, tint = FV.Muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (sync.online) "Se verá al instante en los otros teléfonos" else "Se guardará y se subirá al tener internet",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showFree) {
        FreeAmountDialog(
            onDismiss = { showFree = false },
            onAdd = { name, cents ->
                cart.add(CartLine("free-" + System.nanoTime(), null, name, cents, 1))
                showFree = false
                error = null
            },
        )
    }
}

@Composable
private fun FreeAmountDialog(onDismiss: () -> Unit, onAdd: (String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val cents = parseCents(amount)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text("Monto libre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it }, label = { Text("Monto en $") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Descripción (opcional)") },
                    singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(enabled = cents != null && cents > 0, onClick = { onAdd(name.ifBlank { "Venta rápida" }, cents ?: 0) }) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
