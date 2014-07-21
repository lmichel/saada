package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
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
 * This class manage the expensible container corresponding to an axis
 */


//Hériter directement de MappingPanel ?
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
	
	//List of component which depend of the priority (or fusion selector) in the axis
	protected ArrayList<JComponent> axisPriorityComponents;
	/*
	 * Fonctionnement basé sur ClassMappingPanel() 
	 */
	public VPAxisPanel(VPSTOEPanel mappingPanel,String title)
	{

		//super(title);
		this.setContainer(new CollapsiblePanel(title));
		gbc = new MyGBC(3,3,3,3);
		axisPanel = getContainer().getContentPane();
		axisPanel.setLayout(new GridBagLayout());
		axisPanel.setBackground(AdminComponent.LIGHTBACKGROUND);
		
		
		//Temporaire
//		JLabel ds = AdminComponent.getPlainLabel("<HTML><A HREF=>TEST</A>");
//		ds.addMouseListener(new MouseAdapter(){
//			public void mouseReleased(MouseEvent e) {
//				getArgs();
//			}});
//		axisPanel.add(ds);
	
		this.mappingPanel=mappingPanel;
		//container.setBorder(BorderFactory.createLineBorder(Color.red));
//		ccs = new MyGBC();
//		ccs.left(false);

		

	}
	
	/**
	 * Get the values from the axis fields
	 * @return
	 * @throws QueryException 
	 * @throws Exception 
	 *  
	 */
	public abstract ArrayList<String> getAxisParams();

	
	
	/**
	 * Used to get the Args from the main panel
	 */
//	public String[] getArgs()
//	{
//		String[] args = null ;
//		if(mappingPanel.checkParams())
//		{
//			ArgsParser args_parser = mappingPanel.getArgsParser();
//			if( args_parser != null ) {
//				args = args_parser.getArgs();
//				String summary = "";
//				for( int i=0 ; i<args.length ; i++ ) {
//					summary += args[i] + "\n";
//				}
//				System.out.println(summary);
//			}
//		}
//		return args;
//	}
	
	/**
	 * Create and return a JLabel which display some help if you put your mouse on it
	 * @param textKey
	 * @return helpLabel
	 */
	
	/*
	 *\/!\La classe HelpDesk DEVRA être modifiée pour correspondre au nouveau formulaire !
	 */
	public JLabel setHelpLabel(int textKey) {
		//Image ?
		JLabel helpLabel = new JLabel("?");
		
		helpString=new String("");
		String[] phrases = HelpDesk.get(textKey);
		if( phrases != null ) {
			for( String str: phrases) {
				//helpLabel.append(str + "\n");
				helpString=helpString+str + "\n";
			}
		}
	
		//Pour afficher un tooltip avec des retours à la ligne, texte en gras... il est possible de le formater en html
		//ceci devra être fait dans les String HelpDesk correspondant
		helpLabel.setToolTipText(helpString);
		
		return helpLabel;
	}



	/**
	 * Allow to collapse the axis box
	 */
	public void collapse() 
	{
		this.getContainer().setCollapsed(true);
	}
	
	/**
	 * Allow to expand the axis box
	 */
	public void expand() {
		this.getContainer().setCollapsed(false);
	}
	
	
//	public boolean valid() {
//		return this.getText().matches("(('.*')|([^\\s]+))([,:;\\.](('.*')|([^\\s]+)))*");
//	}
//	
//	public String getText() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public CollapsiblePanel getContainer() {
		return container;
	}

	public void setContainer(CollapsiblePanel container) {
		this.container = container;
	}

	public void setText(String text) {
		// TODO Auto-generated method stub
		
	}


	
	/**
	 * Check the Axis parameter. Return the corresponding error message or an empty String if everything's ok
	 * @return
	 */
	public abstract String checkAxisParams();
	
	public void setOnError(boolean onError) {
		container.setOnError(onError);
	}

	public JPanel getPanel() {
		// TODO Auto-generated method stub
		return axisPanel;
	}

	public MyGBC getGbc() {
		// TODO Auto-generated method stub
		return gbc;
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
