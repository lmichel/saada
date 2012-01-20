package saadadb.admintool.panels;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;

public class RootChoicePanel extends ChoicePanel {
	private static final long serialVersionUID = 1L;

	public RootChoicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, ROOT_PANEL, null, ancestor);
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();

		
		tPanel = this.addSubPanel("Data Management");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Create Collection", "icons/CreateColl.png"
				, new Runnable(){public void run() {
					try {
						rootFrame.activePanel(CREATE_COLLECTION);
					} catch (Exception e) {
						e.printStackTrace();
					}
					}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Load Data", "icons/LoadData.png"
				, new Runnable(){public void run(){rootFrame.activePanel(DATA_LOADER);}});

		c.gridx = 0;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Manage Data", "icons/ManageData.png"
				, new Runnable(){public void run(){
					try {
						rootFrame.activePanel(MANAGE_DATA);
					} catch (Exception e) {
						e.printStackTrace();
					}
					}});
		
		c.gridx = 1;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Manage Meta Data", "icons/MetaData.png"
				, new Runnable(){public void run(){
					System.out.println("loaddata");}});
		c.gridx = 2;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Manage Relationships", "icons/Relation.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(MANAGE_RELATIONS);}});

		tPanel = this.addSubPanel("Data publication");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Database Installation", "icons/Tool.png"
				, new Runnable(){public void run(){
					System.out.println("loaddata");}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Web Publishing", "icons/Web.png"
				, new Runnable(){public void run(){
					System.out.println("loaddata");}});
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		new ChoiceItem(rootFrame, tPanel, c
				, "VO Publishing", "icons/ivoa.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(VO_PUBLISH);}});		
	}

}
