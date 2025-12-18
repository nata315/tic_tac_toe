package client.controllers;
import client.ClientNetwork;
import client.ui.GameWindow;
import javax.swing.SwingUtilities;

public class GameController {
    private GameWindow gameFrame;
    private ClientNetwork clientNetwork;
    private Runnable onExit;

    // Локальные переменные для тестирования
    private String currentPlayer = "X";
    private String[][] board = new String[3][3];
    private boolean gameActive = true;
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;

    public GameController(GameWindow gameFrame, ClientNetwork clientNetwork) {
        this.gameFrame = gameFrame;
        this.clientNetwork = clientNetwork;

        gameFrame.setListener(new GameWindow.GameFrameListener() {
            @Override
            public void onCellClicked(int row, int col) {
                handleMove(row, col);
            }

            @Override
            public void onNewGame() {
                startNewGame();
            }

            @Override
            public void onSurrender() {
                surrenderGame();
            }

            @Override
            public void onExit() {
                exitGame();
            }

            @Override
            public void onSaveGame() {
                saveGame();
            }

            @Override
            public void onLoadGame() {
                loadGame();
            }
        });

        // Начинаем новую игру
        startNewGame();
    }

    private void handleMove(int row, int col) {
        if (!gameActive || board[row][col] != null) {
            return;
        }

        // Локальная обработка хода (в реальной версии будет отправка на сервер)
        board[row][col] = currentPlayer;
        gameFrame.updateBoard(board);
        clientNetwork.sendMove(row, col, currentPlayer);

        // Проверка победы
        if (checkWin(currentPlayer)) {
            gameActive = false;
            gameFrame.disableBoard();

            if (currentPlayer.equals("X")) {
                wins++;
                gameFrame.setStatus("Победа! Вы выиграли!");
            } else {
                losses++;
                gameFrame.setStatus("Поражение! Вы проиграли.");
            }

            gameFrame.setScoreInfo(wins, losses, draws);
            highlightWinningLine();
            return;
        }

        // Проверка ничьей
        if (isBoardFull()) {
            gameActive = false;
            gameFrame.disableBoard();
            draws++;
            gameFrame.setStatus("Ничья!");
            gameFrame.setScoreInfo(wins, losses, draws);
            return;
        }

        // Смена игрока
        currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        gameFrame.setPlayerInfo(currentPlayer);
        gameFrame.setStatus("Ход игрока " + currentPlayer);
    }

    private boolean checkWin(String player) {
        // Проверка строк
        for (int i = 0; i < 3; i++) {
            if (player.equals(board[i][0]) &&
                    player.equals(board[i][1]) &&
                    player.equals(board[i][2])) {
                return true;
            }
        }

        // Проверка столбцов
        for (int i = 0; i < 3; i++) {
            if (player.equals(board[0][i]) &&
                    player.equals(board[1][i]) &&
                    player.equals(board[2][i])) {
                return true;
            }
        }

        // Проверка диагоналей
        if (player.equals(board[0][0]) &&
                player.equals(board[1][1]) &&
                player.equals(board[2][2])) {
            return true;
        }

        if (player.equals(board[0][2]) &&
                player.equals(board[1][1]) &&
                player.equals(board[2][0])) {
            return true;
        }

        return false;
    }

    private int[][] getWinningLine(String player) {
        // Проверка строк
        for (int i = 0; i < 3; i++) {
            if (player.equals(board[i][0]) &&
                    player.equals(board[i][1]) &&
                    player.equals(board[i][2])) {
                return new int[][]{{i, 0}, {i, 1}, {i, 2}};
            }
        }

        // Проверка столбцов
        for (int i = 0; i < 3; i++) {
            if (player.equals(board[0][i]) &&
                    player.equals(board[1][i]) &&
                    player.equals(board[2][i])) {
                return new int[][]{{0, i}, {1, i}, {2, i}};
            }
        }

        // Проверка диагоналей
        if (player.equals(board[0][0]) &&
                player.equals(board[1][1]) &&
                player.equals(board[2][2])) {
            return new int[][]{{0, 0}, {1, 1}, {2, 2}};
        }

        if (player.equals(board[0][2]) &&
                player.equals(board[1][1]) &&
                player.equals(board[2][0])) {
            return new int[][]{{0, 2}, {1, 1}, {2, 0}};
        }

        return null;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void highlightWinningLine() {
        int[][] winningLine = getWinningLine(currentPlayer);
        if (winningLine != null) {
            gameFrame.highlightWinningLine(winningLine);
        }
    }

    private void startNewGame() {
        board = new String[3][3];
        currentPlayer = "X";
        gameActive = true;

        gameFrame.clearBoard();
        gameFrame.enableBoard();
        gameFrame.setPlayerInfo(currentPlayer);
        gameFrame.setStatus("Новая игра! Ход игрока " + currentPlayer);

        System.out.println("Начата новая игра");
    }

    private void surrenderGame() {
        if (!gameActive) return;

        gameActive = false;
        losses++;
        gameFrame.disableBoard();
        gameFrame.setStatus("Вы сдались!");
        gameFrame.setScoreInfo(wins, losses, draws);

        System.out.println("Игрок сдался");
    }

    private void exitGame() {
        clientNetwork.disconnect();
        gameFrame.hideWindow();

        if (onExit != null) {
            onExit.run();
        }

        System.out.println("Выход из игры");
    }

    private void saveGame() {
        // В реальной версии будет отправка запроса на сохранение
        gameFrame.setStatus("Сохранение игры...");
        System.out.println("Запрос на сохранение игры");

        // Имитация сохранения
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameFrame.showInfoDialog("Сохранение", "Игра успешно сохранена!");
            gameFrame.setStatus("Игра сохранена");
        });
    }

    private void loadGame() {
        // В реальной версии будет отправка запроса на загрузку
        gameFrame.setStatus("Загрузка игры...");
        System.out.println("Запрос на загрузку игры");

        // Имитация загрузки
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameFrame.showInfoDialog("Загрузка", "Игра успешно загружена!");
            startNewGame();
        });
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    public void showGameWindow() {
        gameFrame.showWindow();
    }

    public void hideGameWindow() {
        gameFrame.hideWindow();
    }
}
