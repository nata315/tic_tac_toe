package server.storage;

import server.GameManager;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GameStorage {
    private static final String SAVE_DIR = "data/games/";
    private static final String STATS_DIR = "data/stats/";

    public GameStorage() throws IOException {
        Files.createDirectories(Paths.get(SAVE_DIR));
        Files.createDirectories(Paths.get(STATS_DIR));
    }

    public void saveGame(GameManager.GameSession session, String player) throws IOException {
        // Исправлено: getGameId() вместо getCameld()
        String filename = player + "_" + session.getGameId() + ".dat";
        Path filePath = Paths.get(SAVE_DIR + filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(filePath))) {
            oos.writeObject(session);
        }
    }

    public GameManager.GameSession loadGame(String gameId, String player)
            throws IOException, ClassNotFoundException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), player + "_" + gameId + "*.dat")) {

            for (Path entry : stream) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        Files.newInputStream(entry))) {
                    return (GameManager.GameSession) ois.readObject();
                }
            }
        }
        return null;
    }

    public List<String> getUserSavedGames(String player) throws IOException {
        List<String> games = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), player + "_*.dat")) {

            for (Path entry : stream) {
                String filename = entry.getFileName().toString();
                // Извлекаем gameId из имени файла
                String gameId = filename.substring(player.length() + 1, filename.length() - 4);
                games.add(gameId);
            }
        }
        return games;
    }

    public boolean deleteSavedGame(String gameId, String player) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(SAVE_DIR), player + "_" + gameId + "*.dat")) {

            for (Path entry : stream) {
                Files.delete(entry);
                return true;
            }
        }
        return false;
    }
}