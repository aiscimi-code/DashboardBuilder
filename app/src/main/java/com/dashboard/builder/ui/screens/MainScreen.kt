package com.dashboard.builder.ui.screens

import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dashboard.builder.ui.components.BottomMenu
import com.dashboard.builder.ui.components.TabBar
import com.dashboard.builder.ui.components.grid.GridCanvas
import com.dashboard.builder.ui.dialogs.AddBoxSheet
import com.dashboard.builder.ui.dialogs.EditBoxSheet
import com.dashboard.builder.viewmodel.EditMode
import com.dashboard.builder.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val currentTab = uiState.appState.tabs.find { it.id == uiState.selectedTabId }
    val selectedBox = currentTab?.boxes?.find { it.id == uiState.selectedBoxId }

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }

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
                                showEditSheet = true
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
                            val json = viewModel.exportToJson()
                            Toast.makeText(context, "Exported: ${json.take(100)}...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        var gridAvailableWidth by remember { mutableStateOf(0.dp) }
        
        // Measure available width using BoxWithConstraints
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            gridAvailableWidth = maxWidth
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab bar
                TabBar(
                    state = uiState,
                    onTabSelected = { viewModel.selectTab(it) }
                )
                
                // Grid - use available width
                if (currentTab != null) {
                    GridCanvas(
                        tab = currentTab,
                        selectedBoxId = uiState.selectedBoxId,
                        availableWidth = gridAvailableWidth,
                        isMoveMode = uiState.mode == EditMode.MOVE,
                        onBoxSelected = { boxId ->
                            viewModel.selectBox(boxId)
                        },
                        onBoxMoved = { boxId, x, y ->
                            viewModel.moveBox(boxId, x, y)
                        },
                        onBoxResized = { boxId, w, h ->
                            viewModel.resizeBox(boxId, w, h)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
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

    // Edit box sheet (show when keyboard is visible or when manually opened)
    if (showEditSheet && selectedBox != null && currentTab != null) {
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
}