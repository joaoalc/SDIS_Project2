package chordProtocol;

import messages.Message;
import messages.MessageSender;
import messages.MessageType;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Node {

    public static final int M = 5;
    private FingerTableEntry[] fingerTable;
    private int id;
    private FingerTableEntry thisEntry;
    private FingerTableEntry predecessor;
    private MessageSender sender;

    public Node(InetSocketAddress address){
        this.id = generateId(address);
        System.out.println("Node id: " + this.id);
        thisEntry = new FingerTableEntry(this.id, address);
        fingerTable = new FingerTableEntry[M];
        this.predecessor = null;
        this.sender = new MessageSender();
        initFingerTable();
    }

    public void displayFingerTable(){
        System.out.println("------ Finger Table ----------");
        for (int i = 0; i < M; i++){
            System.out.println("Entry " + i + ": " + fingerTable[i].getId());
        }
        System.out.println("------------------------------");
    }

    public FingerTableEntry getEntry(){
        return thisEntry;
    }

    public MessageSender getSender(){
        return sender;
    }

    public FingerTableEntry getPredecessor(){
        return predecessor;
    }

    public void resetPredecessor(){
        predecessor = null;
    }

    public int getId() {
        return id;
    }

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

        System.out.println("Generate id power: " + (int) Math.pow(2, M));
        UUID uuid = UUID.nameUUIDFromBytes(hashed.getBytes(StandardCharsets.UTF_8));
        System.out.println("Generate id uuid: " + uuid);
        int id = Math.floorMod(uuid.hashCode(), (int) Math.pow(2, M)); // uuid.hashCode() % (int) Math.pow(2, M);
        System.out.println("Id: " + id);
        return id;
    }

    private void initFingerTable(){
        for (int i = 0; i < M; i++){
            fingerTable[i] = new FingerTableEntry(-1, null);
        }
    }

    public boolean setFinger(int idx, FingerTableEntry entry){
        if (idx < 0 || idx >= M){
            return false;
        }
        fingerTable[idx] = entry;
        return true;
    }

    public FingerTableEntry getFinger(int idx) throws RuntimeException {
        if (idx < 0 || idx >= M){
            throw new RuntimeException("Finger Table index out of bounds");
        }
        return fingerTable[idx];
    }

    private FingerTableEntry closestNode(int id){
        for (int i = M-1; i > 0; i--){
            FingerTableEntry entry = getFinger(i);
            int entryId = entry.getId();
            if (entryId > this.id && entryId < id){
                return entry;
            }
        }
        return thisEntry;
    }

    public FingerTableEntry findSuccessor(int id){
        FingerTableEntry successor = getFinger(0);
        System.out.println("Find SUCCESSOR: target_id = " + id + "; node_id = " + this.id + "; Successor id = " + successor.getId() + ";");
        if (this.id < id && id <= successor.getId()){
            System.out.println("Inside");
            return successor;
        } else {
            FingerTableEntry closest = closestNode(id);
            if (closest.equals(thisEntry)){
                System.out.println("Closest is equal to this node!");
                return thisEntry;
            }

            // Send message to closest node to find successor
            Message m = new Message(MessageType.FIND_SUCCESSOR, id);
            Message ans = sender.sendWithAnswer(m, closest.getValue());

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

    public void createNewChordRing(){
        this.predecessor = null;
        this.setFinger(0, thisEntry);
    }

    public void joinExistingChordRing(InetSocketAddress peerFromRingAddress){
        this.predecessor = null;

        // Enviar mensagem ao peerFromRingAddress a dizer para encontrar o successor deste node
        // successor = n'.find_successor(n)

        Message m = new Message(MessageType.FIND_SUCCESSOR, this.id);
        System.out.println("Sending FIND SUCCESSOR message to " + peerFromRingAddress.getAddress() + ":" + peerFromRingAddress.getPort());
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
        System.out.println("Successor found! " + successor.getId());
        setFinger(0, successor);
        System.out.println("Actual Successor: " + getFinger(0).getId());

    }

    public void whenNotified(FingerTableEntry node){
        int id = node.getId();
        if (predecessor == null || (id > predecessor.getId() && id < this.id)){
            this.predecessor = node;
        }
    }

}