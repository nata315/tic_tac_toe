package server.storage;

import server.GameManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GameStorage {
    private static final String SAVE_DIR = "data/games/";

    public GameStorage() throws IOException {
        Files.createDirectories(Paths.get(SAVE_DIR));
    }

    // Сохраняем только активные (не завершенные) игры
    public void saveGame(GameManager.GameSession session, String player) throws IOException {
        if (!session.isActive()) {
            throw new IOException("Нельзя сохранить завершенную игру");
        }

        String filename = player + "_" + session.getGameId() + ".dat";
        Path filePath = Paths.get(SAVE_DIR + filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(filePath, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(session);
            System.out.println("Игра сохранена: " + filename);
        }
    }

    public GameManager.GameSession loadGame(String gameId, String player)
            throws IOException, ClassNotFoundException {
        String pattern = player + "_" + gameId + ".dat";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), pattern)) {

            for (Path entry : stream) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        Files.newInputStream(entry))) {
                    GameManager.GameSession session = (GameManager.GameSession) ois.readObject();
                    System.out.println("Игра загружена: " + entry.getFileName());
                    return session;
                }
            }
        }
        return null;
    }

    public List<String> getUserSavedGames(String player) throws IOException {
        List<String> games = new ArrayList<>();
        String pattern = player + "_*.dat";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), pattern)) {

            for (Path entry : stream) {
                String filename = entry.getFileName().toString();
                // Извлекаем gameId из имени файла
                String gameId = filename.substring(player.length() + 1, filename.length() - 4);

                // Проверяем, что игра еще актуальна (не повреждена)
                try {
                    GameManager.GameSession session = loadGame(gameId, player);
                    if (session != null && session.isActive()) {
                        games.add(gameId);
                    }
                } catch (Exception e) {
                    // Пропускаем поврежденные файлы
                    System.err.println("Поврежденный файл игры: " + filename);
                }
            }
        }

        // Сортируем по времени создания (новые сверху)
        games.sort(Collections.reverseOrder());
        return games;
    }

    public boolean deleteSavedGame(String gameId, String player) throws IOException {
        String pattern = player + "_" + gameId + ".dat";

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), pattern)) {

            for (Path entry : stream) {
                Files.delete(entry);
                System.out.println("Игра удалена: " + entry.getFileName());
                return true;
            }
        }
        return false;
    }

    // Очистка старых сохранений
    public void cleanupOldGames(int daysToKeep) throws IOException {
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), "*.dat")) {

            for (Path entry : stream) {
                if (Files.getLastModifiedTime(entry).toMillis() < cutoffTime) {
                    Files.delete(entry);
                    System.out.println("Удалена старая игра: " + entry.getFileName());
                }
            }
        }
    }
}