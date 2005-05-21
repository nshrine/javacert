/*
 * SecurityException.java
 *
 * Created on 17 November 2003, 04:58
 */

package suncertify.db;

/**
 *
 * @author Nick Shrine
 */
public class SecurityException extends Exception {
    
    /**
     * Creates a new instance of <code>SecurityException</code> without detail message.
     */
    public SecurityException() {
        super();
    }
    
    
    /**
     * Constructs an instance of <code>SecurityException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public SecurityException(String msg) {
        super(msg);
    }
}
