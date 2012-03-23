package saadadb.admintool.components.voresources;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.DMAttributeTextField;
import saadadb.admintool.panels.tasks.ObscoreMapperPanel;
import saadadb.admintool.tree.FilteredFieldTree;
import saadadb.admintool.tree.VoClassTree;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.admintool.windows.DataTableWindow;
import saadadb.admintool.windows.MappedTableWindow;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;


public class ModelViewPanel extends JPanel {
	ObscoreMapperPanel obscoreMapperPanel;
	private ModelFieldList resourceList;
	protected JEditorPane descPanel;
	protected DMAttributeTextField mapField;
	private JButton checkButton;
	private JButton quickLookButton;
	private JButton resetButton;
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
		mgbc.weightx = 1; mgbc.weighty = 1;mgbc.anchor = GridBagConstraints.NORTH;mgbc.gridheight= 2;
		this.setLayout(new GridBagLayout());

		this.resourceList = new ModelFieldList(this);
		this.resourceList.setPreferredSize(new Dimension(230,400));
		this.add(this.resourceList, mgbc);

		this.descPanel = new JEditorPane("text/html", "");
		this.descPanel.setEditable(false);
		this.descPanel.setPreferredSize(new Dimension(230, 220));
		this.mapField = new DMAttributeTextField();
		this.mapField.setColumns(24);
		this.checkButton = new JButton("Check Mapping");
		this.resetButton = new JButton("Reset");
		this.quickLookButton = new JButton("QuickLook");
		JPanel jp = new JPanel();
		jp.setBorder(BorderFactory.createTitledBorder("Field Mapper"));
		jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));	
		jp.setPreferredSize(new Dimension(230,300));

		JScrollPane js = new JScrollPane(descPanel);
		js.setPreferredSize(new Dimension(230, 220));
		jp.add(js);
		jp.add(new JLabel("Mapping Statement"));
		jp.add(mapField);
		jp.add(checkButton);
		mgbc.next();
		this.add(jp,mgbc);

		mgbc.gridheight= 1;mgbc.gridy++;		
		jp = new JPanel();
		jp.setPreferredSize(new Dimension(230,50));
		jp.setBackground(AdminComponent.LIGHTBACKGROUND);
		//jp.setBorder(BorderFactory.createTitledBorder(""));
		//jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));	
		jp.setLayout(new FlowLayout());	
		this.resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		jp.add(resetButton);
		this.quickLookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		jp.add(quickLookButton);
		this.add(jp,mgbc);

		mgbc.next(); 
		mgbc.rowEnd();mgbc.gridheight= 2;mgbc.gridy--;
		this.classTree = new FilteredFieldTree("Saada Attributes", 230,400);				
		this.add(classTree, mgbc);

		mgbc.newRow();mgbc.gridy++;mgbc.gridwidth = 3;
		this.add(AdminComponent.getHelpLabel(HelpDesk.MODEL_MAPPER), mgbc);

		this.updateUI();
		mapField.addFocusListener(new FocusListener() {		
			public void focusLost(FocusEvent arg0) {
				try {
					resourceList.storeCurrentMapping();
				} catch (IOException e) {
					AdminComponent.showFatalError(getParent(), e);
				}
			}			
			public void focusGained(FocusEvent arg0) {}
		});
		mapField.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent arg0) {
				if( resourceList.checkContent(true) ) {
					try {
						resourceList.storeCurrentMapping();
					} catch (IOException e) {
						AdminComponent.showFatalError(getParent(), e);
					}
				}
			}			
		});
		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( resourceList.checkContent(true) ) {
					try {
						resourceList.storeCurrentMapping();
					} catch (IOException e) {
						AdminComponent.showFatalError(getParent(), e);
					}
				}
			}
		});
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resourceList.resetMapping();
				mapField.setText("");
			}
		});
		quickLookButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( metaClass == null ) {
					AdminComponent.showInputError(getParent(), "No class selected");	
				} else if( !resourceList.checkContent()){
					AdminComponent.showInputError(getParent(), "Fix mapping errors first (red items)");	
				} else {
						try {
						MappedTableWindow dtw = new MappedTableWindow(ModelViewPanel.this.obscoreMapperPanel.rootFrame
								, resourceList.getQuery());
						dtw.open();
					} catch( Exception e) {
						AdminComponent.showFatalError(getParent(), e);
					}
				}
			}
		});

	}

	public boolean setDataTreePath(DataTreePath dataTreePath) throws Exception {
		this.descPanel.setText("");
		resourceList.resetFields();
		if( dataTreePath.isCategoryLevel() ) {
			metaClass = null;
			metaCollection = Database.getCachemeta().getCollection(dataTreePath.collection);
			category = dataTreePath.category;
			classTree.setAttributeHandlers(category);
			mapField.setAttributeHandlers(category);
			return true;
		} else if( dataTreePath.isClassLevel() ) {
			metaClass = Database.getCachemeta().getClass(dataTreePath.classe);
			metaCollection = null;
			category = dataTreePath.category;
			classTree.setAttributeHandlers(metaClass);
			mapField.setAttributeHandlers(metaClass);
			resourceList.loadMapping();
			return true;
		} else {
			AdminComponent.showInputError(obscoreMapperPanel.rootFrame, "Selet a data tree leaf (class level)");
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
				"<table cellpadding=2 cellspacing=0><TR><TD ALIGN=RIGHT><B><FONT size=-1>Name</TD><TD><FONT size=-1>" + uth.getNickname() + "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B><FONT size=-1>Type</TD><TD><FONT size=-1>" + uth.getType()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B><FONT size=-1>UType</TD><TD><FONT size=-1>" + uth.getUtype()+ "</TD></TR>"
				+ "<TR><TD ALIGN=RIGHT><B><FONT size=-1>Unit</TD><TD><FONT size=-1>" + uth.getUnit()+ "</TD></TR></TABLE>"
				+ "<HR><FONT size=-1>" + uth.getComment() + "<HR>"
		);	
	}

	public void notifyChange() {
		obscoreMapperPanel.notifyChange();
	}

}
