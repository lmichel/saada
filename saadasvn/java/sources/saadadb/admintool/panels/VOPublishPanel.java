package saadadb.admintool.panels;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;

public class VOPublishPanel extends ChoicePanel {
	private static final long serialVersionUID = 1L;

	public VOPublishPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, VO_PUBLISH, null, ancestor);
	}


	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
		
		tPanel = this.addSubPanel("Simple Protocols");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Publish SIA", "icons/SIA.png"
				, new Runnable(){public void run(){
					System.out.println("SIA");}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		(new ChoiceItem(rootFrame, tPanel, c
				, "Publish SSA", "icons/SSA.png"
				, new Runnable(){public void run(){
					System.out.println("SSA");}})).inactive();
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		(new ChoiceItem(rootFrame, tPanel, c
				, "Publish SCS", "icons/ConeSearch.png"
				, new Runnable(){public void run(){
					System.out.println("CS");}})).inactive();
	

		tPanel = this.addSubPanel("TAP Service");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Publish TAP service", "icons/TAP.png"
				, new Runnable(){public void run(){
					System.out.println("TAP");}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Publish ObsCore Table", "icons/ObsCore.png"
				, new Runnable(){public void run(){
					System.out.println("Obs Core");}});
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		new ChoiceItem(rootFrame, tPanel, c
				, "Publish User Defined DM", "icons/UserModel.png"
				, new Runnable(){public void run(){
					System.out.println("user DM");}});		
		
		tPanel = this.addSubPanel("Summary");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "VO Services Summary", "icons/VOServices.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(VO_CURATOR);}});
	}

}
