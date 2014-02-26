package saadadb.admintool.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @author laurentmichel
 * * @version $Id: DialogUCD.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class DialogUCD extends JDialog implements ActionListener,
		PropertyChangeListener {
	private String typed_comment = "";
	private JComboBox combo;
	private JTextField comment_field;
	
	private JOptionPane optionPane;

	private String btnString1 = "Apply";
	private String btnString2 = "Cancel";

	public DialogUCD(Frame aFrame, String title, String[] possible_ucds, String default_comment) {
		super(aFrame, true);
		this.setLocation(aFrame.getLocation());
		setTitle(title);
		
		comment_field = new JTextField(10);
		comment_field.setText(default_comment);

		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = {btnString1, btnString2};
		if( possible_ucds != null && default_comment != null) {
			combo         = new JComboBox(possible_ucds);
			Object[] array = {"Available combinations", combo, "Comment", comment_field};
			optionPane = new JOptionPane(array,
					JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION,
					null,
					options,
					options[0]);
		}
		else if( possible_ucds == null && default_comment != null ){
			Object[] array = {"Comment", comment_field};			
			optionPane = new JOptionPane(array,
					JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION,
					null,
					options,
					options[0]);
		}
		else if( possible_ucds != null && default_comment == null ){
			combo         = new JComboBox(possible_ucds);
			Object[] array = {"Available combinations", combo};			
			optionPane = new JOptionPane(array,
					JOptionPane.QUESTION_MESSAGE,
					JOptionPane.YES_NO_OPTION,
					null,
					options,
					options[0]);
		}
		
		
		
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
				if( comment_field != null ) {
					comment_field.requestFocusInWindow();
				}
			}
		});
		
		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
	}

	public String getTyped_comment() {
		return typed_comment;
	}
	
	public String getSelectedUCD() {
		if( this.combo != null ) {
			return this.combo.getSelectedItem().toString();
		}
		else {
			return null;
		}
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
				typed_comment = comment_field.getText().trim().replace('\'', ' ');
				clearAndHide();
			} else { //user closed dialog or clicked cancel
				typed_comment = null;
				this.combo = null;
				clearAndHide();
			}
		}
	}
	
	
	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		if( comment_field != null ) {
			comment_field.setText(null);
		}
		setVisible(false);
	}
	
	public static void main(String[] args) {
		
	JFrame frame = new JFrame();
	DialogUCD cd = new DialogUCD(frame, "Create a Collection", /*new String[]{"a", "b"}*/ null, "comment");
	cd.pack();
	cd.setLocationRelativeTo(frame);
    cd.setVisible(true);
	}

}


