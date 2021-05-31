package messages;

import chordProtocol.Node;

import javax.net.ssl.SSLSocket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *  The class HandleConnection is run whenever a peer receives an incoming connection and is responsible for dealing with the received connection
 */
public class HandleConnection implements Runnable {

    private SSLSocket sslSocket;
    private Node node;


    /**
     * Constructor for the Chunk class
     *
     * @param s The socket created for the received connection
     * @param n The chord node of the peer
     */
    public HandleConnection(SSLSocket s, Node n){
        this.sslSocket = s;
        node = n;
    }

    /**
     * Runs the handle connection routine
     */
    @Override
    public void run() {
        try{
            ObjectInputStream in = new ObjectInputStream(sslSocket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(sslSocket.getOutputStream());

            Message m = (Message) in.readObject();

            MessageHandler handler = new MessageHandler(node, m, out);
            handler.handle();

            in.close();
            out.close();
            sslSocket.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}