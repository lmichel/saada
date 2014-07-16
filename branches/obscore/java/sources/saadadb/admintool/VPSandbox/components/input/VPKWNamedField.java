package saadadb.admintool.VPSandbox.components.input;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPanel;
import saadadb.admintool.components.AdminComponent;

/**
 * Represent a line in an Axis panel composed of a name and a appendTextField (KeyWord mapped field)
 * @author pertuy
 * @version $Id$
 */

public class VPKWNamedField extends VPKWMapper {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField field;

	
	public VPKWNamedField(VPAxisPanel axisPanel,String name,JTextField mappingTextField) {
		super(axisPanel,name);
		container=new JPanel(new FlowLayout(FlowLayout.CENTER,3,0));
		this.field =  mappingTextField;//new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,bg);
		this.field.setColumns(AdminComponent.STRING_FIELD_NAME);
		container.add(mappingTextField);



		
		//gbc.newRow();
		
		// TODO Auto-generated constructor stub
	}


	public void reset()
	{
		field.setText("");
	}
	
	public String getText()
	{
		return field.getText();
	}

	public JTextField getField()
	{
		return field;
	}
	
	
//	public void setComponents()
//	{
//		panel = axisPanel.getPanel();
//		MyGBC gbc = axisPanel.getGbc();
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel(nameField), gbc);
//		gbc.next();gbc.left(true);
//		panel.add(container,gbc);
//		gbc.newRow();
//	}


}
