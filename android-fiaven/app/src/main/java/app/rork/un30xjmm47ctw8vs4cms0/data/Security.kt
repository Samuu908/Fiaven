package app.rork.un30xjmm47ctw8vs4cms0.data

import java.text.Normalizer

/** Preset security questions for password recovery (no phone or email needed). */
val SecurityQuestions = listOf(
    "¿Cómo se llamaba tu primera mascota?",
    "¿En qué ciudad nació tu mamá?",
    "¿Cómo se llama tu mejor amigo de la infancia?",
    "¿Cuál fue tu primer trabajo?",
    "¿Cómo se llama la calle donde creciste?",
    "¿Cuál es el segundo nombre de tu papá?",
    "¿Cuál fue tu primera escuela?",
)

/** Answers are compared without caring about accents, capitals or extra spaces. */
fun normalizeAnswer(raw: String): String =
    Normalizer.normalize(raw.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace(Regex("\\s+"), " ")

/** Social networks the creator can show under "Creado por". */
enum class SocialNetwork(val id: String, val label: String, val prefix: String) {
    INSTAGRAM("instagram", "Instagram", "@"),
    TIKTOK("tiktok", "TikTok", "@"),
    FACEBOOK("facebook", "Facebook", ""),
    X("x", "X (Twitter)", "@"),
    YOUTUBE("youtube", "YouTube", "@"),
    WHATSAPP("whatsapp", "WhatsApp", "+"),
    WEB("web", "Página web", "");

    companion object {
        fun of(id: String): SocialNetwork = entries.firstOrNull { it.id == id } ?: WEB
    }
}

fun CreditLink.cleanHandle(): String = handle.trim().removePrefix("@").removePrefix("+")

fun CreditLink.display(): String = SocialNetwork.of(network).let { n ->
    if (n == SocialNetwork.WEB) cleanHandle().removePrefix("https://").removePrefix("http://") else n.prefix + cleanHandle()
}

fun CreditLink.url(): String {
    val h = cleanHandle()
    return when (SocialNetwork.of(network)) {
        SocialNetwork.INSTAGRAM -> "https://instagram.com/$h"
        SocialNetwork.TIKTOK -> "https://www.tiktok.com/@$h"
        SocialNetwork.FACEBOOK -> "https://facebook.com/$h"
        SocialNetwork.X -> "https://x.com/$h"
        SocialNetwork.YOUTUBE -> "https://youtube.com/@$h"
        SocialNetwork.WHATSAPP -> "https://wa.me/${h.filter { it.isDigit() }}"
        SocialNetwork.WEB -> if (h.startsWith("http")) h else "https://$h"
    }
}
