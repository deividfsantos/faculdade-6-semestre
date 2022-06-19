package com.t2.redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        // --------------------------------------------------------
        String command = "ping";
        var sourceName = "n1";
        var destName = "n3";
        var output = new ArrayList<>();
        var sourceNode = getNodeByName(nodes, sourceName);
        var destNode = getNodeByName(nodes, destName);
        int ttl = 8;


        var actualSourceRouter = (Router) null;
        var actualDestRouter = (Router) null;
        var actualSourceNode = sourceNode;
        var actualDestNode = destNode;

        var icmpReqStack = new LinkedList<>();
        var hardwareType = HardwareType.NODE;
        for (int i = 0; i < 6; i++) {
            if (isAtSameNetwork(sourceNode.ipAddress(), destNode.ipAddress())) {
                if (notContainsInArpTable(sourceNode, actualDestRouter, destNode.ipAddress(), hardwareType)) {
                    output.add(noteOverPrint(sourceName, destNode.ipAddress(), sourceNode.ipAddress()));
                    sourceNode.arpTable().add(new NetInterface(destNode.ipAddress(), destNode.macAddress()));
                    output.add(arpReply(destNode.name(), sourceNode.name(), destNode.ipAddress(), destNode.macAddress()));
                    destNode.arpTable().add(new NetInterface(sourceNode.ipAddress(), sourceNode.macAddress()));
                }
            } else {
                if (notContainsInArpTable(actualSourceNode, actualDestRouter, actualSourceNode.defaultGateway(), hardwareType)) {
                    if (hardwareType == HardwareType.ROUTER) {
                        RouterTableLine routerTableLineDest = getRouterDest(actualDestRouter, destNode.ipAddress());
                        if (routerTableLineDest.netDest().contains("0.0.0.0")) {
                            //Router to Router
                            output.add(noteOverPrint(actualDestRouter.name(), routerTableLineDest.nextHop(), actualDestRouter.netInterfaces().get(routerTableLineDest.port()).ip()));
                            var router = getRouter(routers, actualSourceNode.defaultGateway());
                            var netInterface = getNetInterface(router.netInterfaces(), actualSourceNode.defaultGateway());

                            output.add(arpReply(actualDestRouter.name(), router.name(), netInterface.ip(), netInterface.macAddress()));
                            actualDestRouter.arpTable().add(netInterface);
                            sourceNode.arpTable().add(netInterface);
                            actualDestRouter = router;
                        } else {
                            //Node to Router
                            NetInterface currentNet = actualDestRouter.netInterfaces().get(routerTableLineDest.port());
                            output.add(noteOverPrint(actualDestRouter.name(), destNode.ipAddress(), currentNet.ip()));
                            output.add(arpReply(destNode.name(), actualDestRouter.name(), destNode.ipAddress(), destNode.macAddress()));
                            actualDestRouter.arpTable().add(new NetInterface(destNode.ipAddress(), destNode.macAddress()));
                            destNode.arpTable().add(new NetInterface(currentNet.ip(), currentNet.macAddress()));
                            hardwareType = HardwareType.NODE;
                        }
                    } else {
                        output.add(noteOverPrint(sourceName, actualSourceNode.defaultGateway(), sourceNode.ipAddress()));
                        var router = getRouter(routers, actualSourceNode.defaultGateway());
                        var netInterface = getNetInterface(router.netInterfaces(), actualSourceNode.defaultGateway());
                        actualSourceNode.arpTable().add(netInterface);
                        router.arpTable().add(new NetInterface(actualSourceNode.ipAddress(), actualSourceNode.macAddress()));
                        output.add(arpReply(router.name(), actualSourceNode.name(), netInterface.ip(), netInterface.macAddress()));
                        actualDestRouter = router;
                    }
                } else {
                    if (hardwareType == HardwareType.NODE) {
                        output.add(icmpRequest(actualDestRouter.name(), actualDestNode.name(), sourceNode.ipAddress(), destNode.ipAddress(), ttl));
                    } else {
                        output.add(icmpRequest(actualSourceNode.name(), actualDestNode.name(), sourceNode.ipAddress(), destNode.ipAddress(), ttl));
                    }
                    hardwareType = HardwareType.ROUTER;
                }
            }
        }

        for (var item : output) {
            System.out.println(item);
        }
    }

    private static NetInterface getNetInterface(List<NetInterface> netInterfaces, String defaultGateway) {
        for (NetInterface netInterface :
                netInterfaces) {
            if (netInterface.ip().contains(defaultGateway)) {
                return netInterface;
            }
        }
        throw new RuntimeException("Net Interface not found");
    }

    private static Router getRouter(List<Router> routers, String defaultGateway) {
        for (Router router :
                routers) {
            for (NetInterface netInterface :
                    router.netInterfaces()) {
                if (netInterface.ip().contains(defaultGateway)) {
                    return router;
                }
            }
        }
        throw new RuntimeException("Router not found");
    }

    private static RouterTableLine getRouterDest(Router actualRouter, String destIp) {
        return actualRouter.routerTable().routerTableLines()
                .stream()
                .filter(routerTableLine -> routerTableLine.netDest().contains(getMask(destIp)))
                .findFirst()
                .orElse(actualRouter.routerTable().routerTableLines().get(actualRouter.routerTable().routerTableLines().size() - 1));
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

    private static boolean notContainsInArpTable(Node sourceNode, Router router, String destIp, HardwareType hardwareType) {
        if (hardwareType == HardwareType.ROUTER) {
            return router.arpTable().stream().noneMatch(netInterface -> netInterface.ip().contains(destIp));
        }
        return sourceNode.arpTable().stream().noneMatch(netInterface -> netInterface.ip().contains(destIp));
    }

    private static String icmpRequest(String source, String dest, String srcIP, String destIP, int ttl) {
        String cleanedSourceIp = cleanIp(srcIP);
        String cleanedDestIp = cleanIp(destIP);
        return String.format("%s ->> %s : ICMP Echo Request<br/>src=%s dst=%s ttl=%d", source, dest, cleanedSourceIp, cleanedDestIp, ttl);
    }

    private static String icmpReply(String source, String dest, String srcIP, String destIP, int ttl) {
        return String.format("%s ->> %s : ICMP Echo Reply<br/>src=%s dst=%s ttl=%d", source, dest, srcIP, destIP, ttl);
    }

    private static String icmpTimeExceeded(String source, String dest, String srcIP, String destIP, int ttl) {
        return String.format("%s ->> %s : ICMP Time Exceeded<br/>src=%s dst=%s ttl=%d", source, dest, srcIP, destIP, ttl);
    }

    private static String arpReply(String source, String dest, String destIp, String destMac) {
        return String.format("%s ->> %s : ARP Reply<br/>%s is at %s", source, dest, cleanIp(destIp), destMac);
    }

    private static String noteOverPrint(String sourceName, String destIp, String sourceIp) {
        var source = cleanIp(sourceIp);
        var dest = cleanIp(destIp);
        return String.format("Note over %s : ARP Request<br/>Who has %s? Tell %s", sourceName, dest, source);
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
