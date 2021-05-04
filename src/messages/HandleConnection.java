package messages;

import chordProtocol.Node;

import javax.net.ssl.SSLSocket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HandleConnection implements Runnable {

    private SSLSocket sslSocket;
    private Node node;

    public HandleConnection(SSLSocket s, Node n){
        this.sslSocket = s;
        node = n;
    }

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