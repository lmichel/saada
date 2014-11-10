/**
 * 
 */
package saadadb.products.setter;

import hecds.wcs.transformations.Projection;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * Column Setter specialized in the processing of WCS value
 * WCS projection cannot be applied with expressions: too much complicated, use of test....
 * Thus they are computed by an instance of {@link Projection}
 * Supported expressions are limited to the invocation of projection methods:
 * getMin/getMax/getCenter/GetNaxis(xenum)
 * Expression form is like WCS.method(axenum)
 * Axe number starts with  as WSC specifies
 *   
 * @author michel
 * @version $Id$
 */
public final class ColumnWcsSetter extends ColumnExpressionSetter {
	private Projection projection;
	public static final String Prefix = "WCS.";

	/**
	 * The projection is connected to the product attribute handler.
	 * @param fieldName
	 * @param expression
	 * @param projection
	 * @throws Exception
	 */
	public ColumnWcsSetter(String fieldName, String expression, Projection projection) throws Exception	{
		super(fieldName, expression /* as constantValue */);
		if( expression == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "WCS column setter cannot be set with a null expression");
		}
		if( projection == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "WCS column setter cannot be set with a null axe model");
		}
		this.settingMode = ColumnSetMode.BY_WCS;
		this.result = null;
		this.computingMode = INIT.WSC_AXE;
		this.projection = projection;
		this.setExpression(expression);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#setExpression(java.lang.String)
	 */
	protected void setExpression(String expression) throws Exception{
		this.expression = expression;
		this.result = null;
		if( !this.expression .startsWith(Prefix)){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "The expression of a WCS column setter must start with 'WCS.'");
		}
	}	

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#calculateExpression()
	 */
	public void calculateExpression() throws Exception{
		String ef = this.expression.replace(Prefix,  "");
		this.projection.setValues();
		switch( ef ) {
		case "getMin(1)":
			this.result = String.valueOf(this.projection.getMin(1));
			break;
		case "getMin(2)":
			this.result = String.valueOf(this.projection.getMin(2));
			break;
		case "getMax(1)":
			this.result = String.valueOf(this.projection.getMax(1));
			break;
		case "getMax(2)":
			this.result = String.valueOf(this.projection.getMax(2));
			break;
		case "getCenter(1)":
			this.result = String.valueOf(this.projection.getCenter(1));
			break;
		case "getCenter(2)":
			this.result = String.valueOf(this.projection.getCenter(2));
			break;
		case "getNaxis(1)":
			this.result = String.valueOf(this.projection.getNaxis(1));
			break;
		case "getNaxis(2)":
			this.result = String.valueOf(this.projection.getNaxis(2));
			break;
		case "getUnit(1)":
			this.result = this.projection.descriptor.getWcsAxeDescriptor(1).unit;
			break;
		case "getUnit(2)":
			this.result = this.projection.descriptor.getWcsAxeDescriptor(2).unit;
			break;
		default:
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "WCS expression " + this.expression  + " not undestoodo");	
		}
	}

}
