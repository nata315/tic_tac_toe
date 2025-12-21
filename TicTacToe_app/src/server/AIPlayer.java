//Логика ИИ-соперника


package server;
import shared.*;

public class AIPlayer {

    public Move getRandomMove(char[][] board) {
        java.util.ArrayList<Move> emptyCells = new java.util.ArrayList<>();

        // Найти все пустые клетки
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    emptyCells.add(new Move(i, j));
                }
            }
        }

        // Выбрать случайную
        if (emptyCells.isEmpty()) {
            return null;
        }

        java.util.Random rand = new java.util.Random();
        return emptyCells.get(rand.nextInt(emptyCells.size()));
    }

    // Добавлен метод isBoardFull() в этот же класс
    public boolean isBoardFull(char[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    public Move getBestMove(char[][] board, char aiSymbol, char playerSymbol) {
        // Простая эвристика: сначала проверяем, можем ли выиграть
        Move winningMove = findWinningMove(board, aiSymbol);
        if (winningMove != null) return winningMove;

        // Затем проверяем, нужно ли блокировать игрока
        Move blockingMove = findWinningMove(board, playerSymbol);
        if (blockingMove != null) return blockingMove;

        // Если центр свободен - занимаем центр
        if (board[1][1] == ' ') return new Move(1, 1);

        // Затем углы
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] corner : corners) {
            if (board[corner[0]][corner[1]] == ' ') {
                return new Move(corner[0], corner[1]);
            }
        }

        // Иначе случайный ход
        return getRandomMove(board);
    }

    private Move findWinningMove(char[][] board, char symbol) {
        // Проверка строк
        for (int i = 0; i < 3; i++) {
            int count = 0;
            Move emptyCell = null;
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == symbol) count++;
                else if (board[i][j] == ' ') emptyCell = new Move(i, j);
            }
            if (count == 2 && emptyCell != null) return emptyCell;
        }

        // Проверка столбцов
        for (int j = 0; j < 3; j++) {
            int count = 0;
            Move emptyCell = null;
            for (int i = 0; i < 3; i++) {
                if (board[i][j] == symbol) count++;
                else if (board[i][j] == ' ') emptyCell = new Move(i, j);
            }
            if (count == 2 && emptyCell != null) return emptyCell;
        }

        // Проверка диагоналей
        int count = 0;
        Move emptyCell = null;
        for (int i = 0; i < 3; i++) {
            if (board[i][i] == symbol) count++;
            else if (board[i][i] == ' ') emptyCell = new Move(i, i);
        }
        if (count == 2 && emptyCell != null) return emptyCell;

        count = 0;
        emptyCell = null;
        for (int i = 0; i < 3; i++) {
            if (board[i][2-i] == symbol) count++;
            else if (board[i][2-i] == ' ') emptyCell = new Move(i, 2-i);
        }
        if (count == 2 && emptyCell != null) return emptyCell;

        return null;
    }
}