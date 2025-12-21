package server;

import shared.GameState;
import shared.Move;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private ConcurrentHashMap<String, GameSession> activeGames = new ConcurrentHashMap<>();
    private AIPlayer aiPlayer = new AIPlayer();

    public static class GameSession {
        private String gameId;
        private String player1;
        private String player2;
        private GameState gameState;
        private boolean isActive;

        public GameSession(String gameId, String player1, String player2) {
            this.gameId = gameId;
            this.player1 = player1;
            this.player2 = player2;
            this.gameState = new GameState();
            this.isActive = true;
        }

        public boolean isValidMove(Move move) {
            int row = move.getRow();
            int col = move.getCol();
            String[][] board = gameState.getBoard();

            // Проверяем границы и пустую клетку
            return row >= 0 && row < 3 &&
                    col >= 0 && col < 3 &&
                    (board[row][col] == null || board[row][col].isEmpty());
        }

        public void makeMove(Move move, String player) {
            if (!isValidMove(move)) {
                throw new IllegalArgumentException("Invalid move");
            }

            // Определяем символ игрока
            String symbol = player.equals(player1) ? "X" : "O";

            // Обновляем доску
            gameState.makeMove(move.getRow(), move.getCol(), symbol);

            // Выводим информацию о ходе в консоль
            System.out.println("Игрок '" + player + "' поставил '" + symbol +
                    "' на клетку [" + move.getRow() + "," + move.getCol() + "]");

            // Выводим текущее состояние доски
            printBoard();

            // Меняем текущего игрока
            char newPlayerSymbol = gameState.getCurrentPlayer() == 'X' ? 'O' : 'X';
            gameState.setCurrentPlayer(newPlayerSymbol);

            // Выводим информацию о следующем ходе
            String nextPlayerName = newPlayerSymbol == 'X' ? player1 : player2;
            System.out.println("Следующий ход: " + newPlayerSymbol + " (" + nextPlayerName + ")");
        }

        private void printBoard() {
            String[][] board = gameState.getBoard();
            System.out.println("Текущее состояние доски:");
            System.out.println("  0 1 2");
            for (int i = 0; i < 3; i++) {
                System.out.print(i + " ");
                for (int j = 0; j < 3; j++) {
                    String cell = board[i][j];
                    if (cell == null || cell.isEmpty()) {
                        System.out.print("· ");
                    } else {
                        System.out.print(cell + " ");
                    }
                }
                System.out.println();
            }
            System.out.println();
        }

        public boolean checkWin(String player) {
            String symbol = player.equals(player1) ? "X" : "O";
            String[][] board = gameState.getBoard();

            // Проверка строк
            for (int i = 0; i < 3; i++) {
                if (symbol.equals(board[i][0]) &&
                        symbol.equals(board[i][1]) &&
                        symbol.equals(board[i][2])) {
                    System.out.println("Игрок '" + player + "' выиграл по строке " + i + "!");
                    return true;
                }
            }

            // Проверка столбцов
            for (int i = 0; i < 3; i++) {
                if (symbol.equals(board[0][i]) &&
                        symbol.equals(board[1][i]) &&
                        symbol.equals(board[2][i])) {
                    System.out.println("Игрок '" + player + "' выиграл по столбцу " + i + "!");
                    return true;
                }
            }

            // Проверка диагоналей
            if (symbol.equals(board[0][0]) &&
                    symbol.equals(board[1][1]) &&
                    symbol.equals(board[2][2])) {
                System.out.println("Игрок '" + player + "' выиграл по главной диагонали!");
                return true;
            }
            if (symbol.equals(board[0][2]) &&
                    symbol.equals(board[1][1]) &&
                    symbol.equals(board[2][0])) {
                System.out.println("Игрок '" + player + "' выиграл по побочной диагонали!");
                return true;
            }

            return false;
        }

        public boolean isBoardFull() {
            String[][] board = gameState.getBoard();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == null || board[i][j].isEmpty()) {
                        return false;
                    }
                }
            }
            System.out.println("Ничья! Доска полностью заполнена.");
            return true;
        }

        public boolean isVsAI() {
            return "AI".equals(player2);
        }

        // Геттеры
        public String getGameId() { return gameId; }
        public GameState getGameState() { return gameState; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) {
            isActive = active;
            if (!active) {
                System.out.println("Игра завершена. ID: " + gameId);
            }
        }
        public String getPlayer1() { return player1; }
        public String getPlayer2() { return player2; }
    }

    public GameState processMove(String gameId, Move move, String player) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            System.out.println("Ошибка: Игровая сессия не найдена: " + gameId);
            throw new IllegalArgumentException("Game session not found: " + gameId);
        }

        // Выводим информацию о начале обработки хода
        System.out.println("\n=== СЕРВЕР: Обработка хода ===");
        System.out.println("Игра: " + gameId);
        System.out.println("Игрок: " + player);
        System.out.println("Ход: [" + move.getRow() + "," + move.getCol() + "]");

        // Проверка возможности хода
        if (!session.isValidMove(move)) {
            System.out.println("Ошибка: Недопустимый ход [" + move.getRow() + "," + move.getCol() + "]");
            throw new IllegalArgumentException("Invalid move");
        }

        // 1. Делаем ход игрока
        session.makeMove(move, player);

        // 2. Проверяем, не закончилась ли игра после хода игрока
        if (session.checkWin(player)) {
            session.getGameState().setGameOver(true);
            session.getGameState().setWinner(player);
            session.setActive(false);
            System.out.println("=== Игра завершена. Победитель: " + player + " ===\n");
            return session.getGameState();
        } else if (session.isBoardFull()) {
            session.getGameState().setGameOver(true);
            session.getGameState().setWinner("DRAW");
            session.setActive(false);
            System.out.println("=== Игра завершена. Ничья! ===\n");
            return session.getGameState();
        }

        // 3. Если игра против ИИ и игра еще активна - делаем ход ИИ
        if (session.isVsAI() && session.isActive()) {
            System.out.println("--- Ход ИИ ---");
            makeAIMove(session);
        }

        System.out.println("=== Обработка хода завершена ===\n");
        return session.getGameState();
    }

    private void makeAIMove(GameSession session) {
        try {
            // Получаем текущее состояние доски
            String[][] board = session.getGameState().getBoard();

            // Получаем ход от ИИ
            Move aiMove = aiPlayer.getRandomMove(board);

            // Если ИИ нашел ход и он валидный
            if (aiMove != null && session.isValidMove(aiMove)) {
                // Делаем ход ИИ
                session.makeMove(aiMove, session.getPlayer2());

                // Проверяем, не выиграл ли ИИ
                if (session.checkWin(session.getPlayer2())) {
                    session.getGameState().setGameOver(true);
                    session.getGameState().setWinner(session.getPlayer2());
                    session.setActive(false);
                    System.out.println("=== Игра завершена. Победитель: ИИ ===\n");
                } else if (session.isBoardFull()) {
                    session.getGameState().setGameOver(true);
                    session.getGameState().setWinner("DRAW");
                    session.setActive(false);
                    System.out.println("=== Игра завершена. Ничья! ===\n");
                }
            } else if (aiMove == null) {
                System.out.println("ИИ не смог найти ход. Доска заполнена.");
            }

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении хода ИИ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public GameSession createNewGame(String player1, String player2) {
        String gameId = generateGameId(player1);
        GameSession session = new GameSession(gameId, player1, player2);
        activeGames.put(gameId, session);

        System.out.println("\n=== Создана новая игра ===");
        System.out.println("ID игры: " + gameId);
        System.out.println("Игрок 1: " + player1 + " (X)");
        System.out.println("Игрок 2: " + player2 + " (O)");
        System.out.println("Первый ход: X (" + player1 + ")");
        System.out.println("======================\n");

        return session;
    }

    public GameSession createGameWithAI(String player) {
        String gameId = generateGameId(player);
        GameSession session = new GameSession(gameId, player, "AI");
        activeGames.put(gameId, session);

        System.out.println("\n=== Создана игра против ИИ ===");
        System.out.println("ID игры: " + gameId);
        System.out.println("Игрок: " + player + " (X)");
        System.out.println("Соперник: ИИ (O) - легкий уровень");
        System.out.println("Первый ход: X (" + player + ")");
        System.out.println("===========================\n");

        return session;
    }

    public GameSession getGameSession(String gameId) {
        GameSession session = activeGames.get(gameId);
        if (session != null) {
            System.out.println("Запрошена игра: " + gameId + " | Статус: " +
                    (session.isActive() ? "активна" : "завершена"));
        }
        return session;
    }

    private String generateGameId(String player) {
        String gameId = player + "_" + System.currentTimeMillis();
        System.out.println("Сгенерирован ID игры: " + gameId);
        return gameId;
    }
}