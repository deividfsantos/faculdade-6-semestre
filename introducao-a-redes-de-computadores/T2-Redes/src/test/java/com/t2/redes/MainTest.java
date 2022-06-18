package com.t2.redes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void getMask1() {
        String mask = Main.getMask("192.168.0.0/24");
        assertEquals("192.168.0.0", mask);
    }

    @Test
    void getMask2() {
        String mask = Main.getMask("40.0.0.10/8");
        assertEquals("40.0.0.0", mask);
    }

    @Test
    void getMask3() {
        String mask = Main.getMask("40.3.4.10/16");
        assertEquals("40.3.0.0", mask);
    }
}