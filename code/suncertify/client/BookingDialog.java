/*
 * BookingDialog.java
 *
 * Created on 17 August 2004, 10:43
 */

package suncertify.client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import suncertify.db.BookingDB;
import suncertify.db.RecordNotFoundException;
import suncertify.Utils;

/**
 * This dialog is used to book a record to a specific person who becomes the
 * owener of the booking. It displays the relevant details for the booking
 * and an editable field to allocate the booking to a person.
 * <p>
 * The person is specified by an 8 digit Customer ID as specified by the format
 * of the database.
 * <p>
 * This dialog should be opened by a top-level GUI window that allows a user
 * to select a record to be booked. The instantiating object needs to supply
 * a {@link suncertify.db.BookingDB BookingDB} object and a record number in
 * order for this class to edit the relevant record in the database.
 *
 * @author Nick Shrine
 */
public class BookingDialog extends JDialog implements ActionListener {
    
    /**
     * Insets for close padding between fields.
     */
    protected static final Insets CLOSE = new Insets(1, 5, 5, 1);
    
    /**
     * Insets for padding between inner components.
     */
    protected static final Insets INNER = new Insets(5, 5, 5, 5);
    
    /**
     * Insets for outer padding.
     */
    protected static final Insets OUTER = new Insets(10, 10, 10, 10);
    
    /**
     * The name of the Book button.
     */
    protected static final String BOOK = "Book";
    
    /**
     * The name of the Cancel button.
     */
    protected static final String CANCEL = "Cancel";
    
    /**
     * The text for the waiting message.
     */
    protected static final String WAITING_MESSAGE =
            "Waiting to obtain lock on record";                
    
    /**
     * {@link suncertify.db.BookingDB BookingDB} object used for data access.
     */
    protected final BookingDB db;    
    
    /**
     * The number of the record currently being booked.
     */
    protected final int recNo;
    
    /**
     * The data fields of the record being booked.
     */
    protected String[] record;    
    
    /**
     * The lock cookie of the record being booked.
     */
    protected long cookie;
    
    /**
     * The text field used to set the owner of the booking.
     */
    protected JTextField ownerField;
    
    /**
     * Creates a new instance of BookingDialog.
     * 
     * @param owner The <code>JFrame</code> that is the parent of this
     *      <code>Dialog</code>.
     * @param db {@link suncertify.db.BookingDB} object to use for data access.
     * @param recNo the number of the record to be booked.
     */
    public BookingDialog(JFrame owner, BookingDB db, int recNo) {        
        super(owner, "Booking", true);
        
        this.db = db;
        this.recNo = recNo;
        record = new String[ClientFrame.FIELDNAMES.length];        
        
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setContentPane(getWaitingPanel());
        pack();
        setResizable(false);
        setLocationRelativeTo(getParent());
        
        Thread openRecord = new Thread(new OpenRecord());
        openRecord.start();
    }        
    
    /**
     * This method is called when a GUI action that raises an 
     * <code>ActionEvent</code> occurs. In this case it handles the actions
     * for the Book and Cancel buttons being pressed and calls the appropriate
     * business method.
     *
     * @param event the event raised by a GUI action.
     */    
    public void actionPerformed(ActionEvent event) {        
        String command = event.getActionCommand();
        String message = null;
        
        if (command.equals(BOOK) || command.equals(CANCEL)) {
            try {
                if (command.equals(BOOK)) {
                    String customerId = ownerField.getText().trim();
                    if (customerId.length() > 0) {
                        Integer.valueOf(customerId); //Check id is an integer
                        if (customerId.length() != 8) {
                            throw new NumberFormatException();
                        }
                        message = "Booking allocated to customer "
                                + customerId;
                    } else {
                        message = "Booking cancelled";
                    }
                    record[6] = customerId; //data field 6 is the customer id.
                    db.update(recNo, record, cookie);
                    JOptionPane.showMessageDialog(this, message, "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }            
                db.unlock(recNo, cookie);
                dispose();
            } catch (NumberFormatException ex) {
                Utils.errorBox(this, "Invalid Customer ID\n\n"
                        + "Must be an 8-digit number");
            } catch (RecordNotFoundException ex) {
                Utils.errorBox(this, ex.getMessage());
            } catch (suncertify.db.SecurityException ex) {
                Utils.errorBox(this, ex.getMessage());
            }            
        }
    }        
        
    /**
     * Returns a panel indicating that the Dialog is waiting to obtain a lock
     * on the requested record.
     *
     * @return a <code>JPanel</code> displaying a waiting message.
     */    
    protected JPanel getWaitingPanel() {        
        JPanel panel = new JPanel(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = OUTER;        
        
        JLabel label = new JLabel(WAITING_MESSAGE);                
        panel.add(label, constraints);
        
        return panel;
    }
    
    /**
     * Returns a panel for editing the booking once the lock on the record
     * has been obtained.
     *
     * @return a <code>JPanel</code> for editing the booking.
     */    
    protected JPanel getEditPanel() {                
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();        
        constraints.insets = INNER; 
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());       
        detailsPanel.setBorder(new TitledBorder("Details"));
        
        for (int i = 0; i < ClientFrame.FIELDNAMES.length - 1; i++) {
            constraints.gridy = i;            
            constraints.weightx = 0.0;
            constraints.anchor = GridBagConstraints.LINE_START;
            detailsPanel.add(new JLabel(ClientFrame.FIELDNAMES[i].toString()),
                    constraints);
            JTextField field = new JTextField(record[i].trim());
            field.setEditable(false);
            field.setBorder(new EmptyBorder(1, 1, 1, 1));           
            constraints.weightx = 0.1;
            detailsPanel.add(field, constraints);
        }
        
        constraints.gridy = 0;
        constraints.insets = OUTER;
        panel.add(detailsPanel, constraints);   
        
        JPanel bookingPanel = new JPanel(new GridBagLayout());     
        bookingPanel.setBorder(new TitledBorder("Book Room"));
        JLabel ownerLabel = new JLabel("Customer ID");                
        constraints.insets = CLOSE;
        bookingPanel.add(ownerLabel, constraints);        
        ownerField = new JTextField(record[6].trim(), 8);
        constraints.gridy = 1;        
        bookingPanel.add(ownerField, constraints);
        JButton bookButton = new JButton(BOOK);
        bookButton.addActionListener(this);
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.LINE_END;        
        bookingPanel.add(bookButton, constraints);
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = OUTER;
        panel.add(bookingPanel, constraints);   
        
        JButton cancelButton = new JButton(CANCEL);
        cancelButton.addActionListener(this);
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        panel.add(cancelButton, constraints);   
        
        return panel;
    }
    
    /**
     * An inner class that implements <code>Runnable</code> in order to 
     * obtain the lock on the record to be opened in a seperate thread, 
     * waiting if need be for the record to become available without locking
     * up the GUI thread thereby allowing it to display a waiting message.
     */
    protected class OpenRecord implements Runnable {
        
        /**
         * Creates a new instance of OpenRecord.
         */
        public OpenRecord() {
            
        }
        
        /**
         * This method obtains the lock on the record required by the outer
         * {@link BookingDialog BookingDialog} object and reads the data for
         * the record. It may sleep while waiting for the lock on the record.
         * <p>
         * After obtaining the lock and reading the data it changes the
         * contents of the outer dialog object from the default waiting message
         * to a panel for editing the booking.
         */
        public void run() {            
            try {
                cookie = db.lock(recNo); //May cause thread to sleep
                record = db.read(recNo);
                setContentPane(getEditPanel()); //Change waiting panel to edit
                pack();
                setLocationRelativeTo(getParent());                
            } catch (RecordNotFoundException ex) {
                Utils.errorBox(BookingDialog.this, ex.getMessage());
            }
        }        
    }
}
