package com.t2.redes;

import java.util.ArrayList;
import java.util.List;

public class Structure {
    public void build(List<String> lines, ArrayList<Node> nodes, ArrayList<Router> routers, ArrayList<RouterTable> routerTables) {
        int i = 1;
        i = buildNodes(lines, nodes, i);
        i++; //Jump over String line ex: #ROUTER line
        i = buildRouters(lines, routers, i);
        i++; //Jump over String line ex: #ROUTERTABLE line
        buildRouterTables(lines, routerTables, i);

        complementRoutersWithRouterTables(routers, routerTables);
    }

    private void complementRoutersWithRouterTables(ArrayList<Router> routers, ArrayList<RouterTable> routerTables) {
        for (int j = 0; j < routers.size(); j++) {
            for (RouterTable routerTable : routerTables) {
                if (routerTable.name().equalsIgnoreCase(routers.get(j).name())) {
                    Router element = routers.get(j).withRouterTable(routerTable);
                    routers.set(j, element);
                }
            }
        }
    }

    private void buildRouterTables(List<String> lines, ArrayList<RouterTable> routerTables, int i) {
        for (int j = i; j < lines.size(); j++) {
            var split = lines.get(j).split(",");
            var name = split[0];
            var netDest = split[1];
            var nextHop = split[2];
            var port = Integer.parseInt(split[3]);
            var routerLine = new RouterTableLine(netDest, nextHop, port);
            var contains = false;
            for (RouterTable routerTable : routerTables) {
                if (routerTable.name().contains(name)) {
                    routerTable.routerTableLines().add(routerLine);
                    contains = true;
                    break;
                }
            }
            if (!contains || routerTables.isEmpty()) {
                var routerLines = new ArrayList<>(List.of(routerLine));
                var newRouterTable = new RouterTable(name, routerLines);
                routerTables.add(newRouterTable);
            }
        }
    }

    private int buildRouters(List<String> lines, ArrayList<Router> routers, int i) {
        while (!lines.get(i).contains("ROUTERTABLE")) {
            var split = lines.get(i).split(",");
            var name = split[0];
            var netQuantities = Integer.parseInt(split[1]);
            var netInterfaces = new ArrayList<NetInterface>();
            for (int j = 0; j < netQuantities; j++) {
                var macAddress = split[2 + j * 2];
                var ipAddress = split[3 + j * 2];
                var netInterface = new NetInterface(ipAddress, macAddress);
                netInterfaces.add(netInterface);
            }
            var router = new Router(name, netInterfaces, null, new ArrayList<>());
            routers.add(router);
            i++;
        }
        return i;
    }

    private int buildNodes(List<String> lines, ArrayList<Node> nodes, int i) {
        while (!lines.get(i).contains("ROUTER")) {
            var split = lines.get(i).split(",");
            var name = split[0];
            var macAddress = split[1];
            var ipAddress = split[2];
            var defaultGateway = split[3];
            var node = new Node(name, macAddress, ipAddress, defaultGateway, new ArrayList<>());
            nodes.add(node);
            i++;
        }
        return i;
    }

}
