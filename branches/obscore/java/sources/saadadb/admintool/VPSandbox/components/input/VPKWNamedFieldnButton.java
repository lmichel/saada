package saadadb.admintool.VPSandbox.components.input;

import javax.swing.JButton;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;

public class VPKWNamedFieldnButton extends VPKWNamedField {

	private JButton button;
	
	public VPKWNamedFieldnButton(VPAxisPriorityPanel axisPanel, String name,
			JTextField mappingTextField, JButton button) {
		super(axisPanel, name, mappingTextField);
		this.button=button;
		container.add(button);
	}

	public JButton getButton() {
		return button;
	}
	
	
	public void reset()
	{
		super.reset();
		button.setEnabled(false);
	}

}
