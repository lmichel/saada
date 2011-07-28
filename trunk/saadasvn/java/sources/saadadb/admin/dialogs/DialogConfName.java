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
import javax.swing.JTextField;

import saadadb.util.RegExp;


public class DialogConfName extends JDialog implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String typed_name = null;
	private String typed_comment = null;
	private JTextField name_field;
	private JOptionPane optionPane;
	
	private String btnString1 = "Save";
	private String btnString2 = "Cancel";
	
	/**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
	public String getTyped_name() {
		return typed_name;
	}
	public String getTyped_comment() {
		return typed_comment;
	}
	
	/** Creates the reusable dialog. */
	public DialogConfName(Frame aFrame, String title, String default_name) {
		super(aFrame, true);
		setTitle(title);
		
		name_field = new JTextField(10);
		name_field.setText(default_name);
		
		Object[] array = {"Configuration Name", name_field};
		Object[] options = {btnString1, btnString2};
		optionPane = new JOptionPane(array,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[1]);
		setContentPane(optionPane);
		
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
		
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce) {
				name_field.requestFocusInWindow();
			}
		});
		
		name_field.addActionListener(this);
		optionPane.addPropertyChangeListener(this);
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
				if( typed_name.matches(RegExp.COLLNAME) ) {
					clearAndHide();							
					
				} else {
					JOptionPane.showMessageDialog(
							DialogConfName.this,
							"Configuration name must contain '_' letters and digits except at the first position",
							"",
							JOptionPane.ERROR_MESSAGE);
					typed_name = null;
					name_field.requestFocusInWindow();
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
