package com.deividsantos.ia.t;

import java.util.Random;


class Test2 {

    private static int[] cargas = {27, 7, 6, 5, 4, 6, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 27, 7, 6, 5, 4, 6, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};

    private static final int TAM = 11;

    private static int[][] populacao;

    private static int[][] intermediaria;
//

    public static void main(String[] args) {
        Random rand = new Random();
        populacao = new int[TAM][cargas.length + 1];
        intermediaria = new int[TAM][cargas.length + 1];
        int melhor;

        //cria a população inicial
        init();

        for (int g = 0; g < 300; g++) {
            System.out.println("Geração: " + g);
            calculaAptidao();
            printMatriz();
            melhor = getBest();
            System.out.println("Metodo Elitismo = " + melhor);
            if (achouSolucao(melhor)) break;
            crossover();
            populacao = intermediaria;
            if (rand.nextInt(5) == 0) {
                System.out.println("Mutação");
                mutacao();
            }
        }
    }


    public static void init() {
        Random rand = new Random();

        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < cargas.length; j++) {
                populacao[i][j] = rand.nextInt(2);
            }
        }
    }

    public static void printMatriz() {
        int j = 0;
        for (int i = 0; i < TAM; i++) {
            System.out.print("C: " + i + " - ");
            for (j = 0; j < cargas.length; j++) {
                System.out.print(populacao[i][j] + " ");
            }
            System.out.println("F: " + populacao[i][j]);
        }
    }
    //Dicas pro proximo: não da pra segurar shift

    public static int aptidao(int individuo) {
        int somaZero = 0;
        int somaUm = 0;
        for (int j = 0; j < cargas.length; j++) {
            if (populacao[individuo][j] == 0) {
                somaZero += cargas[j];
            } else {
                somaUm += cargas[j];
            }
        }
        return Math.abs(somaZero - somaUm);

    }

    public static void calculaAptidao() {
        for (int i = 0; i < TAM; i++) {
            populacao[i][cargas.length] = aptidao(i);
        }
    }

    public static int getBest() {
        int min = populacao[0][cargas.length];
        int linha = 0;
        for (int i = 1; i < TAM; i++) {
            if (populacao[i][cargas.length] < min) {
                min = populacao[i][cargas.length];
                linha = i;
            }
        }

        for (int i = 0; i < cargas.length; i++)
            intermediaria[0][i] = populacao[linha][i];

        return linha;
    }

    public static int torneio() {
        Random rand = new Random();
        int individuo1, individuo2;

        individuo1 = rand.nextInt(TAM);
        individuo2 = rand.nextInt(TAM);

        if (populacao[individuo1][cargas.length] < populacao[individuo2][cargas.length])
            return individuo1;
        else
            return individuo2;
    }

    public static void crossover() {

        for (int j = 1; j < TAM; j = j + 2) {
            int ind1 = torneio();
            int ind2 = torneio();
            for (int k = 0; k < cargas.length / 2; k++) {
                intermediaria[j][k] = populacao[ind1][k];
                intermediaria[j + 1][k] = populacao[ind2][k];
            }
            for (int k = cargas.length / 2; k < cargas.length; k++) {
                intermediaria[j][k] = populacao[ind2][k];
                intermediaria[j + 1][k] = populacao[ind1][k];
            }
        }

    }

    public static void mutacao() {
        Random rand = new Random();
        int quant = rand.nextInt(3) + 1;
        for (int i = 0; i < quant; i++) {
            int individuo = rand.nextInt(TAM);
            int posicao = rand.nextInt(cargas.length);

            System.out.println("Cromossomo " + individuo + " sofreu mutação na carga de indice " + posicao);
            if (populacao[individuo][posicao] == 0) populacao[individuo][posicao] = 1;
            else populacao[individuo][posicao] = 0;
        }

    }

    public static boolean achouSolucao(int melhor) {
        if (populacao[melhor][cargas.length] == 0) {
            int soma = 0;
            System.out.println("\nAchou a solução ótima. Ela corresponde ao cromossomo :" + melhor);
            System.out.println("Solução Decodificada: ");
            System.out.println("Pessoa 0: ");
            for (int i = 0; i < cargas.length; i++)
                if (populacao[melhor][i] == 0) {
                    System.out.print(cargas[i] + " ");
                    soma = soma + cargas[i];
                }
            System.out.println(" - Total: " + soma);

            soma = 0;
            System.out.println("Pessoa 1: ");
            for (int i = 0; i < cargas.length; i++)
                if (populacao[melhor][i] == 1) {
                    System.out.print(cargas[i] + " ");
                    soma = soma + cargas[i];
                }
            System.out.println(" - Total: " + soma);
            return true;
        }
        return false;
    }
}
