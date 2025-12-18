package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerCore {
    private int port;
    private GameManager gameManager;
    private AuthManager authManager;
    private ExecutorService threadPool;
    private boolean running;

    public ServerCore(int port, GameManager gameManager, AuthManager authManager) {
        this.port = port;
        this.gameManager = gameManager;
        this.authManager = authManager;
        this.threadPool = Executors.newFixedThreadPool(10);
        this.running = false;
    }

    public void start() throws IOException {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое подключение: " + clientSocket.getInetAddress());

                // Исправленный конструктор - передаем все необходимые параметры
                ConnectionHandler handler = new ConnectionHandler(clientSocket, gameManager, authManager);
                threadPool.execute(handler);
            }
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        System.out.println("Сервер остановлен");
    }

    public boolean isRunning() {
        return running;
    }
}