package client;

import java.io.IOException;

public class ClientNetwork {
    private String serverHost;
    private int serverPort;
    private boolean connected;

    public ClientNetwork(String host, int port) throws IOException {
        this.serverHost = host;
        this.serverPort = port;
        this.connected = true;
        System.out.println("Подключение к серверу " + host + ":" + port);
    }

    public void startListening(MessageHandler handler) {
        System.out.println("Начало прослушивания сообщений от сервера");
        // В реальной версии здесь будет поток для получения сообщений
    }

    public void sendMessage(Object message) {
        System.out.println("Отправка сообщения: " + message);
        // В реальной версии здесь будет отправка по сети
    }

    public boolean login(String username, String password) {
        System.out.println("Попытка входа: " + username);
        // В реальной версии здесь будет отправка запроса на сервер
        return true;
    }

    public void sendMove(int row, int col, String player) {
        System.out.println("Ход: " + player + " на [" + row + "," + col + "]");
        // В реальной версии здесь будет отправка хода на сервер
    }

    public void disconnect() {
        connected = false;
        System.out.println("Отключение от сервера");
    }

    public boolean isConnected() {
        return connected;
    }

    public interface MessageHandler {
        void handleMessage(Object message);
    }
}
