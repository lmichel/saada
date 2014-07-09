package saadadb.admintool.VPSandbox.components.input;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.components.input.MappingTextField;
import saadadb.admintool.utils.MyGBC;
import saadadb.enums.DataMapLevel;

/**
 * Represent a line in an Axis panel composed of a name and a appendTextField (KeyWord mapped field)
 * @author pertuy
 * @version $Id$
 */

public class VPKWNamedField extends VPKWMapperPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MappingTextField field;
	private String nameField;
	
	public VPKWNamedField(VPAxisPriorityPanel axisPanel,String name,MappingTextField mappingTextField) {
		super(axisPanel,name);
		MyGBC gbc = axisPanel.getGbc();
		container=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0));
		this.field =  mappingTextField;//new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,bg);
		this.field.setColumns(AdminComponent.STRING_FIELD_NAME);
		container.add(mappingTextField);
		nameField=name;


		
		//gbc.newRow();
		
		// TODO Auto-generated constructor stub
	}


	
	public String getText()
	{
		return field.getText();
	}

	public MappingTextField getField()
	{
		return field;
	}
	
	
	public void setComponents()
	{
		panel = axisPanel.getPanel();
		MyGBC gbc = axisPanel.getGbc();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel(nameField), gbc);
		gbc.next();gbc.left(true);
		panel.add(container,gbc);
		gbc.newRow();
	}


}
