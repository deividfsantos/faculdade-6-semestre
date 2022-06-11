package com.t2.redes;

import java.util.List;

record Router(String name, List<NetInterface> netInterfaces,
              RouterTable routerTable, List<NetInterface> arpTable) {
    public Router withRouterTable(RouterTable routerTable) {
        return new Router(name(), netInterfaces(), routerTable, arpTable());
    }
}
