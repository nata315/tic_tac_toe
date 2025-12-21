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

    public boolean isBoardFull(String[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null || board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Move getBestMove(String[][] board, String aiSymbol, String playerSymbol) {
        // Простая эвристика: сначала проверяем, можем ли выиграть
        Move winningMove = findWinningMove(board, aiSymbol);
        if (winningMove != null) return winningMove;

        // Затем проверяем, нужно ли блокировать игрока
        Move blockingMove = findWinningMove(board, playerSymbol);
        if (blockingMove != null) return blockingMove;

        // Если центр свободен - занимаем центр
        if (board[1][1] == null || board[1][1].isEmpty()) {
            return new Move(1, 1);
        }

        // Затем углы
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] corner : corners) {
            if (board[corner[0]][corner[1]] == null || board[corner[0]][corner[1]].isEmpty()) {
                return new Move(corner[0], corner[1]);
            }
        }

        // Иначе случайный ход
        return getRandomMove(board);
    }

    private Move findWinningMove(String[][] board, String symbol) {
        // Проверка строк
        for (int i = 0; i < 3; i++) {
            int count = 0;
            Move emptyCell = null;
            for (int j = 0; j < 3; j++) {
                if (symbol.equals(board[i][j])) {
                    count++;
                } else if (board[i][j] == null || board[i][j].isEmpty()) {
                    emptyCell = new Move(i, j);
                }
            }
            if (count == 2 && emptyCell != null) return emptyCell;
        }

        // Проверка столбцов
        for (int j = 0; j < 3; j++) {
            int count = 0;
            Move emptyCell = null;
            for (int i = 0; i < 3; i++) {
                if (symbol.equals(board[i][j])) {
                    count++;
                } else if (board[i][j] == null || board[i][j].isEmpty()) {
                    emptyCell = new Move(i, j);
                }
            }
            if (count == 2 && emptyCell != null) return emptyCell;
        }

        // Проверка диагоналей
        int count = 0;
        Move emptyCell = null;
        for (int i = 0; i < 3; i++) {
            if (symbol.equals(board[i][i])) {
                count++;
            } else if (board[i][i] == null || board[i][i].isEmpty()) {
                emptyCell = new Move(i, i);
            }
        }
        if (count == 2 && emptyCell != null) return emptyCell;

        count = 0;
        emptyCell = null;
        for (int i = 0; i < 3; i++) {
            if (symbol.equals(board[i][2-i])) {
                count++;
            } else if (board[i][2-i] == null || board[i][2-i].isEmpty()) {
                emptyCell = new Move(i, 2-i);
            }
        }
        if (count == 2 && emptyCell != null) return emptyCell;

        return null;
    }
}