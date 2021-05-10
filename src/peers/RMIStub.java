package peers;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *  The interface RMIStub is used with the RMI
 */
public interface RMIStub extends Remote {
    public void backup(String file, int replicationDegree) throws RemoteException;
    public void delete(String file) throws RemoteException;
    public void restore(String file) throws RemoteException;
    public void reclaim(int newCapacity) throws RemoteException;
    public void state() throws RemoteException;
}