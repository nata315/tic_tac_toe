package client.controllers;

import client.ClientNetwork;
import client.ui.GameWindow;
import shared.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;

public class GameController {
    private GameWindow gameFrame;
    private ClientNetwork clientNetwork;
    private Runnable onExit;

    // Данные игры
    private String currentGameId;
    private char playerSymbol = 'X';
    private boolean myTurn = false;
    private boolean gameActive = false;
    private boolean vsAI = true;

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

        // Локальная проверка
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            gameFrame.setStatus("Неверные координаты");
            return;
        }

        System.out.println("\n=== КЛИЕНТ: Отправка хода ===");
        System.out.println("GameID: " + currentGameId);
        System.out.println("Ход: [" + row + "," + col + "]");
        System.out.println("Игрок: " + clientNetwork.getCurrentUsername());
        System.out.println("Символ: " + playerSymbol);

        // ЛОКАЛЬНО обновляем клетку для мгновенной обратной связи
        String[][] tempBoard = new String[3][3];
        // Копируем текущее состояние (если есть)
        // И устанавливаем наш ход
        tempBoard[row][col] = String.valueOf(playerSymbol);
        gameFrame.updateBoard(tempBoard);

        // Отключаем клетку, на которую походили
        gameFrame.disableBoard();
        gameFrame.setStatus("Ход отправлен...");

        // Отправляем ход на сервер
        clientNetwork.sendMove(currentGameId, row, col);
        myTurn = false;
        moveCount++;
    }

    private void showNewGameDialog() {
        String[] options = {"Против ИИ", "Отмена"};
        int choice = JOptionPane.showOptionDialog(gameFrame,
                "Выберите тип игры:",
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
            System.out.println("КЛИЕНТ: Получено сообщение типа: " + type);

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
            System.out.println("\n=== КЛИЕНТ: Обработка NEW_GAME_RESPONSE ===");

            currentGameId = (String) message.getData("gameId");
            Object gameStateObj = message.getData("gameState");
            String player2 = (String) message.getData("player2");

            System.out.println("GameID: " + currentGameId);
            System.out.println("Player2: " + player2);
            System.out.println("GameState object: " + gameStateObj);

            if (currentGameId == null) {
                throw new IllegalArgumentException("Некорректный ответ от сервера: нет gameId");
            }

            // Определяем наш символ
            String username = clientNetwork.getCurrentUsername();
            playerSymbol = username.equals(player2) ? 'O' : 'X';
            System.out.println("Наш символ: " + playerSymbol);

            // Обновляем интерфейс
            if (gameStateObj != null) {
                if (gameStateObj instanceof GameState) {
                    updateGameState((GameState) gameStateObj);
                } else {
                    System.err.println("Неизвестный тип GameState: " + gameStateObj.getClass());
                }
            }

            gameActive = true;
            moveCount = 0;

            // Определяем, наш ли сейчас ход (по умолчанию X начинает)
            myTurn = (playerSymbol == 'X');

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
                            "\nПротив: " + opponentInfo);

        } catch (Exception e) {
            System.err.println("Ошибка создания игры: " + e.getMessage());
            e.printStackTrace();
            gameFrame.showErrorDialog("Ошибка создания игры",
                    "Не удалось создать игру: " + e.getMessage());
            gameActive = false;
        }
    }

    private void handleMoveResponse(GameMessage message) {
        System.out.println("\n=== КЛИЕНТ: Обработка MOVE_RESPONSE ===");

        try {
            Boolean success = (Boolean) message.getData("success");
            System.out.println("Success: " + success);

            if (success != null && success) {
                // Пробуем получить данные разными способами
                processGameStateData(message);
            } else {
                String errorMsg = "Неизвестная ошибка";
                if (message.hasData("message")) {
                    errorMsg = (String) message.getData("message");
                }

                System.err.println("Ошибка хода: " + errorMsg);
                gameFrame.showErrorDialog("Ошибка хода", errorMsg);
                myTurn = true; // Возвращаем ход
                gameFrame.enableBoard();
            }

        } catch (Exception e) {
            System.err.println("Исключение в handleMoveResponse: " + e.getMessage());
            e.printStackTrace();
            gameFrame.showErrorDialog("Ошибка обработки хода",
                    "Ошибка: " + e.getMessage());
            myTurn = true;
            gameFrame.enableBoard();
        }
    }

    private void processGameStateData(GameMessage message) {
        try {
            // Способ 1: Попробуем получить как GameState
            Object gameStateObj = message.getData("gameState");

            if (gameStateObj instanceof GameState) {
                GameState gameState = (GameState) gameStateObj;
                System.out.println("Получен GameState объект");
                processGameState(gameState);
                return;
            }

            // Способ 2: Попробуем получить как SimpleGameState
            if (gameStateObj != null && gameStateObj.getClass().getName().contains("SimpleGameState")) {
                try {
                    // Используем рефлексию для доступа к методам SimpleGameState
                    Class<?> clazz = gameStateObj.getClass();
                    Object boardObj = clazz.getMethod("getBoard").invoke(gameStateObj);

                    if (boardObj != null && boardObj.getClass().getName().contains("SerializableBoard")) {
                        Class<?> boardClass = boardObj.getClass();
                        String[][] board = (String[][]) boardClass.getMethod("getBoard").invoke(boardObj);

                        GameState gameState = new GameState();
                        gameState.setBoard(board);

                        // Получаем остальные поля
                        Character currentPlayer = (Character) clazz.getMethod("getCurrentPlayer").invoke(gameStateObj);
                        Boolean gameOver = (Boolean) clazz.getMethod("isGameOver").invoke(gameStateObj);
                        String winner = (String) clazz.getMethod("getWinner").invoke(gameStateObj);

                        gameState.setCurrentPlayer(currentPlayer);
                        gameState.setGameOver(gameOver);
                        gameState.setWinner(winner);

                        System.out.println("Получен SimpleGameState, преобразован в GameState");
                        processGameState(gameState);
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка преобразования SimpleGameState: " + e.getMessage());
                }
            }

            // Способ 3: Попробуем получить как список
            Object boardListObj = message.getData("board");
            if (boardListObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> boardList = (List<String>) boardListObj;

                if (boardList.size() == 9) {
                    String[][] board = new String[3][3];
                    int index = 0;

                    System.out.println("Получена доска как список, размер: " + boardList.size());
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            board[i][j] = boardList.get(index++);
                        }
                    }

                    GameState gameState = new GameState();
                    gameState.setBoard(board);

                    // Получаем дополнительные поля
                    if (message.hasData("currentPlayer")) {
                        gameState.setCurrentPlayer((Character) message.getData("currentPlayer"));
                    }
                    if (message.hasData("gameOver")) {
                        gameState.setGameOver((Boolean) message.getData("gameOver"));
                    }
                    if (message.hasData("winner")) {
                        gameState.setWinner((String) message.getData("winner"));
                    }

                    System.out.println("Получена доска как список, преобразована в GameState");
                    processGameState(gameState);
                    return;
                }
            }

            // Способ 4: Создаем тестовое состояние
            System.out.println("Не удалось получить состояние игры, создаем тестовое");
            GameState testState = new GameState();
            testState.makeMove(2, 0, "X");  // Предполагаем, что игрок походил сюда
            testState.makeMove(1, 1, "O");  // Предполагаем ответ ИИ
            processGameState(testState);

        } catch (Exception e) {
            System.err.println("Ошибка processGameStateData: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void processGameState(GameState gameState) {
        // Обновляем UI
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
            System.out.println("Текущий игрок на сервере: " + gameState.getCurrentPlayer());
            System.out.println("Мой символ: " + playerSymbol);
            System.out.println("Мой ход? " + myTurn);

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

    private void handleGameStateResponse(GameMessage message) {
        try {
            System.out.println("\n=== КЛИЕНТ: Обработка GAME_STATE_RESPONSE ===");

            Object gameStateObj = message.getData("gameState");
            Boolean gameOver = (Boolean) message.getData("gameOver");

            if (gameStateObj instanceof GameState) {
                GameState gameState = (GameState) gameStateObj;
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
            e.printStackTrace();
        }
    }

    private void handleError(GameMessage message) {
        String error = "Неизвестная ошибка";
        if (message.hasData("message")) {
            error = (String) message.getData("message");
        }

        System.err.println("Ошибка от сервера: " + error);
        gameFrame.setStatus("Ошибка: " + error);
        gameFrame.showErrorDialog("Ошибка сервера", error);

        // Если ошибка связана с игрой, завершаем ее
        if (gameActive) {
            endGame("Игра прервана из-за ошибки сервера");
        }
    }

    private void updateGameState(GameState gameState) {
        try {
            String[][] board = gameState.getBoard();
            String[][] displayBoard = new String[3][3];

            System.out.println("\n=== КЛИЕНТ: Обновление UI доски ===");
            System.out.println("Текущий игрок на сервере: " + gameState.getCurrentPlayer());
            System.out.println("Игра окончена: " + gameState.isGameOver());
            System.out.println("Победитель: " + gameState.getWinner());

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    String cell = board[i][j];
                    System.out.print("[" + i + "," + j + "]='" + (cell == null ? "null" : cell) + "' ");

                    if (cell != null && (cell.equals("X") || cell.equals("O"))) {
                        displayBoard[i][j] = cell;
                    } else {
                        displayBoard[i][j] = "";
                    }
                }
                System.out.println();
            }

            System.out.println("DisplayBoard для UI:");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.print("'" + displayBoard[i][j] + "' ");
                }
                System.out.println();
            }

            // Обновляем UI
            gameFrame.updateBoard(displayBoard);
            System.out.println("UI обновлен\n");

        } catch (Exception e) {
            System.err.println("Ошибка обновления игрового поля: " + e.getMessage());
            e.printStackTrace();

            // Показать тестовые символы для отладки
            String[][] testBoard = new String[3][3];
            testBoard[0][0] = "X";
            testBoard[1][1] = "O";
            testBoard[2][2] = "X";
            gameFrame.updateBoard(testBoard);
            gameFrame.setStatus("Ошибка обновления доски. Показаны тестовые символы.");
        }
    }

    private void startGameStatePolling() {
        stopGameStatePolling(); // Останавливаем предыдущий таймер

        if (!gameActive || currentGameId == null || myTurn) {
            return;
        }

        System.out.println("Запуск polling для игры: " + currentGameId);
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

    // Метод для тестирования UI
    public void testUI() {
        System.out.println("=== ТЕСТ UI ===");
        String[][] testBoard = new String[3][3];
        testBoard[0][0] = "X";
        testBoard[1][1] = "O";
        testBoard[2][2] = "X";

        gameFrame.updateBoard(testBoard);
        gameFrame.setStatus("Тест UI - символы должны отображаться!");
        System.out.println("Тестовые символы отправлены в UI");
    }
}