package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import saadadb.admintool.utils.MyGBC;


public abstract class VPKwMapperPanel{
	
	/*
	 * components contains the area which are concerned by the "priority" 
	 */
	protected JComponent[] components;
	protected MyGBC gbc;
	protected JPanel panel;

	public VPKwMapperPanel(VPAxisPriorityPanel axisPanel)
	{
		gbc = axisPanel.gbc;
		panel = axisPanel.panel;

		//setBackground(AdminComponent.LIGHTBACKGROUND);
	}
	
	
	/**
	 * Add the class's components to the VPAxisPriorityPanel from the constructor
	 */
	public abstract void setComponents();

}
