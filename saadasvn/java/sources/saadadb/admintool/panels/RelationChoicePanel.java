package saadadb.admintool.panels;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;

public class RelationChoicePanel extends ChoicePanel {
	private static final long serialVersionUID = 1L;

	public RelationChoicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, MANAGE_RELATIONS, null, ancestor);
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
		
		tPanel = this.addSubPanel("Relationship Management");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "New Relationship", "icons/CreateRel.png"
				, new Runnable(){public void run(){
					System.out.println("SIA");}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		(new ChoiceItem(rootFrame, tPanel, c
				, "Comment Relationship", "icons/CommentRel.png"
				, new Runnable(){public void run(){
					System.out.println("SSA");}})).inactive();
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		(new ChoiceItem(rootFrame, tPanel, c
				, "Drop Relationship", "icons/DropRel.png"
				, new Runnable(){public void run(){
					System.out.println("CS");}})).inactive();
	

		tPanel = this.addSubPanel("Link Management");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Populate relationship", "icons/PopulateRel.png"
				, new Runnable(){public void run(){
					System.out.println("TAP");}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Empty Relationship", "icons/EmptyRel.png"
				, new Runnable(){public void run(){
					System.out.println("Obs Core");}});
		
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		new ChoiceItem(rootFrame, tPanel, c
				, "Index Relationship", "icons/IndexRel.png"
				, new Runnable(){public void run(){
					System.out.println("user DM");}});		
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		new ChoiceItem(rootFrame, tPanel, c
				, "Drop Relationship Index", "icons/DropRelIndex.png"
				, new Runnable(){public void run(){
					System.out.println("user DM");}});		
		
	}

}
