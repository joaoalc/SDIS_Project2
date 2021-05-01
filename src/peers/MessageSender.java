package peers;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;

public class MessageSender {

    public MessageSender(){

    }

    public String sendWithAnswer(String message, InetSocketAddress address){

        SSLSocket s = null;

        System.out.println("Host address: " + address.getAddress());

        try{
            s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address.getAddress(), address.getPort());
            Message m = new Message("Teste");
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(m);

            System.out.println("Sent");
        } catch(IOException e){
            e.printStackTrace();
            return "";
        }

        //Answer
        String answer = "";
        try{
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            System.out.println("Reader ready");
            try{
                Message m = (Message) in.readObject();
                System.out.println("Message: " + m.getMessage());
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }

            System.out.println("After message loop");

            s.close();
        } catch (IOException e){
            e.printStackTrace();
            return "";
        }

        return answer;
    }

}