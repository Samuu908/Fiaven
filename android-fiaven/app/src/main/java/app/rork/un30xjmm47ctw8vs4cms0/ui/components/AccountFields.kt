package app.rork.un30xjmm47ctw8vs4cms0.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.rork.un30xjmm47ctw8vs4cms0.data.Credits
import app.rork.un30xjmm47ctw8vs4cms0.data.SecurityQuestions
import app.rork.un30xjmm47ctw8vs4cms0.data.display
import app.rork.un30xjmm47ctw8vs4cms0.data.url
import app.rork.un30xjmm47ctw8vs4cms0.ui.screens.fieldColors
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPicker(question: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }, modifier = modifier) {
        OutlinedTextField(
            value = question,
            onValueChange = {},
            readOnly = true,
            label = { Text("Pregunta de seguridad") },
            placeholder = { Text("Elige una pregunta") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = FV.Surface) {
            SecurityQuestions.forEach { q ->
                DropdownMenuItem(text = { Text(q) }, onClick = { onChange(q); open = false })
            }
        }
    }
}

@Composable
fun AnswerField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.take(80)) },
        label = { Text("Tu respuesta") },
        supportingText = { Text("No importan las mayúsculas ni los acentos") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
) {
    var show by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Lock, null) },
        trailingIcon = {
            IconButton(onClick = { show = !show }) {
                Icon(if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, if (show) "Ocultar" else "Mostrar")
            }
        },
        singleLine = true,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Small "Creado por Samuel Martinez · @Samuuj_m" line; the handle opens the profile. */
@Composable
fun CreatorCredit(credits: Credits, modifier: Modifier = Modifier) {
    val uri = LocalUriHandler.current
    val first = credits.links.firstOrNull()
    Row(
        modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("Creado por ", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
        Text(credits.name, style = MaterialTheme.typography.bodySmall, color = FV.Ink, fontWeight = FontWeight.SemiBold)
        if (first != null) {
            Text(" · ", style = MaterialTheme.typography.bodySmall, color = FV.Muted)
            Text(
                first.display(),
                style = MaterialTheme.typography.bodySmall, color = FV.Green, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { runCatching { uri.openUri(first.url()) } }.padding(vertical = 6.dp),
            )
        }
    }
}

