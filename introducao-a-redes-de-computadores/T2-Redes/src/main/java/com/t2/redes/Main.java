package com.t2.redes;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        var fileName = args[0];
        var command = args[1];
        var sourceName = args[2];
        var destName = args[3];
        var fileReader = new FileReader();
        var lines = fileReader.readFile(fileName);

        var nodes = new ArrayList<Node>();
        var routers = new ArrayList<Router>();
        var routerTables = new ArrayList<RouterTable>();
        var structure = new Structure();
        structure.build(lines, nodes, routers, routerTables);

        if (command.equalsIgnoreCase("ping")) {
            var ping = new Ping();
            ping.run(nodes, routers, routerTables, sourceName, destName);
        } else if (command.equalsIgnoreCase("traceroute")) {
            var traceroute = new Traceroute();
            traceroute.run(nodes, routers, routerTables, sourceName, destName);
        } else {
            System.out.println("Comando não encontrado.");
        }
    }

}
