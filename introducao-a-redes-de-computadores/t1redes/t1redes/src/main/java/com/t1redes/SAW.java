package com.t1redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SAW {

    private int currentLine = 1;
    private int currentFrame = 0;
    private int sequence = 0;
    private EventType currentEventType = EventType.FRAME;

    List<String> result = new ArrayList<>();

    private Integer totalBits;
    private List<Integer> lostPkgs;
    private Integer numFrames;

    public SAW(String seqbits, String num_frames, String lost_pkts) {
        this.totalBits = (int) Math.pow(2, Double.parseDouble(seqbits)) - 1;
        this.lostPkgs = Arrays.stream(lost_pkts.split(","))
                .map(Integer::valueOf)
                .toList();

        this.numFrames = Integer.parseInt(num_frames);
    }

    public List<String> saw() {
        while (sequence < numFrames || currentEventType == EventType.ACK) {
            runFrames(totalBits, lostPkgs);
        }
        return result;
    }

    private void runFrames(int totalBits, List<Integer> lostPkts) {
        if (lostPkts.contains(currentLine)) {
            if (currentEventType == EventType.FRAME) {
                result.add("A -x B : (" + ++sequence + ") Frame " + currentFrame);
                result.add("Note over A : TIMEOUT (" + sequence + ")");
            } else {
                result.add("B --x A : Ack " + getNextFrame(totalBits));
                result.add("Note over A : TIMEOUT (" + sequence + ")");
            }
            currentEventType = EventType.RET;
        } else {
            if (currentEventType == EventType.FRAME) {
                result.add("A ->> B : (" + ++sequence + ") Frame " + currentFrame);
            } else if (currentEventType == EventType.ACK) {
                updateFrame(totalBits);
                result.add("B -->> A : Ack " + currentFrame);
            } else {
                result.add("A ->> B : (" + sequence + ") Frame " + currentFrame + " (RET)");
            }
            changeEventType();
        }
        currentLine++;
    }

    private void changeEventType() {
        if (currentEventType == EventType.FRAME || currentEventType == EventType.RET) {
            currentEventType = EventType.ACK;
        } else {
            currentEventType = EventType.FRAME;
        }
    }

    private void updateFrame(int totalBits) {
        currentFrame++;
        if (currentFrame > totalBits) {
            currentFrame = 0;
        }
    }

    private Integer getNextFrame(int totalBits) {
        int nextFrame = currentFrame + 1;
        if (nextFrame > totalBits) {
            return 0;
        }
        return nextFrame;
    }
}
