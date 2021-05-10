package messages;

import chordProtocol.FingerTableEntry;
import subProtocols.SubProtocolsData;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private FingerTableEntry data;
    private int id;
    private SubProtocolsData content;

    public Message(MessageType type){
        this.type = type;
        data = null;
        id = -1;
        content = null;
    }

    public Message(MessageType type, SubProtocolsData content){
        this(type);
        this.content = content;
    }

    public Message(MessageType type, int id){
        this.type = type;
        data = null;
        this.id = id;
        content = null;
    }

    public Message(MessageType type, FingerTableEntry data){
        this.type = type;
        this.data = data;
        id = -1;
        content = null;
    }

    public Message(MessageType type, FingerTableEntry data, int id){
        this.type = type;
        this.data = data;
        this.id = id;
        content = null;
    }

    public boolean hasData(){
        return data != null;
    }

    public MessageType getType(){
        return type;
    }

    public int getId() {
        return id;
    }

    public FingerTableEntry getData() {
        return data;
    }

    public SubProtocolsData getContent() {
        return content;
    }

    public boolean isFindSuccessorMessage(){
        return type == MessageType.FIND_SUCCESSOR;
    }

    public boolean isSuccessorMessage(){
        return type == MessageType.SUCCESSOR;
    }

    public boolean isFindPredecessorMessage(){
        return type == MessageType.FIND_PREDECESSOR;
    }

    public boolean isPredecessorMessage(){
        return type == MessageType.PREDECESSOR;
    }

    public boolean isNotificationMessage(){
        return type == MessageType.NOTIFICATION;
    }

    public boolean isNotifiedMessage(){
        return type == MessageType.NOTIFIED;
    }

    public boolean isCheckMessage(){
        return type == MessageType.CHECK;
    }

    public boolean isAliveMessage(){
        return type == MessageType.ALIVE;
    }

    public boolean isStoredMessage(){
        return type == MessageType.STORED;
    }

}