package chordProtocol;

import messages.Message;
import messages.MessageType;

public class Stabilization implements Runnable {

    private Node node;

    public Stabilization(Node n){
        node = n;
    }

    @Override
    public void run() {
        // Ask successor for its predecessor and put it in the x variable
        FingerTableEntry succ = node.getFinger(0);

        Message m = new Message(MessageType.FIND_PREDECESSOR);
        Message ans = node.getSender().sendWithAnswer(m, succ.getValue());

        if (!ans.isPredecessorMessage()){
            System.out.println("Error on finding predecessor (stabilization) [Message doesn't match expected type].");
            System.out.println("Current node: " + node.getId() + ".  Target Node: " + succ.getId() + ".");
            return;
        }

        if (!ans.hasData()){
            return;
        }

        FingerTableEntry x = ans.getData();

        int id = x.getId();

        if (id > node.getId() && id < succ.getId()){
            node.setFinger(0, x);
        }

        // Notify successor
        Message nm = new Message(MessageType.NOTIFICATION);
        ans = node.getSender().sendWithAnswer(nm, succ.getValue());

        if (!ans.isNotifiedMessage()){
            System.out.println("Error on finding predecessor (stabilization) [Message doesn't match expected type].");
            System.out.println("Current node: " + node.getId() + ".  Target Node: " + succ.getId() + ".");
            return;
        }

    }
}