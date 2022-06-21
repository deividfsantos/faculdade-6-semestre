package com.t2.redes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileReader {

    public List<String> readFile(String fileName) {
        try {
            Path path = Paths.get(buildFilePath(fileName));
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildFilePath(String fileName) {
        String projectDirectory = System.getProperty("user.dir");
        String fileSeparator = System.getProperty("file.separator");
        return projectDirectory + fileSeparator + "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + fileName;
    }
}
