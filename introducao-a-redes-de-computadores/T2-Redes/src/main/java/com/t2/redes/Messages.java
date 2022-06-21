package com.t2.redes;

public class Messages {

    public String icmpRequestMessage(String source, String dest, String srcIP, String destIP, int ttl) {
        String cleanedSourceIp = cleanIp(srcIP);
        String cleanedDestIp = cleanIp(destIP);
        return String.format("%s ->> %s : ICMP Echo Request<br/>src=%s dst=%s ttl=%d", source, dest, cleanedSourceIp, cleanedDestIp, ttl);
    }

    public String icmpReplyMessage(String source, String dest, String srcIP, String destIP, int ttl) {
        var cleanedSrcIP = cleanIp(srcIP);
        var cleanedDstIP = cleanIp(destIP);
        return String.format("%s ->> %s : ICMP Echo Reply<br/>src=%s dst=%s ttl=%d", source, dest, cleanedSrcIP, cleanedDstIP, ttl);
    }

    public String icmpTimeExceededMessage(String source, String dest, String srcIP, String destIP, int ttl) {
        String cleanedSourceIp = cleanIp(srcIP);
        String cleanedDestIp = cleanIp(destIP);
        return String.format("%s ->> %s : ICMP Time Exceeded<br/>src=%s dst=%s ttl=%d", source, dest, cleanedSourceIp, cleanedDestIp, ttl);
    }

    public String arpReplyMessage(String source, String dest, String destIp, String destMac) {
        return String.format("%s ->> %s : ARP Reply<br/>%s is at %s", source, dest, cleanIp(destIp), destMac);
    }

    public String arpRequestMessage(String sourceName, String destIp, String sourceIp) {
        var source = cleanIp(sourceIp);
        var dest = cleanIp(destIp);
        return String.format("Note over %s : ARP Request<br/>Who has %s? Tell %s", sourceName, dest, source);
    }

    private String cleanIp(String sourceIp) {
        return sourceIp.replaceAll("/.*", "");
    }

}
