package com.t2.redes;

import java.util.ArrayList;
import java.util.Arrays;
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
        run(nodes, routers, routerTables);
    }

    public static List<String> run(List<Node> nodes, List<Router> routers, List<RouterTable> routerTables) {
        String command = "ping";
        var sourceName = "n1";
        var destName = "n3";
        var output = new ArrayList<String>();
        var sourceNode = getNodeByName(nodes, sourceName);
        var destNode = getNodeByName(nodes, destName);

        var currentSource = sourceNode;
        var currentDest = destNode;

        var currentRouter = (Router) null;

        var sourceMode = HardwareType.NODE;
        boolean replyMode = false;

        for (int i = 0; i < 10; i++) {
            if (sourceMode == HardwareType.ROUTER) {
                for (NetInterface netInterface : currentRouter.netInterfaces()) {
                    if (isAtSameNetwork(netInterface.ipAddress(), currentDest.ipAddress())) {
                        if (notContainsInArpTable(currentRouter.arpTable(), currentDest.ipAddress())) {
                            output.add(arpRequestMessage(currentRouter.name(), currentDest.ipAddress(), netInterface.ipAddress()));
                            output.add(arpReplyMessage(currentDest.name(), currentRouter.name(), currentDest.ipAddress(), currentDest.macAddress()));
                            currentRouter.arpTable().add(new NetInterface(currentDest.ipAddress(), currentDest.macAddress()));
                            currentDest.arpTable().add(new NetInterface(netInterface.ipAddress(), netInterface.macAddress()));
                        } else if (!replyMode){
                            output.add(icmpRequestMessage(currentRouter.name(), currentDest.name(), sourceNode.ipAddress(), destNode.ipAddress(), 8));
                            currentSource = currentDest;
                            currentDest = sourceNode;
                            replyMode = true;
                            sourceMode = HardwareType.NODE;
                        }
                    }
                }

//                if (destNode.ipAddress().equalsIgnoreCase(currentDest.ipAddress())){
//                    var temp = currentSource;
//                    currentSource = currentDest;
//                    currentDest = temp;
//                    if (isAtSameNetwork(sourceNode.ipAddress(), currentDest.ipAddress())) {
//                    } else {
//                        if (notContainsInArpTable(currentSource.arpTable(), currentSource.defaultGateway())) {
//                        } else {
//                            Router router = getRouter(routers, currentSource.defaultGateway());
//                            output.add(icmpReplyMessage(currentSource.name(), router.name(), destNode.ipAddress(), sourceNode.ipAddress(), 8));
//                            currentRouter = router;
//                        }
//                    }
//                }
            } else {
                if (isAtSameNetwork(currentSource.ipAddress(), currentDest.ipAddress())) {
                    if (notContainsInArpTable(currentSource.arpTable(), currentDest.ipAddress())) {
                        output.add(arpRequestMessage(currentSource.name(), currentDest.ipAddress(), currentSource.ipAddress()));
                        output.add(arpReplyMessage(currentDest.name(), currentSource.name(), currentDest.ipAddress(), currentDest.macAddress()));
                        currentSource.arpTable().add(new NetInterface(currentDest.ipAddress(), currentDest.macAddress()));
                    }
                    output.add(icmpRequestMessage(sourceName, destName, currentSource.ipAddress(), currentDest.ipAddress(), 8));
                    output.add(icmpReplyMessage(destName, sourceName, currentDest.ipAddress(), currentSource.ipAddress(), 8));
                } else {
                    if (notContainsInArpTable(currentSource.arpTable(), currentSource.defaultGateway())) {
                        Router router = getRouter(routers, currentSource.defaultGateway());
                        output.add(arpRequestMessage(currentSource.name(), currentSource.defaultGateway(), currentSource.ipAddress()));
                        for (NetInterface netInterface : router.netInterfaces()) {
                            if (netInterface.ipAddress().contains(currentSource.defaultGateway())) {
                                output.add(arpReplyMessage(router.name(), currentSource.name(), netInterface.ipAddress(), netInterface.macAddress()));
                                currentSource.arpTable().add(netInterface);
                                router.arpTable().add(new NetInterface(currentSource.ipAddress(), currentSource.macAddress()));
                            }
                        }
                    } else {
                        Router router = getRouter(routers, currentSource.defaultGateway());
                        output.add(icmpRequestMessage(currentSource.name(), router.name(), sourceNode.ipAddress(), destNode.ipAddress(), 8));
                        currentRouter = router;
                        sourceMode = HardwareType.ROUTER;
                    }
                }
                if(currentSource.ipAddress().equalsIgnoreCase(destNode.ipAddress()) && replyMode) {
                    if (isAtSameNetwork(currentSource.ipAddress(), currentDest.ipAddress())) {

                    } else {
                        if (notContainsInArpTable(currentSource.arpTable(), currentSource.defaultGateway())) {
                        } else {
                            Router router = getRouter(routers, currentSource.defaultGateway());
                            output.add(icmpReplyMessage(currentSource.name(), router.name(), destNode.ipAddress(), sourceNode.ipAddress(), 8));
                            currentRouter = router;

                        }
                    }
                }
            }
        }

        for (var item : output) {
            System.out.println(item);
        }
        return output;
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
