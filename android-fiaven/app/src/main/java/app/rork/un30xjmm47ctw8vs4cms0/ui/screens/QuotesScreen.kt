package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import app.rork.un30xjmm47ctw8vs4cms0.ui.cedulaDigits
import app.rork.un30xjmm47ctw8vs4cms0.ui.matchesQuery
import app.rork.un30xjmm47ctw8vs4cms0.ui.prettyCedula
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.data.Product
import app.rork.un30xjmm47ctw8vs4cms0.data.Quote
import app.rork.un30xjmm47ctw8vs4cms0.data.SaleItem
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.bs
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.IconBadge
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.ScanButton
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.rememberBarcodeScanner
import app.rork.un30xjmm47ctw8vs4cms0.ui.matches
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseCents
import app.rork.un30xjmm47ctw8vs4cms0.ui.quoteNumber
import app.rork.un30xjmm47ctw8vs4cms0.ui.renderQuote
import app.rork.un30xjmm47ctw8vs4cms0.ui.shareImage
import app.rork.un30xjmm47ctw8vs4cms0.ui.shortDate
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.MoneyLarge
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd
import kotlinx.coroutines.launch

/** Opens WhatsApp (or the share sheet) with the quote rendered as an image. */
fun sendQuote(context: android.content.Context, quote: Quote, businessName: String, whatsapp: Boolean = true) {
    val bmp = renderQuote(quote, businessName)
    val text = "Hola ${quote.customerName.ifBlank { "" }}, te envío el presupuesto ${quoteNumber(quote.number)} por ${usd(quote.totalCents)}.".replace("  ", " ")
    runCatching { shareImage(context, bmp, "presupuesto-${quote.number}.png", text, quote.customerPhone, whatsapp) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(vm: AppViewModel, onBack: () -> Unit, onNew: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    var open by remember { mutableStateOf<Quote?>(null) }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Presupuestos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nuevo presupuesto") },
                containerColor = FV.Green, contentColor = FV.Surface,
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Arma un presupuesto con tu catálogo y envíalo como imagen por WhatsApp.",
                    style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                )
            }
            if (data.quotes.isEmpty()) {
                item { EmptyState(Icons.Filled.Description, "Sin presupuestos", "Toca “Nuevo presupuesto” para crear el primero.") }
            } else {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                        Column {
                            data.quotes.forEachIndexed { i, q ->
                                if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(start = 74.dp))
                                app.rork.un30xjmm47ctw8vs4cms0.ui.components.ClickableRow(onClick = { open = q }) {
                                    IconBadge(Icons.Filled.Description, FV.Green, FV.GreenTint)
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(q.customerName.ifBlank { "Cliente" }, style = MaterialTheme.typography.bodyLarge, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${quoteNumber(q.number)} · ${shortDate(q.createdAt)} · ${q.items.sumOf { it.qty }} art.",
                                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                                        )
                                    }
                                    Text(usd(q.totalCents), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = FV.Ink)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    open?.let { q -> QuotePreviewDialog(vm, q, onDismiss = { open = null }) }
}

@Composable
fun QuotePreviewDialog(vm: AppViewModel, quote: Quote, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val session by vm.session.collectAsStateWithLifecycle()
    val business = session?.businessName.orEmpty()
    val image = remember(quote, business) { renderQuote(quote, business).asImageBitmap() }
    var confirmDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), color = FV.Canvas, modifier = Modifier.fillMaxWidth(0.92f)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Presupuesto ${quoteNumber(quote.number)}", style = MaterialTheme.typography.titleLarge, color = FV.Ink, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar") }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FV.Hairline),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Image(image, "Vista del presupuesto", contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { sendQuote(context, quote, business) },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text("Enviar por WhatsApp")
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { sendQuote(context, quote, business, whatsapp = false) }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Compartir")
                    }
                    OutlinedButton(onClick = { confirmDelete = true }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Delete, null, Modifier.size(18.dp), tint = FV.Danger); Spacer(Modifier.width(6.dp)); Text("Borrar", color = FV.Danger)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = FV.Surface,
            title = { Text("¿Borrar este presupuesto?") },
            text = { Text("Se borrará de la lista en todos los teléfonos. Las imágenes ya enviadas no cambian.") },
            confirmButton = { TextButton(onClick = { vm.deleteQuote(quote); confirmDelete = false; onDismiss() }) { Text("Borrar", color = FV.Danger) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

private data class QuoteLine(val key: String, val productId: String?, val name: String, val priceCents: Long, val qty: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteBuilderScreen(vm: AppViewModel, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var customer by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var validDays by remember { mutableStateOf(7) }
    var showFree by remember { mutableStateOf(false) }
    val lines = remember { mutableStateListOf<QuoteLine>() }
    val total = lines.sumOf { it.priceCents * it.qty }

    fun change(p: Product, delta: Int) {
        val i = lines.indexOfFirst { it.key == p.id }
        if (i < 0) { if (delta > 0) lines.add(QuoteLine(p.id, p.id, p.name, p.priceCents, 1)) }
        else {
            val q = lines[i].qty + delta
            if (q <= 0) lines.removeAt(i) else lines[i] = lines[i].copy(qty = q)
        }
    }
    val scan = rememberBarcodeScanner(
        onResult = { code ->
            val p = vm.productByBarcode(code)
            if (p != null) { change(p, 1); scope.launch { snackbar.showSnackbar("Agregado: ${p.name}") } }
            else scope.launch { snackbar.showSnackbar("No hay producto con el código $code") }
        },
        onError = { msg -> scope.launch { snackbar.showSnackbar(msg) } },
    )
    val filtered = remember(query, data.products) { data.products.filter { it.matches(query) } }

    Scaffold(
        containerColor = FV.Canvas,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Nuevo presupuesto") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        bottomBar = {
            Surface(color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                Column(Modifier.navigationBarsPadding().imePadding().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(usd(total), style = MoneyLarge, color = FV.Green)
                        Spacer(Modifier.width(10.dp))
                        Text("· " + bs(total, ledger.effectiveRate), style = MaterialTheme.typography.titleMedium, color = FV.Muted, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val q = vm.saveQuote(customer, phone, lines.map { SaleItem(it.productId, it.name, it.priceCents, it.qty) }, note, validDays, cedula)
                            sendQuote(context, q, session?.businessName.orEmpty())
                            onBack()
                        },
                        enabled = lines.isNotEmpty() && customer.isNotBlank(),
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                        Text("Crear y enviar por WhatsApp", style = MaterialTheme.typography.titleMedium)
                    }
                    if (customer.isBlank() || lines.isEmpty()) {
                        Text(
                            if (customer.isBlank()) "Escribe el nombre del cliente" else "Agrega al menos un producto",
                            style = MaterialTheme.typography.bodySmall, color = FV.Muted,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FvCard(contentPadding = PaddingValues(16.dp)) {
                    Text("Cliente", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        customer, { customer = it; picked = false },
                        placeholder = { Text("Nombre del cliente") },
                        leadingIcon = { Icon(Icons.Filled.Person, null) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(),
                    )
                    val suggestions = remember(customer, cedula, data.customers, picked) {
                        val byCed = cedulaDigits(cedula)
                        when {
                            picked -> emptyList()
                            byCed.length >= 3 -> data.customers.filter { cedulaDigits(it.cedula).contains(byCed) }.take(5)
                            customer.isNotBlank() -> data.customers.filter { it.matchesQuery(customer) && !it.name.equals(customer.trim(), true) }.take(5)
                            else -> emptyList()
                        }
                    }
                    if (suggestions.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            items(suggestions, key = { it.id }) { c ->
                                SuggestionChip(
                                    onClick = {
                                        customer = c.name; phone = c.phone; picked = true
                                        if (c.cedula.isNotBlank()) cedula = prettyCedula(c.cedula)
                                    },
                                    label = { Text(if (c.cedula.isBlank()) c.name else "${c.name} · ${prettyCedula(c.cedula)}") },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        cedula,
                        { v ->
                            cedula = v.take(16)
                            val d = cedulaDigits(v)
                            val hit = if (d.length >= 6) data.customers.firstOrNull { cedulaDigits(it.cedula) == d } else null
                            if (hit != null) { customer = hit.name; phone = hit.phone; picked = true }
                        },
                        placeholder = { Text("Cédula o RIF (opcional)") },
                        leadingIcon = { Icon(Icons.Filled.Badge, null) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        phone, { phone = it },
                        placeholder = { Text("WhatsApp (opcional)") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (lines.isNotEmpty()) {
                item {
                    FvCard(contentPadding = PaddingValues(16.dp)) {
                        Text("En el presupuesto", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                        lines.forEach { l ->
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${l.qty} × ${l.name}", style = MaterialTheme.typography.bodyLarge, color = FV.Ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(usd(l.priceCents * l.qty), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = FV.Ink)
                                IconButton(onClick = { lines.remove(l) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.Close, "Quitar", Modifier.size(16.dp), tint = FV.Muted)
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Buscar producto o código") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        Row {
                            if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") }
                            ScanButton(scan)
                        }
                    },
                    singleLine = true, shape = RoundedCornerShape(28.dp), colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                AssistChip(onClick = { showFree = true }, label = { Text("Artículo sin catálogo") }, leadingIcon = { Icon(Icons.Filled.Edit, null, Modifier.size(18.dp)) })
            }
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = FV.Surface, border = BorderStroke(1.dp, FV.Hairline)) {
                    Column {
                        if (filtered.isEmpty()) {
                            EmptyState(Icons.Filled.Search, if (data.products.isEmpty()) "Catálogo vacío" else "Sin resultados",
                                if (data.products.isEmpty()) "Agrega productos en Catálogo o usa “Artículo sin catálogo”." else "Prueba con otro nombre o código.")
                        }
                        filtered.take(60).forEachIndexed { i, p ->
                            if (i > 0) HorizontalDivider(color = FV.Hairline, modifier = Modifier.padding(horizontal = 16.dp))
                            val q = lines.find { it.key == p.id }?.qty ?: 0
                            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.bodyLarge, color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(usd(p.priceCents), style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                                }
                                if (q > 0) {
                                    FilledTonalIconButton(onClick = { change(p, -1) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Remove, "Quitar uno", Modifier.size(18.dp)) }
                                    Text("$q", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                                }
                                FilledTonalIconButton(
                                    onClick = { change(p, 1) }, modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = FV.GreenSoft, contentColor = FV.Green),
                                ) { Icon(Icons.Filled.Add, "Agregar ${p.name}", Modifier.size(18.dp)) }
                            }
                        }
                    }
                }
            }
            item {
                FvCard(contentPadding = PaddingValues(16.dp)) {
                    Text("Válido por", style = MaterialTheme.typography.titleSmall, color = FV.Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                        listOf(3, 7, 15, 30).forEach { d ->
                            FilterChip(selected = validDays == d, onClick = { validDays = d }, label = { Text("$d días") })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        note, { note = it },
                        label = { Text("Nota (opcional)") },
                        placeholder = { Text("Ej.: Incluye instalación. Precios sujetos a disponibilidad.") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(16.dp), colors = fieldColors(), modifier = Modifier.fillMaxWidth(), minLines = 2,
                    )
                }
            }
        }
    }

    if (showFree) {
        var name by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        val cents = parseCents(amount)
        val n = qty.toIntOrNull()
        AlertDialog(
            onDismissRequest = { showFree = false },
            containerColor = FV.Surface,
            title = { Text("Artículo sin catálogo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Descripción") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(amount, { amount = it }, label = { Text("Precio $") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f))
                        OutlinedTextField(qty, { qty = it.filter { c -> c.isDigit() }.take(5) }, label = { Text("Cant.") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.width(96.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank() && cents != null && cents > 0 && n != null && n > 0,
                    onClick = { lines.add(QuoteLine("free-" + System.nanoTime(), null, name.trim(), cents ?: 0, n ?: 1)); showFree = false },
                ) { Text("Agregar") }
            },
            dismissButton = { TextButton(onClick = { showFree = false }) { Text("Cancelar") } },
        )
    }
}
