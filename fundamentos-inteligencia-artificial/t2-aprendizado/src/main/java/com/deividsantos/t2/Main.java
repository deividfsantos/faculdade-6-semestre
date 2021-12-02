package com.deividsantos.t2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("Digite o caminho completo do arquivo do labirinto: ");
        String fileName = input.nextLine();
        String carga = Files.readString(Path.of(buildFilePath(fileName)));

        GeneticAlgorithmExample geneticAlgorithm = new GeneticAlgorithmExample(carga);
        geneticAlgorithm.execute();
    }

    private static String buildFilePath(String fileName) {
        String projectDirectory = System.getProperty("user.dir");
        String fileSeparator = System.getProperty("file.separator");
        return projectDirectory + fileSeparator + "src" + fileSeparator + "main" + fileSeparator + "resources" + fileSeparator + fileName;
    }
}
