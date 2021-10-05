package com.deividsantos.ia.t;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

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

    private static final int TAM_POPULACAO = 23;

    private static int[][] populacao;

    private static int[][] intermediaria;

    public static final int LARGURA_TABULEIRO = 12;
    public static final int ALTURA_TABULEIRO = 12;

    public static final int INDICE_MAX_LARGURA_TABULEIRO = LARGURA_TABULEIRO - 1;
    public static final int INDICE_MAX_ALTURA_TABULEIRO = ALTURA_TABULEIRO - 1;

    private static char[][] matrizMovimento;

    private static final int tamanhoCaminhoMaximo = carga.split("0").length;

    public static void main(String[] args) {
        Random rand = new Random();
        populacao = new int[TAM_POPULACAO][tamanhoCaminhoMaximo + 1];
        intermediaria = new int[TAM_POPULACAO][tamanhoCaminhoMaximo + 1];
        int melhor;

        //cria a população inicial
        init();
        int i = 0;
        for (int g = 0; g < 1000000; g++) {
            calculaAptidao();
            melhor = getBest();
            if (achouSolucao(melhor)) break;
            crossover();
            populacao = intermediaria;
            mutacao(rand);
            i++;
            printCaminhos(g);
        }
        System.out.println("Iterações: " + i);
    }

    private static void printCaminhos(int g) {
        if (g % 100000 == 0) {
            printMatriz();
            printMelhorCaminho(0);
        }

        if (g > 1000000 - 50) {
            printMatriz();
            printMelhorCaminho(0);
        }
    }

    private static void mutacao(Random rand) {
        if (rand.nextInt(3) == 0) {
            mutacao();
        }
    }

    public static char[][] calcularTabuleiroMovimento() {
        char[][] tabuleiro = new char[LARGURA_TABULEIRO][ALTURA_TABULEIRO];
        String[] linhasTabuleiro = carga.split("\n");
        for (int i = 0; i < LARGURA_TABULEIRO; i++) {
            String[] colunasTabuleiro = linhasTabuleiro[i].split(" ");
            for (int j = 0; j < ALTURA_TABULEIRO; j++) {
                tabuleiro[LARGURA_TABULEIRO - 1 - i][j] = colunasTabuleiro[j].charAt(0);
            }
        }
        return tabuleiro;
    }

    public static void init() {
        Random rand = new Random();
        matrizMovimento = calcularTabuleiroMovimento();

        for (int i = 0; i < TAM_POPULACAO; i++) {
            for (int j = 0; j < tamanhoCaminhoMaximo; j++) {
                populacao[i][j] = rand.nextInt(4);
            }
        }
    }

    public static void printMatriz() {
        int j;
        for (int i = 0; i < TAM_POPULACAO; i++) {
            System.out.print("C: " + i + " - ");
            for (j = 0; j < tamanhoCaminhoMaximo; j++) {
                System.out.print(Movimento.getFromValue(populacao[i][j]) + " ");
            }
            System.out.println("F: " + populacao[i][j]);
        }
    }

    public static int aptidao(int indiceIndividuo) {
        int[] passosIndividuo = populacao[indiceIndividuo];
        int x = 0;
        int y = INDICE_MAX_ALTURA_TABULEIRO;
        int resultado = 0;

        List<Integer> pontosX = new ArrayList<>();
        List<Integer> pontosY = new ArrayList<>();

        boolean contemS = false;
        int descidaX = 0;
        int descidaY = 0;
        int contadorParede = 0;
        int contadorExterno = 0;
        for (int passo : passosIndividuo) {
            resultado -= 500;
            if (passo == 0) {
                x--;
            } else if (passo == 1) {
                x++;
            } else if (passo == 2) {
                y++;
            } else if (passo == 3) {
                y--;
            }

            for (int i = 0; i < pontosX.size(); i++) {
                if (pontosX.get(i) == x && pontosY.get(i) == y) {
                    resultado -= 6000;
                }
            }
            pontosX.add(x);
            pontosY.add(y);

            if (x < 0) {
                resultado -= 5000 * -x;
                contadorExterno++;
            }
            if (y < 0) {
                resultado -= 5000 * -y;
                contadorExterno++;
            }
            if (x > INDICE_MAX_LARGURA_TABULEIRO) {
                resultado -= 5000 * x - INDICE_MAX_LARGURA_TABULEIRO;
                contadorExterno++;
            }
            if (y > INDICE_MAX_ALTURA_TABULEIRO) {
                resultado -= 5000 * y - INDICE_MAX_ALTURA_TABULEIRO;
                contadorExterno++;
            }

            descidaX -= 30 * Math.pow(INDICE_MAX_LARGURA_TABULEIRO - x, 2);
            descidaY -= 30 * Math.pow(y, 2);

            if (x <= INDICE_MAX_LARGURA_TABULEIRO && x >= 0 && y <= INDICE_MAX_ALTURA_TABULEIRO && y >= 0) {
                if (matrizMovimento[y][x] == '1') {
                    resultado -= 15000;
                    contadorParede++;
                } else if (matrizMovimento[y][x] == '0') {
                    resultado += 1000;
                } else if (matrizMovimento[y][x] == 'S') {
                    contemS = true;
                    break;
                }
            }
        }

        if (!contemS) {
            resultado -= 150000;
            resultado += descidaX;
            resultado += descidaY;
        } else {
            resultado += 150000;
        }

        if (contemS && contadorParede == 0 && contadorExterno < 20) {
            resultado = 1000000001;
        }

        return resultado;
    }


    public static void calculaAptidao() {
        for (int i = 0; i < TAM_POPULACAO; i++) {
            populacao[i][tamanhoCaminhoMaximo] = aptidao(i);
        }
    }

    public static int getBest() {
        int max = populacao[0][tamanhoCaminhoMaximo];
        int linha = 0;
        for (int i = 1; i < TAM_POPULACAO; i++) {
            if (populacao[i][tamanhoCaminhoMaximo] > max) {
                max = populacao[i][tamanhoCaminhoMaximo];
                linha = i;
            }
        }

        for (int i = 0; i < tamanhoCaminhoMaximo; i++)
            intermediaria[0][i] = populacao[linha][i];

        return linha;
    }

    public static int torneio() {
        Random rand = new Random();
        int individuo1, individuo2;

        individuo1 = rand.nextInt(TAM_POPULACAO);
        individuo2 = rand.nextInt(TAM_POPULACAO);

        if (populacao[individuo1][tamanhoCaminhoMaximo] > populacao[individuo2][tamanhoCaminhoMaximo])
            return individuo1;
        else
            return individuo2;
    }

    public static void crossover() {
        for (int j = 1; j < TAM_POPULACAO; j = j + 2) {
            int ind1 = torneio();
            int ind2 = torneio();
            for (int k = 0; k < tamanhoCaminhoMaximo / 2; k++) {
                intermediaria[j][k] = populacao[ind1][k];
                intermediaria[j + 1][k] = populacao[ind2][k];
            }
            for (int k = tamanhoCaminhoMaximo / 2; k < tamanhoCaminhoMaximo; k++) {
                intermediaria[j][k] = populacao[ind2][k];
                intermediaria[j + 1][k] = populacao[ind1][k];
            }
        }
    }

    public static void mutacao() {
        Random rand = new Random();
        int quant = rand.nextInt(9) + 1;
        for (int i = 0; i < quant; i++) {
            int individuo = rand.nextInt(TAM_POPULACAO);
            int posicao = rand.nextInt(tamanhoCaminhoMaximo);

            int movimentacaoAleatoria = rand.nextInt(4);
            populacao[individuo][posicao] = movimentacaoAleatoria;
        }

    }

    public static boolean achouSolucao(int melhor) {
        if (populacao[melhor][tamanhoCaminhoMaximo] == 1000000001) {
            System.out.println("\nAchou a solução ótima. Ela corresponde ao cromossomo: " + melhor);
            System.out.println("Solução:");
            printMatriz();
            printMelhorCaminho(melhor);
            return true;
        }
        return false;
    }

    private static void printMelhorCaminho(int melhorCaminho) {
        int x = 0;
        int y = INDICE_MAX_ALTURA_TABULEIRO;
        int[] pontosX = new int[populacao[melhorCaminho].length - 2];
        int[] pontosY = new int[populacao[melhorCaminho].length - 2];
        for (int k = 0; k < populacao[melhorCaminho].length - 2; k++) {
            int passo = populacao[melhorCaminho][k];
            if (passo == 0) {
                x--;
            }
            if (passo == 1) {
                x++;
            }
            if (passo == 2) {
                y++;
            }
            if (passo == 3) {
                y--;
            }

            pontosX[k] = x;
            pontosY[k] = y;
        }

        for (int i = matrizMovimento.length - 1; i >= 0; i--) {
            for (int j = 0; j < matrizMovimento[i].length; j++) {
                boolean printou = false;
                boolean achouS = false;
                for (int k = 0; k < pontosX.length; k++) {
                    if (pontosX[k] == 11 && pontosY[k] == 0) {
                        achouS = true;
                    }
                    if (j == pontosX[k] && pontosY[k] == i && !achouS) {
                        System.out.print(ANSI_RED + matrizMovimento[i][j] + ANSI_RESET + " ");
                        printou = true;
                        break;
                    }
                }
                if (!printou) {
                    if (matrizMovimento[i][j] == 'E') {
                        System.out.print(ANSI_RED + 'E' + ANSI_RESET + " ");
                    } else if (achouS && matrizMovimento[i][j] == 'S') {
                        System.out.print(ANSI_RED + 'S' + ANSI_RESET + " ");
                    } else {
                        System.out.print(matrizMovimento[i][j] + " ");
                    }
                }
            }
            System.out.println();
        }
    }
}
