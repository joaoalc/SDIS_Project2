package subProtocols;

import chordProtocol.FingerTableEntry;
import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkFileSystemManager;
import filesystem.ChunkInfo;
import filesystem.FileInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 *  The class Restore is responsible for executing the restore protocol
 */
public class Restore implements Runnable {

    private String filename;
    private ChunkFileSystemManager manager;
    private Node node;

    /**
     * Constructor for the Restore class
     *
     * @param filename The name of the file to be restored
     * @param node The current chord node
     */
    public Restore(String filename, Node node){
        this.filename = filename;
        this.manager = Peer.getManager();
        this.node = node;
    }

    /**
     * Checks if the requested file can be restored or not
     *
     * @return Returns the information of the requested file if it can be restored, null otherwise
     */
    private FileInfo canRestoreFile(){
        Vector<FileInfo> peerFiles = manager.getPeerFiles();
        for (FileInfo fi: peerFiles){
            if (fi.getPathName().equals(filename)){
                return fi;
            }
        }
        return null;
    }

    /**
     * Asks the peers that had previously stored a certain chunk for it
     *
     * @param targets The peers that had previously stored the chunk
     * @param m The message to be sent to the other peers
     * @return Returns the requested chunk, null if the operation fails
     */
    private Chunk askForChunk(Vector<FingerTableEntry> targets, Message m){

        for (FingerTableEntry entry: targets) {
            Message answer = node.getSender().sendWithAnswer(m, entry.getValue());

            if (answer == null) {
                continue;
            }

            SubProtocolsData answerContent = answer.getContent();
            if (answerContent == null) {
                continue;
            }

            Chunk c = answerContent.getChunk();
            if (c == null) {
                continue;
            } else {
                return c;
            }
        }
        return null;
    }

    /**
     * Runs the restore protocol
     */
    @Override
    public void run() {
        System.out.println("[Peer] Initiating restore protocol.");

        FileInfo fileInfo = canRestoreFile();

        if(fileInfo == null){
            System.out.println("Can't restore the specified file, it hasn't been backed up");
            return;
        }

        File folder = new File("files/peer" + Peer.getId() + "/restored");
        folder.mkdir();

        File restored = new File("files/peer" + Peer.getId() + "/restored/" + fileInfo.getPathName());
        try{
            restored.createNewFile();
        } catch(IOException e){
            e.printStackTrace();
        }

        String fileId = fileInfo.getFileId();

        boolean done = false;
        final int MAX_CHUNK_SIZE = 64000;
        int currentChunkNo = 1;

        while(!done){

            Message m = new Message(MessageType.GETCHUNK, new ChunkInfo(fileId, currentChunkNo, -1, -1, -1, node.getEntry()));
            Vector<FingerTableEntry> targets = manager.getPeersThatHaveChunk(fileId + "-" + currentChunkNo);

            Chunk c = askForChunk(targets, m);
            if (c == null){
                System.out.println("Error on restore, chunk is null");
                return;
            }

            if (c.getData().length < 64000){
                System.out.println("Last chunk received");
                done = true;
            }

            try{
                manager.writeToFile(c.getData(), restored, MAX_CHUNK_SIZE * (currentChunkNo-1));
            } catch(Exception e){
                System.out.println("Couldn't write chunk to file!");
                return;
            }

            currentChunkNo++;

        }

        Peer.serialize();

    }
}