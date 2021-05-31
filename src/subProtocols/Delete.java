package subProtocols;

import chordProtocol.Node;
import filesystem.ChunkFileSystemManager;
import messages.Message;
import messages.MessageType;
import peers.Peer;

/**
 *  The class Delete is responsible for executing the delete protocol
 */
public class Delete implements Runnable {

    private String fileId;
    private String filename;
    private Node node;
    private ChunkFileSystemManager manager;

    /**
     * Constructor for the Delete class
     *
     * @param filename The name of the file whose chunks are to be deleted
     * @param fileId The id of the file whose chunks are to be deleted
     * @param node The current chord node
     */
    public Delete(String fileId, String filename, Node node){
        this.fileId = fileId;
        this.filename = filename;
        this.node = node;
        manager = Peer.getManager();
    }

    /**
     * Runs the delete protocol
     */
    @Override
    public void run() {

        System.out.println("[Peer] Initiating delete protocol.");
        SubProtocolsData content = new SubProtocolsData(Peer.getId());
        content.setFileId(this.fileId);
        Message m = new Message(MessageType.DELETE, content);

        int delay = 1000;
        Message answer = null;
        int tryNo = 0;
        do{
            answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());
            try{
                Thread.sleep(delay);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            delay *= 2;
            tryNo++;
        } while(answer == null && tryNo < 5);

        if (answer == null){
            System.out.println("Delete protocol failed...");
            Peer.serialize();
            return;
        }

        manager.removeBackedUpFile(this.filename);
        Peer.serialize();

    }
}