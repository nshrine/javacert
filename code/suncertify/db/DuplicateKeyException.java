/*
 * DuplicateKeyException.java
 *
 * Created on 17 November 2003, 04:59
 */

package suncertify.db;

/**
 * DuplicateKeyException is thrown if an attempt to create a duplicate record
 * is made.
 *
 * @author Nick Shrine
 */
public class DuplicateKeyException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>DuplicateKeyException</code>
     * without detail message.
     */
    public DuplicateKeyException() {
        super();
    }
        
    /**
     * Constructs an instance of <code>DuplicateKeyException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DuplicateKeyException(String msg) {
        super(msg);
    }
}
