package com.t2.redes;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StructureTest {

    @Test
    void testStructureBuilder() {
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

        assertEquals(nodes.size(), 4);
        assertEquals(nodes.get(0).arpTable().size(), 0);
        assertEquals(nodes.get(0).name(), "n1");
        assertEquals(nodes.get(0).macAddress(), "00:00:00:00:00:01");
        assertEquals(nodes.get(0).defaultGateway(), "192.168.0.1");
        assertEquals(nodes.get(0).ipAddress(), "192.168.0.2/24");
        assertEquals(nodes.get(1).arpTable().size(), 0);
        assertEquals(nodes.get(1).name(), "n2");
        assertEquals(nodes.get(1).macAddress(), "00:00:00:00:00:02");
        assertEquals(nodes.get(1).defaultGateway(), "192.168.0.1");
        assertEquals(nodes.get(1).ipAddress(), "192.168.0.3/24");
        assertEquals(nodes.get(2).arpTable().size(), 0);
        assertEquals(nodes.get(2).name(), "n3");
        assertEquals(nodes.get(2).macAddress(), "00:00:00:00:00:03");
        assertEquals(nodes.get(2).defaultGateway(), "192.168.1.1");
        assertEquals(nodes.get(2).ipAddress(), "192.168.1.2/24");
        assertEquals(nodes.get(3).arpTable().size(), 0);
        assertEquals(nodes.get(3).name(), "n4");
        assertEquals(nodes.get(3).macAddress(), "00:00:00:00:00:04");
        assertEquals(nodes.get(3).defaultGateway(), "192.168.1.1");
        assertEquals(nodes.get(3).ipAddress(), "192.168.1.3/24");
        assertEquals(routers.size(), 1);
        assertEquals(routers.get(0).arpTable().size(), 0);
        assertEquals(routers.get(0).name(), "r1");
        assertEquals(routers.get(0).netInterfaces().size(), 2);
        assertEquals(routers.get(0).netInterfaces().get(0).ip(), "192.168.0.1/24");
        assertEquals(routers.get(0).netInterfaces().get(0).macAddress(), "00:00:00:00:00:05");
        assertEquals(routers.get(0).netInterfaces().get(1).ip(), "192.168.1.1/24");
        assertEquals(routers.get(0).netInterfaces().get(1).macAddress(), "00:00:00:00:00:06");
        assertEquals(routers.get(0).routerTable().routerLines().size(), 2);
        assertEquals(routers.get(0).arpTable().size(), 0);
        assertEquals(routerTables.size(), 2);
        assertEquals(routerTables.get(0).name(), "r1");
        assertEquals(routerTables.get(0).routerLines().size(), 2);
        assertEquals(routerTables.get(0).routerLines().get(0).netDest(), "0.0.0.0");
        assertEquals(routerTables.get(0).routerLines().get(0).netSource(), "192.168.0.0/24");
        assertEquals(routerTables.get(0).routerLines().get(0).nextHop(), "0");
        assertEquals(routerTables.get(0).routerLines().get(1).netDest(), "0.0.0.0");
        assertEquals(routerTables.get(0).routerLines().get(1).netSource(), "192.168.1.0/24");
        assertEquals(routerTables.get(0).routerLines().get(1).nextHop(), "1");
        assertEquals(routerTables.get(1).name(), "r2");
        assertEquals(routerTables.get(1).routerLines().size(), 1);
        assertEquals(routerTables.get(1).routerLines().get(0).netDest(), "0.0.0.0");
        assertEquals(routerTables.get(1).routerLines().get(0).netSource(), "192.168.0.0/24");
        assertEquals(routerTables.get(1).routerLines().get(0).nextHop(), "0");
    }
}