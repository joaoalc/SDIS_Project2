package chordProtocol;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Node {

    private static final int M = 5;
    private FingerTableEntry[] fingerTable;
    private int id;
    private InetSocketAddress address;
    private InetSocketAddress predecessor;

    public Node(InetSocketAddress address){
        this.address = address;
        this.id = getId();
        fingerTable = new FingerTableEntry[M];
        this.predecessor = null;
    }

    private int getId(){
        int port = this.address.getPort();
        String addr = this.address.getAddress().toString();
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

        return UUID.nameUUIDFromBytes(hashed.getBytes(StandardCharsets.UTF_8)).hashCode() % (int) Math.pow(2, M);
    }

    private void initFingerTable(){
        for (int i = 0; i < M; i++){
            fingerTable[i] = new FingerTableEntry(-1, null);
        }
    }

    private boolean setFinger(int idx, InetSocketAddress value){
        if (idx < 0 || idx >= M){
            return false;
        }
        fingerTable[idx].setValue(value);
        return true;
    }

    private FingerTableEntry getFinger(int idx) throws RuntimeException {
        if (idx < 0 || idx >= M){
            throw new RuntimeException("Finger Table index out of bounds");
        }
        return fingerTable[idx];
    }

    private InetSocketAddress closestNode(int id){
        for (int i = M-1; i >= 0; i--){
            FingerTableEntry entry = getFinger(i);
            int entryId = entry.getId();
            if (entryId > this.id && entryId < id){
                return entry.getValue();
            }
        }
        return address;
    }

    public InetSocketAddress findSuccessor(int id){
        FingerTableEntry successor = getFinger(0);
        if (this.id < id && id < successor.getId()){
            return successor.getValue();
        } else {
            InetSocketAddress closest = closestNode(id);
            // Send message to closest node to find successor

            return ;
        }
    }

    private void createNewChordRing(){
        this.predecessor = null;
        this.setFinger(0, address);
    }

    private void joinExistingChordRing(InetSocketAddress peerFromRingAddress){
        this.predecessor = null;

        // Enviar mensagem ao peerFromRingAddress a dizer para encontrar o successor deste node
        // successor = n'.find_successor(n)

        setFinger(0, );

    }

}