package peers;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;

public class MessageReceiver implements Runnable {

    private SSLServerSocket s;

    public MessageReceiver(int port){
        try{
            s = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
            s.setNeedClientAuth(true);
            s.setEnabledCipherSuites(((SSLServerSocketFactory) SSLServerSocketFactory.getDefault()).getSupportedCipherSuites());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String receive(){
        try{
            SSLSocket sslSocket = (SSLSocket) s.accept();
            System.out.println("Address: " + sslSocket.getInetAddress().getHostAddress());

            ObjectInputStream in = new ObjectInputStream(sslSocket.getInputStream());
            try{
                Message m = (Message) in.readObject();
                System.out.println("Message: " + m.getMessage());
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
            System.out.println("Reader ready");
            System.out.println("Reader finished");
            Message m = new Message("Resposta");
            ObjectOutputStream out = new ObjectOutputStream(sslSocket.getOutputStream());
            out.writeObject(m);
            System.out.println("Wrote answer");
            sslSocket.close();
        } catch(IOException e){
            e.printStackTrace();
            return "";
        }


        return "";
    }

    public void run(){

        while(true){
            receive();
        }

    }

}