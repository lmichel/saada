package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;

public class ObservationMapping extends AxeMapping {

	ObservationMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"obs_id", "obs_collection", "facility_name", "instrument_name", "target_name"}, entryMode);
		this.priority = PriorityMode.ONLY;
		String[] ss;
		if( (ss = ap.getNameComponents(entryMode)) != null  ){
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
		this.priority = ap.getObsMappingPriority();
		this.completeColumns();
	}

	public static void main(String[] args) throws FatalException {
		ObservationMapping om = new ObservationMapping(new ArgsParser(new String[]{"-name=abc,eee,'zere'"}), false);
		System.out.println(om);
	}
}
