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