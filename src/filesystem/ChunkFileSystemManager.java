
package filesystem;

import chordProtocol.FingerTableEntry;
import peers.Peer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 *  The class ChunkFileSystemManager represents the filesystem of a peer
 */
public class ChunkFileSystemManager implements Serializable{

    private final int MAX_CHUNK_SIZE = 64000;
    private final int INITIAL_CAPACITY = 50000; // In KBytes

    private static final long serialVersionUID = 4L;

    private Vector<String> chunksInFilesystem;
    private Vector<ChunkInfo> storedChunks;
    private Vector<FileInfo> backedUpFiles;
    private Vector<FileInfo> peerFiles;
    private ConcurrentHashMap<String, Integer> chunksCurrentReplicationDegrees;
    private ConcurrentHashMap<String, Vector<Chunk>> restored_files;
    private int currentCapacity;
    private Vector<ChunkInfo> predecessorChunks;
    private Vector<ChunkInfo> successorChunks;
    private ConcurrentHashMap<String, Vector<FingerTableEntry>> peersThatHaveChunk;
    private FingerTableEntry entry;

    /**
     * Constructor for the ChunkFileSystemManager class
     */
    public ChunkFileSystemManager(FingerTableEntry entry){
        chunksInFilesystem = new Vector<String>();
        chunksCurrentReplicationDegrees = new ConcurrentHashMap<String, Integer>();
        storedChunks = new Vector<ChunkInfo>();
        restored_files = new ConcurrentHashMap<String, Vector<Chunk>>();
        backedUpFiles = new Vector<FileInfo>();
        currentCapacity = INITIAL_CAPACITY;
        peerFiles = new Vector<FileInfo>();
        predecessorChunks = new Vector<ChunkInfo>();
        successorChunks = new Vector<ChunkInfo>();
        peersThatHaveChunk = new ConcurrentHashMap<String, Vector<FingerTableEntry>>();
        this.entry = entry;
    }

    /**
     * Sets a chunk's replication degree
     *
     * @param chunkName The name of the chunk
     * @param repDegree The new replication degree dor the chunk
     */
    public void setChunkRepDegree(String chunkName, int repDegree){
        chunksCurrentReplicationDegrees.put(chunkName, repDegree);
    }

    /**
     * Gets a file's desired replication degree
     *
     * @param fileId The id of the file
     * @return Returns the file's desired replication degree or -1 if the file hasn't been backed up
     */
    public int getFileRepDegree(String fileId){
        for (FileInfo fi: backedUpFiles){
            if (fi.getFileId().equals(fileId)){
                return fi.getReplicationDegree();
            }
        }
        return -1;
    }

    /**
     * Gets a file's information from the file's id
     *
     * @param fileId The id of the file
     * @return Returns the file's information or null if the file is not from the current peer
     */
    public FileInfo getFileInfoFromFileId(String fileId){
        for (FileInfo fi: peerFiles){
            if (fi.getFileId().equals(fileId)){
                return fi;
            }
        }
        return null;
    }

    /**
     * Gets a vector containing the peers who have a certain chunk stored
     *
     * @param chunkName The name of the chunk
     * @return Returns a vector containing the peers who have stored the chunk
     */
    public Vector<FingerTableEntry> getPeersThatHaveChunk(String chunkName){
        return peersThatHaveChunk.getOrDefault(chunkName, null);
    }

    /**
     * Sets the vector of the peers that have a certain chunk stored
     *
     * @param chunkName The name of the chunk
     * @param vec The vector containing the peers that have stored the chunk
     */
    public void setPeersThatHaveChunk(String chunkName, Vector<FingerTableEntry> vec){
        peersThatHaveChunk.put(chunkName, vec);
    }

    /**
     * Setter for the predecessorChunks attribute
     *
     * @param  predecessorChunks The new value for the predecessorChunks attribute
     */
    public void setPredecessorChunks(Vector<ChunkInfo> predecessorChunks){
        this.predecessorChunks = predecessorChunks;
    }

    /**
     * Setter for the successorChunks attribute
     *
     * @param  successorChunks The new value for the successorChunks attribute
     */
    public void setSuccessorChunks(Vector<ChunkInfo> successorChunks) {
        this.successorChunks = successorChunks;
    }

    /**
     * Getter for the predecessorChunks attribute
     *
     * @return Returns the predecessorChunks attribute
     */
    public Vector<ChunkInfo> getPredecessorChunks() {
        return predecessorChunks;
    }

    /**
     * Getter for the successorChunks attribute
     *
     * @return Returns the successorChunks attribute
     */
    public Vector<ChunkInfo> getSuccessorChunks() {
        return successorChunks;
    }

    /**
     * Getter for the peerFiles attribute
     *
     * @return Returns the peerFiles attribute
     */
    public Vector<FileInfo> getPeerFiles() {
        return peerFiles;
    }

    /**
     * Sets the peer's capacity
     *
     * @param newCapacity The new value for the peer's capacity
     */
    public void setCurrentCapacity(int newCapacity){
        currentCapacity = Math.min(newCapacity, INITIAL_CAPACITY);
    }

    /**
     * Getter for the peer's capacity
     *
     * @return Returns the peer's current capacity
     */
    public int getCurrentCapacity(){
        return currentCapacity;
    }

    /**
     * Checks if the peer will need to delete chunks in order to achieve the desired capacity or not
     *
     * @param newCapacity The new value for the peer's capacity
     *
     * @return Returns true if the peer needs to delete chunks, false otherwise
     */
    public boolean needsToDeleteChunks(int newCapacity){
        return getUsedStorage() > newCapacity;
    }

    /**
     * Gets the amount of storage that is being used by the peer
     *
     * @return Returns the amount of storage that is being used by the peer
     */
    public double getUsedStorage(){
        int usedStorage = 0;
        for (ChunkInfo c: storedChunks){
            usedStorage += c.getDataLength();
        }
        return (double)usedStorage / 1000;
    }

    /**
     * Gets the amount of storage that is not being used by the peer
     *
     * @return Returns the amount of storage that is not being used by the peer
     */
    public double getNotUsedStorage(){
        return currentCapacity - getUsedStorage();
    }

    /**
     * Gets the chunks that the peer has stored in its filesystem
     *
     * @return Returns a vector containing the chunks that the peer has stored in its filesystem
     */
    public Vector<ChunkInfo> getStoredChunks(){
        return this.storedChunks;
    }

    /**
     * Gets the replication degrees of the chunks from a certain file
     *
     * @param fileId The id of the file
     *
     * @return Returns a vector containing the replication degrees of the chunks from a certain file
     */
    public Vector<int[]> getChunksRepDegrees(String fileId){
        Vector<int[]> vec = new Vector<int[]>();
        for (String s: chunksCurrentReplicationDegrees.keySet()){
            if (chunkIsFromFile(s, fileId)){
                String[] sParts = s.split("-");
                int chunkNo = Integer.parseInt(sParts[1]);
                int arr[] = new int[2];
                arr[0] = chunkNo;
                arr[1] = chunksCurrentReplicationDegrees.get(s);
                vec.add(arr);
            }
        }
        return vec;
    }

    /**
     * Finds a backed up file
     *
     * @param pathname The name of the file
     *
     * @return Returns the file if it has been backed up, null otherwise
     */
    private FileInfo findBackedUpFile(String pathname){
        for (FileInfo info: backedUpFiles){
            if (info.getPathName().equals(pathname)){
                return info;
            }
        }
        return null;
    }

    /**
     * Marks a file as backed up
     *
     * @param f The information of the file
     */
    public void addBackedUpFile(FileInfo f){
        FileInfo fi = findBackedUpFile(f.getPathName());
        if (fi != null){
            return;
        }
        backedUpFiles.add(f);
    }

    /**
     * Finds a peer's file
     *
     * @param pathname The name of the file
     *
     * @return Returns the file if it has been found, null otherwise
     */
    private FileInfo findFile(String pathname){
        for (FileInfo info: peerFiles){
            if (info.getPathName().equals(pathname)){
                return info;
            }
        }
        return null;
    }

    /**
     * Adds a file to the peer's files
     *
     * @param f The information of the file
     */
    public void addFile(FileInfo f){
        FileInfo fi = findFile(f.getPathName());
        if (fi != null){
            return;
        }
        peerFiles.add(f);
    }

    /**
     * Marks a file as not backed up
     *
     * @param filename The name of the file
     */
    public void removeBackedUpFile(String filename){
        FileInfo fi = findBackedUpFile(filename);
        if (fi == null){
            return;
        }
        backedUpFiles.remove(fi);
    }

    /**
     * Getter for the backedUpFiles attribute
     *
     * @return Returns the backedUpFiles attribute
     */
    public Vector<FileInfo> getBackedUpFiles(){
        return backedUpFiles;
    }

    /**
     * Increments a chunk's current replication degree
     *
     * @param chunkName The name of the chunk
     */
    public void addChunkReplicationDegree(String chunkName){
        Integer count = chunksCurrentReplicationDegrees.get(chunkName);
        if (count == null){
            chunksCurrentReplicationDegrees.put(chunkName, 1);
        } else {
            chunksCurrentReplicationDegrees.put(chunkName, count+1);
        }
    }

    /**
     * Decrements a chunk's current replication degree
     *
     * @param chunkName The name of the chunk
     */
    public void decreaseChunkReplicationDegree(String chunkName){
        Integer count = chunksCurrentReplicationDegrees.get(chunkName);
        if (count == null){
            synchronized (this){
                chunksCurrentReplicationDegrees.put(chunkName, 0);
            }
        } else {
            synchronized (this){
                chunksCurrentReplicationDegrees.put(chunkName, count-1);
            }
        }
    }

    /**
     * Resets a chunk's current replication degree
     *
     * @param chunkName The name of the chunk
     */
    public synchronized void resetChunkReplicationDegree(String chunkName){
        chunksCurrentReplicationDegrees.put(chunkName, 0);
    }

    /**
     * Gets a chunk's current replication degree
     *
     * @param chunkName The name of the chunk
     *
     * @return Returns the current rel«plication degree of the chunk
     */
    public int getChunkReplicationDegree(String chunkName){
        Integer count = chunksCurrentReplicationDegrees.get(chunkName);
        if (count == null) return 0;
        else return count.intValue();
    }

    /**
     * Stores a chunk that has been restored
     *
     * @param c The chunk to store
     *
     * @return Returns 1 if successful
     */
    public int storeRestoredChunk(Chunk c){
        String fileId = c.getFileId();
        int chunkNo = c.getChunkNo();
       if (!restored_files.containsKey(fileId)){
           restored_files.put(fileId, new Vector<Chunk>());
       }

       restored_files.get(fileId).add(c);
        return 1;
    }

    /**
     * Gets the chunks from a restored file
     *
     * @param fileId The id of the file
     *
     * @return Returns a vector containing the restored chunks if they exist, null otherwise
     */
    public Vector<Chunk> getRestoredChunksFromFile(String fileId){
        if (restored_files.containsKey(fileId)){
            return restored_files.get(fileId);
        }
        return null;
    }

    /**
     * Removes a file from the restored files
     *
     * @param fileId The id of the file
     *
     * @return Returns true if successful, false otherwise
     */
    public boolean removeRestoredFile(String fileId){
        if (restored_files.containsKey(fileId)){
            restored_files.remove(fileId);
            return true;
        }
        return false;
    }

    /**
     * Deletes a chunk from the peer's filesystem
     *
     * @param chunkName The name of the chunk
     *
     * @return Returns true if successful, false otherwise
     */
    public boolean deleteChunkFromFilesystem(String chunkName){

        File f  = new File("files/peer" + Peer.getId() + "/chunks/" + chunkName);
        return f.delete();
    }

    /**
     * Checks if a chunk is from one of the peer's files
     *
     * @param c The chunk
     *
     * @return Returns true if the chunk is from one of the peer's files, false otherwise
     */
    private boolean chunkIsFromPeersFile(Chunk c){
        for (FileInfo fi: peerFiles){
            if (c.getFileId().equals(fi.getFileId()))
                return true;
        }
        for (FileInfo fi: backedUpFiles){
            if (c.getFileId().equals(fi.getFileId()))
                return true;
        }
        return false;
    }

    /**
     * Writes the received data to a file at a given offset
     *
     * @param data The data to be written to the file
     * @param f The file to write the data to
     * @param offset The position of the file where the data should be written
     */
    public void writeToFile(byte[] data, File f, int offset) throws IOException, RuntimeException{
        Path path = f.toPath();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        channel.write(buffer, offset, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                System.out.println("Writing to file finished");
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                throw new RuntimeException("Writing failed");
            }
        });
    }

    /**
     * Stores a chunk on the peer's filesystem
     *
     * @param c The chunk to be stored
     *
     * @return Returns 0 if there is no space to store the chunk, 1 if successful and -1 if an error occurs
     */
    public int storeChunk(Chunk c){

        if (chunkIsFromPeersFile(c)){
            return -1;
        }

        double notUsedStorage = getNotUsedStorage();
        if (c.getData().length > notUsedStorage*1000 || currentCapacity <= 0.5){
            System.out.println("Can't store chunk, not enough space...");
            return -2;
        }

        String fileId = c.getFileId();
        int chunkNo = c.getChunkNo();

        File f1 = new File("files/peer" + String.valueOf(Peer.getId()));
        f1.mkdir();

        File f = new File("files/peer" + String.valueOf(Peer.getId()) + "/chunks");
        f.mkdir();

        String chunkName = fileId + "-" + String.valueOf(chunkNo);
        if (chunkIsStored(chunkName)){
            Peer.serialize();
            return -3;
        }
        String path = "files/peer" + String.valueOf(Peer.getId()) + "/chunks/" + chunkName;
        File chunkFile = new File(path);
        try{
            chunkFile.createNewFile();
            writeToFile(c.getData(), chunkFile, 0);
            storedChunks.add(new ChunkInfo(c.getFileId(), c.getChunkNo(), c.getReplicationDegree(), c.getData().length, c.getOriginalPeerId(), c.getOriginalEntry()));
            chunksInFilesystem.add(fileId + "-" + String.valueOf(chunkNo));
        } catch (RuntimeException e){
            System.out.println("Couldn't write to file!");
            return -1;
        } catch (IOException e){
            e.printStackTrace();
            return -1;
        }
        this.addChunkReplicationDegree(c.getFileId() + "-" + c.getChunkNo());
        Peer.serialize();
        return 1;

    }

    /**
     * Deletes all chunks from a certain file from the peer's filesystem
     *
     * @param fileId The id of the file
     *
     * @return Returns 1 if successful, -1 otherwise
     */
    public int delete(String fileId){

        String basePath = "files/peer" + Peer.getId() + "/chunks/";
        Vector<String> to_remove = new Vector<String>();
        for (String chunkName: chunksInFilesystem){
            System.out.println("Target chunk: " + chunkName);
            if (chunkIsFromFile(chunkName, fileId)){
                File f = new File(basePath + chunkName);
                if (f.delete()){
                    removeFromStoredChunks(chunkName);
                    to_remove.add(chunkName);
                } else {
                    System.out.println("Could not delete chunk " + chunkName);
                }
            }
        }
        for (String name: to_remove){
            chunksInFilesystem.remove(name);
        }
        Peer.serialize();
        return 1;

    }

    /**
     * Checks if a chunk is from a certain file
     *
     * @param chunkName The name of the chunk
     * @param fileId The id of the file
     *
     * @return Returns true if the chunk is from the file, false otherwise
     */
    private boolean chunkIsFromFile(String chunkName, String fileId){
        return chunkName.contains(fileId);
    }

    /**
     * Checks if a chunk is stored in the peer's filesystem
     *
     * @param chunkName The name of the chunk
     *
     * @return Returns true if the chunk is stored in the peer's filesystem, false otherwise
     */
    private boolean chunkIsStored(String chunkName){
        for(int i = 0; i < chunksInFilesystem.size(); i++){
            if (chunksInFilesystem.get(i).equals(chunkName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a peer has a certain chunk stored
     *
     * @param fileId The id of the chunk's file
     * @param chunkNo The chunk's sequence number
     *
     * @return Returns true if the peer has the chunk stored, false otherwise
     */
    public boolean hasChunk(String fileId, int chunkNo){
        return chunkIsStored(fileId + "-" + String.valueOf(chunkNo));
    }

    /**
     * Gets a certain chunk
     *
     * @param fileId The id of the chunk's file
     * @param chunkNo The chunk's sequence number
     *
     * @return Returns the chunk if it is stored in the peer's filesystem, null otherwise
     */
    public Chunk getChunk(String fileId, int chunkNo){
        if (hasChunk(fileId, chunkNo)){
            for (int i = 0; i < storedChunks.size(); i++){
                if (storedChunks.get(i).getFileId().equals(fileId) && storedChunks.get(i).getChunkNo() == chunkNo){
                    File f = new File("files/peer" + Peer.getId() + "/chunks/" + fileId + "-" + chunkNo);
                    byte[] fileData;
                    try{
                        fileData = Files.readAllBytes(f.toPath());
                    } catch (IOException e){
                        e.printStackTrace();
                        return null;
                    }

                    return new Chunk(fileId, chunkNo, fileData, storedChunks.get(i).getReplicationDegree(), storedChunks.get(i).getOriginalPeerId());
                }
            }
        }
        return null;
    }

    /**
     * Removes a chunk from the peer's stored chunks
     *
     * @param chunkName The name of the chunk
     *
     * @return Returns true if successful, false otherwise
     */
    public boolean removeFromStoredChunks(String chunkName){
        for (ChunkInfo c: storedChunks){
            if ((c.getFileId() + "-" + c.getChunkNo()).equals(chunkName)){
                storedChunks.remove(c);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a file's byte data
     *
     * @param f The file
     *
     * @return Returns the file's byte data
     */
    private byte[] getFileBytes(File f) {
        byte[] bytes = new byte[(int) f.length()];

        try{
            FileInputStream fis = new FileInputStream(f);
            fis.read(bytes);
            fis.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return bytes;

    }

    /**
     * Writes the received chunks' data to a file
     *
     * @param f The file
     * @param chunks The chunks to be written
     *
     * @return Returns true if successful, false otherwise
     */
    public boolean writeChunksToFile(File f, Vector<Chunk> chunks){
        try{
            FileOutputStream fos = new FileOutputStream(f, true);
            for (Chunk c: chunks){
                fos.write(c.getData());
            }
            fos.close();
            return true;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets a certain chunk from a file
     *
     * @param f The file
     * @param replicationDegree The chunk's desired replication degree
     * @param chunkNo The chunk's sequence number on the file
     * @param fileId The id of the file
     * @return Returns the requested chunk, or null if something fails
     */
    public Chunk getChunkFromFile(File f, int replicationDegree, int chunkNo, String fileId) throws IOException{
        Path p = f.toPath();
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
        int offset = (chunkNo - 1) * 64000;
        int len = Math.min(64000, (int)(f.length()-offset));
        ByteBuffer buffer = ByteBuffer.allocate(len);
        Chunk c;
        Future<Integer> operation = channel.read(buffer, offset);

        while(!operation.isDone()){
            if (operation.isCancelled()){
                return null;
            }
        }

        buffer.flip();
        byte[] buf = new byte[len];
        buffer.get(buf);
        buffer.clear();
        return new Chunk(fileId, chunkNo, buf, replicationDegree, Peer.getId());

    }

    /**
     * Splits a file into chunks
     *
     * @param f The file
     * @param replicationDegree The desired replication degree for the chunks
     *
     * @return Returns the file's chunks
     */
    public Vector<Chunk> getChunksFromFile(File f, int replicationDegree) throws NoSuchAlgorithmException, IOException{

        byte[] fileBytes = getFileBytes(f);

        Vector<Chunk> chunks = new Vector<Chunk>();

        int numChunks = fileBytes.length / MAX_CHUNK_SIZE + 1;
        int remainder = fileBytes.length % MAX_CHUNK_SIZE;
        String fileId = generateFileId(f);
        int offset = 0;

        for (int i = 0; i < numChunks; i++){
            Chunk c;
            if (i == numChunks-1){
                c  = new Chunk(fileId, i+1, new byte[remainder], replicationDegree, entry.getId());
                c.setOriginalEntry(entry);
            } else {
                c = new Chunk(fileId, i+1, new byte[MAX_CHUNK_SIZE], replicationDegree, entry.getId());
                c.setOriginalEntry(entry);
            }
            int end = Math.min(fileBytes.length, offset+MAX_CHUNK_SIZE);

            if (c.copyPartOfArrayToData(fileBytes, offset, end)){
                offset += end-offset;
            }
            chunks.add(c);
        }

        return chunks;

    }

    /**
     * Generates the id of a file
     *
     * @param f The file
     *
     * @return Returns the id of the file
     */
    public String generateFileId(File f) throws NoSuchAlgorithmException {
        String starting = f.getName() + f.lastModified();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] stringHash = digest.digest(starting.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexHash = new StringBuilder(stringHash.length*2);

        for (int i = 0; i < stringHash.length; i++){
            hexHash.append(String.format("%02x", stringHash[i]));
        }

        return hexHash.toString();
    }

}