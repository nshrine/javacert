/*
 * SecurityException.java
 *
 * Created on 17 November 2003, 04:58
 */

package suncertify.db;

/**
 * SecurityException is thrown if an incorrect lock cookie is supplied when
 * calling a method that requires a record to be locked first.
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
