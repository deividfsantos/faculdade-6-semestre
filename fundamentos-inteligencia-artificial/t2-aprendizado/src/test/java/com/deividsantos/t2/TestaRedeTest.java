package com.deividsantos.t2;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestaRedeTest {
    TestaRede testaRede = new TestaRede(new double[44], new int[][]{{9, 0, 0, 1, 0, 8, 0, 0, 8, 1},       //9 : entrada/saida;   8: moeda;    1:parede;  0:caminho livre
            {0, 1, 0, 8, 0, 1, 0, 1, 0, 8},
            {0, 0, 8, 0, 1, 1, 8, 0, 1, 1},
            {8, 1, 1, 0, 8, 1, 1, 8, 0, 1},
            {8, 0, 0, 0, 0, 1, 1, 0, 1, 1},
            {1, 1, 1, 1, 0, 1, 1, 0, 1, 1},
            {1, 0, 1, 1, 0, 1, 1, 8, 0, 8},
            {8, 0, 8, 8, 0, 1, 1, 1, 1, 1},
            {1, 8, 1, 8, 0, 0, 8, 8, 1, 1},
            {1, 8, 1, 8, 1, 8, 0, 0, 0, 9}
    });

    @Test
    void entorno() {
        double[] entorno1 = testaRede.entorno(0, 0);
        assertEquals(-1, entorno1[0]);
        assertEquals(-1, entorno1[1]);
        assertEquals(0, entorno1[2]);
        assertEquals(0, entorno1[3]);
        assertEquals(18, entorno1[4]);
    }

    @Test
    void entorno2() {
        double[] entorno2 = testaRede.entorno(5, 3);
        assertEquals(0, entorno2[0]);
        assertEquals(1, entorno2[1]);
        assertEquals(1, entorno2[2]);
        assertEquals(0, entorno2[3]);
        assertEquals(10, entorno2[4]);
    }

    @Test
    void entorno3() {
        double[] entorno3 = testaRede.entorno(-1, -1);
        assertEquals(-1, entorno3[0]);
        assertEquals(-1, entorno3[1]);
        assertEquals(-1, entorno3[2]);
        assertEquals(-1, entorno3[3]);
        assertEquals(20, entorno3[4]);
    }

    @Test
    void entorno4() {
        double[] entorno4 = testaRede.entorno(0, -1);
        assertEquals(-1, entorno4[0]);
        assertEquals(-1, entorno4[1]);
        assertEquals(-1, entorno4[2]);
        assertEquals(9, entorno4[3]);
        assertEquals(19, entorno4[4]);
    }

    @Test
    void entorno5() {
        double[] entorno5 = testaRede.entorno(-1, 0);
        assertEquals(-1, entorno5[0]);
        assertEquals(-1, entorno5[1]);
        assertEquals(9, entorno5[2]);
        assertEquals(-1, entorno5[3]);
        assertEquals(19, entorno5[4]);
    }

    @Test
    void entorno6() {
        double[] entorno6 = testaRede.entorno(10, 10);
        assertEquals(-1, entorno6[0]);
        assertEquals(-1, entorno6[1]);
        assertEquals(-1, entorno6[2]);
        assertEquals(-1, entorno6[3]);
        assertEquals(2, entorno6[4]);
    }

    @Test
    void entorno7() {
        double[] entorno7 = testaRede.entorno(9, 9);
        assertEquals(1, entorno7[0]);
        assertEquals(0, entorno7[1]);
        assertEquals(-1, entorno7[2]);
        assertEquals(-1, entorno7[3]);
        assertEquals(0, entorno7[4]);
    }

    @Test
    void entorno8() {
        double[] entorno8 = testaRede.entorno(10, 9);
        assertEquals(9, entorno8[0]);
        assertEquals(-1, entorno8[1]);
        assertEquals(-1, entorno8[2]);
        assertEquals(-1, entorno8[3]);
        assertEquals(1, entorno8[4]);
    }

    @Test
    void entorno9() {
        double[] entorno9 = testaRede.entorno(9, 10);
        assertEquals(-1, entorno9[0]);
        assertEquals(9, entorno9[1]);
        assertEquals(-1, entorno9[2]);
        assertEquals(-1, entorno9[3]);
        assertEquals(1, entorno9[4]);
    }

    @Test
    void entorno10() {
        double[] entorno10 = testaRede.entorno(-5, -3);
        assertEquals(-1, entorno10[0]);
        assertEquals(-1, entorno10[1]);
        assertEquals(-1, entorno10[2]);
        assertEquals(-1, entorno10[3]);
        assertEquals(26, entorno10[4]);
    }

    @Test
    void entorno11() {
        double[] entorno11 = testaRede.entorno(15, 12);
        assertEquals(-1, entorno11[0]);
        assertEquals(-1, entorno11[1]);
        assertEquals(-1, entorno11[2]);
        assertEquals(-1, entorno11[3]);
        assertEquals(9, entorno11[4]);
    }

    @Test
    void entorno12() {
        double[] entorno11 = testaRede.entorno(1, -1);
        assertEquals(-1, entorno11[0]);
        assertEquals(-1, entorno11[1]);
        assertEquals(-1, entorno11[2]);
        assertEquals(0, entorno11[3]);
        assertEquals(18, entorno11[4]);
    }

    @Test
    void entorno13() {
        double[] entorno11 = testaRede.entorno(1, -2);
        assertEquals(-1, entorno11[0]);
        assertEquals(-1, entorno11[1]);
        assertEquals(-1, entorno11[2]);
        assertEquals(-1, entorno11[3]);
        assertEquals(19, entorno11[4]);
    }

    @Test
    void entorno14() {
        double[] entorno11 = testaRede.entorno(10, 0);
        assertEquals(1, entorno11[0]);
        assertEquals(-1, entorno11[1]);
        assertEquals(-1, entorno11[2]);
        assertEquals(-1, entorno11[3]);
        assertEquals(10, entorno11[4]);
    }
}