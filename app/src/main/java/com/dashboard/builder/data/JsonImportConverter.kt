package com.dashboard.builder.data

import com.dashboard.builder.data.model.*
import kotlinx.serialization.json.*

/**
 * Converts arbitrary JSON into an [AppState] the dashboard can display.
 *
 * Strategy
 * --------
 * 1. First try to parse the JSON as a native [AppState] (our own export format).
 *    If that works, return it directly.
 * 2. If not, try to parse as a single [Tab] (single-tab export format).
 * 3. Otherwise treat the file as external/domain JSON and build an [AppState]
 *    from it heuristically:
 *    - The root object's top-level keys each become a Tab.
 *    - Inside each tab, the content is flattened into [Box] widgets:
 *        • Scalar string/number values  → TEXT box  (label = key, value = value)
 *        • Boolean values               → CHECKBOX_LIST box with one item
 *        • Arrays of objects            → CHECKBOX_LIST (one item per element,
 *                                         rendered as a readable summary line)
 *        • Nested objects               → TEXT box showing a compact summary
 *
 * This lets users import any structured JSON (hotel schedules, task lists, etc.)
 * and immediately see the data laid out on the grid.
 */
object JsonImportConverter {

    private val lenientJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    /**
     * Parse [jsonText] into an [AppState].
     * Returns a [Result] wrapping the state, or a failure with a descriptive message.
     */
    fun parse(jsonText: String): Result<AppState> {
        val trimmed = jsonText.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("File is empty"))

        // ── 1. Try native AppState ────────────────────────────────────────────
        try {
            val appState = lenientJson.decodeFromString<AppState>(trimmed)
            // A successful parse of AppState from *our* format will have the
            // "appVersion" key present in the JSON. Reject silent coercions of
            // foreign JSON that happen to deserialise due to ignoreUnknownKeys.
            val root = Json.parseToJsonElement(trimmed).jsonObject
            if (root.containsKey("appVersion") || root.containsKey("tabs")) {
                return Result.success(appState)
            }
        } catch (_: Exception) { /* fall through */ }

        // ── 2. Try native Tab ────────────────────────────────────────────────
        try {
            val root = Json.parseToJsonElement(trimmed).jsonObject
            if (root.containsKey("id") && root.containsKey("boxes")) {
                val tab = lenientJson.decodeFromString<Tab>(trimmed)
                val appState = AppState(tabs = listOf(tab))
                return Result.success(appState)
            }
        } catch (_: Exception) { /* fall through */ }

        // ── 3. Convert arbitrary JSON ─────────────────────────────────────────
        return try {
            val root = Json.parseToJsonElement(trimmed).jsonObject
            val tabs = buildTabsFromObject(root)
            if (tabs.isEmpty()) {
                Result.failure(IllegalArgumentException("No usable data found in JSON"))
            } else {
                Result.success(AppState(appVersion = "imported", tabs = tabs))
            }
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Cannot parse JSON: ${e.message}"))
        }
    }

    // ── Tab builder ───────────────────────────────────────────────────────────

    private fun buildTabsFromObject(root: JsonObject): List<Tab> {
        val tabs = mutableListOf<Tab>()
        val tabIds = ('A'..'J').map { it.toString() }
        var tabIndex = 0

        root.entries.forEachIndexed { _, (key, value) ->
            if (tabIndex >= tabIds.size) return@forEachIndexed
            val tabId = tabIds[tabIndex++]
            val tabName = key.replace('_', ' ').split(' ')
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                .take(20)

            val boxes = buildBoxesFromValue(key, value)
            if (boxes.isNotEmpty()) {
                tabs.add(Tab(id = tabId, name = tabName, boxes = boxes))
            }
        }
        return tabs
    }

    // ── Box builder ───────────────────────────────────────────────────────────

    private fun buildBoxesFromValue(sectionKey: String, sectionValue: JsonElement): List<Box> {
        val boxes = mutableListOf<Box>()
        var row = 0
        var boxIndex = 1

        val nextId: (String) -> String = { type -> "${type}_imp_${boxIndex++}" }

        // Header box for the section
        boxes.add(
            Box(
                id = nextId("text"),
                type = BoxType.TEXT,
                label = "",
                position = Position(0, row++),
                size = Size(10, 1),
                config = TextConfig(value = "📋 ${sectionKey.replace('_', ' ').uppercase()}"),
                style = Style(backgroundColor = "#1565C0")
            )
        )

        when (sectionValue) {
            is JsonObject -> {
                sectionValue.entries.forEach { (key, value) ->
                    val boxRow = buildFieldBox(nextId, key, value, row)
                    if (boxRow != null) {
                        boxes.add(boxRow.first)
                        row += boxRow.second
                    }
                }
            }
            is JsonArray -> {
                val checkboxItems = sectionValue.mapIndexedNotNull { _, el ->
                    val summary = summariseElement(el)
                    if (summary.isNotBlank()) CheckboxItem(text = summary, checked = false) else null
                }
                if (checkboxItems.isNotEmpty()) {
                    val height = (checkboxItems.size + 1).coerceAtMost(6)
                    boxes.add(
                        Box(
                            id = nextId("check"),
                            type = BoxType.CHECKBOX_LIST,
                            label = sectionKey.replace('_', ' '),
                            position = Position(0, row),
                            size = Size(10, height),
                            config = CheckboxListConfig(items = checkboxItems),
                            style = Style(backgroundColor = "#FFFFFF")
                        )
                    )
                    row += height
                }
            }
            else -> {
                val text = sectionValue.toString().trim('"')
                boxes.add(
                    Box(
                        id = nextId("text"),
                        type = BoxType.TEXT,
                        label = sectionKey,
                        position = Position(0, row++),
                        size = Size(10, 1),
                        config = TextConfig(value = text),
                        style = Style(backgroundColor = "#FFFFFF")
                    )
                )
            }
        }

        return boxes
    }

    /**
     * Turn a single key-value field into a Box. Returns (box, rowsConsumed) or null.
     */
    private fun buildFieldBox(
        nextId: (String) -> String,
        key: String,
        value: JsonElement,
        startRow: Int
    ): Pair<Box, Int>? {
        val label = key.replace('_', ' ')
            .split(' ').joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        return when {
            // Boolean → single-item checkbox
            value is JsonPrimitive && value.booleanOrNull != null -> {
                val item = CheckboxItem(text = label, checked = value.boolean)
                val box = Box(
                    id = nextId("check"),
                    type = BoxType.CHECKBOX_LIST,
                    label = "",
                    position = Position(0, startRow),
                    size = Size(10, 1),
                    config = CheckboxListConfig(items = listOf(item)),
                    style = Style(backgroundColor = "#F5F5F5")
                )
                Pair(box, 1)
            }

            // Number → counter
            value is JsonPrimitive && value.intOrNull != null -> {
                val box = Box(
                    id = nextId("counter"),
                    type = BoxType.COUNTER,
                    label = label,
                    position = Position(0, startRow),
                    size = Size(5, 1),
                    config = CounterConfig(value = value.int),
                    style = Style(backgroundColor = "#E3F2FD")
                )
                Pair(box, 1)
            }

            // String scalar → text box
            value is JsonPrimitive -> {
                val text = value.content
                if (text.isBlank() || text == "null") return null
                val box = Box(
                    id = nextId("text"),
                    type = BoxType.TEXT,
                    label = label,
                    position = Position(0, startRow),
                    size = Size(10, 1),
                    config = TextConfig(value = "$label: $text"),
                    style = Style(backgroundColor = "#FFFFFF")
                )
                Pair(box, 1)
            }

            // Array of objects → checkbox list (one item per element)
            value is JsonArray && value.isNotEmpty() -> {
                val items = value.mapNotNull { el ->
                    val summary = summariseElement(el)
                    if (summary.isNotBlank()) CheckboxItem(text = summary, checked = false) else null
                }
                if (items.isEmpty()) return null
                val height = (items.size + 1).coerceIn(2, 8)
                val box = Box(
                    id = nextId("check"),
                    type = BoxType.CHECKBOX_LIST,
                    label = label,
                    position = Position(0, startRow),
                    size = Size(10, height),
                    config = CheckboxListConfig(items = items),
                    style = Style(backgroundColor = "#FFFFFF")
                )
                Pair(box, height)
            }

            // Nested object → compact text summary
            value is JsonObject -> {
                val summary = value.entries.take(4).joinToString("  ·  ") { (k, v) ->
                    "$k: ${v.toString().trim('"').take(30)}"
                }
                if (summary.isBlank()) return null
                val box = Box(
                    id = nextId("text"),
                    type = BoxType.TEXT,
                    label = label,
                    position = Position(0, startRow),
                    size = Size(10, 1),
                    config = TextConfig(value = summary),
                    style = Style(backgroundColor = "#F9F9F9")
                )
                Pair(box, 1)
            }

            else -> null
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Produce a short one-line summary of any JsonElement for use in a CheckboxItem. */
    private fun summariseElement(el: JsonElement): String {
        return when (el) {
            is JsonPrimitive -> el.content
            is JsonObject -> {
                // Pick the most descriptive fields available
                val picks = listOf("name", "room_number", "request", "text", "title",
                    "description", "label", "id", "type", "status")
                val found = picks.firstNotNullOfOrNull { k ->
                    el[k]?.let { v -> if (v is JsonPrimitive) v.content.takeIf { it.isNotBlank() } else null }
                }
                if (found != null) {
                    // Append a second useful field if available
                    val extra = listOf("status", "priority", "type", "role").firstNotNullOfOrNull { k ->
                        el[k]?.let { v ->
                            if (v is JsonPrimitive && v.content != found) v.content.takeIf { it.isNotBlank() }
                            else null
                        }
                    }
                    if (extra != null) "$found [$extra]" else found
                } else {
                    el.entries.take(2).joinToString(", ") { (k, v) ->
                        "$k: ${v.toString().trim('"').take(25)}"
                    }
                }
            }
            is JsonArray -> el.take(3).joinToString(", ") { summariseElement(it) }
            else -> ""
        }
    }
}
