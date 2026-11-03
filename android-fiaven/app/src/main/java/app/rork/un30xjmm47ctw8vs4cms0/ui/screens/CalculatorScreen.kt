package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber
import app.rork.un30xjmm47ctw8vs4cms0.ui.parseDecimal
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import kotlinx.coroutines.launch

/** Formats what the user typed ("1234,5") with thousands dots, keeping a trailing comma while typing. */
private fun prettyInput(raw: String): String {
    if (raw.isEmpty()) return "0"
    val parts = raw.split(',')
    val int = parts[0].ifEmpty { "0" }.toLongOrNull() ?: 0L
    val intText = formatNumber(int.toDouble()).substringBefore(',')
    return if (raw.contains(',')) "$intText,${parts.getOrElse(1) { "" }}" else intText
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(vm: AppViewModel, onBack: () -> Unit) {
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val bcv by vm.bcv.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var input by rememberSaveable { mutableStateOf("") }
    var usdToBs by rememberSaveable { mutableStateOf(true) }
    var useBcv by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { if (bcv.rate <= 0 && !bcv.loading) vm.refreshBcv() }

    val showBcvOption = bcv.rate > 0 && !ledger.rateIsBcv && kotlin.math.abs(bcv.rate - ledger.effectiveRate) > 0.0001
    val rate = if (useBcv && showBcvOption) bcv.rate else ledger.effectiveRate
    val amount = parseDecimal(input.replace(".", ""))?.toDouble() ?: 0.0
    val result = when {
        rate <= 0 -> 0.0
        usdToBs -> amount * rate
        else -> amount / rate
    }
    val fromCode = if (usdToBs) "$" else "Bs"
    val toCode = if (usdToBs) "Bs" else "$"
    val resultText = "$toCode ${formatNumber(result)}"
    val swapTurn by animateFloatAsState(if (usdToBs) 0f else 180f, label = "swap")

    fun press(key: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        input = when (key) {
            "⌫" -> input.dropLast(1)
            "," -> if (input.contains(',')) input else input.ifEmpty { "0" } + ","
            else -> {
                val decimals = input.substringAfter(',', "")
                when {
                    input.contains(',') && decimals.length >= 2 -> input
                    !input.contains(',') && input.length >= 10 -> input
                    input == "0" -> key
                    else -> input + key
                }
            }
        }
    }

    Scaffold(
        containerColor = FV.Canvas,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Calculadora de dólares") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
            // Rate in use
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RateOption(
                    selected = !useBcv || !showBcvOption,
                    label = (if (ledger.rateIsBcv) "Tasa BCV" else "Tasa del negocio") +
                        if (ledger.effectiveRate > 0) " · Bs ${formatNumber(ledger.effectiveRate)}" else "",
                ) { useBcv = false }
                if (showBcvOption) {
                    RateOption(selected = useBcv, label = "BCV · Bs ${formatNumber(bcv.rate)}") { useBcv = true }
                }
            }
            Spacer(Modifier.height(12.dp))

            FvCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                Box {
                    Column {
                        AmountRow(
                            label = if (usdToBs) "Dólares" else "Bolívares",
                            code = fromCode,
                            value = prettyInput(input),
                            color = FV.Ink,
                            onClear = if (input.isNotEmpty()) ({ input = "" }) else null,
                        )
                        HorizontalDivider(color = FV.Hairline)
                        Box(Modifier.fillMaxWidth().background(FV.GreenTint)) {
                            AmountRow(
                                label = if (usdToBs) "Equivale en bolívares" else "Equivale en dólares",
                                code = toCode,
                                value = formatNumber(result),
                                color = FV.Green,
                                trailingIcon = Icons.Filled.ContentCopy,
                                onTrailing = {
                                    clipboard.setText(AnnotatedString(resultText))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch { snackbar.showSnackbar("Copiado: $resultText") }
                                },
                            )
                        }
                    }
                    // Swap button sits on the divider
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Keep the number on screen meaningful: the old result becomes the new input.
                            if (amount > 0 && rate > 0) {
                                input = formatNumber(result).replace(".", "").removeSuffix(",00")
                            }
                            usdToBs = !usdToBs
                        },
                        shape = CircleShape,
                        color = FV.Green,
                        contentColor = FV.Surface,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp).size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.SwapVert, "Cambiar dirección", modifier = Modifier.rotate(swapTurn))
                        }
                    }
                }
            }
            if (rate <= 0) {
                Text(
                    "Aún no hay tasa. Ve a la pestaña Tasa para obtener la del BCV.",
                    style = MaterialTheme.typography.bodySmall, color = FV.AmberText,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val quick = if (usdToBs) listOf("1", "5", "10", "20", "50", "100") else listOf("100", "500", "1000", "5000", "10000")
                quick.forEach { q ->
                    FilterChip(
                        selected = input == q,
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); input = q },
                        label = { Text("$fromCode ${prettyInput(q)}") },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = FV.Surface,
                            selectedContainerColor = FV.GreenSoft,
                            selectedLabelColor = FV.GreenDark,
                        ),
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Keypad(onKey = ::press, onClearAll = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); input = "" })
        }
    }
}

@Composable
private fun RateOption(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = FV.Surface,
            selectedContainerColor = FV.GreenSoft,
            selectedLabelColor = FV.GreenDark,
        ),
    )
}

@Composable
private fun AmountRow(
    label: String,
    code: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    onClear: (() -> Unit)? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailing: (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 84.dp, top = 16.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = FV.Muted, modifier = Modifier.weight(1f))
            if (onClear != null) {
                Text(
                    "Borrar", style = MaterialTheme.typography.labelLarge, color = FV.GreenDark,
                    modifier = Modifier.clickable(onClick = onClear).padding(4.dp),
                )
            }
            if (trailingIcon != null && onTrailing != null) {
                Icon(trailingIcon, "Copiar", tint = FV.Green, modifier = Modifier.clickable(onClick = onTrailing).padding(4.dp).size(18.dp))
            }
        }
        Spacer(Modifier.height(2.dp))
        AnimatedContent(
            targetState = code,
            transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
            label = "amount",
        ) { c ->
            Row(verticalAlignment = Alignment.Bottom) {
                Text(c, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = color.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 6.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    value,
                    fontSize = if (value.length > 12) 30.sp else 40.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp,
                    color = color, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Keypad(onKey: (String) -> Unit, onClearAll: () -> Unit) {
    val rows = listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3"), listOf(",", "0", "⌫"))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { k -> Key(k, onClick = { onKey(k) }, onLongClick = if (k == "⌫") onClearAll else null) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.Key(key: String, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "key")
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (key == "⌫") FV.GreenSoft else FV.Surface,
        border = BorderStroke(1.dp, FV.Hairline),
        modifier = Modifier.weight(1f).height(60.dp).scale(scale)
            .combinedClickable(interactionSource = interaction, indication = null, onClick = onClick, onLongClick = onLongClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "⌫") Icon(Icons.AutoMirrored.Filled.Backspace, "Borrar", tint = FV.GreenDark)
            else Text(key, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = FV.Ink)
        }
    }
}
