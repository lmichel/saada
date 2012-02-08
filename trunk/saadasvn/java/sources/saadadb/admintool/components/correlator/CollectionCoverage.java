package saadadb.admintool.components.correlator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;


public class CollectionCoverage extends CollapsiblePanel {
	private JTextField primary_classes = new JTextField(20);
	private JComboBox primary_combo = new JComboBox();
	private JTextField secondary_classes = new JTextField(20);
	private JComboBox secondary_combo = new JComboBox();
	private RelationPopulatePanel taskPanel;
	
	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public CollectionCoverage(RelationPopulatePanel taskPanel, Component toActive) {
		super("Limit the join to a subset of end-point collections");
		this.taskPanel = taskPanel;
		
		primary_classes = new JTextField(20);
		primary_classes.setToolTipText("Subset of the primary collection covered by the join query");

		primary_combo = new JComboBox();
		primary_combo.setToolTipText("List of sub-classes of the primary collection - transfered to the text field when selected");
		primary_combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( primary_combo.getSelectedItem() == null ) {
					return;
				}
				String si = primary_combo.getSelectedItem().toString();
				String ct = primary_classes.getText().trim();
				if( !ct.equals(si) && !ct.matches(".*,\\s*" + si + ".*") && !ct.matches(".*" + si + "\\s*,.*")) {
					if( si.startsWith("ANY")) {
						primary_classes.setText("*");
					} else if( ct.length() == 0 || ct.equals("*")){
						primary_classes.setText(si);			
					} else {
						primary_classes.setText((ct + "," + si).replaceAll(",,", ","));			
					}
					CollectionCoverage.this.taskPanel.updateAvailableAttributes("p.");
				}
			}	
		});

		secondary_classes = new JTextField(20);
		secondary_classes.setToolTipText("Subset of the secondary collection covered by the join query");

		secondary_combo = new JComboBox();
		secondary_combo.setToolTipText("List of sub-classes of the secondary collection - transfered to the text field when selected");

		MyGBC mc = new MyGBC(5,5,5,5); mc.anchor = GridBagConstraints.NORTH;
		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		mc.right(false);
		panel.add(AdminComponent.getPlainLabel("Primary"), mc);
		mc.next();mc.left(false);
		panel.add(primary_combo, mc);
		mc.rowEnd();
		panel.add(primary_classes, mc);
		mc.newRow();mc.right(false);
		panel.add(AdminComponent.getPlainLabel("Secondary"), mc);
		mc.next();mc.left(false);
		panel.add(secondary_combo, mc);
		mc.rowEnd();
		panel.add(secondary_classes, mc);
		mc.newRow();mc.right(false);

	}
	
	/**
	 * @return
	 */
	public String getPrimaryCLasses() {
		return primary_classes.getText();
	}
	/**
	 * @return
	 */
	public String getSecondaryCLasses() {
		return secondary_classes.getText();
	}

}
