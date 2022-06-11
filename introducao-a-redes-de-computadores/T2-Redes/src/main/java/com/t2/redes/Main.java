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

        var nodes = new ArrayList<>();
        var routers = new ArrayList<Router>();
        var routerTables = new ArrayList<RouterTable>();
        int i = 1;
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
        i++; //Jump over String line ex: #ROUTER line
        while (!lines.get(i).contains("ROUTERTABLE")) {
            var split = lines.get(i).split(",");
            var name = split[0];
            var netQuantities = Integer.parseInt(split[1]);
            var netInterfaces = new ArrayList<NetInterface>();
            for (int j = 0; j < netQuantities; j++) {
                var macAddress = split[2 + j];
                var ipAddress = split[3 + j];
                var netInterface = new NetInterface(macAddress, ipAddress);
                netInterfaces.add(netInterface);
            }
            var router = new Router(name, netInterfaces, null, new ArrayList<>());
            routers.add(router);
            i++;
        }
        i++; //Jump over String line ex: #ROUTERTABLE line
        for (int j = i; j < lines.size(); j++) {
            var split = lines.get(j).split(",");
            var name = split[0];
            var netSource = split[1];
            var netDest = split[2];
            var nextHop = split[3];
            var routerLine = new RouterLine(netSource, netDest, nextHop);
            if (routerTables.isEmpty()) {
                var routerLines = new ArrayList<>(List.of(routerLine));
                var routerTable = new RouterTable(name, routerLines);
                routerTables.add(routerTable);
                continue;
            }
            for (int k = 0; k < routerTables.size(); k++) {
                if (!routerTables.get(k).name().contains(name)) {
                    var routerLines = new ArrayList<>(List.of(routerLine));
                    var newRouterTable = new RouterTable(name, routerLines);
                    routerTables.add(newRouterTable);
                } else {
                    routerTables.get(k).routerLines().add(routerLine);
                }
            }
        }

        for (int j = 0; j < routers.size(); j++) {
            for (RouterTable routerTable : routerTables) {
                if (routerTable.name().equalsIgnoreCase(routers.get(j).name())) {
                    Router element = routers.get(j).withRouterTable(routerTable);
                    routers.set(j, element);
                }
            }
        }

        System.out.println(nodes);
        System.out.println(routers);
        System.out.println(routerTables);
    }

}
