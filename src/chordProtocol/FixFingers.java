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
            FingerTableEntry successor = node.findSuccessor(successorToFindId);
            System.out.println("[FixFingers] Iteration " + next + ": successor_of_node" + successorToFindId + "=" + successor.getId());
            if (successor == null){
                System.out.println("Error fixing fingers [successor is null].");
                System.out.println("Current node: " + node.getId() + ".  Target Node: " + successor.getId() + ".");
                return;
            }

            node.setFinger(next, successor);
        }
        System.out.println("-----------------------");

        node.displayFingerTable();

    }
}