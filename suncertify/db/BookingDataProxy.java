/*
 * BookingDataProxy.java
 *
 * Created on 01 September 2004, 14:35
 */

package suncertify.db;

import java.rmi.RemoteException;
import suncertify.db.*;
import suncertify.server.RemoteBookingDB;

/**
 *
 * @author  nrs
 */
public class BookingDataProxy implements BookingDB {
    
    protected final RemoteBookingDB db;
    
    /** Creates a new instance of BookingDataProxy */
    BookingDataProxy(RemoteBookingDB db) {        
        this.db = db;
    }
    
    public int create(String[] data) throws DuplicateKeyException {        
        int result;
        
        try {
            result = db.create(data);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    public void delete(int recNo, long lockCookie)
            throws RecordNotFoundException, suncertify.db.SecurityException {        
        try {
            db.delete(recNo, lockCookie);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public int[] find(String[] criteria) {        
        int[] result;
        
        try {
            result = db.find(criteria);
        } catch(RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    public int[] findExact(String[] criteria, int operator) {        
        int[] result;
        
        try {
            result = db.findExact(criteria, operator);
        } catch(RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    public long lock(int recNo) throws RecordNotFoundException {        
        long result;
        
        try {
            result = db.lock(recNo);
        } catch(RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    public String[] read(int recNo) throws RecordNotFoundException {        
        String[] result = null;
        
        try {
            result = db.read(recNo);
        } catch(RemoteException ex) {
            throw new RuntimeException(ex);
        }
        
        return result;
    }
    
    public void unlock(int recNo, long cookie)
            throws RecordNotFoundException, suncertify.db.SecurityException {        
        try {
            db.unlock(recNo, cookie);
        } catch(RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void update(int recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, suncertify.db.SecurityException {         
        try {
            db.update(recNo, data, lockCookie);
        } catch(RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }            
}
