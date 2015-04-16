package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.RegExpMatcher;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.MappingMode;

/**
 * Expression quoted : constant value, expression to be computed otherwise
 * @author michel
 * @version $Id$
 *
 */
public class ColumnMapping {
	public static final String NUMERIC = "Numeric";
	public static final String UNDEFINED = "Undefined";
	private MappingMode mappingMode = MappingMode.NOMAPPING;
	private Set<AttributeHandler> attributeHandlers =new TreeSet<AttributeHandler>();
	private static final Pattern constPattern = Pattern.compile(RegExp.QUOTED_EXPRESSION);
	private static final Pattern numPattern = Pattern.compile(RegExp.NUMERIC_PARAM);
	private static final Pattern numUnitPattern = Pattern.compile(RegExp.NUMERIC_UNIT_PARAM);
	public String message; // used for logging
	/**
	 * when mode=keyword or expression
	 */
	private String expression;

	/**
	 * @param mappingMode
	 * @param unit
	 * @param value
	 * @throws FatalException
	 */
	ColumnMapping(MappingMode mappingMode, String unit, String value, String message) throws FatalException{
		this.mappingMode = mappingMode;
		this.message = message;
		AttributeHandler ah = new AttributeHandler();
		if( this.mappingMode == MappingMode.VALUE) {
			ah.setNameattr(ColumnMapping.NUMERIC);
			ah.setNameorg(ColumnMapping.NUMERIC);
			ah.setUnit(unit);
			ah.setValue(value);
			this.attributeHandlers.add(ah);
		} else if(this.mappingMode==MappingMode.EXPRESSION || mappingMode==MappingMode.KEYWORD){
			//We have an expression with ONE keyword.
			Pattern keywordsPattern = Pattern.compile(RegExp.KEYWORD);
			Matcher m=keywordsPattern.matcher(value);
			//We search for keywords in the expression, each keyword become an attribute
			int cpt=1;
			System.out.println(RegExp.KEYWORD);
			while(m.find())
			{
				AttributeHandler temp = new AttributeHandler();
				temp.setNameattr(m.group(1).trim());
				temp.setNameorg(m.group(1).trim());
				temp.setUnit(unit);
				this.attributeHandlers.add(temp);
				cpt++;
			}
			//When we are in "Attribute mode" we consider we're in an expression composed of only one Attribute
			this.expression=value;
		}else if( this.mappingMode != MappingMode.NOMAPPING) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Mapping mode SQL not suported yet");
		}
	}

	/**
	 * @param unit
	 * @param values
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String value, String message) throws FatalException{
		this.message = message;
		AttributeHandler ah = new AttributeHandler();
		ah.setUnit(unit);	
		String v;
		if( (v = this.isConstant(value)) != null ) {
			ah.setNameattr(ColumnMapping.NUMERIC);
			ah.setNameorg(ColumnMapping.NUMERIC);
			ah.setValue(v);	
			this.expression=v;
			this.mappingMode = MappingMode.VALUE;
			this.attributeHandlers.add(ah);
		} else if( isSingleKeyword(value) ){
			this.mappingMode = MappingMode.KEYWORD;
			AttributeHandler temp = new AttributeHandler();
			temp.setNameattr(value);
			temp.setNameorg(value);
			temp.setUnit(unit);
			this.attributeHandlers.add(temp);
			this.expression=value;
		} else {
			//Expression replace Attribute
			this.mappingMode = MappingMode.EXPRESSION;
			Pattern keywordsPattern = Pattern.compile(RegExp.KEYWORD);
			Matcher m=keywordsPattern.matcher(value);
			//We search for keywords in the expression, each keyword become an attribute
			while(m.find())
			{
				AttributeHandler temp = new AttributeHandler();
				temp.setNameattr(m.group(1).trim());
				temp.setNameorg(m.group(1).trim());
				temp.setUnit(unit);
				this.attributeHandlers.add(temp);
			}
			this.expression=value;
		}
	}

	/**
	 * @param value
	 * @param message
	 * @throws FatalException
	 */
	ColumnMapping(String value, String message) throws FatalException{
		this.message = message;
		AttributeHandler ah = new AttributeHandler();
		String v;
		if( (v = this.isConstant(value)) != null ) {
			ah.setNameattr(ColumnMapping.NUMERIC);
			ah.setNameorg(ColumnMapping.NUMERIC);
			ah.setValue(v);	
			this.expression=v;
			this.mappingMode = MappingMode.VALUE;
			this.attributeHandlers.add(ah);
			this.extractUnit();

		} else if( isSingleKeyword(value) ){
			this.mappingMode = MappingMode.KEYWORD;
			AttributeHandler temp = new AttributeHandler();
			temp.setNameattr(value);
			temp.setNameorg(value);
			this.attributeHandlers.add(temp);
			this.expression=value;
		} else {
			//Expression replace Attribute
			this.mappingMode = MappingMode.EXPRESSION;
			Pattern keywordsPattern = Pattern.compile(RegExp.KEYWORD);
			Matcher m=keywordsPattern.matcher(value);
			//We search for keywords in the expression, each keyword become an attribute
			while(m.find())
			{
				AttributeHandler temp = new AttributeHandler();
				temp.setNameattr(m.group(1).trim());
				temp.setNameorg(m.group(1).trim());
				this.attributeHandlers.add(temp);
			}
			this.expression=value;
		}
	}

	/**
	 * @param unit
	 * @param values
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String[] values, String message ) throws FatalException{
		this.message = message;
		this.mappingMode = (values == null)? MappingMode.NOMAPPING:  MappingMode.VALUE;
		for( String s: values ) {
			AttributeHandler ah = new AttributeHandler();
			ah.setUnit(unit);	
			String v;
			if( (v = this.isConstant(s)) != null ) {
				ah.setNameattr(ColumnMapping.NUMERIC);
				ah.setNameorg(ColumnMapping.NUMERIC);
				ah.setAsConstant();
				ah.setValue(v);					
			} else if( isSingleKeyword(s) ){
				this.mappingMode = MappingMode.KEYWORD;
				AttributeHandler temp = new AttributeHandler();
				temp.setNameattr(s);
				temp.setNameorg(s);
				temp.setUnit(unit);
				this.attributeHandlers.add(temp);
				this.expression=s;
			} else {
				if( s.matches(".*['\"]+.*" )) {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong parameter " + s);					
				}
				this.mappingMode = MappingMode.KEYWORD;
				ah.setNameattr(s);
				ah.setNameorg(s);
			}
			this.attributeHandlers.add(ah);
		}
	}


	/**
	 * Check if val is a single keyword: it must strictly  match the {@link RegExp#KEYWORD}  reg exp or
	 * it can be similar to AAA-BBB if its length equals 8 (FITS keyword)
	 * @param val
	 * @return true if it is a singme keyword
	 */
	private boolean isSingleKeyword(String val){
		return ( val.matches("^" + RegExp.KEYWORD + "$") || (val.length() == 8 && val.matches("^[A-Za-z]+-[A-Za-z]+$")));
	}


	/**
	 * return true if the value is quoted or if it is a number or if it s number followed 
	 * with a unit string (units are not checked)
	 * @param val
	 * @return
	 */
	private String isConstant(String val){
		Matcher m = constPattern.matcher(val.trim());
		if( m.find() && m.groupCount() == 1 ) {
			return m.group(1).trim();	
		} 
		m = numPattern.matcher(val.trim());
		if( m.find() && m.groupCount() == 1 ) {
			return m.group(1).trim();	
		} 
		m = numUnitPattern.matcher(val.trim());
		if( m.find() && m.groupCount() == 2 ) {
			return val.trim();	
		} 
		return null;
	}
	public boolean byValue() {
		return(mappingMode == MappingMode.VALUE );
	}
	public boolean byKeyword() {
		return(mappingMode == MappingMode.KEYWORD );
	}
	public boolean bySql() {
		return(mappingMode == MappingMode.SQL);
	}
	public boolean notMapped() {
		return(mappingMode == MappingMode.NOMAPPING);
	}

	public boolean byExpression() {
		return(mappingMode == MappingMode.EXPRESSION);
	}

	public MappingMode getMode() {
		return mappingMode;
	}
	/**
	 * @return
	 */
	public AttributeHandler getAttributeHandler() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			for( AttributeHandler ah: attributeHandlers)
				return ah;
			return null;
		}
	}
	/**
	 * This method avoid the caller to check first that te AH is not null before to get its value
	 * @return
	 */
	public String getValue() {
		AttributeHandler ah = this.getAttributeHandler();
		return ((ah == null)? "": (ah.getValue() == null)? "": ah.getValue());
	}

	/**
	 * This method avoid the caller to check first that te AH is not null before to get its unit
	 * @return
	 */
	public String getUnit() {
		AttributeHandler ah = this.getAttributeHandler();
		return ((ah == null)? "": (ah.getUnit() == null)? "": ah.getUnit());
	}

	/**
	 * @return
	 */
	public Set<AttributeHandler> getHandlers() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			return attributeHandlers;
		}
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getValues() {
		Set<AttributeHandler> ahs = this.getHandlers();
		if( ahs == null ){
			return null;
		} else {
			List<String> retour = new ArrayList<String>();
			for( AttributeHandler ah: ahs ){
				retour.add( ((ah == null)? "": (ah.getValue() == null)? "": ah.getValue()));
			}
			return retour;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour = "Mode " + this.mappingMode + "\n";
		for( AttributeHandler ah: this.attributeHandlers ) {
			retour += "   ah: " + ah + "\n";
		}
		if(this.expression==null || this.expression.isEmpty())
			retour += "  No expression associate\n";
		else
			retour+=" Expr: "+this.expression+"\n";
		return retour;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Only works in VALUE mode. Checks if then value look like numericUnit.
	 * If yes, the Unit is set as unit for the attributeHandler and numeric as value
	 */
	private void extractUnit() {
		if( this.mappingMode == MappingMode.VALUE) {
			for( AttributeHandler ah : this.attributeHandlers ) {
				RegExpMatcher rm = new RegExpMatcher(RegExp.NUMERIC + "(.*)", 1);
				List<String> ms = rm.getMatches(ah.getValue());
				if( ms != null ){
					String unit = ms.get(0);
					String v = ah.getValue().replace(unit, "");
					ah.setUnit(unit);
					ah.setValue(v);
					this.message += "unit " + unit + " extracted from param " + ah.getNameorg();
				}
			}
		}
	}
	
	public static void main(String[] args) throws FatalException {
		ColumnMapping cm =new ColumnMapping("'13arcsec'", "");
		System.out.println(cm);
		cm =new ColumnMapping("13arcsec", "");
		System.out.println(cm);
		cm =new ColumnMapping("13", "");
		System.out.println(cm);
	}
}
