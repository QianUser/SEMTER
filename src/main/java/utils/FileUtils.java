package utils;

import exception.FailToCreateFileException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileUtils {

    public static void createDirectory(String filename) throws FailToCreateFileException {
        createDirectory(new File(filename));
    }

    public static void createDirectory(File file) throws FailToCreateFileException {
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new FailToCreateFileException("Can't create the directory: " + file);
            }
        }
    }

    public static Object readObject(String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(path);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        return objectInputStream.readObject();
    }

    public static void writeObject(Object object, String path) throws IOException, FailToCreateFileException {
        FileUtils.createDirectory(path);
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        fileOutputStream.close();
    }

    public static String getPathName(String... pathNames) {
        return String.join(File.separator, pathNames);
    }

    public static List<String> walkDir(String dirname) throws IOException {
        return walkDir(new File(dirname));
    }

    public static List<String> walkDir(File dir) throws IOException {
        if (!dir.exists()) {
            throw new IOException("Directory not exists: " + dir);
        }
        List<String> list = new ArrayList<>();
        if (dir.isFile()) {
            list.add(dir.getAbsolutePath());
            return list;
        } else {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                list.addAll(walkDir(file));
            }
        }
        return list;
    }

}
