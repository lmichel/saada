/**
 * 
 */
package saadadb.products.setter;

import java.util.List;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.products.datafile.DataFile;
import saadadb.util.RegExpMatcher;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * Do computation on the value of a given column
 * There is not attribute handler but a data file to be given each time the expression has to be computed
 * The column expression is computed and replaced with its value within the expression.
 * The expression is then computed by a local instance of a {@link ColumnExpressionSetter}
 * 
 * Supported column expressions are :
 *   Column.getMinValue(colName)
 *   Column.getMaxValue(colName)
 *   Column.getNbRows(colName)
 * 
 * @author michel
 * @version $Id$
 */
public final class ColumnRowSetter extends ColumnExpressionSetter {
	/**
	 * Allow to identify the column expression
	 */
	public static final String Prefix = "Column";
	/**
	 * Name of the searched column
	 */
	private String colName;
	/**
	 * Column function name (without prefix)
	 */
	private String funcName;
	/**
	 * Global column expression
	 */
	private String colExpr;
	
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
		RegExpMatcher rem = new RegExpMatcher(".*(" + Prefix + "\\.([a-zA-Z]*)\\(([^()]*)\\)).*", 3);
		List<String> lg = rem.getMatches(this.expression);
		if( lg !=  null){
			this.colExpr = lg.get(0);
			this.funcName = lg.get(1);
			this.colName = lg.get(2);		
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
			this.calculateExpression(extrema);
		}
	}
	
	/**
	 * calculateExpression split in 2 method for debug helping
	 * @param extrema
	 * @throws Exception
	 */
	private void calculateExpression(Object[] extrema)  throws Exception{
		String colValue = null;
		switch( this.funcName ) {
		case "getMinValue": colValue = extrema[0].toString();break;
		case "getMaxValue": colValue = extrema[1].toString();break;
		case "getNbPoints": colValue = extrema[2].toString();break;
		default: IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER,  this.funcName + " not understood for a row column setter");
		}
		/*
		 * Replace the column expression with its value and compute the new expression
		 */
		ColumnExpressionSetter ces = new ColumnExpressionSetter("foo");
		ces.setExpression(this.expression.replace(colExpr, "'" + colValue + "'"));
		ces.calculateExpression();
		this.result = ces.getValue();
		if( this.result == null ){
			this.settingMode = ColumnSetMode.NOT_SET;
			this.completeConversionMsg("Expression [" + this.expression + "] return null");				
		}
		
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#calculateExpression()
	 */
	public void calculateExpression() throws Exception{
		IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "ColumnRowSetter requires a datafile to compute an expression: usescalculateExpression(dataFile)" );				
	}

	/**
	 * Unit test 'a la' Saada
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		ColumnRowSetter crs = new ColumnRowSetter("aaa" , "MJD('2014-11-17') + Column.getMinValue(xxx)");
		System.out.println(crs.colExpr + " " + crs.funcName + " " + crs.colName);
		crs.calculateExpression(new Object[]{1000., 2000.0});
		System.out.println(crs);
		
		crs = new ColumnRowSetter("aaa" , "MJD('2014-11-17') + MJD(Column.getMinValue(xxx))");
		System.out.println(crs.colExpr + " " + crs.funcName + " " + crs.colName);
		crs.calculateExpression(new Object[]{"2014-11-17", "2014-11-17"});
		System.out.println(crs);
		
		crs = new ColumnRowSetter("aaa" , "MJD('2014-11-17') + Column.getMinValue(xxx)");
		System.out.println(crs.colExpr + " " + crs.funcName + " " + crs.colName);
		crs.calculateExpression(new Object[]{2.78568203109231E8, 2.78568203109231E8});
		System.out.println(crs);	
	}
}
