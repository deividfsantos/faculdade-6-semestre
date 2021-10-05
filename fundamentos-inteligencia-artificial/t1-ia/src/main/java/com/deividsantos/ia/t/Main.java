package com.deividsantos.ia.t;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

class Main {


    private static final String carga = "E 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 1 1 1 0 0 0 0 0 1 1 1\n" +
            "1 0 0 0 0 1 1 1 0 1 1 0\n" +
            "1 0 1 1 1 1 1 1 0 0 0 0\n" +
            "0 0 0 1 0 0 0 0 1 0 1 1\n" +
            "1 1 0 0 0 1 0 1 0 0 1 1\n" +
            "1 1 1 0 1 1 0 0 0 1 1 0\n" +
            "0 0 1 0 0 1 0 1 0 1 1 0\n" +
            "0 0 0 0 1 1 0 0 0 1 1 0\n" +
            "1 1 1 0 1 0 0 1 1 1 1 0\n" +
            "1 1 1 0 1 0 0 0 0 1 1 1\n" +
            "1 1 1 0 0 0 0 1 0 0 0 S";

    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("Digite o caminho completo do arquivo do labirinto: ");
        String carga = Files.readString(Path.of(input.nextLine()));

        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(carga);
        geneticAlgorithm.execute();
    }
}
