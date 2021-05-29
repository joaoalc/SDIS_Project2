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

public class Restore implements Runnable {

    private String filename;
    private ChunkFileSystemManager manager;
    private Node node;

    public Restore(String filename, Node node){
        this.filename = filename;
        this.manager = Peer.getManager();
        this.node = node;
    }

    private FileInfo canRestoreFile(){
        Vector<FileInfo> peerFiles = manager.getPeerFiles();
        for (FileInfo fi: peerFiles){
            if (fi.getPathName().equals(filename)){
                return fi;
            }
        }
        return null;
    }

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

    @Override
    public void run() {
        System.out.println("Restore starting");

        FileInfo fileInfo = canRestoreFile();

        if(fileInfo == null){
            System.out.println("Can't restore the specified file, it hasn't been backed up");
            return;
        }

        File folder = new File("files/peer" + Peer.getId() + "/restored");
        folder.mkdir();

        String fileId = fileInfo.getFileId();

        boolean done = false;
        int currentChunkNo = 1;
        Vector<Chunk> restoredChunks = new Vector<Chunk>();

        while(!done){

            // Enviar mensagem a dizer que chunk queremos
            Message m = new Message(MessageType.GETCHUNK, new ChunkInfo(fileId, currentChunkNo, -1, -1, -1, node.getEntry()));
            Vector<FingerTableEntry> targets = manager.getPeersThatHaveChunk(fileId + "-" + currentChunkNo);
            System.out.println("Targets HEREEEEEEEEEEEEEEEEE");
            System.out.println(targets.size());

            Chunk c = askForChunk(targets, m);
            if (c == null){
                System.out.println("Error on restore, chunk is null");
                return;
            }

            if (c.getData().length < 64000){
                System.out.println("Last chunk received");
                done = true;
            }

            restoredChunks.add(c);
            currentChunkNo++;

            try{
                Thread.sleep(1000);
            } catch(InterruptedException e){
                e.printStackTrace();
            }

        }

        //Collections.sort(restoredChunks);

        // Check order of the received chunks

        File restored = new File("files/peer" + Peer.getId() + "/restored/" + fileInfo.getPathName());
        try{
            restored.createNewFile();
        } catch(IOException e){
            e.printStackTrace();
        }

        manager.writeChunksToFile(restored, restoredChunks);
        Peer.serialize();

    }
}