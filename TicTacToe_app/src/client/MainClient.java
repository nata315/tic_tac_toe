package client;

import client.controllers.*;
import client.ui.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private ClientNetwork clientNetwork;
    private LoginWindow loginFrame;
    private GameWindow gameFrame;
    private LoginController loginController;
    private GameController gameController;

    public static void main(String[] args) {
        // Устанавливаем Look and Feel (стиль графического интерфейса)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Запускаем в EDT (специальный поток для обработки событий интерфейса)
        SwingUtilities.invokeLater(() -> {
            new MainClient().start();
        });
    }

    // запуск клиента
    public void start() {
        try {
            System.out.println("=== Запуск клиента Крестики-Нолики ===");
            System.out.println("Сервер: " + SERVER_HOST + ":" + SERVER_PORT);

            // попытка подключения к серверу
            clientNetwork = new ClientNetwork(SERVER_HOST, SERVER_PORT);

            // создание интерфейса
            loginFrame = new LoginWindow();
            gameFrame = new GameWindow();

            // создание контроллеров
            loginController = new LoginController(loginFrame, clientNetwork);
            gameController = new GameController(gameFrame, clientNetwork);

            // настройка обработки сетевых сообщений
            clientNetwork.startListening(message -> {
                System.out.println("Получено сетевое сообщение типа: " + message.getType());

                // Перенаправление сообщений контроллерам
                String type = message.getType();

                if (type == null) {
                    System.err.println("Получено сообщение без типа!");
                    return;
                }

                if (type.contains("LOGIN") || type.contains("REGISTER")) {
                    loginController.handleServerResponse(message);
                } else if (type.contains("GAME") || type.contains("MOVE") || "ERROR".equals(type)) {
                    gameController.handleServerMessage(message);
                } else {
                    System.out.println("Неизвестный тип сообщения для обработки: " + type);
                }
            });

            // настройка перехода между окнами
            loginController.setOnLoginSuccess(() -> {
                loginController.hideLoginWindow();
                gameController.showGameWindow();
                gameFrame.setStatus("Добро пожаловать, " +
                        clientNetwork.getCurrentUsername() + "!");
            });

            gameController.setOnExit(() -> {
                gameController.reset();
                gameController.hideGameWindow();
                loginController.showLoginWindow();
            });

            // обработка закрытия окон
            setupWindowListeners();

            // демонстрация окна входа
            loginController.showLoginWindow();

            System.out.println("Клиент успешно инициализирован");

        } catch (Exception e) {
            System.err.println("Ошибка при запуске клиента: " + e.getMessage());
            e.printStackTrace();

            SwingUtilities.invokeLater(() -> {
                int choice = JOptionPane.showOptionDialog(null,
                        "Не удалось подключиться к серверу: " + e.getMessage() +
                                "\n\nУбедитесь, что:\n" +
                                "1. Сервер запущен на " + SERVER_HOST + ":" + SERVER_PORT + "\n" +
                                "2. Файрволл не блокирует соединение\n" +
                                "\nПопробовать снова?",
                        "Ошибка подключения",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        new String[]{"Повторить", "Выход"},
                        "Повторить");

                if (choice == 0) {
                    // Перезапускаем клиент
                    new MainClient().start();
                } else {
                    System.exit(0);
                }
            });
        }
    }

    // настройка обработчиков закрытия окон
    private void setupWindowListeners() {
        // При закрытии окна входа
        loginFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        // При закрытии окна игры
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameController.exitGame();
            }
        });
    }

    // отключение клиента
    private void shutdown() {
        System.out.println("Завершение работы клиента...");

        if (clientNetwork != null) {
            clientNetwork.disconnect();
        }

        System.exit(0);
    }
}
