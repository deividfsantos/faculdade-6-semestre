package com.t2.redes;

import java.util.List;

record Node(String name, String macAddress, String ipAddress, String defaultGateway, List<NetInterface> arpTable) {

}
