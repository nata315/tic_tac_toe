package client;

import client.controllers.*;
import client.ui.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private ClientNetwork clientNetwork;
    private LoginWindow loginFrame;
    private GameWindow gameFrame;
    private LoginController loginController;
    private GameController gameController;

    public static void main(String[] args) {
        // Устанавливаем Look and Feel для более современного вида
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Запускаем приложение в потоке EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainClient().start();
            }
        });
    }

    public void start() {
        try {
            System.out.println("Запуск клиента...");

            // Создаем сетевое подключение (заглушка)
            clientNetwork = new ClientNetwork(SERVER_HOST, SERVER_PORT);

            // Создаем UI компоненты
            loginFrame = new LoginWindow();
            gameFrame = new GameWindow();

            // Создаем контроллеры
            loginController = new LoginController(loginFrame, clientNetwork);
            gameController = new GameController(gameFrame, clientNetwork);

            // Настраиваем переходы между окнами
            loginController.setOnLoginSuccess(new Runnable() {
                @Override
                public void run() {
                    loginController.hideLoginWindow();
                    gameController.showGameWindow();
                }
            });

            gameController.setOnExit(new Runnable() {
                @Override
                public void run() {
                    gameController.hideGameWindow();
                    loginController.showLoginWindow();
                }
            });

            // Показываем окно входа
            loginController.showLoginWindow();

            System.out.println("Клиент успешно запущен");

        } catch (Exception e) {
            System.err.println("Ошибка при запуске клиента: " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Не удалось запустить клиент: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
