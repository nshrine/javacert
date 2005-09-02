/*
 * TestWrite.java
 *
 * Created on 25 April 2004, 13:36
 */

import suncertify.db.*;
/**
 *
 * @author  Nick
 */
public class TestWrite {
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String[] entry = {"MyHotel", "Bigham", "23", "Y", "$34.34", "2005/09/02", ""};
        
        try {
            Data data = new Data(args[0]);        
            System.out.println(data.create(entry));
        } catch(Exception ex) {    
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
