package shared;

import java.io.Serializable;

public class SerializableBoard implements Serializable {
    private String[][] board;

    public SerializableBoard() {
        this.board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
    }

    public SerializableBoard(String[][] board) {
        this.board = board;
    }

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public void setCell(int row, int col, String value) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            board[row][col] = value;
        }
    }

    public String getCell(int row, int col) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            return board[row][col];
        }
        return "";
    }

    public void print() {
        System.out.println("SerializableBoard:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print("[" + i + "," + j + "]=" + board[i][j] + " ");
            }
            System.out.println();
        }
    }
}