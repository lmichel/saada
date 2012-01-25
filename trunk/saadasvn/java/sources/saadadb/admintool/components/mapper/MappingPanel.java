/**
 * 
 */
package saadadb.admintool.components.mapper;

import java.awt.Color;

import javax.swing.JTextArea;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.editors.MappingKWPanel;
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
	
	public void setHelpLabel(String[] phrases) {
		helpLabel.setText("");
		for( String str: phrases) {
			helpLabel.append(str + "\n");
		}
	}

	abstract public  String getText() ;
	abstract public  void setText(String text) ;
	abstract public  void reset() ;
	abstract public void setParams(ArgsParser parser) ;


}
