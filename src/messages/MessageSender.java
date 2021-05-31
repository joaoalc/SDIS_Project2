package messages;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;

/**
 *  The class MessageSender is responsible for sending messages
 */
public class MessageSender {

    /**
     * Constructor for the MessageSender class
     */
    public MessageSender(){
    }

    /**
     * Sends a message to the received address and waits for a response
     *
     * @param message The message to be sent
     * @param address The address to send the message to
     */
    public Message sendWithAnswer(Message message, InetSocketAddress address){

        SSLSocket s = null;

        try{
            s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address.getAddress(), address.getPort());
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(message);
        } catch(IOException e){
            System.out.println("Can't send message...");
            return null;
        }

        //Answer
        Message answer = null;
        try{
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
            try{
                answer = (Message) in.readObject();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
                return null;
            }
            s.close();
        } catch (IOException e){
            System.out.println("Can't receive answer...");
            return null;
        }

        return answer;
    }

}