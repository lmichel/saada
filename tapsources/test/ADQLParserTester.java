package test;

import saadadb.vo.tap.SaadaSQLTranslator;
import adql.parser.ADQLParser;
import adql.parser.ParseException;
import adql.query.ADQLQuery;
import adql.translator.ADQLTranslator;
import adql.translator.TranslationException;

public class ADQLParserTester {
	/*
	 * SELECT * FROM tap_schema.columns
	 */

	public ADQLParserTester(String query) throws ParseException, TranslationException {

		ADQLParser parser = new ADQLParser();
		parser.setDebug(false);
		ADQLQuery adqlQuery = parser.parseQuery(query);
		ADQLTranslator translator = new SaadaSQLTranslator();
		System.out.println(translator.translate(adqlQuery));
	}

	public static void main(String[] args) throws ParseException, TranslationException {
		String query = "SELECT * FROM data WHERE CONTAINS(POINT('ICRS', ivoa.ra, tap_schema.dec), POLYGON('ICRS GEOCENTER', 10.0, -10.5, 20.0, 20.5, 30.0, 30.5)) = 1";
				//"SELECT * FROM data WHERE DISTANCE(POINT('ICRS GEOCENTER', 25.0, -19.5), POINT('ICRS GEOCENTER', 25.4, -20.0)) = 1";
				//"SELECT * FROM data WHERE CONTAINS(POINT('ICRS', ivoa.ra, tap_schema.dec), CIRCLE('ICRS', 13, 2, 10)) = 1";
		new ADQLParserTester(query);

	}
}
