package suncertify.db;

/**
 * Interface for URLyBird application as supplied.
 */
public interface DB {
    
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
    public String[] read(int recNo) throws RecordNotFoundException;

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
    public void update(int recNo, String[] data, long lockCookie) throws
        RecordNotFoundException, SecurityException;

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
    public void delete(int recNo, long lockCookie) throws
            RecordNotFoundException, SecurityException;

    /**
     * Returns an array of record numbers that match the specified
     * criteria. Field n in the database file is described by
     * criteria[n]. A null value in criteria[n] matches any field
     * value. A non-null  value in criteria[n] matches any field
     * value that begins with criteria[n]. (For example, "Fred"
     * matches "Fred" or "Freddy".)
     *
     * @param criteria the criteria to be matched.
     * @return an array of record numbers that match the specified
     *          criteria.
     */
    public int[] find(String[] criteria);

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
    public int create(String[] data) throws DuplicateKeyException;

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
    public long lock(int recNo) throws RecordNotFoundException;

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
    public void unlock(int recNo, long cookie) throws RecordNotFoundException,
            SecurityException;

}