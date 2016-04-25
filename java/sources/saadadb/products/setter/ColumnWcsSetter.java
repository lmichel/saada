/**
 * 
 */
package saadadb.products.setter;

import hecds.wcs.transformations.Projection;

import java.util.ArrayList;
import java.util.List;

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
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Field " + this.fieldName + ": WCS column setter cannot be set with a null expression");
		}
		if( projection == null ){
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Field " + this.fieldName + ": WCS column setter cannot be set with a null axe model");
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
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Field " + this.fieldName + ": The expression of a WCS column setter must start with 'WCS.' for field " + this.fieldName);
		}
	}	

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnExpressionSetter#calculateExpression()
	 */
	public void calculateExpression() throws Exception{
		if( !this.projection.containsValidValues()){
			this.settingMode = ColumnSetMode.NOT_SET;
			this.completeDetectionMsg(this.projection + " has no valid values");				
			this.result = null;
			return;
		}
		String ef = this.expression.replace(Prefix,  "");
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
			this.result = this.projection.getDescriptor().getWcsAxeDescriptor(1).unit;
			break;
		case "getUnit(2)":
			this.result = this.projection.getDescriptor().getWcsAxeDescriptor(2).unit;
			break;
		case "getAstroFrame()":
			this.result = this.projection.getDescriptor().getCoosys();
			break;
		case "getWorldPixelSize()":
			this.result = String.valueOf(this.projection.getWorldPixelSize());
			break;
			/*
			 * The region is stored as a string in the value field and as a List<Double> in the storedValue field
			 * That is not the standard purpose of storedValue but that avoids useless conversion String <> List
			 * There is no way to detect this behavior from the API. Just  remind it.
			 */
		case "getWorldPixelRegion()":
			List<List<Double>> tempo = this.projection.getWorldBoundaries();
			StringBuffer sb = new StringBuffer();
			List<Double> sv = new ArrayList<Double>();
			for( List<Double> cl: tempo){
				for( Double ci: cl){
					sb.append(Double.toString(ci));
					sb.append(" ");
					sv.add(ci);
				}
				this.result = sb.toString();	
			}
			this.storedValue = sv;
			break;
		case "getFieldOfView()":
			double c1 = this.projection.getCenter(1);
			double c2 = this.projection.getCenter(2);
			double d1 = cds.astro.Astrocoo.distance(this.projection.getMin(1) , c2, this.projection.getMax(1), c1);
			double d2 = cds.astro.Astrocoo.distance(c1, this.projection.getMin(2), c1, this.projection.getMax(2));
			this.result =  (d1 > d2)?Double.toString(d2): Double.toString(d1);
			this.unit = "deg";
			break;
		case "getStokes()":
			int st = (int) this.projection.getCenter(1);
			switch (st) {
			case 1 : this.result = "I"; break;// Standard Stokes unpolarized
			case 2 : this.result = "Q"; break;//  Standard Stokes linear
			case 3 : this.result = "U"; break;//  Standard Stokes linear
			case 4 : this.result = "V"; break;//  Standard Stokes circular
			case -1: this.result = "RR"; break;//  Right-right circular
			case -2: this.result = "LL"; break;//  Left-left circular
			case -3: this.result = "RL"; break;//  Right-left cross-circular
			case -4: this.result = "LR"; break;//  Left-right cross-circular
			case -5: this.result = "XX"; break;//  X parallel linear
			case -6: this.result = "YY"; break;//  Y parallel linear
			case -7: this.result = "XY"; break;//  XY cross linear
			case -8: this.result = "YX"; break;//  YX cross linear
			default: this.result = "I"; break;// Standard Stokes unpolarized
			}
			this.completeDetectionMsg("Taken from WCS pixel_value=" + st);
			break;
		default: 
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Field " + this.fieldName + ": WCS expression " + this.expression  + " not undestood");	
		}     
		if( this.result == null ){
			this.settingMode = ColumnSetMode.NOT_SET;
			this.completeDetectionMsg("Expression [" + this.expression + "] return null");				
		}
	}

}
