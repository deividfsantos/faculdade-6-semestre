package com.t1redes;

public class Main {

    public static void main(String[] args) {
        String algo = "saw";
        String seqbits = "1";
        String num_frames = "10";
        String lost_pkts = "3,10,15";

        SAW saw = new SAW();
        saw.saw(algo, seqbits, num_frames, lost_pkts);

    }
}
