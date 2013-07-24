package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaRelation;

/**
 * Panel with a clickable list of relations
 * @author michel
 * @version $Id$
 *
 */
public class RelationshipChooser extends JPanel {
	private static final long serialVersionUID = 1L;
	private JList confList = new JList(new DefaultListModel());
	private JTextArea description = new JTextArea(6, 24);
	private TaskPanel taskPanel;
	private String selectedRelation = null;
	private Component toActivate;
	private Runnable runnable;
	private String endPoint;
	private int lastSelectedIndex = 0;

	/**
	 * @param taskPanel
	 * @param toActivate
	 */
	public RelationshipChooser(TaskPanel taskPanel, Component toActivate) {
		this(taskPanel, toActivate, null);
	}

	/**
	 * @param taskPanel
	 * @param toActivate
	 * @param runnable
	 */
	public RelationshipChooser(TaskPanel taskPanel, Component toActivate, Runnable runnable) {
		this.toActivate = toActivate;
		this.taskPanel = taskPanel;
		this.confList.setVisibleRowCount(6);
		this.confList.setFixedCellWidth(24);
		this.confList.setCellRenderer(new MyCellRenderer());
		this.confList.setFont(AdminComponent.plainFont);
		this.runnable = runnable;
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.description.setEditable(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.gridx = 0; c.gridy = 0;c.weightx = 0;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("List of relationships"));
		scrollPane.setPreferredSize(new Dimension(350,100));
		this.add(scrollPane, c);

		JScrollPane jspDescription = new JScrollPane(description);
		jspDescription.setBorder(BorderFactory.createTitledBorder("Description of selected relationship"));
		c.fill = GridBagConstraints.BOTH;
		c.gridx++; c.weightx = 1; c.gridheight = 2;
		this.add(jspDescription, c);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0; c.gridy = 1; c.gridheight = 1;
		this.add(AdminComponent.getHelpLabel(HelpDesk.RELATION_SELECTOR), c);

		confList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if( confList.getSelectedValue() != null ) {
					if( acceptChange() ) {
						RelationshipChooser.this.taskPanel.cancelChanges();
						setDescription();
						lastSelectedIndex = confList.getSelectedIndex();
					}
				}	
			}
		});
	}

	/**
	 * @return
	 */
	private boolean acceptChange() {
		if( RelationshipChooser.this.taskPanel.hasChanged() && confList.getSelectedIndex() != lastSelectedIndex 
				&& !AdminComponent.showConfirmDialog(RelationshipChooser.this.taskPanel
						, "Modifications not saved. Do you want to continue anyway?") ) {
			confList.setSelectedIndex(lastSelectedIndex);
			return false;
		} else {
			return true;
		}

	}

	/**
	 * Programmatically selection of one relation
	 * @param relationName
	 */
	public void selectRelation(String relationName){
		ListModel model = confList.getModel();
		for( int i=0;  i<model.getSize() ; i++ ) {
			if( ((String)(model.getElementAt(i))).equals(relationName) ) {
				if( acceptChange() ) {
					confList.setSelectedIndex(i);
					setDescription();
					lastSelectedIndex = confList.getSelectedIndex();
					return;
				}
			}
		}
	}

	/**
	 * Write out the  description of the current relation
	 */
	public void setDescription() {
		selectedRelation = confList.getSelectedValue().toString();
		//selectedRelation = selectedRelation.split("(</b>)|<b>")[1].trim();;
		try {
			description.setText(Database.getCachemeta().getRelation(selectedRelation).toString());
			if( RelationshipChooser.this.toActivate != null) RelationshipChooser.this.toActivate.setEnabled(true);
			if( RelationshipChooser.this.runnable != null ) RelationshipChooser.this.runnable.run();
		} catch (Exception e) {
			AdminComponent.showFatalError(RelationshipChooser.this.taskPanel.rootFrame, e);
		}
		RelationshipChooser.this.taskPanel.setSelectedResource("Relation: " + selectedRelation, null);		
	}

	/**
	 * Update the relation list from the data tree path
	 * @param dataTreePath
	 * @throws FatalException
	 */
	public void setDataTreePath(DataTreePath dataTreePath) throws FatalException  {
		selectedRelation = null;
		description.setText("");
		if( this.toActivate != null) this.toActivate.setEnabled(false);

		DefaultListModel model = (DefaultListModel) confList.getModel();
		model.removeAllElements();
		if(dataTreePath != null ){
			// Use a set to avoid loopback relationships to be displayed twice in the list
			Set<String> rls = new LinkedHashSet<String>();
			this.endPoint = dataTreePath.collection + "." +  dataTreePath.category.toUpperCase();
			for(String r: Database.getCachemeta().getRelationNamesStartingFromColl(
					dataTreePath.collection, Category.getCategory(dataTreePath.category.toUpperCase()))) {
				rls.add(r);
			}
			for(String r: Database.getCachemeta().getRelationNamesEndingOnColl(
					dataTreePath.collection, Category.getCategory(dataTreePath.category.toUpperCase()))) {
				rls.add(r);
			}
			for( String r: rls ) {
				model.addElement(r);			
			}
		}
	}

	public String getSelectedRelation() {
		return selectedRelation;
	}

	/**
	 * Display relation names with some extr infos (target/source, indexed or not)
	 * @author michel
	 * @version $Id$
	 *
	 */
	class MyCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(
				JList list,
				Object value,   // value to display
				int index,      // cell index
				boolean iss,    // is the cell selected
				boolean chf)    // the list and the cell have the focus
		{
			try {
				MetaRelation mr = Database.getCachemeta().getRelation((String)value);
				String start = mr.getPrimary_coll() + "." + Category.explain(mr.getPrimary_category());
				String end   = mr.getSecondary_coll() + "." + Category.explain(mr.getSecondary_category());
				String retour  = "";
				String ep;
				if( start.equals(end )) {
					ep = "(loopback)"; 
				}
				else if( RelationshipChooser.this.endPoint.equals(start)) {
					ep = "(to " + end + ")"; 
				}
				else {
					ep = "(from " + start + ")"; 
				}
				retour = "<html><b>" + value + "</b> &gt; <i>" + ep + "</html>";
				Color col = ( mr.isIndexed() ) ?new Color(0x4F7B60): Color.RED;
				JLabel jl = (JLabel) super.getListCellRendererComponent(list, retour, index, iss, chf);
				jl.setForeground(col);
			} catch (FatalException e) {}
			return this;
		}
	}

}
