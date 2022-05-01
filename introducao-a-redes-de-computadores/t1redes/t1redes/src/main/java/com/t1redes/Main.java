package com.t1redes;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String algo = "saw";
        String seqbits = "2";
        String num_frames = "6";
        String lost_pkts = "3";

        GBN gbn = new GBN(algo, seqbits, num_frames, lost_pkts);
        gbn.gbn().forEach(System.out::println);
    }
}
