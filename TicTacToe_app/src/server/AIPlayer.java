package server;

import shared.Move;
import java.util.ArrayList;
import java.util.Random;

public class AIPlayer {
    private Random random = new Random();

    public Move getRandomMove(String[][] board) {
        ArrayList<Move> emptyCells = new ArrayList<>();

        // Найти все пустые клетки
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null || board[i][j].isEmpty()) {
                    emptyCells.add(new Move(i, j));
                }
            }
        }

        // Выбрать случайную
        if (emptyCells.isEmpty()) {
            return null;
        }

        return emptyCells.get(random.nextInt(emptyCells.size()));
    }
}