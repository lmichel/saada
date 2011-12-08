package saadadb.admintool.panels;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;

public class LoadDataPanel extends ChoicePanel {

	public LoadDataPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, LOAD_DATA, null, ancestor);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();

		
		tPanel = this.addSubPanel("Data Loader Filter");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "New Filter", "icons/Filter.png"
				, new Runnable(){public void run(){
					System.out.println("new filter");}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		(new ChoiceItem(rootFrame, tPanel, c
				, "Select Filter", "icons/SelectFilter.png"
				, new Runnable(){public void run(){
					System.out.println("select filter");}})).inactive();
		
	

		tPanel = this.addSubPanel("Loader");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Load Data", "icons/LoadData.png"
				, new Runnable(){public void run(){
					System.out.println("loadcdata");}});
	
	}


}
