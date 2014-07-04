package saadadb.admintool.VPSandbox.components.mapper;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;

public class VPKWName extends VPKwMapperPanel{

	// Trouver classe textfield correspondant à l'actuel SAADANAME
	//MappingTextfieldPanel
	
//	protected ColumnSetter obs_collectionSetter=null;
//	protected ColumnSetter target_nameSetter=null;
//	protected ColumnSetter facility_nameSetter=null;
//	protected ColumnSetter instrument_nameSetter=null;
	
	private AppendMappingTextField obs_collection;
	private AppendMappingTextField target_name;
	private AppendMappingTextField facility_name;
	private AppendMappingTextField instrument_name;
	
	public VPKWName(VPSTOEPanel mappingPanel,VPAxisPriorityPanel axisPanel) {
		super(axisPanel);
		/*
		 * Attention ! 3 derniers paramètres corrects ?
		 */
		obs_collection = new AppendMappingTextField(mappingPanel,2, false, null);
		obs_collection.setColumns(AdminComponent.STRING_FIELD_NAME);
		target_name =  new AppendMappingTextField(mappingPanel,2, false, null);
		target_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		facility_name = new AppendMappingTextField(mappingPanel,2, false, null);
		facility_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		instrument_name =  new AppendMappingTextField(mappingPanel,2, false, null);
		instrument_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		components = new JComponent[4];
		components[0]=obs_collection;
		components[1]=target_name;
		components[2]=facility_name;
		components[3]=instrument_name;
		
	}
	
	public void setComponents()
	{
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Collection "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(obs_collection,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Target Name "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(target_name,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Facility Name "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(facility_name,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Instrument Name "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(instrument_name,gbc);
	
	}

}
