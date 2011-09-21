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

import saadadb.admin.SaadaDBAdmin;
import saadadb.exceptions.AbortException;


public class AbortExceptionDialog extends JDialog implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JOptionPane optionPane;
	
	private String btnString1 = "OK";
	
	
	/** Creates the reusable dialog. */
	public AbortExceptionDialog(Frame aFrame, AbortException ie) {
		super(aFrame, true);
		setTitle("SQL Abort Exception");
		
		Object[] array = {"The Following Error Occured:", SaadaDBAdmin.getPlainLabel(ie.getMessage())
				        , "For This Reason", SaadaDBAdmin.getPlainLabel(ie.getContext())
				        , "<HTML><BR>The current transaction has been aborted<BR>All data modifications since the last commit are lost", null};
		Object[] options = {btnString1};
		optionPane = new JOptionPane(array,
				JOptionPane.ERROR_MESSAGE,
				JOptionPane.YES_NO_OPTION,
				null,
				options,
				options[0]);
		
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
			clearAndHide();
		}
	}
	
	
	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		setVisible(false);
	}
	
	public static void main(String[] args) {
		AbortExceptionDialog ied = new AbortExceptionDialog(null, new AbortException("PLante", "c'est comme ca"));
		
	}
}
