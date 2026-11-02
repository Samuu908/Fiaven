package app.rork.un30xjmm47ctw8vs4cms0.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.RateChip
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.CajaScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.CalculatorScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.MovementsScreen
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ReceiptLong
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.AccountScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.AboutScreen
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.CatalogScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.CustomerDetailScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.FiadosScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.LoginScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.NewSaleSheet
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.RateScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.SummaryScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.TeamScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.BusinessAvatar
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.ThemeMode
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.ThemePrefs
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import app.rork.un30xjmm47ctw8vs4cms0.ui.hourLabel
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.ClosingScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.NoteEditorScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.NotesScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.QuoteBuilderScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.QuotesScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.SupplierDetailScreen
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.SuppliersScreen
import androidx.compose.material.icons.filled.LocalShipping

private data class Tab(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val tabs = listOf(
    Tab("caja", "Caja", Icons.Outlined.Home, Icons.Filled.Home),
    Tab("fiados", "Fiados", Icons.Outlined.People, Icons.Filled.People),
    Tab("catalogo", "Catálogo", Icons.Outlined.Inventory2, Icons.Filled.Inventory2),
    Tab("tasa", "Tasa", Icons.Outlined.BarChart, Icons.Filled.BarChart),
)

@Composable
fun AppNavigation() {
    val vm: AppViewModel = viewModel()
    val session by vm.session.collectAsStateWithLifecycle()
    if (session == null) LoginScreen(vm) else MainShell(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(vm: AppViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isTab = tabs.any { it.route == route }
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val data by vm.data.collectAsStateWithLifecycle()
    var showSale by rememberSaveable { mutableStateOf(false) }
    var lastSale by remember { mutableStateOf<app.rork.un30xjmm47ctw8vs4cms0.data.Sale?>(null) }
    var menu by remember { mutableStateOf(false) }
    var closedNotice by remember { mutableStateOf(false) }

    fun goTab(r: String) {
        nav.navigate(r) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = FV.Canvas,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (isTab) {
                Column(Modifier.padding(bottom = 8.dp)) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BusinessAvatar(
                                    data.photo, session?.businessName.orEmpty(), size = 42,
                                    modifier = Modifier.clickable { nav.navigate("cuenta") },
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Kashly", style = MaterialTheme.typography.headlineSmall, color = FV.GreenDark)
                                    Text(
                                        session?.businessName?.ifBlank { null } ?: "Tu negocio, siempre contigo",
                                        style = MaterialTheme.typography.bodyMedium, color = FV.Muted, maxLines = 1,
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { nav.navigate("calculadora") }) { Icon(Icons.Filled.Calculate, "Calculadora") }
                            IconButton(onClick = { vm.syncNow() }) { Icon(Icons.Filled.Sync, "Sincronizar ahora") }
                            IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "Más opciones") }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Movimientos") },
                                    leadingIcon = { Icon(Icons.Filled.ReceiptLong, null) },
                                    onClick = { menu = false; nav.navigate("movimientos") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Cierre de caja") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                                    onClick = { menu = false; nav.navigate("cierre") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Proveedores") },
                                    leadingIcon = { Icon(Icons.Filled.LocalShipping, null) },
                                    onClick = { menu = false; nav.navigate("proveedores") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Presupuestos") },
                                    leadingIcon = { Icon(Icons.Filled.Description, null) },
                                    onClick = { menu = false; nav.navigate("presupuestos") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Notas") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.StickyNote2, null) },
                                    onClick = { menu = false; nav.navigate("notas") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Calculadora") },
                                    leadingIcon = { Icon(Icons.Filled.Calculate, null) },
                                    onClick = { menu = false; nav.navigate("calculadora") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Resumen semanal") },
                                    leadingIcon = { Icon(Icons.Filled.Insights, null) },
                                    onClick = { menu = false; nav.navigate("resumen") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Equipo y accesos") },
                                    leadingIcon = { Icon(Icons.Filled.Groups, null) },
                                    onClick = { menu = false; nav.navigate("equipo") },
                                )
                                DropdownMenuItem(
                                    text = { Text(if (FV.isDark) "Modo claro" else "Modo oscuro") },
                                    leadingIcon = { Icon(if (FV.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode, null) },
                                    onClick = {
                                        menu = false
                                        ThemePrefs.set(context, if (FV.isDark) ThemeMode.LIGHT else ThemeMode.DARK)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Mi cuenta") },
                                    leadingIcon = { Icon(Icons.Filled.AccountCircle, null) },
                                    onClick = { menu = false; nav.navigate("cuenta") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Acerca de") },
                                    leadingIcon = { Icon(Icons.Filled.Info, null) },
                                    onClick = { menu = false; nav.navigate("acerca") },
                                )
                                DropdownMenuItem(
                                    text = { Text("Cerrar sesión") },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                                    onClick = { menu = false; vm.logout() },
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
                    )
                    RateChip(
                        rate = ledger.effectiveRate,
                        isBcv = ledger.rateIsBcv,
                        onClick = { goTab("tasa") },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        },
        bottomBar = {
            if (isTab) {
                NavigationBar(containerColor = FV.Surface, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        val selected = route == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { goTab(tab.route) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FV.GreenDark,
                                selectedTextColor = FV.GreenDark,
                                indicatorColor = FV.GreenSoft,
                                unselectedIconColor = FV.Muted,
                                unselectedTextColor = FV.Muted,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(route == "caja", enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (ledger.todayClosing != null) closedNotice = true else showSale = true
                    },
                    icon = { Icon(if (vm.saleDraft.cart.isEmpty()) Icons.Filled.Add else Icons.Filled.ShoppingCart, null) },
                    text = {
                        val n = vm.saleDraft.units
                        Text(if (n == 0) "Nueva venta" else "Continuar venta · $n")
                    },
                    containerColor = FV.Green,
                    contentColor = FV.Surface,
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "caja",
            // Detail screens own their Scaffold (and insets); only tabs sit inside the shell padding.
            modifier = if (isTab || route == null) Modifier.fillMaxSize().padding(padding) else Modifier.fillMaxSize(),
        ) {
            composable("caja") {
                CajaScreen(
                    vm,
                    onOpenFiados = { goTab("fiados") },
                    onOpenCustomer = { nav.navigate("cliente/$it") },
                    onOpenSummary = { nav.navigate("resumen") },
                    onOpenAccount = { nav.navigate("cuenta") },
                    onOpenMovements = { nav.navigate("movimientos") },
                    onOpenClosing = { nav.navigate("cierre") },
                )
            }
            composable("fiados") { FiadosScreen(vm, onOpen = { nav.navigate("cliente/$it") }) }
            composable("catalogo") { CatalogScreen(vm) }
            composable("tasa") { RateScreen(vm, onOpenCalculator = { nav.navigate("calculadora") }, onSaved = { scope.launch { snackbar.showSnackbar("Tasa guardada para todo el equipo") } }) }
            composable("cliente/{id}") { backStack ->
                CustomerDetailScreen(
                    vm,
                    customerId = backStack.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                )
            }
            composable("equipo") { TeamScreen(vm, onBack = { nav.popBackStack() }) }
            composable("resumen") { SummaryScreen(vm, onBack = { nav.popBackStack() }) }
            composable("calculadora") { CalculatorScreen(vm, onBack = { nav.popBackStack() }) }
            composable("movimientos") {
                MovementsScreen(vm, onBack = { nav.popBackStack() }, onOpenCustomer = { nav.navigate("cliente/$it") })
            }
            composable("cuenta") {
                AccountScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onOpenAbout = { nav.navigate("acerca") },
                    onSecuritySaved = { scope.launch { snackbar.showSnackbar("Pregunta de seguridad guardada") } },
                )
            }
            composable("cierre") {
                ClosingScreen(
                    vm, onBack = { nav.popBackStack() },
                    onClosed = { scope.launch { snackbar.showSnackbar("Caja de hoy cerrada") } },
                )
            }
            composable("notas") { NotesScreen(vm, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("nota/$it") }) }
            composable("nota/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")
                NoteEditorScreen(vm, noteId = id?.takeIf { it != "nueva" }, onBack = { nav.popBackStack() })
            }
            composable("presupuestos") {
                QuotesScreen(vm, onBack = { nav.popBackStack() }, onNew = { nav.navigate("presupuesto/nuevo") })
            }
            composable("proveedores") {
                SuppliersScreen(vm, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("proveedor/$it") })
            }
            composable("proveedor/{id}") { backStack ->
                SupplierDetailScreen(vm, supplierId = backStack.arguments?.getString("id").orEmpty(), onBack = { nav.popBackStack() })
            }
            composable("presupuesto/nuevo") { QuoteBuilderScreen(vm, onBack = { nav.popBackStack() }) }
            composable("acerca") {
                AboutScreen(vm, onBack = { nav.popBackStack() }, onSaved = { scope.launch { snackbar.showSnackbar("Créditos actualizados") } })
            }
        }
    }

    val closing = ledger.todayClosing
    if (closedNotice && closing != null) {
        val owner = session?.isOwner == true
        AlertDialog(
            onDismissRequest = { closedNotice = false },
            containerColor = FV.Surface,
            icon = { Icon(Icons.Filled.Lock, null, tint = FV.GreenDark) },
            title = { Text("La caja de hoy está cerrada") },
            text = {
                Text(
                    "${closing.authorName} cerró el día a las ${hourLabel(closing.createdAt)}. " +
                        if (owner) "Reabre la caja para anotar más ventas de hoy." else "Pídele al dueño que la reabra para anotar más ventas.",
                    color = FV.Muted,
                )
            },
            confirmButton = {
                if (owner) {
                    TextButton(onClick = { vm.reopenDay(closing); closedNotice = false; showSale = true }) {
                        Text("Reabrir y vender", color = FV.GreenDark)
                    }
                } else {
                    TextButton(onClick = { closedNotice = false }) { Text("Entendido", color = FV.GreenDark) }
                }
            },
            dismissButton = {
                TextButton(onClick = { closedNotice = false; nav.navigate("cierre") }) { Text("Ver cierre", color = FV.Muted) }
            },
        )
    }

    if (showSale) {
        NewSaleSheet(
            vm,
            onDismiss = { showSale = false },
            onSaved = { fiado, sale ->
                showSale = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (sale != null) lastSale = sale
                else scope.launch { snackbar.showSnackbar(if (fiado) "Fiado registrado" else "Venta guardada") }
            },
        )
    }

    lastSale?.let { sale ->
        app.rork.un30xjmm47ctw8vs4cms0.ui.screens.SaleInvoiceDialog(
            sale, vm.session.value?.businessName.orEmpty(), justSaved = true, onDismiss = { lastSale = null },
        )
    }
}
