package saadadb.vo.request.formator.process;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.exceptions.SaadaException;

/**
 * This class extracts a query and parameters from an expression.
 * This expression must have the following form :
 * 
 * 			query;param1=something,param2=anotherthing
 * 
 * The query part must not contains semicolons
 * Parameters must have the following form :
 * 
 * 			'qualifier'='value'
 * 
 * 	There can be as much parameters as needed.
 * @author hahn
 *
 */
public class Extractor {

	private String expression;
	private String query;
	private boolean  arithmetic;
	
	private LinkedHashMap<String, String> params;
	public Extractor() {
		init();
	}
	/**
	 * Initializes the attributes
	 */
	private void init() {
		expression ="";
		query = "";
		arithmetic = true;
		params = new LinkedHashMap<String, String>();
	}
	/**
	 * Extract the expression and the parameters
	 * @throws Exception 
	 */
	public void extract() throws Exception {
		String untreatedExp = expression;
		int semiIndex;
		semiIndex = untreatedExp.indexOf(";");
		extractExpression(untreatedExp, semiIndex);
		if (semiIndex != -1) {
			// this is the query for the expressionColumnsetter
			query = untreatedExp.substring(0, semiIndex);
			untreatedExp = untreatedExp.replace(query + ";", "");
			extractParameters(untreatedExp);
			computeParams();
		}
	}
	
	/**
	 * Extract the expression
	 * 
	 * @param exp
	 *            The expression
	 * @param semiIndex
	 *            The index of the semicolumn if any or -1 if none
	 */
	private void extractExpression(String exp, int semiIndex) {
		String tmp;
		if (semiIndex != -1) {
			tmp = exp.substring(0, semiIndex);
		} else {
			tmp = exp;
		}
		tmp = tmp.trim();
		if (tmp != null && !tmp.isEmpty()) {
			query = tmp;
		} else {
			query = "";
		}
		//System.out.println("Query : "+query);
	}
	
	/**
	 * Extracts parameters from the expression
	 * 
	 * @param exp
	 * @throws Exception 
	 */
	private void extractParameters(String exp) throws Exception {
		if (!exp.isEmpty()) {
			String expTmp = exp;
			int coma;
			String tmp;
			String[] ps;

			do {
				// System.out.println("processing : "+expTmp);
				coma = expTmp.indexOf(",");
				if (coma != -1) {
					tmp = expTmp.substring(0, coma);
				} else {
					tmp = expTmp;
				}
				ps = tmp.split("=");
				if (ps.length == 2 && !ps[0].trim().isEmpty() && !ps[1].trim().isEmpty()) {
					params.put(ps[0], ps[1]);
				} else {
					SaadaException.throwNewException(SaadaException.WRONG_PARAMETER, "Parameter '" + ps[0]
							+ "' and its value must be separated by '='");
				}
				expTmp = expTmp.replace(tmp, "");
				if (expTmp.startsWith(",")) {
					expTmp = expTmp.replace(",", "");
				}

			} while (coma != -1);
		}
	}
	
	/**
	 * Reads the params map and set the params flags accordingly
	 * @throws Exception 
	 */
	private void computeParams() throws Exception {
		String val;
		for (Map.Entry<String, String> e : params.entrySet()) {
/*
 * They can be many parameters, just add cases in the switch/case to handle them
 */
			switch (e.getKey().toLowerCase().trim()) {

			case "arithmetic":
				val = e.getValue().trim();
				if (val.equalsIgnoreCase("true")) {
					this.arithmetic = true;
				} else if (val.equalsIgnoreCase("false")) {
					this.arithmetic = false;
				}
				//System.out.println("param arithmetic found, value = " + arithmetic);
				break;
			case "":
				break;
			default:
				SaadaException.throwNewException(SaadaException.WRONG_PARAMETER, "Parameter '" + e.getKey() + "' Not recognized");
				break;
			}
		}
	}
	/**
	 * Get the Query
	 * @return
	 */
	public String getQuery() {
		return query;
	}
	/**
	 * get the arithmetic value or true if none has been parsed
	 * @return
	 */
	public boolean getArithmetic() {
		return arithmetic;
	}
	/**
	 * Get the raw expression ('Query';'parameter1=...'
	 * @return
	 */
	public String getExpression() {
		return expression;
	}
	
	/**
	 * 	Sets the raw expression ad extracts the query and parameters
	 */
	public void setExpression(String expression) throws Exception {
		this.expression = expression;
		extract();
	}
	/**
	 * Return a map of all the parsed parameters
	 * @return
	 */
	public LinkedHashMap<String, String> getAllParams(){
		return params;
	}
	/**
	 * return the number of parameters that have been parsed
	 * @return
	 */
	public int getParamSize() {
		return params.size();
	}
	public String toString() {
		return "Extactor: expression: " + expression
		+ "query: " + query
		+ "arithmetic: " + arithmetic;
	}


}
