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
import saadadb.admintool.components.voresources.ModelFieldMapper;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 *
 */
public class ObscoreMapperPanel extends TaskPanel {
	private ModelFieldMapper modelFiledMapper;
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

	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);
		saveButton = new SaveButton(this);
		try {
			vor = VOResource.getResource("ObsCore");
			modelFiledMapper = new ModelFieldMapper(this);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			showFatalError(rootFrame, e);
		}
		JPanel tPanel = this.addSubPanel("Class to Obscore Mapper");
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;
		tPanel.add(modelFiledMapper, emc);
		
		this.setActionBar(new Component[]{runButton, saveButton
				, debugButton});

	}

	public VOResource getVor() {
		return this.vor;
	}

}
