package suncertify.server;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import suncertify.db.BookingData;

public class ServerControlFrame extends JFrame implements ActionListener {
    
    public static final String BUTTON1 = "Show Locked Records";
    
    protected final BookingData db;
    protected JTextArea locksDisplay;
    
    public ServerControlFrame(BookingData db) {
        this.db = db;
        initComponents();
    }
    
    protected void initComponents() {        
        JButton button1 = new JButton(BUTTON1);
        button1.addActionListener(this);
        getContentPane().add(button1, BorderLayout.NORTH);
        
        locksDisplay = new JTextArea(10, 40);
        getContentPane().add(locksDisplay, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(BUTTON1)) {
            Map lockedRecords = db.getLockedRecords();
            Iterator itr = lockedRecords.keySet().iterator();
            StringBuffer sb = new StringBuffer();
            
            while(itr.hasNext()) {
                Integer key = (Integer)itr.next();
                sb.append("Record " + key + " with cookie " + 
                    lockedRecords.get(key) + "\n");
            }
            
            locksDisplay.setText(sb.toString());
        }
    }
    
}