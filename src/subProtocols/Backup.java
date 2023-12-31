package subProtocols;

import chordProtocol.FingerTableEntry;
import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkFileSystemManager;
import filesystem.FileInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Vector;

/**
 *  The class Backup is responsible for executing the backup protocol
 */
public class Backup implements Runnable {

    private String filename;
    private int replicationDegree;
    private ChunkFileSystemManager manager;
    private Node node;
    private boolean isSingleChunk;

    /**
     * Constructor for the Backup class
     *
     * @param filename The name of the file/chunk to be backed up
     * @param replicationDegree The desired replication degree for the file/chunk to be backed up
     * @param node The current chord node
     * @param singleChunk Flag to tell whether to execute the backup of a whole file or of a single chunk
     */
    public Backup(String filename, int replicationDegree, Node node, boolean singleChunk){
        this.filename = filename;
        this.replicationDegree = replicationDegree;
        manager = Peer.getManager();
        this.node = node;
        isSingleChunk = singleChunk;
    }

    /**
     * Runs the backup protocol
     */
    @Override
    public void run() {
        System.out.println("[Peer] Initiating backup protocol.");
        if (isSingleChunk){
            runSingleChunkBackup();
        } else {
            backupFile();
        }

    }

    /**
     * Backs up a whole file
     */
    public void backupFile(){
        File f = new File("files/peer" + Peer.getId() + "/peer_files/" + this.filename);
        final int MAX_CHUNK_SIZE = 64000;
        int numChunks = (int)(f.length()) / MAX_CHUNK_SIZE + 1;
        int remainder = (int)(f.length()) % MAX_CHUNK_SIZE;

        final String fileId;
        try{
            fileId = manager.generateFileId(f);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        Peer.getManager().addFile(new FileInfo(this.filename, fileId, this.replicationDegree));

        Path p = f.toPath();
        AsynchronousFileChannel channel;
        try{
            channel = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }

        for (int i = 1; i <= numChunks; i++){
            int size;
            if (i == numChunks){
                size = remainder;
            } else {
                size = MAX_CHUNK_SIZE;
            }
            ByteBuffer buffer = ByteBuffer.allocate(size);

            long offset = (i-1) * MAX_CHUNK_SIZE;
            final int chunkNo = i;

            try{
                channel.read(buffer, offset, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        System.out.println("Completed backup of chunk.");
                        attachment.flip();
                        byte[] chunkBytes = new byte[size];
                        attachment.get(chunkBytes);
                        attachment.clear();

                        Chunk c1 = new Chunk(fileId, chunkNo, chunkBytes, replicationDegree, node.getEntry().getId());
                        c1.setOriginalEntry(node.getEntry());
                        runSingleBackup(c1);

                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        throw new RuntimeException("Backup failed");
                    }
                });
            } catch(RuntimeException e){
                System.out.println("Backup failed");
                e.printStackTrace();
                return;
            }

        }

        manager.addBackedUpFile(new FileInfo(this.filename, fileId, replicationDegree));
        System.out.println("[Peer] Single backup protocol finished.");
        Peer.serialize();

    }

    /**
     * Backs up a single chunk
     */
    public void runSingleBackup(Chunk chunk){
        SubProtocolsData content = new SubProtocolsData(Peer.getId());
        content.setChunk(chunk);
        content.setReplicationDegree(chunk.getReplicationDegree());
        Message m = new Message(MessageType.PUTCHUNK, content);
        Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());
        String fileId = chunk.getFileId();

        if (answer == null){
            System.out.println("Answer is null!");
            return;
        }

        if (answer.isStoredMessage() || answer.isFailedMessage()){
            SubProtocolsData c = answer.getContent();
            if (content == null){
                System.out.println("Content is null");
                return;
            }

            Vector<FingerTableEntry> peersThatStored = c.getPeersThatBackedUpChunk();

            manager.setPeersThatHaveChunk(fileId + "-" + chunk.getChunkNo(), peersThatStored);

            int receivedRepDegree = c.getReplicationDegree();
            System.out.println("Received replication degree: " + receivedRepDegree);
            if (receivedRepDegree == -1){
                System.out.println("Rep degree wrong");
                Peer.serialize();
                return;
            } else if (receivedRepDegree > 0){
                manager.setChunkRepDegree(fileId + "-" + chunk.getChunkNo(), chunk.getReplicationDegree() - receivedRepDegree);
                System.out.println("Error while backing up, cant achieve the desired replication degree on chunk " + chunk.getChunkNo());
                Peer.serialize();
                return;
            }
            manager.setChunkRepDegree(fileId + "-" + chunk.getChunkNo(), chunk.getReplicationDegree());
        } else {
            System.out.println("Error on messages");
            Peer.serialize();
            return;
        }

    }

    /**
     * Backs up a single chunk
     */
    public void runSingleChunkBackup(){

        System.out.println("Starting single chunk backup.");

        String[] parts = filename.split("-");
        String fileId = parts[0];
        int chunkNo = Integer.parseInt(parts[1]);
        FileInfo info = manager.getFileInfoFromFileId(fileId);
        File f = new File("files/peer" + Peer.getId() + "/peer_files/" + info.getPathName());

        Chunk chunk = null;

        try{
            chunk = manager.getChunkFromFile(f, info.getReplicationDegree(), chunkNo, fileId);
            chunk.setOriginalEntry(node.getEntry());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        SubProtocolsData content = new SubProtocolsData(Peer.getId());
        System.out.println("");
        content.setChunk(chunk);
        content.setReplicationDegree(info.getReplicationDegree());
        Message m = new Message(MessageType.PUTCHUNK, content);
        Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());

        if (answer == null){
            System.out.println("Answer is null!");
            return;
        }

        if (answer.isStoredMessage() || answer.isFailedMessage()){
            SubProtocolsData c = answer.getContent();
            if (content == null){
                System.out.println("Content is null");
                return;
            }

            Vector<FingerTableEntry> peersThatStored = c.getPeersThatBackedUpChunk();

            manager.setPeersThatHaveChunk(fileId + "-" + chunk.getChunkNo(), peersThatStored);

            int receivedRepDegree = c.getReplicationDegree();
            if (receivedRepDegree == -1){
                System.out.println("Rep degree wrong");
                Peer.serialize();
                return;
            } else if (receivedRepDegree > 0){
                System.out.println("Error while backing up, cant achieve the desired replication degree on chunk " + chunk.getChunkNo());
                Peer.serialize();
                return;
            }
            manager.setChunkRepDegree(fileId + "-" + chunk.getChunkNo(), receivedRepDegree);
        } else {
            System.out.println("Error on messages");
            Peer.serialize();
            return;
        }

        System.out.println("Single chunk backup finished!");
    }

}