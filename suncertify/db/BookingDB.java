/*
 * BookingDB.java
 *
 * Created on 26 August 2004, 14:45
 */

package suncertify.db;

/**
 * An extended version of the DB interface to provide specific functionality
 * required for the URLyBird Application.
 *
 * @author Nick Shrine
 */
public interface BookingDB extends DB {
    
    /*
     * Search type whereby all criteria must match.
     */
    public static final int SEARCH_TYPE_AND = 0;
    
    /*
     * Search type whereby any criteria matching will return a match.
     */
    public static final int SEARCH_TYPE_OR = 1;    
    
    /**
     * Returns records that exactly match the specified criteria.
     * An operator type must be specified to determine the type of matching
     * to be done.
     *
     * @param criteria the criteria to be matched.
     * @param operator the type of match to be performed, either 
     * {@link BookingDB#SEARCH_TYPE_AND AND} or 
     * {@link BookingDB#SEARCH_TYPE_OR OR}.
     *
     * @return an array of record numbers that match the specified criteria.
     */    
    public int[] findExact(String[] criteria, int operator);            
}
