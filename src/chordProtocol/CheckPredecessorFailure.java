package chordProtocol;

import messages.Message;
import messages.MessageType;

public class CheckPredecessorFailure implements Runnable {

    private Node node;

    public CheckPredecessorFailure(Node n){
        node = n;
    }

    @Override
    public void run() {

        System.out.println("---------- Check Predecessor Failure -------------");
        // Send message to predecessor to check if it is alive
        FingerTableEntry predecessor = node.getPredecessor();
        if (predecessor == null){
            System.out.println("-----------------------------");
            return;
        }

        Message m = new Message(MessageType.CHECK);
        Message ans = node.getSender().sendWithAnswer(m, predecessor.getValue());

        // If it is not alive
        if (ans == null){
            node.resetPredecessor();
        } else if (!ans.isAliveMessage()){
            System.out.println("Error on checking predecessor [Message doesn't match expected type].");
            System.out.println("Current node: " + node.getId() + ".  Target Node: " + predecessor.getId() + ".");
            return;
        }

        System.out.println("-----------------------------");

    }
}