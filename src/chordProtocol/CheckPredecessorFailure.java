package chordProtocol;

import filesystem.ChunkInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.util.Vector;

/**
 *  The class CheckPredecessorFailure is run periodically and is responsible for checking if the node's predecessor is alive
 */
public class CheckPredecessorFailure implements Runnable {

    private Node node;

    public CheckPredecessorFailure(Node n){
        node = n;
    }


    /**
     * Runs the check predecessor routine
     */
    @Override
    public void run() {
        System.out.println("[Check Predecessor] Starting");

        // Send message to predecessor to check if it is alive
        FingerTableEntry predecessor = node.getPredecessor();
        if (predecessor == null){
            return;
        }

        Message m = new Message(MessageType.CHECK);
        Message ans = node.getSender().sendWithAnswer(m, predecessor.getValue());

        // If it is not alive
        if (ans == null){

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Backup the peer's chunks again
                    Vector<ChunkInfo> chunks = Peer.getManager().getPredecessorChunks();
                    for (ChunkInfo ci: chunks){
                        FingerTableEntry entry = ci.getEntry();
                        if (entry == null){
                            System.out.println("Entry is null.");
                            return;
                        }
                        Message m1 = new Message(MessageType.DECREASE_REP_DEGREE, ci);
                        Message answer = node.getSender().sendWithAnswer(m1, entry.getValue());
                        if (answer == null){
                            System.out.println("Answer is null");
                            return;
                        }

                    }
                }
            });

            t.start();

            node.resetPredecessor();
        } else if (!ans.isAliveMessage()){
            return;
        }

    }
}