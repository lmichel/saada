/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.Map;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.voresources.ModelFieldMapper;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaCollection;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 *
 */
public class ObscoreMapperPanel extends TaskPanel {
	private ModelFieldMapper modelFieldMapper;
	private RunTaskButton runButton;
	private SaveButton saveButton;
	private VOResource vor;

	public ObscoreMapperPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, OBSCORE_MAPPER, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Map<String, Object> getParamMap() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
		this.setSelectedResource("VO Model ObsCore", null);
	}


	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( dataTreePath == null ) {
			return;
		} else if( dataTreePath.isCollectionLevel() ) {
			showInputError(rootFrame, "No category (IMAGE,ENTRY....) nor class in the selected data tree node");
			return;
		} else 	if( this.hasChanged() && !showConfirmDialog(rootFrame, "Do you want to discard the current changes?") ) {
			return;
		} else {
			super.setDataTreePath(dataTreePath);
			try {
				modelFieldMapper.setDataTreePath(dataTreePath);
			} catch (SaadaException e) {
				showFatalError(rootFrame, e);
			}
		}
	}

	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);
		saveButton = new SaveButton(this);
		try {
			vor = VOResource.getResource("ObsCore");
			modelFieldMapper = new ModelFieldMapper(this);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			showFatalError(rootFrame, e);
		}
		JPanel tPanel = this.addSubPanel("Class to Obscore Mapper");
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;
		tPanel.add(modelFieldMapper, emc);
		
		this.setActionBar(new Component[]{runButton, saveButton
				, debugButton});

	}

	public VOResource getVor() {
		return this.vor;
	}

}
