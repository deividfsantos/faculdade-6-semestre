package com.t2.redes;

import java.util.List;

record Node(String name, String macAddress, String ipAddress, String defaultGateway, List<NetInterface> arpTable) {


    public void ping(Node destNode, List<Router> routers) {
        if (isAtSameNetwork(destNode.ipAddress)) {
            sendPackage("arp_request", null, destNode.ipAddress, null, null);
            sendPackage("echo_request", destNode, destNode.ipAddress, null, this);
        } else {
            sendPackage("arp_request", null, ipAddress, null, null);
            sendPackage("echo_request", destNode, destNode.ipAddress, null, null);
        }
    }


    private void sendPackage(String packageType, Node dest, String arpDestIp, Node startSource, Node finalDest) {
        if () {

        }
    }

    private boolean containsInArpTable(String ipAddress) {
        for (NetInterface netInterface :
                arpTable) {
            if (netInterface.ipAddress().contains(ipAddress)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAtSameNetwork(String destIp) {
        var maskSource = getMask(ipAddress);
        var maskDest = getMask(destIp);
        return maskSource.equalsIgnoreCase(maskDest);
    }

    public static String getMask(String destIp) {
        var destBytes = destIp.split("\\.");
        var prefix = Integer.parseInt(destIp.split("/")[1]);
        var result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (prefix / 8 > i) {
                result.append(destBytes[i]);
            } else {
                result.append("0");
            }
            if (i != 3) {
                result.append(".");
            }
        }
        return result.toString();
    }
}
