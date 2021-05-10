package subProtocols;

import filesystem.Chunk;

import java.io.Serializable;

public class SubProtocolsData implements Serializable {

    private int replicationDegree;
    private byte[] content;
    private Chunk c;
    private int senderId;

    public SubProtocolsData(int senderId){
        replicationDegree = -1;
        content = null;
        this.senderId = senderId;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setChunk(Chunk c){
        this.c = c;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSenderId() {
        return senderId;
    }
}