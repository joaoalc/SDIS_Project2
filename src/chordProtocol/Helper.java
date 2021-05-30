package chordProtocol;

/**
 * The Helper class has a helper method
 */
public class Helper {

    /**
     * Checks if an id is between the other two received ids in the chord node
     *
     * @param  n1 The lower bound
     * @param  n1 The target id
     * @param  n1 The upper bound
     */
    public static boolean between(int n1, int n2, int n3){
        if (n1 < 0 || n2 < 0 || n3 < 0) return false;
        if (n1 < n3) return (n1 < n2 && n2 < n3);
        else return (n1 < n2 || n2 < n3);
    }
}