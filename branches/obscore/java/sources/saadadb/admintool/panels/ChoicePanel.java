package saadadb.admintool.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ComponentTitledBorder;
import saadadb.util.Messenger;

/**
 * Super class of all panels showing a set of functions as clickable icons.
 * Sub classes of ChoicePanel never run  actions, they just propose action selectors
 * 
 * @author laurent
 * @version $Id: ChoicePanel.java 743 2013-08-01 14:22:12Z rybak.jonathan $
 *
 */
public abstract class ChoicePanel extends AdminPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChoicePanel(AdminTool rootFrame, String title, String icon, String ancestor) {
		super(rootFrame, title, icon, ancestor);
	}
	
	public void initCmdThread(){}


	/**
	 * 
	 */
	protected void setToolBar() {
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setPreferredSize(new Dimension(1000,48));
		tPanel.setMaximumSize(new Dimension(1000,48));
		tPanel.setBackground(LIGHTBACKGROUND);
		JLabel tl = getTitleLabel(" " + title + " ");
		tl.setOpaque(true);
		tl.setBorder(BorderFactory.createLineBorder(Color.black));
		Color tl_color = new Color(255, 233, 181);
		tl.setBackground(tl_color);
		this.setBorder(new ComponentTitledBorder(tl, this, BorderFactory.createLineBorder(Color.black)));
		//tPanel.setBackground(Color.RED);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 7, 3, 1);
		JButton jb;
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1; c.gridheight = 2;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.SOUTH;

		jb = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/back.png")));
		jb.setToolTipText("Back to previous panel");
		jb.setPreferredSize(new Dimension(60, 40));
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				rootFrame.activePanel(ancestor);				
			}});
		tPanel.add(jb, c);
		c.gridx++;
		if(title.equals(ROOT_PANEL))
			jb.setEnabled(false);
		else
			jb.setEnabled(true);

		if( c != null || ! ancestor.equals(ROOT_PANEL)) {
			jb = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/maison.png")));
			jb.setToolTipText("Go to Root Panel");
			jb.setPreferredSize(new Dimension(60, 40));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					rootFrame.activePanel(ROOT_PANEL);				
				}});

			tPanel.add(jb, c);
		}


		c.gridx = 2; c.gridy = 0;
		c.gridwidth = 2; c.gridheight = 1;
		c.weightx = 0.8; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		/*if( treePathLabel == null ) treePathLabel = getSubTitleLabel("");
		tPanel.add(treePathLabel, c);*/
		this.initTreePathPanel();
		treePathPanel = this.getTreePathPanel();
		treePathPanel.setBackground(AdminComponent.LIGHTBACKGROUND);
		tPanel.add(treePathPanel, c);

		c.gridx = 2; c.gridy = 1;
		c.gridwidth = 1; c.gridheight = 1;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		tPanel.add(new JLabel(new ImageIcon("icons/question.png")), c);
		
		c.gridx = 3; c.gridy = 1;
		c.weightx = 0.75; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;

		if( selectResourceLabel == null ) selectResourceLabel = getSubTitleLabel("");
		tPanel.add(selectResourceLabel, c);
		this.add(tPanel);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#active()
	 */
	public void active() {}


}
