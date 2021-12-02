package com.deividsantos.t2;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AlgoritmoGenetico {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private double[][] populacao;
    private final int[][] labirinto;
    private final int tamanhoPopulacao = 20;
    private static final Integer TOTAL_ITERACOES = 2000;
    private final int tamanhoCaminho;


    private double[][] intermediaria;

    public AlgoritmoGenetico(Integer tamanhoCaminho, int[][] labirinto) {
        this.tamanhoCaminho = tamanhoCaminho;
        this.labirinto = labirinto;
    }

    public void execute() {
        int melhor = getBest();
        if (achouSolucao(melhor)) System.out.println("Achou");
        crossover();
        populacao = intermediaria;
        mutacao();
    }

    private void mutacao() {
        Random rand = new Random();
        if (rand.nextInt(3) == 0) {
            int quant = rand.nextInt(10) + 1;
            for (int i = 0; i < quant; i++) {
                int individuo = rand.nextInt(tamanhoPopulacao - 1) + 1;
                int posicao = rand.nextInt(tamanhoCaminho);

                double novoPeso = rand.nextDouble() * 2 - 1;
                populacao[individuo][posicao] = novoPeso;
            }
        }
    }

    public void init() {
        Random rand = new Random();
        this.populacao = new double[tamanhoPopulacao][tamanhoCaminho + 1];
        this.intermediaria = new double[tamanhoPopulacao][tamanhoCaminho + 1];
        for (int i = 0; i < tamanhoPopulacao; i++) {
            for (int j = 0; j < tamanhoCaminho; j++) {
                double weigth = rand.nextDouble() * 2 - 1;
                populacao[i][j] = weigth;
            }
        }
    }

    public double aptidao(List<Integer> passosX, List<Integer> passosY, int quantidadeMoedas) {
        double resultado = 0;
        int totalPassos = passosX.size();
        Integer ultimoPassoX = passosX.get(passosX.size() - 1);
        Integer ultimoPassoY = passosY.get(passosY.size() - 1);
        for (int i = 0; i < passosX.size(); i++) {
            for (int j = i + 1; j < passosX.size(); j++) {
                if (Objects.equals(passosX.get(i), passosX.get(j)) && Objects.equals(passosY.get(i), passosY.get(j))) {
                    resultado--;
                }
            }
        }
        if (ultimoPassoX == 9 && ultimoPassoY == 9) {
            resultado = 99999;
        }
        int distancia = distancia(ultimoPassoX, ultimoPassoY, 9, 9);
        resultado += totalPassos - ((double) distancia / 5) + quantidadeMoedas;
        if(resultado > 5){
//            System.out.println("EHOSGURI");
        }
        return resultado;
    }

    public void calculaAptidao(int indiceIndividuo, List<Integer> passosX, List<Integer> passosY, int quantidadeMoedas) {
        double aptidao = aptidao(passosX, passosY, quantidadeMoedas);
        populacao[indiceIndividuo][tamanhoCaminho] = aptidao;
    }


    public void printMatriz() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tamanhoPopulacao; i++) {
            builder.append("F: ").append(populacao[i][tamanhoCaminho]).append("\n");
        }
        System.out.println(builder);
    }

    public int getBest() {
        double max = populacao[0][tamanhoCaminho];
        int linha = 0;
        for (int i = 1; i < tamanhoPopulacao; i++) {
            if (populacao[i][tamanhoCaminho] > max) {
                max = populacao[i][tamanhoCaminho];
                linha = i;
            }
        }

        for (int i = 0; i < tamanhoCaminho + 1; i++)
            intermediaria[0][i] = populacao[linha][i];

        return linha;
    }

    public int torneio() {
        Random rand = new Random();
        int individuo1, individuo2;

        individuo1 = rand.nextInt(tamanhoPopulacao);
        individuo2 = rand.nextInt(tamanhoPopulacao);

        if (populacao[individuo1][tamanhoCaminho] > populacao[individuo2][tamanhoCaminho])
            return individuo1;
        else
            return individuo2;
    }

    public void crossover() {
        for (int j = 1; j < tamanhoPopulacao; j++) {
            int ind1 = torneio();
            int ind2 = torneio();
            for (int k = 0; k < tamanhoCaminho; k++) {
                double peso1 = populacao[ind1][k];
                double peso2 = populacao[ind2][k];
                intermediaria[ind1][k] = (peso1 + peso2) / 2;
            }
        }
    }

    public boolean achouSolucao(int melhor) {
        if (populacao[melhor][tamanhoCaminho] == 1000000001) {
            System.out.println("\nAchou a solução ótima. Ela corresponde ao cromossomo: " + melhor);
            System.out.println("Solução:");
            return true;
        }
        return false;
    }

    public int getTamanhoPopulacao() {
        return tamanhoPopulacao;
    }

    public double[][] getPopulacao() {
        return populacao;
    }


    public int distancia(int linhaOrigem, int colunaOrigem, int linhaDestino, int colunaDestino) {
        return Math.abs(linhaOrigem - linhaDestino) + Math.abs(colunaOrigem - colunaDestino);
    }
}
