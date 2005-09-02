/*
 * RecordNotFoundException.java
 *
 * Created on 17 November 2003, 04:57
 */

package suncertify.db;

/**
 * This exception is thrown if the Record requested does not exist or is
 * deleted.
 *
 * @author Nick Shrine
 */
public class RecordNotFoundException extends Exception {
    
    /**
     * Creates a new instance of <code>RecordNotFoundException</code> without
     * detail message.
     */
    public RecordNotFoundException() { 
        super();
    }
        
    /**
     * Constructs an instance of <code>RecordNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public RecordNotFoundException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>RecordNotFoundException</code> with the
     * specified <code>Throwable</code> object as the cause.
     *
     * @param cause <code>Throwable</code> object that specifies the cause of
     *          this exception.
     */    
    public RecordNotFoundException(Throwable cause) {
        super(cause);
    }
}
