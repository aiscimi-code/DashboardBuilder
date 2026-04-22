# Export / Import — File-Picker Implementation
## DashboardBuilder · April 2026

---

## Problem Summary

The previous export/import had two major flaws:

1. **Export** always wrote to a hardcoded path inside `context.filesDir`
   (e.g. `data/data/com.dashboard.builder/files/dashboard_all.json`).
   This is private internal storage — completely invisible to the user
   in Files, Downloads, or any other app. There was no way to share,
   back up, or move these files.

2. **Import** required the user to paste raw JSON text into a tiny
   120dp text field inside the dialog. No file picker, no way to
   browse files. Worse, importing a single-tab JSON silently failed
   because `decodeFromString<AppState>()` throws on a `Tab` object
   and the exception was swallowed with no feedback.

---

## What Was Changed

### Files modified

| File | Change |
|---|---|
| `viewmodel/MainViewModel.kt` | New Json instance; `importFromJson` returns errors; new `importTabFromJson` |
| `ui/dialogs/ExportImportDialog.kt` | Complete rewrite — pure UI, no Context, four picker buttons |
| `ui/screens/MainScreen.kt` | Three SAF launchers; rewired dialog call; Save button uses SAF |

---

## How It Works Now

**Export** — tapping "Export All Tabs" or "Export Current Tab" opens the
Android system "Save As" picker (`CreateDocument`). The user can name
the file and save it anywhere: Downloads, Google Drive, a USB drive, etc.
The current tab's name is used as the suggested filename for single-tab
exports (e.g. `dashboard_Training.json`).

**Import** — tapping "Import All Tabs" or "Import Single Tab" opens the
system file browser (`OpenDocument`), filtered to `application/json` and
`text/plain`. Selecting a file reads it via `ContentResolver` and passes
the text to the ViewModel. Errors are shown in a Toast.

**Single-tab import** correctly uses `decodeFromString<Tab>()` instead
of `<AppState>()`. If the imported tab's ID matches an existing tab it
replaces it; if not, it is appended.

**The Save button** in the top bar also now opens the SAF picker instead
of writing to the hidden internal files directory.

---

## Full Code

---

### 1. `viewmodel/MainViewModel.kt` — changed section only

Replace everything from `// Export JSON` to the end of the class (the
closing `}`) with the block below.

```kotlin
    // ── Serialization helper ──────────────────────────────────────
    // ignoreUnknownKeys lets older exports load cleanly on newer builds
    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    // ── Export ────────────────────────────────────────────────────

    /** Serialise the full AppState (all tabs). */
    fun exportToJson(): String =
        json.encodeToString(AppState.serializer(), _uiState.value.appState)

    /** Serialise only the currently-selected tab. */
    fun exportCurrentTabToJson(): String {
        val currentTab = _uiState.value.run {
            appState.tabs.find { it.id == selectedTabId }
        } ?: return ""
        return json.encodeToString(Tab.serializer(), currentTab)
    }

    // ── Import ────────────────────────────────────────────────────

    /**
     * Import a full AppState JSON (all tabs).
     * Returns null on success or an error message on failure.
     */
    fun importFromJson(jsonText: String): String? {
        return try {
            val appState = json.decodeFromString<AppState>(jsonText)
            _uiState.update { it.copy(appState = appState) }
            null
        } catch (e: Exception) {
            e.message ?: "Unknown parse error"
        }
    }

    /**
     * Import a single-Tab JSON, replacing the tab with the same id
     * or appending it if no match exists.
     * Returns null on success or an error message on failure.
     */
    fun importTabFromJson(jsonText: String): String? {
        return try {
            val incoming = json.decodeFromString<Tab>(jsonText)
            _uiState.update { state ->
                val tabs = state.appState.tabs
                val updatedTabs = if (tabs.any { it.id == incoming.id }) {
                    tabs.map { if (it.id == incoming.id) incoming else it }
                } else {
                    tabs + incoming
                }
                state.copy(appState = state.appState.copy(tabs = updatedTabs))
            }
            null
        } catch (e: Exception) {
            e.message ?: "Unknown parse error"
        }
    }
}
```

---

### 2. `ui/dialogs/ExportImportDialog.kt` — full file replacement

```kotlin
package com.dashboard.builder.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Export / Import dialog.
 *
 * All file I/O happens outside this composable via the four callbacks —
 * the caller registers SAF launchers (CreateDocument / OpenDocument) and
 * invokes them from these lambdas. This keeps the dialog pure-UI with no
 * Context dependency.
 */
@Composable
fun ExportImportDialog(
    onDismiss: () -> Unit,
    onExportAll: () -> Unit,
    onExportCurrentTab: () -> Unit,
    onImportAll: () -> Unit,
    onImportCurrentTab: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export / Import Layout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // ── Export ──────────────────────────────────────────────
                Text("Export", style = MaterialTheme.typography.titleSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onExportAll(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("All tabs", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onExportCurrentTab(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Current tab", style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // ── Import ──────────────────────────────────────────────
                Text("Import", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Pick a previously exported .json file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onImportAll(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("All tabs", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onImportCurrentTab(); onDismiss() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Single tab", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
```

---

### 3. `ui/screens/MainScreen.kt` — full file replacement

```kotlin
package com.dashboard.builder.ui.screens

import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashboard.builder.ui.components.BottomMenu
import com.dashboard.builder.ui.components.TabBar
import com.dashboard.builder.ui.components.grid.GridCanvas
import com.dashboard.builder.ui.dialogs.AddBoxSheet
import com.dashboard.builder.ui.dialogs.EditBoxSheet
import com.dashboard.builder.ui.dialogs.ExportImportDialog
import com.dashboard.builder.viewmodel.EditMode
import com.dashboard.builder.viewmodel.MainViewModel
import com.dashboard.builder.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    // Get actual screen width in dp
    val screenWidthDp = configuration.screenWidthDp.dp

    val currentTab = uiState.appState.tabs.find { it.id == uiState.selectedTabId }
    val selectedBox = currentTab?.boxes?.find { it.id == uiState.selectedBoxId }

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showFullScreenEditor by remember { mutableStateOf(false) }
    var showExportImportDialog by remember { mutableStateOf(false) }

    // ── SAF file launchers ───────────────────────────────────────────────────
    // We need to know *which* export to perform when the picker returns a URI,
    // so we track the pending mode in a remembered ref.
    var pendingExportAll by remember { mutableStateOf(true) }

    /** Export launcher — opens the system "Save As" picker. */
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val json = if (pendingExportAll) viewModel.exportToJson()
                       else viewModel.exportCurrentTabToJson()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Import-all launcher — opens the system file picker, replaces all tabs. */
    val importAllLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.readText() ?: return@rememberLauncherForActivityResult
            val error = viewModel.importFromJson(json)
            if (error == null) {
                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Import failed: $error", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** Import-tab launcher — opens the system file picker, merges a single tab. */
    val importTabLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.readText() ?: return@rememberLauncherForActivityResult
            val error = viewModel.importTabFromJson(json)
            if (error == null) {
                Toast.makeText(context, "Tab imported successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Import failed: $error", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    // ── end SAF launchers ────────────────────────────────────────────────────

    // Track keyboard visibility
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardVisible = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Dashboard Builder") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                // Options bar at top when keyboard is NOT visible
                if (!isKeyboardVisible) {
                    BottomMenu(
                        currentMode = uiState.mode,
                        hasSelection = uiState.selectedBoxId != null,
                        onAddClick = { showAddSheet = true },
                        onEditClick = {
                            if (uiState.selectedBoxId != null) {
                                if (selectedBox?.type == BoxType.TEXT) {
                                    showFullScreenEditor = true
                                } else {
                                    showEditSheet = true
                                }
                            }
                        },
                        onMoveClick = {
                            if (uiState.selectedBoxId != null) {
                                viewModel.setMode(EditMode.MOVE)
                            }
                        },
                        onDeleteClick = {
                            uiState.selectedBoxId?.let { viewModel.deleteBox(it) }
                        },
                        onExportClick = {
                            showExportImportDialog = true
                        },
                        onSaveClick = {
                            // Quick-save all tabs using SAF so user can choose location
                            pendingExportAll = true
                            exportLauncher.launch("dashboard_layout.json")
                        },
                        onUndoClick = {
                            viewModel.undo()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab bar
            TabBar(
                state = uiState,
                onTabSelected = { viewModel.selectTab(it) }
            )

            // Grid - use actual screen width to fit 10 columns
            if (currentTab != null) {
                GridCanvas(
                    tab = currentTab,
                    selectedBoxId = uiState.selectedBoxId,
                    availableWidth = screenWidthDp,
                    isMoveMode = uiState.mode == EditMode.MOVE,
                    onBoxSelected = { boxId ->
                        viewModel.selectBox(boxId)
                    },
                    onBoxDoubleSelected = { boxId ->
                        viewModel.selectBox(boxId)
                        val box = currentTab.boxes.find { it.id == boxId }
                        if (box?.type == BoxType.TEXT) {
                            showFullScreenEditor = true
                        } else {
                            showEditSheet = true
                        }
                    },
                    onBoxMoved = { boxId, x, y ->
                        viewModel.moveBox(boxId, x, y)
                    },
                    onBoxResized = { boxId, w, h ->
                        viewModel.resizeBox(boxId, w, h)
                    },
                    onInputChange = { boxId, value ->
                        viewModel.updateInputValue(boxId, value)
                    },
                    onCheckboxToggle = { boxId, index ->
                        viewModel.toggleCheckbox(boxId, index)
                    },
                    onBoxButtonClick = { boxId ->
                        viewModel.onButtonClick(boxId)
                    },
                    onBoxIncrement = { boxId ->
                        viewModel.incrementCounter(boxId)
                    },
                    onBoxDecrement = { boxId ->
                        viewModel.decrementCounter(boxId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Add box sheet
    if (showAddSheet) {
        AddBoxSheet(
            onDismiss = { showAddSheet = false },
            onBoxTypeSelected = { boxType ->
                viewModel.addBox(type = boxType)
                showAddSheet = false
            }
        )
    }

    // Edit box sheet (show for non-text boxes or when manually opened)
    if (showEditSheet && selectedBox != null && currentTab != null && selectedBox.type != BoxType.TEXT) {
        EditBoxSheet(
            box = selectedBox,
            allBoxes = currentTab.boxes,
            onDismiss = { showEditSheet = false },
            onUpdateLabel = { viewModel.updateBoxLabel(selectedBox.id, it) },
            onUpdateLocked = { viewModel.updateBoxLocked(selectedBox.id, it) },
            onUpdateBackgroundColor = { viewModel.updateBoxStyle(selectedBox.id, it) },
            onUpdateConfig = { viewModel.updateBoxConfig(selectedBox.id, it) },
            onUpdateSize = { w, h -> viewModel.resizeBox(selectedBox.id, w, h) },
            onAddAction = { viewModel.addAction(selectedBox.id, it) },
            onRemoveAction = { viewModel.removeAction(selectedBox.id, it) }
        )
    }

    if (showExportImportDialog) {
        ExportImportDialog(
            onDismiss = { showExportImportDialog = false },
            onExportAll = {
                pendingExportAll = true
                exportLauncher.launch("dashboard_all.json")
            },
            onExportCurrentTab = {
                pendingExportAll = false
                val tabName = currentTab?.name?.replace(Regex("[^A-Za-z0-9_-]"), "_") ?: "tab"
                exportLauncher.launch("dashboard_$tabName.json")
            },
            onImportAll = {
                importAllLauncher.launch(arrayOf("application/json", "text/plain"))
            },
            onImportCurrentTab = {
                importTabLauncher.launch(arrayOf("application/json", "text/plain"))
            }
        )
    }

    // Full screen text editor
    if (showFullScreenEditor && selectedBox != null) {
        FullScreenTextEditor(
            box = selectedBox,
            onDismiss = { showFullScreenEditor = false },
            onSave = { newValue ->
                val newConfig = selectedBox.config.let {
                    when (it) {
                        is TextConfig -> it.copy(value = newValue)
                        is InputConfig -> it.copy(value = newValue)
                        else -> it
                    }
                }
                viewModel.updateBoxConfig(selectedBox.id, newConfig)
                showFullScreenEditor = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenTextEditor(
    box: Box,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textValue by remember {
        mutableStateOf(
            when (val config = box.config) {
                is TextConfig -> config.value
                is InputConfig -> config.value
                else -> ""
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit ${box.type.name}") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(textValue) }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (box.label.isNotEmpty()) {
                OutlinedTextField(
                    value = box.label,
                    onValueChange = { },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { }) { Icon(Icons.Default.ContentCut, "Cut") }
                IconButton(onClick = { }) { Icon(Icons.Default.ContentPaste, "Paste") }
                IconButton(onClick = { }) { Icon(Icons.Default.SelectAll, "Select All") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                textStyle = TextStyle(fontSize = 16.sp),
                placeholder = { Text("Enter text...") }
            )
        }
    }
}
```

---

## Where to place the files

```
app/src/main/java/com/dashboard/builder/
├── viewmodel/
│   └── MainViewModel.kt          ← replace export/import section at bottom
├── ui/
│   ├── dialogs/
│   │   └── ExportImportDialog.kt ← replace entire file
│   └── screens/
│       └── MainScreen.kt         ← replace entire file
```

No changes to `AndroidManifest.xml`, `build.gradle.kts`, or any other
file are required. The SAF (Storage Access Framework) APIs used here
(`CreateDocument`, `OpenDocument`) do not require any manifest
permissions — Android grants URI access automatically through the
system picker.
