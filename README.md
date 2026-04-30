# Fitness Tracker App

A Java Swing desktop app for logging and managing personal activities (work, exercise, study, social, etc.) with local file persistence.

## Features

- Add activities with:
  - type
  - duration (hours + minutes)
  - collaborators (comma-separated)
  - quality rating (1-5)
  - notes
  - date (auto-set to today in Add flow)
- View all activities in a sortable table.
- Edit and delete activities from the main table.
- Search activities by:
  - type
  - date (`yyyy-MM-dd`)
  - collaborator
  - keyword (notes)
- Persist data locally in `data.bin`.

## Project Structure

- `src/main/`
  - App entry point (`main.MainGUI`).
- `src/ui/`
  - Swing UI screens:
    - `MainGUI` (main window/table)
    - `AddGUI`
    - `EditGUI`
    - `SearchGUI`
- `src/service/`
  - Business logic (`ActivityService`) for CRUD/search operations.
- `src/storage/`
  - File I/O layer (`StorageManager`) that loads/saves `data.bin`.
- `src/model/`
  - Data models (`Activity`, `Duration`).
- `docs/`
  - Project diagrams and design docs (`Use Case`, `Sequence`, `Class Diagram`).

## Requirements

- Java JDK installed (8+ recommended; newer is fine).
- macOS/Linux/Windows terminal.

## Build and Run

From the project root:

```bash
mkdir -p out
javac -d out src/model/*.java src/storage/*.java src/service/*.java src/ui/*.java src/main/*.java
java -cp out main.MainGUI
```

If already compiled, run again with:

```bash
java -cp out main.MainGUI
```

## Data File

- App data is stored in `data.bin` in the project root.
- `data.bin` is local runtime data and should generally be ignored in git.

## General Usage

1. Launch the app.
2. Use **Add** to create a new activity.
3. Right-click a row in the table to **Edit** or **Delete**.
4. Use **Search** to filter by category and query.
5. Use **Refresh** to reload the full list from in-memory state.

