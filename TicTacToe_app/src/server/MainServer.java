//Запуск сервера


package server;

import server.storage.GameStorage;

public class MainServer {
    public static void main(String[] args) {
        try {
            System.out.println("Сервер Крестики-Нолики запускается...");

            // Инициализация компонентов
            AuthManager authManager = new AuthManager();
            GameManager gameManager = new GameManager();

            // Инициализация хранилища (если используется)
            try {
                GameStorage gameStorage = new GameStorage();
                System.out.println("Хранилище игр инициализировано");
            } catch (Exception e) {
                System.out.println("Ошибка инициализации хранилища: " + e.getMessage());
            }

            // Запуск основного сервера
            int port = 12345;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("Неверный порт, используем порт по умолчанию: " + port);
                }
            }

            ServerCore server = new ServerCore(port, gameManager, authManager);
            System.out.println("Сервер запущен на порту " + port);
            server.start();

        } catch (Exception e) {
            System.err.println("Критическая ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}