# Android Minesweeper

A simple Minesweeper game implemented for Android. This project demonstrates basic Android development concepts including:

*   Custom game logic in Kotlin.
*   UI creation with XML layouts and dynamic view manipulation in `MainActivity`.
*   Event handling for user interactions (click and long-click).
*   Resource management for strings and colors.
*   Basic game state management.

## How to Run

1.  **Get the code:**
    *   Clone this repository: `git clone <repository_url>` (Replace `<repository_url>` with the actual URL if available)
    *   Or download the source code as a ZIP file and extract it.

2.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select "Open" (or "Open an Existing Project..." / "Import Project..." depending on your Android Studio version and welcome screen).
    *   Navigate to the root directory where you cloned or extracted the project files.
    *   Select the project's root `build.gradle` file or the directory itself and click "OK" or "Open".
    *   Android Studio will import the project. **Wait for Gradle to sync dependencies. This might take a few moments. If you encounter errors like 'unresolved reference', ensure the Gradle sync has completed successfully. You can manually trigger a sync by going to 'File' > 'Sync Project with Gradle Files'.**

3.  **Build and Run:**
    *   Once Gradle sync is complete and the project has been indexed, you can build and run the app.
    *   Ensure you have an Android Virtual Device (AVD) configured in Android Studio (via Tools > AVD Manager) or a physical Android device connected to your computer with USB debugging enabled.
    *   Select your target device from the device dropdown menu in the toolbar.
    *   Click the "Run 'app'" button (the green play icon) in the toolbar, or select "Run" > "Run 'app'" from the main menu.
    *   Android Studio will build the APK and install it on the selected device/emulator.

## Project Structure

*   `app/src/main/java/com/example/minesweeper/`:
    *   `MinesweeperGame.kt`: Contains the core game logic, including board generation, mine placement, cell state management, and win/loss conditions.
    *   `MainActivity.kt`: The main UI controller. It initializes the game, sets up the game board view with `Button`s in a `GridLayout`, handles user clicks and long-clicks on cells, updates the timer and mine count, and manages game reset functionality.
*   `app/src/main/res/layout/`:
    *   `activity_main.xml`: XML layout file defining the structure of the main game screen, including `TextView`s for timer and mine count, the `GridLayout` for the game board, and a `Button` for resetting the game.
*   `app/src/main/res/values/`:
    *   `colors.xml`: Defines color resources used in the application (though currently, `MainActivity` uses some hardcoded colors for cell states).
    *   `strings.xml`: Defines string resources for UI elements like the app name, button texts, and messages.
*   `app/src/main/AndroidManifest.xml`: The application manifest file. It declares the `MainActivity` as the launcher activity and sets basic application metadata.

## Features Implemented

*   Classic Minesweeper gameplay:
    *   Click to reveal cells.
    *   Game over on hitting a mine.
    *   Numbers in revealed cells indicate adjacent mines.
    *   Flood fill for empty cells (cells with no adjacent mines).
    *   Long-click to toggle flags on cells.
*   Game Board:
    *   Dynamically generated grid of buttons.
    *   Board size is currently fixed at 10x10 with 15 mines (configurable in `MainActivity.kt` variables: `boardWidth`, `boardHeight`, `numMines`).
    *   Cell buttons are sized to fit the screen width.
*   Game Information:
    *   Timer to track game duration (MM:SS format).
    *   Mine counter displaying remaining mines (total mines - flags used).
*   Controls:
    *   Reset button to start a new game.
*   Game State:
    *   Detection of game win (all non-mine cells revealed).
    *   Detection of game loss (mine clicked).
    *   Toast messages for win/loss conditions.
    *   Reveals all mines at the end of the game.
*   Basic UI:
    *   Uses standard Android UI components.
    *   Text characters "💣" for mines and "🚩" for flags.
    *   Background color changes to indicate revealed cells and clicked mines.
    *   Externalized strings and colors in resource files.
    *   Basic `AndroidManifest.xml` setup.
