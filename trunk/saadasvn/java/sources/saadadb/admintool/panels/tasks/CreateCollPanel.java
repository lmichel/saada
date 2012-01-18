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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadCreateCollection;
import saadadb.admintool.cmdthread.ThreadDropCollection;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.ComponentTitledBorder;
import saadadb.admintool.components.FreeTextField;
import saadadb.admintool.components.NodeNameTextField;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.util.RegExp;


/**
 * @author laurent
 * @version $Id$
 *
 */
public class CreateCollPanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	protected NodeNameTextField nameField ;
	protected FreeTextField commentField;
	protected RunTaskButton runButton;
	
	public CreateCollPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, CREATE_COLLECTION, null, ancestor);
		cmdThread = new ThreadCreateCollection(rootFrame);
	}
	
	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected CreateCollPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.cmdThread = cmdThread;
	}

	public void initCmdThread() {
		cmdThread = new ThreadCreateCollection(rootFrame);
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

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
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
		runButton = new RunTaskButton(this);
		try {
			nameField = new NodeNameTextField(16, "^" + RegExp.COLLNAME + "$", runButton);
			nameField.addPropertyChangeListener("value", this);
		} catch (ParseException e1) {
			AdminComponent.showFatalError(rootFrame, e1);
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
		commentField = new FreeTextField(6, 24);
		tPanel.add(commentField.getPanel(), c);
		this.setActionBar(new Component[]{runButton, (new AntButton(this))});
		}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", this.nameField.getText());
		map.put("comment", this.commentField.getText());
		return map;
	}


}
