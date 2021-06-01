package chordProtocol;

import messages.Message;
import messages.MessageType;

/**
 * The class Stabilization is run periodically and is responsible for making sure the finger table is correct
 */
public class Stabilization implements Runnable {

    private Node node;

    public Stabilization(Node n){
        node = n;
    }

    @Override
    public void run() {
        System.out.println("[Stabilize] Starting");

        System.out.println("My id: " + node.getId());
        if (node.getFinger(0) != null){
            System.out.println("My successor: " + node.getFinger(0).getId());
        } else {
            System.out.println("My successor: null");
        }
        if (node.getPredecessor() != null){
            System.out.println("My Predecessor: " + node.getPredecessor().getId());
        } else {
            System.out.println("My Predecessor: null");
        }
        // Ask successor for its predecessor and put it in the x variable
        FingerTableEntry succ = node.getFinger(0);

        if (succ == null){
            return;
        }

        Message m = new Message(MessageType.FIND_PREDECESSOR);
        Message ans = node.getSender().sendWithAnswer(m, succ.getValue());

        if (ans == null){
            node.setFinger(0, new FingerTableEntry(-1, null));
            return;
        }

        if (!ans.isPredecessorMessage()){
            return;
        }

        if (!ans.hasData()){
        } else {
            FingerTableEntry x = ans.getData();

            if (x == null){
                return;
            }

            int id = x.getId();

            if (Helper.between(node.getId(), id, succ.getId())){
                node.setFinger(0, x);
            }
        }

        // Notify successor
        Message nm = new Message(MessageType.NOTIFICATION, node.getEntry());
        ans = node.getSender().sendWithAnswer(nm, succ.getValue());

        if (!ans.isNotifiedMessage()){
            return;
        }

    }

}