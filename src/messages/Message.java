package messages;

import chordProtocol.FingerTableEntry;

import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private FingerTableEntry data;
    private int id;

    public Message(MessageType type){
        this.type = type;
        data = null;
        id = -1;
    }

    public Message(MessageType type, int id){
        this.type = type;
        data = null;
        this.id = id;
    }

    public Message(MessageType type, FingerTableEntry data){
        this.type = type;
        this.data = data;
        id = -1;
    }

    public boolean hasData(){
        return data != null;
    }

    public int getId() {
        return id;
    }

    public FingerTableEntry getData() {
        return data;
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

}