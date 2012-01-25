/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadEmptyCategory;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaCollection;
import saadadb.util.RegExp;


/**
 * @author laurent
 * @version $Id$
 *
 */
public class CategoryEmptyPanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	protected JTextField nameField ;
	protected JTextField categoryField ;
	protected RunTaskButton runButton;

	public CategoryEmptyPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_CATEGORY, null, ancestor);
		cmdThread = new ThreadEmptyCategory(rootFrame);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected CategoryEmptyPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.cmdThread = cmdThread;
	}

	public void initCmdThread() {
		cmdThread = new ThreadEmptyCategory(rootFrame);
	}

	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( dataTreePath.isCollectionLevel() ) {
			showInputError(rootFrame, "No category (IMAGE,ENTRY....) in the selected data tree node");
		}
		else {
			super.setDataTreePath(dataTreePath);
			treePathLabel.setText(dataTreePath.collection + "." + dataTreePath.category);
			MetaCollection mc;
			try {
				mc = Database.getCachemeta().getCollection(dataTreePath.collection);
				nameField.setText(mc.getName());
				if( "ENTRY".equals(dataTreePath.category)) {
					categoryField.setText("TABLE");
				}
				else {
					categoryField.setText(dataTreePath.category);
				}
			} catch (FatalException e) {
				showFatalError(rootFrame, e);
			}
		}
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
		tPanel.add(getPlainLabel("Category"), c);

		c.gridx = 1;
		c.gridy = 1;	
		c.weightx = 0.8;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.WEST;
		categoryField = new JTextField(16);
		tPanel.add(categoryField, c);
		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});


		nameField.setEditable(false);
		categoryField.setEditable(false);
	}



	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("collection", this.nameField.getText());
		map.put("category", this.categoryField.getText());
		return map;
	}


}
