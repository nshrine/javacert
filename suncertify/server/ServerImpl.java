/*
 * ServerImpl.java
 *
 * Created on 19 September 2004, 14:29
 */

package suncertify.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import suncertify.db.BookingDB;

/**
 * Implementation of the {@link Server Server} interface.
 *
 * @author  Nick
 */
public class ServerImpl extends UnicastRemoteObject implements Server {
    
    /**
     * Server-side data access object.
     */
    protected final BookingDB db;
    
    /**     
     * Creates a Server object using the provided BookingDB object
     * for data access on the server side.
     *
     * @param db the server-side data access object to be used.
     *
     * @throws RemoteException if there is a communication problem with the
     *          client.
     */
    public ServerImpl(BookingDB db) throws RemoteException {
        super();
        this.db = db;
    }
    
    /**
     * Returns a {@link RemoteBookingDB RemoteBookingDB} remote object for
     * remote clients to use for accessing data stored on the network server.
     *
     * @throws RemoteException if there is a communication problem with the
     *          client.
     *
     * @return {@link RemoteBookingDB RemoteBookingDB} object to use for data
     *          access.
     */    
    public RemoteBookingDB getBookingDB() throws RemoteException {        
        return new RemoteBookingData(db);
    }    
}
