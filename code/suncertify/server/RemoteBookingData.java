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
    
    /**
     * Reads a record from the file. Returns an array where each element is a
     * record value.
     *
     * @param recNo the record number of the record to be read.
     *
     * @throws RecordNotFoundException if the record does not exist or there is
     *          an error accessing the database.
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     *
     * @return an array where each element is a record value.
     */    
    public String[] read(int recNo) throws RecordNotFoundException,
            RemoteException {         
        return db.read(recNo);
    }
    
    /**
     * Modifies the fields of a record. The new value for field n 
     * appears in data[n]. Throws SecurityException
     * if the record is locked with a cookie other than lockCookie.
     *
     * @param recNo the record number of the record to be modified.
     * @param data the modified data.
     * @param lockCookie the cookie that the record was locked with.
     *
     * @throws RecordNotFoundException if the record does not exist or there is
     *          an error accessing the database.
     * @throws suncertify.db.SecurityException if the record is locked with a
     *          cookie other than lockCookie.
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     */
    public void update(int recNo, String[] data, long lockCookie) throws
            RecordNotFoundException, suncertify.db.SecurityException,
            RemoteException {                
        db.update(recNo, data, lockCookie);                
    }
    
    /**
     * Deletes a record, making the record number and associated disk
     * storage available for reuse.
     * Throws SecurityException if the record is locked with a cookie
     * other than lockCookie.
     *
     * @param recNo the record number of the record to be deleted.
     * @param lockCookie the cookie that the record was locked with.
     *
     * @throws RecordNotFoundException if the record does not exist or there is
     *          an error reading the database.
     * @throws suncertify.db.SecurityException if the record is locked with a
     *          cookie other than lockCookie.
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     */
    public void delete(int recNo, long lockCookie) throws
            RecordNotFoundException, suncertify.db.SecurityException,
            RemoteException {        
        db.delete(recNo, lockCookie);
    }
    
    /**
     * Returns an array of record numbers that match the specified
     * criteria. Field n in the database file is described by
     * criteria[n]. A null value in criteria[n] matches any field
     * value. A non-null  value in criteria[n] matches any field
     * value that begins with criteria[n]. (For example, "Fred"
     * matches "Fred" or "Freddy".)
     *
     * @param criteria the criteria to be matched.
     *    
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     *
     * @return an array of record numbers that match the specified
     *          criteria.     
     */
    public int[] find(String[] criteria) throws RemoteException {        
        return db.find(criteria);
    }        
       
    /**
     * Creates a new record in the database (possibly reusing a
     * deleted entry). Inserts the given data, and returns the record
     * number of the new record.
     *
     * @param data the data for the new record.
     *
     * @throws DuplicateKeyException unimplemented.
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     *
     * @return the record number of the new record.
     */
    public int create(String[] data)
            throws DuplicateKeyException, RemoteException {        
        return db.create(data);
    }
       
    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * Returned value is a cookie that must be used when the record is unlocked,
     * updated, or deleted. If the specified record is already locked by a 
     * different client, the current thread gives up the CPU and consumes no 
     * CPU cycles until the record is unlocked.
     *
     * @param recNo the record number of the record to be locked.
     *
     * @throws RecordNotFoundException if the record does not exists or there 
     *          is an error accessing the database.
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     *
     * @return a cookie that must be used when the record is unlocked,
     *          updated, or deleted.
     */
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
    
    /**
     * Releases the lock on a record. Cookie must be the cookie
     * returned when the record was locked; otherwise throws SecurityException.
     *
     * @param recNo the record number of the record to be unlocked.
     * @param cookie the cookie that the record was locked with.
     *
     * @throws RecordNotFoundException if the record does not exist or there is 
     *          an error accessing the database.
     * @throws suncertify.db.SecurityException if the record is locked with a
     *          cookie other than cookie.
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     */
    public void unlock(int recNo, long cookie) throws RecordNotFoundException,
            suncertify.db.SecurityException, RemoteException {
        db.unlock(recNo, cookie);        
        savedCookie = 0;
        lockedRecord = 0;
    }
    
    /**
     * Returns records that exactly match the specified criteria.
     * An operator type must be specified to determine the type of matching
     * to be done.
     *
     * @param criteria the criteria to be matched.
     * @param operator the type of match to be performed, either 
     * {@link suncertify.db.BookingDB#SEARCH_TYPE_AND AND} or 
     * {@link suncertify.db.BookingDB#SEARCH_TYPE_OR OR}.
     *
     * @throws RemoteException if there is a communication problem between the
     *          server and client.
     *
     * @return an array of record numbers that match the specified criteria.
     */ 
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
