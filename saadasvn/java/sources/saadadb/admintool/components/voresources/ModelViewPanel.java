package saadadb.admintool.components.voresources;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.editors.TAPServicePanel;
import saadadb.admintool.panels.tasks.ObscoreMapperPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.SaadaException;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capabilities;


public class ModelViewPanel extends JPanel {
	private ObscoreMapperPanel obscoreMapperPanel;
	private ModelFieldList resourceList;
	private JEditorPane descPanel;
	JTextField mapField;
	private JButton checkButton;
	
	
	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws Exception 
	 */
	public ModelViewPanel(ObscoreMapperPanel obscoreMapperPanel) throws Exception {
		this.obscoreMapperPanel = obscoreMapperPanel;
		this.resourceList = new ModelFieldList(this);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		
		MyGBC mgbc = new MyGBC(5,5,5,5);
		mgbc.gridx = 0; mgbc.gridy = 0;mgbc.weightx = 1; mgbc.weighty = 1;mgbc.gridheight = 5;
		this.setLayout(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(this.resourceList);
		jsp.setPreferredSize(new Dimension(250,300));
		this.add(jsp, mgbc);
		mgbc.next();mgbc.gridheight = 1;
		this.add(new JLabel("Field Description"), mgbc);

		mgbc.newRow(); mgbc.gridx = 1;
		descPanel = new JEditorPane("text/html", "");
		descPanel.setPreferredSize(new Dimension(250, 170));
		descPanel.setEditable(false);
		this.add(new JScrollPane(this.descPanel), mgbc);
		
		mgbc.newRow(); mgbc.gridx = 1;
		this.add(new JLabel("Mapping Statement"), mgbc);
		
		mgbc.newRow(); mgbc.gridx = 1;
		mapField = new JTextField(24);
		this.add(mapField, mgbc);
		
		mgbc.newRow(); mgbc.gridx = 1;
		checkButton = new JButton("Check and Store");
		this.add(checkButton, mgbc);

		

	}
	
	public boolean setDataTreePath(DataTreePath dataTreePath) throws SaadaException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(obscoreMapperPanel.rootFrame, "Selet a data tree node either at category or class level");
			return false;
		}
		return true;
	}
	public void setDescription(String description){
		this.descPanel.setText(description);
	}
	public String getDescription() {
		return this.descPanel.getText();
	}
	public boolean isEmpty() {
		return (this.resourceList.items.size() == 0) ? true: false;
	}
	public VOResource getVor() {
		return obscoreMapperPanel.getVor();
	}

	public void setUtypeHandler(UTypeHandler uth, String mappingStmt) {
		this.mapField.setText(mappingStmt);
		this.descPanel.setText(
				"<table><TR><TD ALIGN=RIGHT><B>Name</TD><TD>" + uth.getNickname() + "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>Type</TD><TD>" + uth.getType()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>UType</TD><TD>" + uth.getUtype()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>Unit</TD><TD>" + uth.getUnit()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>Descrption</TD><TD>" + uth.getComment()+ "</TD></TR></TABLE><BR>"
		);	
	}
}
