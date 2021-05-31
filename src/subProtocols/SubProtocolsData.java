package subProtocols;

import chordProtocol.FingerTableEntry;
import filesystem.Chunk;
import filesystem.ChunkInfo;

import java.io.Serializable;
import java.util.Vector;

/**
 *  The class SubProtocolsData holds the necessary data for any of the protocols
 *  The attributes are set depending on the protocol
 */
public class SubProtocolsData implements Serializable {

    private int replicationDegree;
    private String fileId;
    private Chunk c;
    private int senderId;
    private Vector<ChunkInfo> storedChunks;
    private Vector<FingerTableEntry> peersThatBackedUpChunk;

    /**
     * Constructor for the SubProtocolsData class
     *
     * @param senderId The id of the peer that sends the data
     */
    public SubProtocolsData(int senderId){
        replicationDegree = -1;
        this.senderId = senderId;
        fileId = null;
        c = null;
        storedChunks = null;
        peersThatBackedUpChunk = new Vector<FingerTableEntry>();
    }

    /**
     * Adds a peer to the vector of peers that have backed up a certain chunk
     *
     * @param entry The peer's FingerTableEntry
     */
    public void addToPeersThatStoreChunk(FingerTableEntry entry){
        peersThatBackedUpChunk.add(entry);
    }

    /**
     * Getter for the peersThatBackedUpChunk attribute
     *
     * @return Returns the peersThatBackedUpChunk attribute
     */
    public Vector<FingerTableEntry> getPeersThatBackedUpChunk() {
        return peersThatBackedUpChunk;
    }

    /**
     * Setter for the peersThatBackedUpChunk attribute
     *
     * @param  peersThatBackedUpChunk The new value for the peersThatBackedUpChunk attribute
     */
    public void setPeersThatBackedUpChunk(Vector<FingerTableEntry> peersThatBackedUpChunk) {
        this.peersThatBackedUpChunk = peersThatBackedUpChunk;
    }

    /**
     * Setter for the fileId attribute
     *
     * @param  fileId The new value for the fileId attribute
     */
    public void setFileId(String fileId){
        this.fileId = fileId;
    }

    /**
     * Getter for the fileId attribute
     *
     * @return Returns the fileId attribute
     */
    public String getFileId(){
        return fileId;
    }

    /**
     * Setter for the storedChunks attribute
     *
     * @param  chunks The new value for the storedChunks attribute
     */
    public void setStoredChunks(Vector<ChunkInfo> chunks){
        this.storedChunks = chunks;
    }

    /**
     * Getter for the storedChunks attribute
     *
     * @return Returns the storedChunks attribute
     */
    public Vector<ChunkInfo> getStoredChunks(){
        return this.storedChunks;
    }

    /**
     * Setter for the c attribute
     *
     * @param  c The new value for the c attribute
     */
    public void setChunk(Chunk c){
        this.c = c;
    }

    /**
     * Getter for the c attribute
     *
     * @return Returns the c attribute
     */
    public Chunk getChunk(){
        return c;
    }

    /**
     * Setter for the replicationDegree attribute
     *
     * @param  replicationDegree The new value for the replicationDegree attribute
     */
    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    /**
     * Getter for the replicationDegree attribute
     *
     * @return Returns the replicationDegree attribute
     */
    public int getReplicationDegree() {
        return replicationDegree;
    }

    /**
     * Getter for the senderId attribute
     *
     * @return Returns the senderId attribute
     */
    public int getSenderId() {
        return senderId;
    }
}