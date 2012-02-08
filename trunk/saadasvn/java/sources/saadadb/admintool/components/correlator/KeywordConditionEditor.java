package saadadb.admintool.components.correlator;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import saadadb.admin.relation.CorrQueryEditor;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;


public class KeywordConditionEditor extends CollapsiblePanel {
	private JComboBox primary_att = new JComboBox();
	private JComboBox secondary_att = new JComboBox();
	private JTextArea condition = new JTextArea(12, 48);
	private RelationPopulatePanel taskPanel;
	private JTextArea active_att_receiver;
	
	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public KeywordConditionEditor(RelationPopulatePanel taskPanel, Component toActive) {
		super("Condition based on Keywords");
		this.taskPanel = taskPanel;
		this.condition.setToolTipText("Primary and secondary records are linked when the condition (SQL statement) is true.");
		this.condition.addCaretListener(new CaretListener() {

			public void caretUpdate(CaretEvent e) {
				JTextArea jta = (JTextArea) e.getSource();
				if( jta != KeywordConditionEditor.this.active_att_receiver )  {
					KeywordConditionEditor.this.activateQueryField((JTextArea) e.getSource());
				}
			}
			
		});
		primary_att.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( primary_att.getSelectedItem() != null && active_att_receiver != null 
						&& !primary_att.getSelectedItem().toString().startsWith("-")) {
					active_att_receiver.insert(primary_att.getSelectedItem().toString().split(" ")[0]
							                 , active_att_receiver.getCaretPosition());
				}
			}
		});
		secondary_att.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( secondary_att.getSelectedItem() != null && active_att_receiver != null 
						&& !secondary_att.getSelectedItem().toString().startsWith("-")) {
					active_att_receiver.insert(secondary_att.getSelectedItem().toString().split(" ")[0]
							                 , active_att_receiver.getCaretPosition());
				}
			}
		});
		
		MyGBC mc = new MyGBC(5,5,5,5); 
		
		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		mc.gridwidth = 6;
		panel.add(new JScrollPane(condition), mc);
		mc.newRow(); mc.gridwidth = 3;
		panel.add(primary_att, mc);
		mc.gridx = 3;mc.gridwidth = 3;
		panel.add(secondary_att, mc);
	}
	
	/**
	 * @param field
	 */
	public void activateQueryField(JTextArea field) {
		if( active_att_receiver != null ) {
			active_att_receiver.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));					
		}
		if( field.isEditable() && field.isEnabled()) {
			active_att_receiver = field;
			active_att_receiver.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		}
	}

}
