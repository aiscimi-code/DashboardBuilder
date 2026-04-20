package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Style(
    val backgroundColor: String = "#FFFFFF",
    val textColor: String = "#000000"
)