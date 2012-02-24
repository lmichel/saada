package saadadb.admintool.components.voresources;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.DMAttributeTextField;
import saadadb.admintool.panels.tasks.ObscoreMapperPanel;
import saadadb.admintool.tree.FilteredFieldTree;
import saadadb.admintool.tree.VoClassTree;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;


public class ModelViewPanel extends JPanel {
	private ObscoreMapperPanel obscoreMapperPanel;
	private ModelFieldList resourceList;
	protected JEditorPane descPanel;
	protected DMAttributeTextField mapField;
	private JButton checkButton;
	private FilteredFieldTree classTree;

	protected MetaClass metaClass;
	protected MetaCollection metaCollection;
	protected String category;
	
	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws Exception 
	 */
	public ModelViewPanel(ObscoreMapperPanel obscoreMapperPanel) throws Exception {
		this.obscoreMapperPanel = obscoreMapperPanel;
		
		this.setBackground(AdminComponent.LIGHTBACKGROUND);

		MyGBC mgbc = new MyGBC(5,5,5,5);
		mgbc.weightx = 1; mgbc.weighty = 1;mgbc.anchor = GridBagConstraints.NORTH;
		this.setLayout(new GridBagLayout());
		
		this.resourceList = new ModelFieldList(this);
		this.resourceList.setPreferredSize(new Dimension(230,400));
		this.add(this.resourceList, mgbc);
		
		this.descPanel = new JEditorPane("text/html", "");
		this.descPanel.setEditable(false);
		this.descPanel.setPreferredSize(new Dimension(230, 220));
		this.mapField = new DMAttributeTextField();
		this.mapField.setColumns(24);
		this.checkButton = new JButton("Check and Store");
		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createTitledBorder("Field Mapper"));
		jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));	
		jp.setPreferredSize(new Dimension(230,300));

		jp.add(new JLabel("Field Description"));
		JScrollPane js = new JScrollPane(descPanel);
		js.setPreferredSize(new Dimension(230, 220));
		jp.add(js);
		jp.add(new JLabel("Mapping Statement"));
		jp.add(mapField);
		jp.add(checkButton);
		mgbc.next();
		this.add(jp,mgbc);
		
		mgbc.rowEnd();
		this.classTree = new FilteredFieldTree("Saada Attributes", 230,400);				
		this.add(classTree, mgbc);
		
		mgbc.newRow(); mgbc.gridwidth = 3;
		this.add(AdminComponent.getHelpLabel(HelpDesk.MODEL_MAPPER), mgbc);

		this.updateUI();
		mapField.addFocusListener(new FocusListener() {		
			public void focusLost(FocusEvent arg0) {
				resourceList.storeCurrentMapping();
			}			
			public void focusGained(FocusEvent arg0) {}
		});
		mapField.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				resourceList.storeCurrentMapping();			
			}
		});
		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resourceList.checkContent(true);
			}
		});
	}

	public boolean setDataTreePath(DataTreePath dataTreePath) throws SaadaException {
		if( dataTreePath.isCategoryLevel() ) {
			metaClass = null;
			metaCollection = Database.getCachemeta().getCollection(dataTreePath.collection);
			category = dataTreePath.category;
			classTree.setAttributeHandlers(category);
			mapField.setAttributeHandlers(category);
			resourceList.resetFields();
			return true;
		} else if( dataTreePath.isClassLevel() ) {
			metaClass = Database.getCachemeta().getClass(dataTreePath.classe);
			metaCollection = null;
			category = dataTreePath.category;
			classTree.setAttributeHandlers(metaClass);
			mapField.setAttributeHandlers(metaClass);
			resourceList.resetFields();
			return true;
		} else {
			resourceList.resetFields();
			AdminComponent.showInputError(obscoreMapperPanel.rootFrame, "Selet a data tree node either at category or class level");
			return false;
		}
	}
	
	public void setDescription(String description){
		this.descPanel.setText(description);
	}
	public String getDescription() {
		return this.descPanel.getText();
	}
	public boolean isEmpty() {
		return (this.resourceList.items.size() == 0) ? true: false;
	}
	public VOResource getVor() {
		return obscoreMapperPanel.getVor();
	}

	public void setUtypeHandler(UTypeHandler uth, String mappingStmt) {
		this.mapField.setText(mappingStmt);
		this.descPanel.setText(
				"<table><TR><TD ALIGN=RIGHT><B>Name</TD><TD>" + uth.getNickname() + "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>Type</TD><TD>" + uth.getType()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>UType</TD><TD>" + uth.getUtype()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>Unit</TD><TD>" + uth.getUnit()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B>Description</TD><TD>" + uth.getComment()+ "</TD></TR></TABLE><BR>"
		);	
	}

	public void notifyChange() {
		obscoreMapperPanel.notifyChange();
	}

}
