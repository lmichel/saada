package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadMangeExtAttr;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.components.input.UcdTextField;
import saadadb.admintool.components.input.UnitTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.admintool.windows.VoTreeFrame;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;


@SuppressWarnings("serial")
public class ExtendedAttPanel extends TaskPanel {
	protected NodeNameTextField nameField ;
	protected UnitTextField unitField;
	protected UcdTextField ucdField;
	protected UcdTextField utypeField;
	protected FreeTextField commentField;
	protected RunTaskButton runButton;
	protected int help_key = HelpDesk.COLL_CREATE;
	protected JRadioButton newBtn ;
	protected JRadioButton dropBtn ;
	protected JRadioButton renameBtn;
	protected ButtonGroup buttonGroup;
	protected JComboBox existingAttList ;
	protected JComboBox typeList ;
	protected JLabel nodeLabel;
	protected Map<String, AttributeHandler> ahMap;
	private int categoryNum;
	private String categoryName;

	public ExtendedAttPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, MANAGE_EXTATTR, null, ancestor);
		this.cmdThread = new ThreadMangeExtAttr(rootFrame, MANAGE_EXTATTR);
	}
	protected ExtendedAttPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.cmdThread = cmdThread;
	}

	@Override
	public void initCmdThread() {
		this.cmdThread = new ThreadMangeExtAttr(rootFrame, MANAGE_EXTATTR);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath)  {
		super.setDataTreePath(dataTreePath);
		nameField.setText("");
		commentField.setText("");
		existingAttList.removeAllItems();
		if(dataTreePath != null &&  dataTreePath.isCategoryLevel() ){
			try {
				categoryNum = Category.getCategory(dataTreePath.category);
				categoryName = dataTreePath.category;
				ahMap = Database.getCachemeta().getAtt_extend(categoryNum);
				for( AttributeHandler ah: ahMap.values()) {
					existingAttList.addItem(ah.getNameattr());
				}
			} catch (FatalException e) {}
		}
	}

	@Override
	protected Map<String, Object> getParamMap() {		
		Map<String, Object> retour = new LinkedHashMap<String, Object>();
		retour.put("category", categoryName);
		if( newBtn.isSelected() ) {
			retour.put("name",nameField.getText().trim());	
			retour.put("command", "create");
			retour.put("type", typeList.getSelectedItem().toString());
			retour.put("comment",commentField.getText().trim());	
			retour.put("unit",unitField.getText().trim());	
			retour.put("ucd",ucdField.getText().trim());	
			retour.put("utype",utypeField.getText().trim());	
		} else if( dropBtn.isSelected() ) {
			retour.put("command", "remove");
			retour.put("name",existingAttList.getSelectedItem().toString());	
		} else {
			retour.put("command", "rename");
			retour.put("name",existingAttList.getSelectedItem().toString());	
			retour.put("newname", nameField.getText());
			retour.put("type", typeList.getSelectedItem().toString());
			retour.put("comment",commentField.getText().trim());	
			retour.put("unit",unitField.getText().trim());	
			retour.put("ucd",ucdField.getText().trim());	
			retour.put("utype",utypeField.getText().trim());	
		}
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));
	}
	protected void setHelpKey() {
		help_key = HelpDesk.EXTATT_EDIT;
	}
	protected void setNodeLabel() {
		nodeLabel = getPlainLabel("Attribute Name");
	}

	@Override
	protected void setActivePanel() {
		this.setHelpKey();
		this.setNodeLabel();
		runButton = new RunTaskButton(this);
		nameField = new NodeNameTextField(16, "^" + RegExp.EXTATTRIBUTE + "$", runButton);
		nameField.addPropertyChangeListener("value", this);	
		commentField = new FreeTextField(6, 24);
		ucdField = new UcdTextField(20, commentField);
		utypeField = new UcdTextField(20, commentField);
		unitField = new UnitTextField(10);

		newBtn = new JRadioButton("New");
		dropBtn = new JRadioButton("Drop");
		renameBtn = new JRadioButton("Modify");
		buttonGroup = new ButtonGroup();

		buttonGroup.add(newBtn);
		buttonGroup.add(dropBtn);
		buttonGroup.add(renameBtn);	
		existingAttList = new JComboBox();
		existingAttList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {readAH();}
		});
		typeList = new JComboBox(JavaTypeUtility.ATTREXTENDTYPES);

		MyGBC mgbc = new MyGBC(5,5,5,5);		
		JPanel tPanel = this.addSubPanel("Input Parameters");

		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Existing Attributes"), mgbc);		
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(existingAttList, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(nodeLabel, mgbc);		
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(nameField, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Supported Types"), mgbc);		
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(typeList, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Description"), mgbc);
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(commentField.getPanel(), mgbc);


		mgbc.newRow();
		JLabel ds = AdminComponent.getPlainLabel("<HTML><A HREF=>Open Meta Data panel</A> ");
		ds.setToolTipText("Show dataloader parameters matching the current configuration.");
		ds.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e) {
				VoTreeFrame vtf = VoTreeFrame.getInstance(rootFrame, new JComponent[]{unitField, ucdField, utypeField});
				vtf.open(SQLJTable.META_PANEL);
			}
		});
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(ds, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Unit"), mgbc);
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(unitField, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("UCD"), mgbc);
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(ucdField, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("UType"), mgbc);
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		tPanel.add(utypeField, mgbc);

		mgbc.newRow();
		mgbc.anchor = GridBagConstraints.EAST;
		tPanel.add(getPlainLabel("Action"), mgbc);
		mgbc.rowEnd();
		mgbc.anchor =  GridBagConstraints.WEST;
		JPanel jp = new JPanel();
		jp.add(newBtn);
		jp.add(dropBtn);
		jp.add(renameBtn);
		tPanel.add(jp, mgbc);

		mgbc.newRow();
		mgbc.gridwidth=2;
		mgbc.anchor = GridBagConstraints.WEST;
		tPanel.add(getHelpLabel(help_key), mgbc);

		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});			
		newBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {configureForm(e.getSource());}
		});
		dropBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {configureForm(e.getSource());}
		});
		renameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {configureForm(e.getSource());}
		});
		newBtn.setSelected(true);
		configureForm(newBtn);
	}

	/**
	 * Read the selected 
	 */
	private void readAH(){
		AttributeHandler ah;
		if( ahMap != null && existingAttList.getSelectedItem() != null 
				&& (ah = ahMap.get(existingAttList.getSelectedItem().toString())) != null ) {
			if( !newBtn.isSelected() ) {
				nameField.setText(ah.getNameattr());
			} else {
				nameField.setText("");

			}
			commentField.setText(ah.getComment());
			unitField.setText(ah.getUnit());
			utypeField.setText(ah.getUtype());
			ucdField.setText(ah.getUcd());
			for( int i=0 ; i<typeList.getItemCount() ; i++) {
				if( typeList.getItemAt(i).equals(ah.getType()) ) {
					typeList.setSelectedIndex(i);
				}
			}
		}
	}
	/**
	 * enable/disable the components according to the action
	 * @param source
	 */
	private void configureForm(Object source) {
		try {
			if( source == newBtn){
				this.nameField.setEnabled(true);
				this.commentField.setEnabled(true);
				this.existingAttList.setEnabled(false);
				this.typeList.setEnabled(true);
				this.nameField.setText("");
				this.commentField.setText("");
			} else if( Database.getWrapper().supportAlterColumn() ) {
				if( source == dropBtn){
					this.nameField.setEnabled(false);
					this.commentField.setEnabled(false);
					this.existingAttList.setEnabled(true);
					this.typeList.setEnabled(false);
					this.readAH();
				} else {
					this.nameField.setEnabled(true);
					this.commentField.setEnabled(true);
					this.existingAttList.setEnabled(true);
					this.typeList.setEnabled(false);
					this.readAH();
				}
			} else {
				AdminComponent.getHelpLabel(new String[]{"Operation Forbidden "
						, Database.getWrapper().getDBMS() + " does not support to alter table columns"});
				newBtn.setSelected(true);
				configureForm(newBtn);
			}
		} catch(Exception e) {Messenger.printStackTrace(e);}
	} 
}
