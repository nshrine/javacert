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
import java.util.Properties;

/**
 *
 * @author Nick Shrine
 */
public class ConfigurationDialog extends JDialog implements ActionListener,
            CaretListener {
    
    protected static final Insets INNER = new Insets(5, 5, 5, 5);
    protected static final int FIELD_WIDTH = 15;
    protected static final String CHOOSE = "Choose";
    protected static final String OK = "OK";
    protected static final String CANCEL = "Cancel";
        
    protected final Configuration config;    
    protected final String[] keys;       
    
    protected JTextField[] valueFields;
    protected JButton okButton;
    protected boolean confirmed;
    
    /**
     * Creates a new instance of ConfigurationDialog
     * @param config
     */
    public ConfigurationDialog(Configuration config) {        
        super((JFrame)null, config.getDescription(), true);
    
        this.config = config;
        this.keys = config.getKeys();
        
        initComponents();
    }
    
    /**
     *
     * @return
     */    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     *
     * @param e
     */    
    public void actionPerformed(ActionEvent e) {        
        String command = e.getActionCommand();
        
        if (command.equals(OK)) {        
            for (int i = 0; i < keys.length; i++) {
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
     *
     * @param e
     */    
    public void caretUpdate(CaretEvent e) {        
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
        okButton.setEnabled(true);
    }
    
    protected void initComponents() {        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);        
        getContentPane().setLayout(new GridBagLayout());
        
        JPanel propsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = INNER; 
        propsPanel.setBorder(new TitledBorder("Configuration"));
                
        valueFields = new JTextField[keys.length];        
        for(int i=0; i < keys.length; i++) {
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
            if(Configuration.getKeyType(keys[i])
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
