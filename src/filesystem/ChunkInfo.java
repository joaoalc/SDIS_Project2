package filesystem;

import java.io.Serializable;

public class ChunkInfo implements Serializable {

    private String fileId;
    private int chunkNo;
    private int replicationDegree;
    private int dataLength;
    private int originalPeerId;

    public ChunkInfo(String fileId, int chunkNo, int replicationDegree, int dataLength, int originalPeerId){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.dataLength = dataLength;
        this.originalPeerId = originalPeerId;
    }

    public int getOriginalPeerId() {
        return originalPeerId;
    }

    public void setOriginalPeerId(int originalPeerId) {
        this.originalPeerId = originalPeerId;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}