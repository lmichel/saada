package saadadb.admintool.components.voresources;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
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
	private JEditorPane descPanel;
	protected DMAttributeTextField mapField;
	private JButton checkButton;
	private FilteredFieldTree classTree;

	private MetaClass metaClass;
	private MetaCollection metaCollection;
	private String category;


	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws Exception 
	 */
	public ModelViewPanel(ObscoreMapperPanel obscoreMapperPanel) throws Exception {
		this.obscoreMapperPanel = obscoreMapperPanel;
		this.resourceList = new ModelFieldList(this);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);

		MyGBC mgbc = new MyGBC(5,5,5,5);
		mgbc.gridx = 0; mgbc.gridy = 0;mgbc.weightx = 1; mgbc.weighty = 1;mgbc.gridheight = 5;
		this.setLayout(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(this.resourceList);
		jsp.setPreferredSize(new Dimension(200,250));
		this.add(jsp, mgbc);
		
		mgbc.next();mgbc.gridheight = 1;
		this.add(new JLabel("Field Description"), mgbc);
		
		mgbc.next();mgbc.gridheight = 5;
		classTree = new FilteredFieldTree("Saada Attributes", 250, 170);		
		this.add(classTree, mgbc);

		mgbc.newRow(); mgbc.gridx = 1;mgbc.gridheight = 1;
		descPanel = new JEditorPane("text/html", "");
		descPanel.setEditable(false);
		JScrollPane js = new JScrollPane(descPanel);
		js.setPreferredSize(new Dimension(250, 170));
		descPanel.setPreferredSize(new Dimension(250, 170));
		this.add(js, mgbc);

		mgbc.newRow(); mgbc.gridx = 1;
		this.add(new JLabel("Mapping Statement"), mgbc);

		mgbc.newRow(); mgbc.gridx = 1;
		mapField = new DMAttributeTextField();
		mapField.setColumns(24);
		this.add(mapField, mgbc);

		mgbc.newRow(); mgbc.gridx = 1;
		checkButton = new JButton("Check and Store");
		this.add(checkButton, mgbc);
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
	}

	public boolean setDataTreePath(DataTreePath dataTreePath) throws SaadaException {
		if( dataTreePath.isCategoryLevel() ) {
			metaClass = null;
			metaCollection = Database.getCachemeta().getCollection(dataTreePath.collection);
			category = dataTreePath.category;
			classTree.setAttributeHandlers(MetaCollection.getAttribute_handlers(Category.getCategory(category)));
			return true;
		} else if( dataTreePath.isClassLevel() ) {
			metaClass = Database.getCachemeta().getClass(dataTreePath.classe);
			metaCollection = null;
			category = dataTreePath.category;
			classTree.setAttributeHandlers(metaClass.getAttributes_handlers());
			return true;
		} else {
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
				+ "<TR><TD ALIGN=RIGHT><B>Descrption</TD><TD>" + uth.getComment()+ "</TD></TR></TABLE><BR>"
		);	
	}

	public void notifyChange() {
		obscoreMapperPanel.notifyChange();
	}

}
