package subProtocols;

import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkFileSystemManager;
import filesystem.FileInfo;
import messages.Message;
import messages.MessageType;
import peers.Peer;

import java.io.File;
import java.util.Vector;

public class Backup implements Runnable {

    private String filename;
    private int replicationDegree;
    private ChunkFileSystemManager manager;
    private Node node;
    /*
    private Path p;
    private File f;
    private AsynchronousFileChannel channel;
    private ByteBuffer buffer;
     */

    public Backup(String filename, int replicationDegree, Node node){
        this.filename = filename;
        this.replicationDegree = replicationDegree;
        manager = Peer.getManager();
        this.node = node;
        /*
        f = new File(this.filename);
        p = Paths.get(this.filename);
        try{
            channel = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }

        buffer = ByteBuffer.allocate(64000);
        */
    }

    @Override
    public void run() {

        File f = new File("files/peer" + Peer.getId() + "/peer_files/" + this.filename);
        Vector<Chunk> chunks = null;
        try{
            chunks =manager.getChunksFromFile(f, replicationDegree);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        String fileId = "";
        try{
            fileId = manager.generateFileId(f);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        Peer.getManager().addFile(new FileInfo(this.filename, fileId, replicationDegree));

        for (int i = 0; i < chunks.size(); i++){

            SubProtocolsData content = new SubProtocolsData(Peer.getId());
            content.setChunk(chunks.get(i));
            content.setReplicationDegree(replicationDegree);
            Message m = new Message(MessageType.PUTCHUNK, content);
            Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());
            System.out.println("[BACKUP] Got answer to PutChunk!");

            if (answer == null){
                System.out.println("Answer is null!");
                return;
            }

            if (answer.isStoredMessage()){
                SubProtocolsData c = answer.getContent();
                if (content == null){
                    System.out.println("Content is null");
                    return;
                }

                int receivedRepDegree = c.getReplicationDegree();
                System.out.println("Received replication degree: " + receivedRepDegree);
                if (receivedRepDegree == -1){
                    System.out.println("Rep degree wrong");
                    Peer.serialize();
                    return;
                } else if (receivedRepDegree > 0){
                    System.out.println("Error while backing up, cant achieve the desired replication degree on chunk " + chunks.get(i).getChunkNo());
                    Peer.serialize();
                    return;
                }
            } else if (answer.isFailedMessage()){
                System.out.println("Backup failed");
                Peer.serialize();
                return;
            } else {
                System.out.println("Error on messages");
                Peer.serialize();
                return;
            }

        }

        manager.addBackedUpFile(new FileInfo(this.filename, fileId, replicationDegree));

        System.out.println("[Peer] Backup protocol finished.");
        Peer.serialize();

    }
}