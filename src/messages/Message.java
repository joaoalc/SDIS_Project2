package messages;

import chordProtocol.FingerTableEntry;
import filesystem.ChunkInfo;
import subProtocols.SubProtocolsData;

import java.io.Serializable;

/**
 *  The class Message represents a message to be exchanged between peers
 */
public class Message implements Serializable {

    private MessageType type;
    private FingerTableEntry data;
    private int id;
    private SubProtocolsData content;
    private ChunkInfo info;

    /**
     * Constructor for the Message class
     *
     * @param type The type of the message
     */
    public Message(MessageType type){
        this.type = type;
        data = null;
        id = -1;
        content = null;
        info = null;
    }

    /**
     * Constructor for the Message class
     *
     * @param type The type of the message
     * @param content The message's content
     */
    public Message(MessageType type, SubProtocolsData content){
        this(type);
        this.content = content;
        info = null;
    }

    /**
     * Constructor for the Message class
     *
     * @param type The type of the message
     * @param id The id of the Peer/Node
     */
    public Message(MessageType type, int id){
        this.type = type;
        data = null;
        this.id = id;
        content = null;
        info = null;
    }

    /**
     * Constructor for the Message class
     *
     * @param type The type of the message
     * @param data The FingerTableEntry of the Peer/Node
     */
    public Message(MessageType type, FingerTableEntry data){
        this.type = type;
        this.data = data;
        id = -1;
        content = null;
        info = null;
    }

    /**
     * Constructor for the Message class
     *
     * @param type The type of the message
     * @param data The FingerTableEntry of the Peer/Node
     * @param id The id of the Peer/Node
     */
    public Message(MessageType type, FingerTableEntry data, int id){
        this.type = type;
        this.data = data;
        this.id = id;
        content = null;
        info = null;
    }

    /**
     * Constructor for the Message class
     *
     * @param type The type of the message
     * @param info The information of the message's chunk
     */
    public Message(MessageType type, ChunkInfo info){
        this(type);
        this.info = info;
    }

    /**
     * Getter for the info attribute
     *
     * @return Returns the info attribute
     */
    public ChunkInfo getInfo() {
        return info;
    }

    /**
     * Setter for the id attribute
     *
     * @param id The new value for the id attribute
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Checks if the message holds any data
     *
     * @return Returns true if the message holds any data, false otherwise
     */
    public boolean hasData(){
        return data != null;
    }

    /**
     * Getter for the type attribute
     *
     * @return Returns the type attribute
     */
    public MessageType getType(){
        return type;
    }

    /**
     * Getter for the id attribute
     *
     * @return Returns the id attribute
     */
    public int getId() {
        return id;
    }

    /**
     * Getter for the data attribute
     *
     * @return Returns the data attribute
     */
    public FingerTableEntry getData() {
        return data;
    }

    /**
     * Getter for the content attribute
     *
     * @return Returns the content attribute
     */
    public SubProtocolsData getContent() {
        return content;
    }

    /**
     * Checks if the message is a SUCCESSOR message
     *
     * @return Returns true if the message is a SUCCESSOR message, false otherwise
     */
    public boolean isSuccessorMessage(){
        return type == MessageType.SUCCESSOR;
    }

    /**
     * Checks if the message is a PREDECESSOR message
     *
     * @return Returns true if the message is a PREDECESSOR message, false otherwise
     */
    public boolean isPredecessorMessage(){
        return type == MessageType.PREDECESSOR;
    }

    /**
     * Checks if the message is a NOTIFIED message
     *
     * @return Returns true if the message is a NOTIFIED message, false otherwise
     */
    public boolean isNotifiedMessage(){
        return type == MessageType.NOTIFIED;
    }

    /**
     * Checks if the message is a ALIVE message
     *
     * @return Returns true if the message is a ALIVE message, false otherwise
     */
    public boolean isAliveMessage(){
        return type == MessageType.ALIVE;
    }

    /**
     * Checks if the message is a STORED message
     *
     * @return Returns true if the message is a STORED message, false otherwise
     */
    public boolean isStoredMessage(){
        return type == MessageType.STORED;
    }

    /**
     * Checks if the message is a FAILED message
     *
     * @return Returns true if the message is a FAILED message, false otherwise
     */
    public boolean isFailedMessage(){
        return type == MessageType.FAILED;
    }

}