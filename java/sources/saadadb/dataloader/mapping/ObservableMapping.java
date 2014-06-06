package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.enums.PriorityMode;
import saadadb.exceptions.FatalException;

public class ObservableMapping extends AxisMapping {

	ObservableMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"o_ucd", "o_unit", "o_calib_status"}, entryMode);
		this.priority = PriorityMode.ONLY;
		String s;
		if( (s = ap.getOucd(entryMode)) != null  ){
			this.columnMapping.put("o_ucd", new ColumnMapping(null, s, "o_ucd"));
		} 
		if( (s = ap.getOunit(entryMode)) != null  ){
			this.columnMapping.put("o_unit", new ColumnMapping(null, s, "o_unit"));
		}
		if( (s = ap.getOcalibstatus(entryMode)) != null  ){
			this.columnMapping.put("o_calib_status", new ColumnMapping(null, s, "o_calib_status"));
		}
		this.priority = ap.getObservableMappingPriority();
		this.completeColumns();
	}

	public static void main(String[] args) throws FatalException {
		ObservableMapping om = new ObservableMapping(new ArgsParser(new String[]{"-name=abc,eee,'zere'"}), false);
		System.out.println(om);
	}
}
