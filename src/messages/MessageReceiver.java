package messages;

import chordProtocol.Node;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageReceiver implements Runnable {

    private SSLServerSocket s;
    private ExecutorService executor;
    private Node node;

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

    public void receiveConnection(){
        try{
            SSLSocket sslSocket = (SSLSocket) s.accept();
            executor.submit(new HandleConnection(sslSocket, node));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void run(){

        while(true){
            receiveConnection();
        }

    }

}