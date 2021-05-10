/**
 * File:    FileInfo.java
 *
 * Authors:  Tomás Costa Fontes (up201806252@fe.up.pt), Pedro Emanuel de Sousa Pinto (up201806251@fe.up.pt)
 * Date:     april 2021
 *
 */

package filesystem;

import java.io.Serializable;

/**
 *  The class FileInfo contains information of a file.
 */
public class FileInfo implements Serializable {

    private String fileId;
    private String pathName;
    private int replicationDegree;

    /**
     * Constructor for the Channel class
     *
     * @param pathName The file's name
     * @param fileId The id of the file
     * @param replicationDegree The replication degree of the file's chunks
     *
     */
    public FileInfo(String pathName, String fileId, int replicationDegree){
        this.pathName = pathName;
        this.fileId = fileId;
        this.replicationDegree = replicationDegree;
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
     * @param fileId The id of the file
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Getter for the pathName attribute
     *
     * @return Returns the pathName attribute
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * Setter for the pathName attribute
     *
     * @param pathName The name of the file
     */
    public void setPathName(String pathName) {
        this.pathName = pathName;
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
     * Setter for the replicationDegree attribute
     *
     * @param replicationDegree The replication degree of the file's chunks
     */
    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }
}