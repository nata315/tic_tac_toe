package shared;

import java.io.Serializable;

public class GameState implements Serializable {
    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private String winner;

    public GameState() {
        this.board = new char[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
        this.currentPlayer = 'X'; // X начинает первым
        this.gameOver = false;
    }

    // Геттеры и сеттеры
    public char[][] getBoard() { return board; }
    public char getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }

    public void setBoard(char[][] board) { this.board = board; }
    public void setCurrentPlayer(char currentPlayer) { this.currentPlayer = currentPlayer; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public void setWinner(String winner) { this.winner = winner; }

    // Метод для выполнения хода
    public void makeMove(int row, int col, char symbol) {
        board[row][col] = symbol;
    }
}