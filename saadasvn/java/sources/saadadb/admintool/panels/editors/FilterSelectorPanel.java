/**
 * 
 */
package saadadb.admintool.panels.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.LoaderConfigChooser;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

// To delete !

/**
 * @author laurentmichel
 *
 */

@Deprecated
public class FilterSelectorPanel extends EditPanel {
	private static final long serialVersionUID = 1L;
	protected  LoaderConfigChooser configChooser;
	private String collection, category;
	
	public FilterSelectorPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EXPLORE_DATA, null, ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected FilterSelectorPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootOrCollectionLevel())
		{
			showInputError(rootFrame, "You must select either a category (IMAGE, SPECTRUM, ...) or a class.");
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( this.isDataTreePathLocked() ){
			showInputError(rootFrame, "Can not change data treepath in this context");
		} else if( dataTreePath != null ) {
			if( dataTreePath.isCollectionLevel() ) {
				category = null;
			} else {
				super.setDataTreePath(dataTreePath);
				collection = dataTreePath.collection;
				if( "ENTRY".equals(dataTreePath.category)) {
					category = "TABLE";
				}
				else {
					category = dataTreePath.category;
				}
			}
			try {
				configChooser.setCategory(category, null);
			} catch (FatalException e) {
				Messenger.trapFatalException(e);
			}
			treePathLabel.setText(dataTreePath.toString());
		}
	}


	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}


	@Override
	protected void setActivePanel() {
		JPanel tPanel = this.addSubPanel("Data Loader Filter Selector");
		configChooser = new LoaderConfigChooser(this);
		tPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;c.gridy = 0;
		c.weightx = 0;
		tPanel.add(configChooser, c);
		c.gridy++;
		tPanel.add(getHelpLabel(HelpDesk.FILTER_CHOOSER), c);
		
	}

	/**
	 * Called by the config editor to set the new config
	 * @param confName
	 */
	public void setConfig(String confName) {
		try {
			configChooser.setCategory(this.category, confName);
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
	}

	@Override
	public void active() {
	}
}
