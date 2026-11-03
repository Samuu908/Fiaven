package app.rork.un30xjmm47ctw8vs4cms0.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import app.rork.un30xjmm47ctw8vs4cms0.data.Quote
import app.rork.un30xjmm47ctw8vs4cms0.data.Sale
import app.rork.un30xjmm47ctw8vs4cms0.data.SaleItem
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

private const val W = 1080f
private const val M = 72f

private val GREEN = 0xFF2E7D5B.toInt()
private val GREEN_DARK = 0xFF1F5E43.toInt()
private val GREEN_TINT = 0xFFEFF6F2.toInt()
private val INK = 0xFF1B2A22.toInt()
private val MUTED = 0xFF5F6F66.toInt()
private val HAIRLINE = 0xFFE2E8E4.toInt()
private val CANVAS = 0xFFF6F8F6.toInt()

private fun paint(size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        textAlign = align
    }

private fun ellipsize(text: String, p: Paint, maxWidth: Float): String {
    if (p.measureText(text) <= maxWidth) return text
    val n = p.breakText(text, true, maxWidth - p.measureText("…"), null)
    return text.take(n).trimEnd() + "…"
}

private fun wrap(text: String, p: Paint, maxWidth: Float): List<String> {
    val out = ArrayList<String>()
    for (para in text.split('\n')) {
        var line = ""
        for (word in para.split(' ')) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (p.measureText(candidate) <= maxWidth) line = candidate
            else { if (line.isNotEmpty()) out += line; line = word }
        }
        out += line
    }
    return out
}

fun quoteNumber(n: Int): String = "N° " + n.toString().padStart(4, '0')

/** Data drawn on a quote or sale invoice image. */
private class DocData(
    val kind: String,
    val number: String,
    val customerName: String,
    val customerCedula: String,
    val customerPhone: String,
    val createdAt: Long,
    val rightInfo: String,
    val items: List<SaleItem>,
    val totalCents: Long,
    val rate: Double,
    val note: String,
    val footer: String,
)

/** Draws the quote as an invoice-like image ready to send by WhatsApp. */
fun renderQuote(quote: Quote, businessName: String): Bitmap = renderDoc(
    DocData(
        kind = "Presupuesto",
        number = quoteNumber(quote.number),
        customerName = quote.customerName,
        customerCedula = quote.customerCedula,
        customerPhone = quote.customerPhone,
        createdAt = quote.createdAt,
        rightInfo = "Válido hasta ${shortDate(quote.createdAt + quote.validDays * DAY_MS)}",
        items = quote.items,
        totalCents = quote.totalCents,
        rate = quote.rate,
        note = quote.note,
        footer = "Precios en dólares; el monto en bolívares cambia según la tasa del día.",
    ),
    businessName,
)

/** Short, stable invoice number for a sale (from its id, so every phone shows the same). */
fun saleNumber(sale: Sale): String = "N° " + sale.id.filter { it.isLetterOrDigit() }.takeLast(6).uppercase()

/** Sale receipt styled like an electronic invoice: customer, cédula, items, totals in $ and Bs. */
fun renderSaleInvoice(sale: Sale, businessName: String, attendedBy: String? = null): Bitmap = renderDoc(
    DocData(
        kind = if (sale.isFiado) "Nota de venta · a crédito" else "Nota de venta · contado",
        number = saleNumber(sale),
        customerName = sale.customerName.orEmpty(),
        customerCedula = sale.customerCedula.orEmpty(),
        customerPhone = sale.customerPhone.orEmpty(),
        createdAt = sale.createdAt,
        rightInfo = "Hora ${hourLabel(sale.createdAt)} · Atendió ${(attendedBy ?: sale.attendedBy)?.ifBlank { null } ?: sale.authorName}",
        items = sale.items,
        totalCents = sale.totalCents,
        rate = sale.rate,
        note = salePaymentLine(sale),
        footer = "Gracias por su compra. Monto en bolívares a la tasa del día de la venta.",
    ),
    businessName,
)

/** "Pago: Efectivo $ + Pago móvil · Recibido $ 10,00 + Bs 500,00". */
fun salePaymentLine(sale: Sale): String {
    if (sale.isFiado) return ""
    val parts = ArrayList<String>()
    sale.payMethod?.ifBlank { null }?.let { parts += "Pago: $it" }
    if (sale.paidUsdCents != null || sale.paidBsCents != null) {
        parts += "Recibido " + listOfNotNull(
            sale.paidUsdCents?.takeIf { it > 0 }?.let { usd(it) },
            sale.paidBsCents?.takeIf { it > 0 }?.let { "Bs " + formatNumber(it / 100.0) },
        ).joinToString(" + ")
    }
    return parts.joinToString(" · ")
}

private fun renderDoc(quote: DocData, businessName: String): Bitmap {
    val title = paint(54f, 0xFFFFFFFF.toInt(), bold = true)
    val subtitle = paint(30f, 0xFFD5EADF.toInt())
    val badge = paint(30f, 0xFFFFFFFF.toInt(), bold = true, align = Paint.Align.RIGHT)
    val label = paint(28f, MUTED)
    val value = paint(32f, INK, bold = true)
    val head = paint(26f, GREEN_DARK, bold = true)
    val cell = paint(32f, INK)
    val cellRight = paint(32f, INK, align = Paint.Align.RIGHT)
    val cellBoldRight = paint(32f, INK, bold = true, align = Paint.Align.RIGHT)
    val totalLabel = paint(34f, INK, bold = true)
    val totalValue = paint(60f, GREEN, bold = true, align = Paint.Align.RIGHT)
    val bsValue = paint(30f, MUTED, align = Paint.Align.RIGHT)
    val note = paint(30f, INK)
    val foot = paint(26f, MUTED, align = Paint.Align.CENTER)

    val contentW = W - 2 * M
    val noteLines = if (quote.note.isNotBlank()) wrap(quote.note, note, contentW - 48f) else emptyList()
    val rowH = 76f
    val height = 260f + 40f + 200f + 70f + rowH * quote.items.size + 40f + 190f +
        (if (noteLines.isNotEmpty()) 90f + noteLines.size * 44f else 0f) + 170f

    val bmp = Bitmap.createBitmap(W.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(0xFFFFFFFF.toInt())

    // Header band
    c.drawRect(0f, 0f, W, 260f, Paint().apply { color = GREEN })
    c.drawText(ellipsize(businessName.ifBlank { "Mi negocio" }, title, contentW - 300f), M, 120f, title)
    c.drawText(quote.kind, M, 172f, subtitle)
    val pillW = maxOf(250f, badge.measureText(quote.number) + 60f)
    val pill = RectF(W - M - pillW, 82f, W - M, 142f)
    c.drawRoundRect(pill, 30f, 30f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GREEN_DARK })
    c.drawText(quote.number, W - M - 30f, 123f, badge)

    // Info block
    var y = 260f + 70f
    val col2 = M + contentW / 2
    c.drawText("Cliente", M, y, label)
    c.drawText("Fecha", col2, y, label)
    y += 44f
    c.drawText(ellipsize(quote.customerName.ifBlank { "Cliente" }, value, contentW / 2 - 20f), M, y, value)
    c.drawText(shortDate(quote.createdAt), col2, y, value)
    y += 50f
    val idLine = listOfNotNull(
        prettyCedula(quote.customerCedula).ifBlank { null }?.let { "C.I./RIF: $it" },
        quote.customerPhone.ifBlank { null },
    ).joinToString("  ·  ")
    c.drawText(ellipsize(idLine.ifBlank { " " }, label, contentW / 2 - 20f), M, y, label)
    c.drawText(ellipsize(quote.rightInfo, label, contentW / 2), col2, y, label)
    y += 56f

    // Table header
    val qtyX = M + 24f
    val descX = M + 130f
    val priceRight = M + contentW - 230f
    val totalRight = M + contentW - 24f
    c.drawRoundRect(RectF(M, y, W - M, y + 70f), 18f, 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GREEN_TINT })
    c.drawText("CANT.", qtyX, y + 45f, head)
    c.drawText("DESCRIPCIÓN", descX, y + 45f, head)
    c.drawText("PRECIO", priceRight, y + 45f, paint(26f, GREEN_DARK, bold = true, align = Paint.Align.RIGHT))
    c.drawText("TOTAL", totalRight, y + 45f, paint(26f, GREEN_DARK, bold = true, align = Paint.Align.RIGHT))
    y += 70f

    val line = Paint().apply { color = HAIRLINE; strokeWidth = 2f }
    for (item in quote.items) {
        val base = y + rowH / 2 + 11f
        c.drawText("${item.qty}", qtyX, base, cell)
        c.drawText(ellipsize(item.name, cell, priceRight - descX - 190f), descX, base, cell)
        c.drawText(usd(item.priceCents), priceRight, base, cellRight)
        c.drawText(usd(item.priceCents * item.qty), totalRight, base, cellBoldRight)
        y += rowH
        c.drawLine(M + 16f, y, W - M - 16f, y, line)
    }

    // Total
    y += 40f
    c.drawRoundRect(RectF(M, y, W - M, y + 160f), 28f, 28f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CANVAS })
    c.drawText("Total", M + 32f, y + 72f, totalLabel)
    c.drawText(usd(quote.totalCents), W - M - 32f, y + 82f, totalValue)
    if (quote.rate > 0) {
        c.drawText("${bs(quote.totalCents, quote.rate)} · tasa Bs ${formatNumber(quote.rate)}", W - M - 32f, y + 128f, bsValue)
    }
    y += 190f

    if (noteLines.isNotEmpty()) {
        c.drawText("Nota", M, y + 30f, label)
        y += 60f
        for (l in noteLines) { c.drawText(l, M, y + 30f, note); y += 44f }
        y += 30f
    }

    // Footer
    y += 40f
    c.drawLine(M, y, W - M, y, line)
    c.drawText(ellipsize(quote.footer, foot, contentW), W / 2, y + 52f, foot)
    c.drawText("Hecho con Kashly", W / 2, y + 96f, paint(26f, GREEN, bold = true, align = Paint.Align.CENTER))
    return bmp
}

/** Venezuelan local numbers (0414...) to WhatsApp international format (58414...). */
fun whatsappNumber(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    return if (digits.startsWith("0")) "58" + digits.drop(1) else digits
}

/**
 * Shares [bitmap] as PNG. Goes straight to WhatsApp (and to the customer's chat when a phone is known);
 * falls back to the system share sheet if WhatsApp is not installed.
 */
fun shareImage(context: Context, bitmap: Bitmap, fileName: String, text: String, phone: String = "", whatsapp: Boolean = true) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, fileName)
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
    fun base() = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (whatsapp) {
        val number = whatsappNumber(phone)
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            val intent = base().setPackage(pkg)
            if (number.length >= 10) intent.putExtra("jid", "$number@s.whatsapp.net")
            if (runCatching { context.startActivity(intent) }.isSuccess) return
        }
    }
    context.startActivity(Intent.createChooser(base(), "Enviar"))
}

/**
 * Saves [bitmap] to the phone's gallery (Pictures/Kashly). Returns true on success.
 * Android 9 and older would need a storage permission, so there we return false and the caller shares instead.
 */
fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    return runCatching {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kashly")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: return false
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        true
    }.getOrDefault(false)
}
