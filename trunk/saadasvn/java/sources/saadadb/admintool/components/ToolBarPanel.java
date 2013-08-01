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

	public ToolBarPanel(final AdminPanel adminPanel, boolean withTreePath, boolean withSelectedResource , boolean withTaskLabel) {
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(new Dimension(1000,48));
		this.setMaximumSize(new Dimension(1000,48));
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		JLabel tl = AdminComponent.getTitleLabel(" " + adminPanel.getTitle() + " ");
		tl.setOpaque(true);
		tl.setBorder(BorderFactory.createLineBorder(Color.black));
		Color tl_color = new Color(255, 233, 181);
		tl.setBackground(tl_color);
		adminPanel.setBorder(new ComponentTitledBorder(tl, this, BorderFactory.createLineBorder(Color.black)));
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
		if( ! adminPanel.getTitle().equals(AdminComponent.ROOT_PANEL)) {

			jb = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/back.png")));
			jb.setToolTipText("Back to previous panel");
			jb.setPreferredSize(new Dimension(60, 40));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					adminPanel.rootFrame.activePanel(adminPanel.getAncestor());		
				}});
			this.add(jb, c);
			c.gridx++;
		}

		if( c != null || ! adminPanel.getAncestor().equals(AdminComponent.ROOT_PANEL)) {
			jb = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/maison.png")));
			jb.setToolTipText("Go to Root Panel");
			jb.setPreferredSize(new Dimension(60, 40));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					adminPanel.rootFrame.activePanel(AdminComponent.ROOT_PANEL);				
				}});

			this.add(jb, c);
		}
		/*
		 * Put an empty label to push button on the left
		 */
		if( !withTreePath && ! withSelectedResource && ! withTaskLabel) {
			c.gridx = 2; c.gridy = 0;
			c.gridwidth = 2; c.gridheight = 1;
			c.weightx = 0.8; c.weighty = 1;
			c.anchor = GridBagConstraints.LINE_START;
			this.add(new JLabel(""), c);
		}
		else {
			if( withTreePath ) {
				c.gridx = 2; c.gridy = 0;
				c.gridwidth = 2; c.gridheight = 1;
				c.weightx = 0.8; c.weighty = 1;
				c.anchor = GridBagConstraints.LINE_START;
				this.add(adminPanel.getTreePathPanel(), c);
				//this.add(adminPanel.getTreePathLabel(), c);
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
				this.add(adminPanel.getSelectResourceLabel(), c);
			}		
			if( withTaskLabel ) {
				c.gridx = 2; c.gridy = 2;
				c.gridwidth = 2; c.gridheight = 1;
				c.weightx = 0.8; c.weighty = 1;
				this.add(adminPanel.getCurrentTaskLabel(), c);
			}
		}

	}

}
