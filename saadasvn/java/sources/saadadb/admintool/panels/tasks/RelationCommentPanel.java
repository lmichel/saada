/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadRelationComment;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class RelationCommentPanel extends RelationDropPanel {
	private static final long serialVersionUID = 1L;
	protected FreeTextField commentField;

	public RelationCommentPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, COMMENT_RELATION, null, ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected RelationCommentPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.cmdThread = cmdThread;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) ) {
			try {
				setSelectedResource("", null);
				this.relationChooser.setDataTreePath(dataTreePath);
				this.commentField.setText("");
			} catch (FatalException e) {
				Messenger.trapFatalException(e);
			}
		}
	}
	
	public void setSelectedResource(String label, String explanation) {	
		super.setSelectedResource(label, explanation);
		String r = this.relationChooser.getSelectedRelation();
		if( r != null) {
			this.commentField.setText(Database.getCachemeta().getRelation(r).getDescription());
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	@Override
	protected Map<String, Object> getParamMap() {
		if( relationChooser.getSelectedRelation() != null ) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("relation", relationChooser.getSelectedRelation());
			map.put("comment", this.commentField.getText().trim());
			return map;
		}
		else {
			return null;	
		}
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationComment(rootFrame, COMMENT_RELATION);
	}

	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);

		JPanel tPanel = this.addSubPanel("Relationship Selector");
		relationChooser = new RelationshipChooser(this, runButton);
		MyGBC c = new MyGBC(5,5,5,5);c.gridwidth = 2;
		tPanel.add(relationChooser, c);
		
		c.newRow();c.gridwidth = 2;		c.anchor = GridBagConstraints.WEST;

		tPanel.add(getHelpLabel(HelpDesk.RELATION_SELECTOR), c);
		
		c.newRow();c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Description"), c);
		
		c.rowEnd();
		c.anchor =  GridBagConstraints.WEST;
		commentField = new FreeTextField(6, 24);
		tPanel.add(commentField.getPanel(), c);

		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
	}
}
