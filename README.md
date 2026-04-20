# Dashboard Builder

A native Android application for building customizable dashboards with a drag-and-drop grid system. Designed for creating personal trainer and productivity dashboards with various widget types.

## Technology Stack

- **Language:** Kotlin 1.9.x
- **UI Framework:** Jetpack Compose (Modern declarative UI toolkit)
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM (Model-View-ViewModel)
- **State Management:** Kotlin StateFlow / Compose State
- **Serialization:** Kotlinx Serialization (JSON)

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

## Key Features

1. **10-Column Responsive Grid** - Automatically sizes to fit screen width
2. **Drag-and-Drop** - Move boxes by dragging in Move mode
3. **Double-Tap Edit** - Quick access to edit dialog
4. **Full-Screen Text Editor** - For editing TEXT and INPUT box content
5. **Box-to-Box Actions** - Configure actions (e.g., button increments counter)
6. **Tab System** - Support for multiple dashboard tabs (A-J)
7. **JSON Export/Import** - Save and load dashboard configurations

## Building

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Default Sample Data

Tab A includes a personal trainer dashboard with:
- Workout stats counters (Workouts, Calories, Steps)
- Exercise checklist
- Action buttons (Start Workout, View Progress)
- Notes input field