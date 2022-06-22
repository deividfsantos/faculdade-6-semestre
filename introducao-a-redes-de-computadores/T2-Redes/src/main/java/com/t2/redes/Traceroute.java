package com.t2.redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Traceroute {

    private final Messages messages;

    public Traceroute() {
        this.messages = new Messages();
    }

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
        Traceroute traceroute = new Traceroute();
        List<String> run = traceroute.run(nodes, routers, "n1", "n3");
    }

    public List<String> run(List<Node> nodes, List<Router> routers, String sourceName, String destName) {
        var output = new ArrayList<String>();
        var sourceNode = getNodeByName(nodes, sourceName);
        var destNode = getNodeByName(nodes, destName);

        var currentSource = sourceNode;
        var currentDest = destNode;
        var timeExceededSourceIp = (String) null;

        var currentRouter = (Router) null;

        var sourceMode = HardwareType.NODE;
        boolean replyMode = false;

        boolean end = false;

        var ttlRequest = 1;
        var ttlReply = 8;
        var ttlTimeExceeded = 8;
        var lastTtl = ttlRequest;
        while (!end && ttlTimeExceeded > 0) {
            if (ttlReply == 0 || ttlRequest == 0) {
                currentDest = destNode;
            }
            if (sourceMode == HardwareType.ROUTER) {
                List<RouterTableLine> routerTableLines = currentRouter.routerTable().routerTableLines();
                NetInterface netInterface = getNetInterface(currentDest, currentRouter, routerTableLines);
                if (isAtSameNetwork(netInterface.ipAddress(), currentDest.ipAddress())) {
                    if (ttlReply <= 0 || ttlRequest <= 0) {
                        output.add(messages.icmpTimeExceededMessage(currentRouter.name(), currentSource.name(), currentSource.defaultGateway(), currentSource.ipAddress(), ttlTimeExceeded));
                        currentSource = sourceNode;
                        ttlTimeExceeded = 8;
                        sourceMode = HardwareType.NODE;
                        ttlRequest = lastTtl + 1;
                        lastTtl = ttlRequest;
                    } else if (notContainsInArpTable(currentRouter.arpTable(), currentDest.ipAddress())) {
                        output.add(messages.arpRequestMessage(currentRouter.name(), currentDest.ipAddress(), netInterface.ipAddress()));
                        output.add(messages.arpReplyMessage(currentDest.name(), currentRouter.name(), currentDest.ipAddress(), currentDest.macAddress()));
                        currentRouter.arpTable().add(new NetInterface(currentDest.ipAddress(), currentDest.macAddress()));
                        currentDest.arpTable().add(new NetInterface(netInterface.ipAddress(), netInterface.macAddress()));
                    } else if (!replyMode) {
                        output.add(messages.icmpRequestMessage(currentRouter.name(), currentDest.name(), sourceNode.ipAddress(), destNode.ipAddress(), ttlRequest));
                        currentSource = currentDest;
                        currentDest = sourceNode;
                        replyMode = true;
                        sourceMode = HardwareType.NODE;
                    } else {
                        Router router = getRouter(routers, currentSource.defaultGateway());
                        output.add(messages.icmpReplyMessage(currentRouter.name(), currentDest.name(), destNode.ipAddress(), sourceNode.ipAddress(), ttlReply));
                        ttlReply--;
                        currentRouter = router;
                        end = true;
                    }
                } else {
                    RouterTableLine routerTableLine = currentRouter.routerTable().routerTableLines().get(currentRouter.routerTable().routerTableLines().size() - 1);
                    Router routerDest = getRouter(routers, routerTableLine.nextHop());
                    NetInterface netInterfaceDest = getNetInterface(routerDest.netInterfaces(), routerTableLine.nextHop());
                    if (ttlReply <= 0 || ttlRequest <= 0) {
                        output.add(messages.icmpTimeExceededMessage(currentRouter.name(), routerDest.name(), netInterface.ipAddress(), currentSource.ipAddress(), ttlTimeExceeded));
                        currentRouter = routerDest;
                        ttlTimeExceeded--;
                        sourceMode = HardwareType.NODE;
                        ttlRequest = lastTtl + 1;
                        lastTtl = ttlRequest;
                    } else if (notContainsInArpTable(currentRouter.arpTable(), netInterfaceDest.ipAddress())) {
                        output.add(messages.arpRequestMessage(currentRouter.name(), routerTableLine.nextHop(), netInterface.ipAddress()));
                        output.add(messages.arpReplyMessage(routerDest.name(), currentRouter.name(), routerTableLine.nextHop(), netInterfaceDest.macAddress()));
                        currentRouter.arpTable().add(new NetInterface(netInterfaceDest.ipAddress(), netInterfaceDest.macAddress()));
                        routerDest.arpTable().add(new NetInterface(netInterface.ipAddress(), netInterface.macAddress()));
                    } else if (!replyMode) {
                        output.add(messages.icmpRequestMessage(currentRouter.name(), routerDest.name(), sourceNode.ipAddress(), destNode.ipAddress(), ttlRequest));
                        currentRouter = routerDest;
                        ttlRequest--;
                    } else {
                        output.add(messages.icmpReplyMessage(currentRouter.name(), routerDest.name(), destNode.ipAddress(), sourceNode.ipAddress(), ttlReply));
                        ttlReply--;
                        currentRouter = routerDest;
                    }
                }
            } else {
                if (isAtSameNetwork(currentSource.ipAddress(), currentDest.ipAddress())) {
                    if (notContainsInArpTable(currentSource.arpTable(), currentDest.ipAddress())) {
                        output.add(messages.arpRequestMessage(currentSource.name(), currentDest.ipAddress(), currentSource.ipAddress()));
                        output.add(messages.arpReplyMessage(currentDest.name(), currentSource.name(), currentDest.ipAddress(), currentDest.macAddress()));
                        currentSource.arpTable().add(new NetInterface(currentDest.ipAddress(), currentDest.macAddress()));
                    }
                    output.add(messages.icmpRequestMessage(sourceName, destName, currentSource.ipAddress(), currentDest.ipAddress(), ttlRequest));
                    ttlRequest--;
                    output.add(messages.icmpReplyMessage(destName, sourceName, currentDest.ipAddress(), currentSource.ipAddress(), ttlReply));
                    ttlReply--;
                    end = true;
                } else {
                    if (notContainsInArpTable(currentSource.arpTable(), currentSource.defaultGateway())) {
                        Router router = getRouter(routers, currentSource.defaultGateway());
                        output.add(messages.arpRequestMessage(currentSource.name(), currentSource.defaultGateway(), currentSource.ipAddress()));
                        var netInterface = getNetInterfaceFromNode(router, currentSource.defaultGateway());
                        output.add(messages.arpReplyMessage(router.name(), currentSource.name(), netInterface.ipAddress(), netInterface.macAddress()));
                        currentSource.arpTable().add(netInterface);
                        router.arpTable().add(new NetInterface(currentSource.ipAddress(), currentSource.macAddress()));
                    } else if (!replyMode) {
                        Router router = getRouter(routers, currentSource.defaultGateway());
                        output.add(messages.icmpRequestMessage(currentSource.name(), router.name(), sourceNode.ipAddress(), destNode.ipAddress(), ttlRequest));
                        ttlRequest--;
                        currentRouter = router;
                        sourceMode = HardwareType.ROUTER;
                    } else {
                        Router router = getRouter(routers, currentSource.defaultGateway());
                        output.add(messages.icmpReplyMessage(currentSource.name(), router.name(), destNode.ipAddress(), sourceNode.ipAddress(), ttlReply));
                        ttlReply--;
                        currentRouter = router;
                        sourceMode = HardwareType.ROUTER;
                    }
                }
            }
        }

        for (var item : output) {
            System.out.println(item);
        }
        return output;
    }

    private NetInterface getNetInterfaceFromNode(Router router, String defaultGateway) {
        for (NetInterface netInterface : router.netInterfaces()) {
            if (netInterface.ipAddress().contains(defaultGateway)) {
                return netInterface;
            }
        }
        throw new RuntimeException("Network not found");
    }

    private NetInterface getNetInterface(Node currentDest, Router currentRouter, List<RouterTableLine> routerTableLines) {
        for (RouterTableLine routerTableLine : routerTableLines) {
            if (getMask(routerTableLine.netDest()).equalsIgnoreCase(getMask(currentDest.ipAddress()))) {
                Integer port = routerTableLine.port();
                return currentRouter.netInterfaces().get(port);
            }
        }
        return currentRouter.netInterfaces().get(routerTableLines.get(routerTableLines.size() - 1).port());
    }


    private boolean notContainsInArpTable(List<NetInterface> arpTable, String ipAddress) {
        for (NetInterface netInterface :
                arpTable) {
            if (netInterface.ipAddress().contains(ipAddress)) {
                return false;
            }
        }
        return true;
    }

    private NetInterface getNetInterface(List<NetInterface> netInterfaces, String defaultGateway) {
        for (NetInterface netInterface : netInterfaces) {
            if (netInterface.ipAddress().contains(defaultGateway)) {
                return netInterface;
            }
        }
        throw new RuntimeException("Net Interface not found");
    }

    private Router getRouter(List<Router> routers, String ipAddress) {
        for (Router router : routers) {
            for (NetInterface netInterface : router.netInterfaces()) {
                if (netInterface.ipAddress().contains(ipAddress)) {
                    return router;
                }
            }
        }
        throw new RuntimeException("Router not found");
    }

    private RouterTableLine getRouterDest(Router actualRouter, String destIp) {
        return actualRouter.routerTable().routerTableLines().stream().filter(routerTableLine -> routerTableLine.netDest().contains(getMask(destIp))).findFirst().orElse(actualRouter.routerTable().routerTableLines().get(actualRouter.routerTable().routerTableLines().size() - 1));
    }

    public String getMask(String destIp) {
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

    private String cleanIp(String sourceIp) {
        return sourceIp.replaceAll("/.*", "");
    }

    private boolean isAtSameNetwork(String sourceIp, String destIp) {
        var maskSource = getMask(sourceIp);
        var maskDest = getMask(destIp);
        return maskSource.equalsIgnoreCase(maskDest);
    }

    private Node getNodeByName(List<Node> nodes, String name) {
        for (Node node : nodes) {
            if (node.name().equalsIgnoreCase(name)) {
                return node;
            }
        }
        throw new RuntimeException("Node not found");
    }
}
