package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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

public class RelationCreatePanel extends TaskPanel {
	/** * @version $Id: MappingRelationPanel.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
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
	protected JComboBox  qualList;		


	public RelationCreatePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, CREATE_RELATION, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationCreate(rootFrame, CREATE_RELATION);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) )
			primaryField.setText(dataTreePath.collection + "." + dataTreePath.category.toUpperCase());
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
				for( int i=0 ; i<this.qualList.getItemCount() ; i++ ) {
					relationConfiguration.setQualifier(qualList.getItemAt(i).toString(), "double");
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


	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);

		JPanel tPanel;
		MyGBC mc = new MyGBC(5,5,5,5);

		tPanel = this.addSubPanel("Relationship");
		mc.right(false);
		tPanel.add(getPlainLabel("Relation Name"), mc);

		mc.next();mc.left(false);
		nameField = new NodeNameTextField(16, RegExp.COLLNAME, runButton);
		tPanel.add(nameField, mc);
		mc.rowEnd();
		tPanel.add(getHelpLabel(HelpDesk.NODE_NAME), mc);

		mc.newRow();mc.right(false);
		tPanel.add(getPlainLabel("Description"), mc);
		mc.next();mc.left(true);mc.gridwidth =2;
		commentField = new FreeTextField(2, 24);
		tPanel.add(new JScrollPane(commentField), mc);

		tPanel = this.addSubPanel("End Points");
		mc.reset(5,5,5,5);
		mc.right(false);
		tPanel.add(getPlainLabel("From"), mc);
		mc.next(); mc.left(false);
		primaryField = new CollectionTextField();
		tPanel.add(primaryField, mc);
		primaryField.setEditable(false);
		mc.rowEnd();mc.gridheight = 2;
		tPanel.add(getHelpLabel(HelpDesk.RELATION_COLLECTIONS), mc);

		mc.newRow();
		mc.right(false);
		tPanel.add(getPlainLabel("To"), mc);
		mc.next(); mc.left(false);
		secondaryField = new CollectionTextField();
		tPanel.add(secondaryField, mc);

		tPanel = this.addSubPanel("Qualifiers");
		qualAdd = new JButton("Add");
		qualAdd.setToolTipText("Add the new qualifier to the relationship.");
		qualAdd.addActionListener(new ActionListener() {
			public void actionPerformed(@SuppressWarnings("unused") ActionEvent arg0) {
				String name = qualName.getText().trim();
				for( int i=0 ; i<RelationCreatePanel.this.qualList.getItemCount() ;  i++ ) {
					if( RelationCreatePanel.this.qualList.getItemAt(i).toString().equals(name)) {
						JOptionPane.showMessageDialog(rootFrame,
								"Duplicate qualifier <" + name + "> ",
								"Configuration Error",
								JOptionPane.ERROR_MESSAGE);					
						return ;
					}
				}
				RelationCreatePanel.this.qualList.addItem(name) ;
			}
		});
		qualAdd.setEnabled(false);
		qualDel = new JButton("Remove");
		qualDel.setToolTipText("Remove the selected qualfier from the relationship");
		qualDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RelationCreatePanel.this.qualList.removeItem(RelationCreatePanel.this.qualList.getSelectedItem());
			}
		});
		qualName = new NodeNameTextField(16, RegExp.EXTATTRIBUTE, qualAdd);
		qualList = new JComboBox();		

		mc.reset(5,5,5,5);
		mc.right(false);
		tPanel.add(getPlainLabel("Name"), mc);
		mc.next();mc.left(false);
		tPanel.add(qualName, mc);
		mc.next();
		tPanel.add(qualAdd, mc);
		mc.rowEnd();mc.gridheight = 2;
		tPanel.add(getHelpLabel(HelpDesk.RELATION_QUALIFIER), mc);
		mc.newRow();mc.right(false);
		tPanel.add(getPlainLabel("List"), mc);
		mc.next();mc.left(false);
		tPanel.add(qualList, mc);
		mc.next();
		tPanel.add(qualDel, mc);

		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}
}
