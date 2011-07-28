package saadadb.admin.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;


public class SelectClass extends JDialog implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String class_name = null;
	private JComboBox class_selector;
	
	private JOptionPane optionPane;
	
	private String btnString1 = "Load";
	private String btnString2 = "Cancel";
	
	/**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
	public String getTyped_name() {
		return class_name;
	}
	
	/** Creates the reusable dialog. */
	public SelectClass(Frame aFrame, String[] class_list) {
		super(aFrame, true);
		setTitle("Class Selector");
		class_selector = new JComboBox(class_list);		
		
		Object[] array = {"Classes", class_selector};
		
		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = {btnString1, btnString2};
		
		//Create the JOptionPane.
		optionPane = new JOptionPane(array,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[1]);
		
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
		this.setLocationRelativeTo(this.getParent());
		this.pack();
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
				class_name = class_selector.getSelectedItem().toString();
			} else { //user closed dialog or clicked cancel
				class_name = null;
			}
			clearAndHide();
		}
	}
	
	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		setVisible(false);
	}
}
