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

public class Peer extends Node {

    private int port;
    private String address;

    public Peer(String address, int port){
        super(new InetSocketAddress("localhost", port));
        this.address = "localhost";
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    public static void main(String[] args){

        if (args.length != 2){
            System.out.println("Usage: java Peer <port> <action>");
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);
        String action = args[2];

        Peer p = new Peer(address, port);

        System.out.println("Peer");

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new MessageReceiver(p.getPort()));

        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(3);

        Stabilization stabilization = new Stabilization(p);
        scheduledExecutor.scheduleAtFixedRate(stabilization, 1, 20, TimeUnit.SECONDS);

        FixFingers fixFingers = new FixFingers(p);
        scheduledExecutor.scheduleAtFixedRate(fixFingers, 1, 20, TimeUnit.SECONDS);

        CheckPredecessorFailure checkPredecessorFailure = new CheckPredecessorFailure(p);
        scheduledExecutor.scheduleAtFixedRate(checkPredecessorFailure, 1, 20, TimeUnit.SECONDS);


        /*
        if (action.equals("RECEIVE")){
            Thread t = new Thread(p.getReceiver());
            t.start();
        } else {
            InetSocketAddress address = new InetSocketAddress("localhost", 8001);
            String answer = p.sender.sendWithAnswer("Message test", address);
            System.out.println("Answer: " + answer);
        }
        */
        
    }


}