/**
 * 
 */
package saadadb.admintool.components.mapper;

import java.awt.Color;

import javax.swing.JTextArea;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.command.ArgsParser;

/**
 * @author laurentmichel
 *
 */
public abstract class MappingPanel {
	public final CollapsiblePanel container;
	public final MappingKWPanel mappingPanel;
	public String[] helpText;
	public JTextArea helpLabel;

	public MappingPanel(MappingKWPanel mappingPanel, String title) {
		this.container = new CollapsiblePanel(title);	
		this.mappingPanel = mappingPanel;
		helpLabel = new JTextArea();
		helpLabel.setEditable(false);	
		helpLabel.setFont(AdminComponent.helpFont);		
		helpLabel.setBackground(AdminComponent.LIGHTBACKGROUND);
		helpLabel.setForeground(Color.GRAY);

	}

	public void setHelpLabel(int textKey) {
		helpLabel.setText("");
		String[] phrases = HelpDesk.get(textKey);
		if( phrases != null ) {
			for( String str: phrases) {
				helpLabel.append(str + "\n");
			}
		}
	}

	/**
	 * @param param_compo
	 * @return
	 */
	protected static final String getMergedComponent(String[] param_compo) {
		String param = "";
		if( param_compo != null ) {
			for( int i=0 ; i<param_compo.length; i++ ) {
				if( i >= 1) param += ",";
				param += param_compo[i];
			}
		}
		return param;
	}

	public boolean valid() {
		return this.getText().matches("(('.*')|([^\\s]+))([,:;\\.](('.*')|([^\\s]+)))*");
	}
	/**
	 * @param onError
	 */
	public void setOnError(boolean onError) {
		container.setOnError(onError);
	}
	abstract public  String getText() ;
	abstract public  void setText(String text) ;
	abstract public  void reset() ;


}
