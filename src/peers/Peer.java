package peers;

import chordProtocol.CheckPredecessorFailure;
import chordProtocol.FixFingers;
import chordProtocol.Node;
import chordProtocol.Stabilization;
import messages.MessageReceiver;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
Client:
java -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore -Djavax.net.ssl.trustStorePassword=123456 peers.Peer localhost 8000

Server:


 */

public class Peer {

    private int port;
    private String address;
    private Node node;

    public Peer(String address, int port){
        this.address = address;
        this.port = port;
        node = new Node(new InetSocketAddress(address, port));
    }

    public int getPort(){
        return port;
    }

    public Node getNode(){
        return node;
    }

    public void start(){

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new MessageReceiver(port, node));

        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(3);

        Stabilization stabilization = new Stabilization(node);
        scheduledExecutor.scheduleAtFixedRate(stabilization, 1, 10, TimeUnit.SECONDS);


        FixFingers fixFingers = new FixFingers(node);
        scheduledExecutor.scheduleAtFixedRate(fixFingers, 2, 10, TimeUnit.SECONDS);


        CheckPredecessorFailure checkPredecessorFailure = new CheckPredecessorFailure(node);
        scheduledExecutor.scheduleAtFixedRate(checkPredecessorFailure, 3, 10, TimeUnit.SECONDS);

    }

    public static void main(String[] args){

        System.out.println(args.length);
        if (args.length != 2 && args.length != 4){
            System.out.println("Usage: java Peer <address> <port> [<chord_peer_address> <chord_peer_port>]");
            return;
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);

        Peer p = new Peer(address, port);

        System.out.println("Peer");
        p.start();

        if (args.length == 2){
            p.getNode().createNewChordRing();
        } else if (args.length == 4){
            String peerFromRingAddress = args[2];
            int peerFromRingPort = Integer.parseInt(args[3]);
            p.getNode().joinExistingChordRing(new InetSocketAddress(peerFromRingAddress, peerFromRingPort));
        }

    }


}