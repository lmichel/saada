package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;

/*
 * This class manage the expensible container corresponding to an axis
 */


//Hériter directement de MappingPanel ?
public abstract class VPAxisPanel {
	protected JPanel axisPanel;
	protected JLabel axisLabel;
	private CollapsiblePanel container;
	protected MyGBC ccs;
	private String helpString;
	protected JLabel helpLabel;
	/*
	 * Fonctionnement basé sur ClassMappingPanel() 
	 */
	public VPAxisPanel(String title)
	{

		//super(title);
		this.setContainer(new CollapsiblePanel(title));
		axisPanel =  getContainer().getContentPane();
		axisPanel.setLayout(new GridBagLayout());
		axisPanel.setBackground(AdminComponent.LIGHTBACKGROUND);
		ccs = new MyGBC();
		ccs.left(false);

		

	}
	
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

	/*
	 * Used to know if our Panel is collapsed or not
	 */
	public boolean isCollapsed()
	{
		return getContainer().isCollapsed();
	}
	

	/**
	 * Allow to collapse the axis box
	 */
	public void collapse() {
		this.getContainer().setCollapsed(true);
	}
	
	/**
	 * Allow to expand the axis box
	 */
	public void expand() {
		this.getContainer().setCollapsed(false);
	}
	
	
	public boolean valid() {
		return this.getText().matches("(('.*')|([^\\s]+))([,:;\\.](('.*')|([^\\s]+)))*");
	}
	
	abstract public  String getText() ;

	public CollapsiblePanel getContainer() {
		return container;
	}

	public void setContainer(CollapsiblePanel container) {
		this.container = container;
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
