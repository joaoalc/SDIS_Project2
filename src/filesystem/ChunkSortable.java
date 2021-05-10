/**
 * File:    ChunkSortable.java
 *
 * Authors:  Tomás Costa Fontes (up201806252@fe.up.pt), Pedro Emanuel de Sousa Pinto (up201806251@fe.up.pt)
 * Date:     april 2021
 *
 */

package filesystem;

public class ChunkSortable implements Comparable<ChunkSortable>{

    private Chunk c;
    private int score;

    public ChunkSortable(Chunk c, int score){
        this.c = c;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public Chunk getChunk() {
        return c;
    }

    @Override
    public int compareTo(ChunkSortable chunkSortable) {
        return this.score - chunkSortable.getScore();
    }
}