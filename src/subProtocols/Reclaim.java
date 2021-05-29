package subProtocols;

import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkFileSystemManager;
import filesystem.ChunkInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.io.IOException;
import java.util.Vector;

public class Reclaim implements Runnable {

    private int newCapacity;
    private ChunkFileSystemManager manager;
    private Node node;

    public Reclaim(int newCapacity, Node node){
        this.newCapacity = newCapacity;
        this.manager = Peer.getManager();
        this.node = node;
    }

    private boolean deleteChunksUntilNeeded(){
        double usedStorage = manager.getUsedStorage();
        double capacity = manager.getCurrentCapacity();

        Vector<ChunkInfo> v = manager.getStoredChunks();
        Vector<ChunkInfo> to_remove = new Vector<ChunkInfo>();
        int idx = 0;
        while (usedStorage > capacity && idx < v.size()){

            System.out.println("Beginning");
            System.out.println("Used storage " + usedStorage);
            System.out.println("Capacity: " + capacity);
            System.out.println("Idx: " + idx);
            System.out.println("Size: " + v.size());

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
            System.out.println("End");
            System.out.println("Used storage " + usedStorage);
            System.out.println("Capacity: " + capacity);
            System.out.println("Idx: " + idx);
            System.out.println("Size: " + v.size());
        }

        for (ChunkInfo ci: to_remove){
            manager.removeFromStoredChunks(ci.getFileId() + "-" + ci.getChunkNo());
        }

        return true;
    }

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