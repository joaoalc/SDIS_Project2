package filesystem;

import chordProtocol.FingerTableEntry;

import java.io.Serializable;

/**
 *  The class ChunkInfo stores information about a chunk
 */
public class ChunkInfo implements Serializable {

    private String fileId;
    private int chunkNo;
    private int replicationDegree;
    private int dataLength;
    private int originalPeerId;
    private FingerTableEntry entry;

    /**
     * Constructor for the Chunk class
     *
     * @param fileId The id of the chunk's file
     * @param chunkNo The sequence number of the chunk
     * @param dataLength The length of the chunk's data
     * @param replicationDegree The desired replication degree for the chunk
     * @param originalPeerId The id of the peer who owns the chunk's file
     * @param entry The FingerTableEntry object that represents the chunk's original peer
     */
    public ChunkInfo(String fileId, int chunkNo, int replicationDegree, int dataLength, int originalPeerId, FingerTableEntry entry){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.dataLength = dataLength;
        this.originalPeerId = originalPeerId;
        this.entry = entry;
    }

    /**
     * Getter for the entry attribute
     *
     * @return Returns the entry attribute
     */
    public FingerTableEntry getEntry() {
        return entry;
    }

    /**
     * Getter for the originalPeerId attribute
     *
     * @return Returns the originalPeerId attribute
     */
    public int getOriginalPeerId() {
        return originalPeerId;
    }

    /**
     * Getter for the dataLength attribute
     *
     * @return Returns the dataLength attribute
     */
    public int getDataLength() {
        return dataLength;
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
     * Getter for the fileId attribute
     *
     * @return Returns the fileId attribute
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Setter for the fileId attribute
     *
     * @param  fileId The new value for the fileId attribute
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Getter for the chunkNo attribute
     *
     * @return Returns the chunkNo attribute
     */
    public int getChunkNo() {
        return chunkNo;
    }

}