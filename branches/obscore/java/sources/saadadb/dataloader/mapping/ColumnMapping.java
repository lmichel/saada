package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
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
	private List<AttributeHandler> attributeHandlers =new ArrayList<AttributeHandler>();
	private static final Pattern constPattern = Pattern.compile("^'(.*)'$");
	private static final Pattern numPattern = Pattern.compile("^(?:(" + RegExp.NUMERIC + "))$");
	public final String label; // used for logging
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
	ColumnMapping(MappingMode mappingMode, String unit, String value, String label) throws FatalException{
		this.mappingMode = mappingMode;
		this.label = label;
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
			while(m.find())
			{
				AttributeHandler temp = new AttributeHandler();
				temp.setNameattr(m.group(1).trim());
				temp.setNameorg(m.group(1).trim());
				temp.setUnit(unit);
				this.attributeHandlers.add(temp);
			}
			//When we are in "Attribute mode" we consider we're in an expression composed of only one Attribute
			this.expression=value;
			this.attributeHandlers.add(ah);
		}else if( this.mappingMode != MappingMode.NOMAPPING) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Mapping mode SQL not suported yet");
		}
	}

	/**
	 * @param unit
	 * @param values
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String value, String label) throws FatalException{
		this.label = label;
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
	 * @param unit
	 * @param values
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String[] values, String label ) throws FatalException{
		this.label = label;
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
	 * return true if the value is quoted or if it is a number
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

	/**
	 * @return
	 */
	public AttributeHandler getAttributeHandler() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			return attributeHandlers.get(0);
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
	 * @return
	 */
	public List<AttributeHandler> getHandlers() {
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
		List<AttributeHandler> ahs = this.getHandlers();
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

}
