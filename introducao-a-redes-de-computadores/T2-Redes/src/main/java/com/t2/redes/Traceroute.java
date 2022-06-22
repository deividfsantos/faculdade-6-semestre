package com.t2.redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Traceroute {

    private final Messages messages;
    private final Mask mask;

    public Traceroute() {
        this.messages = new Messages();
        this.mask = new Mask();
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
                currentDest = currentSource;
            }
            if (sourceMode == HardwareType.ROUTER) {
                List<RouterTableLine> routerTableLines = currentRouter.routerTable().routerTableLines();
                NetInterface netInterface = getNetInterface(currentDest, currentRouter, routerTableLines);
                if (isAtSameNetwork(netInterface.ipAddress(), currentDest.ipAddress())) {
                    if (notContainsInArpTable(currentRouter.arpTable(), currentDest.ipAddress())) {
                        output.add(messages.arpRequestMessage(currentRouter.name(), currentDest.ipAddress(), netInterface.ipAddress()));
                        output.add(messages.arpReplyMessage(currentDest.name(), currentRouter.name(), currentDest.ipAddress(), currentDest.macAddress()));
                        currentRouter.arpTable().add(new NetInterface(currentDest.ipAddress(), currentDest.macAddress()));
                        currentDest.arpTable().add(new NetInterface(netInterface.ipAddress(), netInterface.macAddress()));
                    } else if (ttlReply <= 0 || ttlRequest <= 0) {
                        if (timeExceededSourceIp == null) {
                            timeExceededSourceIp = netInterface.ipAddress();
                        }
                        output.add(messages.icmpTimeExceededMessage(currentRouter.name(), currentSource.name(), timeExceededSourceIp, currentSource.ipAddress(), ttlTimeExceeded));
                        currentDest = destNode;
                        ttlTimeExceeded = 8;
                        sourceMode = HardwareType.NODE;
                        ttlRequest = lastTtl + 1;
                        lastTtl = ttlRequest;
                        timeExceededSourceIp = null;
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
                    RouterTableLine routerTableLine = getRouterTableLine(currentRouter, currentDest.ipAddress());
                    Router routerDest = getRouter(routers, routerTableLine.nextHop());
                    NetInterface netInterfaceDest = getNetInterface(routerDest.netInterfaces(), routerTableLine.nextHop());
                    if (notContainsInArpTable(currentRouter.arpTable(), netInterfaceDest.ipAddress())) {
                        output.add(messages.arpRequestMessage(currentRouter.name(), routerTableLine.nextHop(), netInterface.ipAddress()));
                        output.add(messages.arpReplyMessage(routerDest.name(), currentRouter.name(), routerTableLine.nextHop(), netInterfaceDest.macAddress()));
                        currentRouter.arpTable().add(new NetInterface(netInterfaceDest.ipAddress(), netInterfaceDest.macAddress()));
                        routerDest.arpTable().add(new NetInterface(netInterface.ipAddress(), netInterface.macAddress()));
                    } else if (ttlReply <= 0 || ttlRequest <= 0) {
                        if (timeExceededSourceIp == null) {
                            timeExceededSourceIp = netInterface.ipAddress();
                        }
                        output.add(messages.icmpTimeExceededMessage(currentRouter.name(), routerDest.name(), timeExceededSourceIp, currentSource.ipAddress(), ttlTimeExceeded));
                        currentRouter = routerDest;
                        ttlTimeExceeded--;
                        currentDest = sourceNode;
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

    private RouterTableLine getRouterTableLine(Router currentRouter, String currentSourceIpAddress) {
        String mask = this.mask.getMask(currentSourceIpAddress);
        for (RouterTableLine routerTableLine :
                currentRouter.routerTable().routerTableLines()) {
            if (routerTableLine.netDest().equalsIgnoreCase(mask)) {
                return routerTableLine;
            }
        }

        for (RouterTableLine routerTableLine :
                currentRouter.routerTable().routerTableLines()) {
            if (routerTableLine.netDest().equalsIgnoreCase("0.0.0.0/0")) {
                return routerTableLine;
            }
        }
        throw new RuntimeException("Router not found in router table.");
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
            if (mask.getMaskBin(routerTableLine.netDest()).equalsIgnoreCase(mask.getMaskBin(currentDest.ipAddress()))) {
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
        return netInterfaces.get(0);
    }

    private Router getRouter(List<Router> routers, String ipAddress) {
        for (Router router : routers) {
            for (NetInterface netInterface : router.netInterfaces()) {
                if (netInterface.ipAddress().contains(ipAddress)) {
                    return router;
                }
            }
        }
        return routers.get(0);
    }

    private boolean isAtSameNetwork(String sourceIp, String destIp) {
        var maskSource = mask.getMaskBin(sourceIp);
        var maskDest = mask.getMaskBin(destIp);
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
