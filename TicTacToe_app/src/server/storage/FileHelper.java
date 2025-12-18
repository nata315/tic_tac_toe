package server.storage;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileHelper {

    // Метод 1: Проверка существования и создание директории
    public static void ensureDirectoryExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("Создана директория: " + directoryPath);
        }
    }

    // Метод 2: Чтение всех строк из файла
    public static List<String> readAllLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new ArrayList<>(); // Возвращаем пустой список
        }

        return Files.readAllLines(path);
    }

    // Метод 3: Запись строк в файл (перезапись)
    public static void writeAllLines(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);

        // Создаем родительские директории, если нужно
        if (path.getParent() != null) {
            ensureDirectoryExists(path.getParent().toString());
        }

        Files.write(path, lines);
    }

    // Метод 4: Добавление строки в конец файла
    public static void appendLine(String filePath, String line) throws IOException {
        Path path = Paths.get(filePath);

        // Создаем файл, если его нет
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                ensureDirectoryExists(path.getParent().toString());
            }
            Files.createFile(path);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.APPEND)) {
            writer.write(line);
            writer.newLine();
        }
    }

    // Метод 5: Поиск строки в файле
    public static boolean containsLine(String filePath, String searchLine) throws IOException {
        List<String> lines = readAllLines(filePath);
        return lines.contains(searchLine);
    }

    // Метод 6: Удаление строки из файла
    public static boolean removeLine(String filePath, String lineToRemove) throws IOException {
        List<String> lines = readAllLines(filePath);
        boolean removed = lines.remove(lineToRemove);

        if (removed) {
            writeAllLines(filePath, lines);
        }

        return removed;
    }

    // Метод 7: Сериализация объекта в файл
    public static void serializeObject(String filePath, Object obj) throws IOException {
        Path path = Paths.get(filePath);

        // Создаем директории, если нужно
        if (path.getParent() != null) {
            ensureDirectoryExists(path.getParent().toString());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(path))) {
            oos.writeObject(obj);
        }
    }

    // Метод 8: Десериализация объекта из файла
    public static Object deserializeObject(String filePath) throws IOException, ClassNotFoundException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(path))) {
            return ois.readObject();
        }
    }

    // Метод 9: Копирование файла
    public static void copyFile(String sourcePath, String destPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path dest = Paths.get(destPath);

        // Создаем директории для назначения
        if (dest.getParent() != null) {
            ensureDirectoryExists(dest.getParent().toString());
        }

        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    // Метод 10: Удаление файла или директории
    public static boolean deleteFileOrDirectory(String path) throws IOException {
        Path filePath = Paths.get(path);

        if (!Files.exists(filePath)) {
            return false;
        }

        // Если это директория, удаляем рекурсивно
        if (Files.isDirectory(filePath)) {
            Files.walk(filePath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } else {
            // Если это файл
            Files.delete(filePath);
        }

        return true;
    }

    // Метод 11: Получение списка файлов в директории
    public static List<String> listFiles(String directoryPath, String extension) throws IOException {
        List<String> files = new ArrayList<>();
        Path dir = Paths.get(directoryPath);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return files;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {
                    String filename = entry.getFileName().toString();
                    if (extension == null || filename.endsWith(extension)) {
                        files.add(filename);
                    }
                }
            }
        }

        return files;
    }

    // Метод 12: Чтение конфигурационного файла (key=value)
    public static Properties readConfigFile(String configPath) throws IOException {
        Properties props = new Properties();
        Path path = Paths.get(configPath);

        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) {
                props.load(input);
            }
        }

        return props;
    }

    // Метод 13: Запись конфигурационного файла
    public static void writeConfigFile(String configPath, Properties props) throws IOException {
        Path path = Paths.get(configPath);

        // Создаем директории, если нужно
        if (path.getParent() != null) {
            ensureDirectoryExists(path.getParent().toString());
        }

        try (OutputStream output = Files.newOutputStream(path)) {
            props.store(output, "Конфигурация сервера");
        }
    }
}