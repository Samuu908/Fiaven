package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.formatNumber
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** Evaluates "12,5+3×2−10%" left-to-right with × ÷ precedence. Comma is the decimal separator. */
private fun evaluate(expr: String): BigDecimal? {
    if (expr.isBlank()) return null
    val tokens = ArrayList<Any>()
    var num = StringBuilder()
    fun flush(): Boolean {
        if (num.isEmpty()) return false
        val v = num.toString().replace(',', '.').toBigDecimalOrNull() ?: return false
        num = StringBuilder()
        tokens += v
        return true
    }
    for (c in expr) {
        when {
            c.isDigit() || c == ',' -> num.append(c)
            c == '-' && num.isEmpty() -> num.append(c)
            c == '%' -> {
                if (!flush()) return null
                val last = tokens.removeAt(tokens.size - 1) as BigDecimal
                // "100 + 10%" → 100 + 10; "50 × 10%" → 50 × 0.1
                val op = tokens.getOrNull(tokens.size - 1) as? Char
                val base = tokens.getOrNull(tokens.size - 2) as? BigDecimal
                tokens += if ((op == '+' || op == '−') && base != null) base.multiply(last).divide(BigDecimal(100))
                else last.divide(BigDecimal(100))
            }
            c in "+−×÷" -> {
                flush()
                if (tokens.isEmpty() || tokens.last() is Char) return null
                tokens += c
            }
        }
    }
    flush()
    if (tokens.isEmpty()) return null
    if (tokens.last() is Char) tokens.removeAt(tokens.size - 1)
    // × and ÷ first
    val pass = ArrayList<Any>()
    var i = 0
    while (i < tokens.size) {
        val t = tokens[i]
        if (t is Char && (t == '×' || t == '÷')) {
            val a = pass.removeAt(pass.size - 1) as BigDecimal
            val b = tokens[i + 1] as BigDecimal
            pass += if (t == '×') a.multiply(b) else {
                if (b.signum() == 0) return null
                a.divide(b, MathContext(16, RoundingMode.HALF_UP))
            }
            i += 2
        } else { pass += t; i++ }
    }
    var acc = pass[0] as BigDecimal
    var j = 1
    while (j < pass.size) {
        val op = pass[j] as Char
        val b = pass[j + 1] as BigDecimal
        acc = if (op == '+') acc.add(b) else acc.subtract(b)
        j += 2
    }
    return acc
}

private fun pretty(v: BigDecimal): String {
    val d = v.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
    val neg = d.signum() < 0
    val abs = d.abs()
    val intPart = abs.toBigInteger().toString().reversed().chunked(3).joinToString(".").reversed()
    val frac = abs.toPlainString().substringAfter('.', "")
    return (if (neg) "-" else "") + intPart + if (frac.isNotEmpty() && abs.scale() > 0) ",$frac" else ""
}

/** Regular calculator, with a quick "in Bs / in $" line using today's rate. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicCalculatorScreen(vm: AppViewModel, onBack: () -> Unit, onOpenDollar: () -> Unit) {
    val ledger by vm.ledger.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var expr by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf("") }
    val result = remember(expr) { evaluate(expr) }
    val rate = ledger.effectiveRate

    fun press(k: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        when (k) {
            "C" -> { expr = ""; history = "" }
            "⌫" -> expr = expr.dropLast(1)
            "=" -> result?.let { history = "$expr ="; expr = pretty(it).replace(".", "") }
            "+", "−", "×", "÷" -> {
                expr = when {
                    expr.isEmpty() -> ""
                    expr.last() in "+−×÷" -> expr.dropLast(1) + k
                    else -> expr + k
                }
            }
            "," -> {
                val lastNum = expr.takeLastWhile { it.isDigit() || it == ',' }
                if (!lastNum.contains(',')) expr += if (lastNum.isEmpty()) "0," else ","
            }
            "%" -> if (expr.isNotEmpty() && expr.last().isDigit()) expr += "%"
            else -> if (expr.length < 40) expr += k
        }
    }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Calculadora") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                actions = {
                    IconButton(onClick = onOpenDollar) { Icon(Icons.Filled.CurrencyExchange, "Calculadora de dólares", tint = FV.GreenDark) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).navigationBarsPadding().padding(16.dp)) {
            Column(
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
            ) {
                Text(history, style = MaterialTheme.typography.titleMedium, color = FV.Muted, maxLines = 1)
                Text(
                    expr.ifEmpty { "0" },
                    fontSize = if (expr.length > 14) 30.sp else 46.sp, fontWeight = FontWeight.SemiBold,
                    color = FV.Ink, textAlign = TextAlign.End, maxLines = 2, lineHeight = 50.sp,
                )
                val showResult = result != null && expr.any { it in "+−×÷%" }
                Text(
                    if (showResult) "= " + pretty(result!!) else " ",
                    style = MaterialTheme.typography.headlineSmall, color = FV.Green,
                )
                if (result != null && rate > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Si son $: Bs ${formatNumber(result.toDouble() * rate)}   ·   Si son Bs: $ ${formatNumber(result.toDouble() / rate)}",
                        style = MaterialTheme.typography.bodySmall, color = FV.Muted, textAlign = TextAlign.End,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            val rows = listOf(
                listOf("C", "⌫", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("00", "0", ",", "="),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { k ->
                            val kind = when (k) {
                                "=" -> 2
                                "+", "−", "×", "÷", "%" -> 1
                                "C", "⌫" -> 3
                                else -> 0
                            }
                            CalcKey(k, kind, Modifier.weight(1f)) { press(k) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(label: String, kind: Int, modifier: Modifier, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.93f else 1f, label = "key")
    val (bg, fg) = when (kind) {
        2 -> FV.Green to FV.Surface
        1 -> FV.GreenSoft to FV.GreenDark
        3 -> FV.Neutral to FV.Danger
        else -> FV.Surface to FV.Ink
    }
    Surface(
        onClick = onClick, interactionSource = source,
        shape = RoundedCornerShape(22.dp), color = bg,
        border = if (kind == 0) androidx.compose.foundation.BorderStroke(1.dp, FV.Hairline) else null,
        modifier = modifier.height(64.dp).scale(s),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "⌫") Icon(Icons.AutoMirrored.Filled.Backspace, "Borrar", tint = fg)
            else Text(label, fontSize = 26.sp, fontWeight = if (kind == 0) FontWeight.Medium else FontWeight.SemiBold, color = fg)
        }
    }
}
