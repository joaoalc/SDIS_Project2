package messages;

import chordProtocol.Node;

public class MessageHandler implements Runnable {

    private Node node;
    private Message message;

    public MessageHandler(Node n, Message m){
        node = n;
        message = m;
    }

    @Override
    public void run() {



    }

}