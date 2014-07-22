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
	
	/*
	 * The list of axis (subpanel) of the form 
	 */
	private VPClassMappingPanel classMapping;
	private VPObservationMappingPanel observationMapping;
	private VPSpaceMappingPanel spaceMapping;
	private VPTimeMappingPanel timeMapping;
	private VPObservableMappingPanel observableMapping;
	private VPEnergyMappingPanel energyMapping;
	private VPOtherMappingPanel otherMapping;
	private VPIgnoredMappingPanel ignoredMapping;

	private static final long serialVersionUID = 1L;

	public VPSTOEPanel(AdminTool rootFrame, String title, int category,
			String ancestor) {
		super(rootFrame, title, category, ancestor);
	}

	/**
	 * Call when the user select "Create Filter", it builds the filter panel
	 */
	@Override
	protected void setActivePanel() {
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
		editorPanel.setBackground(LIGHTBACKGROUND);
		editorPanel.setLayout(new GridBagLayout());
		this.addCategoryPanel();
		globalGridConstraint.gridy++;

		//we build the subpanels/Axis;
		buildAxis();

		tPanel.add(new JScrollPane(editorPanel),localGridConstraint);

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

		//Temporary
		if(this.title.equals(NEW_MAPPER))
			category=Category.SPECTRUM;

		classMapping = new VPClassMappingPanel(this);
		classMapping.expand();
		editorPanel.add(classMapping.getContainer(),globalGridConstraint);

		globalGridConstraint.gridy++;

		//if we're in the case of a Table
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
		//Every other category
		else
		{

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

	}
	/**
	 * This function simply reset the form (every panel/axis)
	 */
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

	/**
	 * This method instantiate an object which contains every argument from the form
	 */
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
		 * Extended Keywords
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

	/**
	 * This method check every parameter entered by the user in the form. In case of error 
	 * the corresponding panel is selected and a warning message is displayed
	 */
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
}
