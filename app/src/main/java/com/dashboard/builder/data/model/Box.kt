package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Box(
    val id: String,
    val type: BoxType,
    val label: String = "",
    val position: Position = Position(),
    val size: Size = Size(),
    val locked: Boolean = false,
    val floating: Boolean = false,
    val style: Style = Style(),
    val config: BoxConfig,
    val actions: List<Action> = emptyList()
) {
    companion object {
        private var idCounter = 0

        fun generateId(type: BoxType): String {
            idCounter++
            return "${type.name.lowercase()}_$idCounter"
        }

        fun create(type: BoxType, position: Position = Position()): Box {
            return Box(
                id = generateId(type),
                type = type,
                label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                position = position,
                size = Size(2, 2),
                config = BoxConfig.default(type)
            )
        }
    }
}