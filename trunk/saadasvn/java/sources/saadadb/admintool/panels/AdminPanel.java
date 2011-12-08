/**
 * 
 */
package saadadb.admintool.panels;

import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.DataTreePath;
import utils.TreePath;

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

	public AdminPanel(AdminTool rootFrame, String title, String icon, String ancestor) {
		super(rootFrame);
		this.icon = icon;
		this.title = title;
		this.ancestor = ancestor;
		this.setMainPanel();
		this.setToolBar();
		this.setActivePanel();

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

		this.add(tPanel);
		return tPanel;

	}
	
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		treePathLabel.setText(dataTreePath.toString());
	}
	
	public void setSelectedResource(String label, String explanation) {	
		super.setSelectedResource(label, explanation);
		selectResourceLabel.setText(selectedResource);
	}
	public void setCurrentTask(String currentTask) {	
		super.setCurrentTask(currentTask);
		currentTaskLabel.setText(currentTask);
	}
	
	/**
	 * 
	 */
	protected abstract void setToolBar() ;
	
	/**
	 * 
	 */
	protected abstract void setActivePanel() ;

}
