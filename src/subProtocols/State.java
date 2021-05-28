package subProtocols;

import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkFileSystemManager;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import peers.Peer;

import java.util.Vector;

public class State implements Runnable {

    private ChunkFileSystemManager manager;
    private Node node;

    public State(Node node){
        manager = Peer.getManager();
        this.node = node;
    }

    @Override
    public void run() {
        String toPrint = "";
        toPrint += "----------------------------------------------------------";
        toPrint += "STATE\n";
        toPrint += "Backed Up Files:";
        if (manager.getBackedUpFiles().isEmpty()){
            toPrint += "\tThere are no files backed up...";
        } else {
            for (FileInfo fi: manager.getBackedUpFiles()){
                toPrint += "\tFilename: " + fi.getPathName();
                toPrint += "\t\tFileId: " + fi.getFileId();
                toPrint += "\t\tReplication Degree: " + fi.getReplicationDegree();
                System.out.println("\t\t Chunks: ");

                Vector<int[]> fileChunks = manager.getChunksRepDegrees(fi.getFileId());

                for (int[] arr: fileChunks){
                    System.out.println("\t\t\tChunk No: " + arr[0]);
                    System.out.println("\t\t\tChunk Current Replication Degree: " + arr[1]);
                }
            }
        }
        System.out.println("\nStored Chunks:");
        if (manager.getStoredChunks().isEmpty()){
            System.out.println("\tThere are no stored chunks...");
        } else {
            for (ChunkInfo c: manager.getStoredChunks()){
                System.out.println("\tChunk No: " + c.getChunkNo());
                System.out.println("\t\tSize: " + ((double)c.getDataLength() / 1000));
                System.out.println("\t\tDesired Replication Degree: " + c.getReplicationDegree());
                int currentRepDegree = manager.getChunkReplicationDegree(c.getFileId() + "-" + c.getChunkNo());
                System.out.println("\t\tCurrent Replication Degree: " + currentRepDegree);
            }
        }
        System.out.println("\nSpace:");
        System.out.println("Storage Capacity: " + manager.getCurrentCapacity());
        System.out.println("Amount of storage used: " + manager.getUsedStorage());

        System.out.println("\nPredecessor (" + node.getPredecessor().getId() + ") chunks:");
        if (manager.getPredecessorChunks().isEmpty()){
            System.out.println("\tThe predecessor doesn't have any stored chunks...");
        } else {
            for (ChunkInfo ci: manager.getPredecessorChunks()){
                System.out.println("\tChunk: " + ci.getFileId() + "-" + ci.getChunkNo());
            }
        }

        System.out.println("\nSuccessor (" + node.getFinger(0).getId() + ") chunks:");
        if (manager.getSuccessorChunks().isEmpty()){
            System.out.println("\tThe successor doesn't have any stored chunks...");
        } else {
            for (ChunkInfo ci: manager.getSuccessorChunks()){
                System.out.println("\tChunk: " + ci.getFileId() + "-" + ci.getChunkNo());
            }
        }

        System.out.println("----------------------------------------------------------");
    }
}