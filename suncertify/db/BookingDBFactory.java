/*
 * BookingDBFactory.java
 *
 * Created on 06 September 2004, 13:23
 */
package suncertify.db;

import java.util.Properties;
import java.rmi.*;
import java.io.IOException;
import suncertify.Configuration;
import suncertify.server.*;

/**
 * This factory class is used to make instances of classes that implement the
 * {@link BookingDB BookingDB} interface.
 * <p>
 * The constructor takes a {@link suncertify.Configuration Configuration}
 * object as its argument which specifies the mode of operation that is
 * required, either server, standalone client or remote network client.
 * <p>
 * The appropriate implementation of <code>BookingDB</code> will be returned
 * when {@link #getBookingDB getBookingDB} is called based on the specified
 * operating mode.
 *
 * @author Nick Shrine
 */
public class BookingDBFactory {        
    
    /**
     * Specifies the mode of operation and therefore the implemenatation of
     * <code>BookingDB</code> that this class should make.
     */    
    protected final Configuration config;
    
    /**
     * Constructs a new factory object which will create objects that implement
     * the <code>BookingDB</code> interface whose implementation is determined
     * by the specified <code>Configuration</code>.
     *
     * @param config the {@link suncertify.Configuration Configuration} object
     *      that specifies the mode of operation.
     *
     * @see suncertify.Configuration#
     */    
    public BookingDBFactory(Configuration config) {        
        this.config = config;
    }
    
    /**
     * Returns the appropriate implementation of the <code>BookingDB</code>
     * interface based on the mode of operation specified when this factory
     * class was initialised.
     *
     * @throws InvalidDataFileException If the file specified by
     *      {@link #config config} is not a valid data file.
     * @throws IOException If there is a problem accessing the data file.
     * @throws RemoteException If there is a communication problem with the
     *      server.
     * @throws NotBoundException If a remote instance of a
     *      {@link suncertify.server.Server Server} object is not bound in the
     * JNDI namespace.
     *
     * @return an object that implements the <code>BookingDB</code> interface
     *      to be used by clients for accessing the database.
     *
     * @see BookingDB
     */    
    public BookingDB getBookingDB() throws InvalidDataFileException, 
            IOException, RemoteException, NotBoundException {        
        BookingDB db = null;
        
        if(config.equals(Configuration.SERVER)
                || config.equals(Configuration.ALONE)) {
            String filename = config.getFile();
            db = new BookingData(filename);
        } else if(config.equals(Configuration.CLIENT)) {
            String host = config.getHost();
            int port = config.getPort();
            String url = "rmi://" + host + ":" + port + "/" + 
                    Configuration.JNI_NAME;
            Server server = (Server)Naming.lookup(url);
            RemoteBookingDB rdb = server.getBookingDB();
            db = new BookingDataProxy(rdb);
        }
        
        return db;
    }        
}
