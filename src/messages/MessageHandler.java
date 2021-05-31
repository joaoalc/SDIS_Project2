package messages;

import chordProtocol.FingerTableEntry;
import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkInfo;
import peers.Peer;
import subProtocols.Backup;
import subProtocols.SubProtocolsData;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 *  The class MessageHandler is responsible for dealing with a received message
 */
public class MessageHandler {

    private Node node;
    private Message message;
    private ObjectOutputStream out;

    /**
     * Constructor for the MessageHandler class
     *
     * @param n The current chord node
     * @param m The received message
     * @param out The socket's output stream
     */
    public MessageHandler(Node n, Message m, ObjectOutputStream out){
        node = n;
        message = m;
        this.out = out;
    }

    /**
     * Calls the method corresponding to the received message's type
     */
    public void handle(){

        switch (message.getType()){
            case FIND_PREDECESSOR:
                handleFindPredecessor();
                break;
            case FIND_SUCCESSOR:
                int id = message.getId();
                if (id == -1){
                    throw new RuntimeException("ID is null");
                }
                handleFindSuccessor(id);
                break;
            case NOTIFICATION:
                if (!message.hasData()){
                    throw new RuntimeException("Message doesn't carry the node");
                }
                FingerTableEntry n = message.getData();
                handleNotification(n);
                break;
            case CHECK:
                handleCheck();
                break;
            case JOIN:
                if (!message.hasData()){
                    throw new RuntimeException("Data not set on join");
                }
                FingerTableEntry entry = message.getData();
                handleJoin(entry);
                break;
            case PUTCHUNK:
                System.out.println("[Handler] Received PUTCHUNK");
                SubProtocolsData content = message.getContent();
                handlePutChunk(content);
                break;
            case DELETE:
                System.out.println("[Handler] Received DELETE");
                SubProtocolsData deleteContent = message.getContent();
                handleDelete(deleteContent);
                break;
            case GETCHUNK:
                System.out.println("[Handler] Received GETCHUNK");
                ChunkInfo ci = message.getInfo();
                handleGetChunk(ci);
                break;
            case INFOSUCC:
                SubProtocolsData succInfoContent = message.getContent();
                handleSuccInfo(succInfoContent);
                break;
            case INFOPRED:
                SubProtocolsData predInfoContent = message.getContent();
                handlePredInfo(predInfoContent);
                break;
            case DECREASE_REP_DEGREE:
                System.out.println("[Handler] Received DECREASE_REP_DEGREE");
                ChunkInfo chunkInfo = message.getInfo();
                handleDecreaseRepDegree(chunkInfo);
                break;
            default:
                break;
        }

    }

    /**
     * Sends an answer to the received message
     *
     * @param  answer The answer
     */
    private void sendAnswer(Message answer) throws IOException {
        out.writeObject(answer);
    }

    /**
     * Handles a FIND_PREDECESSOR message
     */
    private void handleFindPredecessor(){
        Message answer = new Message(MessageType.PREDECESSOR, node.getPredecessor());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles a FIND_SUCCESSOR message
     *
     * @param id The id of the node whose successor is to be found
     */
    private void handleFindSuccessor(int id){
        FingerTableEntry successor = node.findSuccessor(id);
        Message answer = new Message(MessageType.SUCCESSOR, successor, id);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles a NOTIFICATION message
     *
     * @param n The FingerTableEntry of the node who sent the notification
     */
    private void handleNotification(FingerTableEntry n){
        node.whenNotified(n);
        Message answer = new Message(MessageType.NOTIFIED);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles a CHECK message
     */
    private void handleCheck(){
        Message answer = new Message(MessageType.ALIVE);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles a JOIN message
     *
     * @param entry The FingerTableEntry of the node that wants to join the chord ring
     */
    private void handleJoin(FingerTableEntry entry){
        FingerTableEntry successor = node.findSuccessor(entry.getId());
        Message answer = new Message(MessageType.SUCCESSOR, successor, entry.getId());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles a PUTCHUNK message
     *
     * @param content The message's content
     */
    private void handlePutChunk(SubProtocolsData content){

        Chunk c = content.getChunk();
        if (c == null){
            System.out.println("Chunk is null");
            return;
        }

        int stored = Peer.getManager().storeChunk(c);
        if (stored == -1){ // If it's my file
            // Backup finished, desired replication degree not achieved
            Message answer = new Message(MessageType.FAILED, content);
            try{
                sendAnswer(answer);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        } else if (stored < 0 && stored != -3){
            // Not stored
            // Send to successor
            Message m = new Message(MessageType.PUTCHUNK, content);
            Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());

            try{
                sendAnswer(answer);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        }

        content.addToPeersThatStoreChunk(node.getEntry());

        int newRepDegree = content.getReplicationDegree() - 1;
        if (newRepDegree <= 0){
            // Backup finished, desired replication degree achieved
            SubProtocolsData answerData = new SubProtocolsData(Peer.getId());
            answerData.setChunk(content.getChunk());
            answerData.setPeersThatBackedUpChunk(content.getPeersThatBackedUpChunk());
            answerData.setReplicationDegree(newRepDegree);
            Message answer = new Message(MessageType.STORED, answerData);
            try{
                sendAnswer(answer);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        }

        content.setReplicationDegree(newRepDegree);
        // Send to successor
        Message m = new Message(MessageType.PUTCHUNK, content);
        Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());

        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Handles a DELETE message
     *
     * @param content The message's content
     */
    private void handleDelete(SubProtocolsData content){

        int id = content.getSenderId();
        if (id == -1){
            System.out.println("Error on delete, senderId is -1...");
            return;
        } else if (id == Peer.getId()){
            // Chegou de volta ao original, enviar resposta
            Message answer = new Message(MessageType.DELETED);
            try{
                sendAnswer(answer);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        }

        String fileId = content.getFileId();
        if (fileId == null){
            System.out.println("Error on delete, fileId is null...");
            return;
        }

        int res = Peer.getManager().delete(fileId);
        if (res == -1){
            System.out.println("Error deleting chunk...");
            return;
        }

        // Send DELETE to successor
        Message toForward = new Message(MessageType.DELETE, content);
        Message answer = node.getSender().sendWithAnswer(toForward, node.getFinger(0).getValue());

        // Send answer back
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Handles a GETCHUNK message
     *
     * @param ci The information of the chunk to get
     */
    public void handleGetChunk(ChunkInfo ci){

        if (ci == null){
            System.out.println("Error on GetChunk, ChunkInfo is null");
            return;
        }

        if (Peer.getManager().hasChunk(ci.getFileId(), ci.getChunkNo())){
            Chunk c = Peer.getManager().getChunk(ci.getFileId(), ci.getChunkNo());
            SubProtocolsData data = new SubProtocolsData(Peer.getId());
            data.setChunk(c);
            // Send chunk back
            Message sendBack = new Message(MessageType.CHUNK, data);

            try{
                sendAnswer(sendBack);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        } else {
            SubProtocolsData data = null;
            Message sendBack = new Message(MessageType.CHUNK, data);
            try{
                sendAnswer(sendBack);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    /**
     * Handles an INFO_SUCC message
     *
     * @param infoContent The message's content
     */
    private void handleSuccInfo(SubProtocolsData infoContent){

        if (infoContent == null){
            System.out.println("Error");
            return;
        }

        Vector<ChunkInfo> chunks = infoContent.getStoredChunks();
        if (chunks == null){
            System.out.println("Error, chunks vector is null.");
        }

        Peer.getManager().setPredecessorChunks(chunks);

        Message answer = new Message(MessageType.INFOSUCC, Peer.getId());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
        Peer.serialize();
    }

    /**
     * Handles an INFO_PRED message
     *
     * @param infoContent The message's content
     */
    private void handlePredInfo(SubProtocolsData infoContent){

        if (infoContent == null){
            System.out.println("Error");
            return;
        }

        Vector<ChunkInfo> chunks = infoContent.getStoredChunks();
        if (chunks == null){
            System.out.println("Error, chunks vector is null.");
        }

        Peer.getManager().setSuccessorChunks(chunks);

        Message answer = new Message(MessageType.INFOPRED, Peer.getId());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
        Peer.serialize();
    }

    /**
     * Handles a DECREASE_REP_DEGREE message
     *
     * @param chunkInfo The information of the chunk whose replication degree will be decreased
     */
    private void handleDecreaseRepDegree(ChunkInfo chunkInfo){

        if (chunkInfo == null){
            System.out.println("ChunkInfo is null.");
            return;
        }

        Peer.getManager().decreaseChunkReplicationDegree(chunkInfo.getFileId() + "-" + chunkInfo.getChunkNo());
        int fileRepDegree = Peer.getManager().getFileRepDegree(chunkInfo.getFileId());
        int currentChunkRepDegree = Peer.getManager().getChunkReplicationDegree(chunkInfo.getFileId() + "-" + chunkInfo.getChunkNo());
        if (fileRepDegree == -1){
            System.out.println("Error");
            return;
        }

        Message answer = new Message(MessageType.ALIVE, Peer.getId());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }

        if (currentChunkRepDegree < fileRepDegree){
            try{
                Thread.sleep(4000);
            } catch(InterruptedException e){
                return;
            }

            Backup protocol = new Backup(chunkInfo.getFileId() + "-" + chunkInfo.getChunkNo(), chunkInfo.getReplicationDegree(), node, true);
            node.getThreadExecutor().execute(protocol);
        }

    }

}