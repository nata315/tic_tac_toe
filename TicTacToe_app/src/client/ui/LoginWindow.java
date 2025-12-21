//Окно входа


package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// класс для отображения окна входа или регистрации в системе
public class LoginWindow extends JFrame {
    // поля для ввода
    private JTextField usernameField;
    private JPasswordField passwordField;
    // Кнопки входа, регистрации
    private JButton loginButton;
    private JButton registerButton;
    // Метка для сообщений
    private JLabel statusLabel;

    // интерфейс, который определяет метод для обработки событий входа\регистрации
    public interface LoginListener {
        void onLogin(String username, String password);
        void onRegister(String username, String password);
    }

    // слушатель
    private LoginListener listener;

    // конструктор окна входа
    public LoginWindow() {
        initComponents();
        setupWindow();
    }

    // метод для создания и размещения всех элементов
    private void initComponents() {
        // текст заголовка
        setTitle("Крестики-Нолики - Вход");
        // закрытие приложения = выход из него
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // отключение возможности изменить размер
        setResizable(false);

        // основная панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // полярное расположение
        // настройка цветов
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(248, 248, 248));

        // панель заголовок
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // последовательное расположение
        // настройка цвета, шрифта, текста
        titlePanel.setBackground(new Color(248, 248, 248));
        JLabel titleLabel = new JLabel("КРЕСТИКИ-НОЛИКИ");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(41, 128, 185));
        // добавление заголовка на панель
        titlePanel.add(titleLabel);

        // панель входа
        JPanel loginPanel = createLoginPanel();

        // статус: настройка текста, шрифта, цвета
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.RED);

        // добавление панелей на основную панель
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // добавление основной панели на форму
        add(mainPanel);
        // автоматическое определение размера
        pack();
        // центрирование окна на экране
        setLocationRelativeTo(null);
    }

    // метод для создания панели с формой входа
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); // табличное расположение
        // установка цвета
        panel.setBackground(Color.WHITE);

        // создание рамки для панели
        panel.setBorder(BorderFactory.createCompoundBorder(
                // внешняя рамка
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                // внутренние отступы
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // настройки для более гибкого размещения
        GridBagConstraints gbc = new GridBagConstraints();
        // отступы
        gbc.insets = new Insets(10, 10, 10, 10);
        // растягивание по горизонатли
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // логин
        JLabel usernameLabel = new JLabel("Логин:");
        // настройка шрифта
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; // колонка 0
        gbc.gridy = 0; // строка 0
        gbc.anchor = GridBagConstraints.EAST; // выравнивание по правому краю
        panel.add(usernameLabel, gbc);

        // поле ввода логина
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1; // колонка 1
        gbc.anchor = GridBagConstraints.WEST; // выравнивание по левому краю
        panel.add(usernameField, gbc);

        // пароль
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; // колонка 0
        gbc.gridy = 1; // строка 1
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(passwordLabel, gbc);

        // поле ввода пароля
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1; // колонка 1
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        // кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        // создание стилей для кнопок
        loginButton = createStyledButton("Войти", new Color(46, 204, 113));
        registerButton = createStyledButton("Регистрация", new Color(52, 152, 219));

        // добавление слушателей для кнопок
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        // добавление кнопок на панель
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; // колонка 0
        gbc.gridy = 2; // строка 2
        gbc.gridwidth = 2; // занимает две колонки
        gbc.anchor = GridBagConstraints.CENTER; // выравнивание по центру
        panel.add(buttonPanel, gbc);

        // обработка нажатия Enter
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(); // Enter = кнопка "Войти"
                }
            }
        };

        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);

        return panel;
    }

    // метод для создания стилизованной кнопки
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        // фиксированный размер
        button.setPreferredSize(new Dimension(120, 35));
        // настройка шрифта
        button.setFont(new Font("Arial", Font.BOLD, 14));
        // белый цвет
        button.setForeground(Color.BLACK);
        // основной цвет
        button.setBackground(color);
        // отключение рамки при фокусировке
        button.setFocusPainted(false);
        // внутренние отступы
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Эффекты при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // цвет становится светлее
                button.setBackground(color.brighter());
                // курсор в виде руки
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // возвращаем исходный цвет
                button.setBackground(color);
            }
        });

        return button;
    }

    // метод обработки нажатия кнопки "Войти"
    private void handleLogin() {
        // получение данных с полей
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // валидация полей
        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Заполните все поля!");
            return;
        }

        // уведомление слушателя
        if (listener != null) {
            listener.onLogin(username, password);
        }
    }

    // метод обработки нажатия кнопки "Регистрация"
    // аналогичен обработке кнопке "Войти"
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Заполните все поля!");
            return;
        }

        if (listener != null) {
            listener.onRegister(username, password);
        }
    }

    // метод установки иконки
    private void setupWindow() {
        try {
            // Поиск иконки через ClassLoader. getClass() - метод, с помощью которого получаем класс
            // текущего объекта. getClassLoader() - метод представляет собой загрузчик ресурсов, который знает,
            // что и где искать. getResource() - метод ищет файл по указанному пути.
            java.net.URL iconURL = getClass().getClassLoader().getResource("client/resources/icon.jpg");

            // если иконка была найдена
            if (iconURL != null) {
                // переменная, которая хранит ссылку на иконку
                ImageIcon icon = new ImageIcon(iconURL);
                // setIconImage() устанавливает иконку в заголовок окна.
                // getImage() получает объект Image из ImageIcon
                setIconImage(icon.getImage());
                System.out.println("Иконка успешно загружена: " + iconURL);
            } else {
                System.out.println("Иконка не найдена. Используется стандартная иконка.");
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки иконки: " + e.getMessage());
        }
    }

    // метод установки слушателя
    public void setListener(LoginListener listener) {
        this.listener = listener;
    }

    // метод установки сообщения со статусом
    public void setStatus(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(message);
            }
        });
    }

    // метод для очистки поля ввода
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        setStatus(" ");
    }

    // метод для отображения окна
    public void showWindow() {
        setVisible(true);
    }

    // метод для скрытия окна
    public void hideWindow() {
        setVisible(false);
    }

}
