package saadadb.admintool.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.AdminTool;

public abstract class EditPanel extends AdminPanel{
	protected JButton loadButton;
	protected JButton saveAsButton;
	protected JButton saveButton;

	public EditPanel(AdminTool rootFrame, String title, String icon, String ancestor) {
		super(rootFrame, title, icon, ancestor);
	}

	/**
	 * Add a fixed size panel filled with the buttons commanding the task
	 * @param buttons
	 */
	protected void setActionBar() {
		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
		saveAsButton = new JButton("Save as");
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setBackground(LIGHTBACKGROUND);
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0; c.gridx = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.weightx = 0;

		tPanel.add(loadButton, c);
		c.gridx++;
		tPanel.add(saveButton, c);
		c.gridx++;
		tPanel.add(saveAsButton, c);
		c.gridx++;
		
		/*
		 * Just to push all previous components to the left
		 */
		c.weightx = 1;
		tPanel.add(new JLabel(" "), c);
		this.add(tPanel);	
	}
	

}
