package com.deividsantos.ia.t;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

class Main {
    public static void main(String[] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("Digite o caminho completo do arquivo do labirinto: ");
        String carga = Files.readString(Path.of(input.nextLine()));

        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(carga);
        geneticAlgorithm.execute();
    }
}
