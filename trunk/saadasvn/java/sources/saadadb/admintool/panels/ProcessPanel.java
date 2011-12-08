package saadadb.admintool.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ComponentTitledBorder;

public  class ProcessPanel extends AdminPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProcessPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, PROCESS_PANEL, null, ancestor);
	}

	/**
	 * 
	 */
	protected void setToolBar() {
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setPreferredSize(new Dimension(1000,64));
		tPanel.setMaximumSize(new Dimension(1000,64));
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
		c.gridwidth = 1; c.gridheight = 3;
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
		if( treePathLabel == null ) treePathLabel = getSubTitleLabel("TreePath");
		tPanel.add(treePathLabel, c);

		c.gridx = 2; c.gridy = 1;
		c.gridwidth = 1; c.gridheight = 1;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		tPanel.add(new JLabel(new ImageIcon("icons/question.png")), c);
		
		c.gridx = 3; c.gridy = 1;
		c.weightx = 0.75; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		if( selectResourceLabel == null ) selectResourceLabel = getSubTitleLabel("No selected resource");
		tPanel.add(selectResourceLabel, c);

		c.gridx = 2; c.gridy = 2;
		c.gridwidth = 2; c.gridheight = 1;
		c.weightx = 0.8; c.weighty = 1;
		if( currentTaskLabel == null ) currentTaskLabel = getSubTitleLabel("No task");
		tPanel.add(currentTaskLabel, c);

		this.add(tPanel);
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel = this.addSubPanel("Console");
		JScrollPane jcp = new JScrollPane();
		JTextArea jte = new JTextArea();
		jcp.add(jte);
		jte.setBackground(IVORY);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;	
		c.weightx = 1; c.weighty = 1;	
		c.fill = GridBagConstraints.BOTH;

		tPanel.add(jcp, c);
		jcp.setPreferredSize(tPanel.getPreferredSize());
		 c = new GridBagConstraints();
		 c.gridx = 0; c.gridy = 0;	
		c.weightx = 0; c.weighty = 0;	
		c.anchor = GridBagConstraints.PAGE_END;
		
		
		tPanel = this.addSubPanel("Process Control");
		//tPanel.setPreferredSize(new Dimension(1000, 36));
		tPanel.setMaximumSize(new Dimension(1000, 36));
		JButton jb = new JButton(new ImageIcon("icons/Run.png"));
		tPanel.add(jb, c);
		c.gridx++;
		jb = new JButton(new ImageIcon("icons/Abort.png"));
		tPanel.add(jb, c);
		c.gridx++;
		jb = new JButton(new ImageIcon("icons/DebugMode.png"));
		tPanel.add(jb, c);
		c.gridx++;
		jb = new JButton(new ImageIcon("icons/Ant.png"));
		tPanel.add(jb, c);
		c.gridx++;
		
		c.weightx = 1; c.weighty = 0;	
		c.anchor= GridBagConstraints.SOUTHEAST;
		JPanel stsPanel = new JPanel();
		Border empty = new EmptyBorder(new Insets(2,2,2,2));

		stsPanel.setLayout(new BoxLayout(stsPanel, BoxLayout.LINE_AXIS));
		JLabel jl = new JLabel(new ImageIcon("icons/Processor.png"));
		jl.setBorder(empty);
		stsPanel.add(jl);
		
		jl = new JLabel(new ImageIcon("icons/Disk.png"));
		jl.setBorder(empty);	
		stsPanel.add(jl);
		
		jl = new JLabel(new ImageIcon("icons/Database.png"));
		jl.setBorder(empty);	
		stsPanel.add(jl);
		
		stsPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

		tPanel.add(stsPanel, c);		
	}


}
