package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Size(
    val w: Int = 10,
    val h: Int = 2
)