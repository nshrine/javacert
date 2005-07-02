/*
 * Bootstrap.java
 *
 * Created on 26 August 2004, 16:05
 */

package suncertify;

import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import javax.swing.JOptionPane;
import suncertify.db.*;
import suncertify.client.ClientFrame;
import suncertify.server.Server;
import suncertify.server.ServerImpl;

/**
 * This class is used to start the application.
 * <p>
 * It parses the command-line arguments and loads the corresponding 
 * configuration either from an existing file or by instantiating a default
 * instance of the {@link Configuration Configuration} class.
 * <p>
 * It then starts a {@link ConfigurationDialog ConfigurationDialog} which 
 * allows GUI editing of the application parameters before the application 
 * (either server or client) is started proper.
 * <p>
 * After the application has been started the configuration used is saved to
 * file. 
 *
 * @author Nick Shrine
 */
public class Bootstrap {
        
    /**
     * The command-line argument for starting the server.
     */
    public static final String SERVER_ARG = "server";
    
    /**
     * The command-line argument for starting the standalone client.
     */
    public static final String ALONE_ARG = "alone";                
    
    /**
     * Main method to start the application.
     *
     * @param args the command-line arguments used to specify mode of
     *          operation.
     */    
    public static void main(String[] args) {        
        Configuration config = null;
        
        /* Determine the configuration from the command-line argument */
        if (args.length > 1) {
            usage();
            System.exit(1);
        } else if (args.length == 0) {
            config = Configuration.CLIENT;
        } else if (args.length == 1) {
            if (args[0].equals(SERVER_ARG)) {
                config = Configuration.SERVER;
            } else if (args[0].equals(ALONE_ARG)) {
                config = Configuration.ALONE;
            } else {
                usage();
                System.exit(1);
            }
        }
                
        /*
         * Load the configuration file and if it is missing display a warning
         * and inform the user that defaults are being used.
         */
        try {
            config.load();
        } catch (IOException ex) {
            Utils.warnBox(null, "Unable to load configuration: " 
                    + ex.getMessage() + ".\n Using defaults");
        }                
        
        /* If the user hit Cancel in the configuration dialog exit */
        if (!editConfiguration(config)) { 
            System.exit(0);
        }
        
        /*
         * Create a new BookingDBFactory and try to obtain a data access
         * object with which to initialise the application.
         */
        BookingDBFactory factory = new BookingDBFactory(config);
        BookingDB db = null;        
        try {
            db = factory.getBookingDB();
        } catch (InvalidDataFileException ex) {
            Utils.errorBox(null, "Invalid data file: " + ex.getMessage());
            System.exit(1);
        } catch (RemoteException ex) {
            Utils.errorBox(null, "Unable to connect to server."
                    + "\n\nDetails:\n" + ex.getMessage()
                    + "\n\nCheck the server host and port number "
                    + "and that the server has been started.");
            System.exit(1);
        } catch (IOException ex) {
            Utils.errorBox(null, "Unable to open database: "
                    + ex.getMessage());
            System.exit(1);
        } catch (NotBoundException ex) {
            Utils.errorBox(null, "Server not started: " + ex.getMessage());
            System.exit(1);
        }
        
        /* Start the application */
        if (config.equals(Configuration.CLIENT)
                || config.equals(Configuration.ALONE)) {            
            startClient(db);            
        } else if (config.equals(Configuration.SERVER)) {
            try {
                startServer(config, db);
            } catch (RemoteException ex) {
                Utils.errorBox(null, "Unable to start server. "
                        + "\n\nDetails:\n" + ex.getMessage());
                System.exit(1);
            } catch (AlreadyBoundException ex) {
                Utils.errorBox(null, "Server already running on port "
                        + config.getPort());
                System.exit(1);
            } 
        }
        
        /* Save the configuration */
        try {
            config.save();
        } catch (IOException ex) {
            Utils.warnBox(null, "Unable to save configuration: "
                    + ex.getMessage());            
        }        
    }        
    
    /**
     * Starts a configuration dialog to edit the application's parameters.
     *
     * @param config the {@link Configuration Configuration} object to edit.
     *
     * @return <code>true</code> if the edits were confirmed by the user or 
     *          <code>false</code> if the user hit Cancel.
     */    
    public static boolean editConfiguration(Configuration config) {
        ConfigurationDialog editConfig = new ConfigurationDialog(config);
        editConfig.setLocationRelativeTo(null);
        editConfig.setVisible(true);
        return editConfig.isConfirmed();
    }
    
    /**
     * Starts the GUI client.
     *
     * @param db the {@link suncertify.db.BookingDB BookingDB} object for the
     *          client to use for data access.
     */    
    public static void startClient(BookingDB db) {    
        ClientFrame frame = new ClientFrame(db);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }        
    
    /**
     * Starts the network server.
     *
     * @param config the {@link Configuration Configuration} object that 
     *          holds the application parameters.
     * @param db the {@link suncertify.db.BookingDB BookingDB} object for the
     *          server to use for data access.
     *
     * @throws RemoteException if there is a network communication problem.
     * @throws AlreadyBoundException if an instance of the server is already
     *          bound in the namespace.
     */    
    public static void startServer(Configuration config, BookingDB db)
            throws RemoteException, AlreadyBoundException {
        int port = config.getPort();
        Registry registry = LocateRegistry.createRegistry(port);       
        Server server = new ServerImpl(db);
        registry.bind(Configuration.JNI_NAME, server);        
        JOptionPane.showMessageDialog(null, "Server started on port "
                + config.getPort());
    }
        
    /**
     * Prints the usage information for command-line invocation.
     */
    protected static void usage() {        
        System.err.println("Usage: Bootstrap "
                + "[" + SERVER_ARG + ", " + ALONE_ARG + "]"
                + "\n Bootstrap:\twith no arguments starts the network client."
                + "\n Bootstrap server:\tstarts the network server."
                + "\n Bootstrap alone:\t starts the standalone client");    
    }
}
