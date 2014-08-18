package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.enums.MappingMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.RegExp;

/**
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
	 * when mode=attribute or expression
	 */
	private String expression;

	//private static final Pattern constPattern = Pattern.compile("(?:^(?:(?:'(.*)')|(?:(" + RegExp.NUMERIC + ")))$)");
	//private static final Pattern constPattern = Pattern.compile("(?:^(?:(" + RegExp.NUMERIC + "))$)");
	//private static final Pattern constPattern = Pattern.compile("^([0-9]+)$");

//	/**
//	 * @param mappingMode
//	 * @param unit
//	 * @param value
//	 * @throws FatalException
//	 */
//	ColumnMapping(MappingMode mappingMode, String unit, String value, String label) throws FatalException{
//		this.mappingMode = mappingMode;
//		this.label = label;
//		AttributeHandler ah = new AttributeHandler();
//		if( this.mappingMode == MappingMode.VALUE) {
//			ah.setNameattr(ColumnMapping.NUMERIC);
//			ah.setNameorg(ColumnMapping.NUMERIC);
//			ah.setUnit(unit);
//			ah.setValue(value);
//			this.attributeHandlers.add(ah);
//		} else if( this.mappingMode == MappingMode.ATTRIBUTE) {
//			ah.setNameattr(value);
//			ah.setNameorg(value);
//			ah.setUnit(unit);			
//			this.attributeHandlers.add(ah);
//		} else if( this.mappingMode != MappingMode.NOMAPPING) {
//			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Mapping mode SQL not suported yet");
//		}
//	}
	
	

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
		} else if(this.mappingMode==MappingMode.EXPRESSION){
			//We have an expression with ONE keyword.
			ah.setNameattr(value);
			ah.setNameorg(value);
			ah.setUnit(unit);
			//When we are in "Attribute mode" we consider we're in an expression composed of only one Attribute
			expression=value.toString();
			this.attributeHandlers.add(ah);
		}else if( this.mappingMode != MappingMode.NOMAPPING) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Mapping mode SQL not suported yet");
		}
	}
	
//
//	/**
//	 * @param unit
//	 * @param values
//	 * @throws FatalException
//	 */
//	ColumnMapping(String unit, String value, String label) throws FatalException{
//		this.label = label;
//		AttributeHandler ah = new AttributeHandler();
//		ah.setUnit(unit);			
//		Matcher m = constPattern.matcher(value);
//		if( m.find() && m.groupCount() == 1 ) {
//			ah.setNameattr(ColumnMapping.NUMERIC);
//			ah.setNameorg(ColumnMapping.NUMERIC);
//			ah.setValue(m.group(1).trim());					
//			this.mappingMode = MappingMode.VALUE;
//		} else {
//			if( value.matches(".*['\"]+.*" )) {
//				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong parameter " + value);					
//			}
//			this.mappingMode = MappingMode.ATTRIBUTE;
//			ah.setNameattr(value);
//			ah.setNameorg(value);
//		}
//		this.attributeHandlers.add(ah);
//
//	}
	
	
	/**
	 * @param unit
	 * @param values
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String value, String label) throws FatalException{
		this.label = label;
		AttributeHandler ah = new AttributeHandler();
		ah.setUnit(unit);			
		Matcher m = constPattern.matcher(value);
		if( m.find() && m.groupCount() == 1 ) {
			ah.setNameattr(ColumnMapping.NUMERIC);
			ah.setNameorg(ColumnMapping.NUMERIC);
			ah.setValue(m.group(1).trim());					
			this.mappingMode = MappingMode.VALUE;
		} else {
			if( value.matches(".*['\"]+.*" )) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong parameter " + value);					
			}
			//Expression replace Attribute
			this.mappingMode = MappingMode.EXPRESSION;
			ah.setNameattr(value);
			ah.setNameorg(value);
		}
		this.attributeHandlers.add(ah);

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
			} else {
				if( s.matches(".*['\"]+.*" )) {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong parameter " + s);					
				}
				this.mappingMode = MappingMode.ATTRIBUTE;
				ah.setNameattr(s);
				ah.setNameorg(s);
			}
			this.attributeHandlers.add(ah);
			System.out.println(this);
		}
	}
	
	
	
	/**
	 * this constructor handle the expression case
	 * @param unit
	 * @param values = list of attributes handler
	 * @param expression = expression containing constants and attributes handler
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String[] values, String expression, String label ) throws FatalException{
		this.label = label;
		this.mappingMode = (expression == null)? MappingMode.NOMAPPING:  MappingMode.EXPRESSION;
		this.expression=expression;
		if(values!=null)
			for( String s: values ) {
				AttributeHandler ah = new AttributeHandler();
				ah.setUnit(unit);	
				String v;
				if( (v = this.isConstant(s)) != null ) {
					ah.setNameattr(ColumnMapping.NUMERIC);
					ah.setNameorg(ColumnMapping.NUMERIC);
					ah.setAsConstant();
					ah.setValue(v);					
				} else {
					if( s.matches(".*['\"]+.*" )) {
						FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong parameter " + s);					
					}
//					this.mappingMode = MappingMode.ATTRIBUTE;
					ah.setNameattr(s);
					ah.setNameorg(s);
				}
				this.attributeHandlers.add(ah);
				System.out.println(this);
			}
	}

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
	public boolean byAttribute() {
		return(mappingMode == MappingMode.ATTRIBUTE );
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
			retour += " " + ah + "\n";
		}
		retour+="Expression = "+this.expression+"\n";
		return retour;
	}

}
