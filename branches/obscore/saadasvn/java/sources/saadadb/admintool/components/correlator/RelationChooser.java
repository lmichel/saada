package saadadb.admintool.components.correlator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.FatalException;


public class RelationChooser extends CollapsiblePanel {
	private RelationshipChooser configChooser;
	RelationPopulatePanel taskPanel ;
	
	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public RelationChooser(RelationPopulatePanel taskPanel, Component toActive) {
		super("Relationship Selector");
		this.taskPanel = taskPanel;
		configChooser = new RelationshipChooser(taskPanel, toActive, RelationshipChooser.TREEPATH_RELATIONS, new Runnable() {
			public void run() {
				RelationChooser.this.taskPanel.load();				
			}
		});
		MyGBC mc = new MyGBC(5,5,5,5); mc.anchor = GridBagConstraints.NORTH;
		this.getContentPane().setLayout(new GridBagLayout());
		this.getContentPane().add(configChooser, mc);
	}
	
	public void selectRelation(String relationName){
		configChooser.selectRelation(relationName);
	}

	/**
	 * @return
	 */
	public String getSelectedRelation() {
		return configChooser.getSelectedRelation();
	}

	public void setDataTreePath(DataTreePath dataTreePath) throws FatalException {
		configChooser.setDataTreePath(dataTreePath);		
	}

}
