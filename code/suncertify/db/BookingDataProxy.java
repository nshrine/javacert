/*
 * BookingDataProxy.java
 *
 * Created on 01 September 2004, 14:35
 */

package suncertify.db;

import java.rmi.RemoteException;
import suncertify.server.RemoteBookingDB;

/**
 * This class provides a proxy so that classes can access data across the 
 * network transparently as if they were using a non networked data access
 * object that implements the {@link BookingDB BookingDB} interaface.
 * <p>
 * Instances of this class should be obtained using {@link BookingDBFactory
 * BookingDBFactory}.
 *
 * @author Nick Shrine
 */
public class BookingDataProxy implements BookingDB {
    
    /**
     * {@link suncertify.server.RemoteBookingDB RemoteBookingDB} object that
     * this class provides a proxy for.
     */
    protected final RemoteBookingDB db;
    
    /** 
     * Creates a new instance of BookingDataProxy using the supplied 
     * {@link suncertify.server.RemoteBookingDB RemoteBookingDB} object for
     * data access.
     *  
     * @param db {@link suncertify.server.RemoteBookingDB RemoteBookingDB}
     * object to use for data access.
     */
    BookingDataProxy(RemoteBookingDB db) {        
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
     *
     * @return an array where each element is a record value.
     */  
    public String[] read(int recNo) throws RecordNotFoundException {        
        String[] result = null;
        
        try {
            result = db.read(recNo);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
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
     */
    public void update(int recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, suncertify.db.SecurityException {
        try {
            db.update(recNo, data, lockCookie);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
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
     */
    public void delete(int recNo, long lockCookie)
            throws RecordNotFoundException, suncertify.db.SecurityException {
        try {
            db.delete(recNo, lockCookie);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
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
     * @return an array of record numbers that match the specified
     *          criteria.
     */
    public int[] find(String[] criteria) {        
        int[] result;
        
        try {
            result = db.find(criteria);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    /**
     * Creates a new record in the database (possibly reusing a
     * deleted entry). Inserts the given data, and returns the record
     * number of the new record.
     *
     * @param data the data for the new record.
     *
     * @throws DuplicateKeyException unimplemented.
     *
     * @return the record number of the new record.
     */
    public int create(String[] data) throws DuplicateKeyException {        
        int result;
        
        try {
            result = db.create(data);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
            
    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * Returned value is a cookie that must be used when the record is
     * unlocked, updated, or deleted. If the specified record is already locked
     * by a different client, the current thread gives up the CPU and consumes
     * no CPU cycles until the record is unlocked.
     *
     * @param recNo the record number of the record to be locked.
     *
     * @throws RecordNotFoundException if the record does not exists or there
     *          is an error accessing the database.
     *
     * @return a cookie that must be used when the record is unlocked,
     *          updated, or deleted.
     */
    public long lock(int recNo) throws RecordNotFoundException {        
        long result;
        
        try {
            result = db.lock(recNo);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
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
     */
    public void unlock(int recNo, long cookie)
            throws RecordNotFoundException, suncertify.db.SecurityException {
        try {
            db.unlock(recNo, cookie);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Returns records that exactly match the specified criteria.
     * An operator type must be specified to determine the type of matching
     * to be done.
     *
     * @param criteria the criteria to be matched.
     * @param operator the type of match to be performed, either 
     * {@link BookingDB#SEARCH_TYPE_AND AND} or 
     * {@link BookingDB#SEARCH_TYPE_OR OR}.
     *
     * @return an array of record numbers that match the specified criteria.
     */    
    public int[] findExact(String[] criteria, int operator) {        
        int[] result;
        
        try {
            result = db.findExact(criteria, operator);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }                    
}
