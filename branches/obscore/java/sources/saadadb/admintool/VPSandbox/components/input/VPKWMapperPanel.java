package saadadb.admintool.VPSandbox.components.input;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.dnd.TreepathDropableTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;


public abstract class VPKWMapperPanel extends TreepathDropableTextField{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * components contains the area which are concerned by the "priority" 
	 */
//	protected JComponent[] components;
	protected VPSTOEPanel mappingPanel;
	//protected VPAxisPriorityPanel axisPanel;

	public VPKWMapperPanel(VPSTOEPanel form)
	{
		//this.axisPanel=axisPanel;
		this.mappingPanel=form;
		
//		argsParser=mappingPanel.getArgs();
//		if (argsParser!=null)
//			System.out.println(argsParser.getCategory());
//		else
//			System.out.println("Erreur");
		 
		 
		


		//setBackground(AdminComponent.LIGHTBACKGROUND);
	}
	
//	/**
//	 * Get the input from each component
//	 */
//	public abstract  ArrayList<String> getParams();
//	
//	/**
//	 * Add the class's components to the VPAxisPriorityPanel from the constructor
//	 */
//	public abstract void setComponents();

}
