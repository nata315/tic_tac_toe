//Работа с файлами пользователей


package server.storage;
// Сетевые возможности
import java.net.*;           // ServerSocket, Socket
import java.io.*;            // Потоки ввода/вывода
// Многопоточность
import java.util.concurrent.*; // ExecutorService, ThreadPool
// Работа с файлами
import java.nio.file.*;      // Files, Paths
import java.util.*;          // Коллекции


import shared.User;

//Работа с файлами пользователей
public class UserStorage {
    // Сериализация объектов в файлы
    public void saveUser(User user) {
        String filename = "data/users/" + user.getUsername() + ".dat";

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(user);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User loadUser(String username) {
        String filename = "data/users/" + username + ".dat";

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filename))) {
            return (User) ois.readObject();
        } catch (FileNotFoundException e) {
            return null; // Пользователь не найден
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
