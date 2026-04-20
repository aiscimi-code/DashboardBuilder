package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class BoxConfig {
    companion object {
        fun default(type: BoxType): BoxConfig = when (type) {
            BoxType.INPUT -> InputConfig()
            BoxType.TEXT -> TextConfig()
            BoxType.BUTTON -> ButtonConfig()
            BoxType.CHECKBOX_LIST -> CheckboxListConfig()
            BoxType.COUNTER -> CounterConfig()
        }
    }
}

@Serializable
data class InputConfig(
    val placeholder: String = "",
    val value: String = ""
) : BoxConfig()

@Serializable
data class TextConfig(
    val value: String = "Text",
    val editable: Boolean = false
) : BoxConfig()

@Serializable
data class ButtonConfig(
    val text: String = "Button"
) : BoxConfig()

@Serializable
data class CheckboxItem(
    val text: String,
    val checked: Boolean = false
)

@Serializable
data class CheckboxListConfig(
    val items: List<CheckboxItem> = emptyList()
) : BoxConfig()

@Serializable
data class CounterConfig(
    val value: Int = 0
) : BoxConfig()