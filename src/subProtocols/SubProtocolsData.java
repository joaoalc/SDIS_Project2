package subProtocols;

import filesystem.Chunk;
import filesystem.ChunkInfo;

import java.io.Serializable;
import java.util.Vector;

public class SubProtocolsData implements Serializable {

    private int replicationDegree;
    private String fileId;
    private Chunk c;
    private int senderId;
    private Vector<ChunkInfo> storedChunks;
    private Vector<Integer> peersThatBackedUpChunk;

    public SubProtocolsData(int senderId){
        replicationDegree = -1;
        this.senderId = senderId;
        fileId = null;
        c = null;
        storedChunks = null;
        peersThatBackedUpChunk = new Vector<Integer>();
    }

    public void addToPeersThatStoreChunk(int id){
        peersThatBackedUpChunk.add(id);
    }

    public Vector<Integer> getPeersThatBackedUpChunk() {
        return peersThatBackedUpChunk;
    }

    public void setPeersThatBackedUpChunk(Vector<Integer> peersThatBackedUpChunk) {
        this.peersThatBackedUpChunk = peersThatBackedUpChunk;
    }

    public void setFileId(String fileId){
        this.fileId = fileId;
    }

    public String getFileId(){
        return fileId;
    }

    public void setStoredChunks(Vector<ChunkInfo> chunks){
        this.storedChunks = chunks;
    }

    public Vector<ChunkInfo> getStoredChunks(){
        return this.storedChunks;
    }

    public void setChunk(Chunk c){
        this.c = c;
    }

    public Chunk getChunk(){
        return c;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public int getSenderId() {
        return senderId;
    }
}