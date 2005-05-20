/*
 * Bootstrap.java
 *
 * Created on 26 August 2004, 16:05
 */
package suncertify;

import java.rmi.*;
import java.rmi.registry.*;
import java.util.Properties;
import java.io.*;
import javax.swing.JOptionPane;
import suncertify.db.*;
import suncertify.client.ClientFrame;
import suncertify.server.Server;
import suncertify.server.ServerImpl;

/**
 *
 * @author Nick Shrine
 */
public class Bootstrap {
                    
    public static final String SERVER_ARG = "server";
    public static final String ALONE_ARG = "alone";                
    
    /**
     *
     * @param args
     */    
    public static void main(String[] args) {        
        
        /* For debugging */
//        System.setProperty("java.rmi.dgc.leaseValue", "5000");
        
        Configuration config = null;
        
        if (args.length > 1) {
            usage();
            System.exit(1);
        } else if (args.length == 0) {
            config = Configuration.CLIENT;
        } else if(args.length == 1) {
            if (args[0].equals(SERVER_ARG)) {
                config = Configuration.SERVER;
            } else if (args[0].equals(ALONE_ARG)) {
                config = Configuration.ALONE;
            } else {
                usage();
                System.exit(1);
            }
        }
                
        try {
            config.load();
        } catch (IOException ex) {
            Utils.warnBox(null, "Unable to load configuration: " + 
                    ex.getMessage() + ".\n Using defaults");
        }                
        
        if(!editConfiguration(config)) { 
            System.exit(0);
        }
        
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
                Utils.errorBox(null, "Server already running on port " +
                        config.getPort());
                System.exit(1);
            } 
        }
        
        try {
            config.save();
        } catch (IOException ex) {
            Utils.warnBox(null, "Unable to save configuration: " + 
                    ex.getMessage());            
        }        
    }        
    
    /**
     *
     * @param config
     * @return
     */    
    public static boolean editConfiguration(Configuration config) {
        ConfigurationDialog editConfig = new ConfigurationDialog(config);
        editConfig.setLocationRelativeTo(null);
        editConfig.setVisible(true);
        return editConfig.isConfirmed();
    }
    
    /**
     *
     * @param db
     */    
    public static void startClient(BookingDB db) {    
        ClientFrame frame = new ClientFrame(db);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }        
    
    /**
     *
     * @param config
     * @param db
     * @throws RemoteException
     * @throws AlreadyBoundException
     */    
    public static void startServer(Configuration config, BookingDB db)
        throws RemoteException, AlreadyBoundException {
        int port = config.getPort();
        Registry registry = LocateRegistry.createRegistry(port);       
        Server server = new ServerImpl(db);
        registry.bind(Configuration.JNI_NAME, server);        
        JOptionPane.showMessageDialog(null, "Server started on port " +
            config.getPort());
//        ServerControlFrame viewLocks = new ServerControlFrame((BookingData)db);
//        viewLocks.setVisible(true);
    }
        
    protected static void usage() {        
        System.err.println("Usage: Bootstrap "
                + "[" + SERVER_ARG + ", " + ALONE_ARG + "]"
                + "\n Bootstrap:\twith no arguments starts the network client."
                + "\n Bootstrap server:\tstarts the network server."
                + "\n Bootstrap alone:\t starts the standalone client");    
    }
}
