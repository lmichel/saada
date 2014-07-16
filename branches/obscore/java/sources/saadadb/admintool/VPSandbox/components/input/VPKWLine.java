package saadadb.admintool.VPSandbox.components.input;

import javax.swing.JComponent;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;


/**
 * Class not used right now, could be a more generalist replacement for the class type "VPKWNamedFieldnXXXX"
 * 
 * @author pertuy
 * @version $Id$
 */
public class VPKWLine extends VPKWNamedField {

	private JComponent component;
	
	public VPKWLine(VPAxisPriorityPanel axisPanel, String name,
			JTextField mappingTextField,JComponent component) {
		super(axisPanel, name, mappingTextField);
		this.component=component;
		container.add(this.component);
	}

	public JComponent getComponent() {
		return component;
	}
	
	

}
