package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;

public class VPAxisPriorityPanel extends VPAxisPanel {

	protected VPPriorityPanel priority;
	protected VPKwMapperPanel kwMapper;;
	protected MyGBC gbc;
	protected JPanel panel;

	/**
	 * 
	 * @param mappingPanel : Calling class
	 * @param title : Title of the axis
	 * @param classMapping : reference of the "Help" String for the axis
	 */
	public VPAxisPriorityPanel(VPSTOEPanel mappingPanel,String title,int classMapping) {
		// TODO Auto-generated constructor stub
		super(title);
		panel = new JPanel(new GridBagLayout());
		panel=this.getContainer().getContentPane();
		gbc = new MyGBC(3,3,3,3);
		helpLabel = new JLabel("?");
		//gbc.gridwidth=3;
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Priority "), gbc);
		gbc.next();
		gbc.left(false);
		
		priority=new VPPriorityPanel(mappingPanel,"plop");
		panel.add(priority,gbc);
//		panel.add(AdminComponent.getPlainLabel("Priority "), gbc);
		gbc.next();
		gbc.right(true);
		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		panel.add(helpLabel,gbc);
		gbc.newRow();
		
//		gbc.left(false);

	
	
	}


	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

}