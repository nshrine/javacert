/*
 * RemoteBookingData.java
 *
 * Created on 26 August 2004, 16:16
 */

package suncertify.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import suncertify.db.*;

/**
 * This implementation of the {@link RemoteBookingDB RemoteBookingDB} interface
 * allows remote clients to access the database. One instanace per client is
 * instantiated on the server.
 * <p>
 * A note of the record currently locked by the remote client is kept so that
 * if the client dies or the network connection is lost the record can be
 * unlocked on the server.
 *
 * @author Nick Shrine
 */
public class RemoteBookingData extends UnicastRemoteObject implements
        RemoteBookingDB, Unreferenced {        
    
    /**
     * Server-side data access object.
     */
    protected final BookingDB db;
    
    /**
     * The record locked by the currently connected client.
     */
    protected int lockedRecord;
    
    /**
     * The cookie with which the currently locked record was locked.
     */
    protected long savedCookie;
    
    /**
     * Flag to indicate that the connection to the client has been lost.
     */
    protected boolean unreferenced;
    
    /**     
     * Creates a RemoteBookingData object using the provided BookingDB object
     * for data access on the server side.
     *
     * @param db the server-side data access object to be used.
     *
     * @throws RemoteException if there is a communication problem with the
     *          client.
     */
    public RemoteBookingData(BookingDB db) throws RemoteException {         
        super();                
        this.db = db;              
    }
    
    public String[] read(int recNo) throws RecordNotFoundException,
            RemoteException {         
        return db.read(recNo);
    }
    
    public void update(int recNo, String[] data, long lockCookie) throws
            RecordNotFoundException, suncertify.db.SecurityException,
            RemoteException {                
        db.update(recNo, data, lockCookie);                
    }
    
    public void delete(int recNo, long lockCookie) throws
            RecordNotFoundException, suncertify.db.SecurityException,
            RemoteException {        
        db.delete(recNo, lockCookie);
    }
    
    public int[] find(String[] criteria) throws RemoteException {        
        return db.find(criteria);
    }        
        
    public int create(String[] data)
            throws DuplicateKeyException, RemoteException {        
        return db.create(data);
    }
        
    public long lock(int recNo) throws RecordNotFoundException,
            RemoteException {    
        long cookie = db.lock(recNo); // Thread may sleep here
        
        /*
         * If the client died while the current thread was sleeping waiting
         * to obtain a lock in the db.lock() call above then we need to unlock
         * the record we just obtained the lock on.
         */
        if (unreferenced) {
            try {
                db.unlock(recNo, cookie);
            } catch (suncertify.db.SecurityException ex) {
                throw new RuntimeException(ex); // Should never happen.
            }
        } else {
            savedCookie = cookie;
            lockedRecord = recNo;
        }
        
        return cookie;
    }       
    
    public void unlock(int recNo, long cookie) throws RecordNotFoundException,
            suncertify.db.SecurityException, RemoteException {
        db.unlock(recNo, cookie);        
        savedCookie = 0;
        lockedRecord = 0;
    }
    
    public int[] findExact(String[] criteria, int operator) throws 
            RemoteException {        
        return (db.findExact(criteria, operator));        
    }               
         
    /**
     * Called by the RMI runtime sometime after the runtime determines that the
     * connection to the client has been lost.
     * If the client had locked a record in the database when the connection 
     * was lost then it is unlocked.
     * <p>
     * The {@link RemoteBookingData#unreferenced unreferenced} flag is also set
     * so that if a thread on the server side is waiting to obtain a lock on a
     * record when the client dies it will immediately release the lock once it
     * has obtained it.
     */
    public void unreferenced() {         
        unreferenced = true; // Set flag to indicate the client has died
        
        if (lockedRecord > 0) {
            try {                
                unlock(lockedRecord, savedCookie);
            } catch (Exception ex) {
                throw new RuntimeException(ex); // Should never happen
            }
        }
    }    
}
