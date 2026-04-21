package com.dashboard.builder.ui.screens

import android.view.ViewTreeObserver
import android.widget.Toast
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
                            val json = viewModel.exportToJson()
                            try {
                                val file = java.io.File(context.filesDir, "dashboard_layout.json")
                                file.writeText(json)
                                Toast.makeText(context, "Layout saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
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
            context = context,
            onDismiss = { showExportImportDialog = false },
            onExport = { targetAll ->
                val json = if (targetAll) viewModel.exportToJson() else viewModel.exportCurrentTabToJson()
                try {
                    val file = java.io.File(context.filesDir, if (targetAll) "dashboard_all.json" else "dashboard_current.json")
                    file.writeText(json)
                    Toast.makeText(context, "Exported to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                showExportImportDialog = false
            },
            onImport = { targetAll, jsonContent ->
                try {
                    viewModel.importFromJson(jsonContent)
                    Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                showExportImportDialog = false
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