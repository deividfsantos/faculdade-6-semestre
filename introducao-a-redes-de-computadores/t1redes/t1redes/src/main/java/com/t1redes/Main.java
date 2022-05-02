package com.t1redes;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args == null || args.length < 4 || args[4] == null || args[4].isEmpty() || args[1] == null || args[1].isEmpty()
                || args[2] == null || args[2].isEmpty() || args[3] == null || args[3].isEmpty()) {
            System.out.println("Todos os parâmetros devem ser preenchidos.");
            System.exit(0);
        }
        String algo = args[1];
        String seqbits = args[2];
        String num_frames = args[3];
        String lost_pkts = args[4];

        if (algo.equalsIgnoreCase("saw")) {
            SAW saw = new SAW(seqbits, num_frames, lost_pkts);
            List<String> saw1 = saw.saw();
            saw1.forEach(System.out::println);
        } else if (algo.equalsIgnoreCase("gbn")) {
            GBN gbn = new GBN(seqbits, num_frames, lost_pkts);
            List<String> gbn1 = gbn.gbn();
            gbn1.forEach(System.out::println);
        } else if (algo.equalsIgnoreCase("sr")) {
            System.out.println("Não implementado");
        }
    }
}
