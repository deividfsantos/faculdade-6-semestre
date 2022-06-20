package com.t2.redes;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MainTest {

    @Test
    void getMask1() {
        String mask = Main.getMask("192.168.0.0/24");
        assertEquals("192.168.0.0", mask);
    }

    @Test
    void getMask2() {
        String mask = Main.getMask("40.0.0.10/8");
        assertEquals("40.0.0.0", mask);
    }

    @Test
    void getMask3() {
        String mask = Main.getMask("40.3.4.10/16");
        assertEquals("40.3.0.0", mask);
    }

    @Test
    void getMask4() {
        String mask1 = Main.getMask("192.168.0.3/24");
        String mask2 = Main.getMask("192.168.0.2/24");
        String mask3 = Main.getMask("192.168.1.2/24");
        assertEquals("192.168.0.0", mask2);
        assertEquals("192.168.1.0", mask3);
        assertEquals(mask1, mask2);
        assertNotEquals(mask1, mask3);
    }

    @Test
    void test1() {
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
        List<String> run = Main.run(nodes, routers, routerTables, "n1", "n2");
        assertEquals(4, run.size());
        assertEquals("Note over n1 : ARP Request<br/>Who has 192.168.0.3? Tell 192.168.0.2", run.get(0));
        assertEquals("n2 ->> n1 : ARP Reply<br/>192.168.0.3 is at 00:00:00:00:00:02", run.get(1));
        assertEquals("n1 ->> n2 : ICMP Echo Request<br/>src=192.168.0.2 dst=192.168.0.3 ttl=8", run.get(2));
        assertEquals("n2 ->> n1 : ICMP Echo Reply<br/>src=192.168.0.3 dst=192.168.0.2 ttl=8", run.get(3));
    }

    @Test
    void test2() {
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
        List<String> run = Main.run(nodes, routers, routerTables, "n1", "n3");
        assertEquals(8, run.size());
        assertEquals("Note over n1 : ARP Request<br/>Who has 192.168.0.1? Tell 192.168.0.2", run.get(0));
        assertEquals("r1 ->> n1 : ARP Reply<br/>192.168.0.1 is at 00:00:00:00:00:05", run.get(1));
        assertEquals("n1 ->> r1 : ICMP Echo Request<br/>src=192.168.0.2 dst=192.168.1.2 ttl=8", run.get(2));
        assertEquals("Note over r1 : ARP Request<br/>Who has 192.168.1.2? Tell 192.168.1.1", run.get(3));
        assertEquals("n3 ->> r1 : ARP Reply<br/>192.168.1.2 is at 00:00:00:00:00:03", run.get(4));
        assertEquals("r1 ->> n3 : ICMP Echo Request<br/>src=192.168.0.2 dst=192.168.1.2 ttl=8", run.get(5));
        assertEquals("n3 ->> r1 : ICMP Echo Reply<br/>src=192.168.1.2 dst=192.168.0.2 ttl=8", run.get(6));
        assertEquals("r1 ->> n1 : ICMP Echo Reply<br/>src=192.168.1.2 dst=192.168.0.2 ttl=8", run.get(7));
    }

    @Test
    void test3() {
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
        List<String> run = Main.run(nodes, routers, routerTables, "n1", "n2");
//        assertEquals(8, run.size());
        assertEquals("Note over n1 : ARP Request<br/>Who has 10.0.0.1? Tell 10.0.0.10", run.get(0));
        assertEquals("r1 ->> n1 : ARP Reply<br/>10.0.0.1 is at 00:00:00:00:00:10", run.get(1));
        assertEquals("n1 ->> r1 : ICMP Echo Request<br/>src=10.0.0.10 dst=40.0.0.10 ttl=8", run.get(2));
        assertEquals("Note over r1 : ARP Request<br/>Who has 20.0.0.2? Tell 20.0.0.1", run.get(3));
        assertEquals("r2 ->> r1 : ARP Reply<br/>20.0.0.2 is at 00:00:00:00:00:20", run.get(4));
        assertEquals("r1 ->> r2 : ICMP Echo Request<br/>src=10.0.0.10 dst=40.0.0.10 ttl=8", run.get(5));
        assertEquals("Note over r2 : ARP Request<br/>Who has 30.0.0.2? Tell 30.0.0.1", run.get(6));
        assertEquals("r3 ->> r2 : ARP Reply<br/>30.0.0.2 is at 00:00:00:00:00:30", run.get(7));
        assertEquals("r2 ->> r3 : ICMP Echo Request<br/>src=10.0.0.10 dst=40.0.0.10 ttl=8", run.get(8));
        assertEquals("Note over r3 : ARP Request<br/>Who has 40.0.0.10? Tell 40.0.0.1", run.get(9));
        assertEquals("n2 ->> r3 : ARP Reply<br/>40.0.0.10 is at 00:00:00:00:00:02", run.get(10));
        assertEquals("r3 ->> n2 : ICMP Echo Request<br/>src=10.0.0.10 dst=40.0.0.10 ttl=8", run.get(11));
        assertEquals("n2 ->> r3 : ICMP Echo Reply<br/>src=40.0.0.10 dst=10.0.0.10 ttl=8", run.get(12));
        assertEquals("Note over r3 : ARP Request<br/>Who has 50.0.0.1? Tell 50.0.0.2", run.get(13));
        assertEquals("r1 ->> r3 : ARP Reply<br/>50.0.0.1 is at 00:00:00:00:00:12", run.get(14));
//        assertEquals("r3 ->> r1 : ICMP Echo Reply<br/>src=40.0.0.10 dst=10.0.0.10 ttl=8", run.get(15));
//        assertEquals("r1 ->> n1 : ICMP Echo Reply<br/>src=40.0.0.10 dst=10.0.0.10 ttl=8", run.get(16));
    }
}