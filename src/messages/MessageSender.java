package messages;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;

public class MessageSender {

    public MessageSender(){

    }

    public Message sendWithAnswer(Message message, InetSocketAddress address){

        SSLSocket s = null;

        try{
            s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address.getAddress(), address.getPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(message);
            System.out.println("Wrote");
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }

        //Answer
        Message answer = null;
        try{
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            try{
                answer = (Message) in.readObject();
                System.out.println("Read");
            } catch(ClassNotFoundException e){
                e.printStackTrace();
                return null;
            }
            s.close();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return answer;
    }

}