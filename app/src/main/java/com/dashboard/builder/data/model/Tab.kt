package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Tab(
    val id: String,
    val name: String,
    val backgroundColor: String = "#F5F5F5",
    val boxes: List<Box> = emptyList()
) {
    companion object {
        fun createDefault(id: String, name: String): Tab {
            return Tab(id = id, name = name)
        }
    }
}