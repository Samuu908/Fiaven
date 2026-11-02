package app.rork.un30xjmm47ctw8vs4cms0.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rork.un30xjmm47ctw8vs4cms0.data.BusinessPhoto
import app.rork.un30xjmm47ctw8vs4cms0.ui.decodePhoto
import app.rork.un30xjmm47ctw8vs4cms0.ui.initials
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

/** The business photo in a rounded square; falls back to the business initials. */
@Composable
fun BusinessAvatar(photo: BusinessPhoto?, businessName: String, size: Int = 40, modifier: Modifier = Modifier) {
    val bitmap = remember(photo?.updatedAt, photo?.data?.length) { photo?.data?.let(::decodePhoto)?.asImageBitmap() }
    val shape = RoundedCornerShape((size * 0.3f).dp)
    Box(
        modifier.size(size.dp).clip(shape).background(FV.GreenSoft).border(BorderStroke(1.dp, FV.Hairline), shape),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(bitmap, label = "businessPhoto") { img ->
            if (img != null) {
                Image(img, contentDescription = "Foto del negocio", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        initials(businessName.ifBlank { "Kashly" }),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = (size * 0.36f).sp),
                        color = FV.GreenDark, fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
