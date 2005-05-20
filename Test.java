/*
 * Test.java
 *
 * Created on 14 September 2004, 16:38
 */

/**
 *
 * @author  nrs
 */
import java.awt.event.ActionEvent;
import suncertify.Configuration;
import suncertify.db.*;
import suncertify.client.BookingDialog;

public class Test {        
    
    protected final BookingDBFactory factory;
    
    public Test() throws Exception {
        
        Configuration config = Configuration.CLIENT;        
        config.set("client.host", "localhost");
        config.set("client.port", "1099");
            //"/home/staff/nrs/java/cert/SCJD/assignment/src/db-1x3.db");
        factory = new BookingDBFactory(config);        
    }        
    
    protected class Task implements Runnable {                                
                
        public void run() {
            BookingDB db = null;
            try {
                db = factory.getBookingDB();
            } catch (Exception ex) {
                System.err.println(ex);
            }
            
            for(int i=0; i<100; i++) {
                int recNo = 1 + (int)Math.round(Math.random() * 10);
                System.out.print(this + ":Trying to lock " + recNo + "...");
                try {
                    long cookie = db.lock(recNo);
                    System.out.println("locked");                    
                    db.unlock(recNo, cookie);
                    System.out.println(this + ":Unlocked " + recNo);
                } catch(Exception ex) {
                    System.out.println(ex);
                }
            }
        }
    }
    
    public static void main(String[] args) throws Exception {     
        
        Test test = new Test();               
        for(int i=0; i<20; i++) {
            Thread t = new Thread(test.new Task());
            t.start();
        }
    }
}


