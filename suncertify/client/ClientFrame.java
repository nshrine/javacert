/*
 * Client.java
 *
 * Created on 06 July 2004, 14:09
 */
package suncertify.client;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import suncertify.db.BookingDB;

/**
 * The Main frame of the URLyBird GUI Client that should be instantiated to
 * start the client.
 * <p>
 * The GUI requires a data access object that implements the {@link
 * suncertify.db.BookingDB BookingDB} interface in order to communicate with
 * the hotel booking database.
 *  
 * @author Nick Shrine
 */
public class ClientFrame extends JFrame implements ActionListener,
        CaretListener {        
    
    /**
     * The title of the main window.
     */
    protected static final String TITLE = "URLyBird Hotel Booking System";    
    
    /**
     * The number of columns to use for the name and location search fields.
     */
    protected static final int FIELD_COLS = 20;    
    
    /**
     * The size of the insets to use for decorative padding between components.
     */
    protected static final Insets INSETS = new Insets(5, 5, 5, 5);    
    
    /**
     * The name of the first menu.
     */
    protected static final String MENU1 = "File";
    
    /**
     * The name of the second menu.
     */
    protected static final String MENU2 = "Edit";        
    
    /**
     * The name of the search button and the identifier for the search command
     * that is issued when performing search for records by a criteria.
     */
    protected static final String SEARCH = "Search";
    
    /**
     * The name of the show all button and the identifier for the show all
     * command that is issued when wanting to display all records.
     */
    protected static final String ALL = "Show All";
    
    /**
     * The name of the edit booking button.
     */
    protected static final String BOOK = "Edit Booking";    
    
    /**
     * The name of the quit button.
     */
    protected static final String QUIT = "Exit";    
    
    /**
     * The name of the AND drop-down choice.
     */
    protected static final String AND = "AND";
    
    /**
     * The name of the OR drop-down choice.
     */
    protected static final String OR = "OR";
    
    /**
     * The list of search types for the combo box.
     */
    protected static final String[] SEARCH_TYPES = { AND, OR };
    
    /**
     * The column headings for the display table.
     */
    public static final String[] FIELDNAMES = { "Hotel Name", 
                                                "Location", 
                                                "Occupancy",
                                                "Smoking",
                                                "Price",
                                                "Date Available",
                                                "Customer ID"};
                                                
    /**
     * The column widths of the display table.
     */
    public static final int[] COLUMN_WIDTHS = { 100,
                                                70,
                                                20,
                                                20,
                                                20,
                                                50,
                                                50 };
                
    /**
     * The column alignments for the display table.
     */
    public static final int[] COLUMN_ALIGNMENT = { JLabel.LEFT, 
                                                   JLabel.LEFT, 
                                                   JLabel.CENTER, 
                                                   JLabel.CENTER, 
                                                   JLabel.RIGHT, 
                                                   JLabel.CENTER, 
                                                   JLabel.RIGHT };
                                                   
    /**
     * The ratio of the height of the main window to the screen height.
     */
    public static final float FRAME_HEIGHT_RATIO = 0.75f;
    
    /**
     * The ratio of the width of the main window to the screen width.
     */
    public static final float FRAME_WIDTH_RATIO = 0.67f;
   
    /**
     * {@link suncertify.db.BookingDB BookingDB} object used for data access.
     */
    protected final BookingDB db;
    
    /**
     * {@link BookingTableModel BookingTableModel} object used for modelling 
     * the data for the display table.
     */
    protected final BookingTableModel tableModel;
    
    /**
     * The name field in the search panel.
     */
    protected JTextField nameField;
    
    /**
     * The location field in the search panel.
     */
    protected JTextField locationField;
    
    /**
     * The combo box for selecting the search type in the seach panel.
     */
    protected JComboBox searchTypeCombo;
    
    /**
     * The table for displaying the booking details and search results.
     */
    protected JTable bookingTable;
    
    /**
     * Creates a new instance of the Client using the supplied 
     * {@link suncertify.db.BookingDB} object for data access.
     *
     * @param db {@link suncertify.db.BookingDB} object to use for data access.
     */
    public ClientFrame(BookingDB db) {              
        super(TITLE);        
        this.db = db;        
        tableModel = new BookingTableModel(db);                
        initComponents();
    }
            
    /**
     * Called when an action that raises an <code>ActionEvent</code> is 
     * performed on the GUI.
     *
     * @param event the event raised by a GUI action.
     */
    public void actionPerformed(ActionEvent event) {        
        String command = event.getActionCommand();
        
        if (command.equals(SEARCH) || command.equals(ALL)) {
            search(command);
        } else if (command.equals(BOOK)) {
            book();
        } else if (command.equals(QUIT)) {            
            System.exit(0);          
        }
    }
    
    /**
     * Called when the caret in the search panel text fields is moved. This
     * method checks the contents of the text fields after an edit of the
     * contents and enables the search GUI components if the contents are 
     * valid.
     *
     * @param event The event raised by a caret change.
     */
    public void caretUpdate(CaretEvent event) {        
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        
        /*
         * If the name and location search fields are blank disable the
         * search type combo box.
         */
        if ((name.length() > 0) && (location.length() > 0)) {
            searchTypeCombo.setEnabled(true);
        } else {
            searchTypeCombo.setSelectedItem(AND);
            searchTypeCombo.setEnabled(false);
        }
    }
    
    /**
     * This method is called when the Search or Show All buttons are pressed
     * and populates the display table with either the results of the search
     * or all records if the Show All button was pressed.
     *
     * @param command the command to be run that determines which records are
     *      displayed in the table, either {@link #SEARCH SEARCH} or
     *      {@link #ALL SHOW ALL}.
     *
     * @see suncertify.db.BookingDB#findExact
     */    
    protected void search(String command) {
        
        /* There is one search criteria for each of the 7 data fields */
        String[] criteria = new String[7]; 
        
        if (command.equals(SEARCH)) {
            criteria[0] = nameField.getText().trim(); //hotel name field 0
            criteria[1] = locationField.getText().trim(); //location field 1
            
            /* The search algorithm requires empty criteria to be set to null */
            for (int i = 0; i < 2; i++) {
                criteria[i] = (criteria[i].length() == 0 ? null : criteria[i]);
            }
        }
        
        if (searchTypeCombo.getSelectedItem().equals(AND)
                || command.equals(ALL)) {            
            tableModel.setRecords(db.findExact
                    (criteria, BookingDB.SEARCH_TYPE_AND));
        } else if (searchTypeCombo.getSelectedItem().equals(OR)) {
            tableModel.setRecords(db.findExact(criteria,
                    BookingDB.SEARCH_TYPE_OR));
        }
    }
    
    /**
     * This method is called to bring up the booking dialog for a reservation
     * when the Book menu item is selected or when a row in the display table
     * is double-clicked on.
     */
    protected void book() {        
        int row = bookingTable.getSelectedRow();
        
        /* If a reservation isn't selected display an informative dialog */
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a row from the table first.");         
        } else {
            int recNo = tableModel.getRecNo(row);       
            JDialog bookingDialog = new BookingDialog(this, db, recNo);        
            bookingDialog.setVisible(true);
            tableModel.fireTableDataChanged(); //Update table with new booking
        }
    }        
        
    /**
     * Lays out the GUI components.
     */
    protected void initComponents() {                        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        
        JMenuBar menubar = new JMenuBar();
        JMenu menu1 = new JMenu(MENU1);
        menu1.setMnemonic(KeyEvent.VK_F);
        JMenuItem quitItem = new JMenuItem(QUIT, KeyEvent.VK_X);
        quitItem.addActionListener(this);
        menu1.add(quitItem);
        menubar.add(menu1);
        JMenu menu2 = new JMenu(MENU2);
        menu2.setMnemonic(KeyEvent.VK_E);
        JMenuItem bookItem = new JMenuItem(BOOK, KeyEvent.VK_B);
        bookItem.addActionListener(this);
        menu2.add(bookItem);
        menubar.add(menu2);
        setJMenuBar(menubar);
                
        JPanel searchPanel = new JPanel();        
        searchPanel.setLayout(new GridBagLayout());
        searchPanel.setBorder(new TitledBorder("View"));        
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = INSETS;
        
        JLabel nameLabel = new JLabel("Name");
        constraints.gridx = 0;
        constraints.gridy = 0;        
        constraints.anchor = GridBagConstraints.LINE_END;
        searchPanel.add(nameLabel, constraints);
                
        nameField = new JTextField(FIELD_COLS);
        nameField.addCaretListener(this);
        constraints.gridx = 1;
        constraints.gridy = 0;                
        searchPanel.add(nameField, constraints);        
        
        searchTypeCombo = new JComboBox(SEARCH_TYPES);
        searchTypeCombo.setEnabled(false);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.LINE_START;
        searchPanel.add(searchTypeCombo, constraints);
        
        JLabel locationLabel = new JLabel("Location");        
        constraints.gridx = 0;
        constraints.gridy = 1;                
        constraints.anchor = GridBagConstraints.LINE_END;
        searchPanel.add(locationLabel, constraints);
        
        locationField = new JTextField(FIELD_COLS);        
        locationField.addCaretListener(this);
        constraints.gridx = 1;
        constraints.gridy = 1;                
        searchPanel.add(locationField, constraints);                
        
        JButton searchButton = new JButton(SEARCH);
        searchButton.addActionListener(this);
        constraints.gridx = 1;
        constraints.gridy = 2;                        
        searchPanel.add(searchButton, constraints);        
        
        JButton showAllButton = new JButton(ALL);
        showAllButton.addActionListener(this);
        constraints.gridx = 2;
        constraints.gridy = 2;        
        searchPanel.add(showAllButton, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 0;  
        constraints.anchor = GridBagConstraints.PAGE_START;
        getContentPane().add(searchPanel, constraints);
        
        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {            
                if (event.getClickCount() > 1) {                
                    book();
                }
            }
        });         
        TableColumnModel columnModel = bookingTable.getColumnModel();
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            TableColumn column = columnModel.getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTHS[i]);
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(COLUMN_ALIGNMENT[i]);
            column.setCellRenderer(renderer);
        }        
        
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        constraints.fill = GridBagConstraints.BOTH;        
        getContentPane().add(new JScrollPane(bookingTable), constraints);
        
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screensize.getWidth() * FRAME_WIDTH_RATIO);
        int height = (int) (screensize.getHeight() * FRAME_HEIGHT_RATIO);
        setSize(width, height);        
    }            
}
