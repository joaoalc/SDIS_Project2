package chordProtocol;

/**
 * The class FixFingers is run periodically and is responsible for making sure the node's finger table values are correct
 */
public class FixFingers implements Runnable {

    private Node node;

    /**
     * Constructor for the FixFingers class
     *
     * @param n The node
     *
     */
    public FixFingers(Node n){
        node = n;
    }

    /**
     * Runs the Fix Fingers routine
     */
    @Override
    public void run() {
        System.out.println("[Fix Fingers] Starting");
        for (int next = 1; next <= Node.M; next++){
            int successorToFindId = (node.getId() + (int) Math.pow(2, next-1)) % (int) Math.pow(2, Node.M);
            // Find successor of id successorToFindId and update the corresponding finger
            FingerTableEntry successor = node.findSuccessor(successorToFindId);

            node.setFinger(next-1, successor);
        }

    }
}