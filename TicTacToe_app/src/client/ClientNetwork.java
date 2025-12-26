package client;

import shared.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

//Сетевое подключение к серверу
public class ClientNetwork {
    // сетевые компоненты
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ExecutorService executorService;

    // обработчики событий
    private Consumer<GameMessage> messageHandler;
    private boolean connected;
    private String currentUsername;

    // конструктор
    public ClientNetwork(String host, int port) throws IOException {
        try {
            // создание входных точек, потоков и интерфейс для их управления
            this.socket = new Socket(host, port);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.executorService = Executors.newSingleThreadExecutor();
            this.connected = true;
            System.out.println("Подключено к серверу " + host + ":" + port);
        } catch (ConnectException e) {
            throw new IOException("Не удалось подключиться к серверу. Убедитесь, что сервер запущен.");
        }
    }

    // метод для запуска потока для приема сообщений от сервера
    public void startListening(Consumer<GameMessage> handler) {
        this.messageHandler = handler;
        executorService.submit(() -> {
            try {
                // пока есть соединение
                while (connected) {
                    // читает входной поток и обрабатывает его
                    Object obj = inputStream.readObject();
                    if (obj instanceof GameMessage) {
                        GameMessage message = (GameMessage) obj;
                        if (messageHandler != null) {
                            messageHandler.accept(message);
                        }
                    } else {
                        System.err.println("Получен неизвестный объект: " + obj.getClass());
                    }
                }
            } catch (EOFException e) {
                System.out.println("Соединение закрыто сервером");
                disconnect();
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    System.err.println("Ошибка при чтении: " + e.getMessage());
                    disconnect();
                }
            }
        });
    }

    // метод отправляющий сообщения
    public void sendMessage(GameMessage message) {
        // при отсутствии связи
        if (!connected) {
            System.err.println("Попытка отправить сообщение при разорванном соединении");
            return;
        }

        try {
            outputStream.writeObject(message);
            outputStream.flush();
            System.out.println("Отправлено сообщение типа: " + message.getType());
        } catch (IOException e) {
            System.err.println("Ошибка при отправке: " + e.getMessage());
            disconnect();
        }
    }

    // отправка запроса на вход пользователя
    public boolean login(String username, String password) {
        try {
            GameMessage message = new GameMessage("LOGIN");
            message.addData("username", username);
            message.addData("password", password);

            sendMessage(message);
            currentUsername = username;
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при входе: " + e.getMessage());
            return false;
        }
    }

    // отправка запроса на регистрацию пользователя
    public boolean register(String username, String password) {
        try {
            GameMessage message = new GameMessage("REGISTER");
            message.addData("username", username);
            message.addData("password", password);

            sendMessage(message);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка при регистрации: " + e.getMessage());
            return false;
        }
    }

    // отправка запроса на создание новой игры
    public void createNewGame(boolean vsAI) {
        try {
            GameMessage message = new GameMessage("NEW_GAME");
            message.addData("player", currentUsername);
            message.addData("vsAI", vsAI);

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при создании игры: " + e.getMessage());
        }
    }

    // отправка запроса совершение хода
    public void sendMove(String gameId, int row, int col) {
        try {
            System.out.println("\n=== КЛИЕНТ: Отправка хода ===");
            System.out.println("GameID: " + gameId);
            System.out.println("Ход: [" + row + "," + col + "]");
            System.out.println("Пользователь: " + currentUsername);

            GameMessage message = new GameMessage("MOVE");
            message.addData("gameId", gameId);
            message.addData("player", currentUsername);
            message.addData("move", new Move(row, col));

            // проверка сериализацию Move
            Move m = new Move(row, col);
            System.out.println("Создан Move: row=" + m.getRow() + ", col=" + m.getCol());

            sendMessage(message);

        } catch (Exception e) {
            System.err.println("Ошибка при отправке хода: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // запрос текущего состояния игры
    public void getGameState(String gameId) {
        try {
            GameMessage message = new GameMessage("GET_GAME_STATE");
            message.addData("gameId", gameId);

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при запросе состояния: " + e.getMessage());
        }
    }

    // завершение работы с сервером
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // ошибка игнорируется при закрытии
        }

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Отключено от сервера");
    }

    // отправка запроса на сохранение игры
    public void saveGame(String gameId) {
        try {
            GameMessage message = new GameMessage("SAVE_GAME");
            message.addData("gameId", gameId);
            message.addData("player", currentUsername);

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении игры: " + e.getMessage());
        }
    }

    // отправка запроса на загрузку игры
    public void loadSavedGame(String gameId) {
        try {
            GameMessage message = new GameMessage("LOAD_GAME");
            message.addData("gameId", gameId);
            message.addData("player", currentUsername);

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке игры: " + e.getMessage());
        }
    }

    // отправка запроса на получение сохраненных игр
    public void getSavedGames() {
        try {
            GameMessage message = new GameMessage("GET_SAVED_GAMES");
            message.addData("player", currentUsername);

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при получении списка игр: " + e.getMessage());
        }
    }

    // проверка соединения
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    // метод отвечающий за получение имени пользователя
    public String getCurrentUsername() {
        return currentUsername;
    }
}
