# Project Context – Dashboard Builder

## Purpose
A lightweight Android app that lets users design simple dashboard layouts by dragging, resizing, and configuring **boxes** (text, input, counter, checkbox, etc.). The layout can be saved as JSON, exported/imported, and supports undo/redo.

## High‑Level Architecture
- **MVVM** (Model‑View‑ViewModel) pattern
- **ViewModel**: `MainViewModel.kt`
  - Holds a `MutableStateFlow<UiState>` exposing UI state via `uiState`.
  - Implements CRUD for boxes, undo stack, export/import, and actions.
- **UI (Compose)**: `MainScreen.kt`
  - Top app bar + **BottomMenu** (add, edit, move, delete, save, undo, export).
  - **GridCanvas** renders the current tab’s boxes.
  - Dialogs: `AddBoxSheet`, `EditBoxSheet`, `ExportImportDialog`.
- **Data Model** (`data/model`):
  - `AppState`, `Tab`, `Box`, `BoxConfig` (various subclasses like `TextConfig`, `CheckboxListConfig`).
  - Serialisation via Kotlinx‑serialization to JSON.
- **Undo Stack**: `undoStack` in ViewModel (push before mutating actions).

## Key Directories

DashboardBuilder/
├─ app/                 # Android app module
│  ├─ src/main/java/com/dashboard/builder/
│  │   ├─ ui/          # Compose UI components
│  │   │   ├─ components/   # BottomMenu, TabBar, etc.
│  │   │   ├─ dialogs/      # Add/Edit/Export dialogs
│  │   │   └─ screens/       # MainScreen
│  │   ├─ viewmodel/   # MainViewModel
│  │   └─ data/model/  # Data classes & serialization
│  └─ build.gradle.kts
├─ gradle/               # Wrapper scripts
├─ settings.gradle.kts
└─ README.md


## Important Features Implemented
- **Save**: Writes current layout JSON to internal storage.
- **Undo**: Stores previous `AppState` snapshots (max 10).
- **Export/Import Dialog**: Allows exporting all tabs or current tab to a JSON file and importing back.
- **BottomMenu Icons**: Add, Edit, Move, Delete, Save, Undo, Export.

## Building & Running
```bash
# From repository root
./gradlew assembleDebug       # Build APK (debug)
./gradlew installDebug       # Install on a connected device/emulator

Watch for compile errors; ensure the pushUndo calls are closed with ) (they were recently fixed).

Extending the Project

1. Add new box types – create a BoxConfig subclass and UI render in GridCanvas.
2. Enhance undo/redo – expand the stack or add a redo list.
3. Persist layouts externally – modify ExportImportDialog to write to external storage or cloud.

───

This file is a quick‑reference for anyone (or any AI agent) needing a high‑level view of the Dashboard Builder codebase without digging through every source file.