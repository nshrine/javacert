/*
 * BookingTableModel.java
 *
 * Created on 11 August 2004, 16:44
 */
package suncertify.client;

import javax.swing.table.AbstractTableModel;
import suncertify.db.BookingDB;
import suncertify.db.RecordNotFoundException;

/**
 * <code>TableModel</code> implementation to model the data stored in a
 * database accessed with a {@link suncertify.db.BookingDB BookingDB}
 * implememtation making it available for display in a <code>JTable</code>.
 * 
 * @author Nick Shrine
 */
public class BookingTableModel extends AbstractTableModel {            
    
    /**
     * {@link suncertify.db.BookingDB BookingDB} object used for data access.
     */
    protected final BookingDB db;    
    
    /**
     * The column headings for the <code>TableModel</code>.
     */
    protected final String[] fieldNames;
    
    /**
     * The number of columns in the Table.
     */    
    protected final int numCols;    
    
    /**
     * Array storing the record numbers the rows of the table correspond to.
     */
    protected int[] records;
    
    /**
     * Creates a new instance of the <code>TableModel</code> using the
     * supplied {@link suncertify.db.BookingDB} object for data access.
     *
     * @param db {@link suncertify.db.BookingDB} object to use for data access.
     */
    public BookingTableModel(BookingDB db) {        
        this.db = db;        
        fieldNames = ClientFrame.FIELDNAMES;
        numCols = fieldNames.length;
        
        /* Do a find that returns all records to initialise record numbers */
        records = db.find(new String[numCols]);
    }
    
    /**
     * Returns the record number corresponding to the specified row.
     *
     * @param row the table row for which the record number is required.
     *
     * @return the record number for the specified row.
     */    
    public int getRecNo(int row) {        
        return records[row];
    }
    
    /**
     * Sets the record numbers to be held in the rows of the table.
     *
     * @param records the array of record numbers specifying the records to
     * be displayed in the rows of the table.
     */    
    public void setRecords(int[] records) {        
        this.records = records;
        fireTableDataChanged();
    }
    
    /**
     * Returns the number of columns in the table.
     *
     * @return the number of columns in the table.
     */    
    public int getColumnCount() {        
        return numCols;
    }
    
    /**
     * Returns the number of rows in the table.
     *
     * @return the number of rows in the table.
     */    
    public int getRowCount() {        
        return records.length;
    }
    
    /**
     * Returns the object stored at the specified row and column of the table.
     *
     * @param row the row number of the object to be returned.
     * @param column the column number of the object to be returned.
     *
     * @return the object at the specified row and column.
     */    
    public Object getValueAt(int row, int column) {
        String[] rowData = null;
        Object value = null;
        
        try {
            rowData = db.read(records[row]);
            value = rowData[column].trim();
        } catch (RecordNotFoundException ex) {            
            throw new RuntimeException(ex); //Should never happen.
        }
        
        return value;
    }
    
    /**
     * Returns the name of the column at the specified column number.
     *
     * @param column the column number.
     *
     * @return the name of the specified column.
     */    
    public String getColumnName(int column) {        
        return fieldNames[column];
    }
}
