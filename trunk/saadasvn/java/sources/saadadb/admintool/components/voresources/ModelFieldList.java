package saadadb.admintool.components.voresources;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.HelpedTextField;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.util.Messenger;
import saadadb.vo.registry.Capabilities;


public class ModelFieldList extends JPanel implements ActionListener{
	private ModelViewPanel modelViewPanel;
	private JCheckBox filterMand = new JCheckBox("Man");
	private JCheckBox filterNotset = new JCheckBox("N/S");
	private JCheckBox filterFailed = new JCheckBox("Fld");
	private HelpedTextField filterName = new HelpedTextField("Field Name Filter", 15);
	private JPanel itemsPanel = new JPanel();


	//protected  ArrayList<ModelFiedlEditor> editors = new ArrayList<ModelFiedlEditor>();
	protected  ArrayList<FieldItem> items = new ArrayList<FieldItem>();

	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws SaadaException 
	 */
	public ModelFieldList(ModelViewPanel modelViewPanel) throws SaadaException {
		this.setBorder(BorderFactory.createTitledBorder("Fields of the ObsCore Model"));
		this.filterFailed.setToolTipText("Select fields with a mapping failing at checking test");
		this.filterMand.setToolTipText("Select mandatory fields");
		this.filterNotset.setToolTipText("Select fields with an emprty mapping");

		this.modelViewPanel = modelViewPanel;
		this.setLayout(new GridBagLayout());
		this.itemsPanel.setLayout(new GridBagLayout());

		MyGBC mgbc = new MyGBC(2,2,2,2);
		mgbc.gridwidth = 3;
		this.displayListItems();

		JScrollPane jsp = new JScrollPane(this.itemsPanel);
		mgbc.weightx = 1; mgbc.weighty = 1;
		mgbc.fill = GridBagConstraints.BOTH;
		this.add(jsp, mgbc);

		mgbc.weightx = 0; mgbc.weighty = 0;		
		mgbc.gridwidth = 1;
		mgbc.newRow();
		this.add(filterMand, mgbc);
		mgbc.next();
		this.add(filterNotset, mgbc);
		mgbc.next();
		this.add(filterFailed, mgbc);
		mgbc.newRow();
		mgbc.gridwidth = 3;
		this.add(filterName, mgbc);
		updateUI();

		filterName.setRunnable(new Runnable() {
			public void run() {
				try {
					ModelFieldList.this.filterListItems(filterName.getText(), filterMand.isSelected(), filterNotset.isSelected(), filterFailed.isSelected());
					ModelFieldList.this.unSelectAll();
				} catch (SaadaException e) {Messenger.printStackTrace(e);}
			}
		});
		filterMand.addActionListener(this);
		filterNotset.addActionListener(this);
		filterFailed.addActionListener(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			this.filterListItems(filterName.getText(), filterMand.isSelected(), filterNotset.isSelected(), filterFailed.isSelected());
			this.unSelectAll();
		} catch (SaadaException e1) {Messenger.printStackTrace(e1);}
	}


	/**
	 * 
	 */
	protected void unSelectAll() {
		for( FieldItem tsi: items) {
			tsi.unSelect();
		}
	}
	
	/**
	 * 
	 */
	protected void resetFields() {
		for( FieldItem tsi: items) {
			tsi.unSelect();
			tsi.setNotSet();
		}		
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	void displayListItems() throws SaadaException {
		this.itemsPanel.removeAll();
		MyGBC gbc = new MyGBC(0, 0, 0, 0);
		for( UTypeHandler uth: modelViewPanel.getVor().getUTypeHandlers() ) {
			FieldItem fi = new FieldItem(uth);
			items.add(fi);
			fi.setAlignmentX(Component.LEFT_ALIGNMENT);
			gbc.left(true);gbc.rowEnd();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor =GridBagConstraints.FIRST_LINE_START;
			gbc.weighty = 0;
			gbc.weightx = 1;
			this.itemsPanel.add(fi, gbc);
			gbc.newRow();
		}
		gbc.weighty = 1;
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(0,0));
		this.itemsPanel.add(jp, gbc);
		updateUI();
	}
	/**
	 * @param nameFilter
	 * @param mand
	 * @param notset
	 * @param failed
	 * @throws SaadaException
	 */
	void filterListItems(String nameFilter, boolean mand, boolean notset, boolean failed) throws SaadaException {
		this.itemsPanel.removeAll();
		MyGBC gbc = new MyGBC(0, 0, 0, 0);
		for( FieldItem fi: items ) {
			if( fi.passFilter(nameFilter, mand, notset, failed) ) {
				fi.setAlignmentX(Component.LEFT_ALIGNMENT);
				gbc.left(true);gbc.rowEnd();
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.anchor =GridBagConstraints.FIRST_LINE_START;
				gbc.weighty = 0;
				gbc.weightx = 1;
				this.itemsPanel.add(fi, gbc);
				gbc.newRow();
			}
		}
		gbc.weighty = 1;
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(0,0));
		this.itemsPanel.add(jp, gbc);
		this.unSelectAll();
		this.modelViewPanel.descPanel.setText("");
		this.modelViewPanel.mapField.setText("");

		updateUI();
	}

	/**
	 * 
	 */
	protected void storeCurrentMapping() {
		for( FieldItem it: items) {
			if( it.selected ) {
				it.mappingStmt = this.modelViewPanel.mapField.getText();
				this.modelViewPanel.notifyChange();
				return;
			}
		}	
	}
	
	/**
	 * @param with_dialog
	 * @return
	 */
	public boolean checkContent(boolean with_dialog) {
		FieldItem sit = null;
		for( FieldItem it: items) {
			if( it.selected ) {
				sit = it;
				break;
			}
		}	
		if( sit == null ) {
			if( with_dialog)AdminComponent.showInputError(this.getParent(), "No selected DM field");	
			return false;
		}
		String stmt = this. formatMappingText(sit.uth);
		try {
			if(stmt.length() == 0 ) {
				sit.setNotSet();
				return true;								
			}
			SQLQuery squery = new SQLQuery();
			String from;
			MetaClass mc = this.modelViewPanel.metaClass;
			MetaCollection mco= this.modelViewPanel.metaCollection;
			
			if( mc == null && mco == null ) {
				sit.setFailed();			
				if( with_dialog)AdminComponent.showInputError(this.getParent(), "No selected data tree node");	
				return false;
			}
			else if( mc != null ) {
				from = mc +  ", " + Database.getCachemeta().getCollectionTableName(mco.getName(), mc.getCategory());
			} else {
				from = Database.getCachemeta().getCollectionTableName(mco.getName(), Category.getCategory(this.modelViewPanel.category));
			}
			if( squery.run("Select " + stmt + " from " + from + "  limit 1") != null ) {
				sit.setOK();
				squery.close();
				return true;			
			}
			else {
				AdminComponent.showInputError(this.getParent(), "Expression <" + this.modelViewPanel.mapField.getText() + "> can not be computed in SQL on table " + mc.getName());
				squery.close();
				sit.setFailed();			
			}
		} catch (Exception e) {
			sit.setFailed();			
			if( with_dialog)AdminComponent.showInputError(this.getParent(), e.toString());
		}
		return false;
	}

	/**
	 * @param uth
	 * @return
	 */
	private String formatMappingText(UTypeHandler uth) {
		String mt = this.modelViewPanel.mapField.getText().trim();
		if( mt.length() == 0 ) {
			mt = "'null'";
		}
		if( mt.startsWith("'") ){
			if( uth.getType().equals("char")) {
				return mt;
			}
			/*
			 * Remove quotes for numerics
			 */
			else {
				return mt.replaceAll("'", "");														
			}
		}
		else if( mt.startsWith("\"") ){
			if( uth.getType().equals("char")) {
				return mt.replaceAll("\"", "'");		
			}
			/*
			 * Remove quotes for numerics
			 */
			else {
				return mt.replaceAll("\"", "");														
			}
		}
		else {
			String mtt = mt.trim();
			if( mtt.startsWith("(") && mtt.endsWith(")") ) {
				return mtt;
			}
			else {
				return "(" + mtt + ")";
			}
		}		
	}


	@SuppressWarnings("serial")
	public class FieldItem extends JPanel {
		public  final Color SELECTED = AdminComponent.OLD_HEADER;
		public  final Color UNSELECTED = Color.WHITE;
		protected UTypeHandler uth;
		private JLabel label = new JLabel();
		private boolean selected = false;
		private boolean passed = false;
		private String mappingStmt;
		private Color borderColor;

		protected FieldItem(UTypeHandler uth) throws SaadaException {
			this.uth = uth;
			this.setUI();
		}
		private void setUI() {
			this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			Border border;
			String label;

			switch( uth.getRequ_level()) {
			case UTypeHandler.MANDATORY: 
				label = uth.getNickname() + " (MAN)";
				borderColor = Color.BLACK;
				break;
			case UTypeHandler.RECOMMENDED: 				
				label = uth.getNickname() + " (REC)";
				borderColor = Color.DARK_GRAY;
				break;
			default:
				label = uth.getNickname() + " (OPT)";
				borderColor = Color.GRAY;
			}
			border = BorderFactory.createLineBorder(borderColor);
			this.setBorder(border);
			this.label.setText(label);
			this.label.setOpaque(false);
			this.add(this.label);
			this.setBackground(UNSELECTED);
			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			this.addMouseListener(new MouseAdapter() {		
				public void mouseReleased(MouseEvent arg0) {
					ModelFieldList mfl= ModelFieldList.this;
					mfl.storeCurrentMapping();
					mfl.modelViewPanel.setUtypeHandler(uth, mappingStmt);
					mfl.unSelectAll();
					select();
					// Taking the focus notifies the text field that a change must be processed 
					mfl.requestFocus();
				}		
			});
			updateUI();
		}
		protected boolean passFilter(String nameFilter, boolean mand, boolean notset, boolean failed) {
			if( nameFilter != null && nameFilter.length() > 0 && uth.getNickname().indexOf(nameFilter) == -1 ) {
				return false;
			}
			if( mand && uth.getRequ_level()!=  UTypeHandler.MANDATORY) {
				return false;
			}
			if( notset && mappingStmt != null && mappingStmt.length() > 0 ) {
				return false;
			}
			if( failed && (passed || mappingStmt == null ||  mappingStmt.length() == 0) ) {
				return false;
			}
			return true;
		}

		public void select() {
			this.setBackground(SELECTED);
			if( this.label.getForeground() == Color.black ) {
				this.label.setForeground(Color.WHITE);
			}
			this.selected = true;
		}
		public void unSelect() {
			this.setBackground(UNSELECTED);
			if( this.label.getForeground() == Color.white ) {
				this.label.setForeground(Color.BLACK);
			}
			this.selected = false;
		}
		public boolean isSelected() {
			return selected;
		}
		public void setFailed() {
			this.label.setForeground(AdminComponent.KO_COLOR);
			this.passed = false;
		}
		public void setOK() {
			this.label.setForeground(AdminComponent.OK_COLOR);
			this.passed = true;
		}
		public void setNotSet() {
			this.label.setForeground(Color.BLACK);
			this.passed = true;
		}
	}

}
