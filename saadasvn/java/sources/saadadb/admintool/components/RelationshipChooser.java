package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaRelation;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

/**
 * Panel with a clickable list of relations
 * @author michel
 * @version $Id$
 *
 */
public class RelationshipChooser extends JPanel {
	private static final long serialVersionUID = 1L;
	private JList<String> confList = new JList<String>(new DefaultListModel<String>());
	private JXTable descriptionTable;
	private DefaultTableModel dm;
	private TaskPanel taskPanel;
	private String selectedRelation = null;
	private Component toActivate;
	private Runnable runnable;
	private String endPoint;
	private int lastSelectedIndex = 0;
	private final static int TABLE_COLUMN_SIZE_0 = 80;
	private final static int TABLE_COLUMN_SIZE_1 = 240;

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
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.gridx = 0; c.gridy = 0;c.weightx = 0;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("List of relationships"));
		scrollPane.setPreferredSize(new Dimension(350,100));
		this.add(scrollPane, c);

		
		descriptionTable = new JXTable(dm);
		descriptionTable.setHighlighters(HighlighterFactory.createSimpleStriping());
		descriptionTable.setRowSelectionAllowed(false);
		descriptionTable.setShowHorizontalLines(false);
		descriptionTable.setShowVerticalLines(false);
		descriptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		descriptionTable.setEditable(false);
		JTableHeader tableHeader = this.descriptionTable.getTableHeader();
	
		class IconTableCellRenderer extends DefaultTableCellRenderer {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setPreferredSize(new Dimension(0,0));
				return label;
			}
		}
		tableHeader.setDefaultRenderer(new IconTableCellRenderer());

		
		JScrollPane jspDescription = new JScrollPane(descriptionTable);
		jspDescription.setPreferredSize(new Dimension(320,185));
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
	public void setDescription() 
	{
		selectedRelation = confList.getSelectedValue().toString();
		RelationshipChooser.this.taskPanel.setSelectedResource("Relation: " + selectedRelation, null);
		this.dm = new DefaultTableModel();
		this.dm.addColumn("Attribute");
		this.dm.addColumn("Value");
		try 
		{
			MetaRelation mr = Database.getCachemeta().getRelation(selectedRelation);
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT count(*) FROM " + mr.getName());
			String content = "";
			while (rs.next()) 
			{
				content += rs.getString(1);
				break;
			}
			squery.close();
			content += " links ";

			String[] qls = mr.getQualifier_names().toArray(new String[0]);
			String quals = "";
			if( qls.length == 0 ) 
				quals += "No qualifier";
			else 
			{
				for( String q: qls) 
					quals += q + " ";
			}
			String correlator = mr.getCorrelator();
			this.dm.addRow(new String[] { "Name", mr.getName() });
			this.dm.addRow(new String[] { "From", mr.getPrimary_coll()+ "." + Category.explain(mr.getPrimary_category()) });
			this.dm.addRow(new String[] { "To", mr.getSecondary_coll() + "." + Category.explain(mr.getSecondary_category()) });
			this.dm.addRow(new String[] { "Qualifiers", quals });
			this.dm.addRow(new String[] { "Correlator", (correlator.compareTo("")==0?"No correlator":correlator) });
			this.dm.addRow(new String[] { "Content", content });
			this.dm.addRow(new String[] { "Description", mr.getDescription().trim() });
			this.dm.addRow(new String[] { "Indexed", (mr.isIndexed()?"Yes":"No") });
			if( RelationshipChooser.this.toActivate != null) RelationshipChooser.this.toActivate.setEnabled(true);
			if( RelationshipChooser.this.runnable != null ) RelationshipChooser.this.runnable.run();
		} 
		catch (Exception e) 
		{
			AdminComponent.showFatalError(RelationshipChooser.this.taskPanel.rootFrame, e);
		}
		RelationshipChooser.this.taskPanel.setSelectedResource("Relation: " + selectedRelation, null);
		MultiLineTableCellRenderer renderer = new MultiLineTableCellRenderer();
		this.descriptionTable.setModel(dm);	
		TableColumnModel columnModel = this.descriptionTable.getColumnModel();
		for (int i=0 ; i<columnModel.getColumnCount() ; i++)
			columnModel.getColumn(i).setCellRenderer(renderer);
		this.descriptionTable.packColumn(0, TABLE_COLUMN_SIZE_0);
		this.descriptionTable.packColumn(1, TABLE_COLUMN_SIZE_1);
	}

	/**
	 * Update the relation list from the data tree path
	 * @param dataTreePath
	 * @throws FatalException
	 */
	public void setDataTreePath(DataTreePath dataTreePath) throws FatalException  {
		selectedRelation = null;
		this.dm = new DefaultTableModel();
		this.descriptionTable.setModel(dm);
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
			for( String r: rls ) 
			{
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
	
	/**
	   * Multiline Table Cell Renderer.
	   */

	/**
	 * Multiline Table Cell Renderer.
	 */
	public class MultiLineTableCellRenderer extends JTextArea 
		implements TableCellRenderer {
		private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
	 
		public MultiLineTableCellRenderer() 
		{
			setLineWrap(true);
			setWrapStyleWord(true);
			setOpaque(true);
		}
	 
		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setFont(table.getFont());
			if (hasFocus) {
				if (table.isCellEditable(row, column)) {
					setForeground(UIManager.getColor("Table.focusCellForeground"));
					setBackground(UIManager.getColor("Table.focusCellBackground"));
				}
			} else {
				setBorder(new EmptyBorder(1, 2, 1, 2));
			}
			if (value != null) {
				setText(value.toString());
			} else {
				setText("");
			}
			adjustRowHeight(table, row, column);
			return this;
		}
	 
		/**
		 * Calculate the new preferred height for a given row, and sets the height on the table.
		 */
		private void adjustRowHeight(JTable table, int row, int column) 
		{
			int cWidth = (column==1?RelationshipChooser.TABLE_COLUMN_SIZE_1-10:table.getTableHeader().getColumnModel().getColumn(column).getWidth());
			if (column==1)
				Messenger.printMsg(Messenger.DEBUG, "Width : " + cWidth);
			setSize(new Dimension(cWidth, 1000));
			int prefH = getPreferredSize().height;
			while (rowColHeight.size() <= row) {
				rowColHeight.add(new ArrayList<Integer>(column));
			}
			List<Integer> colHeights = rowColHeight.get(row);
			while (colHeights.size() <= column) {
				colHeights.add(0);
			}
			colHeights.set(column, prefH);
			int maxH = prefH;
			for (Integer colHeight : colHeights) {
				if (colHeight > maxH) {
					maxH = colHeight;
				}
			}
			if (table.getRowHeight(row) != maxH) {
				table.setRowHeight(row, maxH);
			}
		}
	}
}
