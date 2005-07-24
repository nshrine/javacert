/*
 * BookingData.java
 *
 * Created on 26 August 2004, 14:54
 */

package suncertify.db;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import suncertify.Utils;

/**
 * Implementation of the BookingDB interface for the URLyBird Application.
 *
 * @author Nick Shrine
 */
public class BookingData extends Data implements BookingDB {        
    
    /**
     * Constructs a data object that controls access to the data file specified
     * by the filename parameter.
     *
     * @param filename the binary file containing the data.
     * @throws FileNotFoundException if the data cannot be read.
     * @throws InvalidDataFileException if the data file is not a valid 
     *          URLyBird data file.
     * @throws IOException if there is an IO error opening the data file.
     */    
    protected BookingData(String filename) throws FileNotFoundException,
            InvalidDataFileException, IOException {        
        super(filename);
    }
      
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
    public synchronized int[] findExact(String[] criteria, int operator) {
        String[] data = null;
        List results = new ArrayList();
        
        recordloop: 
            for (int recNo = 1; recNo <= numRecords; recNo++) {            
                try {                                 
                    data = read(recNo);                
                } catch (RecordNotFoundException ex) {                
                    continue;
                }                 
                for (int i = 0; i < data.length; i++) {
                    switch (operator) {                        
                    case SEARCH_TYPE_AND: 

                        /* Detect a non-match: continue to next record */
                        if ((criteria[i] != null)
                                && !data[i].trim().equals(criteria[i])) {
                            continue recordloop;
                        }
                        break;

                    case SEARCH_TYPE_OR: 

                        /*
                         * Detect a match: add the record to the results 
                         * and continue to next record.
                         */
                        if (data[i].trim().equals(criteria[i])) {
                            results.add(new Integer(recNo));                    
                            continue recordloop;
                        }
                        break;
                    }
                }
                
                /*
                 * If operator was AND then all criteria match so
                 * add record to results.
                 */
                if (operator == SEARCH_TYPE_AND) { 
                    results.add(new Integer(recNo));                    
                }
        } //End recordloop                
        
        return (Utils.toIntArray(results)); 
    }            
}
