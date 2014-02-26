/**
 * 
 */
package saadadb.admintool.components.voresources;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.tasks.ObscoreMapperPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;

/**
 * @author laurentmichel
 *
 */
public class ModelFieldMapper extends CollapsiblePanel{
	private static final long serialVersionUID = 1L;
	private ModelViewPanel modelViewPanel;

	public ModelFieldMapper(ObscoreMapperPanel obscoreMapperPanel) throws Exception {
		super("Field Mapper");
		this.modelViewPanel = new ModelViewPanel(obscoreMapperPanel);
		this.getContentPane().setLayout(new GridBagLayout());
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;
		this.getContentPane().add(modelViewPanel, emc);

	}

	public void setDataTreePath(DataTreePath dataTreePath) throws Exception {
		modelViewPanel.setDataTreePath(dataTreePath);		
	}

}
