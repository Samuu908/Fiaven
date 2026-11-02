package app.rork.un30xjmm47ctw8vs4cms0.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Opens Google's code scanner (camera handled by Google Play services, no camera permission).
 * Returns a function that starts a scan; [onResult] receives the raw code.
 */
@Composable
fun rememberBarcodeScanner(onResult: (String) -> Unit, onError: (String) -> Unit = {}): () -> Unit {
    val context = LocalContext.current
    val result by rememberUpdatedState(onResult)
    val error by rememberUpdatedState(onError)
    return remember(context) {
        {
            try {
                val options = GmsBarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93, Barcode.FORMAT_ITF,
                        Barcode.FORMAT_CODABAR, Barcode.FORMAT_QR_CODE,
                    )
                    .enableAutoZoom()
                    .build()
                GmsBarcodeScanning.getClient(context, options).startScan()
                    .addOnSuccessListener { code -> code.rawValue?.trim()?.takeIf { it.isNotEmpty() }?.let(result) }
                    .addOnFailureListener { error("No se pudo abrir el escáner. Revisa que Google Play esté actualizado.") }
            } catch (e: Exception) {
                error("Este teléfono no puede abrir el escáner de códigos.")
            }
        }
    }
}

@Composable
fun ScanButton(onClick: () -> Unit, tint: Color = FV.GreenDark) {
    IconButton(onClick = onClick) { Icon(Icons.Filled.QrCodeScanner, "Escanear código de barras", tint = tint) }
}
