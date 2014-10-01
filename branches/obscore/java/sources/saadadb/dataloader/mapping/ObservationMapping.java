package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.PriorityMode;

public class ObservationMapping extends AxisMapping {

	ObservationMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"obs_id", "calib_level", "obs_publisher_did", "obs_collection", "data_product_type", "facility_name", "instrument_name", "target_name"}, entryMode);
		this.priority = PriorityMode.ONLY;
		String[] ss;
		if( (ss = ap.getNameComponents(entryMode)).length != 0  ){
			this.columnMapping.put("obs_id", new ColumnMapping(null, ss, "obs_id"));
		}
		String s;
		if( (s = ap.getObscollection(entryMode)) != null  ){
			this.columnMapping.put("obs_collection", new ColumnMapping(null, s, "obs_collection"));
		} 
		if( (s = ap.getFacility(entryMode)) != null  ){
			this.columnMapping.put("facility_name", new ColumnMapping(null, s, "facility_name"));
		}
		if( (s = ap.getInstrument(entryMode)) != null  ){
			this.columnMapping.put("instrument_name", new ColumnMapping(null, s, "instrument_name"));
		}
		if( (s = ap.getTarget(entryMode)) != null  ){
			this.columnMapping.put("target_name", new ColumnMapping(null, s, "target_name"));
		}
		if( (s = ap.getPublisherdid()) != null  ){
			this.columnMapping.put("obs_publisher_did", new ColumnMapping(null, s, "obs_publisher_did"));
		}
		if( (s = ap.getProductType()) != null  ){
			this.columnMapping.put("data_product_type", new ColumnMapping(null, s, "data_product_type"));
		}
		int i;
		if( (i = ap.getCalibLevel()) != SaadaConstant.INT  ){
			this.columnMapping.put("calib_level", new ColumnMapping(null, Integer.toString(i), "calib_level"));
		}
		this.priority = ap.getObsMappingPriority();
		this.completeColumns();
	}

	public static void main(String[] args) throws FatalException {
		ObservationMapping om = new ObservationMapping(new ArgsParser(new String[]{"-name=abc,eee,'zere'"}), false);
		System.out.println(om);
	}
}
