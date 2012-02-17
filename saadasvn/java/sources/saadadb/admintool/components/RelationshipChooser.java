package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaRelation;

public class RelationshipChooser extends JPanel {
	private JList confList = new JList(new DefaultListModel());
	private JTextArea description = new JTextArea(6, 24);
	private TaskPanel taskPanel ;
	private String selectedRelation = null;
	private Component toActivate;
	private Runnable runnable;
	private String endPoint;

	public RelationshipChooser(TaskPanel taskPanel, Component toActivate) {
		this(taskPanel, toActivate, null);
	}

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
		c.gridx = 0; c.gridy = 0;c.weightx = 0;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setPreferredSize(new Dimension(350,100));
		this.add(scrollPane, c);

		c.gridx++;c.weightx = 1;
		this.add(new JScrollPane(description), c);

		confList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if( confList.getSelectedValue() != null ) {
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
			}
		});
	}

	public void setDataTreePath(DataTreePath dataTreePath) throws FatalException  {
		selectedRelation = null;
		description.setText("");
		if( this.toActivate != null) this.toActivate.setEnabled(false);

		DefaultListModel model = (DefaultListModel) confList.getModel();
		model.removeAllElements();
		if(dataTreePath != null ){
			this.endPoint = dataTreePath.collection + "." +  dataTreePath.category.toUpperCase();
			for(String r: Database.getCachemeta().getRelationNamesStartingFromColl(
					dataTreePath.collection, Category.getCategory(dataTreePath.category.toUpperCase()))) {
				model.addElement(r);			
			}
			for(String r: Database.getCachemeta().getRelationNamesEndingOnColl(
					dataTreePath.collection, Category.getCategory(dataTreePath.category.toUpperCase()))) {
				MetaRelation mr = Database.getCachemeta().getRelation(r);
				JLabel jl = new JLabel("<html>" + mr.getSecondary_coll() + "." + Category.explain(mr.getSecondary_category()) + " &gt; <b>" + r + "</b></html>");
				model.addElement(r);			
			}
		}
	}

	public String getSelectedRelation() {
		return selectedRelation;
	}

	class MyCellRenderer extends DefaultListCellRenderer {
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
				System.out.println(RelationshipChooser.this.endPoint);
				System.out.println(start);
				System.out.println(end + "\n");
				if( RelationshipChooser.this.endPoint.equals(start)) {
					ep = "(to " + end + ")"; 
				}
				else if( RelationshipChooser.this.endPoint.equals(end)) {
					ep = "(from " + start + ")"; 
				}
				else {
					ep = "(loopback)"; 
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
