package com.t2.redes;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        var file = """
                #NODE
                n1,00:00:00:00:00:01,192.168.0.2/24,192.168.0.1
                n2,00:00:00:00:00:02,192.168.0.3/24,192.168.0.1
                n3,00:00:00:00:00:03,192.168.1.2/24,192.168.1.1
                n4,00:00:00:00:00:04,192.168.1.3/24,192.168.1.1
                #ROUTER
                r1,2,00:00:00:00:00:05,192.168.0.1/24,00:00:00:00:00:06,192.168.1.1/24
                #ROUTERTABLE
                r1,192.168.0.0/24,0.0.0.0,0
                r2,192.168.0.0/24,0.0.0.0,0
                r1,192.168.1.0/24,0.0.0.0,1""";
        var lines = Arrays.asList(file.split("\n"));

        var nodes = new ArrayList<Node>();
        var routers = new ArrayList<Router>();
        var routerTables = new ArrayList<RouterTable>();
        var structure = new Structure();
        structure.build(lines, nodes, routers, routerTables);

        var source = "n1";
        var dest = "n2";

        var output = new ArrayList<>();
        var sourceNode = getNodeByName(nodes, source);
        var destNode = getNodeByName(nodes, dest);
        String ipToFind = null;
        if (isAtSameNetwork(sourceNode.ipAddress(), destNode.ipAddress())) {
            ipToFind = destNode.ipAddress();
            output.add(noteOverPrint(source, sourceNode.ipAddress(), ipToFind));
            sourceNode.arpTable().add(new NetInterface(destNode.ipAddress(), destNode.macAddress()));
            output.add(arpReply(sourceNode.name(), destNode.name(), destNode.ipAddress(), destNode.macAddress()));
        } else {
            ipToFind = sourceNode.defaultGateway();
            output.add(noteOverPrint(source, sourceNode.ipAddress(), ipToFind));
            for (Router router : routers) {
                for (NetInterface netInterface : router.netInterfaces()) {
                    if (cleanIp(netInterface.ip()).equalsIgnoreCase(ipToFind)) {
                        sourceNode.arpTable().add(netInterface);
                        output.add(arpReply(sourceNode.name(), router.name(), netInterface.ip(), netInterface.macAddress()));
                    }
                }
            }
        }
    }

    private static String arpReply(String sourceName, String destName, String destIp, String destMac) {
        return String.format("%s ->> %s : ARP Reply<br/>%s is at %s",
                destName, sourceName, cleanIp(destIp), destMac);
    }

    private static String noteOverPrint(String sourceName, String sourceIp, String destIp) {
        var source = cleanIp(sourceIp);
        return String.format("Note over %s : ARP Request<br/>Who has %s? Tell %s", sourceName, destIp, source);
    }

    private static String cleanIp(String sourceIp) {
        return sourceIp.replaceAll("/.*", "");
    }

    private static boolean isAtSameNetwork(String sourceIp, String destIp) {
        var splittedIpSource = sourceIp.split("\\.");
        var splittedIpDest = destIp.split("\\.");
        for (int i = 0; i < 3; i++) {
            if (!splittedIpSource[i].equalsIgnoreCase(splittedIpDest[i])) {
                return false;
            }
        }
        return true;
    }

    private static Node getNodeByName(ArrayList<Node> nodes, String name) {
        for (Node node : nodes) {
            if (node.name().equalsIgnoreCase(name)) {
                return node;
            }
        }
        throw new RuntimeException("Node not found");
    }
}
