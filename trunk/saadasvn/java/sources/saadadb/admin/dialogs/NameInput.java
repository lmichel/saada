package saadadb.admin.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import saadadb.database.Database;
import saadadb.util.RegExp;

public class NameInput extends JDialog implements ActionListener, PropertyChangeListener {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String new_name = null;
	
	private JOptionPane optionPane;
	
	private String btnString1 = "Accept";
	private String btnString2 = "Cancel";
	JTextField name_field = new JTextField(20);
	/**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
	public String getTyped_name() {
		return new_name;
	}
	
	/** Creates the reusable dialog. */
	public NameInput(Frame aFrame, String title) {
		super(aFrame, true);
		setTitle(title);

		Object[] array = {"New Name", name_field};
		
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
				
		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
		this.pack();
		this.setLocationRelativeTo(this.getParent());
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
				new_name = name_field.getText();
				if( new_name.matches(RegExp.COLLNAME) )	 {
					if( Database.getCachemeta().getRelation(new_name) != null ) {
						JOptionPane.showMessageDialog(NameInput.this,
								"Sorry, relation \"" + new_name + "\" "
								+ "already exists",
								"",
								JOptionPane.ERROR_MESSAGE);
						new_name = null;
						name_field.requestFocusInWindow();
					}
					else {
						clearAndHide();
					}
					
				} else { //user closed dialog or clicked cancel
					JOptionPane.showMessageDialog(
							NameInput.this,
							"Relation name must contain '_' letters and digits except at the first position",
							"",
							JOptionPane.ERROR_MESSAGE);
					name_field.requestFocusInWindow();
					new_name = null;
				}
			}
			else {				
				clearAndHide();
			}
		}
	}
	
	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		setVisible(false);
	}

	/**
	 * @return Returns the new_name.
	 */
	public String getNew_name() {
		return new_name;
	}
}

