package saadadb.admintool.VPSandbox.obsolet;

import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import saadadb.admintool.VPSandbox.components.input.VPKWMapperPanel;
import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.MyGBC;
import saadadb.enums.DataMapLevel;
import saadadb.exceptions.FatalException;

public class VPKWName extends VPKWMapperPanel{

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
	
	public VPKWName(VPSTOEPanel mappingPanel,VPAxisPriorityPanel axisPanel) throws FatalException {
		super(mappingPanel);
		/*
		 * Attention ! 3 derniers paramètres corrects ?
		 */
		obs_collection = new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		obs_collection.setColumns(AdminComponent.STRING_FIELD_NAME);
		target_name =  new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		target_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		facility_name = new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		facility_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		instrument_name =  new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		instrument_name.setColumns(AdminComponent.STRING_FIELD_NAME);
//		components = new JComponent[4];
//		components[0]=obs_collection;
//		components[1]=target_name;
//		components[2]=facility_name;
//		components[3]=instrument_name;

		
	}

	@Override
	public boolean setText(TreePath treepath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean valid() {
		// TODO Auto-generated method stub
		return false;
	}
	
//	public void setComponents()
//	{
//		JPanel panel = axisPanel.panel;
//		MyGBC gbc = axisPanel.gbc;
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("Collection "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(obs_collection,gbc);
//		gbc.newRow();
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("Target Name "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(target_name,gbc);
//		gbc.newRow();
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("Facility Name "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(facility_name,gbc);
//		gbc.newRow();
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("Instrument Name "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(instrument_name,gbc);
//
//	}

//	@Override
//	public ArrayList<String> getParams() {
//		ArrayList<String> params = new ArrayList<String>();
//		boolean emptyField=false;
//		if (!axisPanel.getPriority().noBtn.isSelected())
//		{
//			if(obs_collection.getText().length()>0)
//				params.add("-obscollection="+obs_collection.getText());
//			else
//				emptyField=true;
//			if(target_name.getText().length()>0)
//				params.add("-target="+target_name.getText());
//			else
//				emptyField=true;
//			if(facility_name.getText().length()>0)
//				params.add("-facility="+facility_name.getText());
//			else
//				emptyField=true;
//			if(instrument_name.getText().length()>0)
//				params.add("-instrument="+instrument_name.getText());
//			else
//				emptyField=true;
//		}
//		if(axisPanel.getPriority().onlyBtn.isSelected() && emptyField)
//			mappingPanel.showInfo(mappingPanel.rootFrame,"You selected the \"Only\" priority for the Observation Axis but didn't fill each corresponding field");
//			
//		return params;
//	}

}
