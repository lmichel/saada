/*
 * This package is (probably) temporary
 */
package saadadb.admintool.VPSandbox.panels.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.VPSandbox.components.mapper.VPClassMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPEnergyMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPIgnoredEntryMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPIgnoredMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPSpaceEntryMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPObservableMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPObservationEntryMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPObservationMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPOtherEntryMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPOtherMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPSpaceMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPTimeMappingPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;


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
	private VPOtherMappingPanel otherMapping;
	private VPIgnoredMappingPanel ignoredMapping;
	
	
	//private JTabbedPane tabs;
	
	
	//protected ArgsParser args;
	
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
		 //this.category = category;

		// TODO Auto-generated constructor stub
	}

//
//	public ArgsParser getArgs()
//	{
//		return args;
//	}


	@Override
	protected void setActivePanel() {
		// TODO Auto-generated method stub
		// this code have been copied from the old class MappingKwPanel
//		if(checkParams())
//			args = getArgsParser();
		

		
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
		

		editorPanel = new JPanel();
		//test.addTab("Normal Panel", editorPanel);
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
//		localGridConstraint.gridy++;
//		tPanel.add(new JScrollPane(tabs),localGridConstraint);
		this.setActionBar();
		this.setConfName("Default");

		
	}
	
	/**
	 * Build the axis panels and add them to the editor_panel
	 */
	
	
	private void buildAxis()
	{
		
		/*
		 * Category set here because this code is executed by a super class creator
		 * before the category parameter is used
		 */
		if( this.title.equals(MISC_MAPPER) ){
			category = Category.MISC;
		}
		if( this.title.equals(FLATFILE_MAPPER) ){
			category = Category.FLATFILE;
		}
		if( this.title.equals(IMAGE_MAPPER) ){
			category = Category.IMAGE;
		}
		if( this.title.equals(SPECTRUM_MAPPER) ){
			category = Category.SPECTRUM;
		}
		if( this.title.equals(TABLE_MAPPER) ){
			category = Category.TABLE;
		}
		
		//Temporaire
		if(this.title.equals(NEW_MAPPER))
			category=Category.TABLE;
		

		//Build the Class-Mapping axis (include the extension name choice)
		System.out.println(this.category);
		classMapping = new VPClassMappingPanel(this);
		classMapping.expand();
		editorPanel.add(classMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		if(this.category==Category.ENTRY || this.category==Category.TABLE)
		{
			observationMapping = new VPObservationEntryMappingPanel(this);
			observationMapping.expand();
			editorPanel.add(observationMapping.getContainer(),globalGridConstraint);
			
			globalGridConstraint.gridy++;

			spaceMapping = new VPSpaceEntryMappingPanel(this);
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

			observableMapping = new VPObservableMappingPanel(this,rootFrame);
			observableMapping.collapse();
			editorPanel.add(observableMapping.getContainer(),globalGridConstraint);

			globalGridConstraint.gridy++;

			ignoredMapping=new VPIgnoredEntryMappingPanel(this);
			ignoredMapping.collapse();
			editorPanel.add(ignoredMapping.getContainer(),globalGridConstraint);

			globalGridConstraint.gridy++;

			otherMapping = new VPOtherEntryMappingPanel(this);
			otherMapping.collapse();
			editorPanel.add(otherMapping.getContainer(),globalGridConstraint);
		}
		else
		{



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

			observableMapping = new VPObservableMappingPanel(this,rootFrame);
			observableMapping.collapse();
			editorPanel.add(observableMapping.getContainer(),globalGridConstraint);

			globalGridConstraint.gridy++;

			ignoredMapping=new VPIgnoredMappingPanel(this);
			ignoredMapping.collapse();
			editorPanel.add(ignoredMapping.getContainer(),globalGridConstraint);

			globalGridConstraint.gridy++;

			otherMapping = new VPOtherMappingPanel(this);
			otherMapping.collapse();
			editorPanel.add(otherMapping.getContainer(),globalGridConstraint);

		}
		//WIP
	}
	@Override
	public void reset(boolean keep_ext) {
		if(classMapping!=null)
			classMapping.reset(keep_ext);
		
		if(observationMapping!=null)
			observationMapping.reset();
		
		if(spaceMapping!=null)
			spaceMapping.reset();
		
		if(energyMapping!=null)
			energyMapping.reset();
		
		if(timeMapping!=null)
			timeMapping.reset();
		
		if(observableMapping!=null)
			observableMapping.reset();
		
		if(ignoredMapping!=null)
			ignoredMapping.reset();
		
		if(otherMapping!=null)
			otherMapping.reset();
		


	}
	
	@Override
	public ArgsParser getArgsParser() {
		ArrayList<String> params = new ArrayList<String>();
		ArrayList<String> temp;

		/*
		 * Category
		 */
		switch (this.category ) {
		case Category.MISC: params.add("-category=misc"); break;
		case Category.IMAGE: params.add("-category=image"); break;
		case Category.SPECTRUM: params.add("-category=spectrum"); break;
		case Category.TABLE: params.add("-category=table");break;
		case Category.FLATFILE: params.add("-category=flatfile"); break;
		default: break;}
		
		/*
		 * ClassMapping and extension
		 */
		temp=classMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		/*
		 * Observation axis
		 */
		temp=observationMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		
		/*
		 * Space Axis
		 */
		temp=spaceMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		/*
		 * Energy Axis
		 */
		temp=energyMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		/*
		 * Time Axis
		 */
		temp=timeMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		/*
		 * Observable Axis
		 */
		
		temp=observableMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		
		
		/*
		 * Ignored keywords
		 */
		
		
		temp=ignoredMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		
		
		
		
		/*
		 * Other Axis
		 */
		
		temp=otherMapping.getAxisParams();
		if(temp!=null)
		{
			for (String s : temp)
			{
				params.add(s);
			}
		}
		
		try {
			ArgsParser retour;
			retour = new ArgsParser((String[])(params.toArray(new String[0])));
			retour.setName(confName);
			return retour;
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
		return null;
	}

	@Override
	public boolean checkParams() {
		String msg = "";
		String temp ="";
		
		/*
		 * Class mapping check
		 */
		temp=classMapping.checkAxisParams();
		classMapping.setOnError(false);
		if(!temp.isEmpty())
		{
			classMapping.setOnError(true);
			msg+=temp;
		}
		
		/*
		 * Observation check
		 */
		
		temp=observationMapping.checkAxisParams();
		observationMapping.setOnError(false);
		if(!temp.isEmpty())
		{
			observationMapping.setOnError(true);
			msg+=temp;
		}
		
		/*
		 * Space check
		 */
		temp=spaceMapping.checkAxisParams();
		spaceMapping.setOnError(false);
		if(!temp.isEmpty())
		{
			spaceMapping.setOnError(true);
			msg+=temp;
		}
		
		/*
		 * Energy check
		 */
		temp=energyMapping.checkAxisParams();
		energyMapping.setOnError(false);
		if(!temp.isEmpty())
		{
			energyMapping.setOnError(true);
			msg+=temp;
		}

		/*
		 * Time check
		 */
		temp=timeMapping.checkAxisParams();
		timeMapping.setOnError(false);
		if(!temp.isEmpty())
		{
			timeMapping.setOnError(true);
			msg+=temp;
		}

		
		if( msg.length() > 0 ) {
			AdminComponent.showInputError(rootFrame, "<HTML><UL>" + msg);
			return false;
		}
		else {
			return true;
		}

	}
	
/*
 * From this point, olds functions from MappingKWPanel (non-Javadoc)
 * @see saadadb.admintool.panels.EditPanel#setActionBar()
 */
	


	

}
