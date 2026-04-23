# Project Context – Dashboard Builder

## Purpose
A lightweight Android app that lets users design simple dashboard layouts by dragging, resizing, and configuring **boxes** (text, input, counter, checkbox, button, etc.). The layout can be saved as JSON, exported/imported via SAF, supports auto-save with backups, and features box-to-box actions.

## High‑Level Architecture
- **MVVM** (Model‑View‑ViewModel) pattern
- **ViewModel**: `MainViewModel.kt`
  - Holds a `MutableStateFlow<UiState>` exposing UI state via `uiState`.
  - Implements CRUD for boxes, undo stack, export/import, auto-save, restore.
- **UI (Compose)**: `MainScreen.kt`
  - Top app bar + **BottomMenu** (add, edit, move, delete, save, undo, export).
  - **GridCanvas** renders the current tab's boxes.
  - Dialogs: `AddBoxSheet`, `EditBoxSheet`, `ExportImportDialog`.
- **Data Model** (`data/model`):
  - `AppState`, `Tab`, `Box`, `BoxConfig` (various subclasses like `TextConfig`, `CheckboxListConfig`, `ButtonConfig`, `CounterConfig`, `InputConfig`).
  - Serialisation via Kotlinx‑serialization to JSON.
- **Undo Stack**: `undoStack` in ViewModel (push before mutating actions).
- **EventEngine**: Handles box-to-box actions (ON_CLICK, ON_TEXT_CHANGE).

## Key Directories

DashboardBuilder/
├─ app/                 # Android app module
│  ├─ src/main/java/com/dashboard/builder/
│  │  ├─ ui/           # Compose UI components
│  │  │  ├─ components/   # BottomMenu, TabBar, GridCanvas, BoxContent
│  │  │  ├─ dialogs/      # AddBoxSheet, EditBoxSheet, ExportImportDialog
│  │  │  └─ screens/      # MainScreen
│  │  ├─ viewmodel/   # MainViewModel
│  │  ├─ data/model/  # Data classes & serialization
│  │  ├─ data/        # JsonImportConverter (generic JSON import)
│  │  └─ engine/      # EventEngine (box-to-box actions)
│  └─ build.gradle.kts
├─ gradle/              # Wrapper scripts
├─ settings.gradle.kts
└─ README.md

## Box Types
- **TEXT**: Read-only text display
- **INPUT**: Editable text input field
- **BUTTON**: Clickable action button
- **CHECKBOX_LIST**: Todo-style checkbox list
- **COUNTER**: Numeric counter with +/- buttons

## Key Features Implemented
- **10-Column Responsive Grid**: Auto-sizes to screen width
- **Drag-and-Drop**: Move boxes by dragging in Move mode
- **Tab System**: Multiple dashboard tabs (A-J)
- **JSON Export/Import**: Via SAF (Storage Access Framework)
- **Auto-Save**: Saves to internal storage on app onStop()
- **3 Rotating Backups**: Maintains backup_0.json, backup_1.json, backup_2.json
- **Restore**: Restore from backups via Export/Import dialog
- **Generic JSON Import**: Import any structured JSON, auto-converted to dashboard tabs
- **Box-to-Box Actions**: SET_TEXT, ADD_TO_LIST, ADD_TO_CHECKBOX_LIST, INCREMENT_COUNTER, DECREMENT_COUNTER, SWITCH_TAB
- **Undo**: Stores previous AppState snapshots (max 10)

## Building & Running
```bash
# From repository root
./gradlew assembleDebug       # Build APK (debug)
./gradlew installDebug       # Install on a connected device/emulator
```

## GitHub Actions
- Auto-builds APK on push to main
- Workflow: `.github/workflows/build.yml`

---

This file is a quick‑reference for anyone (or any AI agent) needing a high‑level view of the Dashboard Builder codebase without digging through every source file.