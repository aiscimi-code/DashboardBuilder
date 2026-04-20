package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val x: Int = 0,
    val y: Int = 0
)