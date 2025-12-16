package client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// основной класс для графического отображения поля 3х3
public class GameBoardPanel extends JPanel {
    // Двумерный массив для хранения ссылок на кнопки-клетки игрового поля
    private JButton[][] cells;
    // Слушатель событий
    private CellClickListener cellClickListener;

    // интерфейс для обработки нажатий по клеткам
    public interface CellClickListener {
        void onCellClick(int row, int col);
    }

    // Конструктор игрового поля
    public GameBoardPanel() {
        // Установки разметки (менеджера расположений) в виде таблицы
        setLayout(new GridLayout(3, 3, 5, 5));
        // Отступы
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // кнопки-клетки
        cells = new JButton[3][3];
        initializeBoard();
    }

    // метод для создания и инициализации клеток
    private void initializeBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // создание кнопки-клетки
                JButton cell = createCell();

                // координаты, которые позже передаются в обработчик
                final int r = row;
                final int c = col;

                // обработчик клика
                cell.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // при нажатии на кнопку вызывается слушатель с координатами
                        if (cellClickListener != null) {
                            cellClickListener.onCellClick(r, c);
                        }
                    }
                });

                // сохранение кнопки
                cells[row][col] = cell;
                // добавление кнопки на панель
                add(cell);
            }
        }
    }

    // метод для создания и настройки отдельной кнопки-клетки
    private JButton createCell() {
        // создание клетки
        JButton cell = new JButton();
        // настройка размеров кнопки
        cell.setPreferredSize(new Dimension(100, 100));
        // настройка шрифта для более корректного отображения X\0
        cell.setFont(new Font("Arial", Font.BOLD, 40));
        // отключаем прорисовку отдельного контура для кнопки
        cell.setFocusPainted(false);
        // настройка цвета для кнопки и ее рамки
        cell.setBackground(Color.WHITE);
        cell.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Эффект при наведении мыши на кнопку
        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Если клетка доступна и пуста внутри
                if (cell.isEnabled() && cell.getText().isEmpty()) {
                    // Установка светло-голубого цвета кнопки
                    cell.setBackground(new Color(240, 248, 255));
                    // Установка нового курсора в виде руки
                    cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            // возвращение исходного состояния клетки при отведении курсора с клетки
            @Override
            public void mouseExited(MouseEvent e) {
                if (cell.isEnabled() && cell.getText().isEmpty()) {
                    cell.setBackground(Color.WHITE);
                }
            }
        });

        // возвращаем настроенную кнопку
        return cell;
    }

    // метод для установки символов X\O
    public void setCell(int row, int col, String symbol) {
        // проверка на валидность координат кнопок
        if (row < 0 || row >= 3 || col < 0 || col >= 3) return;

        // текущая кнопка
        JButton cell = cells[row][col];

        // установка символа
        cell.setText(symbol);

        // делаем кнопку недоступной
        cell.setEnabled(false);

    }

    // метод для очистки отдельной клетки
    public void clearCell(int row, int col) {
        // проверка на валидность координат кнопок
        if (row < 0 || row >= 3 || col < 0 || col >= 3) return;

        JButton cell = cells[row][col];
        // Убираем текст (символ) с кнопки
        cell.setText("");
        // Делаем кнопку доступной
        cell.setEnabled(true);
        // Возвращение цвета
        cell.setForeground(Color.BLACK);
        cell.setBackground(Color.WHITE);
    }

    // метод для очистки всех клеток
    public void clearBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                clearCell(row, col);
            }
        }
    }

    // метод, чтобы сделать все клетки недоступными
    public void disableBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                cells[row][col].setEnabled(false);
            }
        }
    }

    // метод, чтобы сделать пустые кнопки доступными
    public void enableBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // доступными становятся лишь пустые клетки
                cells[row][col].setEnabled(cells[row][col].getText().isEmpty());
            }
        }
    }

    // метод подцветки линии, которая выйграла
    public void highlightWinningCells(int[][] winningCells) {
        if (winningCells == null) return;

        // для каждой кнопки в линии, которая выйграла
        for (int[] cell : winningCells) {
            int row = cell[0];
            int col = cell[1];
            if (row >= 0 && row < 3 && col >= 0 && col < 3) {
                // устанавливаем желтый цвет для кнопки
                cells[row][col].setBackground(new Color(255, 255, 153));
                // устанавливаем рыжий цвет для рамки
                cells[row][col].setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            }
        }
    }

    // слушателей нажатий по клеткам
    public void setCellClickListener(CellClickListener listener) {
        this.cellClickListener = listener;
    }

    // метод, который возвращает кнопку по координатам
    public JButton getCell(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) return null;
        return cells[row][col];
    }
}
