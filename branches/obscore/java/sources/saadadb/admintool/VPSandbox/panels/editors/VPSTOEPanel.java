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
import saadadb.admintool.VPSandbox.components.mapper.VPObservableMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPObservationMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPSpaceMappingPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPTimeMappingPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
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
	private VPClassMappingPanel classMapping;
	private VPObservationMappingPanel observationMapping;
	private VPSpaceMappingPanel spaceMapping;
	private VPTimeMappingPanel timeMapping;
	private VPObservableMappingPanel observableMapping;
	private VPEnergyMappingPanel energyMapping;
	
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
		// this.category = category;
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
		// temporaire)
		boolean forEntry = false;

		//Build the Class-Mapping axis (include the extension name choice)
		
		classMapping = new VPClassMappingPanel(this);
		classMapping.expand();
		editorPanel.add(classMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		//Build the observable Axis
		//VPObservableMappingPanel ObservableAxis = new VPObservableMappingPanel();
		//ObservableAxis.collapse();
		//editorPanel.add(ObservableAxis.container, globalGridConstraint);

		
		observationMapping = new VPObservationMappingPanel(this,forEntry);
		observationMapping.expand();
		editorPanel.add(observationMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		spaceMapping = new VPSpaceMappingPanel(this,forEntry);
		spaceMapping.collapse();
		editorPanel.add(spaceMapping.getContainer(),globalGridConstraint);
		
		
		globalGridConstraint.gridy++;
		
		energyMapping = new VPEnergyMappingPanel(this,forEntry);
		energyMapping.collapse();
		editorPanel.add(energyMapping.getContainer(),globalGridConstraint);
		

		globalGridConstraint.gridy++;
		
		timeMapping = new VPTimeMappingPanel(this,forEntry);
		timeMapping.collapse();
		editorPanel.add(timeMapping.getContainer(),globalGridConstraint);
		
		globalGridConstraint.gridy++;
		
		observableMapping = new VPObservableMappingPanel(this,forEntry);
		observableMapping.collapse();
		editorPanel.add(observableMapping.getContainer(),globalGridConstraint);

		
		//WIP
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
		 * Observable Panel
		 */
		
		temp=observableMapping.getAxisParams();
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
		temp+=classMapping.checkAxisParams();
		classMapping.setOnError(false);
		if(!temp.isEmpty())
		{
			classMapping.setOnError(true);
			msg+=temp;
		}
	
		
	
//		if( cooSysMapper != null ) {
//			cooSysMapper.setOnError(false);
//			if( !cooSysMapper.isNo() && cooSysMapper.getText().length() == 0 ){
//				cooSysMapper.setOnError(true);
//				msg += "<LI>Empty coord. system  not allowed in this mapping mode</LI>";
//			}
//			if( !cooSysMapper.valid()  ){
//				cooSysMapper.setOnError(true);
//				msg += "<LI>Coord System badly formed</LI>";				
//			}
//		}
//		if( positionMapper != null ) {
//			positionMapper.setOnError(false);
//			if( !positionMapper.isNo() && positionMapper.getText().length() == 0 ){
//				cooSysMapper.setOnError(true);
//				msg += "<LI>Empty coordinates not allowed in this mapping mode</LI>";
//			}
//			if( !positionMapper.valid()  ){
//				positionMapper.setOnError(true);
//				msg += "<LI>Coord mapping badly formed</LI>";				
//			}
//		}
//		if( e_positionMapper != null ) {
//			e_positionMapper.setOnError(false);
//			if( !e_positionMapper.isNo() && e_positionMapper.getText().length() == 0 ){
//				e_positionMapper.setOnError(true);
//				msg += "<LI>Empty coordinates not allowed in this mapping mode</LI>";
//			}
//			if( !e_positionMapper.valid()  ){
//				e_positionMapper.setOnError(true);
//				msg += "<LI>Coord mapping badly formed</LI>";				
//			}
//		}
//		if( positionErrorMapper != null ) {
//			positionErrorMapper.setOnError(false);
//			if( !positionErrorMapper.isNo() && positionErrorMapper.getText().length() == 0 ){
//				positionErrorMapper.setOnError(true);
//				msg += "<LI>Empty coordinates error not allowed in this mapping mode</LI>";
//			}
//			if( !positionErrorMapper.valid()  ){
//				positionErrorMapper.setOnError(true);
//				msg += "<LI>Coord error mapping badly formed</LI>";				
//			}
//		}
//		if( e_positionErrorMapper != null ) {
//			e_positionErrorMapper.setOnError(false);
//			if( !e_positionErrorMapper.isNo() && e_positionErrorMapper.getText().length() == 0 ){
//				e_positionErrorMapper.setOnError(true);
//				msg += "<LI>Empty coordinate error not allowed in this mapping mode</LI>";
//			}
//			if( !e_positionErrorMapper.valid()  ){
//				e_positionErrorMapper.setOnError(true);
//				msg += "<LI>Coord error mapping badly formed</LI>";				
//			}
//		}
//		
//		if( dispersionMapper != null ) {
//			dispersionMapper.setOnError(false);
//			if( !dispersionMapper.isNo() &&  dispersionMapper.getText().length() == 0 ) {
//				dispersionMapper.setOnError(true);
//				msg += "<LI>Empty spectral dispersion not allowed in this mapping mode</LI>";
//			}
//			if( !dispersionMapper.valid()  ){
//				dispersionMapper.setOnError(true);
//				msg += "<LI>Spectral dispersion badly formed</LI>";				
//			}
//		}
		
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
