package saadadb.admintool.VPSandbox.components.mapper;

import javax.swing.JComponent;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;


public abstract class VPKwMapperPanel{
	
	/*
	 * components contains the area which are concerned by the "priority" 
	 */
	protected JComponent[] components;
	protected MyGBC gbc;
	protected JPanel panel;
	protected ArgsParser argsParser;

	public VPKwMapperPanel(VPSTOEPanel mappingPanel, VPAxisPriorityPanel axisPanel) throws FatalException
	{
		gbc = axisPanel.gbc;
		panel = axisPanel.panel;
		
//		argsParser=mappingPanel.getArgs();
//		if (argsParser!=null)
//			System.out.println(argsParser.getCategory());
//		else
//			System.out.println("Erreur");
		 
		 
		


		//setBackground(AdminComponent.LIGHTBACKGROUND);
	}
	
	
	/**
	 * Add the class's components to the VPAxisPriorityPanel from the constructor
	 */
	public abstract void setComponents();

}
