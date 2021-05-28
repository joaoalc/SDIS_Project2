package messages;

import chordProtocol.FingerTableEntry;
import chordProtocol.Node;
import filesystem.Chunk;
import filesystem.ChunkInfo;
import peers.Peer;
import subProtocols.SubProtocolsData;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class MessageHandler {

    private Node node;
    private Message message;
    private ObjectOutputStream out;

    public MessageHandler(Node n, Message m, ObjectOutputStream out){
        node = n;
        message = m;
        this.out = out;
    }

    public void handle(){

        switch (message.getType()){
            case FIND_PREDECESSOR:
                //System.out.println("Received FIND_PREDECESSOR");
                handleFindPredecessor();
                break;
            case FIND_SUCCESSOR:
                int id = message.getId();
                if (id == -1){
                    throw new RuntimeException("ID is null");
                }
                //System.out.println("Received FIND_SUCCESSOR [ID=" + id + "]");
                handleFindSuccessor(id);
                break;
            case NOTIFICATION:
                //System.out.println("Received NOTIFICATION");
                if (!message.hasData()){
                    throw new RuntimeException("Message doesn't carry the node");
                }
                FingerTableEntry n = message.getData();
                handleNotification(n);
                break;
            case CHECK:
                //System.out.println("Received CHECK");
                handleCheck();
                break;
            case JOIN:
                //System.out.println("Received JOIN");
                if (!message.hasData()){
                    throw new RuntimeException("Data not set on join");
                }
                FingerTableEntry entry = message.getData();
                handleJoin(entry);
                break;
            case PUTCHUNK:
                System.out.println("Received PutChunk!");
                SubProtocolsData content = message.getContent();
                handlePutChunk(content);
                break;
            case DELETE:
                System.out.println("Received DELETE!");
                SubProtocolsData deleteContent = message.getContent();
                handleDelete(deleteContent);
                break;
            case GETCHUNK:
                System.out.println("Received GetChunk!");
                ChunkInfo ci = message.getInfo();
                handleGetChunk(ci);
                break;
            case INFOSUCC:
                System.out.println("Info from successor " + message.getId());
                SubProtocolsData succInfoContent = message.getContent();
                handleSuccInfo(succInfoContent);
                break;
            case INFOPRED:
                System.out.println("Info from predecessor " + message.getId());
                SubProtocolsData predInfoContent = message.getContent();
                handlePredInfo(predInfoContent);
                break;
            default:
                System.out.println("DEFAULT");
                break;
        }

    }

    private void sendAnswer(Message answer) throws IOException {
        out.writeObject(answer);
    }

    private void handleFindPredecessor(){
        Message answer = new Message(MessageType.PREDECESSOR, node.getPredecessor());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleFindSuccessor(int id){
        //System.out.println("RECEIVED FIND SUCCESSOR MESSAGE!!");
        FingerTableEntry successor = node.findSuccessor(id);
        Message answer = new Message(MessageType.SUCCESSOR, successor, id);
        try{
            sendAnswer(answer);
            //System.out.println("Sent answer to find successor");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleNotification(FingerTableEntry n){
        node.whenNotified(n);
        Message answer = new Message(MessageType.NOTIFIED);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleCheck(){
        Message answer = new Message(MessageType.ALIVE);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handleJoin(FingerTableEntry entry){
        FingerTableEntry successor = node.findSuccessor(entry.getId());
        Message answer = new Message(MessageType.SUCCESSOR, successor, entry.getId());
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void handlePutChunk(SubProtocolsData content){

        //System.out.println("Rep Degree: " + content.getReplicationDegree());

        Chunk c = content.getChunk();
        if (c == null){
            System.out.println("Chunk is null");
            return;
        }

        int stored = Peer.getManager().storeChunk(c);
        if (stored == -1){ // If it's my file
            //System.out.println("My File, backup failed");
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
            //System.out.println("Not stored, sending to successor");
            Message m = new Message(MessageType.PUTCHUNK, content);
            Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());

            try{
                sendAnswer(answer);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        }

        content.addToPeersThatStoreChunk(node.getId());

        int newRepDegree = content.getReplicationDegree() - 1;
        if (newRepDegree <= 0){
            // Backup finished, desired replication degree achieved
            SubProtocolsData answerData = new SubProtocolsData(Peer.getId());
            answerData.setChunk(content.getChunk());
            answerData.setPeersThatBackedUpChunk(content.getPeersThatBackedUpChunk());
            answerData.setReplicationDegree(newRepDegree);
            //System.out.println("Backup finished, rep degree achieved, rep degree: " + answerData.getReplicationDegree());
            Message answer = new Message(MessageType.STORED, answerData);
            //System.out.println("Answer rep degree: " + answer.getContent().getReplicationDegree());
            try{
                sendAnswer(answer);
            } catch (IOException e){
                e.printStackTrace();
            }
            return;
        }

        content.setReplicationDegree(newRepDegree);
        // Send to successor
        //System.out.println("Sending to successor with new rep degree: " + content.getReplicationDegree());
        Message m = new Message(MessageType.PUTCHUNK, content);
        Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());

        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }

        /*
        SubProtocolsData answerContent = new SubProtocolsData(Peer.getId());
        answerContent.setReplicationDegree(content.getReplicationDegree()-1);

        Message answer = new Message(MessageType.STORED, answerContent);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }*/
    }

    private void handleDelete(SubProtocolsData content){

        int id = content.getSenderId();
        if (id == -1){
            System.out.println("Error on delete, senderId is -1...");
            return;
        } else if (id == Peer.getId()){
            // Chegou de volta ao original, enviar resposta
            //System.out.println("Original peer!");
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
        }

        // Doesn't have the requested chunk, send GetChunk to successor
        Message m = new Message(MessageType.GETCHUNK, ci);
        Message answer = node.getSender().sendWithAnswer(m, node.getFinger(0).getValue());

        if (answer == null){
            System.out.println("Error on restore, answer is null");
            return;
        }

        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private void handleSuccInfo(SubProtocolsData infoContent){

        if (infoContent == null){
            System.out.println("Error");
            return;
        }

        Vector<ChunkInfo> chunks = infoContent.getStoredChunks();
        System.out.println("Received Info from successor with size " + chunks.size());
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
    }

    private void handlePredInfo(SubProtocolsData infoContent){

        if (infoContent == null){
            System.out.println("Error");
            return;
        }

        Vector<ChunkInfo> chunks = infoContent.getStoredChunks();
        System.out.println("Received Info from predecessor with size " + chunks.size());
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
    }

}