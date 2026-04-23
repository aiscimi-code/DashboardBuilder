package com.dashboard.builder.viewmodel

import android.content.Context
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

data class TabPagination(
    val currentPage: Int,
    val totalPages: Int,
    val visibleTabIds: List<String>,
    val showAddButton: Boolean
)

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
    
    // Add a new tab
    fun addTab() {
        val currentState = _uiState.value
        val existingIds = currentState.appState.tabs.map { it.id }
        val newTabId = TabManager.getNextTabId(existingIds)
        
        if (newTabId == null) {
            // Max tabs reached
            return
        }
        
        // Save current state for undo
        pushUndo(currentState.appState)
        
        val newTab = Tab.createDefault(newTabId, "Tab $newTabId")
        _uiState.update { state ->
            state.copy(
                appState = state.appState.copy(
                    tabs = state.appState.tabs + newTab
                ),
                selectedTabId = newTabId,
                selectedBoxId = null,
                mode = EditMode.VIEW
            )
        }
    }
    
    // Get tab pagination info
    fun getTabPagination(): TabPagination {
        val totalTabs = _uiState.value.appState.tabs.size
        val currentPage = TabManager.getPageForTab(_uiState.value.selectedTabId, totalTabs)
        val totalPages = TabManager.getTotalPages(totalTabs, TabManager.canAddMoreTabs(totalTabs))
        val visibleTabs = TabManager.getTabsForPage(currentPage, totalTabs, TabManager.canAddMoreTabs(totalTabs))
        val showAddButton = TabManager.canAddMoreTabs(totalTabs) && 
            currentPage == totalPages - 1 && 
            !visibleTabs.contains("+")
        
        return TabPagination(
            currentPage = currentPage,
            totalPages = totalPages,
            visibleTabIds = visibleTabs,
            showAddButton = showAddButton
        )
    }
    
    // Go to previous page of tabs
    fun previousTabPage() {
        val pagination = getTabPagination()
        if (pagination.currentPage > 0) {
            val currentTabIndex = _uiState.value.appState.tabs.indexOfFirst { it.id == _uiState.value.selectedTabId }
            val newIndex = (currentTabIndex - TabManager.TABS_PER_PAGE).coerceAtLeast(0)
            val newTabId = _uiState.value.appState.tabs.getOrNull(newIndex)?.id ?: return
            selectTab(newTabId)
        }
    }
    
    // Go to next page of tabs
    fun nextTabPage() {
        val pagination = getTabPagination()
        if (pagination.currentPage < pagination.totalPages - 1) {
            val currentTabIndex = _uiState.value.appState.tabs.indexOfFirst { it.id == _uiState.value.selectedTabId }
            val newIndex = (currentTabIndex + TabManager.TABS_PER_PAGE).coerceAtMost(_uiState.value.appState.tabs.size - 1)
            val newTabId = _uiState.value.appState.tabs.getOrNull(newIndex)?.id ?: return
            selectTab(newTabId)
        }
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
    fun addBox(type: BoxType, size: Size? = null) {
        // Save current state for undo
        pushUndo(_uiState.value.appState)
        _uiState.update { state ->
            val tab = state.appState.tabs.find { it.id == state.selectedTabId } ?: return@update state
            // Get default size based on type, or use provided size
            val boxSize = size ?: when (type) {
                BoxType.BUTTON -> Size(1, 1)
                else -> Size(1, 5)
            }
            val position = GridEngine.findFirstAvailable(tab.boxes, boxSize)
            val newBox = Box.create(type, position, boxSize)

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

    // ── Serialization helper ──────────────────────────────────────
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
     * Import JSON — accepts our own AppState/Tab exports *or* any arbitrary
     * JSON file. Uses [JsonImportConverter] to detect the format and convert.
     *
     * For "Import All Tabs": replaces the whole AppState.
     * Returns null on success, or an error message on failure.
     */
    fun importFromJson(jsonText: String): String? {
        val result = com.dashboard.builder.data.JsonImportConverter.parse(jsonText)
        return if (result.isSuccess) {
            _uiState.update { it.copy(appState = result.getOrThrow()) }
            null
        } else {
            result.exceptionOrNull()?.message ?: "Unknown parse error"
        }
    }

    /**
     * Import a single-Tab JSON (or the first tab of any imported JSON),
     * replacing the matching tab or appending if no match.
     * Returns null on success, or an error message on failure.
     */
    fun importTabFromJson(jsonText: String): String? {
        val result = com.dashboard.builder.data.JsonImportConverter.parse(jsonText)
        return if (result.isSuccess) {
            val importedTabs = result.getOrThrow().tabs
            if (importedTabs.isEmpty()) return "No tabs found in imported file"
            // Merge each imported tab into current state
            _uiState.update { state ->
                var tabs = state.appState.tabs
                importedTabs.forEach { incoming ->
                    tabs = if (tabs.any { it.id == incoming.id }) {
                        tabs.map { if (it.id == incoming.id) incoming else it }
                    } else {
                        tabs + incoming
                    }
                }
                state.copy(appState = state.appState.copy(tabs = tabs))
            }
            null
        } else {
            result.exceptionOrNull()?.message ?: "Unknown parse error"
        }
    }

    // ── Auto-save / Backup / Restore ───────────────────────────────────────

    companion object {
        private const val PREFS_NAME = "dashboard_prefs"
        private const val KEY_LAST_BACKUP = "last_backup_index"
        private const val MAX_BACKUPS = 3
    }

    /** Save current state to internal storage and rotate backups. */
    fun saveToInternalStorage(context: Context) {
        try {
            val jsonText = json.encodeToString(AppState.serializer(), _uiState.value.appState)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            // Get current backup index
            var index = prefs.getInt(KEY_LAST_BACKUP, 0)
            val backupFile = File(backupDir, "backup_$index.json")
            backupFile.writeText(jsonText)

            // Rotate: move to next index for next save
            index = (index + 1) % MAX_BACKUPS
            prefs.edit().putInt(KEY_LAST_BACKUP, index).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Load latest state from internal storage. Returns true if loaded. */
    fun loadFromInternalStorage(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val index = prefs.getInt(KEY_LAST_BACKUP, 0)
            val backupDir = File(context.filesDir, "backups")
            val backupFile = File(backupDir, "backup_$index.json")

            if (backupFile.exists()) {
                val jsonText = backupFile.readText()
                val result = com.dashboard.builder.data.JsonImportConverter.parse(jsonText)
                if (result.isSuccess) {
                    _uiState.update { it.copy(appState = result.getOrThrow()) }
                    true
                } else false
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Get list of available backups with timestamps. */
    fun getBackups(context: Context): List<Pair<String, String>> {
        val backups = mutableListOf<Pair<String, String>>()
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return backups

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        for (i in 0 until MAX_BACKUPS) {
            val file = File(backupDir, "backup_$i.json")
            if (file.exists()) {
                val timestamp = dateFormat.format(Date(file.lastModified()))
                backups.add("backup_$i.json" to timestamp)
            }
        }
        return backups.sortedByDescending { it.second }
    }

    /** Restore from a specific backup file. */
    fun restoreBackup(context: Context, filename: String): String? {
        return try {
            val backupDir = File(context.filesDir, "backups")
            val backupFile = File(backupDir, filename)
            if (!backupFile.exists()) return "Backup file not found"

            val jsonText = backupFile.readText()
            val result = com.dashboard.builder.data.JsonImportConverter.parse(jsonText)
            if (result.isSuccess) {
                _uiState.update { it.copy(appState = result.getOrThrow()) }
                null
            } else {
                result.exceptionOrNull()?.message ?: "Failed to parse backup"
            }
        } catch (e: Exception) {
            e.message ?: "Failed to restore backup"
        }
    }
}