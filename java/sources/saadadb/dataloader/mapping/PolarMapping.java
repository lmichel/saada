package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;

public class PolarMapping extends AxisMapping {

	PolarMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"pol_states"}, entryMode);
		this.priority = ap.getPolarMappingPriority();
		String s;
		if( (s = ap.getPolarStates(entryMode)) != null  ){
			this.columnMapping.put("pol_states", new ColumnMapping(null, s, "pol_states"));
		} 
		this.completeColumns();
	}
}
