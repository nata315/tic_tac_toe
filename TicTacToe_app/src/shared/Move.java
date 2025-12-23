//ход игрока


package shared;

import java.io.Serializable;

public class Move implements Serializable {
    private int row;
    private int col;

    public Move() {
        // Конструктор по умолчанию для сериализации
    }

    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "Move{row=" + row + ", col=" + col + "}";
    }
}