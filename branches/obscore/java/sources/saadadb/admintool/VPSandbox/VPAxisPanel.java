package saadadb.admintool.VPSandbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.components.mapper.MapperPrioritySelector;
import saadadb.admintool.components.mapper.MappingPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.util.RegExp;

/*
 * This class manage the expensible container corresponding to an axis
 */


//Hériter directement de MappingPanel ?
public class VPAxisPanel {
	protected JPanel axisPanel;
	protected JLabel axisLabel;
	protected CollapsiblePanel container;
	protected MyGBC ccs;
	/*
	 * Fonctionnement basé sur ClassMappingPanel() 
	 */
	public VPAxisPanel(String title)
	{

		//super(title);
		this.container = new CollapsiblePanel(title);
		axisPanel =  container.getContentPane();
		axisPanel.setLayout(new GridBagLayout());
		axisPanel.setBackground(AdminComponent.LIGHTBACKGROUND);
		ccs = new MyGBC();
		ccs.left(false);

		
	}

	/*
	 * Used to know if our Panel is collapsed or not
	 */
	public boolean isCollapsed()
	{
		return container.isCollapsed();
	}
	

	/**
	 * Allow to collapse the axis box
	 */
	public void collapse() {
		this.container.setCollapsed(true);
	}
	
	/**
	 * Allow to expand the axis box
	 */
	public void expand() {
		this.container.setCollapsed(false);
	}
	
//	
//	@Override
//	public String getText() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setText(String text) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void reset() {
//		// TODO Auto-generated method stub
//		
//	}

	

	

}
