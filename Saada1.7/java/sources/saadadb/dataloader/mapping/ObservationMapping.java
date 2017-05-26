package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;

public class ObservationMapping extends AxeMapping {

	ObservationMapping(ArgsParser ap) throws FatalException {
		super(ap, new String[]{"namesaada"});
		this.priority = Priority.ONLY;
		String[] s;
		if( (s = ap.getNameComponents()) != null  ){
			this.columnMapping.put("namesaada", new ColumnMapping(null, s));
		}
		this.completeColumns();
	}

	public static void main(String[] args) throws FatalException {
		ObservationMapping om = new ObservationMapping(new ArgsParser(new String[]{"-name=abc,eee"}));
		System.out.println(om);
	}
}
