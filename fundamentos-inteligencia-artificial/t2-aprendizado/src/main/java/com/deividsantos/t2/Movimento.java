package com.deividsantos.t2;

public enum Movimento {
    E(0),
    D(1),
    C(2),
    B(3),
    O(99);

    Integer value;

    Movimento(Integer value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Movimento getFromValue(int value) {
        for (int i = 0; i < values().length - 1; i++) {
            if (values()[i].getValue() == value) {
                return values()[i];
            }
        }
        return O;
    }
}
