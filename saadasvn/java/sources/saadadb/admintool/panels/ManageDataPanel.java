/**
 * 
 */
package saadadb.admintool.panels;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;

/**
 * @author laurentmichel
 *
 */
public class ManageDataPanel extends ChoicePanel {

	public ManageDataPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, MANAGE_DATA, null, ancestor);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();

//		
//		tPanel = this.addSubPanel("Data Loader");
//		c.gridx = 0;
//		c.gridy = 0;	
//		c.weightx = 0.5;
//		c.weighty = 0.5;
//		new ChoiceItem(rootFrame, tPanel, c
//				, "Load Data", "icons/LoadData.png"
//				, new Runnable(){public void run(){
//					rootFrame.activePanel(LOAD_DATA);}});
//		c.gridx = 1;
//		c.gridy = 0;	
//		c.weightx = 0.5;
//		c.weighty = 0.5;
//		(new ChoiceItem(rootFrame, tPanel, c
//				, "Statistic", "icons/Statistics.png"
//				, new Runnable(){public void run(){
//					System.out.println("loaddata");}})).inactive();
		
	

		tPanel = this.addSubPanel("Collection Level");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Comment Collection", "icons/CommentCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_COLLECTION);}});
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Empty Collection", "icons/EmptyCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_COLLECTION);}});
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		new ChoiceItem(rootFrame, tPanel, c
				, "Empty Category", "icons/EmptyCategory.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_CATEGORY);}});

		
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		new ChoiceItem(rootFrame, tPanel, c
				, "Remove Collection", "icons/DropCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_COLLECTION);}});		
		
		tPanel = this.addSubPanel("Class Level");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "Comment Class", "icons/CommentClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_CLASS);}});

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;

		new ChoiceItem(rootFrame, tPanel, c
				, "Empty Class", "icons/EmptyClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_CLASS);}});		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;

		new ChoiceItem(rootFrame, tPanel, c
				, "Remove Class", "icons/DropClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_CLASS);}});		
		
		tPanel = this.addSubPanel("Manage SQL Index");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		new ChoiceItem(rootFrame, tPanel, c
				, "SQL Index", "icons/IndexTable.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(SQL_INDEX);}});

		
	}


}
