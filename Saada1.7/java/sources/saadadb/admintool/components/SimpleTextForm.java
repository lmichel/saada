/**
 * 
 */
package saadadb.admintool.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import saadadb.admintool.utils.MyGBC;

/**
 * @author laurentmichel
 *
 */
public class SimpleTextForm extends CollapsiblePanel {

	private static final long serialVersionUID = 1L;
	
	public SimpleTextForm(String text, String[] labels, Component[] components) {
		super(text);
		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		MyGBC mgbc = new MyGBC(5,5,5,5); mgbc.anchor = GridBagConstraints.NORTH;
		for( int i=0 ; i<labels.length ; i++ ) {
			mgbc.right(false);
			panel.add(AdminComponent.getPlainLabel(labels[i]), mgbc);
			mgbc.rowEnd();mgbc.left(true);
			panel.add(components[i], mgbc);
			mgbc.newRow();
		}


	}


}
