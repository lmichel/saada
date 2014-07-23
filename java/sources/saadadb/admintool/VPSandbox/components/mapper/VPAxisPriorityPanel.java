package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.input.VPKWMapper;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.command.ArgsParser;

/**
 * This class represent an Axis/subpanel dependent of a priority selector
 * @author pertuy
 * @version $Id$
 */
public abstract class  VPAxisPriorityPanel extends VPAxisPanel {

	protected VPPriorityPanel priority;
	protected VPKWMapper kwMapper;;
	protected JPanel panel;

	/**
	 * 
	 * @param mappingPanel : Calling class
	 * @param title : Title of the axis
	 * @param classMapping : reference of the "Help" String for the axis
	 */
	public  VPAxisPriorityPanel(VPSTOEPanel mappingPanel,String title,int classMapping) {
		super(mappingPanel,title);
		panel = new JPanel(new GridBagLayout());
		panel=this.getContainer().getContentPane();
		helpLabel = new JLabel("?");
		
		gbc.right(false);
		
		panel.add(AdminComponent.getPlainLabel("Priority "), gbc);
		
		gbc.next();
		gbc.left(false);
		
		//Instantiate a priority mapper
		priority=new VPPriorityPanel(mappingPanel,"Priority");
		panel.add(priority,gbc);
		
		gbc.next();
		gbc.right(true);
		
		//Instantiate the help area
		helpLabel=setHelpLabel(classMapping);
		panel.add(helpLabel,gbc);
		
		gbc.newRow();	
	}

	/**
	 * Set the "Default" radio button selected
	 */
	public void reset()
	{
		priority.noBtn.setSelected(true);
	}

	public JPanel getPanel() {
		return panel;
	}
	
	/**
	 * Check if all the axis fields are empty
	 * @param ap
	 * @return
	 */
	public abstract boolean fieldsEmpty(ArgsParser ap);

	public VPPriorityPanel getPriority() {
		return priority;
	}
}
