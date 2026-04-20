package com.dashboard.builder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashboard.builder.data.model.*
import com.dashboard.builder.engine.EventEngine
import com.dashboard.builder.ui.components.grid.GridEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val appState: AppState = AppState(),
    val selectedTabId: String = "A",
    val selectedBoxId: String? = null,
    val mode: EditMode = EditMode.VIEW
)

enum class EditMode {
    VIEW,
    ADD,
    EDIT,
    MOVE,
    LINK
}

class MainViewModel : ViewModel() {
    // Simple undo stack (stores previous app states)
    private val undoStack = mutableListOf<AppState>()

    private fun pushUndo(state: AppState) {
        // Keep only last 10 states
        if (undoStack.size >= 10) undoStack.removeAt(0)
        undoStack.add(state)
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _uiState.update { it.copy(appState = previous) }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val eventEngine = EventEngine()

    // Tab operations
    fun selectTab(tabId: String) {
        _uiState.update { it.copy(selectedTabId = tabId, selectedBoxId = null, mode = EditMode.VIEW) }
    }

    // Box selection
    fun selectBox(boxId: String?) {
        _uiState.update { it.copy(selectedBoxId = boxId) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedBoxId = null, mode = EditMode.VIEW) }
    }

    // Mode
    fun setMode(mode: EditMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    // Add box
    fun addBox(type: BoxType) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            // Get default size based on type
            val defaultSize = when (type) {
                BoxType.BUTTON -> Size(1, 1)
                else -> Size(10, 2)
            }
            val position = GridEngine.findFirstAvailable(tab.boxes, defaultSize)
            val newBox = Box.create(type, position)

            val updatedTab = tab.copy(boxes = tab.boxes + newBox)
            val updatedTabs = state.appState.tabs.map {
                if (it.id == tab.id) updatedTab else it
            }

            state.copy(
                appState = state.appState.copy(tabs = updatedTabs),
                selectedBoxId = newBox.id,
                mode = EditMode.EDIT
            )
        }
    }

    // Move box
    fun moveBox(boxId: String, newX: Int, newY: Int) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            val box = tab.boxes.find { it.id == boxId } ?: return@update state

            if (box.locked) return@update state

            val clampedX = newX.coerceIn(0, GridEngine.COLUMNS - box.size.w)
            val clampedY = newY.coerceIn(0, GridEngine.ROWS - box.size.h)

            if (GridEngine.canPlace(box, clampedX, clampedY, tab.boxes)) {
                val updatedBox = box.copy(position = Position(clampedX, clampedY))
                val updatedBoxes = tab.boxes.map { if (it.id == boxId) updatedBox else it }
                val updatedTab = tab.copy(boxes = updatedBoxes)

                state.copy(
                    appState = state.appState.copy(
                        tabs = state.appState.tabs.map { if (it.id == tab.id) updatedTab else it }
                    )
                )
            } else {
                state
            }
        }
    }

    // Resize box
    fun resizeBox(boxId: String, newW: Int, newH: Int) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            val box = tab.boxes.find { it.id == boxId } ?: return@update state

            if (box.locked) return@update state

            // Clamp width to not exceed grid (max 10) and remaining space
            val maxWidth = (GridEngine.COLUMNS - box.position.x).coerceAtMost(GridEngine.COLUMNS)
            val maxHeight = (GridEngine.ROWS - box.position.y).coerceAtMost(GridEngine.ROWS)
            val clampedW = newW.coerceIn(1, maxWidth)
            val clampedH = newH.coerceIn(1, maxHeight)

            val resizedBox = box.copy(size = Size(clampedW, clampedH))

            if (GridEngine.canPlace(resizedBox, box.position.x, box.position.y, tab.boxes)) {
                val updatedBoxes = tab.boxes.map { if (it.id == boxId) resizedBox else it }
                val updatedTab = tab.copy(boxes = updatedBoxes)

                state.copy(
                    appState = state.appState.copy(
                        tabs = state.appState.tabs.map { if (it.id == tab.id) updatedTab else it }
                    )
                )
            } else {
                state
            }
        }
    }

    // Delete box
    fun deleteBox(boxId: String) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            val updatedBoxes = tab.boxes.filter { it.id != boxId }
            val updatedTab = tab.copy(boxes = updatedBoxes)

            state.copy(
                appState = state.appState.copy(
                    tabs = state.appState.tabs.map { if (it.id == tab.id) updatedTab else it }
                ),
                selectedBoxId = null,
                mode = EditMode.VIEW
            )
        }
    }

    // Update box properties
    fun updateBoxLabel(boxId: String, label: String) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        updateBox(boxId) { it.copy(label = label) }
    }

    fun updateBoxLocked(boxId: String, locked: Boolean) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        updateBox(boxId) { it.copy(locked = locked) }
    }

    fun updateBoxStyle(boxId: String, backgroundColor: String) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        updateBox(boxId) { it.copy(style = it.style.copy(backgroundColor = backgroundColor)) }
    }

    fun updateBoxConfig(boxId: String, config: BoxConfig) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        updateBox(boxId) { it.copy(config = config) }
    }

    private fun updateBox(boxId: String, transform: (Box) -> Box) {
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            val updatedBoxes = tab.boxes.map { box ->
                if (box.id == boxId) transform(box) else box
            }
            val updatedTab = tab.copy(boxes = updatedBoxes)

            state.copy(
                appState = state.appState.copy(
                    tabs = state.appState.tabs.map { if (it.id == tab.id) updatedTab else it }
                )
            )
        }
    }

    // Input handling
    fun updateInputValue(boxId: String, value: String) {
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            val box = tab.boxes.find { it.id == boxId } ?: return@update state

            val config = box.config
            val updatedBox = when (config) {
                is InputConfig -> box.copy(config = config.copy(value = value))
                is TextConfig -> box.copy(config = config.copy(value = value))
                else -> box
            }
            val updatedBoxes = tab.boxes.map { if (it.id == boxId) updatedBox else it }
            val updatedTab = tab.copy(boxes = updatedBoxes)

            // Trigger onTextChange event for input boxes
            val processedBoxes = if (config is InputConfig) {
                eventEngine.handleEvent(updatedBox, EventType.ON_TEXT_CHANGE, updatedTab)
            } else {
                updatedBoxes
            }
            val finalTab = updatedTab.copy(boxes = processedBoxes)

            state.copy(
                appState = state.appState.copy(
                    tabs = state.appState.tabs.map { if (it.id == tab.id) finalTab else it }
                )
            )
        }
    }

    // Button click
    fun onButtonClick(boxId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
                val box = tab.boxes.find { it.id == boxId } ?: return@update state

                val processedBoxes = eventEngine.handleEvent(box, EventType.ON_CLICK, tab)
                val updatedTab = tab.copy(boxes = processedBoxes)

                state.copy(
                    appState = state.appState.copy(
                        tabs = state.appState.tabs.map { if (it.id == tab.id) updatedTab else it }
                    )
                )
            }
        }
    }

    // Counter controls
    fun incrementCounter(boxId: String) {
        updateBox(boxId) { box ->
            val config = box.config as? CounterConfig ?: return@updateBox box
            box.copy(config = config.copy(value = config.value + 1))
        }
    }

    fun decrementCounter(boxId: String) {
        updateBox(boxId) { box ->
            val config = box.config as? CounterConfig ?: return@updateBox box
            box.copy(config = config.copy(value = config.value - 1))
        }
    }

    // Checkbox toggle
    fun toggleCheckbox(boxId: String, itemIndex: Int) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            val box = tab.boxes.find { it.id == boxId } ?: return@update state

            val config = box.config as? CheckboxListConfig ?: return@update state
            val updatedItems = config.items.mapIndexed { index, item ->
                if (index == itemIndex) item.copy(checked = !item.checked) else item
            }
            val updatedBox = box.copy(config = config.copy(items = updatedItems))
            val updatedBoxes = tab.boxes.map { if (it.id == boxId) updatedBox else it }

            state.copy(
                appState = state.appState.copy(
                    tabs = state.appState.tabs.map { if (it.id == tab.id) tab.copy(boxes = updatedBoxes) else it }
                )
            )
        }
    }

    // Add action to box
    fun addAction(boxId: String, action: Action) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        updateBox(boxId) { box ->
            box.copy(actions = box.actions + action)
        }
    }

    // Remove action from box
    fun removeAction(boxId: String, actionIndex: Int) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        updateBox(boxId) { box ->
            box.copy(actions = box.actions.filterIndexed { index, _ -> index != actionIndex })
        }
    }

    // Export JSON
    fun exportToJson(): String {
        return kotlinx.serialization.json.Json.encodeToString(
            AppState.serializer(),
            _uiState.value.appState
        )
    }

    // Export only the current tab
    fun exportCurrentTabToJson(): String {
        val currentTabId = _uiState.value.selectedTabId
        val currentTab = _uiState.value.appState.tabs.find { it.id == currentTabId }
        return if (currentTab != null) {
            kotlinx.serialization.json.Json.encodeToString(com.dashboard.builder.data.model.Tab.serializer(), currentTab)
        } else {
            ""
        }
    }

    // Import JSON
    fun importFromJson(json: String) {
        try {
            val appState = kotlinx.serialization.json.Json.decodeFromString<AppState>(json)
            _uiState.update { it.copy(appState = appState) }
        } catch (e: Exception) {
            // Handle error - in production, would expose error to UI
        }
    }
}