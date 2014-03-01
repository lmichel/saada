package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private MappingMode mappingMode = MappingMode.NOMAPPING;
	private List<AttributeHandler> attributeHandlers =new ArrayList<AttributeHandler>();
	private static final Pattern constPattern = Pattern.compile("^'(.*)'$");
	private static final Pattern numPattern = Pattern.compile("^(?:(" + RegExp.NUMERIC + "))$");

	//private static final Pattern constPattern = Pattern.compile("(?:^(?:(?:'(.*)')|(?:(" + RegExp.NUMERIC + ")))$)");
	//private static final Pattern constPattern = Pattern.compile("(?:^(?:(" + RegExp.NUMERIC + "))$)");
	//private static final Pattern constPattern = Pattern.compile("^([0-9]+)$");

	/**
	 * @param mappingMode
	 * @param unit
	 * @param value
	 * @throws FatalException
	 */
	ColumnMapping(MappingMode mappingMode, String unit, String value) throws FatalException{
		this.mappingMode = mappingMode;
		AttributeHandler ah = new AttributeHandler();
		if( this.mappingMode == MappingMode.VALUE) {
			ah.setNameattr(ColumnMapping.NUMERIC);
			ah.setNameorg(ColumnMapping.NUMERIC);
			ah.setUnit(unit);
			ah.setValue(value);
			this.attributeHandlers.add(ah);
		} else if( this.mappingMode == MappingMode.ATTRIBUTE) {
			ah.setNameattr(value);
			ah.setNameorg(value);
			ah.setUnit(unit);			
			this.attributeHandlers.add(ah);
		} else if( this.mappingMode != MappingMode.NOMAPPING) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Mapping mode SQL not suported yet");
		}
	}

	/**
	 * @param unit
	 * @param values
	 * @throws FatalException
	 */
	ColumnMapping(String unit, String value) throws FatalException{
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
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrog parameter " + value);					
			}
			this.mappingMode = MappingMode.ATTRIBUTE;
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
	ColumnMapping(String unit, String[] values) throws FatalException{
		this.mappingMode = (values == null)? MappingMode.NOMAPPING:  MappingMode.VALUE;
		for( String s: values ) {
			AttributeHandler ah = new AttributeHandler();
			ah.setUnit(unit);	
			String v;
			if( (v = this.isConstant(s)) != null ) {
//			Matcher m = constPattern.matcher(s.trim());
//			boolean f = m.find();
//			System.out.println(f + " " + constPattern + " " + s + " "  + " " + m.groupCount());
//			for ( int i=1 ; i<=m.groupCount() ; i++) System.out.println(m.group(i));
//			if( f && m.groupCount() == 2 ) {
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

	/**
	 * @return
	 */
	public AttributeHandler getHandler() {
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
		AttributeHandler ah = this.getHandler();
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
		return retour;
	}

}
