/**
 * 
 */
package saadadb.admintool.panels;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.DebugButton;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.SaadaException;

/**
 * @author laurentmichel
 *
 */
public abstract class AdminPanel extends AdminComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String icon,title, ancestor;
	protected JLabel treePathLabel ;
	protected JLabel selectResourceLabel ;
	protected JLabel currentTaskLabel ;
	public final DebugButton debugButton = new DebugButton();
	private boolean lockDataTreePath = false;

	public JLabel getTreePathLabel() {
		return treePathLabel;
	}

	public void initTreePathLabel() {
		treePathLabel = AdminComponent.getSubTitleLabel("TreePath");
	}

	public JLabel getSelectResourceLabel() {
		return selectResourceLabel;
	}

	public void initSelectResourceLabel() {
		selectResourceLabel = AdminComponent.getSubTitleLabel("");
	}

	public JLabel getCurrentTaskLabel() {
		return currentTaskLabel;
	}

	public void initCurrentTaskLabel() {
		currentTaskLabel = AdminComponent.getSubTitleLabel("No task");
	}


	public AdminPanel(AdminTool rootFrame, String title, String icon, String ancestor) {
		super(rootFrame);
		this.icon = icon;
		this.title = title;
		this.ancestor = ancestor;
		this.setMainPanel();
		this.setToolBar();
		this.setActivePanel();
	}

	public void setAncestor(String ancestor) {
		this.ancestor = ancestor;
	}

	public String getAncestor(){
		return this.ancestor;
	}

	public void lockDataTreePath() {
		lockDataTreePath = true;
	}
	public void unlockDataTreePath() {
		lockDataTreePath = false;
	}
	public boolean isDataTreePathLocked() {
		return lockDataTreePath;
	}

	/* (non-Javadoc)
	 * @see components.AdminComponent#setMainPanel()
	 */
	protected void setMainPanel() {
		this.setBackground(LIGHTBACKGROUND);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}

	/**
	 * @return
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}


	/**
	 * @param title
	 * @return 
	 */
	protected JPanel addSubPanel(String title) {

		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setBorder(new TitledBorder(title));
		tPanel.setBackground(LIGHTBACKGROUND); 
		JScrollPane jsp = new JScrollPane(tPanel);
		jsp.setBorder(BorderFactory.createEmptyBorder());
		this.add(new JScrollPane(tPanel));
		return tPanel;

	}
//	/**
//	 * @param title
//	 * @return 
//	 */
//	protected JPanel addScrollSubPanel(String title) {
//
//		JPanel tPanel = new JPanel();
//		tPanel.setLayout(new GridBagLayout());
//		tPanel.setBorder(new TitledBorder(title));
//		tPanel.setBackground(LIGHTBACKGROUND); 
//
//		this.add(new JScrollPane(tPanel));
//		return tPanel;
//	}
	
	private void highlightDataTreePath() {
		if( treePathLabel != null )
			treePathLabel.setForeground(NEW_HEADER);
		if( currentTaskLabel != null )
			currentTaskLabel.setForeground(OLD_HEADER);
		if( selectResourceLabel != null )
			selectResourceLabel.setForeground(OLD_HEADER);
	}
	private void highlightSelectResource() {
		if( treePathLabel != null )
			treePathLabel.setForeground(OLD_HEADER);
		if( currentTaskLabel != null )
			currentTaskLabel.setForeground(OLD_HEADER);
		if( selectResourceLabel != null )
			selectResourceLabel.setForeground(NEW_HEADER);
	}
	private void highlightCurrentTask() {
		if( treePathLabel != null )
			treePathLabel.setForeground(OLD_HEADER);
		if( currentTaskLabel != null )
			currentTaskLabel.setForeground(NEW_HEADER);
		if( selectResourceLabel != null )
			selectResourceLabel.setForeground(OLD_HEADER);
	}

	public void setDataTreePath(DataTreePath dataTreePath) {
		if( dataTreePath != null && !lockDataTreePath ) {
			super.setDataTreePath(dataTreePath);
			if( treePathLabel != null )
				treePathLabel.setText(dataTreePath.toString());
			highlightDataTreePath();
		}
	}

	public void setSelectedResource(String label, String explanation) {	
		super.setSelectedResource(label, explanation);
		if( selectResourceLabel != null ) {
			selectResourceLabel.setText(selectedResource);
			if( selectedResource.length() > 0)
				highlightSelectResource();
		}
	}
	public void setCurrentTask(String currentTask) {	
		super.setCurrentTask(currentTask);
		if( currentTaskLabel != null ) {
			currentTaskLabel.setText(currentTask);
			if( currentTask.length() > 0)
				highlightCurrentTask();
		}
	}

	/**
	 * 
	 */
	protected abstract void setToolBar() ;

	/**
	 * 
	 */
	protected abstract void setActivePanel() ;

	/**
	 * called when the panel get the focus
	 */
	public abstract void active() ;

	/**
	 * @return
	 */
	public boolean hasChanged() {
		return false;
	}

	public void cancelChanges() {
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void save() throws Exception {
	}


}
