package com.dashboard.builder.engine

import com.dashboard.builder.data.model.*

class EventEngine {

    /**
     * Handle an event from a box, executing all matching actions
     */
    fun handleEvent(
        sourceBox: Box,
        event: EventType,
        tab: Tab
    ): List<Box> {
        val actions = sourceBox.actions.filter { it.event == event }
        if (actions.isEmpty()) return tab.boxes

        return tab.boxes.map { targetBox ->
            actions.fold(targetBox) { box, action ->
                if (action.targetBoxId == box.id) {
                    executeAction(box, action, tab)
                } else {
                    box
                }
            }
        }
    }

    private fun executeAction(box: Box, action: Action, tab: Tab): Box {
        val data = resolveData(action.dataSource, tab)

        return when (action.type) {
            ActionType.SET_TEXT -> applySetText(box, data)
            ActionType.ADD_TO_LIST -> applyAddToList(box, data)
            ActionType.ADD_TO_CHECKBOX_LIST -> applyAddToCheckboxList(box, data)
            ActionType.INCREMENT_COUNTER -> applyIncrementCounter(box)
            ActionType.DECREMENT_COUNTER -> applyDecrementCounter(box)
            ActionType.SWITCH_TAB -> box // No direct effect here; handled in ViewModel
        }
    }

    private fun resolveData(source: DataSource?, tab: Tab): String? {
        return when (source) {
            is DataSource.FromBox -> {
                val sourceBox = tab.boxes.find { it.id == source.boxId }
                extractBoxValue(sourceBox)
            }
            is DataSource.Static -> source.value
            null -> null
        }
    }

    private fun extractBoxValue(box: Box?): String? {
        if (box == null) return null
        return when (val config = box.config) {
            is InputConfig -> config.value
            is TextConfig -> config.value
            is ButtonConfig -> config.text
            is CheckboxListConfig -> config.items.joinToString(",") { it.text }
            is CounterConfig -> config.value.toString()
        }
    }

    private fun applySetText(box: Box, data: String?): Box {
        val config = box.config as? TextConfig ?: return box
        return box.copy(config = config.copy(value = data ?: ""))
    }

    private fun applyAddToList(box: Box, data: String?): Box {
        // List box not in MVP, but keeping for future
        return box
    }

    private fun applyAddToCheckboxList(box: Box, data: String?): Box {
        val config = box.config as? CheckboxListConfig ?: return box
        if (data.isNullOrBlank()) return box

        val newItem = CheckboxItem(text = data, checked = false)
        return box.copy(config = config.copy(items = config.items + newItem))
    }

    private fun applyIncrementCounter(box: Box): Box {
        val config = box.config as? CounterConfig ?: return box
        return box.copy(config = config.copy(value = config.value + 1))
    }

    private fun applyDecrementCounter(box: Box): Box {
        val config = box.config as? CounterConfig ?: return box
        return box.copy(config = config.copy(value = config.value - 1))
    }
}