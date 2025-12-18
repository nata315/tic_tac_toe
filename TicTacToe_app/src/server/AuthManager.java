package server;

import shared.User;
import java.io.*;
import java.util.HashMap;

public class AuthManager {
    private HashMap<String, User> users = new HashMap<>();
    private static final String USERS_FILE = "data/users.dat";

    public AuthManager() {
        loadUsers();
    }

    // Исправленный метод authenticate
    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false; // Пользователь уже существует
        }

        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
        return true;
    }

    @SuppressWarnings("unchecked") // Добавляем аннотацию для подавления предупреждения
    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) {
                // Создаем директорию, если не существует
                file.getParentFile().mkdirs();
                saveUsers();
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (HashMap<String, User>) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка загрузки пользователей: " + e.getMessage());
            users = new HashMap<>();
        }
    }

    private void saveUsers() {
        try {
            File file = new File(USERS_FILE);
            file.getParentFile().mkdirs();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(users);
            }
        } catch (IOException e) {
            System.out.println("Ошибка сохранения пользователей: " + e.getMessage());
        }
    }

    public User getUser(String username) {
        return users.get(username);
    }
}