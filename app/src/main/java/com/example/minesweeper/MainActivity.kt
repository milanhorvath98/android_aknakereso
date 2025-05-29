package com.example.minesweeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
// Removed java.util.concurrent.TimeUnit as it's not directly used in the final code,
// calculation is done manually. If needed for other time formatting, it can be re-added.

class MainActivity : AppCompatActivity() {

    private lateinit var game: MinesweeperGame
    private lateinit var boardGridLayout: GridLayout
    private lateinit var timerTextView: TextView
    private lateinit var mineCountTextView: TextView
    private lateinit var resetButton: Button

    private var timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var secondsElapsed: Int = 0

    private var cellButtons: Array<Array<Button>>? = null

    // Default game settings
    private var boardWidth = 10
    private var boardHeight = 10
    private var numMines = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.timerTextView)
        mineCountTextView = findViewById(R.id.mineCountTextView)
        boardGridLayout = findViewById(R.id.boardGridLayout)
        resetButton = findViewById(R.id.resetButton)

        // Example: Allow user to change settings later or load from preferences
        // For now, using defaults
        // boardWidth = intent.getIntExtra("BOARD_WIDTH", 10)
        // boardHeight = intent.getIntExtra("BOARD_HEIGHT", 10)
        // numMines = intent.getIntExtra("NUM_MINES", 15)

        resetButton.setOnClickListener {
            resetGame()
        }
        resetGame()
    }

    private fun resetGame() {
        stopTimer()
        secondsElapsed = 0
        timerTextView.text = getString(R.string.timer_default_text)

        game = MinesweeperGame(boardHeight, boardWidth, numMines) // Corrected order: height, width as per MinesweeperGame constructor
        cellButtons = Array(game.height) { Array(game.width) { Button(this) } }

        setupBoardView()
        updateMineCountText()
        // Game might be immediately over if numMines is too high, though MinesweeperGame doesn't explicitly handle this at init
        if (!game.isGameOver() && !game.isGameWon()) {
            startTimer()
        }
    }

    private fun setupBoardView() {
        boardGridLayout.removeAllViews()
        boardGridLayout.rowCount = game.height
        boardGridLayout.columnCount = game.width

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        // Calculate available width for the GridLayout more accurately
        val screenWidth = displayMetrics.widthPixels
        val layoutPadding = boardGridLayout.paddingLeft + boardGridLayout.paddingRight
        // Consider parent padding if significant
        val parentView = boardGridLayout.parent as? ViewGroup
        val parentPadding = if (parentView != null) parentView.paddingLeft + parentView.paddingRight else 0
        
        val availableWidth = screenWidth - layoutPadding - parentPadding
        val cellMargin = 1 * 2 // Each cell has 1dp margin on left and right
        val cellSize = (availableWidth / game.width) - cellMargin


        for (row in 0 until game.height) {
            for (col in 0 until game.width) {
                val cellButton = Button(this).apply {
                    textSize = 18f // Adjust as needed
                    // To make text more visible, especially for numbers
                    setPadding(0,0,0,0) // Remove default button padding if text is cut off
                }

                val params = GridLayout.LayoutParams().apply {
                    width = if (cellSize > 0) cellSize else GridLayout.LayoutParams.WRAP_CONTENT
                    height = if (cellSize > 0) cellSize else GridLayout.LayoutParams.WRAP_CONTENT // Keep it square
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                    setMargins(1, 1, 1, 1) // 1dp margin around each button
                }
                cellButton.layoutParams = params

                cellButton.setOnClickListener {
                    if (game.isGameOver() || game.isGameWon() || game.board[row][col].isRevealed) return@setOnClickListener
                    game.handleCellClick(row, col)
                    updateBoardView() // Update the whole board
                    checkGameEndConditions()
                }

                cellButton.setOnLongClickListener {
                    if (game.isGameOver() || game.isGameWon() || game.board[row][col].isRevealed) return@setOnLongClickListener true
                    game.toggleFlag(row, col)
                    updateBoardView() // Update only this cell's view if possible, or whole board
                    true // Consume the long click
                }
                cellButtons!![row][col] = cellButton
                boardGridLayout.addView(cellButton)
            }
        }
        updateBoardView() // Initial draw of the board
    }

    private fun updateBoardView() {
        for (row in 0 until game.height) {
            for (col in 0 until game.width) {
                val cell = game.board[row][col]
                val button = cellButtons!![row][col]

                // Disable button if game is over OR if the specific cell is revealed
                button.isEnabled = !game.isGameOver() && !cell.isRevealed


                if (cell.isRevealed) {
                    if (cell.isMine) {
                        button.text = "💣" // Mine emoji
                        button.setBackgroundColor(getColor(android.R.color.holo_red_light)) // Indicate danger
                    } else {
                        button.text = if (cell.adjacentMines > 0) cell.adjacentMines.toString() else ""
                        button.setBackgroundColor(getColor(android.R.color.darker_gray)) // Revealed safe cell
                    }
                } else { // Not revealed
                    button.text = if (cell.isFlagged) "🚩" else "" // Flag emoji
                    button.setBackgroundResource(android.R.drawable.btn_default) // Default button look
                }
                
                // Special handling for game over state to show all mines
                if (game.isGameOver()) {
                    button.isEnabled = false // Disable all buttons on game over
                    if (cell.isMine) {
                         // Show bomb if it's a mine and not flagged (or if flagged correctly)
                        button.text = if (cell.isFlagged) "🚩" else "💣"
                        if (!cell.isFlagged) button.setBackgroundColor(getColor(android.R.color.holo_red_light))
                    }
                }
            }
        }
        updateMineCountText()
        // This check is now more implicitly handled by game.isGameWon() and game.isGameOver()
        // in handleCellClick and toggleFlag, which then call checkGameEndConditions.
        // However, explicit check here after board update can be a safeguard.
        if (game.isGameWon() && !game.isGameOver()) { // Make sure game over toast isn't shown if win is already declared
             checkGameEndConditions()
        }
    }

    private fun updateMineCountText() {
        mineCountTextView.text = "Mines: ${game.getNumMines() - game.getFlagsUsed()}"
    }

    private fun checkGameEndConditions() {
        // Check game.isGameOver() first because a loss sets gameOver = true
        if (game.isGameOver() && !game.isGameWon()) { // Explicitly check not won, for loss condition
            stopTimer()
            Toast.makeText(this, getString(R.string.game_over_message), Toast.LENGTH_LONG).show()
            revealAllMines(false) // isWin = false
        } else if (game.isGameWon()) {
            stopTimer()
            // Ensure gameOver is also set to true on win, which MinesweeperGame does.
            Toast.makeText(this, getString(R.string.win_message), Toast.LENGTH_LONG).show()
            revealAllMines(true) // isWin = true
        }
    }

    private fun revealAllMines(isWin: Boolean) {
        for (r in 0 until game.height) {
            for (c in 0 until game.width) {
                val cell = game.board[r][c]
                val button = cellButtons!![r][c]
                button.isEnabled = false // Disable all buttons

                if (cell.isMine) {
                    button.text = if (isWin || cell.isFlagged) "🚩" else "💣" // If win, all mines are treated as if flagged
                    if (isWin) button.setBackgroundColor(getColor(android.R.color.holo_green_light))
                    else if (!cell.isFlagged) button.setBackgroundColor(getColor(android.R.color.holo_red_light))

                } else { // Non-mine cells
                    if (isWin && !cell.isRevealed) { // For win, reveal unrevealed non-mines
                        button.text = if (cell.adjacentMines > 0) cell.adjacentMines.toString() else ""
                        button.setBackgroundColor(getColor(android.R.color.darker_gray))
                    } else if (!cell.isRevealed) { // For loss, non-revealed, non-mine cells remain as they are (e.g. empty or flagged)
                         button.text = if (cell.isFlagged) "🚩" else ""
                         // Keep default background or specific for unrevealed
                    }
                    // If already revealed, it keeps its number/empty text and darker_gray background
                }
            }
        }
    }

    private fun startTimer() {
        if (timerRunnable != null || game.isGameOver() || game.isGameWon()) return // Already running or game ended

        timerRunnable = object : Runnable {
            override fun run() {
                // Check game state again inside runnable, as it might change between posts
                if (game.isGameOver() || game.isGameWon()) {
                    stopTimer()
                    return
                }

                secondsElapsed++
                val minutes = secondsElapsed / 60
                val secs = secondsElapsed % 60
                timerTextView.text = String.format("%02d:%02d", minutes, secs)

                timerHandler.postDelayed(this, 1000)
            }
        }
        // Post immediately and then every second
        timerHandler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        timerRunnable = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer() // Clean up timer to prevent leaks
    }
}
