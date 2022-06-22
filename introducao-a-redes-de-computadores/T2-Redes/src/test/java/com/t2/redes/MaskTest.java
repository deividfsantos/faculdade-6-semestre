package com.t2.redes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MaskTest {

    Mask mask = new Mask();

    @Test
    void getMask1() {
        String mask = this.mask.getMaskBin("192.168.0.0/24");
        assertEquals("11000000.10101000.00000000.00000000", mask);
    }

    @Test
    void getMask2() {
        String mask = this.mask.getMaskBin("40.0.0.10/8");
        assertEquals("00101000.00000000.00000000.00000000", mask);
    }

    @Test
    void getMask3() {
        String mask = this.mask.getMaskBin("40.3.4.10/16");
        assertEquals("00101000.00000011.00000000.00000000", mask);
    }

    @Test
    void getMask4() {
        String mask1 = mask.getMaskBin("192.168.0.3/24");
        String mask2 = mask.getMaskBin("192.168.0.2/24");
        String mask3 = mask.getMaskBin("192.168.1.2/24");
        assertEquals("11000000.10101000.00000000.00000000", mask2);
        assertEquals("11000000.10101000.00000001.00000000", mask3);
        assertEquals(mask1, mask2);
        assertNotEquals(mask1, mask3);
    }
}