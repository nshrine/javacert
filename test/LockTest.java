/*
 * LockTest.java
 * JUnit based test
 *
 * Created on 11 June 2005, 16:05
 */

import junit.framework.*;
import suncertify.db.Data;

/**
 *
 * @author Nick
 */
public class LockTest extends TestCase {
    
    public LockTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LockTest.class);
        return suite;
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    public void testLock() throws Exception {
        final Data db = new Data(
                "H:\\Nick\\java\\cert\\SCDJ2-1.4\\assignment\\db-1x3.db");
        
        final long[] cookie = new long[11];
        for (int j = 0; j < 3; j++) {
            for (int i = 1; i <= 10; i++) {
                final int r = i;
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            cookie[r] = db.lock(r);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println("Obtained lock :" + r);
                    }              
                });
                t.start();
            }
        }
        db.unlock(7, cookie[7]);
    }            
}
