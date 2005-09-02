/*
 * ConfigurationDialog.java
 *
 * Created on 09 September 2004, 10:32
 */

package suncertify;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog in which to enter the configuration parameters for the 
 * URLyBird application.
 * <p>
 * For example when starting the server, the dialog will ask for the 
 * database file to read and the port number to listen for connections on.
 * <p>
 * The dialog allows the parameters of the {@link Configuration Configuration}
 * object for this invocation to be edited.
 *
 * @author Nick Shrine
 */
public class ConfigurationDialog extends JDialog implements ActionListener,
            CaretListener {
    
    /**
     * The size of the insets to use for decorative padding between components.
     */
    protected static final Insets INNER = new Insets(5, 5, 5, 5);
    
    /**
     * The number of characters to use for the editable fields.
     */
    protected static final int FIELD_WIDTH = 15;
    
    /**
     * The name of the choose file button that opens a file chooser.
     */
    protected static final String CHOOSE = "Choose";
    
    /**
     * The name of the Ok button.
     */
    protected static final String OK = "OK";
    
    /**
     * The name of the cancel button.
     */
    protected static final String CANCEL = "Cancel";
        
    /**
     * The {@link Configuration Configuration} object that this dialog will
     * allow to be edited.
     */    
    protected final Configuration config;    
    
    /**
     * The keys corresponding to the paramaters of the {@link Configuration
     * Configuration} object that we want to edit.
     */
    protected final String[] keys;       
    
    /**
     * Array of <code>JTextField</code>s to be used for editing each value.
     */
    protected JTextField[] valueFields;
    
    /**
     * The Ok button.
     */
    protected JButton okButton;
    
    /**
     * Flag indicating whether the user confirmed the entered values or hit
     * cancel.
     */
    protected boolean confirmed;
    
    /**
     * Creates a new instance of ConfigurationDialog using the supplied
     * {@link Configuration Configuration} instance.
     *
     * @param config the {@link Configuration Configuration} object to be
     *          edited in this dialog.
     */
    public ConfigurationDialog(Configuration config) {        
        super((JFrame) null, config.getDescription(), true);
    
        this.config = config;
        this.keys = config.getKeys();
        
        initComponents();
    }
    
    /**
     * Determines whether the user confirmed the values they entered by
     * hitting ok or whether they hit cancel.
     * 
     * @return true if the user confirmed the values or false if they hit
     *          cancel.
     */    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * This method is called when a GUI action that raises an 
     * <code>ActionEvent</code> occurs. In this case it handles the actions
     * for the Ok and Cancel buttons being pressed.
     *
     * @param event the event raised by a GUI action.
     */
    public void actionPerformed(ActionEvent event) {        
        String command = event.getActionCommand();
        
        if (command.equals(OK)) {        
            for (int i = 0; i < keys.length; i++) {
                
                /* Set the config parameters to the edited field values */
                String value = valueFields[i].getText().trim();                
                config.set(keys[i], value);
            }
            confirmed = true;
            dispose();
        } else if (command.equals(CANCEL)) {
            dispose();
        }
    }
    
    /**
     * Called when the caret in the text fields is moved. This
     * method checks the contents of the text fields after they are edited
     * and enables Ok button if the contents are valid.
     *
     * @param event The event raised by a caret change.
     */  
    public void caretUpdate(CaretEvent event) {        
        for (int i = 0; i < keys.length; i++) {
            String value = valueFields[i].getText().trim();
            if (value.length() < 1) {
                okButton.setEnabled(false);
                return;
            } else {
                try {
                    if (Integer.parseInt(value) < 1) {
                        okButton.setEnabled(false);
                    }
                } catch (NumberFormatException ex) {
                    okButton.setEnabled(false);
                }
            }
        }
        
        /* All fields must be valid so we can enable the ok button */
        okButton.setEnabled(true);
    }
    
    /**
     * Lays out the GUI components, sets their behaviour parameters and
     * registers listeners.
     */
    protected void initComponents() {        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);        
        getContentPane().setLayout(new GridBagLayout());
        
        JPanel propsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = INNER; 
        propsPanel.setBorder(new TitledBorder("Configuration"));
                
        valueFields = new JTextField[keys.length];        
        for (int i = 0; i < keys.length; i++) {
            constraints.gridy = i;
            constraints.gridx = 0;
            JLabel label = new JLabel(Configuration
                    .getKeyDescription(keys[i]));
            propsPanel.add(label, constraints);
            
            final JTextField valueField = new JTextField(config.get(keys[i]),
                    FIELD_WIDTH);
            valueField.addCaretListener(this);
            constraints.gridx++;
            propsPanel.add(valueField, constraints);
            if (Configuration.getKeyType(keys[i])
                    .equals(Configuration.FILE_KEY)) {
                JButton chooseButton = new JButton(CHOOSE);
                
                chooseButton.addActionListener(new ActionListener() {
                    
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fc = new JFileChooser();
                        int result = fc.showOpenDialog(
                            ConfigurationDialog.this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            valueField.setText(fc.getSelectedFile().getPath());
                        }
                    }
                });
                
                constraints.gridx++;                
                propsPanel.add(chooseButton, constraints);
            }
            valueFields[i] = valueField;
        }
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        getContentPane().add(propsPanel, constraints);
        
        JPanel bottom = new JPanel();
        okButton = new JButton(OK);
        caretUpdate(null);
        okButton.addActionListener(this);
        bottom.add(okButton);
        JButton cancelButton = new JButton(CANCEL);
        cancelButton.addActionListener(this);
        bottom.add(cancelButton);
        
        constraints.gridx = 0;
        constraints.gridy = 1;        
        constraints.anchor = GridBagConstraints.CENTER;
        getContentPane().add(bottom, constraints);
        
        pack();        
    }                 
}
