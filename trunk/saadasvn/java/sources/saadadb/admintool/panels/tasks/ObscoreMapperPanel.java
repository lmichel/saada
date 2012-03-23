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
import saadadb.admintool.cmdthread.ThreadDmViewPopulate;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.voresources.ModelFieldMapper;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.SaadaException;
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
	public VOResource vor;

	public ObscoreMapperPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, OBSCORE_MAPPER, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadDmViewPopulate(rootFrame, OBSCORE_MAPPER);
	}

	@Override
	protected Map<String, Object> getParamMap() {		
		LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();
		retour.put("dm", vor);
		retour.put("class", dataTreePath.classe);
		return retour;
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
			runButton.setEnabled(false);
			return;
		} else if( dataTreePath.isClassLevel() ) {
			runButton.setEnabled(true);
			if( !this.hasChanged() ||
			    showConfirmDialog(rootFrame, "Do you want to discard the current changes?") ) {
				super.setDataTreePath(dataTreePath);
				try {
					modelFieldMapper.setDataTreePath(dataTreePath);
				} catch (Exception e) {
					showFatalError(rootFrame, e);
				}
			}
		} else {
			runButton.setEnabled(false);
			showInputError(rootFrame, "DM mapping can only be done at class level. Select a class (a tree leaf) on the Database map");
			return;
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
		modelFieldMapper.setCollapsed(false);
		
		emc.newRow();
		tPanel.add(new CollapsiblePanel("Manage Data Classes Published in ObsCore"), emc);

		this.setActionBar(new Component[]{runButton, saveButton
				, debugButton});

	}

	public VOResource getVor() {
		return this.vor;
	}

}
