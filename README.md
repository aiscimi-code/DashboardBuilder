# Dashboard Builder

A native Android application for building customizable dashboards with a drag-and-drop grid system. Designed for creating personal trainer, productivity, and hotel management dashboards with various widget types.

## Technology Stack

- **Language:** Kotlin 1.9.x
- **UI Framework:** Jetpack Compose (Modern declarative UI toolkit)
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM (Model-View-ViewModel)
- **State Management:** Kotlin StateFlow / Compose State
- **Serialization:** Kotlinx Serialization (JSON)

## Key Features

1. **10-Column Responsive Grid** - Automatically sizes to fit screen width
2. **Drag-and-Drop** - Move boxes by dragging in Move mode
3. **Move Mode** - Tap any box to select, drag to move. All inputs/buttons disabled
4. **Double-Tap Edit** - Quick access to edit dialog
5. **Full-Screen Text Editor** - For editing TEXT and INPUT box content
6. **Box-to-Box Actions** - Configure actions (e.g., button increments counter)
7. **Tab System** - Multiple dashboard tabs (A-J)
8. **JSON Export/Import** - Save and load dashboard configurations via SAF (Storage Access Framework)
9. **Auto-Save & Restore** - Automatically saves on app exit with 3 rotating backups + restore option
10. **Generic JSON Import** - Import any structured JSON file, auto-converted to dashboard tabs

## Programming Methodology

### 1. Reactive UI with Jetpack Compose
- Uses Compose's declarative UI paradigm
- State-driven UI updates via `collectAsStateWithLifecycle()`
- Composable functions for all UI components

### 2. MVVM Architecture
- **Model:** Data classes in `data/model/` (Box, Tab, AppState, BoxConfig)
- **View:** Composables in `ui/screens/` and `ui/components/`
- **ViewModel:** `MainViewModel` manages all business logic and state

### 3. Grid-Based Layout System
- 10-column grid that auto-sizes to screen width
- 32 rows available for placement
- Drag-and-drop positioning with collision detection
- Box dimensions enforced to stay within grid bounds

### 4. Event-Driven Interactions
- Single tap: Select box
- Double tap: Open edit dialog
- Drag: Move or resize box (in Move mode)
- Bottom menu: Add, Edit, Move, Delete, Export

### 5. Box-to-Box Actions (EventEngine)
The app supports inter-box communication via the EventEngine:
- **Events:** ON_CLICK, ON_TEXT_CHANGE
- **Actions:** SET_TEXT, ADD_TO_LIST, ADD_TO_CHECKBOX_LIST, INCREMENT_COUNTER, DECREMENT_COUNTER
- **Data Sources:** Static values or values from other boxes

## Project Structure

```
app/src/main/java/com/dashboard/builder/
├── MainActivity.kt              # Entry point, sets up Compose
├── data/model/
│   ├── Action.kt               # Event actions (ON_CLICK, SET_TEXT, etc.)
│   ├── AppState.kt             # Root state with tabs and version
│   ├── Box.kt                  # Box entity (position, size, type, config)
│   ├── BoxConfig.kt            # Type-specific configs (Text, Button, etc.)
│   ├── BoxType.kt              # Enum: TEXT, INPUT, BUTTON, CHECKBOX_LIST, COUNTER
│   ├── Position.kt             # X, Y grid coordinates
│   ├── Size.kt                 # Width/Height in grid units
│   ├── Tab.kt                  # Tab containing boxes
│   └── Style.kt                # Background color styling
├── engine/
│   └── EventEngine.kt          # Handles box-to-box actions/events
├── ui/
│   ├── components/
│   │   ├── BottomMenu.kt       # Top action bar (Add, Edit, Move, Delete, Export)
│   │   ├── TabBar.kt           # Tab selector (A-J)
│   │   ├── boxes/
│   │   │   └── BoxContent.kt   # Renders box content based on type
│   │   └── grid/
│   │       ├── GridCanvas.kt   # Main grid with boxes and gestures
│   │       └── GridEngine.kt   # Collision detection, placement logic
│   ├── dialogs/
│   │   ├── AddBoxSheet.kt      # Modal for selecting box type
│   │   └── EditBoxSheet.kt     # Modal for editing box properties
│   ├── screens/
│   │   └── MainScreen.kt       # Main screen with Scaffold, menu, grid
│   └── theme/
│       ├── Color.kt            # App color definitions
│       └── Theme.kt            # Material 3 theme config
└── viewmodel/
    └── MainViewModel.kt        # All app state and business logic
```

## Box Types

| Type | Description | Default Size |
|------|-------------|--------------|
| TEXT | Read-only text display | 10x2 |
| INPUT | Editable text input field | 10x2 |
| BUTTON | Clickable action button | 1x1 |
| CHECKBOX_LIST | Todo-style checkbox list | 10x2 |
| COUNTER | Numeric counter with +/- buttons | 10x2 |

## Default Sample Data

### Tab A - Training Dashboard (Light Theme)
A personal trainer demo dashboard with:
- Workout stats counters (Workouts, Calories, Steps)
- Exercise checklist
- Action buttons (Start Workout, View Progress)
- Notes input field

### Tab B - Workout Dashboard (Dark Theme)
An advanced workout planning dashboard based on the "cockpit" design pattern:

🧭 **At-a-Glance Strip**
- Workout Type button (cycles through Strength/Cardio/Mobility)
- Duration counter
- Focus area text display

❤️ **Body Status**
- Sleep hours counter
- Energy level (1-5)
- Readiness score (color-coded)
- Current streak counter

🏋️ **Workout Detail**
- Exercise checkbox lists (Bench Press, Incline DB)
- Sets × Reps × Weight tracking

⏱️ **Timer & Controls**
- Rest timer (seconds)
- Sets completed counter
- Complete Workout button (linked action: increments weekly count)

📊 **Quick Stats**
- Workouts this week
- Total volume (kg)
- Notes input

**Linked Boxes:** Click "Complete" → automatically increments "This Week" counter.

## Building

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Version History

- **v1.1.0** - Auto-save on exit with 3 rotating backups + restore option, generic JSON import
- **v1.0.9** - Tab B workout dashboard with linked boxes (dark theme)
- **v1.0.8** - Move mode: tap any box to select, disable inputs/buttons in move mode
- **v1.0.7** - Grid drag-drop bug fixes (movement, resize, stale closures)
- **v1.0.6** - LocalConfiguration for accurate screen width
- **v1.0.5** - Initial release with basic grid and box types