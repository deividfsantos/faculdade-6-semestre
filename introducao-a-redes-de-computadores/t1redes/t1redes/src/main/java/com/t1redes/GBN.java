package com.t1redes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class GBN {

    private int currentPkg = 1;
    private int sequence = 0;

    List<String> result = new ArrayList<>();
    List<Integer> elementsToAck = new ArrayList<>();
    List<Integer> failedElements = new ArrayList<>();
    List<Integer> numSequence = new LinkedList<>();
    private Integer ackWaitingFrame = 0;
    private int currentFrame = 0;
    private final List<Integer> lostPkgs;
    private final Integer windowSize;
    private final Integer numFrames;

    public GBN(String seqbits, String num_frames, String lost_pkts) {
        this.windowSize = (int) Math.pow(2, Double.parseDouble(seqbits)) - 1;
        this.lostPkgs = Arrays.stream(lost_pkts.split(",")).map(Integer::valueOf).toList();
        this.numFrames = Integer.parseInt(num_frames);
        //Gera uma sequencia com um tamanho sempre correto de numeros que serão usados na sequencia de envios
        for (int i = 0; i < numFrames; i++) {
            for (int j = 0; j < windowSize + 1; j++) {
                this.numSequence.add(j);
            }
        }
    }

    //lista com todas as linhas do resultado final que é executado e printado no final
    // Metódo responsável pela execução do fluxo Go-Back-N
    public List<String> gbn() {
        var currentWindowElement = 0; //Variável para saber qual a posição atual na janela
        while (currentFrame < numFrames) { //Controla as tentativas até que todos os frames tenham sido processados
            currentWindowElement = sendPkg(currentWindowElement);
            currentWindowElement = ackPkgs(currentWindowElement);
            currentWindowElement = timeout(currentWindowElement);
        }
        return result;
    }

    //Em cada execução é feito primeiro a execução dos envios dos pacotes, no trecho abaixo é validado qual o cenário atual
    //1 - Se eu possuo frame para ack ou que falharam no envio, ou seja, retentativa, e também nessa etapa é para os casos onde a retentativa estará com erro
    //2 - Somente envios de pacotes com erros
    //3 - Envios de pacotes com retentativas
    //4 - Envios normais de pacotes pela primeira vez
    private int sendPkg(int currentWindowElement) {
        while (currentWindowElement < windowSize && currentFrame < numFrames) {//Executa uma janela de cada vez
            if ((elementsToAck.contains(sequence) || failedElements.contains(sequence)) && lostPkgs.contains(currentPkg)) {
                failedElements.add(sequence);
                result.add("A -x B : (" + (sequence + 1) + ") Frame " + numSequence.get(currentFrame) + " (RET)");
            } else if (lostPkgs.contains(currentPkg)) {
                failedElements.add(sequence);
                result.add("A -x B : (" + (sequence + 1) + ") Frame " + numSequence.get(currentFrame));
            } else if (elementsToAck.contains(sequence) || failedElements.contains(sequence)) {
                elementsToAck.add(sequence);
                result.add("A ->> B : (" + (sequence + 1) + ") Frame " + numSequence.get(currentFrame) + " (RET)");
            } else {
                elementsToAck.add(sequence);
                result.add("A ->> B : (" + (sequence + 1) + ") Frame " + numSequence.get(currentFrame));
            }
            sequence++;
            currentWindowElement++;
            currentFrame++;
            currentPkg++;
        }
        return currentWindowElement;
    }

    private int ackPkgs(int currentWindowElement) {
        //Validação de acks onde ocorrerá caso existam frames com ack pendente dentro da janela atual
        for (int i = 0; i < windowSize && !elementsToAck.isEmpty(); i++) {
            Integer sentFrame = elementsToAck.get(0); //Item usado para saber qual o frame deve usar dado ack
            //IFS
            //1 - Pacotes com erros
            //2 - Pacotes normais validando se o frame correto está enviando ack
            if (lostPkgs.contains(currentPkg)) {
                result.add("B --x A : Ack " + numSequence.get(sentFrame + 1));
                currentPkg++;
                Integer failed = elementsToAck.remove(0);
                failedElements.add(failed);
            } else if (ackWaitingFrame.equals(sentFrame)) {
                result.add("B -->> A : Ack " + numSequence.get(sentFrame + 1));
                elementsToAck.remove(0);
                currentWindowElement--;
                ackWaitingFrame++; //Variável de controle para saber qual o proximo frame a receber ack
                currentPkg++;
            }
        }
        return currentWindowElement;
    }

    //Metodo com controle de timeouts e limpeza de parametros de configurações
    private int timeout(int currentWindowElement) {
        if (currentWindowElement == windowSize && !elementsToAck.isEmpty()) {
            sequence = numSequence.get(ackWaitingFrame);
            result.add("Note over A : TIMEOUT (" + (sequence + 1) + ")");
            currentWindowElement = 0;
            failedElements.addAll(elementsToAck);
            elementsToAck.clear();
            currentFrame = ackWaitingFrame;
        }
        return currentWindowElement;
    }
}
