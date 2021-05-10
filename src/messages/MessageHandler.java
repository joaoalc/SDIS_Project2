package messages;

import chordProtocol.FingerTableEntry;
import chordProtocol.Node;
import peers.Peer;
import subProtocols.SubProtocolsData;

import java.io.IOException;
import java.io.ObjectOutputStream;

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
                System.out.println("Received FIND_PREDECESSOR");
                handleFindPredecessor();
                break;
            case FIND_SUCCESSOR:
                int id = message.getId();
                if (id == -1){
                    throw new RuntimeException("ID is null");
                }
                System.out.println("Received FIND_SUCCESSOR [ID=" + id + "]");
                handleFindSuccessor(id);
                break;
            case NOTIFICATION:
                System.out.println("Received NOTIFICATION");
                if (!message.hasData()){
                    throw new RuntimeException("Message doesn't carry the node");
                }
                FingerTableEntry n = message.getData();
                handleNotification(n);
                break;
            case CHECK:
                System.out.println("Received CHECK");
                handleCheck();
                break;
            case JOIN:
                System.out.println("Received JOIN");
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
        System.out.println("RECEIVED FIND SUCCESSOR MESSAGE!!");
        FingerTableEntry successor = node.findSuccessor(id);
        Message answer = new Message(MessageType.SUCCESSOR, successor, id);
        try{
            sendAnswer(answer);
            System.out.println("Sent answer to find successor");
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

        System.out.println("Rep Degree: " + content.getReplicationDegree());
        SubProtocolsData answerContent = new SubProtocolsData(Peer.getId());
        answerContent.setReplicationDegree(content.getReplicationDegree()-1);

        Message answer = new Message(MessageType.STORED, answerContent);
        try{
            sendAnswer(answer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}