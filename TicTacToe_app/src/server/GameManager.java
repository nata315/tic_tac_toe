package server;

import shared.GameState;
import shared.Move;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    // Хранение активных игр в ConcurrentHashMap
    private ConcurrentHashMap<String, GameSession> activeGames = new ConcurrentHashMap<>();

    // Внутренний класс для игровой сессии
    public class GameSession {
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

        // Проверка валидности хода
        public boolean isValidMove(Move move) {
            int row = move.getRow();
            int col = move.getCol();
            char[][] board = gameState.getBoard();

            // Проверяем границы и пустую клетку
            return row >= 0 && row < 3 &&
                    col >= 0 && col < 3 &&
                    board[row][col] == ' ';
        }

        // Выполнение хода
        public void makeMove(Move move, String player) {
            if (!isValidMove(move)) {
                throw new IllegalArgumentException("Invalid move");
            }

            // Определяем символ игрока
            char symbol = player.equals(player1) ? 'X' : 'O';

            // Обновляем доску
            gameState.makeMove(move.getRow(), move.getCol(), symbol);

            // Меняем текущего игрока
            gameState.setCurrentPlayer(gameState.getCurrentPlayer() == 'X' ? 'O' : 'X');
        }

        // Проверка победы
        public boolean checkWin(String player) {
            char symbol = player.equals(player1) ? 'X' : 'O';
            char[][] board = gameState.getBoard();

            // Проверка строк
            for (int i = 0; i < 3; i++) {
                if (board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol) {
                    return true;
                }
            }

            // Проверка столбцов
            for (int i = 0; i < 3; i++) {
                if (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol) {
                    return true;
                }
            }

            // Проверка диагоналей
            if (board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) {
                return true;
            }
            if (board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol) {
                return true;
            }

            return false;
        }

        // Проверка заполненности доски
        public boolean isBoardFull() {
            char[][] board = gameState.getBoard();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == ' ') {
                        return false;
                    }
                }
            }
            return true;
        }

        // Геттеры и сеттеры
        public String getGameId() { return gameId; }
        public GameState getGameState() { return gameState; }
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        public String getPlayer1() { return player1; }
        public String getPlayer2() { return player2; }
    }

    public GameState processMove(String gameId, Move move, String player) {
        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new IllegalArgumentException("Game session not found: " + gameId);
        }

        // Проверка возможности хода
        if (!session.isValidMove(move)) {
            throw new IllegalArgumentException("Invalid move");
        }

        // Обновить доску
        session.makeMove(move, player);

        // Проверить статус игры
        if (session.checkWin(player)) {
            session.getGameState().setGameOver(true);
            session.getGameState().setWinner(player);
            session.setActive(false);
        } else if (session.isBoardFull()) {
            session.getGameState().setGameOver(true);
            session.getGameState().setWinner("DRAW");
            session.setActive(false);
        }

        return session.getGameState();
    }

    // Создание новой игровой сессии
    public GameSession createNewGame(String player1, String player2) {
        String gameId = generateGameId(player1);
        GameSession session = new GameSession(gameId, player1, player2);
        activeGames.put(gameId, session);
        return session;
    }

    // Создание игры против AI
    public GameSession createGameWithAI(String player) {
        String gameId = generateGameId(player);
        GameSession session = new GameSession(gameId, player, "AI");
        activeGames.put(gameId, session);
        return session;
    }

    // Получение игровой сессии по ID
    public GameSession getGameSession(String gameId) {
        return activeGames.get(gameId);
    }

    // Удаление игровой сессии
    public void removeGameSession(String gameId) {
        activeGames.remove(gameId);
    }

    // Проверка существования сессии
    public boolean hasGameSession(String gameId) {
        return activeGames.containsKey(gameId);
    }

    // Генерация уникального ID игры
    private String generateGameId(String player) {
        return player + "_" + System.currentTimeMillis();
    }
}