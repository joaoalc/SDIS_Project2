
package filesystem;

import chordProtocol.FingerTableEntry;

import java.io.Serializable;

/**
 *  The class Chunk represents a chunk from a file
 */
public class Chunk implements Comparable<Chunk>, Serializable {

    private String fileId;
    private int chunkNo;
    private byte[] data;
    private int replicationDegree;
    private int originalPeerId;
    private FingerTableEntry originalEntry;

    /**
     * Constructor for the Chunk class
     *
     * @param fileId The id of the chunk's file
     * @param chunkNo The sequence number of the chunk
     * @param data The chunk's data
     * @param replicationDegree The desired replication degree for the chunk
     * @param originalPeerId The id of the peer who owns the chunk's file
     */
    public Chunk(String fileId, int chunkNo, byte[] data, int replicationDegree, int originalPeerId){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.data = data;
        this.replicationDegree = replicationDegree;
        this.originalPeerId = originalPeerId;
        this.originalEntry = null;
    }

    /**
     * Setter for the originalEntry attribute
     *
     * @param  entry The new value for the originalEntry attribute
     */
    public void setOriginalEntry(FingerTableEntry entry){
        originalEntry = entry;
    }

    /**
     * Getter for the originalEntry attribute
     *
     * @return Returns the originalEntry attribute
     */
    public FingerTableEntry getOriginalEntry() {
        return originalEntry;
    }

    /**
     * Getter for the originalPeerId attribute
     *
     * @return Returns the OriginalPeerId attribute
     */
    public int getOriginalPeerId() {
        return originalPeerId;
    }

    /**
     * Setter for the originalPeerId attribute
     *
     * @param  originalPeerId The new value for the originalPeerId attribute
     */
    public void setOriginalPeerId(int originalPeerId) {
        this.originalPeerId = originalPeerId;
    }

    /**
     * Getter for the replicationDegree attribute
     *
     * @return Returns the replicationDegree attribute
     */
    public int getReplicationDegree(){
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
     * Getter for the chunkNo attribute
     *
     * @return Returns the chunkNo attribute
     */
    public int getChunkNo() {
        return chunkNo;
    }

    /**
     * Getter for the data attribute
     *
     * @return Returns the data attribute
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Copies part of a received array to the chunk's data
     *
     * @param src The array from where the data will be copied
     * @param start The index of the array where the data to be copied starts
     * @param end The index of the array where the data to be copied ends
     *
     * @return Returns true if successful
     */
    public boolean copyPartOfArrayToData(byte[] src, int start, int end){
        int len = src.length;
        if (start > len || end > len) return false;
        int idx = 0;
        for (int i = start; i < end; i++){
            this.data[idx] = src[i];
            idx++;
        }

        return true;

    }

    /**
     * Checks if a certain chunk is the last one from its file or not
     *
     * @return Returns true if the chunk is the last one, false otherwise
     */
    public boolean isLast(){
        return data.length < 64000;
    }

    /**
     * Compares a chunk to another one based on their sequence number
     *
     * @param chunk The chunk to compare
     */
    @Override
    public int compareTo(Chunk chunk) {
        return chunkNo - chunk.getChunkNo();
    }
}