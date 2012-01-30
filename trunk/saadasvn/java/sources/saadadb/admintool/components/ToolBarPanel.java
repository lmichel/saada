package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.panels.AdminPanel;

public class ToolBarPanel extends JPanel {

	public ToolBarPanel(final AdminPanel adminLabel, boolean withTreePath, boolean withSelectedResource , boolean withTaskLabel) {
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(1000,48));
		this.setMaximumSize(new Dimension(1000,48));
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		JLabel tl = AdminComponent.getTitleLabel(" " + adminLabel.getTitle() + " ");
		tl.setOpaque(true);
		tl.setBorder(BorderFactory.createLineBorder(Color.black));
		adminLabel.setBorder(new ComponentTitledBorder(tl, this, BorderFactory.createLineBorder(Color.black)));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 7, 1, 1);
		
		JButton jb;
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1; c.gridheight = 0;
		if( withTreePath) c.gridheight++;
		if( withSelectedResource) c.gridheight++;
		if( withTaskLabel) c.gridheight++;
		if( c.gridheight == 0 ) c.gridheight = 1;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.SOUTH;
		if( ! adminLabel.getTitle().equals(AdminComponent.ROOT_PANEL)) {

			jb = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/back.png")));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					adminLabel.rootFrame.activePanel(adminLabel.getAncestor());		
				}});
			this.add(jb, c);
			c.gridx++;
		}

		if( c != null || ! adminLabel.getAncestor().equals(AdminComponent.ROOT_PANEL)) {
			jb = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/maison.png")));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					adminLabel.rootFrame.activePanel(AdminComponent.ROOT_PANEL);				
				}});

			this.add(jb, c);
		}

		if( withTreePath ) {

			c.gridx = 2; c.gridy = 0;
			c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.8; c.weighty = 1;
			c.anchor = GridBagConstraints.LINE_START;
			this.add(adminLabel.getTreePathLabel(), c);
		}


		
		if( withSelectedResource ) {
			c.gridx = 2; c.gridy = 1;
			c.gridwidth = 1; c.gridheight = 1;
			c.weightx = 0; c.weighty = 1;
			c.anchor = GridBagConstraints.LINE_START;
			this.add(new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/question.png"))), c);
			c.gridx = 3; c.gridy = 1;
			c.weightx = 0.75; c.weighty = 1;
			c.anchor = GridBagConstraints.LINE_START;
			this.add(adminLabel.getSelectResourceLabel(), c);
		}
		
		if( withTaskLabel ) {

		c.gridx = 2; c.gridy = 2;
		c.gridwidth = 2; c.gridheight = 1;
		c.weightx = 0.8; c.weighty = 1;
		this.add(adminLabel.getCurrentTaskLabel(), c);
		}

	}

}
