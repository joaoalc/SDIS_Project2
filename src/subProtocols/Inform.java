package subProtocols;

import chordProtocol.FingerTableEntry;
import chordProtocol.Node;
import filesystem.ChunkFileSystemManager;
import filesystem.ChunkInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.util.Vector;

public class Inform implements Runnable {

    private ChunkFileSystemManager manager;
    private Node node;

    public Inform(Node n){
        manager = Peer.getManager();
        node = n;
    }

    @Override
    public void run() {

        Vector<ChunkInfo> chunks = manager.getStoredChunks();

        SubProtocolsData content = new SubProtocolsData(Peer.getId());
        content.setStoredChunks(chunks);
        Message m = new Message(MessageType.INFOSUCC, content);
        m.setId(node.getId());
        FingerTableEntry successor = node.getFinger(0);
        Message answer = null;
        if (successor == null){
            System.out.println("Successor is null, not sending info.");
        } else {
            System.out.println("My ip: " + node.getEntry().getValue());
            System.out.println("Successor ip: " + successor.getValue());
            System.out.println("Sending info to successor (" + successor.getId() + ") with size " + chunks.size());
            answer = node.getSender().sendWithAnswer(m, successor.getValue());
            if (answer == null){
                System.out.println("Couldn't send peer's information to the successor.");
            }
        }

        Message m2 = new Message(MessageType.INFOPRED, content);
        m2.setId(node.getId());
        FingerTableEntry predecessor = node.getPredecessor();
        if (predecessor == null){
            System.out.println("Predecessor is null, not sending info.");
        } else {
            System.out.println("Predecessor ip: " + predecessor.getValue());
            System.out.println("Sending info to predecessor (" + predecessor.getId() + ") with size " + chunks.size());
            answer = node.getSender().sendWithAnswer(m2, predecessor.getValue());
            if (answer == null){
                System.out.println("Couldn't send peer's information to the predecessor.");
            }
        }

    }
}