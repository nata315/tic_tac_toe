//Сетевое подключение к серверу


package client;

import shared.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ClientNetwork {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ExecutorService executorService;
    private Consumer<GameMessage> messageHandler;
    private boolean connected;
    private String currentUsername;

    public ClientNetwork(String host, int port) throws IOException {
        try {
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

    public void startListening(Consumer<GameMessage> handler) {
        this.messageHandler = handler;
        executorService.submit(() -> {
            try {
                while (connected) {
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

    public void sendMessage(GameMessage message) {
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

    // Отправляем сообщение и ждем ответ (синхронно)
    public GameMessage sendAndWait(GameMessage message, int timeoutMs) {
        sendMessage(message);

        // В реальной реализации здесь была бы сложная логика ожидания ответа
        // Но для текущего сервера сделаем просто
        try {
            Thread.sleep(100); // Небольшая задержка для имитации сетевого обмена
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return null; // В этой реализации ответ придет через messageHandler
    }

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

    public void sendMove(String gameId, int row, int col) {
        try {
            GameMessage message = new GameMessage("MOVE");
            message.addData("gameId", gameId);
            message.addData("player", currentUsername);
            message.addData("move", new Move(row, col));

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при отправке хода: " + e.getMessage());
        }
    }

    public void getGameState(String gameId) {
        try {
            GameMessage message = new GameMessage("GET_GAME_STATE");
            message.addData("gameId", gameId);

            sendMessage(message);
        } catch (Exception e) {
            System.err.println("Ошибка при запросе состояния: " + e.getMessage());
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Игнорируем ошибку при закрытии
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

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
