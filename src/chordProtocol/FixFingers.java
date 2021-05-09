package chordProtocol;

public class FixFingers implements Runnable {

    private Node node;

    public FixFingers(Node n){
        node = n;
    }

    @Override
    public void run() {
        node.displayFingerTable();
        System.out.println("-------- Fix Fingers ---------");
        for (int next = 1; next <= Node.M; next++){
            int successorToFindId = (node.getId() + (int) Math.pow(2, next-1)) % (int) Math.pow(2, Node.M);
            // Find successor of id successorToFindId and update the corresponding finger
            System.out.println("Sent find successor");
            FingerTableEntry successor = node.findSuccessor(successorToFindId);
            System.out.println("After find successor");
            if (successor == null){
                System.out.println("Error fixing fingers [successor is null].");
                //System.out.println("Current node: " + node.getId() + ".  Target Node: " + successor.getId() + ".");
                //break;
            } else {
                System.out.println("[FixFingers] Iteration " + (next-1) + ": successor_of_node" + successorToFindId + "=" + successor.getId());
            }

            node.setFinger(next-1, successor);
            System.out.println("Finger set");
        }
        System.out.println("-----------------------");

        node.displayFingerTable();
        //System.out.println("Predecessor: " + node.getPredecessor().getId());

    }
}