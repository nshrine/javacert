/*
 * Server.java
 *
 * Created on 19 September 2004, 14:27
 */

package suncertify.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for server objects that provide <code>RemoteBookingDB</code>
 * objects to clients for remote database access.
 *
 * @author Nick Shrine
 * @see RemoteBookingDB
 */
public interface Server extends Remote {
    
    /**
     * Returns a Remote object with which clients can access the database on
     * the server.
     *
     * @return <code>RemoteBookingDB</code> object for remote database access.
     *
     * @throws RemoteException if there is a communication problem with the 
     *          client.
     */
    public RemoteBookingDB getBookingDB() throws RemoteException;
}
