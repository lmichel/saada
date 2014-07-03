/*
 * This package is (probably) temporary
 */
package saadadb.admintool.VPSandbox.panels.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.VPSandbox.components.mapper.VPClassMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPEnergyMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPObservableMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPObservationMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPSpaceMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPTimeMappingPanel;
import saadadb.admintool.panels.editors.MappingKWPanel;


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
	private VPClassMappingPanel classMapping;
	private VPObservationMappingPanel observationMapping;
	private VPSpaceMappingPanel spaceMapping;
	private VPTimeMappingPanel timeMapping;
	private VPObservableMappingPanel observableMapping;
	private VPEnergyMappingPanel energyMapping;
	
	
	
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
		classMapping = new VPClassMappingPanel(this);
		classMapping.expand();
		editorPanel.add(classMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		//Build the observable Axis
		//VPObservableMappingPanel ObservableAxis = new VPObservableMappingPanel();
		//ObservableAxis.collapse();
		//editorPanel.add(ObservableAxis.container, globalGridConstraint);
		observationMapping = new VPObservationMappingPanel(this);
		observationMapping.expand();
		editorPanel.add(observationMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		spaceMapping = new VPSpaceMappingPanel(this);
		spaceMapping.collapse();
		editorPanel.add(spaceMapping.getContainer(),globalGridConstraint);
		
		
		globalGridConstraint.gridy++;
		
		energyMapping = new VPEnergyMappingPanel(this);
		energyMapping.collapse();
		editorPanel.add(energyMapping.getContainer(),globalGridConstraint);
		

		globalGridConstraint.gridy++;
		
		timeMapping = new VPTimeMappingPanel(this);
		timeMapping.collapse();
		editorPanel.add(timeMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		observableMapping = new VPObservableMappingPanel(this);
		observableMapping.collapse();
		editorPanel.add(observableMapping.getContainer(),globalGridConstraint);

		
		//WIP
	}
	
/*
 * From this point, olds functions from MappingKWPanel (non-Javadoc)
 * @see saadadb.admintool.panels.EditPanel#setActionBar()
 */
	


	

}
