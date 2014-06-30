/*
 * This package is (probably) temporary
 */
package saadadb.admintool.VPSandbox;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.LoaderConfigChooser;
import saadadb.admintool.components.RenameButton;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.SwitchButtonWIP;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.mapper.ClassMapperPanel;
import saadadb.admintool.components.mapper.CoordSysMapperPanel;
import saadadb.admintool.components.mapper.DispersionMapperPanel;
import saadadb.admintool.components.mapper.ExtAttMapperPanel;
import saadadb.admintool.components.mapper.ExtensionTextFieldPanel;
import saadadb.admintool.components.mapper.MappingTextfieldPanel;
import saadadb.admintool.components.mapper.PositionErrorMapperPanel;
import saadadb.admintool.components.mapper.PositionMapperPanel;
import saadadb.admintool.dialogs.DialogConfName;
import saadadb.admintool.dialogs.DialogFileChooser;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.panels.tasks.DataLoaderPanel;
import saadadb.admintool.tree.VoDataProductTree;
import saadadb.api.SaadaDB;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;
import saadadb.util.RegExp;


/**
 * This class is a replacement for MappingKWPanel. It is the source code of the form allowing you to create and edit
 * filters when you load a data.
 * 
 * @author pertuy
 * @version $Id$
 */

// Héritage temporaire (évite les modifications..)
public class VPSTOEPanel extends MappingKWPanel {

//	
//	private GridBagConstraints globalGridConstraint ;
//	private GridBagConstraints e_globalGridConstraint ;
//	private String last_saved = "";
//	private JPanel editorPanel;
//	//private JPanel e_editorPanel;
//		
//	private int category;
//	private String confName = "Default";
//	protected SwitchButtonWIP switchButton;
//	
//	
//	
//	//Old attributes from MAppingKwPanel
//	private MappingTextfieldPanel nameMapper;
//	private MappingTextfieldPanel e_nameMapper;
//	private MappingTextfieldPanel ignoredMapper;
//	private MappingTextfieldPanel e_ignoredMapper;
//	private ExtensionTextFieldPanel extensionMapper;
//	private DispersionMapperPanel dispersionMapper;
//	private ClassMapperPanel classMapper;
//	private ExtAttMapperPanel extAttMapper;
//	private ExtAttMapperPanel e_extAttMapper;
//	private CoordSysMapperPanel cooSysMapper;
//	private PositionMapperPanel positionMapper;
//	private PositionMapperPanel e_positionMapper;
//	private PositionErrorMapperPanel positionErrorMapper;
//	private PositionErrorMapperPanel e_positionErrorMapper;
//	protected  LoaderConfigChooser configChooser;
//	private Container category_panel;
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/*
	 * Gardez la même gestion de catégorie qu'avant ? ou modifier notre façon de faire ?
	 */
	public VPSTOEPanel(AdminTool rootFrame, String title, int category,
			String ancestor) {
		super(rootFrame, title, category, ancestor);
		// this.category = category;
		// TODO Auto-generated constructor stub
	}




	@Override
	protected void setActivePanel() {
		// TODO Auto-generated method stub
		// this code have been copied from the old class MappingKwPanel
		
		globalGridConstraint = new GridBagConstraints();
		globalGridConstraint.weightx = 1;			
		globalGridConstraint.fill = GridBagConstraints.HORIZONTAL;
		globalGridConstraint.anchor = GridBagConstraints.PAGE_START;
		globalGridConstraint.gridx = 0;
		globalGridConstraint.gridy = 0;

		GridBagConstraints localGridConstraint = new GridBagConstraints();
		localGridConstraint.weightx = 1;			
		localGridConstraint.weighty = 1;			
		localGridConstraint.fill = GridBagConstraints.BOTH;
		localGridConstraint.anchor = GridBagConstraints.NORTH;
		localGridConstraint.gridx = 0;
		localGridConstraint.gridy = 0;

		
		JPanel tPanel = this.addSubPanel("Filter Editor");
		editorPanel = new JPanel( );
		editorPanel.setBackground(LIGHTBACKGROUND);
		editorPanel.setLayout(new GridBagLayout());
		this.addCategoryPanel();
		globalGridConstraint.gridy++;

//		e_editorPanel = new JPanel( );
//		e_editorPanel.setBackground(LIGHTBACKGROUND);
//		e_editorPanel.setLayout(new GridBagLayout());

		//globalGridConstraint.gridy++;
		
		/*
		 * Basé sur les appels fait dans MappingKWPanel
		 */
		
		
		buildAxis();
		

		//this.add(axisTest2.axisPanel);
		
		tPanel.add(new JScrollPane(editorPanel),localGridConstraint);

		this.setActionBar();
		this.setConfName("Default");
		
	}
	
	/**
	 * Build the axis panels and add them to the editor_panel
	 */
	
	
	private void buildAxis()
	{
		//Build the Class-Mapping axis (include the extension name choice)
		VPClassMappingPanel classMappingAxis = new VPClassMappingPanel(this);
		classMappingAxis.expand();
		editorPanel.add(classMappingAxis.container,globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		//Build the observable Axis
		VPObservableMappingPanel ObservableAxis = new VPObservableMappingPanel();
		ObservableAxis.collapse();
		editorPanel.add(ObservableAxis.container, globalGridConstraint);

		
		//WIP
	}
	
/*
 * From this point, olds functions from MappingKWPanel (non-Javadoc)
 * @see saadadb.admintool.panels.EditPanel#setActionBar()
 */
	


	

}
