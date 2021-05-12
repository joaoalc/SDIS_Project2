package subProtocols;

import filesystem.Chunk;

import java.io.Serializable;

public class SubProtocolsData implements Serializable {

    private int replicationDegree;
    private String fileId;
    private Chunk c;
    private int senderId;

    public SubProtocolsData(int senderId){
        replicationDegree = -1;
        this.senderId = senderId;
        fileId = null;
        c = null;
    }


    public void setFileId(String fileId){
        this.fileId = fileId;
    }

    public String getFileId(){
        return fileId;
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