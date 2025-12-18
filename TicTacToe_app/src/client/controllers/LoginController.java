package client.controllers;

import client.ClientNetwork;
import client.ui.LoginWindow;
import javax.swing.SwingUtilities;

public class LoginController {

    private LoginWindow loginFrame;
    private ClientNetwork clientNetwork;
    private Runnable onLoginSuccess;

    public LoginController(LoginWindow loginFrame, ClientNetwork clientNetwork) {
        this.loginFrame = loginFrame;
        this.clientNetwork = clientNetwork;

        loginFrame.setListener(new LoginWindow.LoginListener() {
            @Override
            public void onLogin(String username, String password) {
                handleLogin(username, password);
            }

            @Override
            public void onRegister(String username, String password) {
                handleRegister(username, password);
            }
        });
    }

    private void handleLogin(String username, String password) {
        if (!clientNetwork.isConnected()) {
            loginFrame.setStatus("Нет подключения к серверу");
            return;
        }

        loginFrame.setStatus("Подключение...");

        // Локальная проверка (в реальной версии будет обращение к серверу)
        if (username.length() >= 3 && password.length() >= 3) {
            boolean success = clientNetwork.login(username, password);

            if (success) {
                loginFrame.setStatus("Успешный вход!");

                // Небольшая задержка для демонстрации
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (onLoginSuccess != null) {
                            onLoginSuccess.run();
                        }
                    });
                }).start();
            } else {
                loginFrame.setStatus("Неверные учетные данные");
            }
        } else {
            loginFrame.setStatus("Логин и пароль должны быть не менее 3 символов");
        }
    }

    private void handleRegister(String username, String password) {
        // В этой реализации регистрация обрабатывается так же как вход
        // Сервер сам определит, нужно ли создавать нового пользователя
        handleLogin(username, password);
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public void showLoginWindow() {
        loginFrame.clearFields();
        loginFrame.showWindow();
    }

    public void hideLoginWindow() {
        loginFrame.hideWindow();
    }
}
