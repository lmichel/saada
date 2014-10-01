package saadadb.vo.request.query.pql;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.util.Messenger;

public class PQLBandParser extends PQLParamParser {

	protected String bandpassIdentifier;
	
	public PQLBandParser(String value) throws QueryException {
		super(value);
		
	}
	
	@Override
	protected String convert(String value){
		double val = SpectralCoordinate
				.convertSaada("m", Database.getSpect_unit(),Double.valueOf(value));
		
		return String.valueOf(val);
	}
	@Override
	protected String lookForDictionnaryMatch(String value){
		//TODO ADD dictionnaries search
		//Should search for a match in one or more dictionnaries and return a range value "val1/val2"
		//if no match return null
		Messenger.printMsg(Messenger.DEBUG, "lookForDictionnaryMatch(String value) is not implemented and will return null");
		return null;
	}
}
