package saadadb.vo.request.query.pql;

import saadadb.exceptions.QueryException;
import saadadb.util.DateUtils;

public class PQLTimeParser extends PQLParamParser{

	public PQLTimeParser(String value) throws QueryException {
		super(value);
		
	}
	@Override
	protected String convert(String value) throws Exception{
		return DateUtils.getMJD(value);
	}
}
