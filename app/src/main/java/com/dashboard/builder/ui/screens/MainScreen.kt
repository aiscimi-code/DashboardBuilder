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
            val jsonText = if (pendingExportAll) viewModel.exportToJson()
                           else viewModel.exportCurrentTabToJson()
            if (jsonText.isEmpty()) {
                Toast.makeText(context, "Nothing to export", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            // Use "rwt" (read-write-truncate) mode: more reliable than default "w"
            // across different Android versions and storage providers (Downloads,
            // Drive, USB). The bufferedWriter ensures the OS buffer is flushed
            // before the file descriptor is released.
            context.contentResolver.openOutputStream(uri, "rwt")
                ?.bufferedWriter()
                ?.use { writer ->
                    writer.write(jsonText)
                    writer.flush()
                }
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
                                // If it's a text box, open full screen editor
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
                            // Show export/import dialog
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
    
    // Full screen text editor

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
            },
            onRestore = {
                // Show restore dialog - for now just try to restore latest
                val backups = viewModel.getBackups(context)
                if (backups.isNotEmpty()) {
                    val error = viewModel.restoreBackup(context, backups.first().first)
                    if (error != null) {
                        Toast.makeText(context, "Restore failed: $error", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Restored from backup", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No backups found", Toast.LENGTH_SHORT).show()
                }
            },
            restoreLabel = "Restore Backup"
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
            // Label field (if has label)
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
            
            // Text editing toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { /* Cut - handled by text field */ }) {
                    Icon(Icons.Default.ContentCut, "Cut")
                }
                IconButton(onClick = { /* Paste - handled by text field */ }) {
                    Icon(Icons.Default.ContentPaste, "Paste")
                }
                IconButton(onClick = { /* Select All - handled by text field */ }) {
                    Icon(Icons.Default.SelectAll, "Select All")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Full width text input
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