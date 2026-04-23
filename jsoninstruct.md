# How to Create Dashboards in Dashboard Builder

This guide explains how to create any type of dashboard using the Dashboard Builder Android app. Whether you're building a personal trainer dashboard, hotel management system, inventory tracker, or productivity tool — this document walks you through the process.

---

## Table of Contents

1. [Understanding the Grid System](#1-understanding-the-grid-system)
2. [Creating Your First Dashboard](#2-creating-your-first-dashboard)
3. [Box Types Explained](#3-box-types-explained)
4. [Box-to-Box Actions](#4-box-to-box-actions)
5. [Working with Tabs](#5-working-with-tabs)
6. [Saving, Exporting, and Restoring](#6-saving-exporting-and-restoring)
7. [Importing External JSON](#7-importing-external-json)
8. [Real-World Examples](#8-real-world-examples)

---

## 1. Understanding the Grid System

The app uses a **10-column by 32-row grid**. Each box occupies a specific number of columns (width) and rows (height).

- **Columns**: 10 (0-9)
- **Rows**: 32 (0-31)
- **Box positioning**: Boxes snap to grid cells
- **Collision detection**: Boxes cannot overlap; the app prevents placing boxes in occupied spaces

### Grid Coordinates
- Position (0, 0) = top-left corner
- Position (x, y) where x = column, y = row
- Size (w, h) where w = columns wide, h = rows tall

---

## 2. Creating Your First Dashboard

### Step 1: Launch the App
When you first open the app, you'll see the default Training Dashboard with sample boxes.

### Step 2: Add a New Box
1. Tap the **+ (Add)** button in the bottom menu
2. Select a box type from the list (TEXT, INPUT, BUTTON, CHECKBOX_LIST, COUNTER)
3. The box appears in the first available position

### Step 3: Edit a Box
1. **Single tap** on a box to select it (shows selection border)
2. Tap the **Edit** button in the bottom menu, OR
3. **Double-tap** the box to open the edit dialog

### Step 4: Move a Box
1. Tap the **Move** button in the bottom menu
2. Tap and drag the box to a new position
3. Boxes snap to the grid; invalid positions are blocked

### Step 5: Delete a Box
1. Tap to select a box
2. Tap the **Delete** button in the bottom menu

---

## 3. Box Types Explained

### TEXT
- **Purpose**: Display read-only information
- **Use for**: Headers, labels, instructions, static data
- **Default size**: 10 columns × 2 rows
- **Configurable**: Background color, text content

**Example use case**: Display today's date, a welcome message, or section headers.

### INPUT
- **Purpose**: Collect user text input
- **Use for**: Notes fields, search boxes, data entry
- **Default size**: 10 columns × 2 rows
- **Configurable**: Placeholder text, background color

**Example use case**: Workout notes, daily journal entry, quick capture field.

### BUTTON
- **Purpose**: Trigger actions when tapped
- **Use for**: Navigation, executing functions, starting timers
- **Default size**: 1 column × 1 row (can resize)
- **Configurable**: Button text, background color, actions

**Example use case**: "Start Workout", "Complete Task", "Next Tab".

### CHECKBOX_LIST
- **Purpose**: Display and manage a list of checkable items
- **Use for**: Todo lists, exercise sets, shopping lists
- **Default size**: 10 columns × 2 rows (adjustable)
- **Configurable**: List items (text + checked state), background color

**Example use case**: Workout exercise list, morning routine checklist, grocery items.

### COUNTER
- **Purpose**: Display and adjust numeric values
- **Use for**: Scores, quantities, step counts, repetitions
- **Default size**: 10 columns × 2 rows
- **Configurable**: Label, initial value, background color

**Example use case**: Workout count, calories burned, water glasses drank.

---

## 4. Box-to-Box Actions

Boxes can trigger actions in other boxes when clicked or when their content changes. This enables interactive dashboards.

### Available Events
- **ON_CLICK**: Triggered when a button is tapped
- **ON_TEXT_CHANGE**: Triggered when text input changes

### Available Actions
- **SET_TEXT**: Set the text value of another box
- **ADD_TO_LIST**: Add an item to a checkbox list
- **ADD_TO_CHECKBOX_LIST**: Add an item to a checkbox list
- **INCREMENT_COUNTER**: Increase a counter by 1
- **DECREMENT_COUNTER**: Decrease a counter by 1
- **SWITCH_TAB**: Navigate to a different tab

### Data Sources
- **Static**: A fixed value you specify
- **From Box**: Copy value from another box

### How to Configure Actions
1. Select the box that will trigger the action (e.g., a button)
2. Open Edit dialog
3. Scroll to "Actions" section
4. Tap "Add Action"
5. Select: Event (ON_CLICK), Action type, Target box, Data source

---

## 5. Working with Tabs

The app supports up to 10 tabs (A through J), each containing its own independent dashboard.

### Creating Tab Navigation
1. Add a **BUTTON** box on the source tab
2. Configure an action:
   - Event: **ON_CLICK**
   - Action: **SWITCH_TAB**
   - Data Source: **Static** → Enter tab ID (e.g., "B")
3. Add a return button on the target tab that switches back

### Tab Design Tips
- **Tab A (Index)**: Use as a menu/navigation hub listing all sections
- **Tab B (Details)**: First content tab
- **Additional tabs**: Specific views (progress, settings, etc.)

---

## 6. Saving, Exporting, and Restoring

### Auto-Save
The app automatically saves your dashboard to internal storage when:
- App goes to background (onStop)
- You explicitly trigger a save

Three rotating backup files are maintained (backup_0.json, backup_1.json, backup_2.json).

### Manual Export
1. Tap the **Export** button in bottom menu
2. Choose "All tabs" or "Current tab"
3. Select save location via system file picker
4. Dashboard saved as JSON file

### Import
1. Tap the **Export** button (same button handles import)
2. Choose "Import" option
3. Select a JSON file to import

### Restore from Backup
1. Tap the **Export** button
2. Select "Restore Backup"
3. Choose which backup to restore

---

## 7. Importing External JSON

The app can import **any structured JSON file** and automatically convert it to dashboard boxes.

### How It Works
1. JSON top-level keys become tabs
2. Each field becomes a box:
   - Strings → TEXT boxes
   - Numbers → COUNTER boxes (if single) or TEXT (in objects)
   - Booleans → CHECKBOX_LIST with one item
   - Arrays → CHECKBOX_LIST (primitives) or summarized list (objects)

### Example: Hotel JSON
```json
{
  "dashboard": { "hotel_name": "Grand Hotel", "shift": "Morning" },
  "rooms": [
    { "room_number": "101", "status": "Clean", "priority": "High" },
    { "room_number": "102", "status": "Occupied", "priority": "Normal" }
  ],
  "summary": { "total_rooms": 50, "urgent": 3 }
}
```

This automatically creates:
- Tab "dashboard" with hotel info
- Tab "rooms" with room list
- Tab "summary" with counters

---

## 8. Real-World Examples

### Example 1: Personal Workout Tracker

**Scenario**: Create a dashboard to track daily workouts.

**Tab A - Today's Workout**
- Header: "🏋️ Today's Training" (TEXT)
- Workout Type: Button that cycles through "Strength", "Cardio", "Mobility" (BUTTON with SET_TEXT action)
- Exercise 1: "Bench Press 3x10" (CHECKBOX_LIST)
- Exercise 2: "Squats 3x12" (CHECKBOX_LIST)
- Start Workout: Button (BUTTON) → starts workout
- Notes: Input field (INPUT)

**Tab B - Progress**
- Header: "📊 Weekly Progress" (TEXT)
- Workouts This Week: Counter (COUNTER)
- Total Volume: Counter (COUNTER)
- Calories Burned: Counter (COUNTER)
- Streak: Counter showing days (COUNTER)

**Tab Navigation**
- Add button "View Progress" on Tab A → switches to Tab B
- Add button "Back to Workout" on Tab B → switches to Tab A

---

### Example 2: Hotel Room Management

**Scenario**: Housekeeping team managing room statuses.

**Tab A - Room List**
- Header: "🏨 Today's Rooms" (TEXT)
- Room buttons: One per room (BUTTON), each switches to room detail tab

**Tab 101, 102, 103, etc. - Individual Rooms**
- Header: "Room 101" (TEXT)
- Status: TEXT showing "Clean/Dirty/Occupied"
- Priority: TEXT showing priority level
- Guest Name: TEXT
- Check-out Time: TEXT
- Notes: INPUT field
- "Mark Clean" button (BUTTON) → could increment counter
- "Back to List" button → switches back to Tab A

---

### Example 3: Daily Routine Planner

**Scenario**: Morning routine checklist with timers.

**Tab A - Morning Routine**
- Header: "☀️ Morning Routine" (TEXT)
- Current Time: TEXT (could show time)
- Tasks:
  - Wake up: CHECKBOX_LIST with items: "Drink water", "Stretch 5min", "Meditate"
  - Breakfast: CHECKBOX_LIST with items: "Eat breakfast", "Take vitamins"
- Notes: INPUT for any additions

**Tab B - Evening Routine**
- Header: "🌙 Evening Routine" (TEXT)
- Tasks: CHECKBOX_LIST with evening tasks
- Tomorrow's Priorities: INPUT field

---

### Example 4: Inventory Tracker

**Scenario**: Track stock levels for a small business.

**Tab A - Inventory Overview**
- Header: "📦 Inventory Status" (TEXT)
- Total Items: COUNTER
- Low Stock Alerts: TEXT listing items below threshold
- Categories: Button links to category tabs

**Tab B - Electronics**
- Header: "🔌 Electronics" (TEXT)
- Item list: CHECKBOX_LIST with items like "Laptops: 5", "Tablets: 3"
- Add Stock: INPUT + BUTTON to add inventory
- Remove Stock: INPUT + BUTTON to remove

**Tab C - Office Supplies**
- Header: "📎 Office Supplies" (TEXT)
- Similar structure to Electronics tab

---

### Example 5: Recipe & Meal Planner

**Scenario**: Plan weekly meals and shopping.

**Tab A - This Week's Menu**
- Header: "🍽️ This Week's Menu" (TEXT)
- Monday: CHECKBOX_LIST with breakfast/lunch/dinner
- Tuesday: CHECKBOX_LIST
- ...through Sunday

**Tab B - Shopping List**
- Header: "🛒 Shopping List" (TEXT)
- Produce: CHECKBOX_LIST
- Meat: CHECKBOX_LIST
- Dairy: CHECKBOX_LIST
- Pantry: CHECKBOX_LIST
- Add Item: INPUT + BUTTON

---

### Example 6: Project Task Manager

**Scenario**: Track tasks across multiple projects.

**Tab A - All Tasks**
- Header: "📋 Project Tasks" (TEXT)
- Filter buttons: "All", "Today", "This Week"
- Task list: CHECKBOX_LIST with format "Project - Task - Due Date"
- Add Task: INPUT + BUTTON

**Tab B - Project Alpha**
- Header: "🔵 Project Alpha" (TEXT)
- To Do: CHECKBOX_LIST
- In Progress: CHECKBOX_LIST
- Done: CHECKBOX_LIST

**Tab C - Project Beta**
- Header: "🟢 Project Beta" (TEXT)
- Same structure as Alpha

---

### Example 7: Study Session Tracker

**Scenario**: Track study sessions and subjects.

**Tab A - Study Dashboard**
- Header: "📚 Study Session" (TEXT)
- Session Timer: COUNTER (minutes studied)
- Today's Sessions: COUNTER
- Subject: BUTTON cycles through subjects
- "Start Session" button (BUTTON) → starts timer
- "End Session" button (BUTTON) → stops and records

**Tab B - Progress**
- Header: "📈 Study Progress" (TEXT)
- Total Hours This Week: COUNTER
- Total Hours This Month: COUNTER
- Subject Breakdown: CHECKBOX_LIST with hours per subject

---

### Example 8: Import from External API (JSON)

**Scenario**: You have data from an external system (hotel PMS, CRM, etc.) and want to visualize it.

**Process**:
1. Export data as JSON from your external system
2. In Dashboard Builder, tap Export → Import
3. Select the JSON file
4. App automatically creates tabs for each top-level key

**Example JSON**:
```json
{
  "sales_dashboard": {
    "today_revenue": 1500,
    "orders": 45,
    "top_product": "Widget A"
  },
  "products": [
    { "name": "Widget A", "stock": 100, "price": 9.99 },
    { "name": "Widget B", "stock": 50, "price": 14.99 }
  ],
  "alerts": ["Low stock: Widget C", "Review needed: Order #1234"]
}
```

**Result**: Creates three tabs:
- "sales_dashboard" with revenue as counter, orders as counter
- "products" as checkbox list
- "alerts" as checkbox list

---

## Tips & Best Practices

1. **Start Simple**: Begin with one tab, add complexity as needed
2. **Use Headers**: Always add a TEXT box as header for each section
3. **Group Related Content**: Use separate tabs for different views
4. **Navigation**: Always provide a way to get back (return buttons)
5. **Color Coding**: Use background colors to visually organize (e.g., blue for headers, green for success states)
6. **Backup Regularly**: Export important dashboards to files
7. **Test Actions**: After configuring box-to-box actions, test them thoroughly
8. **Use Undo**: If something goes wrong, hit undo (stores last 10 states)

---

## Troubleshooting

### Box Won't Move
- Ensure target position has enough space
- Check that box isn't locked (in Edit dialog)

### Action Not Working
- Verify target box exists and isn't deleted
- Check that event type matches (ON_CLICK for buttons)

### Import Failed
- Ensure JSON is valid
- Try exporting a working dashboard to see the expected format

### App Crashed on Load
- Try restoring from a backup
- Re-install may help if data is corrupted

---

*Last updated: April 2026*
*Dashboard Builder v1.1.0*