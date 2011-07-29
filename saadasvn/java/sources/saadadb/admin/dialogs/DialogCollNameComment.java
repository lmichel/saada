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
import javax.swing.JTextArea;
import javax.swing.JTextField;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaCollection;
import saadadb.util.RegExp;


public class DialogCollNameComment extends JDialog implements ActionListener, PropertyChangeListener {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String typed_name = null;
	private boolean just_comment = false;
	private String typed_comment = null;
	private JTextField name_field;
	private JTextArea comment_field;
	
	private JOptionPane optionPane;
	
	private String btnString1 = "Create";
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
	public DialogCollNameComment(Frame aFrame, String title, String name) {
		super(aFrame, true);
		setTitle(title);
		
		comment_field = new JTextArea(3, 20);
		name_field = new JTextField(10);
		if( name != null ) {
			name_field.setText(name);
			name_field.setEditable(false);
			btnString1 = "Save Description";
			just_comment = true;
			try {
				MetaCollection sc = Database.getCachemeta().getCollection(name);
				comment_field.setText(sc.getDescription());
			} catch (SaadaException e) {
				SaadaDBAdmin.showFatalError(aFrame, e);
			}
		}
		
		Object[] array = {"Collection Name", name_field, "Collection Description", comment_field};
		
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
				if( just_comment ) {
					this.typed_name = name_field.getText().trim();
					this.typed_comment = comment_field.getText().trim().replace('\'', ' ');
					clearAndHide();												
				}
				else {
					typed_name = name_field.getText().trim();
					if( typed_name.matches(RegExp.COLLNAME) ) {
						try {
							Database.getCachemeta().getCollection(typed_name) ;
							JOptionPane.showMessageDialog(DialogCollNameComment.this,
									"Sorry, collection \"" + typed_name + "\" "
									+ "already exists",
									"",
									JOptionPane.ERROR_MESSAGE);
							typed_name = null;
							name_field.requestFocusInWindow();
						} catch (Exception e1) {
							typed_comment = comment_field.getText().trim().replace('\'', ' ');
							clearAndHide();							
						}
					} else {
						JOptionPane.showMessageDialog(
								DialogCollNameComment.this,
								"Collection name must contain '_' letters and digits except at the first position",
								"",
								JOptionPane.ERROR_MESSAGE);
						typed_name = null;
						name_field.requestFocusInWindow();
					}
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
