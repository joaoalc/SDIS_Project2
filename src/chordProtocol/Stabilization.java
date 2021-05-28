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
        System.out.println("Stabilize");

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
        //System.out.println("------- Stabilization ---------");
        // Ask successor for its predecessor and put it in the x variable
        FingerTableEntry succ = node.getFinger(0);

        if (succ == null){
            //System.out.println("Successor is null");
            return;
        }

        Message m = new Message(MessageType.FIND_PREDECESSOR);
        Message ans = node.getSender().sendWithAnswer(m, succ.getValue());

        if (ans == null){
            //System.out.println("Answer is null on stabilization");
            node.setFinger(0, new FingerTableEntry(-1, null));
            return;
        }

        if (!ans.isPredecessorMessage()){
            //System.out.println("Error on finding predecessor (stabilization) [Message doesn't match expected type].");
            //System.out.println("Current node: " + node.getId() + ".  Target Node: " + succ.getId() + ".");
            return;
        }

        if (!ans.hasData()){
            //System.out.println("No data");
        } else {
            FingerTableEntry x = ans.getData();

            if (x == null){
                //System.out.println("X is null");
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
            //System.out.println("Error on finding predecessor (stabilization) [Message doesn't match expected type].");
            //System.out.println("Current node: " + node.getId() + ".  Target Node: " + succ.getId() + ".");
            return;
        }

        //System.out.println("----------------------");

    }
}