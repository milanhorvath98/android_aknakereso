package com.example.minesweeper

import kotlin.random.Random

data class Cell(
    var isMine: Boolean = false,
    var isRevealed: Boolean = false,
    var isFlagged: Boolean = false,
    var adjacentMines: Int = 0
)

class MinesweeperGame(private var width: Int, private var height: Int, private var numMines: Int) {

    lateinit var board: Array<Array<Cell>>
    var gameOver: Boolean = false
    var gameWon: Boolean = false
    var flagsUsed: Int = 0
    private var minesFlaggedCorrectly: Int = 0

    init {
        resetGame()
    }

    fun resetGame(newWidth: Int = width, newHeight: Int = height, newNumMines: Int = numMines) {
        width = newWidth
        height = newHeight
        numMines = newNumMines

        board = Array(height) { Array(width) { Cell() } }
        gameOver = false
        gameWon = false
        flagsUsed = 0
        minesFlaggedCorrectly = 0

        placeMines()
        calculateAdjacentMines()
    }

    private fun placeMines() {
        var minesPlaced = 0
        while (minesPlaced < numMines) {
            val row = Random.nextInt(height)
            val col = Random.nextInt(width)
            if (!board[row][col].isMine) {
                board[row][col].isMine = true
                minesPlaced++
            }
        }
    }

    private fun calculateAdjacentMines() {
        for (row in 0 until height) {
            for (col in 0 until width) {
                if (!board[row][col].isMine) {
                    var count = 0
                    getNeighbors(row, col).forEach { (nr, nc) ->
                        if (board[nr][nc].isMine) {
                            count++
                        }
                    }
                    board[row][col].adjacentMines = count
                }
            }
        }
    }

    fun handleCellClick(row: Int, col: Int) {
        if (gameOver || gameWon || board[row][col].isFlagged || board[row][col].isRevealed) {
            return
        }

        val cell = board[row][col]
        if (cell.isMine) {
            gameOver = true
            // Reveal all mines
            for (r in 0 until height) {
                for (c in 0 until width) {
                    if (board[r][c].isMine) {
                        board[r][c].isRevealed = true
                    }
                }
            }
            return
        }

        revealCell(row, col)

        if (checkWinCondition()) {
            gameWon = true
            gameOver = true // Game ends when won
        }
    }

    fun toggleFlag(row: Int, col: Int) {
        if (gameOver || gameWon || board[row][col].isRevealed) {
            return
        }

        val cell = board[row][col]
        cell.isFlagged = !cell.isFlagged

        if (cell.isFlagged) {
            flagsUsed++
            if (cell.isMine) {
                minesFlaggedCorrectly++
            }
        } else {
            flagsUsed--
            if (cell.isMine) {
                minesFlaggedCorrectly--
            }
        }

        if (checkWinCondition()) {
            gameWon = true
            gameOver = true
        }
    }

    private fun revealCell(row: Int, col: Int) {
        if (row < 0 || row >= height || col < 0 || col >= width || board[row][col].isRevealed || board[row][col].isFlagged) {
            return
        }

        val cell = board[row][col]
        cell.isRevealed = true

        if (cell.adjacentMines == 0 && !cell.isMine) { // Flood fill for empty cells
            getNeighbors(row, col).forEach { (nr, nc) ->
                if(!board[nr][nc].isRevealed){ // only reveal if not already revealed
                   revealCell(nr, nc)
                }
            }
        }
    }

    private fun checkWinCondition(): Boolean {
        // Win if all non-mine cells are revealed
        var nonMineCellsRevealed = 0
        var nonMineCellsCount = 0
        for (row in 0 until height) {
            for (col in 0 until width) {
                if (!board[row][col].isMine) {
                    nonMineCellsCount++
                    if (board[row][col].isRevealed) {
                        nonMineCellsRevealed++
                    }
                }
            }
        }
        if (nonMineCellsRevealed == nonMineCellsCount) {
            return true
        }

        // Alternative win: all mines are flagged, and all non-mines are revealed
        // This is implicitly covered by the above if all flags are correct.
        // If we want a stricter "all mines must be flagged":
        // return minesFlaggedCorrectly == numMines && nonMineCellsRevealed == nonMineCellsCount

        return false
    }

    private fun getNeighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        val neighbors = mutableListOf<Pair<Int, Int>>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue // Skip the cell itself
                val nr, nc: Int
                nr = row + dr
                nc = col + dc
                if (nr >= 0 && nr < height && nc >= 0 && nc < width) {
                    neighbors.add(Pair(nr, nc))
                }
            }
        }
        return neighbors
    }

    // Getter for the board to be used by UI
    fun getBoard(): Array<Array<Cell>> = board

    // Getter for game state
    fun isGameOver(): Boolean = gameOver
    fun isGameWon(): Boolean = gameWon
    fun getFlagsUsed(): Int = flagsUsed
    fun getNumMines(): Int = numMines
}
