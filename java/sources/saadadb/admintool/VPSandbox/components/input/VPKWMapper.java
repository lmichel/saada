package saadadb.admintool.VPSandbox.components.input;

import java.awt.Container;

import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.MyGBC;




/**
 * Every KwMapper represent a line in one axis
 * @author pertuy
 * @version $Id$
 */
public abstract class VPKWMapper{
	
	

	private static final long serialVersionUID = 1L;
	/*
	 * components contains the area which are concerned by the "priority" 
	 */
	//protected JComponent[] components;
	//protected VPSTOEPanel mappingPanel;
	protected VPAxisPanel axisPanel;
	protected String title;
	protected Container container;
	protected JPanel panel;
	protected JLabel fieldName;
	//protected MyGBC gbc;

	public VPKWMapper(VPAxisPanel axisPanel,String title)
	{
		this.axisPanel=axisPanel;
		this.title=title;
		fieldName=AdminComponent.getPlainLabel(title);
	
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
	public void setComponents()
	{
		panel = axisPanel.getPanel();
		MyGBC gbc = axisPanel.getGbc();
		gbc.right(false);
		panel.add(fieldName, gbc);
		gbc.next();gbc.left(true);
		panel.add(container,gbc);
		gbc.newRow();
		//container.setVisible(false);
	}

}
