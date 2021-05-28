package chordProtocol;

import filesystem.ChunkInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;
import subProtocols.Backup;

import java.util.Vector;

public class CheckPredecessorFailure implements Runnable {

    private Node node;

    public CheckPredecessorFailure(Node n){
        node = n;
    }

    @Override
    public void run() {
        System.out.println("Check Predecessor");

        //System.out.println("---------- Check Predecessor Failure -------------");
        // Send message to predecessor to check if it is alive
        FingerTableEntry predecessor = node.getPredecessor();
        if (predecessor == null){
            //System.out.println("-----------------------------");
            return;
        }

        Message m = new Message(MessageType.CHECK);
        Message ans = node.getSender().sendWithAnswer(m, predecessor.getValue());

        System.out.println("After check predecessor message");

        // If it is not alive
        if (ans == null){
            System.out.println("Node is not alive!!!!!!!!!!!!!!!!!!!!");
            // Backup the peer's chunks again

            Vector<ChunkInfo> chunks = Peer.getManager().getPredecessorChunks();
            for (ChunkInfo ci: chunks){
                System.out.println("Starting backup for chunk " + ci.getFileId() + "-" + ci.getChunkNo());
                System.out.println("Original id: " + ci.getOriginalPeerId());

                FingerTableEntry entry = ci.getEntry();
                if (entry == null){
                    System.out.println("Entry is null.");
                    return;
                }
                Message m1 = new Message(MessageType.DO_BACKUP, ci);
                Message answer = node.getSender().sendWithAnswer(m1, entry.getValue());
                if (answer == null){
                    System.out.println("Answer is null");
                    return;
                }

            }

            node.resetPredecessor();
        } else if (!ans.isAliveMessage()){
            //System.out.println("Error on checking predecessor [Message doesn't match expected type].");
            //System.out.println("Current node: " + node.getId() + ".  Target Node: " + predecessor.getId() + ".");
            return;
        }

        //System.out.println("-----------------------------");

    }
}