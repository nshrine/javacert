/*
 * SecurityException.java
 *
 * Created on 17 November 2003, 04:58
 */

package suncertify.db;

/**
 * SecurityException is thrown if the user who does not own the lock cookie
 * for a locked record attempts to access it.
 *
 * @author Nick Shrine
 *
 * @see Data
 */
public class SecurityException extends Exception {
    
    /**
     * Creates a new instance of <code>SecurityException</code> without
     * detail message.
     */
    public SecurityException() {
        super();
    }
        
    /**
     * Constructs an instance of <code>SecurityException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SecurityException(String msg) {
        super(msg);
    }
}
