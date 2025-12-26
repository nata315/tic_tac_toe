package client.controllers;

import client.ClientNetwork;
import client.ui.GameWindow;
import shared.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;

// класс отвечающий за игровой контроллер
public class GameController {
    // ссылки на компоненты игры
    private GameWindow gameFrame;
    private ClientNetwork clientNetwork;
    private Runnable onExit;

    // данные игры
    private String currentGameId;
    private char playerSymbol = 'X';
    private boolean myTurn = false;
    private boolean gameActive = false;
    private boolean vsAI = true;

    // сетевые компоненты
    private Timer gameStateTimer;
    private int moveCount = 0;

    // счетчик итогов игры
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;

    // данные сохраненных игр
    private boolean isSavedGame = false;
    private List<String> savedGameIds = new ArrayList<>();

    // конструктор
    public GameController(GameWindow gameFrame, ClientNetwork clientNetwork) {
        this.gameFrame = gameFrame;
        this.clientNetwork = clientNetwork;

        // настройка слушателей событий
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

        // Инициализация окна игры
        gameFrame.setStatus("Подключение к серверу...");
        gameFrame.disableBoard();

        // Инициализация статистики в UI
        updateScoreDisplay();
    }

    // метод, который запускает новую игру
    public void startNewGame(boolean vsAI) {
        // Завершение текущей игры перед началом новой
        if (gameActive && currentGameId != null) {
            endGame("Завершено для новой игры");
        }

        // Сброс состояния контроллера
        resetLocalState();

        this.vsAI = vsAI;
        this.isSavedGame = false;

        // Очистка интерфейса
        gameFrame.clearBoard();
        gameFrame.setStatus("Создание новой игры...");
        gameFrame.disableBoard();

        // Отправка запроса на сервер для начала новой игры
        clientNetwork.createNewGame(vsAI);
    }

    // метод, который сбрасывает локальное состояние контроллера
    private void resetLocalState() {
        stopGameStatePolling();
        currentGameId = null;
        playerSymbol = 'X';
        myTurn = false;
        gameActive = false;
        moveCount = 0;
    }

    // метод, отвечающий за обработку хода игрока
    private void handleMove(int row, int col) {
        // Проверка возможности хода
        if (!gameActive || !myTurn || currentGameId == null) {
            return;
        }

        // Проверка валидности координат
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            gameFrame.setStatus("Неверные координаты");
            return;
        }

        System.out.println("\n=== КЛИЕНТ: Отправка хода ===");
        System.out.println("GameID: " + currentGameId);
        System.out.println("Ход: [" + row + "," + col + "]");
        System.out.println("Игрок: " + clientNetwork.getCurrentUsername());
        System.out.println("Символ: " + playerSymbol);

        // Локальное обновление клетки для мгновенной обратной связи
        String[][] tempBoard = new String[3][3];
        tempBoard[row][col] = String.valueOf(playerSymbol);
        gameFrame.updateBoard(tempBoard);

        // Блокировка клетки и отправление текущего статуса
        gameFrame.disableBoard();
        gameFrame.setStatus("Ход отправлен...");

        // Отправление ход на сервер
        clientNetwork.sendMove(currentGameId, row, col);
        myTurn = false;
        moveCount++;
    }

    // метод, показывающий диалоговое окно для создания новой игры
    private void showNewGameDialog() {
        String[] options = {"Да", "Нет"};
        int choice = JOptionPane.showOptionDialog(gameFrame,
                "Начать новую игру:",
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

    // метод отвечающий за то, что происходит, когда игрок решил сдаться
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

    // метод отвечающий за выход из игры
    public void exitGame() {
        endGame("Выход из игры");
        gameFrame.hideWindow();

        if (onExit != null) {
            onExit.run(); // возврат в LoginWindow
        }
    }

    // метод отвечающий за завершение ткущей игровой сессии
    private void endGame(String message) {
        gameActive = false;
        stopGameStatePolling();
        gameFrame.disableBoard();
        gameFrame.clearBoard();
        gameFrame.setStatus(message);
        currentGameId = null;
        moveCount = 0;
    }

    // метод отвечающий за сохранение текущей игры
    private void saveGame() {
        if (gameActive && currentGameId != null) {
            gameFrame.showInfoDialog("Информация",
                    "Сохранение игры.\n" +
                            "ID игры: " + currentGameId);
            clientNetwork.saveGame(currentGameId);

        } else {
            gameFrame.showInfoDialog("Информация", "Нет активной игры для сохранения");
        }
    }

    // метод отвечающий за загрузку игры
    private void loadGame() {
        // запрос списка сохраненных игр у сервера
        clientNetwork.getSavedGames();
        gameFrame.setStatus("Загрузка списка сохраненных игр...");
    }

     // диалог выбора игры для загрузки
    public void showLoadGameDialog(List<String> gameIds) {
        if (gameIds == null || gameIds.isEmpty()) {
            gameFrame.showInfoDialog("Нет сохраненных игр",
                    "У вас нет сохраненных игр. Начните новую игру!");
            return;
        }

        // Создание диалога выбора игры
        String[] options = gameIds.toArray(new String[0]);
        String selectedGame = (String) JOptionPane.showInputDialog(
                gameFrame,
                "Выберите игру для загрузки:",
                "Загрузка игры",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selectedGame != null) {
            isSavedGame = true;
            clientNetwork.loadSavedGame(selectedGame);
            gameFrame.setStatus("Загрузка игры " + selectedGame + "...");
        }
    }

    // основной обработчик сообщений от сервера
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

                    case "SAVE_GAME_RESPONSE":
                        handleSaveGameResponse(message);
                        break;

                    case "LOAD_GAME_RESPONSE":
                        handleLoadGameResponse(message);
                        break;

                    case "GET_SAVED_GAMES_RESPONSE":
                        handleGetSavedGamesResponse(message);
                        break;

                    case "LOGIN_RESPONSE":
                    case "REGISTER_RESPONSE":
                        // эти сообщения обрабатываются LoginController
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

    // обработка ответа на создание новой игры
    private void handleNewGameResponse(GameMessage message) {
        try {
            System.out.println("\n=== КЛИЕНТ: Обработка NEW_GAME_RESPONSE ===");

            // получение данных новой игры от сервера
            currentGameId = (String) message.getData("gameId");
            Object gameStateObj = message.getData("gameState");
            String player2 = (String) message.getData("player2");

            System.out.println("GameID: " + currentGameId);
            System.out.println("Player2: " + player2);
            System.out.println("GameState object: " + gameStateObj);

            if (currentGameId == null) {
                throw new IllegalArgumentException("Некорректный ответ от сервера: нет gameId");
            }

            // определение символа игрока
            String username = clientNetwork.getCurrentUsername();
            playerSymbol = username.equals(player2) ? 'O' : 'X';
            System.out.println("Наш символ: " + playerSymbol);

            // обновление интерфейса
            if (gameStateObj != null) {
                if (gameStateObj instanceof GameState) {
                    updateGameState((GameState) gameStateObj);
                } else {
                    System.err.println("Неизвестный тип GameState: " + gameStateObj.getClass());
                }
            }

            // активация игры
            gameActive = true;
            moveCount = 0;

            // Определение сейчас ли ход игрока
            myTurn = (playerSymbol == 'X');

            if (myTurn) {
                gameFrame.setStatus("Ваш ход! Вы играете за " + playerSymbol);
                gameFrame.enableBoard();
            } else {
                gameFrame.setStatus("Ожидание хода противника...");
                gameFrame.disableBoard();
                if (vsAI && "AI".equals(player2)) {
                    startGameStatePolling();
                }
            }

            // установка информации об игроке
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

    // Обработка ответа на ход игрока
    private void handleMoveResponse(GameMessage message) {
        System.out.println("\n=== КЛИЕНТ: Обработка MOVE_RESPONSE ===");

        try {
            Boolean success = (Boolean) message.getData("success");
            System.out.println("Success: " + success);

            if (success != null && success) {
                // Успешный ход - обновляем состояние игры
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

    // метод отвечающий за извлечение и обработку состояния игры из сообщения
    private void processGameStateData(GameMessage message) {
        try {
            // Получение, как GameState объекта
            Object gameStateObj = message.getData("gameState");

            if (gameStateObj instanceof GameState) {
                GameState gameState = (GameState) gameStateObj;
                System.out.println("Получен GameState объект");
                processGameState(gameState);
                return;
            }

            // Получение, как SimpleGameState
            if (gameStateObj != null && gameStateObj.getClass().getName().contains("SimpleGameState")) {
                try {
                    // Использование рефлексии для доступа к методам SimpleGameState
                    Class<?> clazz = gameStateObj.getClass();
                    Object boardObj = clazz.getMethod("getBoard").invoke(gameStateObj);

                    if (boardObj != null && boardObj.getClass().getName().contains("SerializableBoard")) {
                        Class<?> boardClass = boardObj.getClass();
                        String[][] board = (String[][]) boardClass.getMethod("getBoard").invoke(boardObj);

                        GameState gameState = new GameState();
                        gameState.setBoard(board);

                        // Получение остальных полей
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

            //  Получение, как списка
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

                    // Получение дополнительных полей
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

            // Создание тестового состояния
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

    // метод отвечающий за обработку состояния игры после хода
    private void processGameState(GameState gameState) {
        // обновление интерфейса
        updateGameState(gameState);

        // проверка окончания игры
        if (gameState.isGameOver()) {
            String winner = gameState.getWinner();
            if ("DRAW".equals(winner)) {
                draws++;
                endGame("Ничья!");
                gameFrame.showInfoDialog("Ничья!",
                        "Нет победителя.");
            } else if (winner != null && winner.equals(clientNetwork.getCurrentUsername())) {
                wins++;
                endGame("Поздравляем! Вы победили!");
                gameFrame.showInfoDialog("Поздравляем!",
                        "Вы победили!");
            } else {
                losses++;
                endGame("Игра окончена. Вы проиграли.");
                gameFrame.showInfoDialog("Игра окончена.",
                        "Вы проиграли!");
            }

            updateScoreDisplay();
        } else {
            // обновление хода
            myTurn = (gameState.getCurrentPlayer() == playerSymbol);
            System.out.println("Текущий игрок на сервере: " + gameState.getCurrentPlayer());
            System.out.println("Мой символ: " + playerSymbol);
            System.out.println("Мой ход? " + myTurn);

            // проверка чей сейчас ход
            if (myTurn) {
                gameFrame.setStatus("Ваш ход!");
                gameFrame.enableBoard();
            } else {
                gameFrame.setStatus("Ход противника...");
                gameFrame.disableBoard();

                // запуск опроса для получения хода ИИ
                if (vsAI) {
                    startGameStatePolling();
                }
            }
        }
    }

    // метод отвечающий за обновление статистики игрока
    private void updateScoreDisplay() {
        gameFrame.setScoreInfo(wins, losses, draws);
    }

    // метод отвечающий за обработку ответа на состояние игры
    private void handleGameStateResponse(GameMessage message) {
        try {
            System.out.println("\n=== КЛИЕНТ: Обработка GAME_STATE_RESPONSE ===");

            Object gameStateObj = message.getData("gameState");
            Boolean gameOver = (Boolean) message.getData("gameOver");

            if (gameStateObj instanceof GameState) {
                GameState gameState = (GameState) gameStateObj;
                updateGameState(gameState);

                // если игра закончилась
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
                    // обновление статуса хода
                    myTurn = (gameState.getCurrentPlayer() == playerSymbol);

                    if (myTurn) {
                        stopGameStatePolling();
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

    // метод отвечающий за обработку ошибки
    private void handleError(GameMessage message) {
        String error = "Неизвестная ошибка";
        if (message.hasData("message")) {
            error = (String) message.getData("message");
        }

        System.err.println("Ошибка от сервера: " + error);
        gameFrame.setStatus("Ошибка: " + error);
        gameFrame.showErrorDialog("Ошибка сервера", error);

        if (gameActive) {
            endGame("Игра прервана из-за ошибки сервера");
        }
    }

    // метод отвечающий за обновление состояния игры
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

            // Обновление интерфейса
            gameFrame.updateBoard(displayBoard);
            System.out.println("UI обновлен\n");

        } catch (Exception e) {
            System.err.println("Ошибка обновления игрового поля: " + e.getMessage());
            e.printStackTrace();

            // Тестовые символы для отладки
            String[][] testBoard = new String[3][3];
            testBoard[0][0] = "X";
            testBoard[1][1] = "O";
            testBoard[2][2] = "X";
            gameFrame.updateBoard(testBoard);
            gameFrame.setStatus("Ошибка обновления доски. Показаны тестовые символы.");
        }
    }

    // периодический опрос состояния игры
    private void startGameStatePolling() {
        stopGameStatePolling(); // Остановка предыдущего таймера

        if (!gameActive || currentGameId == null || myTurn) {
            return;
        }

        System.out.println("Запуск polling для игры: " + currentGameId);
        // новый таймер
        gameStateTimer = new Timer(true);
        gameStateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // если игра активна и сейчас не ход игрока
                if (gameActive && currentGameId != null && !myTurn) {
                    System.out.println("Опрашиваем состояние игры: " + currentGameId);
                    clientNetwork.getGameState(currentGameId);
                } else {
                    stopGameStatePolling();
                }
            }
        }, 1000, 2000); // Начинаем через 1 сек, повторяем каждые 2 сек
    }

    // прекращение периодического опроса состояния игры
    private void stopGameStatePolling() {
        if (gameStateTimer != null) {
            gameStateTimer.cancel();
            gameStateTimer = null;
        }
    }

    // метод отвечающий за выход игры
    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    // метод показывающий игровое окно
    public void showGameWindow() {
        gameFrame.showWindow();
    }

    // метод скрывающий игровое окно
    public void hideGameWindow() {
        gameFrame.hideWindow();
    }

    // метод сбрасывающий текущую игру
    public void reset() {
        endGame("Игра сброшена");
        gameFrame.clearBoard();
        gameFrame.setStatus("Готов к новой игре");
    }

    // метод отвечающий за ответ на сохранение игры
    private void handleSaveGameResponse(GameMessage message) {
        Boolean success = (Boolean) message.getData("success");
        if (success != null && success) {
            String gameId = (String) message.getData("gameId");
            gameFrame.showInfoDialog("Сохранение игры",
                    "Игра успешно сохранена!\nID игры: " + gameId);
        } else {
            String error = (String) message.getData("message");
            gameFrame.showErrorDialog("Ошибка сохранения",
                    "Не удалось сохранить игру: " + error);
        }
    }

    // метод отвечающий за обработку ответа на загрузку
    private void handleLoadGameResponse(GameMessage message) {
        Boolean success = (Boolean) message.getData("success");

        if (success != null && success) {
            currentGameId = (String) message.getData("gameId");
            Object gameStateObj = message.getData("gameState");
            String player2 = (String) message.getData("player2");

            // обновление состояния игры
            if (gameStateObj instanceof GameState) {
                GameState gameState = (GameState) gameStateObj;

                // определение символа
                String username = clientNetwork.getCurrentUsername();
                playerSymbol = username.equals(player2) ? 'O' : 'X';

                // обновление интерфейса
                updateGameState(gameState);
                gameFrame.setPlayerInfo(String.valueOf(playerSymbol));

                gameActive = true;
                isSavedGame = true;

                // определение, чей сейчас ход
                myTurn = (gameState.getCurrentPlayer() == playerSymbol);

                if (myTurn) {
                    gameFrame.setStatus("Ваш ход! (Загруженная игра)");
                    gameFrame.enableBoard();
                } else {
                    gameFrame.setStatus("Ход противника... (Загруженная игра)");
                    gameFrame.disableBoard();
                }

                gameFrame.showInfoDialog("Игра загружена",
                        "Игра успешно загружена!\nID: " + currentGameId +
                                "\nВы играете за: " + playerSymbol);
            }
        } else {
            String error = (String) message.getData("message");
            gameFrame.showErrorDialog("Ошибка загрузки",
                    "Не удалось загрузить игру: " + error);
        }
    }

    //  метод отвечающий за ответ на получение сохраненных игр
    private void handleGetSavedGamesResponse(GameMessage message) {
        @SuppressWarnings("unchecked")
        List<String> gameIds = (List<String>) message.getData("gameIds");

        if (gameIds != null) {
            this.savedGameIds = gameIds;
            showLoadGameDialog(gameIds);
        } else {
            gameFrame.showInfoDialog("Нет сохраненных игр",
                    "У вас нет сохраненных игр.");
        }
    }
}