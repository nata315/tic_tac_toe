package client.controllers;

import client.ClientNetwork;
import client.ui.GameWindow;
import shared.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;


public class GameController {
    private GameWindow gameFrame;
    private ClientNetwork clientNetwork;
    private Runnable onExit;

    // Данные игры
    private String currentGameId;
    private char playerSymbol = 'X';
    private boolean myTurn = false;
    private boolean gameActive = false;
    private boolean vsAI = true; // По умолчанию играем с ИИ

    private Timer gameStateTimer;
    private int moveCount = 0;

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
                showNewGameDialog();
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

        // Инициализируем окно игры
        gameFrame.setStatus("Подключение к серверу...");
        gameFrame.disableBoard();
    }

    public void startNewGame(boolean vsAI) {
        this.vsAI = vsAI;

        if (!gameActive) {
            clientNetwork.createNewGame(vsAI);
            gameFrame.setStatus("Создание новой игры...");
            gameFrame.disableBoard();
        }
    }

    private void handleMove(int row, int col) {
        if (!gameActive || !myTurn || currentGameId == null) {
            return;
        }

        // Локальная проверка (на всякий случай)
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            gameFrame.setStatus("Неверные координаты");
            return;
        }

        clientNetwork.sendMove(currentGameId, row, col);
        gameFrame.setStatus("Ход отправлен на сервер...");
        myTurn = false;
        gameFrame.disableBoard();
        moveCount++;
    }

    private void showNewGameDialog() {
        // Пока только против ИИ, так как сервер не поддерживает игру против игрока
        String[] options = {"Против ИИ", "Отмена"};
        int choice = JOptionPane.showOptionDialog(gameFrame,
                "Выберите тип игры (пока доступно только против ИИ):",
                "Новая игра",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            startNewGame(true);
        }
    }

    private void surrenderGame() {
        if (gameActive) {
            int confirm = JOptionPane.showConfirmDialog(gameFrame,
                    "Вы уверены, что хотите сдаться?\nТекущая игра будет завершена.",
                    "Подтверждение сдачи",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                endGame("Вы сдались!");
            }
        }
    }

    public void exitGame() {
        endGame("Выход из игры");
        gameFrame.hideWindow();

        if (onExit != null) {
            onExit.run();
        }
    }

    private void endGame(String message) {
        gameActive = false;
        stopGameStatePolling();
        gameFrame.disableBoard();
        gameFrame.setStatus(message);
        currentGameId = null;
        moveCount = 0;
    }

    private void saveGame() {
        if (gameActive && currentGameId != null) {
            gameFrame.showInfoDialog("Информация",
                    "Игра автоматически сохраняется на сервере.\n" +
                            "ID игры: " + currentGameId);
        } else {
            gameFrame.showInfoDialog("Информация", "Нет активной игры для сохранения");
        }
    }

    private void loadGame() {
        gameFrame.showInfoDialog("Функция в разработке",
                "Загрузка игр будет доступна в будущих версиях.\n" +
                        "Пожалуйста, создайте новую игру.");
    }

    public void handleServerMessage(GameMessage message) {
        SwingUtilities.invokeLater(() -> {
            String type = message.getType();
            System.out.println("Получено сообщение типа: " + type);

            try {
                switch (type) {
                    case "NEW_GAME_RESPONSE":
                        handleNewGameResponse(message);
                        break;

                    case "MOVE_RESPONSE":
                        handleMoveResponse(message);
                        break;

                    case "GAME_STATE_RESPONSE":
                        handleGameStateResponse(message);
                        break;

                    case "LOGIN_RESPONSE":
                    case "REGISTER_RESPONSE":
                        // Эти сообщения обрабатываются LoginController
                        break;

                    case "ERROR":
                        handleError(message);
                        break;

                    default:
                        System.out.println("Неизвестный тип сообщения: " + type);
                }
            } catch (Exception e) {
                System.err.println("Ошибка обработки сообщения: " + e.getMessage());
                e.printStackTrace();
                gameFrame.showErrorDialog("Ошибка",
                        "Ошибка обработки ответа сервера: " + e.getMessage());
            }
        });
    }

    private void handleNewGameResponse(GameMessage message) {
        try {
            currentGameId = (String) message.getData("gameId");
            GameState gameState = (GameState) message.getData("gameState");
            String player2 = (String) message.getData("player2");

            if (currentGameId == null || gameState == null) {
                throw new IllegalArgumentException("Некорректный ответ от сервера");
            }

            // Определяем наш символ
            String username = clientNetwork.getCurrentUsername();
            playerSymbol = username.equals(player2) ? 'O' : 'X';

            // Обновляем интерфейс
            updateGameState(gameState);
            gameActive = true;
            moveCount = 0;

            // Определяем, наш ли сейчас ход
            myTurn = (gameState.getCurrentPlayer() == playerSymbol);

            if (myTurn) {
                gameFrame.setStatus("Ваш ход! Вы играете за " + playerSymbol);
                gameFrame.enableBoard();
            } else {
                gameFrame.setStatus("Ожидание хода противника...");
                gameFrame.disableBoard();
                // Если игра против ИИ, и ИИ ходит первым - запускаем polling
                if (vsAI && "AI".equals(player2)) {
                    startGameStatePolling();
                }
            }

            gameFrame.setPlayerInfo(String.valueOf(playerSymbol));

            // Показываем информацию об игре
            String opponentInfo = "AI".equals(player2) ? "Искусственный интеллект" : player2;
            gameFrame.showInfoDialog("Игра началась",
                    "ID игры: " + currentGameId +
                            "\nВы играете за: " + playerSymbol +
                            "\nПротив: " + opponentInfo +
                            "\n\nПримечание: Сервер пока не реализует автоматический ход ИИ." +
                            "\nПосле вашего хода игра может не продолжаться.");

        } catch (Exception e) {
            gameFrame.showErrorDialog("Ошибка создания игры",
                    "Не удалось создать игру: " + e.getMessage());
            gameActive = false;
        }
    }

    private void handleMoveResponse(GameMessage message) {
        try {
            Boolean success = (Boolean) message.getData("success");

            if (success != null && success) {
                GameState gameState = (GameState) message.getData("gameState");

                if (gameState != null) {
                    updateGameState(gameState);

                    // Проверяем окончание игры
                    if (gameState.isGameOver()) {
                        String winner = gameState.getWinner();
                        if ("DRAW".equals(winner)) {
                            endGame("Ничья!");
                        } else if (winner != null && winner.equals(clientNetwork.getCurrentUsername())) {
                            endGame("Поздравляем! Вы победили!");
                        } else {
                            endGame("Игра окончена. Вы проиграли.");
                        }
                    } else {
                        // Обновляем, чей сейчас ход
                        myTurn = (gameState.getCurrentPlayer() == playerSymbol);

                        if (myTurn) {
                            gameFrame.setStatus("Ваш ход!");
                            gameFrame.enableBoard();
                        } else {
                            gameFrame.setStatus("Ход противника...");
                            gameFrame.disableBoard();

                            // Если игра против ИИ, запускаем polling для получения хода ИИ
                            if (vsAI) {
                                startGameStatePolling();
                            }
                        }
                    }
                }
            } else {
                String errorMsg = "Неизвестная ошибка";
                if (message.hasData("message")) {
                    errorMsg = (String) message.getData("message");
                }

                gameFrame.showErrorDialog("Ошибка хода", errorMsg);
                myTurn = true; // Возвращаем ход
                gameFrame.enableBoard();
            }

        } catch (Exception e) {
            gameFrame.showErrorDialog("Ошибка обработки хода",
                    "Ошибка: " + e.getMessage());
            myTurn = true;
            gameFrame.enableBoard();
        }
    }

    private void handleGameStateResponse(GameMessage message) {
        try {
            GameState gameState = (GameState) message.getData("gameState");
            Boolean gameOver = (Boolean) message.getData("gameOver");

            if (gameState != null) {
                updateGameState(gameState);

                if (gameOver != null && gameOver) {
                    String winner = (String) message.getData("winner");
                    if ("DRAW".equals(winner)) {
                        endGame("Ничья!");
                    } else if (winner != null && winner.equals(clientNetwork.getCurrentUsername())) {
                        endGame("Поздравляем! Вы победили!");
                    } else {
                        endGame("Игра окончена. Вы проиграли.");
                    }
                } else {
                    // Обновляем статус хода
                    myTurn = (gameState.getCurrentPlayer() == playerSymbol);

                    if (myTurn) {
                        stopGameStatePolling(); // Больше не нужно опрашивать
                        gameFrame.setStatus("Ваш ход!");
                        gameFrame.enableBoard();
                    } else {
                        gameFrame.setStatus("Ход противника...");
                        gameFrame.disableBoard();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки состояния игры: " + e.getMessage());
        }
    }

    private void handleError(GameMessage message) {
        String error = "Неизвестная ошибка";
        if (message.hasData("message")) {
            error = (String) message.getData("message");
        }

        gameFrame.setStatus("Ошибка: " + error);
        gameFrame.showErrorDialog("Ошибка сервера", error);

        // Если ошибка связана с игрой, завершаем ее
        if (gameActive) {
            endGame("Игра прервана из-за ошибки сервера");
        }
    }

    private void updateGameState(GameState gameState) {
        try {
            char[][] board = gameState.getBoard();
            String[][] displayBoard = new String[3][3];

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    char cell = board[i][j];
                    if (cell == 'X' || cell == 'O') {
                        displayBoard[i][j] = String.valueOf(cell);
                    } else {
                        displayBoard[i][j] = null;
                    }
                }
            }

            gameFrame.updateBoard(displayBoard);

        } catch (Exception e) {
            System.err.println("Ошибка обновления игрового поля: " + e.getMessage());
        }
    }

    private void startGameStatePolling() {
        stopGameStatePolling(); // Останавливаем предыдущий таймер

        if (!gameActive || currentGameId == null) {
            return;
        }

        gameStateTimer = new Timer(true);
        gameStateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameActive && currentGameId != null && !myTurn) {
                    System.out.println("Опрашиваем состояние игры: " + currentGameId);
                    clientNetwork.getGameState(currentGameId);
                } else {
                    stopGameStatePolling();
                }
            }
        }, 1000, 2000); // Начинаем через 1 сек, повторяем каждые 2 сек
    }

    private void stopGameStatePolling() {
        if (gameStateTimer != null) {
            gameStateTimer.cancel();
            gameStateTimer = null;
        }
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

    public void reset() {
        endGame("Игра сброшена");
        gameFrame.clearBoard();
        gameFrame.setStatus("Готов к новой игре");
    }
}
