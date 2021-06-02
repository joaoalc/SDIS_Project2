package subProtocols;

import chordProtocol.Node;
import filesystem.ChunkFileSystemManager;
import filesystem.ChunkInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.util.Vector;

/**
 *  The class Reclaim is responsible for executing the reclaim protocol
 */
public class Reclaim implements Runnable {

    private int newCapacity;
    private ChunkFileSystemManager manager;
    private Node node;

    /**
     * Constructor for the Reclaim class
     *
     * @param newCapacity The peer's new capacity for storing chunks
     * @param node The current chord node
     */
    public Reclaim(int newCapacity, Node node){
        this.newCapacity = newCapacity;
        this.manager = Peer.getManager();
        this.node = node;
    }

    /**
     * Deletes chunks from the peer's filesystem until the peer's new capacity is satisfied
     *
     * @return Returns true if successful, false otherwise
     */
    private boolean deleteChunksUntilNeeded(){
        double usedStorage = manager.getUsedStorage();
        double capacity = manager.getCurrentCapacity();

        if (capacity <= 0.5){
            Vector<ChunkInfo> v = manager.getStoredChunks();
            Vector<ChunkInfo> to_remove = new Vector<ChunkInfo>();

            int idx = 0;
            while (idx < v.size()){
                ChunkInfo ci = v.get(idx);
                String chunkName = ci.getFileId() + "-" + ci.getChunkNo();
                if (manager.deleteChunkFromFilesystem(chunkName)){
                    to_remove.add(ci);
                    System.out.println("Deleted chunk " + chunkName);
                    usedStorage -= ((double)ci.getDataLength()/1000);
                    Message m = new Message(MessageType.DECREASE_REP_DEGREE, ci);
                    Message answer = node.getSender().sendWithAnswer(m, ci.getEntry().getValue());
                    if (answer == null){
                        System.out.println("Error on reclaim, answer is null...");
                        return false;
                    }

                }
                idx++;
            }

            for (ChunkInfo ci: to_remove){
                manager.removeFromStoredChunks(ci.getFileId() + "-" + ci.getChunkNo());
            }

            return true;

        }

        Vector<ChunkInfo> v = manager.getStoredChunks();
        Vector<ChunkInfo> to_remove = new Vector<ChunkInfo>();
        int idx = 0;
        while (usedStorage > capacity && idx < v.size()){
            ChunkInfo ci = v.get(idx);
            String chunkName = ci.getFileId() + "-" + ci.getChunkNo();
            if (manager.deleteChunkFromFilesystem(chunkName)){
                to_remove.add(ci);
                System.out.println("Deleted chunk " + chunkName);
                usedStorage -= ((double)ci.getDataLength()/1000);
                Message m = new Message(MessageType.DECREASE_REP_DEGREE, ci);
                Message answer = node.getSender().sendWithAnswer(m, ci.getEntry().getValue());
                if (answer == null){
                    System.out.println("Error on reclaim, answer is null...");
                    return false;
                }

            }
            idx++;
        }

        for (ChunkInfo ci: to_remove){
            manager.removeFromStoredChunks(ci.getFileId() + "-" + ci.getChunkNo());
        }

        return true;
    }

    /**
     * Runs the reclaim protocol
     */
    @Override
    public void run() {
        System.out.println("[Peer] Initiating reclaim protocol.");
        if (!manager.needsToDeleteChunks(newCapacity)){
            manager.setCurrentCapacity(newCapacity);
        } else {
            manager.setCurrentCapacity(newCapacity);
            deleteChunksUntilNeeded();
        }

        Peer.serialize();
    }
}