/**
 * 
 */
package saadadb.products.setter;

import java.util.List;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.products.DataFile;
import saadadb.util.RegExpMatcher;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * Do basic computation on the value of a given column
 * There is not attribute handler but a data file to be given each time the expression has to be computed
 * 
 * Supported expressions are :
 *   getMinValue(colName)
 *   getMaxValue(colName)
 *   getNbRows(colName)
 * 
 * @author michel
 * @version $Id$
 */
public final class ColumnRowSetter extends ColumnExpressionSetter {
	public static final String Prefix = "Column";
	private String colName, funcName;
	
	/**
	 * @param fieldName name of the field set by the object
	 * @param expression expression to be applied
	 * @throws Exception
	 */
	public ColumnRowSetter(String fieldName, String expression) throws Exception	{
		super(fieldName, expression /* as constantValue */);
		if( expression == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Row column setter cannot be set with a null expression");
		}
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.result = null;
		this.computingMode = INIT.SINGLE_ATTRIBUTE;
		this.setExpression(expression);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#setExpression(java.lang.String)
	 */
	protected void setExpression(String expression) throws Exception{
		this.expression = expression;
		this.result = null;
		RegExpMatcher rem = new RegExpMatcher(Prefix + "\\.([a-zA-Z]*)\\(([^()]*)\\)", 2);
		List<String> lg = rem.getMatches(this.expression);
		if( lg !=  null){
			this.funcName = lg.get(0);
			this.colName = lg.get(1);		
		} else {
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Expression " + this.expression + " not understood for a row column setter");
		}	
	}

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#calculateExpression()
	 */
	public void calculateExpression(DataFile dataFile) throws Exception{
		if( dataFile != null ) {
			
			Object[] extrema = dataFile.getExtrema(this.colName);
			if( extrema == null ){
				IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot get extrema of column " + this.colName + " for product " + dataFile);				
			}
			switch( this.funcName ) {
			case "getMinValue": this.result = extrema[0].toString();break;
			case "getMaxValue": this.result = extrema[1].toString();break;
			case "getNbPoints": this.result = extrema[2].toString();break;
			default: IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER,  this.funcName + " not understood for a row column setter");
			}
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#calculateExpression()
	 */
	public void calculateExpression() throws Exception{
		IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "ColumnRowSetter requires a datafile to compute an expression: usescalculateExpression(dataFile)" );				
	}

}
