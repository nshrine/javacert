/*
 * InvalidDataFileException.java
 *
 * Created on 24 October 2004, 15:32
 */

package suncertify.db;

/**
 * This exception is thrown if the data file being read by a <code>Data</code>
 * object does not have the correct format.
 *
 * @author Nick Shrine
 *
 * @see Data
 */
public class InvalidDataFileException extends Exception {
    
    /**
     * Creates a new instance of <code>InvalidDataFileException</code> without
     * detail message.
     */
    public InvalidDataFileException() {
        super();
    }
        
    /**
     * Constructs an instance of <code>InvalidDataFileException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidDataFileException(String msg) {
        super(msg);
    }
}
