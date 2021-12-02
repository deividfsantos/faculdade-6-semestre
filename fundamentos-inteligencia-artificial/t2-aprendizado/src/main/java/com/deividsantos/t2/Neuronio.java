package com.deividsantos.t2;

/**
 * Escreva a descrição da classe Neuronio aqui.
 *
 * @author Silvia
 * @version 12/11/2020
 */
public class Neuronio {
    private double[] pesos;      //Número de pesos = número de entradas + 1
    private int defineFuncao;   //0: é a default -> logistica

    public Neuronio(double[] pesos) {
        setPesos(pesos);
        defineFuncao = 0;
    }

    public void setPesos(double[] pesos) {
        this.pesos = pesos;
    }

    public void setFuncao(int defineFuncao) {
        this.defineFuncao = defineFuncao;
    }

    public double calculaY(double[] x) {
        double v = 0;
        int i;
        for (i = 0; i < x.length; i++) {
            v = v + pesos[i] * x[i];
        }
        v = v + pesos[i];  //bias

        switch (defineFuncao) {
            case 1:
                return tangenteHiperbolica(v);
            default:
                return logistica(v);
        }
    }

    public double logistica(double v) {
        return 1 / (1 + Math.exp(-v));
    }

    public double tangenteHiperbolica(double v) {
        return Math.tanh(v);
    }

    public String toString() {
        String msg = "";
        for (int i = 0; i < pesos.length; i++) msg = msg + pesos[i] + " ";
        return msg;
    }
}
