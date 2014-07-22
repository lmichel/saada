package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.QueryException;

/**
 * This class manage the collapsible container corresponding to an axis (subpanel of the form panel, see class STOEPanel)
 */

public abstract class VPAxisPanel {

	public final static int SUBPANELTITLECOLOR = CollapsiblePanel.COLLAPSIBLEPANELTITLECOLOR;
	public final static String SUBPANELHEADER="Header Mapper";
	public final static String SUBPANELENTRY="Entry Mapper";
	public final static int SEPARATORCOLOR = 0xBBBEBF;

	protected MyGBC gbc;
	protected JPanel axisPanel;
	protected JLabel axisLabel;
	private CollapsiblePanel container;
	private String helpString;
	protected JLabel helpLabel;
	protected VPSTOEPanel mappingPanel;

	//List of component dependent of a the priority or a fusion selector(see ClassMappingPanel) in the axis
	protected ArrayList<JComponent> axisPriorityComponents;

	/**
	 * Instantiate a collapsible panel and a GridBagLayout. The collapsible panel contains each field of the corresponding subPanel/axis
	 * @param mappingPanel
	 * @param title
	 */
	public VPAxisPanel(VPSTOEPanel mappingPanel,String title)
	{
		this.setContainer(new CollapsiblePanel(title));
		gbc = new MyGBC(3,3,3,3);
		axisPanel = getContainer().getContentPane();
		axisPanel.setLayout(new GridBagLayout());
		axisPanel.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.mappingPanel=mappingPanel;
	}

	/**
	 * Get the parameters from the axis fields
	 * @return
	 * @throws QueryException 
	 * @throws Exception 
	 *  
	 */
	public abstract ArrayList<String> getAxisParams();

	/**
	 * Create and return a JLabel which display some help if you put your mouse on it
	 * @param textKey
	 * @return helpLabel
	 */

	/*
	 *\/!\La classe HelpDesk DEVRA être modifiée pour correspondre au nouveau formulaire !
	 */
	public JLabel setHelpLabel(int textKey) {
		JLabel helpLabel = new JLabel("?");
		helpString=new String("");
		String[] phrases = HelpDesk.get(textKey);
		if( phrases != null ) {
			for( String str: phrases) {
				helpString=helpString+str + "\n";
			}
		}
		helpLabel.setToolTipText(helpString);
		return helpLabel;
	}


	/**
	 * Allow to collapse the axis' box
	 */
	public void collapse() 
	{
		this.getContainer().setCollapsed(true);
	}

	/**
	 * Allow to expand the axis' box
	 */
	public void expand() {
		this.getContainer().setCollapsed(false);
	}


	public CollapsiblePanel getContainer() {
		return container;
	}

	public void setContainer(CollapsiblePanel container) {
		this.container = container;
	}

	/**
	 * Check the Axis parameter. Return the corresponding error message or an empty String if everything's ok
	 * @return
	 */
	public abstract String checkAxisParams();

	/**
	 * surround the panel with red borders
	 * @param onError
	 */
	public void setOnError(boolean onError) {
		container.setOnError(onError);
	}

	public JPanel getPanel() {
		return axisPanel;
	}

	public MyGBC getGbc() {
		return gbc;
	}
}
