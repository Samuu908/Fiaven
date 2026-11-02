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
    var showSale by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

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
                            Column {
                                Text("FiaVen", style = MaterialTheme.typography.headlineSmall, color = FV.GreenDark)
                                Text(
                                    session?.businessName?.ifBlank { null } ?: "Tu negocio, siempre contigo",
                                    style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { vm.syncNow() }) { Icon(Icons.Filled.Sync, "Sincronizar ahora") }
                            IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "Más opciones") }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
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
                        showSale = true
                    },
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("Nueva venta") },
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
                )
            }
            composable("fiados") { FiadosScreen(vm, onOpen = { nav.navigate("cliente/$it") }) }
            composable("catalogo") { CatalogScreen(vm) }
            composable("tasa") { RateScreen(vm, onSaved = { scope.launch { snackbar.showSnackbar("Tasa guardada para todo el equipo") } }) }
            composable("cliente/{id}") { backStack ->
                CustomerDetailScreen(
                    vm,
                    customerId = backStack.arguments?.getString("id").orEmpty(),
                    onBack = { nav.popBackStack() },
                )
            }
            composable("equipo") { TeamScreen(vm, onBack = { nav.popBackStack() }) }
            composable("resumen") { SummaryScreen(vm, onBack = { nav.popBackStack() }) }
            composable("cuenta") {
                AccountScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onOpenAbout = { nav.navigate("acerca") },
                    onSecuritySaved = { scope.launch { snackbar.showSnackbar("Pregunta de seguridad guardada") } },
                )
            }
            composable("acerca") {
                AboutScreen(vm, onBack = { nav.popBackStack() }, onSaved = { scope.launch { snackbar.showSnackbar("Créditos actualizados") } })
            }
        }
    }

    if (showSale) {
        NewSaleSheet(
            vm,
            onDismiss = { showSale = false },
            onSaved = { fiado ->
                showSale = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { snackbar.showSnackbar(if (fiado) "Fiado registrado" else "Venta guardada") }
            },
        )
    }
}
