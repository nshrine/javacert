/*
 * Configuration.java
 *
 * Created on 06 September 2004, 19:31
 */

package suncertify;

import java.util.Properties;
import java.io.*;

/**
 * This is a wrapper class round a <code>Properties</code> object that holds
 * configuration parameters for the application.
 * <p>
 * Instances hold a description of the configuration and descriptions for
 * the key-value pairs of the enclosed <code>Properties</code> object.
 * <p>
 * In this way a GUI tool for editing the configuration can determine the
 * type of value and dynamically display field prompts based on the
 * retrieved descriptions. 
 *
 * @author Nick Shrine
 */
public class Configuration {        
            
    /**
     * The properties key for the database file.
     */
    public static final String FILE_KEY = "file";
    
    /**
     * The properties key for the server host.
     */
    public static final String HOST_KEY = "host";    
    
    /**
     * The properties key for the server port.
     */
    public static final String PORT_KEY = "port";
        
    /**
     * The description text for the file field.
     */
    public static final String FILE_KEY_DESC = "Database File";
    
    /**
     * The description text for the host field.
     */
    public static final String HOST_KEY_DESC = "Server Host";    
    
    /**
     * The description text for the port field.
     */
    public static final String PORT_KEY_DESC = "Server Port";
    
    /**
     * The default database file name.
     */
    public static final String DEFAULT_FILE = "db-1x3.db";
    
    /**
     * The default server port.
     */
    public static final String DEFAULT_PORT = "1099";
    
    /**
     * The minimum port number.
     */
    public static final int MIN_PORT = 1;
    
    /**
     * The maximum port number.
     */
    public static final int MAX_PORT = 65535;
    
    /**
     * The JNI name for the server to be bound to.
     */
    public static final String JNI_NAME = "urlybird";
    
    /**
     * The file name of the properties file.
     */
    public static final String CONFIG_FILE = "suncertify.properties";
    
    /**
     * The title to use for the properties file.
     */
    public static final String HEADER = "URLyBird Configuration";    
    
    /**
     * The configuration for Server mode.
     */
    public static final Configuration SERVER;
    
    /**
     * The configuration for remote client mode.
     */
    public static final Configuration CLIENT;
    
    /**
     * The configuration for stand alone mode.
     */
    public static final Configuration ALONE;    
    
    /**
     * Initialise the server, client at stand alone
     * <code>Configuration</code> objects.
     */
    static {                
        String[] serverKeys = { FILE_KEY, PORT_KEY };
        String[] clientKeys = { HOST_KEY, PORT_KEY };
        String[] aloneKeys = { FILE_KEY };
        
        SERVER = new Configuration("server", serverKeys, "Server");
        CLIENT = new Configuration("client", clientKeys, "Network Client");
        ALONE = new Configuration("alone", aloneKeys, "Standalone Client");
    }
        
    /**
     * The prefix for the property keys of this configuration.
     */
    protected final String prefix;
    
    /**
     * The set of property keys used by this configuration.
     */
    protected final String[] keys;    
    
    /**
     * The description of the operating mode specified by this configuration.
     */
    protected final String description;
    
    /**
     * The <code>Properties</code> object used for storing the configuration
     * parameters.
     */
    protected final Properties properties;
    
    /**
     * Creates a new <code>Configuration</code> specified by a prefix for
     * property keys to be saved with, a set of keys specifying what sort of
     * values are to be stored and a description indicating the mode of
     * operation that this <code>Configuration</code> specifies.
     *
     * @param prefix the prefix to be used for property keys of this
     *      <code>Configuration</code>.
     * @param keys an array of property keys specifying the sort of values 
     *      this <code>Configuration</code> stores i.e. an array containing
     *      values chosen from {@link #FILE_KEY FILE_KEY},
     *      {@link #HOST_KEY HOST_KEY}, {@link #PORT_KEY PORT_KEY} or any
     *      custom keys that may be required.
     * @param description text that describes the mode of operation this 
     *      <code>Configuration</code> specifies e.g. "Server" or "Client".
     */    
    protected Configuration(String prefix, String[] keys, String description) {
        this.prefix = prefix;
        this.keys = keys;
        this.description = description;
        properties = new Properties();
        properties.put(prefix + "." + FILE_KEY, DEFAULT_FILE);
        properties.put(prefix + "." + PORT_KEY, DEFAULT_PORT);
    }    
    
    /**
     * Returns an array of the property keys that are used by this 
     * <code>Configuration</code>.
     *
     * @return all property keys.
     */    
    public String[] getKeys() {        
        String[] keynames = new String[keys.length];
        
        for (int i = 0; i < keys.length; i++) {
            keynames[i] = prefix + "." + keys[i];
        }
        
        return keynames;
    }        
    
    /**
     * Returns a description of the mode of operation this
     *       <code>Configuration</code> specifies.
     *
     * @return a description of the mode of operation.
     */    
    public String getDescription() {
        return description;
    }
        
    /**
     * Returns the property value associated with the given key.
     *
     * @param key the key of the property required.
     *
     * @return the value associated with the key.
     */    
    public String get(String key) {        
        String value = (String) properties.get(key);        
        return value;
    }
    
    /**
     * Returns the name of the database file specified in this
            <code>Configuration</code>.
     *
     * @return the name of the database file.
     */    
    public String getFile() {        
        String key = prefix + "." + FILE_KEY;
        String value = (String) properties.get(key);        
        return value;        
    }
    
    /**
     * Returns the name of the server host specified in this
     *       <code>Configuration</code>.
     *
     * @return the server host.
     */    
    public String getHost() {        
        String key = prefix + "." + HOST_KEY;
        String value = (String) properties.get(key);        
        return value;        
    }
    
    /**
     * Returns the server port specified in this <code>Configuration</code>.
     *
     * @return the server port.
     */    
    public int getPort() {        
        String key = prefix + "." + PORT_KEY;
        String value = (String) properties.get(key);        
        return Integer.parseInt(value);
    }
    
    /**
     * Sets the property value specified by the given key.
     *
     * @param key the key of the property to set.
     *
     * @param value the value to set the property to.
     */    
    public void set(String key, String value) {
        
        /* If it is the port that is being set, check that it is in range */
        if (getKeyType(key).equals(PORT_KEY)) {
            try {
                int port = Integer.parseInt(value);
                if ((port < MIN_PORT) || (port > MAX_PORT)) {
                    throw new IllegalArgumentException("Port number " 
                            + port + " out of range.");
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(
                        "Port number must be an integer");
            }
        }
        
        properties.put(key, value);
    }
    
    /**
     * Loads the properties for this <code>Configuration</code> from the
     * appropriate properties file.
     *
     * @throws IOException if there is an error reading the file.
     */    
    public void load() throws IOException {        
        FileInputStream in = new FileInputStream(CONFIG_FILE);
        properties.load(in);
    }
    
    /**
     * Saves the properties for this <code>Configuration</code> to the
     * appropriate properties file.
     *
     * @throws IOException if there is an error writing the file.
     */    
    public void save() throws IOException {        
        FileOutputStream out = new FileOutputStream(CONFIG_FILE);
        properties.store(out, HEADER);
    }
    
    /**
     * Returns a description of the value specified by the given key
     * suitable for using as a field label in a GUI properties editor.
     *
     * @param key specifies the property for which a description is required.
     *
     * @return the description of the property.
     */    
    public static String getKeyDescription(String key) {        
        String desc = null;
        
        if (key.endsWith(FILE_KEY)) {
            desc = FILE_KEY_DESC;
        } else if (key.endsWith(HOST_KEY)) {
            desc = HOST_KEY_DESC;
        } else if (key.endsWith(PORT_KEY)) {
            desc = PORT_KEY_DESC;            
        }
        
        return desc;
    }
        
    /**
     * Returns the last part of the key that indicates what type of value it
     * is pointing to. 
     *
     * @param key they key string that we want to determine the type of.
     *
     * @return the type of this key, built-in key types are {@link #FILE_KEY
     * FILE_KEY}, {@link #HOST_KEY HOST_KEY} and {@link #PORT_KEY PORT_KEY}.
     */    
    public static String getKeyType(String key) {        
        String type = key.substring(key.indexOf('.') + 1);
        return type;
    }
}
