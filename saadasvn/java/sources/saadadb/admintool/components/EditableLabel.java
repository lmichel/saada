package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admin.SaadaDBAdmin;
import saadadb.exceptions.AbortException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * @author michel
 * Panel with an editable label. The label is transformed in a text fields by clicking on the button.
 * The text field value is is evaluated when "return" is typed. It must match the ref_regexp regular expression.
 * If it doesn't, error_msg is displayed
 */
public class EditableLabel extends JPanel{
	/** * @version $Id: EditableLabel.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JTextField edit_label;	
	private JComponent text_area;
	private JButton modify = new JButton("Modify");
	private String ref_regexp, error_msg;
	private Runnable to_do_when_ok;
	
	/**
	 * @param text
	 */
	public EditableLabel(String text, String ref_regexp, int size, String error_msg, Runnable to_do_when_ok) {
		this.error_msg = error_msg;
		this.ref_regexp = ref_regexp;		
		this.to_do_when_ok = to_do_when_ok;
		label = SaadaDBAdmin.getPlainLabel(text);
		edit_label = new JTextField(label.getText());
		edit_label.setColumns(size);;
		label.setPreferredSize(new Dimension((int)(edit_label.getPreferredSize().getWidth()) + 15, (int)(edit_label.getPreferredSize().getHeight())));
		text_area = label;
		this.setLayout(new FlowLayout());
		this.add(text_area);
		this.add(modify);
		modify.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if( "Modify".equalsIgnoreCase(command)) {
					EditableLabel.this.setEditable(true);
				}
				else if( "Cancel".equalsIgnoreCase(command)) {
					EditableLabel.this.setEditable(false);
				}
			}
		});
		edit_label.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String newstr = EditableLabel.this.edit_label.getText();
				if( newstr.matches(EditableLabel.this.ref_regexp)) {
					EditableLabel.this.label.setText(EditableLabel.this.edit_label.getText());
					EditableLabel.this.setEditable(false);	
					try {
						// transction must be handled at higher level
						//SQLTable.beginTransaction();
						EditableLabel.this.to_do_when_ok.run();
						//SQLTable.commitTransaction();
					} catch (Exception e1) {
						Messenger.printStackTrace(e1);
						SaadaDBAdmin.showInputError(EditableLabel.this.getParent(), e1.getMessage());
					}
				}
				else {
					SaadaDBAdmin.showInputError(EditableLabel.this.getParent(), newstr + ": " + EditableLabel.this.error_msg);
				}
			}
		
		});
	}
	
	/**
	 * @return
	 */
	public String getText() {
		return this.edit_label.getText();
	}
	
	/**
	 * @param editable
	 */
	private void setEditable(boolean editable) {
		if( editable ) {
			this.remove(0);
			text_area = edit_label;
			this.add(text_area, 0);
			this.updateUI();
			modify.setText("Cancel");			
		}
		else {
			EditableLabel.this.remove(0);
			text_area = label;
			this.add(label, 0);
			this.updateUI();
			modify.setText("Modify");			
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		EditableLabel el = new EditableLabel("AAAA", RegExp.URL, 32, "Not a valid URL", new Runnable(){
			public void run() {
				System.out.println("COUCOU");
			}
		});
		f.add(el);
		f.pack();
		f.setVisible(true);
		
	}
}
