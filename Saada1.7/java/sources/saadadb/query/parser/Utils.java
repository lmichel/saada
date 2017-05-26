package saadadb.query.parser;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

public class Utils {
	/**
	 * @param str
	 * @throws QueryException
	 */
	public static final void checkParentheseAccolade(String str) throws QueryException {
		if (str.concat(" ").split("\\}").length != str.concat(" ").split("\\{").length)
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Error!  number of \"{\" different from the number of \"}\" !! Check your query to fix it!");
		if (str.concat(" ").split("\\)").length != str.concat(" ").split("\\(").length)
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Error! number of \"(\" different from the number of \")\" !! Check your query to fix it!");
	}

}
