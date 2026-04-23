package com.dashboard.builder.tools

import com.dashboard.builder.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Converter that takes a hotel layout JSON (as provided by the user) and creates a Dashboard Builder
 * AppState JSON with one index tab (A) and one tab per room. Each room tab contains details and a button to go back
 * to the index.
 * 
 * Usage (from the project root):
 *   ./gradlew run --args="/path/to/hotel.json"
 * The output will be written to "hotel_dashboard.json" in the project root.
 */

/**
 * Convert pre-parsed HotelData to AppState.
 * Called from MainViewModel when importing hotel JSON.
 */
fun convertHotelToAppState(hotel: HotelData): AppState {
    val tabs = mutableListOf<Tab>()

    // Index tab (A) - list all rooms
    val indexBoxes = mutableListOf<Box>()
    var yPos = 0
    hotel.tab_a_rooms.rooms.forEachIndexed { idx, room ->
        val btnId = "roomBtn_${room.room_number}"
        val buttonBox = Box(
            id = btnId,
            type = BoxType.BUTTON,
            label = "",
            position = Position(0, yPos),
            size = Size(10, 1),
            config = ButtonConfig(text = "Room ${room.room_number}"),
            style = Style(backgroundColor = "#4CAF50"),
            actions = listOf(
                Action(
                    event = EventType.ON_CLICK,
                    type = ActionType.SWITCH_TAB,
                    targetBoxId = "",
                    dataSource = DataSource.Static(room.room_number)
                )
            )
        )
        indexBoxes.add(buttonBox)
        yPos += 1
    }
    
    // Add summary section to index tab
    indexBoxes.add(
        Box(
            id = "summary_header",
            type = BoxType.TEXT,
            label = "",
            position = Position(0, yPos),
            size = Size(10, 1),
            config = TextConfig(value = "📊 Summary"),
            style = Style(backgroundColor = "#1565C0")
        )
    )
    yPos += 1
    indexBoxes.add(
        Box(
            id = "summary_total",
            type = BoxType.TEXT,
            label = "",
            position = Position(0, yPos),
            size = Size(5, 1),
            config = TextConfig(value = "Total: ${hotel.summary.total_rooms}"),
            style = Style(backgroundColor = "#FFFFFF")
        )
    )
    indexBoxes.add(
        Box(
            id = "summary_urgent",
            type = BoxType.TEXT,
            label = "",
            position = Position(5, yPos),
            size = Size(5, 1),
            config = TextConfig(value = "Urgent: ${hotel.summary.urgent_priority}"),
            style = Style(backgroundColor = "#FFEBEE")
        )
    )
    yPos += 1
    indexBoxes.add(
        Box(
            id = "summary_vip",
            type = BoxType.TEXT,
            label = "",
            position = Position(0, yPos),
            size = Size(10, 1),
            config = TextConfig(value = "VIP Rooms: ${hotel.summary.VIP_rooms}"),
            style = Style(backgroundColor = "#FFF8E1")
        )
    )

    val indexTab = Tab(
        id = "A",
        name = "Index",
        backgroundColor = "#F5F5F5",
        boxes = indexBoxes
    )
    tabs.add(indexTab)

    // Room tabs
    hotel.tab_a_rooms.rooms.forEach { room ->
        val boxes = mutableListOf<Box>()
        var row = 0
        // Header
        boxes.add(
            Box(
                id = "header_${room.room_number}",
                type = BoxType.TEXT,
                label = "",
                position = Position(0, row),
                size = Size(10, 1),
                config = TextConfig(value = "Room ${room.room_number}"),
                style = Style(backgroundColor = "#1565C0")
            )
        )
        row += 1
        // Details (one per line)
        fun addDetail(label: String, value: String) {
            boxes.add(
                Box(
                    id = "${label.replace(" ", "_")}_${room.room_number}",
                    type = BoxType.TEXT,
                    label = "",
                    position = Position(0, row),
                    size = Size(10, 1),
                    config = TextConfig(value = "$label: $value"),
                    style = Style(backgroundColor = "#FFFFFF")
                )
            )
            row += 1
        }
        addDetail("Status", room.status)
        addDetail("Type", room.room_type)
        addDetail("Priority", room.priority)
        addDetail("Est. Minutes", room.estimated_minutes.toString())
        if (room.notes.isNotBlank()) addDetail("Notes", room.notes)
        
        // Check for special requests for this room
        val specialRoom = hotel.tab_b_special_requests.rooms[room.room_number]
        if (specialRoom != null) {
            if (specialRoom.guest_name.isNotBlank()) {
                addDetail("Guest", specialRoom.guest_name)
            }
            if (specialRoom.VIP) {
                addDetail("VIP", specialRoom.VIP_level ?: "Yes")
            }
            if (specialRoom.guest_requests.isNotEmpty()) {
                addDetail("Requests", specialRoom.guest_requests.size.toString())
            }
        }
        
        // Back button
        boxes.add(
            Box(
                id = "back_${room.room_number}",
                type = BoxType.BUTTON,
                label = "",
                position = Position(0, row),
                size = Size(10, 1),
                config = ButtonConfig(text = "⬆ Back to Index"),
                style = Style(backgroundColor = "#2196F3"),
                actions = listOf(
                    Action(
                        event = EventType.ON_CLICK,
                        type = ActionType.SWITCH_TAB,
                        targetBoxId = "",
                        dataSource = DataSource.Static("A")
                    )
                )
            )
        )
        val roomTab = Tab(
            id = room.room_number,
            name = "Room ${room.room_number}",
            backgroundColor = "#FFFFFF",
            boxes = boxes
        )
        tabs.add(roomTab)
    }

    // Build final AppState
    return AppState(appVersion = "1.0", tabs = tabs)
}

@Serializable
data class HotelData(
    val dashboard: Dashboard,
    val tab_a_rooms: TabARooms,
    val tab_b_special_requests: TabBSpecialRequests,
    val summary: Summary
) {
    @Serializable data class Dashboard(val meta: Meta, val employee: Employee, val date: String)
    @Serializable data class Meta(val generated_at: String, val hotel_name: String, val shift: String, val manager: String)
    @Serializable data class Employee(val id: String, val name: String, val role: String, val assigned_floors: List<Int>)
    @Serializable data class TabARooms(val description: String, val total_rooms: Int, val rooms: List<Room>)
    @Serializable data class Room(
        val order: Int,
        val room_number: String,
        val room_type: String,
        val status: String,
        val checkout_time: String? = null,
        val checkin_time: String? = null,
        val priority: String,
        val estimated_minutes: Int,
        val notes: String,
        val room_link: String
    )
    @Serializable data class TabBSpecialRequests(val description: String, val rooms: Map<String, SpecialRoom>)
    @Serializable data class SpecialRoom(
        val room_number: String,
        val guest_name: String,
        val guest_requests: List<GuestRequest>,
        val housekeeping_notes: List<String>,
        val VIP: Boolean,
        val VIP_level: String?
    )
    @Serializable data class GuestRequest(val type: String, val request: String, val priority: String, val completed: Boolean)
    @Serializable data class Summary(val total_rooms: Int, val checkout_rooms: Int, val stayover_rooms: Int, val urgent_priority: Int, val high_priority: Int, val estimated_total_minutes: Int, val estimated_total_hours: Double, val VIP_rooms: Int)
}
