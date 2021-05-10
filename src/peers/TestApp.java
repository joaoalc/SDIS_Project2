package peers;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *  The class TestApp implements a testing application for the project
 */
public class TestApp {

    /**
     * Main method (TestApp's entry point)
     *
     * @param args The command line arguments
     */
    public static void main(String[] args){
        if (args.length > 4 || args.length < 2 ){
            System.out.println("Wrong number of arguments!");
            return;
        }

        String accessPoint = args[0];
        String protocol = args[1];

        try {
            Registry reg = LocateRegistry.getRegistry("localhost");
            RMIStub stub = (RMIStub) reg.lookup(accessPoint);
            if (protocol.equals("BACKUP")){
                if (args.length != 4){
                    System.out.println("BACKUP protocol requires four arguments!");
                    return;
                }
                stub.backup(args[2], Integer.parseInt(args[3]));
            } else if (protocol.equals("DELETE")){
                if (args.length != 3){
                    System.out.println("DELETE protocol requires three arguments!");
                    return;
                }
                stub.delete(args[2]);
            } else if (protocol.equals("RESTORE")){
                if (args.length != 3){
                    System.out.println("DELETE protocol requires three arguments!");
                    return;
                }
                System.out.println("RESTORE");
                stub.restore(args[2]);
            } else if(protocol.equals("STATE")){
                if (args.length != 2){
                    System.out.println("STATE protocol doesn't require any additional arguments!");
                    return;
                }
                stub.state();
            } else if (protocol.equals("RECLAIM")){
                if (args.length != 3){
                    System.out.println("RECLAIM protocol requires three arguments!");
                    return;
                }
                stub.reclaim(Integer.parseInt(args[2]));
            }else {
                System.out.println("Invalid protocol");
                return;
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

    }

}