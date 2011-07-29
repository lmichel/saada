package saadadb.admin.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import saadadb.database.Database;


public  * @version $Id$
/* @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String typed_name = null;
	private String typed_passwd = null;
	private JTextField name_field;
	private JPasswordField password_field;
	
	private JOptionPane optionPane;
	
	private String btnString1 = "Connect";
	private String btnString2 = "Cancel";
	
	/**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
	public String getTyped_name() {
		return typed_name;
	}
	public String getTyped_passwd() {
		return typed_passwd;
	}
	
	/** Creates the reusable dialog. */
	public AdminPassword(Frame aFrame) {
		super(aFrame, true);
		setTitle("DBMS Administrator Login");
		
		name_field = new JTextField(Database.getConnector().getJdbc_administrator(), 10);
		name_field.setEditable(false);
		password_field = new JPasswordField(20);
		
		Object[] array = {"Administrator Name", name_field, "Administrator Password", password_field};
		
		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = {btnString1, btnString2};
		
		//Create the JOptionPane.
		optionPane = new JOptionPane(array,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[0]);
		
		//Make this dialog display it.
		setContentPane(optionPane);
		
		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window,
				 * we're going to change the JOptionPane's
				 * value property.
				 */
				optionPane.setValue(new Integer(
						JOptionPane.CLOSED_OPTION));
			}
		});
		
		//Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce) {
				name_field.requestFocusInWindow();
			}
		});
		
		//Register an event handler that puts the text into the option pane.
		name_field.addActionListener(this);
		
		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
		this.pack();
		this.setLocationRelativeTo(aFrame);
        this.setVisible(true);
	}
	
	/** This method handles events for the text field. */
	public void actionPerformed(ActionEvent e) {
		optionPane.setValue(btnString1);
	}
	
	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		
		if (isVisible()
				&& (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) ||
						JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();
			
			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}
			
			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			
			if (btnString1.equals(value)) {
				typed_name = name_field.getText().trim();
				try {
					Database.getConnector().setAdminMode(new String(password_field.getPassword()));
					clearAndHide();
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(AdminPassword.this,
							"Database connection failed",
							"",
							JOptionPane.ERROR_MESSAGE);
					password_field.setText("");
				}
			} else { //user closed dialog or clicked cancel
				typed_name = null;
				clearAndHide();
			}
		}
	}
	
	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		name_field.setText(null);
		setVisible(false);
	}
}
