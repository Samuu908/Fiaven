package app.rork.un30xjmm47ctw8vs4cms0.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.rork.un30xjmm47ctw8vs4cms0.ui.AppViewModel
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.EmptyState
import app.rork.un30xjmm47ctw8vs4cms0.ui.components.FvCard
import app.rork.un30xjmm47ctw8vs4cms0.ui.theme.FV
import app.rork.un30xjmm47ctw8vs4cms0.ui.timeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(vm: AppViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val list = remember(data.notes, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) data.notes else data.notes.filter { it.title.lowercase().contains(q) || it.body.lowercase().contains(q) }
    }

    Scaffold(
        containerColor = FV.Canvas,
        topBar = {
            TopAppBar(
                title = { Text("Notas") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Canvas),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onOpen("nueva") },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nueva nota") },
                containerColor = FV.Green, contentColor = FV.Surface,
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Pedidos, pendientes y recordatorios. Todo el equipo las ve.",
                    style = MaterialTheme.typography.bodyMedium, color = FV.Muted,
                )
            }
            if (data.notes.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        placeholder = { Text("Buscar en notas") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, "Limpiar") } },
                        singleLine = true, shape = RoundedCornerShape(28.dp), colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (list.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.StickyNote2,
                        if (data.notes.isEmpty()) "Tu bloc está vacío" else "Sin resultados",
                        if (data.notes.isEmpty()) "Anota lo que hay que pedir al proveedor, encargos de clientes o cualquier pendiente."
                        else "Prueba con otra palabra.",
                    )
                }
            } else {
                items(list, key = { it.id }) { n ->
                    FvCard(onClick = { onOpen(n.id) }, contentPadding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    n.title.ifBlank { n.body.lineSequence().firstOrNull().orEmpty().ifBlank { "Sin título" } },
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                                    color = FV.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                )
                                if (n.body.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(n.body, style = MaterialTheme.typography.bodyMedium, color = FV.Ink.copy(alpha = 0.8f), maxLines = 4, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            if (n.pinned) Icon(Icons.Filled.PushPin, "Fijada", tint = FV.Green, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${n.authorName} · ${timeLabel(n.updatedAt)}", style = MaterialTheme.typography.labelMedium, color = FV.Muted)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(vm: AppViewModel, noteId: String?, onBack: () -> Unit) {
    val data by vm.data.collectAsStateWithLifecycle()
    val original = remember(noteId) { noteId?.let { id -> data.notes.find { it.id == id } } }
    var savedId by rememberSaveable { mutableStateOf(original?.id) }
    var title by rememberSaveable { mutableStateOf(original?.title.orEmpty()) }
    var body by rememberSaveable { mutableStateOf(original?.body.orEmpty()) }
    var pinned by rememberSaveable { mutableStateOf(original?.pinned ?: false) }
    var confirmDelete by remember { mutableStateOf(false) }

    fun saveAndClose() {
        val changed = title != original?.title.orEmpty() || body != original?.body.orEmpty() || pinned != (original?.pinned ?: false)
        if (changed && (title.isNotBlank() || body.isNotBlank())) savedId = vm.saveNote(savedId, title, body, pinned)
        onBack()
    }
    BackHandler { saveAndClose() }

    val clear = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
        cursorColor = FV.Green,
    )

    Scaffold(
        containerColor = FV.Surface,
        topBar = {
            TopAppBar(
                title = { Text(if (original == null) "Nueva nota" else "Nota") },
                navigationIcon = { IconButton(onClick = { saveAndClose() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Guardar y volver") } },
                actions = {
                    IconButton(onClick = { pinned = !pinned }) {
                        Icon(if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, if (pinned) "Desfijar" else "Fijar arriba", tint = if (pinned) FV.Green else FV.Muted)
                    }
                    if (original != null) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "Borrar nota", tint = FV.Muted) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FV.Surface),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 4.dp)) {
            TextField(
                value = title, onValueChange = { title = it },
                placeholder = { Text("Título", style = MaterialTheme.typography.headlineSmall) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = FV.Ink),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true, colors = clear, modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = body, onValueChange = { body = it },
                placeholder = { Text("Escribe aquí…") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = FV.Ink),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = clear, modifier = Modifier.fillMaxWidth().weight(1f),
            )
            original?.let {
                Text(
                    "Creada por ${it.authorName} · editada ${timeLabel(it.updatedAt)}",
                    style = MaterialTheme.typography.labelMedium, color = FV.Muted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            Spacer(Modifier.width(0.dp))
        }
    }

    if (confirmDelete && original != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = FV.Surface,
            title = { Text("¿Borrar esta nota?") },
            text = { Text("Se borrará en todos los teléfonos del negocio.") },
            confirmButton = { TextButton(onClick = { vm.deleteNote(original); confirmDelete = false; onBack() }) { Text("Borrar", color = FV.Danger) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}
