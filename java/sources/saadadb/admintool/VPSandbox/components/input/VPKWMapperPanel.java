package saadadb.admintool.VPSandbox.components.input;

import java.awt.Container;

import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.utils.MyGBC;




/**
 * Every KwMapper represent a line in one axis
 * @author pertuy
 * @version $Id$
 */
public abstract class VPKWMapperPanel{
	
	

	private static final long serialVersionUID = 1L;
	/*
	 * components contains the area which are concerned by the "priority" 
	 */
	//protected JComponent[] components;
	//protected VPSTOEPanel mappingPanel;
	protected VPAxisPriorityPanel axisPanel;
	protected String Label;
	protected Container container;
	protected JPanel panel;
	protected MyGBC gbc;

	public VPKWMapperPanel(VPAxisPriorityPanel axisPanel,String titre)
	{
		this.axisPanel=axisPanel;
	//this.mappingPanel=form;
		
//		argsParser=mappingPanel.getArgs();
//		if (argsParser!=null)
//			System.out.println(argsParser.getCategory());
//		else
//			System.out.println("Erreur");
		 

		//setBackground(AdminComponent.LIGHTBACKGROUND);
	}
	
	public abstract String getText();

	
//	/**
//	 * Get the input from each component
//	 */
//	public abstract  ArrayList<String> getParams();
//	
//	/**
//	 * Add the class's components to the VPAxisPriorityPanel from the constructor
//	 */
	public abstract void setComponents();

}
