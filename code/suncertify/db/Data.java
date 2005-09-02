/*
 * Data.java
 *
 * Created on 17 November 2003, 05:00
 */

package suncertify.db;

import java.io.*;
import java.util.*;
import suncertify.Utils;

/**
 * Database access class for the URLyBird application.
 * <p>
 * This class uses a binary file as its data store and ensures synchronous
 * access to the data file by requiring clients to obtain a lock cookie before
 * they can write to the file, thereby preventing write access by other clients
 * until the record currently being modified is unlocked.
 * <p>
 * This class is threadsafe allowing concurrent access to the data file 
 * by multiple client threads using a single shared instance.
 * <p>
 * If a client tries to lock a record that is already locked by another object
 * the current thread will sleep until the client with the lock releases the
 * lock on the required record.
 * <p>
 * Where methods take a record number as an argument, record numbers start at
 * 1 for the first record as opposed to 0.
 *
 * @author Nick Shrine
 */
public class Data implements DB {

    /**
     * The character encoding to be used.
     */
    public static final String ENCODING = "US-ASCII";

    /**
     * Indicates the record is valid and not deleted.
     */
    public static final byte VALID = (byte) 0x00;

    /**
     * Indicates the record is deleted.
     */
    public static final byte DELETED = (byte) 0xFF;

    /**
     * The value that must be at the start of a valid data file.
     */
    public static final int MAGIC_COOKIE = 0x103;

    /**
     * The database file.
     */
    protected final RandomAccessFile db;

    /**
     * The number of fields per record in the database.
     */
    protected final int fieldCount;

    /**
     * A map of the database schema where the keys are the field names
     * and the values are the field lengths.
     */
    protected final Map schema;

    /**
     * The length in bytes of the database header preceding the data records 
     * that contains the schema information.     
     */
    protected final int headerLength;

    /**
     * The length of a record in bytes.
     */
    protected final int recordLength;

    /**
     * A map containing the list of currently locked records where the key
     * is the record number and the value is the cookie the record is
     * locked with.
     */
    protected final Map lockedRecords;

    /**
     * Object that generates the lock cookies.
     */
    protected final Random cookieGenerator;

    /**
     * The number of records in the database.
     */
    protected int numRecords;

    /**
     * The number of deleted records in the database.
     */
    protected int deletedRecords;

    /**
     * Constructs a data object that controls access to the data file
     * specified by the filename parameter.
     *
     * @param filename the binary file containing the data.
     *
     * @throws FileNotFoundException if the data cannot be read.
     * @throws InvalidDataFileException if the data file is not a valid 
     *          URLyBird data file.
     * @throws IOException if there is an IO error opening the data file.
     */    
    public Data(String filename) throws FileNotFoundException,
            InvalidDataFileException, IOException {
        db = new RandomAccessFile(filename, "rw");

        try {
            int cookieValue = db.readInt();
            if (cookieValue != MAGIC_COOKIE) {
                throw new InvalidDataFileException(filename);
            }
        } catch (IOException ex) {
            throw new InvalidDataFileException(filename);
        }

        fieldCount = db.readShort();
        Map tmpSchema = loadSchema();
        schema = Collections.unmodifiableMap(tmpSchema);
        headerLength = (int) db.getFilePointer();
        recordLength = getRecordLength();
        numRecords = ((int) db.length() - headerLength) / recordLength;
        deletedRecords = countDeletedRecords();
        cookieGenerator = new Random();
        lockedRecords = new HashMap();
    }

    /**
     * Reads a record from the file. Returns an array where each element is a
     * record value.
     *
     * @param recNo the record number of the record to be read.
     *
     * @throws RecordNotFoundException if the record does not exist or there is
     *          an error accessing the database.
     *
     * @return an array where each element is a record value.
     */    
    public synchronized String[] read(int recNo) throws
            RecordNotFoundException {
        String[] record = new String[fieldCount];
        int i = 0;

        try {
            db.seek(findUndeletedRecord(recNo));
            Iterator itr = schema.values().iterator();
            while (itr.hasNext()) {
                int fieldLength = ((Integer) itr.next()).intValue();
                byte[] fieldBytes = new byte[fieldLength];
                db.readFully(fieldBytes);
                record[i] = new String(fieldBytes, ENCODING);
                
                /*
                 * If the String is null terminated, truncate it to the 
                 * position of the terminator.
                 */
                int indexOfTerminator = record[i].indexOf('\0');
                if (indexOfTerminator > -1) {
                    record[i] = record[i].substring(0, indexOfTerminator);
                }
                i++;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return record;
    }

    /**
     * Modifies the fields of a record. The new value for field n 
     * appears in data[n]. Throws SecurityException
     * if the record is locked with a cookie other than lockCookie.
     *
     * @param recNo the record number of the record to be modified.
     * @param data the modified data.
     * @param lockCookie the cookie that the record was locked with.
     *
     * @throws RecordNotFoundException if the record does not exist or there is
     *          an error accessing the database.
     * @throws SecurityException if the record is locked with a cookie other
     *          than lockCookie.
     */
    public synchronized void update(int recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        try {
            db.seek(findUndeletedRecord(recNo));
            checkLock(recNo, lockCookie);
            write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Deletes a record, making the record number and associated disk
     * storage available for reuse.
     * Throws SecurityException if the record is locked with a cookie
     * other than lockCookie.
     *
     * @param recNo the record number of the record to be deleted.
     * @param lockCookie the cookie that the record was locked with.
     *
     * @throws RecordNotFoundException if the record does not exist or there is
     *          an error reading the database.
     * @throws SecurityException if the record is locked with a cookie
     *          other than lockCookie.
     */
    public synchronized void delete(int recNo, long lockCookie) throws
            RecordNotFoundException, SecurityException {
        checkLock(recNo, lockCookie);

        try {
            db.seek(findRecord(recNo));
            db.writeByte(DELETED);
            deletedRecords++;
            lockedRecords.remove(new Integer(recNo));
        } catch (IOException ex) {                        
            throw new RuntimeException(ex); 
        }
    }

    /**
     * Returns an array of record numbers that match the specified
     * criteria. Field n in the database file is described by
     * criteria[n]. A null value in criteria[n] matches any field
     * value. A non-null  value in criteria[n] matches any field
     * value that begins with criteria[n]. (For example, "Fred"
     * matches "Fred" or "Freddy".)
     *
     * @param criteria the criteria to be matched.
     *
     * @return an array of record numbers that match the specified
     *          criteria.
     */
    public synchronized int[] find(String[] criteria) {
        ArrayList results = new ArrayList(); 

        recordloop: // Loop over records in the data file
            for (int recNo = 1; recNo <= numRecords; recNo++) {
                String[] data = null;
                
                try {
                    data = read(recNo);
                } catch (RecordNotFoundException ex) {
                    
                    /* Record is deleted so continue to the next one */
                    continue recordloop;
                }
                
                for (int i = 0; i < data.length; i++) {
                    
                    /* On first non-match proceed to next record */
                    if ((criteria[i] != null)
                            && !data[i].startsWith(criteria[i])) {
                        continue recordloop;
                    }
                }
                
                /* All criteria must have matched */
                results.add(new Integer(recNo)); 
        }
        
        return Utils.toIntArray(results);
    }

    /**
     * Creates a new record in the database (possibly reusing a
     * deleted entry). Inserts the given data, and returns the record
     * number of the new record.
     *
     * @param data the data for the new record.
     *
     * @throws DuplicateKeyException unimplemented.
     *
     * @return the record number of the new record.
     */
    public synchronized int create(String[] data) throws
            DuplicateKeyException {
        int recNo = 1;

        try {
            
            /* Find the first deleted record */
            while (recNo <= numRecords) {
                db.seek(findRecord(recNo));
                if (db.readByte() == DELETED) {
                    break;
                }
                recNo++;
            }
            
            /* 
             * If none deleted found, recNo will be numRecords + 1 and 
             * therefore the new record will be appended after the last
             * existing record.
             */
            
            db.seek(moveTo(recNo));
            db.writeByte(VALID); //Set the deleted flag to not deleted.
            write(data);

            if (recNo == numRecords) {
                numRecords++; //We have just appended a record
            }                   
        } catch (RecordNotFoundException recNotFound) {
            throw new RuntimeException(recNotFound); //Shouldn't happen
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return recNo;
    }

    /**
     * Locks a record so that it can only be updated or deleted by this client.
     * Returned value is a cookie that must be used when the record is unlocked,
     * updated, or deleted. If the specified record is already locked by a 
     * different client, the current thread gives up the CPU and consumes no 
     * CPU cycles until the record is unlocked.
     *
     * @param recNo the record number of the record to be locked.
     *
     * @throws RecordNotFoundException if the record does not exists or there is
     *          an error accessing the database.
     *
     * @return a cookie that must be used when the record is unlocked,
     *          updated, or deleted.
     */
    public synchronized long lock(int recNo) throws RecordNotFoundException {
        
        /* First check the record exists and is not deleted */
        findUndeletedRecord(recNo);

        /* 
         * If record is already locked wait until we get notification of
         * a lock being released and then check again.
         */
        while (lockedRecords.containsKey(new Integer(recNo))) {
            try {
                wait();                
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex); // Should never happen
            }
        }

        long cookie = cookieGenerator.nextLong();
        lockedRecords.put(new Integer(recNo), new Long(cookie));

        return cookie;
    }

    /**
     * Releases the lock on a record. Cookie must be the cookie
     * returned when the record was locked; otherwise throws SecurityException.
     *
     * @param recNo the record number of the record to be unlocked.
     * @param cookie the cookie that the record was locked with.
     *
     * @throws RecordNotFoundException if the record does not exist or there is 
     *          an error accessing the database.
     * @throws SecurityException if the record is locked with a cookie
     *          other than cookie.
     */
    public synchronized void unlock(int recNo, long cookie) throws
            RecordNotFoundException, SecurityException {
        findUndeletedRecord(recNo); //check record exists first
        checkLock(recNo, cookie); //check it's not locked by someone else
        lockedRecords.remove(new Integer(recNo)); //remove the lock
        notifyAll(); //notify other threads waiting to lock the record
    }

    /**
     * Returns the number of records in the data file,
     * not including records marked as deleted.
     *
     * @return the number of records currently in the data file.
     */    
    public final int getNumRecords() {
        return (numRecords - deletedRecords);
    }

    /**
     * Returns a file pointer to the start of the requested record within
     * the data file, even if the record does not exist so that the file
     * pointer can be positioned for writing new records.
     *
     * @param recNo the record number of the record to be accessed.
     *          
     * @return the file pointer to the location of the start of the requested
     *          record within the data file.
     */        
    protected final synchronized long moveTo(int recNo) {
        long offset = headerLength + ((recNo - 1) * recordLength);
        long fileptr = 0;

        try {
            db.seek(offset);
            fileptr = db.getFilePointer();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return fileptr;
    }
    
    /**
     * Returns a file pointer to the start of the requested record within
     * the data file if the record exists, otherwise a
     * {@link RecordNotFoundException RecordNotFoundException} is thrown.
     * <p>
     * This method also finds deleted records.
     *
     * @param recNo the record number of the record to be found.
     *
     * @throws RecordNotFoundException if the record is not in the database.
     *
     * @return the file pointer to the location of the start of the requested
     *          record within the data file.
     */    
    protected synchronized long findRecord(int recNo) throws
            RecordNotFoundException {
        if (recNo > numRecords) {
            throw new RecordNotFoundException("Record " + recNo
                    + " does not exist.");
        }
        
        long fileptr = moveTo(recNo);
        return fileptr;
    }

    /**
     * Returns the file pointer with which to locate the start of the
     * requested record within the data file.
     * This method only finds records that are not marked as deleted.
     *
     * @param recNo the record number of the record to be found.
     *
     * @throws RecordNotFoundException if the record is not in the database
     *          or is deleted.
     *
     * @return the file pointer to the location of the start of the requested
     *          record within the data file.
     */    
    protected synchronized long findUndeletedRecord(int recNo) throws
            RecordNotFoundException {        
        long fileptr = 0;

        try {
            db.seek(findRecord(recNo));
            if (db.readByte() == DELETED) {
                throw new RecordNotFoundException("Record " + recNo
                        + " is deleted");
            }
            fileptr = db.getFilePointer();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return fileptr;
    }

    /**
     * Checks if the record specified by the record number is locked with
     * the given lock cookie and throws a SecurityException if it isn't.
     *
     * @param recNo the record number of the record to check the lock 
     *          credentials.
     * @param cookie the lock cookie to compare with the stored cookie.
     *
     * @throws SecurityException if the record is locked with a cookie other
     *          than the one specified.
     */    
    protected synchronized void checkLock(int recNo, long cookie) throws
            SecurityException {
        Integer key = new Integer(recNo);

        if (!lockedRecords.containsKey(key)) {
            throw new SecurityException("The record has not been locked");
        }

        Long value = (Long) lockedRecords.get(key);
        long storedCookie = value.longValue();

        if (cookie != storedCookie) {
            throw new SecurityException(
                    "Caller does not own the lock on this record");
        }
    }
    
    /**
     * Writes the data given as the argument to the current location in
     * the data file.
     * The file pointer must be positioned just after the deleted flag
     * of the record in question.
     *
     * @param data the data to be written to the record.
     *
     * @throws IOException if there is an error writing to the file.
     */    
    protected final synchronized void write(final String[] data) throws
            IOException {
        int i = 0;
        Iterator itr = schema.values().iterator();

        while (itr.hasNext()) {
            Integer value = (Integer) itr.next();
            int fieldLength = value.intValue();
            byte[] fieldBytes = new byte[fieldLength];
            byte[] dataBytes = data[i++].getBytes(ENCODING);
            for (int j = 0; j < dataBytes.length; j++) {
                fieldBytes[j] = dataBytes[j];
            }
            db.write(fieldBytes);
        }
    }

    /**
     * Reads the data schema from the file header.
     *
     * @throws IOException if there is an error reading the file.
     *
     * @return a Map where the keys are the field names and the values are the
     *          field lengths in bytes.
     */    
    private Map loadSchema() throws IOException {
        Map map = new LinkedHashMap();

        for (int i = 0; i < fieldCount; i++) {
            int lengthOfFieldName = db.readByte();
            byte[] fieldNameBytes = new byte[lengthOfFieldName];
            db.readFully(fieldNameBytes);
            String fieldName = new String(fieldNameBytes, ENCODING);
            int fieldLength = db.readByte();
            map.put(fieldName, new Integer(fieldLength));
        }

        return map;
    }

    /**
     * Returns the length of records in the data file.
     *
     * @return the length of records in the data file in bytes.
     */    
    private int getRecordLength() {
        int recordLength = 1; //Each record starts with 1 byte flag
        Iterator itr = schema.values().iterator();

        while (itr.hasNext()) {
            Integer value = (Integer) itr.next();
            recordLength += value.intValue();
        }

        return recordLength;
    }

    /**
     * Returns the number of deleted records in the data file.
     *
     * @throws IOException if there is an error reading the file.
     *
     * @return the number of deleted records in the data file.
     */    
    private int countDeletedRecords() throws IOException {
        int numDeleted = 0;

        for (int recNo = 1; recNo <= numRecords; recNo++) {
            try {
                db.seek(findRecord(recNo));
                if (db.readByte() == DELETED) {
                    numDeleted++;
                }
            } catch (RecordNotFoundException ex) {
                break;
            }
        }

        return numDeleted;
    }
}
