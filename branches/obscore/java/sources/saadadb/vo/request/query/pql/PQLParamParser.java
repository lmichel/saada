package saadadb.vo.request.query.pql;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.util.DateUtils;
import saadadb.util.Messenger;

/**
 * @author hahn Parses Parameters which are formatted by min/max values (Open or
 *         closed range) Creates query part that allow a search specifics fields
 */

public class PQLParamParser {

	public static final String valSeparator = "/";
	public static final String qualSeparator = ";";
	protected MinMaxRange range;

	public class MinMaxRange {

		protected String min;
		protected String max;
		protected String qualifier;

		public MinMaxRange() {
			min = null;
			max = null;
			qualifier = null;
		}

		public String getQualifier() {
			return qualifier;
		}

		public void setQualifier(String qualifier) {
			this.qualifier = qualifier;
		}

		public String getMin() {
			return min;
		}

		public void setMin(String min) {
			this.min = min;
		}

		public String getMax() {
			return max;
		}

		public void setMax(String max) {
			this.max = max;
		}
	}

	public PQLParamParser(String value) throws QueryException {
		range = new MinMaxRange();
		parseMinMaxValues(value);
	}

	/**
	 * Get min, max and qualifier values contained in a String and separated by
	 * hard coded separators
	 * 
	 *
	 * @param value
	 * @param valSeparator
	 * @return
	 * @throws QueryException
	 */
	protected void parseMinMaxValues(String value) throws QueryException {
		/*
		 * 0) Look for separator in the string 1) Find a qualifier 2) If only a
		 * single value is found, call findBandpassFilter to look for a match in
		 * vocabulary 3) if findBandpassfilter return null, assume that it is a
		 * single value and put it in range.min else, ... do something with 4)
		 * Find Numerical range of values (separated by a /)
		 */
		// 0)
		// ================================================================================
		String trimVal = value.trim();
		// Check if separators are presents
		int valSeparatorIndex = trimVal.indexOf(valSeparator);
		int qualSeparatorIndex = trimVal.indexOf(qualSeparator);

		// System.out.println("RawValue: " + trimVal);
		// System.out.println("index of " + valSeparator + " :"
		// + valSeparatorIndex);
		// System.out.println("index of " + qualSeparator + " :"
		// + qualSeparatorIndex);

		// 1) ============================= QUALIFIER
		// =======================================
		// if a qualfifier separator is found, remove it from the string and put
		// it in range.qualifier
		if (qualSeparatorIndex != -1) {
			String qual = trimVal.substring(qualSeparatorIndex);
			trimVal = trimVal.replace(qual, "");
			// Remove the qualifier from the string
			qual = qual.replace(qualSeparator, "");
			if (!qual.isEmpty()) {
				range.setQualifier(qual);
			}
		}

		// System.out.println("TrimVal after qualifier is removed: " + trimVal);

		// 2) ======================== Look for a match
		// =====================================
		// Separator not found
		// Make a dictionnary search to look for a <match>
		// if it returns null, then
		// assume that the current value is the min value
		if (valSeparatorIndex == -1) {
			String match = lookForDictionnaryMatch(trimVal);
			if (match == null) {
				// 3) // assume that the current value is the min value
				range.setMin(trimVal);
			} else {
				// TODO DO something
				// The search will return a string the parser can use to get a
				// numerical range
			}
		}
		// 4) ========================== Numerical Range
		// ====================================
		// there no min value
		if (valSeparatorIndex == 0) {
			// System.out.println("No min value");
			range.setMax(trimVal.substring(1).trim());

			// there is no max value
		} else if (valSeparatorIndex == trimVal.length() - 1) {
			// System.out.println("No max value");
			trimVal = trimVal.replace(valSeparator, "");
			range.setMin(trimVal.trim());

			// Both value are present
		} else if (valSeparatorIndex != -1) {
			// System.out.println("Both values present");
			String min = trimVal.substring(0, valSeparatorIndex)
					.replace(valSeparator, "").trim();
			range.setMin(min);
			String max = trimVal.substring(valSeparatorIndex + 1).trim();
			range.setMax(max);
		}

		// =============================================================================
		// System.out.println("Min: " + range.getMin());
		// System.out.println("Max: " + range.getMax());
		// System.out.println("Qualifier: " + range.getQualifier());

		if (range.min == null && range.max == null) {
			QueryException.throwNewException(QueryException.WRONG_PARAMETER,
					"Wrong Parameters : no values or badly formatted");
		}

	}

	// The value specified may be a single value or an open or closed range. If
	// a single value is
	// specified it matches any dataset for which the time coverage includes the
	// specified value. If
	// a two valued range is given, a dataset matches if any portion of it
	// overlaps the given
	// temporal region. An imprecise value such as yyyy indicates the entire
	// period specified, e.g.,
	// 1990-2000 would match any dataset overlapping the range from the
	// beginning of 1990 to
	// the end of 2000.

	/**
	 * Creates a query part that allows a search by range value (open or closed
	 * range) in the given field
	 * 
	 * @param minField
	 *            the minimum field e.g : "t_min"
	 * @param maxField
	 *            the maximum field e.g : "t_max"
	 * @return
	 * @throws Exception
	 */
	public String getQueryPart(String minField, String maxField)
			throws Exception {
		String min = "";
		String max = "";
		if (range.getMin() != null) {
			min = convert(range.getMin());
		}
		if (range.getMax() != null) {
			max = convert(range.getMax());
		}

		return getSQL(min, max, minField, maxField);
	}

	/**
	 * Creates a query part that allows a search by minimal value in the given
	 * field
	 * 
	 * @param field
	 *            field name e.g: "em_res_power"
	 * @return
	 * @throws Exception
	 */
	public String getQueryPart(String field) throws Exception {
		String min = "";

		if (range.getMin() != null) {
			min = convert(range.getMin());
		}

		return getSQL(min, field);
	}

	public String getExactQueryPart(String field) throws Exception {
		String val = "";
		if (range.getMin() != null) {
			val = convert(range.getMin());
		}
		return getExactSQL(val, field);
	}

	/**
	 * Creates a query part that allows a search by range in a specified Field
	 * 
	 * @param min
	 *            minimum value e.g : "10"
	 * @param max
	 *            maximum value e.g: "15"
	 * @param minField
	 *            minimum field e.g : "t_min"
	 * @param maxField
	 *            maximum field e.g : "t_max"
	 * @return
	 */
	protected String getSQL(String min, String max, String minField,
			String maxField) {
		String retour = "";

		// Both value are set
		// return a query part that checks if the range value [min,max] overlaps
		// with [minField, maxField]
		if (range.min != null && range.max != null) {

			retour = min + " BETWEEN " + minField + " AND " + maxField
					+ " AND " + max + " BETWEEN " + minField + " AND "
					+ maxField + " OR " + min + " <= " + minField + " AND "
					+ max + " >= " + maxField + " OR " + minField + " <= "
					+ max + " AND " + min + " <= " + minField + "" + " OR "
					+ maxField + " <= " + max + " AND " + maxField + " >= "
					+ min;

		} else if (range.min == null && range.max != null) {

			retour = maxField + " <= " + max;

		} else if (range.min != null & range.max == null) {

			retour = minField + " >= " + min;
		}

		return retour;
	}

	/**
	 * Creates a query part that allows a search by minimal value in a specified
	 * field
	 * 
	 * @param min
	 *            minimum value e.g : "10"
	 * @param Field
	 *            field e.g : "t_min"
	 * @return
	 */
	protected String getSQL(String min, String field) {
		String retour = "";
		if (range.min != null) {
			retour = field + " >= " + min;
		}
		return retour;
	}

	/**
	 * Creates a query part that allows a search by an exact value in a
	 * specified field
	 * 
	 * @param value
	 * @param field
	 * @return
	 */
	protected String getExactSQL(String value, String field) {
		String retour = "";
		if (range.min != null) {
			retour = field + " = '" + value + "'";
		}
		return retour;
	}

	/**
	 * Look for a match of @param value in dictionaries
	 * 
	 * @param value
	 *            value to match
	 * @return A range value val1/val2 or null if no match
	 */
	protected String lookForDictionnaryMatch(String value) {

		// TODO Implement a dictionnary search that should return a numerical
		// range val1/val2
		// or null is no match
		return null;
	}

	/**
	 * Return the qualifier or null if not set
	 * 
	 * @return
	 */
	public String getQualifier() {
		return range.getQualifier();
	}

	protected String getMinRange() {
		return range.getMin();
	}

	protected String getMaxRange() {
		return range.getMax();
	}

	/**
	 * convert value to a suitable format
	 * 
	 * @param value
	 * @return value
	 * @throws Exception
	 */
	protected String convert(String value) throws Exception {
		return value;
	}

	// debug purpose
	public static void main(String[] args) throws NumberFormatException,
			Exception {
		/*
		 * //Debug for timeQueryPart String toTest =
		 * "1999-05-30/1999-06-25;toto"; PQLParamParser paramParser = new
		 * PQLParamParser(); paramParser.parseMinMaxValues(toTest);
		 * System.out.println(paramParser.getTimeQueryPart());
		 */
		Database.init(args[args.length - 1]);
		Messenger.debug_mode = true;
		System.out.println("Spectral_unit: " + Database.getSpect_unit());

		String toTest = "1/11";
		PQLParamParser paramParser = new PQLBandParser(toTest);
		String retour = paramParser.getQueryPart("em_min", "em_max");
		System.out.println("RETOUR: " + retour);
		Database.close();
	}
}
