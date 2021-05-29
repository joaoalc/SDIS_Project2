package peers;

import chordProtocol.CheckPredecessorFailure;
import chordProtocol.FixFingers;
import chordProtocol.Node;
import chordProtocol.Stabilization;
import filesystem.ChunkFileSystemManager;
import messages.MessageReceiver;
import subProtocols.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
Client:
./peer.sh 1 Peer1 localhost 8000
./peer.sh 2 Peer2 localhost 8001 localhost 8000
./test.sh Peer1 BACKUP teste.txt 1

java -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore -Djavax.net.ssl.trustStorePassword=123456 peers.Peer 1 Peer1 localhost 8000
java -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore -Djavax.net.ssl.trustStorePassword=123456 peers.Peer 2 Peer2 localhost 8001 localhost 8000
java peers.TestApp Peer1 BACKUP teste.txt 1
 */

public class Peer implements RMIStub{

    private int port;
    private String address;
    private Node node;
    private static boolean isRestoring;
    private static int id;
    private static ChunkFileSystemManager manager;

    public Peer(String address, int port, int id){
        this.address = address;
        this.port = port;
        this.id = id;
        System.out.println("Peer Id: " + this.id);
        isRestoring = false;
        node = new Node(new InetSocketAddress(address, port));
        manager = deserialize();
        if (manager == null) manager = new ChunkFileSystemManager(node.getEntry());
    }

    /**
     * Getter for the id attribute
     *
     * @return Returns the id attribute
     */
    public static int getId(){
        return id;
    }

    /**
     * Getter for the manager attribute
     *
     * @return Returns the manager attribute
     */
    public static ChunkFileSystemManager getManager(){
        return manager;
    }

    public int getPort(){
        return port;
    }

    public Node getNode(){
        return node;
    }

    public void start(){

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new MessageReceiver(port, node));

        ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(4);

        Stabilization stabilization = new Stabilization(node);
        scheduledExecutor.scheduleAtFixedRate(stabilization, 1, 2, TimeUnit.SECONDS);


        FixFingers fixFingers = new FixFingers(node);
        scheduledExecutor.scheduleAtFixedRate(fixFingers, 2, 2, TimeUnit.SECONDS);


        CheckPredecessorFailure checkPredecessorFailure = new CheckPredecessorFailure(node);
        scheduledExecutor.scheduleAtFixedRate(checkPredecessorFailure, 3, 2, TimeUnit.SECONDS);

        Inform inform = new Inform(node);
        scheduledExecutor.scheduleAtFixedRate(inform, 4, 2, TimeUnit.SECONDS);

    }

    public static void main(String[] args){

        System.out.println(args.length);
        if (args.length != 4 && args.length != 6){
            System.out.println("Usage: java Peer <id> <peerAccessPoint> <address> <port> [<chord_peer_address> <chord_peer_port>]");
            return;
        }

        int id = Integer.parseInt(args[0]);
        String peerAccessPoint = args[1];
        String address = args[2];
        int port = Integer.parseInt(args[3]);

        Peer p = new Peer(address, port, id);

        try {
            RMIStub stub = (RMIStub) UnicastRemoteObject.exportObject(p, 0);
            Registry reg = LocateRegistry.getRegistry("localhost");
            reg.rebind(peerAccessPoint, stub);
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        p.start();

        if (args.length == 4){
            p.getNode().createNewChordRing();
        } else if (args.length == 6){
            String peerFromRingAddress = args[4];
            int peerFromRingPort = Integer.parseInt(args[5]);
            p.getNode().joinExistingChordRing(new InetSocketAddress(peerFromRingAddress, peerFromRingPort));
        }

    }


    @Override
    public void backup(String file, int replicationDegree) throws RemoteException {

        Backup protocol = new Backup(file, replicationDegree, this.node, false);
        node.getThreadExecutor().execute(protocol);

    }

    @Override
    public void delete(String file) throws RemoteException {
        File f = new File("files/peer" + id + "/peer_files/" + file);
        String fileId = null;
        try{
            fileId = manager.generateFileId(f);
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return;
        }

        Delete protocol = new Delete(fileId, file, node);
        node.getThreadExecutor().execute(protocol);

    }

    @Override
    public void restore(String file) throws RemoteException {
        Restore protocol = new Restore(file, node);
        node.getThreadExecutor().execute(protocol);
    }

    @Override
    public void reclaim(int newCapacity) throws RemoteException {
        Reclaim protocol = new Reclaim(newCapacity, node);
        node.getThreadExecutor().execute(protocol);
    }

    @Override
    public void state() throws RemoteException {
        State protocol = new State(node);
        node.getThreadExecutor().execute(protocol);
    }

    /**
     * Serializes the chunk's filesystem manager and stores it
     */
    public static void serialize(){
        File f1 = new File("files/storage/");
        f1.mkdir();
        File f = new File("files/storage/peer" + Peer.getId() + ".ser");
        try{
            f.createNewFile();

            FileOutputStream os = new FileOutputStream(f, false);
            ObjectOutputStream objectStream = new ObjectOutputStream(os);
            objectStream.writeObject(manager);
            objectStream.close();
            os.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("[Peer] Serialized");

    }

    /**
     * Deserializes the chunk's filesystem manager if it exists, else creates a new one
     *
     * @return Returns the chunk's filesystem manager
     */
    private static ChunkFileSystemManager deserialize(){
        File f = new File("files/storage/peer" + Peer.getId() + ".ser");
        ChunkFileSystemManager c = null;
        if (f.exists()){
            try{
                FileInputStream is = new FileInputStream(f);
                ObjectInputStream objectStream = new ObjectInputStream(is);
                c = (ChunkFileSystemManager) objectStream.readObject();
                objectStream.close();
                is.close();
            } catch(Exception e){
                e.printStackTrace();
            }

            System.out.println("[Peer] Deserialized");

        }

        return c;
    }
}