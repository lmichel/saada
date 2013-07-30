package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadRelationCreate;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.CollectionTextField;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.sqltable.SQLTable;
import saadadb.util.RegExp;

/** * @version $Id: MappingRelationPanel.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 * 
 */
public class RelationCreatePanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	protected JPanel rel_name_panel;
	protected JPanel coll_panel;
	protected JPanel qual_panel;

	protected FreeTextField commentField;
	protected RunTaskButton runButton;
	protected NodeNameTextField nameField ;
	protected NodeNameTextField qualifField ;
	private CollectionTextField primaryField;
	private CollectionTextField secondaryField;
	private JButton qualAdd;
	private JButton qualDel;
	protected NodeNameTextField qualName;
	protected JList<String> qualList;
	protected JScrollPane jsp;
	protected DefaultListModel<String> lstModel;

	public RelationCreatePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, CREATE_RELATION, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationCreate(rootFrame, this, CREATE_RELATION);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootOrCollectionLevel())
		{
			showInputError(rootFrame, "You must select either a collection, a category (IMAGE, SPECTRUM, ...) or a class.");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) ) 
		{
			String pf = dataTreePath.collection + "." + dataTreePath.category.toUpperCase();
			if( !pf.equals(this.primaryField.getText()))
			{
				this.primaryField.setText(pf);	
			}
		}
	}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();

		String name = this.nameField.getText();
		if( SQLTable.tableExist(name)  ) {
			AdminComponent.showFatalError(rootFrame, "The name <" + name + "> is already used");
			return retour;
		}
		else if( !name.matches(RegExp.CLASSNAME) ) {
			AdminComponent.showFatalError(rootFrame, "Relation name <" + name + "> badly formed");			
			return retour;
		}
		else if( primaryField.getText().trim().length() == 0 ) {
			AdminComponent.showInfo(rootFrame, "No primary collection given");						
			return retour;
		}
		else if( secondaryField.getText().trim().length() == 0 ) {
			AdminComponent.showFatalError(rootFrame, "No secondary collection given");						
			return retour;
		}
		else {
			try {
				RelationConf relationConfiguration = new RelationConf();
				String[] collcat = primaryField.getText().trim().split("\\.");
				relationConfiguration.setNameRelation(name);
				relationConfiguration.setDescription(this.commentField.getText().trim());
				relationConfiguration.setColPrimary_name(collcat[0]);
				relationConfiguration.setColPrimary_type(Category.getCategory(collcat[1]));
				collcat = secondaryField.getText().trim().split("\\.");

				relationConfiguration.setColSecondary_name(collcat[0]);
				relationConfiguration.setColSecondary_type(Category.getCategory(collcat[1]));
				relationConfiguration.setClass_name(name);	
				for( int i=0 ; i<this.qualList.getModel().getSize() ; i++ ) {
					relationConfiguration.setQualifier(qualList.getModel().getElementAt(i).toString(), "double");
				}
				retour.put("config", relationConfiguration);
				relationConfiguration.save();
				return retour;
			} catch(Exception e) {
				showFatalError(rootFrame, e);
			}
		}
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setSelectedResource(java.lang.String, java.lang.String)
	 */
	public void setSelectedResource(String label, String explanation) {	
		super.setSelectedResource(label, explanation);
	}

	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);

		MyGBC mc = new MyGBC(5,5,5,5);

		rel_name_panel = this.addSubPanel("Relationship");
		mc.right(false);
		JLabel relationNameLabel = getPlainLabel("Relation Name");
		rel_name_panel.add(relationNameLabel, mc);

		mc.next(); mc.left(false);
		nameField = new NodeNameTextField(24, RegExp.COLLNAME, runButton);
		rel_name_panel.add(nameField, mc);
		mc.rowEnd();
		mc.newRow(); mc.left(false); mc.next();
		rel_name_panel.add(getHelpLabel(HelpDesk.NODE_NAME), mc);
		mc.rowEnd();
		mc.newRow();mc.right(false);
		rel_name_panel.add(getPlainLabel("Description"), mc);
		mc.next();mc.left(true);mc.gridwidth =2;
		commentField = new FreeTextField(3, 24);
		rel_name_panel.add(new JScrollPane(commentField), mc);
		
		nameField.addKeyListener(new KeyListener() 
		{
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) 
			{
				String source = ((NodeNameTextField)e.getSource()).getText();
				if (source.compareTo("")!=0)
				{
					RelationCreatePanel.this.setPanelEnable("endpoints", true);
					RelationCreatePanel.this.setPanelEnable("qualifier", true);
				}
				else
				{
					RelationCreatePanel.this.setPanelEnable("endpoints", false);
					RelationCreatePanel.this.setPanelEnable("qualifier", false);
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});

		coll_panel = this.addSubPanel("End Points");
		mc.reset(5,5,5,5);
		mc.right(false);
		JLabel fromLabel= getPlainLabel("From");
		fromLabel.setPreferredSize(new Dimension(relationNameLabel.getPreferredSize().width, fromLabel.getPreferredSize().height));
		fromLabel.setHorizontalAlignment(JLabel.RIGHT);
		coll_panel.add(fromLabel, mc);
		mc.next(); mc.left(false);
		primaryField = new CollectionTextField();
		coll_panel.add(primaryField, mc);
		primaryField.setEditable(false);
		mc.rowEnd();
		mc.newRow(); mc.left(false);
		mc.rowEnd();
		coll_panel.add(getHelpLabel(HelpDesk.RELATION_COLLECTIONS), mc);
		mc.newRow();
		mc.right(false);
		coll_panel.add(getPlainLabel("To"), mc);
		mc.next(); mc.left(false);
		secondaryField = new CollectionTextField();
		coll_panel.add(secondaryField, mc);

		qual_panel = this.addSubPanel("Qualifiers");
		lstModel = new DefaultListModel<String>();
		qualAdd = new JButton("Add");
		qualAdd.setEnabled(false);
		qualAdd.setToolTipText("Add the new qualifier to the relationship");
		qualAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				RelationCreatePanel.this.addQualifier();
				RelationCreatePanel.this.qualName.requestFocus();
			}
		});
		qualDel = new JButton("Remove");
		qualDel.setToolTipText("Remove the selected qualifier from the relationship");
		qualDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (RelationCreatePanel.this.qualList.getSelectedIndex()!=-1)
				{
					int[] selectedIndices = qualList.getSelectedIndices();
					for (int i=selectedIndices.length-1 ; i>=0 ; i--)
					{
						RelationCreatePanel.this.lstModel.removeElementAt(selectedIndices[i]);
					}
					if (RelationCreatePanel.this.qualList.getModel().getSize()==0)
						qualDel.setEnabled(false);
				}
			}
		});
		qualDel.setEnabled(false);
		
		qualName = new NodeNameTextField(16, RegExp.EXTATTRIBUTE, qualAdd);
		qualName.addKeyListener(new KeyListener() 
		{
			@Override
			public void keyReleased(KeyEvent e) {}

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) 
			{
				if (e.getKeyChar()==KeyEvent.VK_ENTER)
				{
					RelationCreatePanel.this.addQualifier();
				}
			}
		});
		qualList = new JList<String>(lstModel);
		qualList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jsp = new JScrollPane(qualList);
		jsp.setPreferredSize(new Dimension(170,90));
		jsp.setBorder(BorderFactory.createTitledBorder("List of qualifiers"));

		mc.reset(5,5,5,5);
		mc.left(false);
		JLabel qualifierLabel = getPlainLabel("New qualifier");
		qualifierLabel.setPreferredSize(new Dimension(relationNameLabel.getPreferredSize().width, qualifierLabel.getPreferredSize().height));
		qualifierLabel.setHorizontalAlignment(JLabel.RIGHT);
		qual_panel.add(qualifierLabel, mc);
		mc.next();mc.left(false);
		qual_panel.add(qualName, mc);
		mc.next();mc.left(false);
		qual_panel.add(qualAdd, mc);
		mc.rowEnd();
		mc.gridheight = 1;
		mc.newRow(); mc.next();mc.gridheight = 2;mc.gridwidth = 2;mc.left(false);
		qual_panel.add(getHelpLabel(HelpDesk.RELATION_QUALIFIER), mc);
		mc.rowEnd();mc.next();
		mc.gridheight = 1;mc.gridheight = 1;mc.left(true);
		mc.insets = new Insets(2, 5, 2, 5);
		qual_panel.add(jsp, mc);
		mc.newRow();mc.next();mc.next();mc.next();mc.left(false);
		qualDel.setPreferredSize(new Dimension(jsp.getPreferredSize().width,qualDel.getPreferredSize().height));
		qual_panel.add(qualDel, mc);
		mc.rowEnd();
		
		this.setPanelEnable("endpoints", false);
		this.setPanelEnable("qualifier", false);
		
		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
	}
	
	private void addQualifier()
	{
		String name = qualName.getText().trim();
		for( int i=0 ; i<RelationCreatePanel.this.qualList.getModel().getSize() ;  i++ ) {
			if( RelationCreatePanel.this.qualList.getModel().getElementAt(i).toString().equals(name)) {
				JOptionPane.showMessageDialog(rootFrame,
						"Duplicate qualifier <" + name + "> ",
						"Configuration Error",
						JOptionPane.ERROR_MESSAGE);		
				return ;
			}
		}
		qualDel.setEnabled(true);
		qualName.setText("");
		RelationCreatePanel.this.lstModel.addElement(name);
		if (RelationCreatePanel.this.lstModel.getSize()>0)
		{
			RelationCreatePanel.this.qualList.setSelectedIndex(RelationCreatePanel.this.lstModel.getSize()-1);
		}
	}
	
	private void setPanelEnable(String panelName, boolean enabled)
	{
		if (panelName.compareTo("endpoints")==0)
		{
			coll_panel.setEnabled(enabled);
			primaryField.setEnabled(enabled);
			secondaryField.setEnabled(enabled);
		}
		
		if (panelName.compareTo("qualifier")==0)
		{
			qual_panel.setEnabled(enabled);
			qualName.setEnabled(enabled);
			qualList.setEnabled(enabled);
			jsp.setEnabled(enabled);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));
	}
}
