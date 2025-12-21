//Окно игры


package client.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// класс для отображения окна с игрой
public class GameWindow extends JFrame {
    // информационные метки
    private JLabel statusLabel;
    private JLabel playerLabel;
    private JLabel scoreLabel;
    // игровое поле
    private GameBoardPanel gameBoard;
    // кнопки управления
    private JButton newGameButton;
    private JButton surrenderButton;
    private JButton exitButton;
    private JButton saveButton;
    private JButton loadButton;

    // интерфейс, который определяет все действия доступные в игре
    public interface GameFrameListener {
        void onCellClicked(int row, int col);
        void onNewGame();
        void onSurrender();
        void onExit();
        void onSaveGame();
        void onLoadGame();
    }

    // слушатель
    private GameFrameListener listener;

    // конструктор окна с игрой
    public GameWindow() {
        initComponents();
        setupWindow();
    }

    // метод для создания и размещения всех элементов
    private void initComponents() {
        // заголовок
        setTitle("Крестики-Нолики");
        // закрытие окна = выход из приложения
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // минимальный размер
        setMinimumSize(new Dimension(500, 600));

        // основная панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // полярное расположение
        // отступы
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // настройка цвета
        mainPanel.setBackground(new Color(245, 245, 245));

        // верхняя панель - информация
        JPanel infoPanel = createInfoPanel();

        // центр - игровое поле
        gameBoard = new GameBoardPanel();

        // настройка обработчика нажатия по клеткам
        gameBoard.setCellClickListener(new GameBoardPanel.CellClickListener() {
            @Override
            public void onCellClick(int row, int col) {
                if (listener != null) {
                    listener.onCellClicked(row, col);
                }
            }
        });

        // панель для центрирования поля
        JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // последовательное расположение
        // настройка цвета
        boardPanel.setBackground(new Color(245, 245, 245));
        // добавление панели
        boardPanel.add(gameBoard);

        // Нижняя панель - кнопки
        JPanel controlPanel = createControlPanel();

        // добавление панелей в основную панель
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // добавление основной панели на форму
        add(mainPanel);
        // автоматическое определение размеров
        pack();
        // центрирование
        setLocationRelativeTo(null);
    }

    // метод создания панели с информацией об игре
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5)); // табличное расположение
        // настройка цвета
        panel.setBackground(Color.WHITE);
        // настройка рамки
        panel.setBorder(BorderFactory.createCompoundBorder(
                // настройка цвета
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                // отступы
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // статус игры
        statusLabel = new JLabel("Добро пожаловать!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setForeground(new Color(44, 62, 80));

        // информация об игроке
        playerLabel = new JLabel("Вы играете за: X", SwingConstants.CENTER);
        playerLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // статистика игры
        scoreLabel = new JLabel("Побед: 0 | Поражений: 0 | Ничьих: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLabel.setForeground(new Color(127, 140, 141));

        // добавление панелей на информационную панель
        panel.add(statusLabel);
        panel.add(playerLabel);
        panel.add(scoreLabel);

        return panel;
    }

    // метод создания панели с кнопками управления
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        // настройка цвета
        panel.setBackground(new Color(245, 245, 245));
        // отступ
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Панель основных кнопок
        JPanel mainButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        mainButtonsPanel.setBackground(new Color(245, 245, 245));

        // кнопки
        newGameButton = createControlButton("Новая игра", new Color(39, 174, 96));
        surrenderButton = createControlButton("Сдаться", new Color(231, 76, 60));
        exitButton = createControlButton("Выход", new Color(149, 165, 166));

        // добавление кнопок на панель с основными кнопками
        mainButtonsPanel.add(newGameButton);
        mainButtonsPanel.add(surrenderButton);
        mainButtonsPanel.add(exitButton);

        // Панель дополнительных кнопок
        JPanel extraButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        extraButtonsPanel.setBackground(new Color(245, 245, 245));

        // кнопки сохранения/загрузки
        saveButton = createControlButton("Сохранить", new Color(52, 152, 219));
        loadButton = createControlButton("Загрузить", new Color(155, 89, 182));

        // добавление дополнительных кнопок на панель дополнительных кнопок
        extraButtonsPanel.add(saveButton);
        extraButtonsPanel.add(loadButton);

        // добавление кнопок на панель
        panel.add(mainButtonsPanel);
        panel.add(extraButtonsPanel);

        // Обработчики кнопок
        newGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) listener.onNewGame();
            }
        });

        surrenderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) listener.onSurrender();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) listener.onExit();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) listener.onSaveGame();
            }
        });

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listener != null) listener.onLoadGame();
            }
        });

        return panel;
    }

    // метод создания стилизованной кнопки управления
    private JButton createControlButton(String text, Color color) {
        JButton button = new JButton(text);
        // настройка внешнего вида кнопки: размер, шрифт, цвет, отключение рамки по фокусу, отступы
        button.setPreferredSize(new Dimension(140, 40));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Эффекты при наведении
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // цвет кнопки
                button.setBackground(color.brighter());
                // курсор в виде мыши
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color); // возвращение исходного цвета
            }
        });

        return button;
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

    // Методы для обновления отображения игрового поля
    public void updateBoard(String[][] boardState) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String symbol = boardState[row][col];
                if (symbol != null && !symbol.isEmpty()) {
                    gameBoard.setCell(row, col, symbol); // установить символ
                } else {
                    gameBoard.clearCell(row, col); //очистить клетку
                }
            }
        }
    }

    // метод установки статуса
    public void setStatus(String status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(status);
            }
        });
    }

    // метод установки информации о том, чей ход в данный момент
    public void setPlayerInfo(String playerSymbol) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                playerLabel.setText("Вы играете за: " + playerSymbol);
            }
        });
    }

    // метод обновления статистики
    /*public void setScoreInfo(int wins, int losses, int draws) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scoreLabel.setText(String.format(
                        "Побед: %d | Поражений: %d | Ничьих: %d",
                        wins, losses, draws
                ));
            }
        });
    }*/

    // метод отключения игрового поля
    public void disableBoard() {
        gameBoard.disableBoard();
        surrenderButton.setEnabled(false);
    }

    // метод включения игрового поля
    public void enableBoard() {
        gameBoard.enableBoard();
        surrenderButton.setEnabled(true);
    }

    // метод подцветки выигрышной линии
    public void highlightWinningLine(int[][] winningCells) {
        gameBoard.highlightWinningCells(winningCells);
    }

    // метод очистки игрового поля
    public void clearBoard() {
        gameBoard.clearBoard();
    }

    // метод установки слушателей
    public void setListener(GameFrameListener listener) {
        this.listener = listener;
    }

    // метод для показа окна
    public void showWindow() {
        setVisible(true);
    }

    // метод для скрытия окна
    public void hideWindow() {
        setVisible(false);
    }

    // метод показывает информационное диалоговое окно
    public void showInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // метод показывает диалоговое окно с ошибкой
    public void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
