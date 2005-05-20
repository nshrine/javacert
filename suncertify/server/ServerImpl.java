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
    
    public RemoteBookingDB getBookingDB() throws RemoteException {        
        return new RemoteBookingData(db);
    }    
}
