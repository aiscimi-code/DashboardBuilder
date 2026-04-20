package com.dashboard.builder.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppState(
    val appVersion: String = "1.0",
    val tabs: List<Tab> = createDefaultTabs()
) {
    companion object {
        fun createDefaultTabs(): List<Tab> {
            // Tab A - Simple demo (existing)
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
            
            // Tab B - Advanced Workout Dashboard with linked boxes
            val tabB = Tab(
                id = "B",
                name = "Workout",
                backgroundColor = "#1A1A2E",
                boxes = listOf(
                    // ===== 🧭 AT-A-GLANCE STRIP (Row 0-1) =====
                    // Header
                    Box(
                        id = "b_header",
                        type = BoxType.TEXT,
                        label = "",
                        position = Position(0, 0),
                        size = Size(10, 1),
                        config = TextConfig(value = "🎯 WORKOUT DASHBOARD"),
                        style = Style(backgroundColor = "#16213E")
                    ),
                    
                    // Workout Type (Button - click to cycle)
                    Box(
                        id = "b_workout_type",
                        type = BoxType.BUTTON,
                        label = "Type",
                        position = Position(0, 1),
                        size = Size(3, 1),
                        config = ButtonConfig(text = "💪 Strength"),
                        style = Style(backgroundColor = "#E94560"),
                        actions = listOf(
                            Action(
                                event = EventType.ON_CLICK,
                                type = ActionType.SET_TEXT,
                                targetBoxId = "b_type_display",
                                dataSource = DataSource.Static("Strength")
                            )
                        )
                    ),
                    
                    // Duration
                    Box(
                        id = "b_duration",
                        type = BoxType.COUNTER,
                        label = "Minutes",
                        position = Position(3, 1),
                        size = Size(2, 1),
                        config = CounterConfig(value = 45),
                        style = Style(backgroundColor = "#0F3460")
                    ),
                    
                    // Focus
                    Box(
                        id = "b_focus",
                        type = BoxType.TEXT,
                        label = "Focus",
                        position = Position(5, 1),
                        size = Size(5, 1),
                        config = TextConfig(value = "Upper Push Day"),
                        style = Style(backgroundColor = "#0F3460")
                    ),
                    
                    // ===== ❤️ BODY STATUS (Row 2) =====
                    // Sleep
                    Box(
                        id = "b_sleep",
                        type = BoxType.COUNTER,
                        label = "Sleep Hrs",
                        position = Position(0, 2),
                        size = Size(2, 1),
                        config = CounterConfig(value = 7),
                        style = Style(backgroundColor = "#1F4068")
                    ),
                    
                    // Energy (1-5 counter)
                    Box(
                        id = "b_energy",
                        type = BoxType.COUNTER,
                        label = "Energy",
                        position = Position(2, 2),
                        size = Size(2, 1),
                        config = CounterConfig(value = 4),
                        style = Style(backgroundColor = "#1F4068")
                    ),
                    
                    // Readiness Score (calculated display)
                    Box(
                        id = "b_readiness",
                        type = BoxType.TEXT,
                        label = "Readiness",
                        position = Position(4, 2),
                        size = Size(3, 1),
                        config = TextConfig(value = "🟢 85% - GO!"),
                        style = Style(backgroundColor = "#2ECC71")
                    ),
                    
                    // Streak
                    Box(
                        id = "b_streak",
                        type = BoxType.COUNTER,
                        label = "Day Streak",
                        position = Position(7, 2),
                        size = Size(3, 1),
                        config = CounterConfig(value = 12),
                        style = Style(backgroundColor = "#F39C12")
                    ),
                    
                    // ===== 📋 WORKOUT DETAIL (Row 3-6) =====
                    // Exercises Header
                    Box(
                        id = "b_exercises_header",
                        type = BoxType.TEXT,
                        label = "",
                        position = Position(0, 3),
                        size = Size(10, 1),
                        config = TextConfig(value = "🏋️ Today's Exercises"),
                        style = Style(backgroundColor = "#16213E")
                    ),
                    
                    // Exercise 1 - Bench Press
                    Box(
                        id = "b_ex1",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Bench Press",
                        position = Position(0, 4),
                        size = Size(10, 2),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Set 1: 3×10 @ 60kg", checked = false),
                                CheckboxItem(text = "Set 2: 3×10 @ 65kg", checked = false),
                                CheckboxItem(text = "Set 3: 3×10 @ 70kg", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#1F4068")
                    ),
                    
                    // Exercise 2 - Incline Dumbbell
                    Box(
                        id = "b_ex2",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Incline DB",
                        position = Position(0, 6),
                        size = Size(10, 2),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Set 1: 3×12 @ 20kg", checked = false),
                                CheckboxItem(text = "Set 2: 3×12 @ 22kg", checked = false),
                                CheckboxItem(text = "Set 3: 3×12 @ 24kg", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#1F4068")
                    ),
                    
                    // ===== ⏱️ TIMER & CONTROLS (Row 7-8) =====
                    // Rest Timer
                    Box(
                        id = "b_rest_timer",
                        type = BoxType.COUNTER,
                        label = "Rest (sec)",
                        position = Position(0, 8),
                        size = Size(3, 2),
                        config = CounterConfig(value = 90),
                        style = Style(backgroundColor = "#E94560")
                    ),
                    
                    // Workout Progress
                    Box(
                        id = "b_progress",
                        type = BoxType.COUNTER,
                        label = "Sets Done",
                        position = Position(3, 8),
                        size = Size(3, 2),
                        config = CounterConfig(value = 0),
                        style = Style(backgroundColor = "#0F3460")
                    ),
                    
                    // Complete Workout Button
                    Box(
                        id = "b_complete",
                        type = BoxType.BUTTON,
                        label = "",
                        position = Position(6, 8),
                        size = Size(4, 2),
                        config = ButtonConfig(text = "✅ Complete"),
                        style = Style(backgroundColor = "#2ECC71"),
                        actions = listOf(
                            Action(
                                event = EventType.ON_CLICK,
                                type = ActionType.INCREMENT_COUNTER,
                                targetBoxId = "b_workouts_done"
                            )
                        )
                    ),
                    
                    // ===== 📊 QUICK STATS (Row 9) =====
                    // Workouts this week
                    Box(
                        id = "b_workouts_done",
                        type = BoxType.COUNTER,
                        label = "This Week",
                        position = Position(0, 10),
                        size = Size(3, 1),
                        config = CounterConfig(value = 3),
                        style = Style(backgroundColor = "#16213E")
                    ),
                    
                    // Total volume
                    Box(
                        id = "b_volume",
                        type = BoxType.COUNTER,
                        label = "Volume (kg)",
                        position = Position(3, 10),
                        size = Size(4, 1),
                        config = CounterConfig(value = 12500),
                        style = Style(backgroundColor = "#16213E")
                    ),
                    
                    // Notes input
                    Box(
                        id = "b_notes",
                        type = BoxType.INPUT,
                        label = "Notes",
                        position = Position(7, 10),
                        size = Size(3, 2),
                        config = InputConfig(placeholder = "How did it go?"),
                        style = Style(backgroundColor = "#1F4068")
                    )
                )
            )
            
            // Tab C - Meals for the week
            val tabC = Tab(
                id = "C",
                name = "Meals",
                backgroundColor = "#FFF8E1",
                boxes = listOf(
                    // Header - Weekly Menu
                    Box(
                        id = "c_header",
                        type = BoxType.TEXT,
                        label = "",
                        position = Position(0, 0),
                        size = Size(10, 1),
                        config = TextConfig(value = "🍽️ Weekly Meal Plan"),
                        style = Style(backgroundColor = "#FF8F00")
                    ),
                    
                    // Day - Monday
                    Box(
                        id = "c_mon",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Monday",
                        position = Position(0, 1),
                        size = Size(5, 3),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Breakfast: Oatmeal & Berries", checked = true),
                                CheckboxItem(text = "Lunch: Chicken Salad", checked = false),
                                CheckboxItem(text = "Dinner: Salmon & Veggies", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#FFFFFF")
                    ),
                    
                    // Day - Tuesday
                    Box(
                        id = "c_tue",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Tuesday",
                        position = Position(5, 1),
                        size = Size(5, 3),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Breakfast: Greek Yogurt", checked = false),
                                CheckboxItem(text = "Lunch: Quinoa Bowl", checked = false),
                                CheckboxItem(text = "Dinner: Pasta Primavera", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#FFFFFF")
                    ),
                    
                    // Day - Wednesday
                    Box(
                        id = "c_wed",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Wednesday",
                        position = Position(0, 4),
                        size = Size(5, 3),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Breakfast: Eggs & Toast", checked = false),
                                CheckboxItem(text = "Lunch: Turkey Wrap", checked = false),
                                CheckboxItem(text = "Dinner: Stir Fry Tofu", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#FFFFFF")
                    ),
                    
                    // Day - Thursday
                    Box(
                        id = "c_thu",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Thursday",
                        position = Position(5, 4),
                        size = Size(5, 3),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Breakfast: Smoothie Bowl", checked = false),
                                CheckboxItem(text = "Lunch: Lentil Soup", checked = false),
                                CheckboxItem(text = "Dinner: Grilled Fish Tacos", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#FFFFFF")
                    ),
                    
                    // Day - Friday
                    Box(
                        id = "c_fri",
                        type = BoxType.CHECKBOX_LIST,
                        label = "Friday",
                        position = Position(0, 7),
                        size = Size(5, 3),
                        config = CheckboxListConfig(
                            items = listOf(
                                CheckboxItem(text = "Breakfast: Avocado Toast", checked = false),
                                CheckboxItem(text = "Lunch: Caesar Salad", checked = false),
                                CheckboxItem(text = "Dinner: Pizza Night!", checked = false)
                            )
                        ),
                        style = Style(backgroundColor = "#FFFFFF")
                    ),
                    
                    // Weekend Header
                    Box(
                        id = "c_weekend",
                        type = BoxType.TEXT,
                        label = "",
                        position = Position(5, 7),
                        size = Size(5, 1),
                        config = TextConfig(value = "🎉 Weekend"),
                        style = Style(backgroundColor = "#FF8F00")
                    ),
                    
                    // Shopping List Summary
                    Box(
                        id = "c_shopping",
                        type = BoxType.TEXT,
                        label = "Shopping List",
                        position = Position(0, 10),
                        size = Size(7, 2),
                        config = TextConfig(value = "🥗 Need: Chicken, Salmon, Eggs, Quinoa, Avocado, Berries, Greek Yogurt, Tofu"),
                        style = Style(backgroundColor = "#E8F5E9")
                    ),
                    
                    // Quick Add Button
                    Box(
                        id = "c_add",
                        type = BoxType.BUTTON,
                        label = "",
                        position = Position(7, 10),
                        size = Size(3, 2),
                        config = ButtonConfig(text = "+ Add Meal"),
                        style = Style(backgroundColor = "#4CAF50")
                    )
                )
            )
            
            // Other tabs are empty
            val otherTabs = ('D'..'J').map { char ->
                Tab(
                    id = char.toString(),
                    name = "Tab $char"
                )
            }
            
            return listOf(tabA, tabB, tabC) + otherTabs
        }
    }
}