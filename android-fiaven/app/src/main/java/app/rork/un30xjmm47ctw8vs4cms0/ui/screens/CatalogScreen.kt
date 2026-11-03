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
import androidx.compose.foundation.verticalScroll
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.DEFAULT_MIN_STOCK
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.centsToInput
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseCents
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import app.rork.un30xjmm47ctw8vs4cms0.ui.StockInfo
import app.rork.un30xjmm47ctw8vs4cms0.ui.matches
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ScanButton
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.rememberBarcodeScanner
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Box
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(vm: AppViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val isOwner = session?.isOwner == true
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }
    var newBarcode by remember { mutableStateOf<String?>(null) }
    val inventory = data.inventory.enabled
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scan = rememberBarcodeScanner(
        onResult = { code ->
            val p = vm.productByBarcode(code)
            when {
                p != null && isOwner -> editing = p
                p != null -> query = code
                isOwner -> { newBarcode = code; creating = true }
                else -> scope.launch { snackbar.showSnackbar("No hay producto con el código $code") }
            }
        },
        onError = { msg -> scope.launch { snackbar.showSnackbar(msg) } },
    )

    val list = remember(data.products, query) { data.products.filter { it.matches(query) } }

    Box(Modifier.fillMaxSize()) {
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
                placeholder = { Text("Buscar producto o código") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") }
                        ScanButton(scan)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (isOwner || inventory) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Filled.Warehouse, FV.Green, FV.GreenTint, size = 40)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Controlar inventario", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                            Text(
                                if (inventory) "Ves cuántos quedan y cuántos se vendieron" else "Opcional: lleva la cuenta de existencias",
                                style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                            )
                        }
                        if (isOwner) {
                            Switch(
                                checked = inventory, onCheckedChange = { vm.setInventory(it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = FV.Green),
                            )
                        }
                    }
                }
            }
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
                                    if (inventory) StockLine(ledger.stock[p.id])
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

    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (creating || editing != null) {
        ProductDialog(
            product = editing,
            rate = ledger.effectiveRate,
            inventory = inventory,
            stockLeft = editing?.let { ledger.stock[it.id]?.left },
            initialBarcode = newBarcode,
            onDismiss = { creating = false; editing = null; newBarcode = null },
            onSave = { name, cents, cost, code, stock, min ->
                vm.saveProduct(editing?.id, name, cents, cost, code, stock, min)
                creating = false; editing = null; newBarcode = null
            },
            onDelete = editing?.let { p -> { vm.deleteProduct(p); editing = null } },
        )
    }
}

@Composable
private fun ProductDialog(
    product: Product?,
    rate: Double,
    inventory: Boolean,
    stockLeft: Int?,
    initialBarcode: String?,
    onDismiss: () -> Unit,
    onSave: (String, Long, Long?, String?, Int?, Int?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var minText by remember { mutableStateOf(product?.minStock?.toString().orEmpty()) }
    var barcode by remember { mutableStateOf(product?.barcode ?: initialBarcode.orEmpty()) }
    val stockStart = stockLeft?.toString().orEmpty()
    var stock by remember { mutableStateOf(stockStart) }
    val stockValue = stock.trim().toIntOrNull()
    val stockInvalid = stock.isNotBlank() && stockValue == null
    val scanCode = rememberBarcodeScanner(onResult = { barcode = it })
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
            Column(
                Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
                OutlinedTextField(
                    barcode, { barcode = it.trim() }, label = { Text("Código de barras (opcional)") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.QrCode2, null) },
                    trailingIcon = { ScanButton(scanCode) },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                )
                if (inventory) {
                    OutlinedTextField(
                        stock, { stock = it.filter { c -> c.isDigit() || c == '-' }.take(7) },
                        label = { Text("Unidades en existencia") }, singleLine = true, isError = stockInvalid,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Cuenta lo que tienes ahora; cada venta lo irá descontando.",
                        color = FV.Muted, style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        minText, { minText = it.filter { c -> c.isDigit() }.take(5) },
                        label = { Text("Avisar cuando queden") }, singleLine = true,
                        placeholder = { Text("$DEFAULT_MIN_STOCK") },
                        suffix = { Text("unidades") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        "Verás un aviso en Caja cuando quede esa cantidad o menos.",
                        color = FV.Muted, style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (confirmDelete) Text("¿Borrar este producto? Las ventas anteriores no cambian.", color = FV.Danger)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && cents != null && cents > 0 && !costInvalid && !stockInvalid,
                onClick = {
                    val newStock = if (inventory && stock.trim() != stockStart) stockValue else null
                    onSave(name, cents ?: 0, costCents, barcode, newStock, minText.toIntOrNull() ?: product?.minStock)
                },
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

@Composable
private fun StockLine(info: StockInfo?) {
    val left = info?.left
    val sold = info?.sold ?: 0
    val (text, color) = when {
        left == null -> "Sin contar · vendidos $sold" to FV.Muted
        left <= 0 -> "Agotado · vendidos $sold" to FV.Danger
        left <= 3 -> "Quedan $left · vendidos $sold" to FV.AmberText
        else -> "Quedan $left · vendidos $sold" to FV.Green
    }
    Text(text, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Medium)
}
