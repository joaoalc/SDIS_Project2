/**
 * File:    ChunkSortable.java
 *
 * Authors:  Tomás Costa Fontes (up201806252@fe.up.pt), Pedro Emanuel de Sousa Pinto (up201806251@fe.up.pt)
 * Date:     april 2021
 *
 */

package filesystem;

public class ChunkSortable implements Comparable<ChunkSortable>{

    private ChunkInfo c;
    private int score;

    public ChunkSortable(ChunkInfo c, int score){
        this.c = c;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public ChunkInfo getChunkInfo() {
        return c;
    }

    @Override
    public int compareTo(ChunkSortable chunkSortable) {
        return this.score - chunkSortable.getScore();
    }
}