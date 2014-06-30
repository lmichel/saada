package saadadb.admintool.VPSandbox;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class VPObservableMappingPanel extends VPAxisPanel {
	private JTextField obs_id_field,obs_collection_field,facility_name_field,instrument_name_field,target_name_field;
	
	public VPObservableMappingPanel(){
		super("Observation Axis");
		JLabel obs_id,obs_collection,facility_name,instrument_name,target_name;
		obs_id= new JLabel("Obs_id");
		obs_id_field = new JTextField();
		obs_collection_field = new JTextField();
		facility_name_field= new JTextField();
		instrument_name_field= new JTextField();
		target_name_field= new JTextField();
		ccs.newRow();
		axisPanel.add(obs_id,ccs);
		ccs.next();
		axisPanel.add(obs_id_field,ccs);
		

		
		// TODO Auto-generated constructor stub
	}


}
