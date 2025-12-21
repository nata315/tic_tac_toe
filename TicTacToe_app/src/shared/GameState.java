package shared;

import java.io.Serializable;
import java.util.Arrays;

public class GameState implements Serializable {
    private String[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private String winner;

    public GameState() {
        this.board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";  // Пустая строка вместо пробела
            }
        }
        this.currentPlayer = 'X';
        this.gameOver = false;
        this.winner = null;
    }

    // Геттеры и сеттеры
    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        if (board == null || board.length != 3) {
            throw new IllegalArgumentException("Invalid board");
        }
        this.board = board;
    }

    public char getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(char currentPlayer) { this.currentPlayer = currentPlayer; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    // Метод для выполнения хода
    public void makeMove(int row, int col, String symbol) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        board[row][col] = symbol;
    }

    // Перегруженный метод для char (для совместимости)
    public void makeMove(int row, int col, char symbol) {
        makeMove(row, col, String.valueOf(symbol));
    }

    // Метод для отладки
    public void printBoard() {
        System.out.println("GameState Board:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print("'" + (board[i][j] == null ? "null" : board[i][j]) + "' ");
            }
            System.out.println();
        }
    }
}