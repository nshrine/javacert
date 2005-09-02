/*
 * Utils.java
 *
 * Created on 13 August 2004, 15:40
 */

package suncertify;

import java.util.*;
import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * A set of utility methods for use by all classes.
 *
 * @author Nick Shrine
 */
public class Utils {        
    
    /**
     * Should never be instantiated.
     */
    private Utils() {
        
    }
    
    /**
     * Converts the given <code>Collection</code> of <code>Integer</code>
     * objects to an array of primitive int values.
     *
     * @param c a <code>Collection</code> of <code>Integer</code> objects to
     *      be converted.
     * @return an array of int values.
     */    
    public static int[] toIntArray(Collection c) {  
        int[] result = new int[c.size()];        
        Iterator itr = c.iterator();
        int i = 0;
        
        while (itr.hasNext()) {
            Integer value = (Integer) itr.next(); 
            result[i++] = value.intValue();
        }
        
        return result;
    }
    
    /**
     * Displays an error dialog as a child of the given component, displaying
     * the given message.
     *
     * @param parent the component that this dialog should be a child of.
     * @param message the message to display.
     */    
    public static void errorBox(Component parent, String message) {        
        JOptionPane.showMessageDialog(parent, message, "Error", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Displays an warning dialog as a child of the given component, displaying
     * the given message.
     *
     * @param parent the component that this dialog should be a child of.
     * @param message the message to display.
     */    
    public static void warnBox(Component parent, String message) {        
        JOptionPane.showMessageDialog(parent, message, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Returns a Date object that is earlier than the given Date by the given
     * number of hours.
     *
     * @param date the date to subtract hours from.
     * @param hours the number of hours to substract from the date.
     *
     * @return the date that is the specified number of hours before the given
     *          date.
     */
    public static Date subtractHours(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -hours);
        Date newDate = calendar.getTime();
        return newDate;
    }
}
