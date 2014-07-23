package saadadb.admintool.VPSandbox.components.input;

import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.MyGBC;

/**
 * Every KwMapper represent a line in one axis/subpanel of the Filter form (see STOEPanel)
 * @author pertuy
 * @version $Id$
 */
public abstract class VPKWMapper{

	//The calling class
	protected VPAxisPanel axisPanel;
	
	//the panel representing the lane
	protected JPanel panel;
	
	//The name of the line
	protected JLabel fieldName;
	
	//every other component from the same line
	protected Container container;
	

	public VPKWMapper(VPAxisPanel axisPanel,String title)
	{
		this.axisPanel=axisPanel;
		fieldName=AdminComponent.getPlainLabel(title);
	}
	
	
	/**
	 * Set the components values
	 * @param text
	 */
	public abstract void setText(String text);
	
	public abstract String getText();
	
	public abstract void setEnable(boolean enable);

	/**
	 * Add the class's components to the VPAxisPriorityPanel from the constructor
	 */
	public void setComponents()
	{
		panel = axisPanel.getPanel();
		MyGBC gbc = axisPanel.getGbc();
		gbc.right(false);
		panel.add(fieldName, gbc);
		gbc.next();gbc.left(true);
		panel.add(container,gbc);
		gbc.newRow();
	}
}
