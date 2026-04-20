package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppState(
    val appVersion: String = "1.0",
    val tabs: List<Tab> = createDefaultTabs()
) {
    companion object {
        fun createDefaultTabs(): List<Tab> {
            return ('A'..'J').map { char ->
                Tab(
                    id = char.toString(),
                    name = "Tab $char"
                )
            }
        }
    }
}