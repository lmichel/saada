/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadCreateCollection;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
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
	protected int help_key = HelpDesk.COLL_CREATE;
	protected JLabel nodeLabel;
	
	public CollCreatePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, CREATE_COLLECTION, null, ancestor);
		cmdThread = new ThreadCreateCollection(rootFrame, CREATE_COLLECTION);
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
		cmdThread = new ThreadCreateCollection(rootFrame,CREATE_COLLECTION);
	}


	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, false, false, false));
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
	}

	protected void setHelpKey() {
		help_key = HelpDesk.COLL_CREATE;
	}
	protected void setNodeLabel() {
		nodeLabel = getPlainLabel("Collection Name");
	}

	@Override
	protected void setActivePanel(){
		this.setHelpKey();
		this.setNodeLabel();
		JPanel tPanel;
		runButton = new RunTaskButton(this);
		nameField = new NodeNameTextField(16, "^" + RegExp.COLLNAME + "$", runButton);
		nameField.addPropertyChangeListener("value", this);
		
		MyGBC mgbc = new MyGBC(5,5,5,5);		
		tPanel = this.addSubPanel("Input Parameters");
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(nodeLabel, mgbc);		

		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(nameField, mgbc);
		
		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Description"), mgbc);
		
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		commentField = new FreeTextField(6, 24);
		tPanel.add(commentField.getPanel(), mgbc);
		
		
		mgbc.newRow();
		mgbc.gridwidth=2;
		mgbc.anchor = GridBagConstraints.WEST;
		tPanel.add(getHelpLabel(help_key), mgbc);
		
		/*EventQueue.invokeLater(new Runnable() 
		{
			   @Override
			   public void run() 
			   {
			         nameField.grabFocus();
			         nameField.requestFocus();
			   }
		});*/

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
