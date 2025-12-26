//Управление окном входа
package client.controllers;

import client.ClientNetwork;
import client.ui.LoginWindow;
import shared.GameMessage;
import javax.swing.SwingUtilities;

// контроллер авторизации
public class LoginController {

    // ссылки на компоненты системы
    private LoginWindow loginFrame;
    private ClientNetwork clientNetwork;
    private Runnable onLoginSuccess;

    // конструктор
    public LoginController(LoginWindow loginFrame, ClientNetwork clientNetwork) {
        this.loginFrame = loginFrame;
        this.clientNetwork = clientNetwork;

        // установка слушателя событий
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

    // метод отвечающий за обработку попытки входа пользователя
    private void handleLogin(String username, String password) {
        // проверка введенных данных
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            loginFrame.setStatus("Заполните все поля");
            return;
        }

        // если отсутствует подключение к серверу
        if (!clientNetwork.isConnected()) {
            loginFrame.setStatus("Нет подключения к серверу");
            return;
        }

        loginFrame.setStatus("Отправка запроса на вход...");
        boolean sent = clientNetwork.login(username.trim(), password.trim());

        if (!sent) {
            loginFrame.setStatus("Ошибка отправки запроса");
        }
    }

    // метод отвечающий за обработку попытки регистрации пользователя
    private void handleRegister(String username, String password) {
        // проверка введенных данных
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            loginFrame.setStatus("Заполните все поля");
            return;
        }

        if (username.length() < 3) {
            loginFrame.setStatus("Логин должен быть не менее 3 символов");
            return;
        }

        if (password.length() < 3) {
            loginFrame.setStatus("Пароль должен быть не менее 3 символов");
            return;
        }

        // если отсутствует подключение к серверу
        if (!clientNetwork.isConnected()) {
            loginFrame.setStatus("Нет подключения к серверу");
            return;
        }

        loginFrame.setStatus("Отправка запроса на регистрацию...");
        boolean sent = clientNetwork.register(username.trim(), password.trim());

        if (!sent) {
            loginFrame.setStatus("Ошибка отправки запроса");
        }
    }

    // метод отвечающий за обработку сообщений от сервера при попытке регистрации
    public void handleServerResponse(GameMessage message) {
        SwingUtilities.invokeLater(() -> {
            try {
                String type = message.getType();

                if ("LOGIN_RESPONSE".equals(type)) {
                    // обработка ответа на вход
                    Boolean success = (Boolean) message.getData("success");

                    if (success != null && success) {
                        loginFrame.setStatus("Успешный вход! Перенаправление...");

                        // задержка, чтобы показать сообщения
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            SwingUtilities.invokeLater(() -> {
                                if (onLoginSuccess != null) {
                                    onLoginSuccess.run();
                                }
                            });
                        }).start();
                    } else {
                        loginFrame.setStatus("Неверный логин или пароль");
                    }
                }
                else if ("REGISTER_RESPONSE".equals(type)) {
                    // обработка ответа на регистрацию
                    Boolean success = (Boolean) message.getData("success");
                    String msg = "Неизвестная ошибка";

                    if (message.hasData("message")) {
                        msg = (String) message.getData("message");
                    }

                    loginFrame.setStatus(msg);

                }
                else if ("ERROR".equals(type)) {
                    // обработка ошибки от сервера
                    String error = "Неизвестная ошибка";
                    if (message.hasData("message")) {
                        error = (String) message.getData("message");
                    }
                    loginFrame.setStatus("Ошибка: " + error);
                }

            } catch (Exception e) {
                System.err.println("Ошибка обработки ответа: " + e.getMessage());
                loginFrame.setStatus("Ошибка обработки ответа сервера");
            }
        });
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
