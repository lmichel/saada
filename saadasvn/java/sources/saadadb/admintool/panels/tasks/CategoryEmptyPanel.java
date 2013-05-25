/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadEmptyCategory;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
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
		cmdThread = new ThreadEmptyCategory(rootFrame, EMPTY_CATEGORY);
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
		cmdThread = new ThreadEmptyCategory(rootFrame, EMPTY_CATEGORY);
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
		if( this.isDataTreePathLocked() ){
			showInputError(rootFrame, "Can not change data treepath in this context");
		}else if( dataTreePath != null ) {
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
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		runButton = new RunTaskButton(this);
		nameField = new NodeNameTextField(16, "^" + RegExp.COLLNAME + "$", runButton);
		nameField.addPropertyChangeListener("value", this);

		MyGBC mgbc = new MyGBC(5,5,5,5);		
		tPanel = this.addSubPanel("Input Parameters");
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Collection Name"), mgbc);

		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(nameField, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Category "), mgbc);

		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		categoryField = new JTextField(16);
		tPanel.add(categoryField, mgbc);
		
		mgbc.newRow();
		mgbc.gridwidth=2;
		mgbc.anchor = GridBagConstraints.WEST;
		tPanel.add(getHelpLabel(HelpDesk.CATEGORY_EMPTY), mgbc);

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
