package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class BoxType {
    INPUT,
    TEXT,
    BUTTON,
    CHECKBOX_LIST,
    COUNTER
}