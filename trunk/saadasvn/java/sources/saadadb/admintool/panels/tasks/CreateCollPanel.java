/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ComponentTitledBorder;
import saadadb.admintool.components.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.util.RegExp;


/**
 * @author laurent
 * @version $Id$
 *
 */
public class CreateCollPanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	private NodeNameTextField nameField ;
	
	public CreateCollPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, CREATE_COLLECTION, null, ancestor);
	}
	
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
		this.setBorder(new ComponentTitledBorder(tl, this, BorderFactory.createLineBorder(Color.black)));
		//tPanel.setBackground(Color.RED);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 7, 1, 1);
		JButton jb;
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1; c.gridheight = 2;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.SOUTH;
		if( ! title.equals(ROOT_PANEL)) {

			jb = new JButton(new ImageIcon("icons/back.png"));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					rootFrame.activePanel(ancestor);				
				}});
			tPanel.add(jb, c);
			c.gridx++;
		}

		if( c != null || ! ancestor.equals(ROOT_PANEL)) {
			jb = new JButton(new ImageIcon("icons/maison.png"));
			c.weightx = 0.8; c.anchor = GridBagConstraints.WEST;
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					rootFrame.activePanel(ROOT_PANEL);				
				}});

			tPanel.add(jb, c);
		}

		this.add(tPanel);
	}


	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		
		tPanel = this.addSubPanel("Input Parameters");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Collection Name"), c);
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.8;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.WEST;
		try {
			nameField = new NodeNameTextField(16, "^" + RegExp.COLLNAME + "$");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tPanel.add(nameField, c);
		
		c.gridx = 0;
		c.gridy = 1;	
		c.weightx = 0;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Description"), c);
		
		c.gridx = 1;
		c.gridy = 1;	
		c.weightx = 0.8;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.WEST;
		JScrollPane jsc = new JScrollPane(new JTextArea(6, 24));
		tPanel.add(jsc, c);
		JButton jb = new JButton(new ImageIcon("icons/Run.png"));
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		this.setActionBar(new Component[]{jb, new JButton(new ImageIcon("icons/Ant.png"))});
//		new ChoiceItem(rootFrame, tPanel, c
//				, "Load Data", "icons/LoadData.png"
//				, new Runnable(){public void run(){
//					System.out.println("loadcdata");}});
		}

}
