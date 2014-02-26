package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;

public class ObservationMapping extends AxeMapping {

	ObservationMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"namesaada"}, entryMode);
		this.priority = PriorityMode.ONLY;
		String[] s;
		if( (s = ap.getNameComponents(entryMode)) != null  ){
			this.columnMapping.put("namesaada", new ColumnMapping(null, s));
		}
		this.completeColumns();
	}

	public static void main(String[] args) throws FatalException {
		ObservationMapping om = new ObservationMapping(new ArgsParser(new String[]{"-name=abc,eee,'zere'"}), false);
		System.out.println(om);
	}
}
