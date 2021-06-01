package chordProtocol;

import messages.Message;
import messages.MessageSender;
import messages.MessageType;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  The class Node represents a chord node
 */
public class Node {

    public static final int M = 5;
    private FingerTableEntry[] fingerTable;
    private int id;
    private FingerTableEntry thisEntry;
    private FingerTableEntry predecessor;
    private MessageSender sender;
    private ExecutorService threadExecutor;

    /**
     * Constructor for the Node class
     *
     * @param address The address of the node
     *
     */
    public Node(InetSocketAddress address){
        this.id = generateId(address);
        System.out.println("Node id: " + this.id);
        thisEntry = new FingerTableEntry(this.id, address);
        fingerTable = new FingerTableEntry[M];
        this.predecessor = null;
        this.sender = new MessageSender();
        threadExecutor = Executors.newFixedThreadPool(10);
        initFingerTable();
    }

    /**
     * Displays the node's finger table on the screen
     */
    public void displayFingerTable(){
        System.out.println("------ Finger Table ----------");
        for (int i = 1; i <= M; i++){
            System.out.println("Entry " + (i-1) + ", of node " + (this.id + (int) Math.pow(2, i-1)) % (int) Math.pow(2, Node.M) + ": " + fingerTable[i-1].getId());
        }
        System.out.println("------------------------------");
        if (predecessor != null){
            System.out.println("Predecessor!!");
            System.out.println(predecessor.getId());
        } else {
            System.out.println("Predecessor is null!!");
        }
    }

    /**
     * Getter for the threadExecutor attribute
     *
     * @return Returns the threadExecutor attribute
     */
    public ExecutorService getThreadExecutor() {
        return threadExecutor;
    }

    /**
     * Getter for the thisEntry attribute
     *
     * @return Returns the thisEntry attribute
     */
    public FingerTableEntry getEntry(){
        return thisEntry;
    }

    /**
     * Getter for the sender attribute
     *
     * @return Returns the sender attribute
     */
    public MessageSender getSender(){
        return sender;
    }

    /**
     * Getter for the predecessor attribute
     *
     * @return Returns the predecessor attribute
     */
    public FingerTableEntry getPredecessor(){
        return predecessor;
    }

    /**
     * Resets the node's predecessor
     */
    public void resetPredecessor(){
        predecessor = null;
    }

    /**
     * Setter for the predecessor attribute
     *
     * @param  entry The new value for the predecessor attribute
     */
    public void setPredecessor(FingerTableEntry entry){
        predecessor = entry;
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
     * Generates an id for the node from its address
     *
     * @param address The node's address
     * @return Returns the node's id
     */
    private int generateId(InetSocketAddress address){
        int port = address.getPort();
        String addr = address.toString();
        String toHash = addr + ":" + port;
        String hashed = "";
        try{
            MessageDigest h = MessageDigest.getInstance("SHA-1");
            h.reset();
            byte[] stringHash = h.digest(toHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexHash = new StringBuilder(stringHash.length*2);
            for (int i = 0; i < stringHash.length; i++){
                hexHash.append(String.format("%02x", stringHash[i]));
            }
            hashed = hexHash.toString();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        UUID uuid = UUID.nameUUIDFromBytes(hashed.getBytes(StandardCharsets.UTF_8));
        int id = Math.floorMod(uuid.hashCode(), (int) Math.pow(2, M)); // uuid.hashCode() % (int) Math.pow(2, M);
        System.out.println("Id: " + id);
        return id;
    }

    /**
     * Initializes the node's finger table
     */
    private void initFingerTable(){
        for (int i = 0; i < M; i++){
            fingerTable[i] = new FingerTableEntry(-1, null);
        }
    }

    /**
     * Sets the finger table value on a certain index
     *
     * @param idx The index of the value on the finger table
     * @param  entry The new value for the originalEntry attribute
     */
    public boolean setFinger(int idx, FingerTableEntry entry){
        if (idx < 0 || idx >= M){
            return false;
        }
        if (entry == null){
            entry = new FingerTableEntry(-1, null);
        }
        fingerTable[idx] = entry;
        return true;
    }

    /**
     * Gets the value from a certain index on the node's finger table
     *
     * @param idx The index of the finger to get
     * @return Returns the finger at index idx on the node's finger table
     */
    public FingerTableEntry getFinger(int idx) throws RuntimeException {
        if (idx < 0 || idx >= M){
            throw new RuntimeException("Finger Table index out of bounds");
        }
        return fingerTable[idx];
    }

    /**
     * Gets the closest node of a certain received node
     *
     * @param id The id of the node to find the closest
     * @return Returns the closest node to the received one
     */
    private FingerTableEntry closestNode(int id){
        for (int i = M-1; i >= 0; i--){
            FingerTableEntry entry = getFinger(i);
            int entryId = entry.getId();
            if (Helper.between(this.id, entryId, id)){
                return entry;
            }
        }
        return thisEntry;
    }

    /**
     * Finds the successor of a certain node
     *
     * @param id The id of the node
     * @return Returns the successor of the received node
     */
    public FingerTableEntry findSuccessor(int id){
        FingerTableEntry successor = getFinger(0);
        if (Helper.between(this.id, id, successor.getId()) || id == successor.getId()){
            return successor;
        } else {
            FingerTableEntry closest = closestNode(id);
            if (closest.equals(thisEntry)){
                return thisEntry;
            }

            // Send message to closest node to find successor
            Message m = new Message(MessageType.FIND_SUCCESSOR, id);
            Message ans = sender.sendWithAnswer(m, closest.getValue());
            if (ans == null){
                return null;
            }
            if (!ans.isSuccessorMessage()){
                System.out.println("Error on finding successor [Message doesn't match expected type].");
                System.out.println("Current node: " + this.id + ".  Target Node: " + id + ".");
                return null;
            }
            if (!ans.hasData()){
                System.out.println("Error on finding successor [Successor is null].");
                System.out.println("Current node: " + this.id + ".  Target Node: " + id + ".");
                return null;
            }
            return ans.getData();
        }
    }

    /**
     * Creates a new chord ring
     */
    public void createNewChordRing(){
        this.predecessor = null;
        this.setFinger(0, thisEntry);
    }

    /**
     * Joins an existing chord ring
     *
     * @param peerFromRingAddress The address of the known peer from the existing chord ring
     */
    public void joinExistingChordRing(InetSocketAddress peerFromRingAddress){
        this.predecessor = null;

        Message m = new Message(MessageType.JOIN, this.thisEntry);
        Message ans = sender.sendWithAnswer(m, peerFromRingAddress);

        if (ans == null){
            System.out.println("Error on joining ring [Answer is null].");
            return;
        }

        if (!ans.isSuccessorMessage()){
            System.out.println("Error on joining ring [Message doesn't match expected type].");
            return;
        }

        if (!ans.hasData()){
            System.out.println("Error on joining ring [Successor is null].");
            return;
        }

        FingerTableEntry successor = ans.getData();
        setFinger(0, successor);

    }

    /**
     * Method to be executed when the node receives a notification
     *
     * @param node The node that sent the notification
     */
    public void whenNotified(FingerTableEntry node){
        if (node.equals(thisEntry)){
            return;
        }
        int id = node.getId();
        if (predecessor == null){
            this.predecessor = node;
        } else if (Helper.between(predecessor.getId(), id, this.id)){
            this.predecessor = node;
        }

    }

}