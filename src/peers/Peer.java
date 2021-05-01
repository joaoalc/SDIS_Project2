package peers;

import java.net.InetSocketAddress;

public class Peer{

    private MessageSender sender;
    private MessageReceiver receiver;
    private int port;

    public Peer(int port){
        this.port = port;
        sender = new MessageSender();
        receiver = new MessageReceiver(this.port);
    }

    public MessageReceiver getReceiver(){
        return receiver;
    }

    public static void main(String[] args){

        if (args.length != 2){
            System.out.println("Usage: java Peer <port> <action>");
        }

        int port = Integer.parseInt(args[0]);
        String action = args[1];

        Peer p = new Peer(port);

        System.out.println("Peer");

        if (action.equals("RECEIVE")){
            Thread t = new Thread(p.getReceiver());
            t.start();
        } else {
            InetSocketAddress address = new InetSocketAddress("localhost", 8001);
            String answer = p.sender.sendWithAnswer("Message test", address);
            System.out.println("Answer: " + answer);
        }
        
    }


}