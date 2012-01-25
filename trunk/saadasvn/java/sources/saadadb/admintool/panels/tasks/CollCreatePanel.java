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
import saadadb.admintool.components.DebugButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.util.RegExp;


/**
 * @author laurent
 * @version $Id$
 *
 */
public class CollCreatePanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	protected NodeNameTextField nameField ;
	protected FreeTextField commentField;
	protected RunTaskButton runButton;
	
	public CollCreatePanel(AdminTool rootFrame, String ancestor) {
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
	protected CollCreatePanel(AdminTool rootFrame, String title,
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
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, false, false, false));
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
		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
		}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", this.nameField.getText());
		map.put("comment", this.commentField.getText());
		return map;
	}


}
