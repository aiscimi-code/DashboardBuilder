package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class EventType {
    ON_CLICK,
    ON_TEXT_CHANGE
}

@Serializable
enum class ActionType {
    SET_TEXT,
    ADD_TO_LIST,
    ADD_TO_CHECKBOX_LIST,
    INCREMENT_COUNTER,
    DECREMENT_COUNTER
}

@Serializable
sealed class DataSource {
    @Serializable
    data class FromBox(val boxId: String) : DataSource()

    @Serializable
    data class Static(val value: String) : DataSource()
}

@Serializable
data class Action(
    val event: EventType,
    val type: ActionType,
    val targetBoxId: String,
    val dataSource: DataSource? = null
)