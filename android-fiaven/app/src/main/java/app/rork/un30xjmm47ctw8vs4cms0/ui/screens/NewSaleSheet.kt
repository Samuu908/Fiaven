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
import app.rork.un30xjmm47ctw8vs4cms0.ui.matches
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseDecimal
import app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber
import androidx.compose.material.icons.filled.Payments
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ScanButton
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.rememberBarcodeScanner
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

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

    val filtered = remember(query, data.products) { data.products.filter { it.matches(query) } }
    val inventory = data.inventory.enabled
    fun leftOf(p: Product): Int? = if (inventory) ledger.stock[p.id]?.left else null
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
        val left = leftOf(p)
        if (delta > 0 && left != null && qtyOf(p) > left) {
            error = if (left <= 0) "${p.name} está agotado según el inventario" else "Solo quedan $left de ${p.name}"
        }
    }
    val scan = rememberBarcodeScanner(
        onResult = { code ->
            val p = vm.productByBarcode(code)
            if (p != null) { change(p, 1); query = "" } else error = "No hay producto con el código $code"
        },
        onError = { error = it },
    )

    var step by remember { mutableStateOf(0) } // 0 = elegir productos, 1 = cobrar
    val units = cart.sumOf { it.qty }
    fun changeLine(line: CartLine, delta: Int) {
        val p = line.productId?.let { id -> data.products.find { it.id == id } }
        if (p != null) { change(p, delta); return }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val i = cart.indexOfFirst { it.key == line.key }
        if (i >= 0) { val q = cart[i].qty + delta; if (q <= 0) cart.removeAt(i) else cart[i] = cart[i].copy(qty = q) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FV.Canvas,
    ) {
        BackHandler(enabled = step == 1) { step = 0; error = null }
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir / 3 } + fadeOut())
            },
            label = "saleStep",
        ) { current ->
        if (current == 0) {
        Column(Modifier.fillMaxHeight(0.94f).imePadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Nueva venta", style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                    Text("Paso 1 de 2 · Elige todo lo que se lleva", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar") }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar producto o código...") },
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
                                Modifier.fillMaxWidth()
                                    .background(if (q > 0) FV.GreenTint else Color.Transparent)
                                    .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("${p.name} · ${usd(p.priceCents)}", style = MaterialTheme.typography.bodyLarge, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    val left = leftOf(p)
                                    if (left != null) {
                                        val rest = left - q
                                        Text(
                                            "${bs(p.priceCents, ledger.effectiveRate)} · " + if (rest <= 0) "agotado" else "quedan $rest",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = when { rest < 0 -> FV.Danger; rest <= 3 -> FV.AmberText; else -> FV.Muted },
                                        )
                                    } else {
                                        Text(bs(p.priceCents, ledger.effectiveRate), style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                                    }
                                }
                                if (q > 0) {
                                    FilledTonalIconButton(onClick = { change(p, -1) }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Filled.Remove, "Quitar uno", modifier = Modifier.size(18.dp))
                                    }
                                    Text("$q", style = MaterialTheme.typography.titleMedium, color = FV.Ink, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
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
            error?.let { Text(it, color = FV.Danger, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (cart.isEmpty()) error = "Agrega al menos un producto"
                    else { error = null; query = ""; step = 1 }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                if (cart.isEmpty()) {
                    Text("Elige productos", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        "OK · $units ${if (units == 1) "artículo" else "artículos"}",
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f),
                    )
                    Text(usd(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        } else {
        Column(Modifier.fillMaxHeight(0.94f).imePadding().navigationBarsPadding().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { step = 0; error = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a productos") }
                Column(Modifier.weight(1f)) {
                    Text("Cobrar", style = MaterialTheme.typography.headlineSmall, color = FV.Ink)
                    Text("Paso 2 de 2 · Revisa y guarda la venta", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar") }
            }
            Spacer(Modifier.height(10.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Surface(
                shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(vertical = 4.dp)) {
                    cart.forEachIndexed { i, line ->
                        if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(line.name, style = MaterialTheme.typography.bodyLarge, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "${line.qty} × ${usd(line.priceCents)} = ${usd(line.priceCents * line.qty)}",
                                    style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                                )
                            }
                            FilledTonalIconButton(onClick = { changeLine(line, -1) }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Filled.Remove, "Quitar uno", modifier = Modifier.size(16.dp))
                            }
                            Text("${line.qty}", style = MaterialTheme.typography.titleMedium, color = FV.Ink, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                            FilledTonalIconButton(
                                onClick = { changeLine(line, 1) },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = FV.GreenSoft, contentColor = FV.Green),
                            ) { Icon(Icons.Filled.Add, "Agregar uno", modifier = Modifier.size(16.dp)) }
                        }
                    }
                    if (cart.isEmpty()) {
                        Text(
                            "No quedan productos. Vuelve atrás para agregar.",
                            style = MaterialTheme.typography.bodyMedium, color = FV.Muted, modifier = Modifier.padding(16.dp),
                        )
                    }
                    HorizontalDivider(color = FV.Hairline)
                    TextButton(onClick = { step = 0; error = null }, modifier = Modifier.padding(horizontal = 6.dp)) {
                        Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agregar más productos")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = !fiado, onClick = { fiado = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Contado") }
                SegmentedButton(
                    selected = fiado, onClick = { fiado = true }, shape = SegmentedButtonDefaults.itemShape(1, 2),
                    colors = SegmentedButtonDefaults.colors(activeContainerColor = FV.AmberSoft, activeContentColor = FV.AmberText),
                ) { Text("Fiado") }
            }

            AnimatedVisibility(!fiado && total > 0) {
                ChangeCalculator(total, ledger.effectiveRate)
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
            Spacer(Modifier.height(4.dp))
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

/** "Paga con" helper for cash sales: shows the change to hand back, in dollars and bolívares. */
@Composable
private fun ChangeCalculator(totalCents: Long, rate: Double) {
    var currency by remember { mutableStateOf("USD") }
    var paid by remember { mutableStateOf("") }
    val canBs = rate > 0
    val totalUsd = totalCents / 100.0
    val totalInCurrency = if (currency == "USD") totalUsd else totalUsd * rate
    val paidValue = parseDecimal(paid)?.toDouble()
    // Everything in USD for the math; small epsilon avoids "falta 0,00" from rounding.
    val paidUsd = paidValue?.let { if (currency == "USD") it else if (canBs) it / rate else null }
    val diffUsd = paidUsd?.minus(totalUsd)
    val bills = if (currency == "USD") listOf(5, 10, 20, 50, 100) else listOf(100, 200, 500, 1000, 2000)

    Surface(
        shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline),
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Payments, null, tint = FV.Green, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Calcular vuelto", style = MaterialTheme.typography.titleSmall, color = FV.Ink, modifier = Modifier.weight(1f))
                SingleChoiceSegmentedButtonRow(Modifier.width(140.dp)) {
                    SegmentedButton(
                        selected = currency == "USD", onClick = { currency = "USD"; paid = "" },
                        shape = SegmentedButtonDefaults.itemShape(0, 2), icon = {},
                    ) { Text("$", style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = currency == "BS", enabled = canBs, onClick = { currency = "BS"; paid = "" },
                        shape = SegmentedButtonDefaults.itemShape(1, 2), icon = {},
                    ) { Text("Bs", style = MaterialTheme.typography.labelMedium) }
                }
            }
            OutlinedTextField(
                value = paid,
                onValueChange = { paid = it },
                placeholder = { Text(if (currency == "USD") "¿Con cuánto paga? (en $)" else "¿Con cuánto paga? (en Bs)") },
                supportingText = {
                    Text(
                        "Total a cobrar: " + if (currency == "USD") usd(totalCents) else "Bs " + formatNumber(totalInCurrency),
                        color = FV.Muted,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    SuggestionChip(
                        onClick = { paid = formatNumber(totalInCurrency).replace(".", "") },
                        label = { Text("Exacto") },
                    )
                }
                items(bills) { b ->
                    SuggestionChip(
                        onClick = { paid = b.toString() },
                        label = { Text(if (currency == "USD") "$ $b" else "Bs $b") },
                    )
                }
            }
            if (diffUsd != null) {
                val missing = diffUsd < -0.004
                Surface(shape = RoundedCornerShape(14.dp), color = if (missing) FV.DangerTint else FV.GreenTint, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        if (missing) {
                            val lack = -diffUsd
                            Text("Falta por pagar", style = MaterialTheme.typography.bodyMedium, color = FV.Danger)
                            Text(
                                "$ " + formatNumber(lack) + if (canBs) "  ·  Bs " + formatNumber(lack * rate) else "",
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = FV.Danger,
                            )
                        } else {
                            val change = diffUsd.coerceAtLeast(0.0)
                            Text("Vuelto a entregar", style = MaterialTheme.typography.bodyMedium, color = FV.Muted)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    if (currency == "USD") "$ " + formatNumber(change) else "Bs " + formatNumber(change * rate),
                                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = FV.Green,
                                )
                                if (canBs) {
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "o " + if (currency == "USD") "Bs " + formatNumber(change * rate) else "$ " + formatNumber(change),
                                        style = MaterialTheme.typography.titleMedium, color = FV.Muted,
                                        modifier = Modifier.padding(bottom = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
