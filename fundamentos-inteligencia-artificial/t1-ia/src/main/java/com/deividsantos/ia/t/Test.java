package com.deividsantos.ia.t;

import java.util.Random;

class Test {
    public static void main(String args[]) {
        int[] tarefas = {10, 5, 5, 3, 7, 2, 3, 5, 8, 2, 4, 4, 2, 10, 5, 5, 3, 7, 2, 3, 5, 8, 2, 4, 4, 2};
        Random rnd = new Random();
        int numeroLinhas = 5;
        int numeroPessoas = 2;
        int[][] matriz = new int[numeroLinhas][tarefas.length + 1];
        int[][] intermediaria = new int[numeroLinhas][tarefas.length + 1];
        for (int i = 0; i < numeroLinhas; i++) {
            for (int j = 0; j < tarefas.length - 1; j++) {
                matriz[i][j] = rnd.nextInt(numeroPessoas);
            }
        }
        for (int g = 0; g < 10; g++) {
            System.out.println("Geração - " + g);
            calculaAptidao(matriz, tarefas);

            printa(matriz);
            int elite = elitismo(matriz);
            System.out.println("Elitismo- " + elite);
            for (int j = 0; j < matriz[0].length; j++) {
                intermediaria[0][j] = matriz[elite][j];
            }
            cruzamento(intermediaria, matriz);
            if (g % 5 == 0) {
                mutacao(intermediaria);
            }
            matriz = intermediaria;
        }
    }

    public static int aptidao(int[] linha, int[] tarefas) {
        int soma0 = 0;
        int soma1 = 0;
        for (int i = 0; i < tarefas.length; i++) {
            if (linha[i] == 0) {
                soma0 += tarefas[i];
            } else {
                soma1 += tarefas[i];
            }
        }
        return Math.abs(soma0 - soma1);
    }

    public static void calculaAptidao(int[][] matriz, int[] tarefas) {
        int j = matriz[0].length - 1;
        for (int i = 0; i < matriz.length; i++) {
            matriz[i][j] = aptidao(matriz[i], tarefas);
        }
    }

    public static void printa(int[][] matriz) {
        int j;
        for (int i = 0; i < matriz.length; i++) {
            System.out.print("Linha " + i + " - ");
            for (j = 0; j < matriz[i].length - 1; j++) {
                System.out.print(matriz[i][j] + " ");
            }
            System.out.println("-H: " + matriz[i][j] + " ");
        }
    }

    public static int elitismo(int[][] matriz) {
        int i;
        int j = matriz[0].length - 1;
        int menor = matriz[0][j];
        int linhaMenor = 0;
        for (i = 0; i < matriz.length; i++) {
            if (menor > matriz[i][j]) {
                menor = matriz[i][j];
                linhaMenor = i;
            }
        }
        return linhaMenor;
    }

    public static int torneio(int[][] matriz) {
        Random rnd = new Random();
        int pai1 = rnd.nextInt(matriz.length);
        int pai2 = rnd.nextInt(matriz.length);
        int j = matriz[0].length - 1;
        if (matriz[pai1][j] < matriz[pai2][j]) {
            return pai1;
        }
        return pai2;
    }

    public static void cruzamento(int[][] intermediaria, int[][] matriz) {

        for (int i = 1; i < matriz.length; i += 2) {
            int c1 = torneio(matriz);
            int c2 = torneio(matriz);

            for (int j = 0; j < matriz[i].length - 1; j++) {
                if (j < matriz[i].length / 2) {
                    intermediaria[i][j] = matriz[c1][j];
                    intermediaria[i + 1][j] = matriz[c2][j];
                } else {
                    intermediaria[i][j] = matriz[c2][j];
                    intermediaria[i + 1][j] = matriz[c1][j];
                }
            }
        }
    }

    public static void mutacao(int[][] inter) {
        Random rnd = new Random();
        int line1 = rnd.nextInt(inter.length);
        int column1 = rnd.nextInt(inter[0].length - 1);
        if (inter[line1][column1] == 0) {
            inter[line1][column1] = 1;
        } else {
            inter[line1][column1] = 0;
        }
    }
}