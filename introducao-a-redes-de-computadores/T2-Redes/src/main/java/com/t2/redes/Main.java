package com.t2.redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        var file = """
                #NODE
                n1,00:00:00:00:00:01,10.0.0.10/8,10.0.0.1
                n2,00:00:00:00:00:02,40.0.0.10/8,40.0.0.1
                #ROUTER
                r1,3,00:00:00:00:00:10,10.0.0.1/8,00:00:00:00:00:11,20.0.0.1/8,00:00:00:00:00:12,50.0.0.1/8
                r2,2,00:00:00:00:00:20,20.0.0.2/8,00:00:00:00:00:21,30.0.0.1/8
                r3,3,00:00:00:00:00:30,30.0.0.2/8,00:00:00:00:00:31,40.0.0.1/8,00:00:00:00:00:32,50.0.0.2/8
                #ROUTERTABLE
                r1,10.0.0.0/8,0.0.0.0,0
                r1,20.0.0.0/8,0.0.0.0,1
                r1,50.0.0.0/8,0.0.0.0,2
                r1,0.0.0.0/0,20.0.0.2,1
                r2,20.0.0.0/8,0.0.0.0,0
                r2,30.0.0.0/8,0.0.0.0,1
                r2,10.0.0.0/8,20.0.0.1,0
                r2,0.0.0.0/0,30.0.0.2,1
                r3,30.0.0.0/8,0.0.0.0,0
                r3,40.0.0.0/8,0.0.0.0,1
                r3,50.0.0.0/8,0.0.0.0,2
                r3,0.0.0.0/0,50.0.0.1,2
                """;

        var lines = Arrays.asList(file.split("\n"));

        var nodes = new ArrayList<Node>();
        var routers = new ArrayList<Router>();
        var routerTables = new ArrayList<RouterTable>();
        var structure = new Structure();
        structure.build(lines, nodes, routers, routerTables);
        // --------------------------------------------------------
        String command = "ping";
        var sourceName = "n1";
        var destName = "n2";

        Node sourceNode = getNodeByName(nodes, sourceName);
        Node destNode = getNodeByName(nodes, destName);
        sourceNode.ping(destNode);
    }

    private static NetInterface getNetInterface(Node currentDest, Router currentRouter, List<RouterTableLine> routerTableLines) {
        for (RouterTableLine routerTableLine : routerTableLines) {
            if (getMask(routerTableLine.netDest()).equalsIgnoreCase(getMask(currentDest.ipAddress()))) {
                Integer port = routerTableLine.port();
                return currentRouter.netInterfaces().get(port);
            }
        }
        return currentRouter.netInterfaces().get(routerTableLines.get(routerTableLines.size() - 1).port());
    }


    private static boolean notContainsInArpTable(List<NetInterface> arpTable, String ipAddress) {
        for (NetInterface netInterface :
                arpTable) {
            if (netInterface.ipAddress().contains(ipAddress)) {
                return false;
            }
        }
        return true;
    }

    private static NetInterface getNetInterface(List<NetInterface> netInterfaces, String defaultGateway) {
        for (NetInterface netInterface : netInterfaces) {
            if (netInterface.ipAddress().contains(defaultGateway)) {
                return netInterface;
            }
        }
        throw new RuntimeException("Net Interface not found");
    }

    private static Router getRouter(List<Router> routers, String defaultGateway) {
        for (Router router : routers) {
            for (NetInterface netInterface : router.netInterfaces()) {
                if (netInterface.ipAddress().contains(defaultGateway)) {
                    return router;
                }
            }
        }
        throw new RuntimeException("Router not found");
    }

    private static RouterTableLine getRouterDest(Router actualRouter, String destIp) {
        return actualRouter.routerTable().routerTableLines().stream().filter(routerTableLine -> routerTableLine.netDest().contains(getMask(destIp))).findFirst().orElse(actualRouter.routerTable().routerTableLines().get(actualRouter.routerTable().routerTableLines().size() - 1));
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

    private static String icmpRequestMessage(String source, String dest, String srcIP, String destIP, int ttl) {
        String cleanedSourceIp = cleanIp(srcIP);
        String cleanedDestIp = cleanIp(destIP);
        return String.format("%s ->> %s : ICMP Echo Request<br/>src=%s dst=%s ttl=%d", source, dest, cleanedSourceIp, cleanedDestIp, ttl);
    }

    private static String icmpReplyMessage(String source, String dest, String srcIP, String destIP, int ttl) {
        var cleanedSrcIP = cleanIp(srcIP);
        var cleanedDstIP = cleanIp(destIP);
        return String.format("%s ->> %s : ICMP Echo Reply<br/>src=%s dst=%s ttl=%d", source, dest, cleanedSrcIP, cleanedDstIP, ttl);
    }

    private static String icmpTimeExceeded(String source, String dest, String srcIP, String destIP, int ttl) {
        return String.format("%s ->> %s : ICMP Time Exceeded<br/>src=%s dst=%s ttl=%d", source, dest, srcIP, destIP, ttl);
    }

    private static String arpReplyMessage(String source, String dest, String destIp, String destMac) {
        return String.format("%s ->> %s : ARP Reply<br/>%s is at %s", source, dest, cleanIp(destIp), destMac);
    }

    private static String arpRequestMessage(String sourceName, String destIp, String sourceIp) {
        var source = cleanIp(sourceIp);
        var dest = cleanIp(destIp);
        return String.format("Note over %s : ARP Request<br/>Who has %s? Tell %s", sourceName, dest, source);
    }

    private static String cleanIp(String sourceIp) {
        return sourceIp.replaceAll("/.*", "");
    }

    private static boolean isAtSameNetwork(String sourceIp, String destIp) {
        var maskSource = getMask(sourceIp);
        var maskDest = getMask(destIp);
        return maskSource.equalsIgnoreCase(maskDest);
    }

    private static Node getNodeByName(List<Node> nodes, String name) {
        for (Node node : nodes) {
            if (node.name().equalsIgnoreCase(name)) {
                return node;
            }
        }
        throw new RuntimeException("Node not found");
    }
}
