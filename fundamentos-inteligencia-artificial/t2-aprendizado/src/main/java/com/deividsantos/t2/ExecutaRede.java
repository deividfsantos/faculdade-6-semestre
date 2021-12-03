package com.deividsantos.t2;

import java.util.ArrayList;
import java.util.List;

/**
 * Escreva a descrição da classe TestaRede aqui.
 *
 * @author Silvia
 * @version 12/11/2020
 */

public class ExecutaRede {
    private final int[][] labirinto;
    private Rede rn;
    private final int linhaSaida;
    private final int colunaSaida;

    private static final Integer ENTRADAS = 5;
    private static final Integer ENTRADAS_MAIS_BIAS = 6;
    private static final Integer NEURONIOS_OCULTA = 6;
    private static final Integer NEURONIOS_SAIDA = 4;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    public ExecutaRede(double[] cromossomo, int[][] labirinto) {
        //Labirinto de teste
        this.labirinto = labirinto;
        linhaSaida = colunaSaida = 9;                          //Coordenadas da célula de saída

        //Configurações da rede
        rn = new Rede(NEURONIOS_OCULTA, NEURONIOS_SAIDA);  //topologia da rede: 8 neurônios na camada oculta e 4 na de saída

        //Setando os pesos na rede
        rn.setPesosNaRede(ENTRADAS, cromossomo);
    }

    public double[] entorno(int linhaAgente, int colunaAgente) {
        double[] visao = new double[ENTRADAS];
        int ind = 0;
        //buscando percepção

        if (linhaAgente - 1 < 0 || colunaAgente >= labirinto[0].length) visao[ind] = -1;           //em cima
        else if (colunaAgente < 0) visao[ind] = -1;
        else visao[ind] = labirinto[linhaAgente - 1][colunaAgente];    //conteúdo célula
        ind++;

        if (colunaAgente - 1 < 0 || linhaAgente >= labirinto.length) visao[ind] = -1;          //esquerda
        else visao[ind] = labirinto[linhaAgente][colunaAgente - 1];    //conteúdo célula
        ind++;

        if (linhaAgente + 1 >= labirinto.length || colunaAgente < 0) visao[ind] = -1;             //abaixo
        else if (colunaAgente + 1 >= labirinto[0].length || linhaAgente + 1 < 0) visao[ind] = -1;
        else visao[ind] = labirinto[linhaAgente + 1][colunaAgente];    //conteúdo célula
        ind++;

        if (colunaAgente + 1 >= labirinto[0].length || linhaAgente < 0) visao[ind] = -1;         //direita
        else if (colunaAgente + 1 < 0 || linhaAgente >= labirinto.length) visao[ind] = -1;
        else visao[ind] = labirinto[linhaAgente][colunaAgente + 1];    //conteúdo célula
        ind++;

        visao[ind] = distancia(linhaAgente, colunaAgente, linhaSaida, colunaSaida);  //distancia da saída
        return visao;
    }

    public int distancia(int linhaOrigem, int colunaOrigem, int linhaDestino, int colunaDestino) {
        return Math.abs(linhaOrigem - linhaDestino) + Math.abs(colunaOrigem - colunaDestino);
    }

    public static void main(String[] args) {
        int[][] labirinto = {{9, 0, 0, 1, 0, 8, 0, 0, 8, 1},       //9 : entrada/saida;   8: moeda;    1:parede;  0:caminho livre
                {0, 1, 0, 8, 0, 1, 0, 1, 0, 8},
                {0, 0, 8, 0, 1, 1, 8, 0, 1, 1},
                {8, 1, 1, 0, 8, 1, 1, 8, 0, 1},
                {8, 0, 0, 0, 0, 1, 1, 0, 1, 1},
                {1, 1, 1, 1, 0, 1, 1, 0, 1, 1},
                {1, 0, 1, 1, 0, 1, 1, 8, 0, 8},
                {8, 0, 8, 8, 0, 1, 1, 1, 1, 1},
                {1, 8, 1, 8, 0, 0, 8, 8, 1, 1},
                {1, 8, 1, 8, 1, 8, 0, 0, 0, 9}
        };
        int totalPesos = ENTRADAS_MAIS_BIAS * NEURONIOS_OCULTA + (NEURONIOS_OCULTA + 1) * NEURONIOS_SAIDA;  //int totalPesos = 9 * 8 + 9 * 4;  //9= 8 entradas + bias , 8 neuronios na camada oculta e 4 na camada de saída
        AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(totalPesos, labirinto);
        algoritmoGenetico.init();

        for (int j = 0; j < 200000; j++) {
            if (j % 50 == 0) {
                System.out.println("Geração: " + j);
            }
            for (int i = 0; i < algoritmoGenetico.getTamanhoPopulacao(); i++) {
                ExecutaRede executaRede = new ExecutaRede(algoritmoGenetico.getPopulacao()[i], labirinto);
                executarRede(executaRede, algoritmoGenetico, i, j % 400 == 0 && i == 0);
            }

            algoritmoGenetico.execute();
        }
    }

    private static void executarRede(ExecutaRede executaRede, AlgoritmoGenetico algoritmoGenetico, int indiceIndividuo, boolean shouldPrint) {
        List<Integer> posicoesX = new ArrayList<>();
        List<Integer> posicoesY = new ArrayList<>();
        posicoesX.add(0);
        posicoesY.add(0);
        int posX = 0, posY = 0;    //posições atuais do agente

        for (int k = 0; k < 1000; k++) {

            double[] percepcao = executaRede.entorno(posX, posY);

            double[] saida = executaRede.rn.propagacao(percepcao);

            int indMaior = 0;
            for (int i = 0; i < NEURONIOS_SAIDA; i++) {
                if (saida[i] > saida[indMaior]) {
                    indMaior = i;
                }
            }
            if (indMaior == 0) {
                posX--;
            }
            if (indMaior == 1) {
                posY--;
            }
            if (indMaior == 2) {
                posX++;
            }
            if (indMaior == 3) {
                posY++;
            }

            int quantidadeMoedas = 0;
            if (posX >= 0 && posY >= 0 && posX <= 9 && posY <= 9) {
                if (executaRede.labirinto[posX][posY] == 9) {
                    quantidadeMoedas++;
                }
            }

            posicoesX.add(posX);
            posicoesY.add(posY);

            if (posX < 0 || posX > 9 || posY < 0 || posY > 9
                    || (posX == 9 && posY == 9)
                    || executaRede.labirinto[posX][posY] == 1) {
                algoritmoGenetico.calculaAptidao(indiceIndividuo, posicoesX, posicoesY, quantidadeMoedas);
                break;
            }
        }

        if (shouldPrint) {
            algoritmoGenetico.printMatriz();
            printLabirinto(executaRede, posicoesX, posicoesY);
        }
    }

    private static void printLabirinto(ExecutaRede teste, List<Integer> posicoesX, List<Integer> posicoesY) {
        int moedasColetadas = 0;
        StringBuilder builder = new StringBuilder();
        int[][] labirinto = teste.labirinto;
        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[0].length; j++) {
                for (int k = 0; k < posicoesX.size(); k++) {
                    if (posicoesX.get(k) == i && posicoesY.get(k) == j) {
                        builder.append(ANSI_RED);
                        if (labirinto[i][j] == 8) {
                            moedasColetadas++;
                        }
                    }
                }

                if (labirinto[i][j] == 0) {
                    builder.append("0 ");
                }
                if (labirinto[i][j] == 1) {
                    builder.append("1 ");
                }
                if (labirinto[i][j] == 9) {
                    builder.append("S ");
                }
                if (labirinto[i][j] == 8) {
                    builder.append("M ");
                }
                builder.append(ANSI_RESET);
            }
            builder.append("\n");
        }
        System.out.println(builder);

        System.out.println("Total de moedas coletadas: " + moedasColetadas * 50);
        System.out.println();
    }
}
