package messages;

import chordProtocol.Node;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  The class MessageReceiver is responsible for receiving incoming connections
 */
public class MessageReceiver implements Runnable {

    private SSLServerSocket s;
    private ExecutorService executor;
    private Node node;

    /**
     * Constructor for the MessageReceiver class
     *
     * @param port The port where to listen for incoming connections
     * @param n The current chord node
     */
    public MessageReceiver(int port, Node n){
        try{
            s = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
            s.setNeedClientAuth(true);
            s.setEnabledCipherSuites(((SSLServerSocketFactory) SSLServerSocketFactory.getDefault()).getSupportedCipherSuites());
        } catch (IOException e){
            e.printStackTrace();
        }
        executor = Executors.newFixedThreadPool(10);
        node = n;
    }

    /**
     * Receives a connection
     */
    public void receiveConnection(){
        try{
            SSLSocket sslSocket = (SSLSocket) s.accept();
            executor.submit(new HandleConnection(sslSocket, node));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Listens for connections and handles them when received
     */
    public void run(){

        while(true){
            receiveConnection();
        }

    }

}