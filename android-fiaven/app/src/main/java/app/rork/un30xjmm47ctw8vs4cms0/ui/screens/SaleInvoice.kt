package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale
import app.rork.un30xjmm47ctw8vs4cms0.ui.renderSaleInvoice
import app.rork.un30xjmm47ctw8vs4cms0.ui.saleNumber
import app.rork.un30xjmm47ctw8vs4cms0.ui.saveImageToGallery
import app.rork.un30xjmm47ctw8vs4cms0.ui.shareImage
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.usd

private fun invoiceFile(sale: Sale) = "factura-" + saleNumber(sale).filter { it.isLetterOrDigit() } + ".png"

/**
 * Invoice-like picture of a sale: preview, save to the gallery, send by WhatsApp or share.
 * [justSaved] shows the green "Venta guardada" header right after checkout.
 */
@Composable
fun SaleInvoiceDialog(sale: Sale, businessName: String, justSaved: Boolean = false, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(sale, businessName) { renderSaleInvoice(sale, businessName) }
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    var saved by remember { mutableStateOf(false) }
    val text = "Hola ${sale.customerName.orEmpty()}, aquí está tu factura ${saleNumber(sale)} por ${usd(sale.totalCents)}.".replace("  ", " ")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), color = FV.Canvas, modifier = Modifier.fillMaxWidth(0.92f)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (justSaved) {
                        Icon(Icons.Filled.CheckCircle, null, tint = FV.Green, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (justSaved) (if (sale.isFiado) "Fiado registrado" else "Venta guardada") else "Factura ${saleNumber(sale)}",
                            style = MaterialTheme.typography.titleLarge, color = FV.Ink,
                        )
                        if (justSaved) Text("Factura ${saleNumber(sale)} lista", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "Cerrar") }
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, FV.Hairline),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                ) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Image(image, "Vista de la factura", contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (saveImageToGallery(context, bitmap, invoiceFile(sale))) {
                            saved = true
                            Toast.makeText(context, "Guardada en Galería › Kashly", Toast.LENGTH_SHORT).show()
                        } else {
                            // Older Android: let the user pick where to save it.
                            runCatching { shareImage(context, bitmap, invoiceFile(sale), text, whatsapp = false) }
                        }
                    },
                    shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Icon(if (saved) Icons.Filled.CheckCircle else Icons.Filled.Download, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (saved) "Guardada en la galería" else "Descargar foto")
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { runCatching { shareImage(context, bitmap, invoiceFile(sale), text, sale.customerPhone.orEmpty()) } },
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("WhatsApp")
                    }
                    OutlinedButton(
                        onClick = { runCatching { shareImage(context, bitmap, invoiceFile(sale), text, whatsapp = false) } },
                        shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Compartir")
                    }
                }
            }
        }
    }
}

/** Type the quantity with the number keyboard instead of tapping + many times. 0 removes the item. */
@Composable
fun QuantityDialog(name: String, current: Int, stockLeft: Int?, onDismiss: () -> Unit, onSet: (Int) -> Unit) {
    val start = if (current > 0) current.toString() else ""
    var value by remember { mutableStateOf(TextFieldValue(start, TextRange(0, start.length))) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val qty = value.text.toIntOrNull()
    fun done() { if (qty != null) onSet(qty) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FV.Surface,
        title = { Text(name, maxLines = 2) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { v -> if (v.text.length <= 6 && v.text.all { it.isDigit() }) value = v },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { done() }),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 25, 50, 100).forEach { n ->
                        SuggestionChip(onClick = { value = TextFieldValue(n.toString(), TextRange(n.toString().length)) }, label = { Text("$n") })
                    }
                }
                if (stockLeft != null) {
                    Text(
                        if (stockLeft <= 0) "Agotado según el inventario" else "Quedan $stockLeft en inventario",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (qty != null && qty > stockLeft) FV.Danger else FV.Muted,
                    )
                }
            }
        },
        confirmButton = { TextButton(enabled = qty != null, onClick = { done() }) { Text(if (qty == 0) "Quitar" else "Listo") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = FV.Muted) } },
    )
}
