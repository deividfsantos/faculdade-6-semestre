package com.t2.redes;

public class Mask {

    public String getMaskBin(String destIp) {
        var destBytes = destIp.split("\\.");
        var prefix = Integer.parseInt(destIp.split("/")[1]);

        var byte1 = Integer.toBinaryString(Integer.parseInt(destBytes[0]));
        var byte2 = Integer.toBinaryString(Integer.parseInt(destBytes[1]));
        var byte3 = Integer.toBinaryString(Integer.parseInt(destBytes[2]));
        var byte4 = Integer.toBinaryString(Integer.parseInt(destBytes[3].replaceAll("/.*", "")));

        String byte1f = String.format("%8s", byte1).replace(" ", "0");
        String byte2f = String.format("%8s", byte2).replace(" ", "0");
        String byte3f = String.format("%8s", byte3).replace(" ", "0");
        String byte4f = String.format("%8s", byte4).replace(" ", "0");
        var bin1 = byte1f + byte2f + byte3f + byte4f;
        var result = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (i % 8 == 0 && i > 0) {
                result.append(".");
            }
            if (i < prefix) {
                result.append(bin1.charAt(i));
            } else {
                result.append("0");
            }
        }
        return result.toString();
    }

    public String getMask(String destIp) {
        var maskBin = getMaskBin(destIp);
        var prefix = Integer.parseInt(destIp.split("/")[1]);
        var split = maskBin.split("\\.");
        var byte1 = Integer.parseInt(split[0], 2);
        var byte2 = Integer.parseInt(split[1], 2);
        var byte3 = Integer.parseInt(split[2], 2);
        var byte4 = Integer.parseInt(split[3], 2);
        return String.format("%s.%s.%s.%s/%s", byte1, byte2, byte3, byte4, prefix);
    }
}
