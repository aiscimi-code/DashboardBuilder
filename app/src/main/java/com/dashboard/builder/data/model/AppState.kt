package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppState(
    val appVersion: String = "1.0",
    val tabs: List<Tab> = createDefaultTabs()
) {
    companion object {
        fun createDefaultTabs(): List<Tab> {
            // Tab A with sample personal trainer dashboard
            val tabA = Tab(
                id = "A",
                name = "Training",
                backgroundColor = "#F5F5F5",
                boxes = listOf(
                    // Header - Today's Stats
                    Box(
                        id = "text_1",
                        type = BoxType.TEXT,
                        label = "",
                        position = Position(0, 0),
                        size = Size(10, 1),
                        config = TextConfig(value = "🏋️ Today's Training"),
                        style = Style(backgroundColor = "#1565C0")
                    ),
                    // Counter - Workout Count
                    Box(
                        id = "counter_1",
                        type = BoxType.COUNTER,
                        label = "Workouts",
                        position = Position(0, 1),
                        size = Size(3, 2),
                        config = CounterConfig(value = 3),
                        style = Style(backgroundColor = "#E3F2FD")
                    ),
                    // Counter - Calories
                    Box(
                        id = "counter_2",
                        type = BoxType.COUNTER,
                        label = "Calories",
                        position = Position(3, 1),
                        size = Size(3, 2),
                        config = CounterConfig(value = 450),
                        style = Style(backgroundColor = "#FFF3E0")
                    ),
                    // Counter - Steps
                    Box(
                        id = "counter_3",
                        type = BoxType.COUNTER,
                        label = "Steps",
                        position = Position(6, 1),
                        size = Size(4, 2),
                        config = CounterConfig(value = 8450),
                        style = Style(backgroundColor = "#E8F5E9")
                    ),
                    // Today's Workout Plan
                    Box(
                        id = "text_2",
                        type = BoxType.TEXT,
                        label = "",
                        position = Position(0, 3),
                        size = Size(10, 1),
                        config = TextConfig(value = "📋 Today's Plan"),
                        style = Style(backgroundColor = "#1565C0")
                    ),
                    // Checkbox List - Exercises
                    Box(
                        id = "check_1",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Exercises",
                        position = Position(0, 4),
                        size = Size(5, 4),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Warm-up (5 min)", checked = true),
                                CheckboxItem(text = "Squats 3x12", checked = true),
                                CheckboxItem(text = "Bench Press 3x10", checked = false),
                                CheckboxItem(text = "Deadlifts 3x8", checked = false),
                                CheckboxItem(text = "Cool down (5 min)", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#FFFFFF")
                    ),
                    // Button - Start Workout
                    Box(
                        id = "button_1",
                        type = BoxType.BUTTON,
                        label = "",
                        position = Position(5, 4),
                        size = Size(5, 2),
                        config = ButtonConfig(text = "▶ Start Workout"),
                        style = Style(backgroundColor = "#4CAF50")
                    ),
                    // Button - View Progress
                    Box(
                        id = "button_2",
                        type = BoxType.BUTTON,
                        label = "",
                        position = Position(5, 6),
                        size = Size(5, 2),
                        config = ButtonConfig(text = "📊 View Progress"),
                        style = Style(backgroundColor = "#2196F3")
                    ),
                    // Input - Quick Note
                    Box(
                        id = "input_1",
                        type = BoxType.INPUT,
                        label = "Notes",
                        position = Position(0, 8),
                        size = Size(10, 2),
                        config = InputConfig(placeholder = "Add workout notes..."),
                        style = Style(backgroundColor = "#FFFFFF")
                    )
                )
            )
            
            // Other tabs are empty
            val otherTabs = ('B'..'J').map { char ->
                Tab(
                    id = char.toString(),
                    name = "Tab $char"
                )
            }
            
            return listOf(tabA) + otherTabs
        }
    }
}