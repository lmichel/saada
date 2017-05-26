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

	private MappingMode mappingMode = MappingMode.NOMAPPING;
	private List<AttributeHandler> attributeHandlers =new ArrayList<AttributeHandler>();
	private static final Pattern constPattern = Pattern.compile("^'(.*)'$");

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
			ah.setNameattr("Numeric");
			ah.setNameorg("Numeric");
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
	ColumnMapping(String unit, String[] values) throws FatalException{
		this.mappingMode = (values == null)? MappingMode.NOMAPPING:  MappingMode.VALUE;
		for( String s: values ) {
			AttributeHandler ah = new AttributeHandler();
			ah.setUnit(unit);			
			Matcher m = constPattern.matcher(s);
			if( m.find() && m.groupCount() == 1 ) {
				ah.setNameattr("Numeric");
				ah.setNameorg("Numeric");
				ah.setValue(m.group(1).trim());					
			} else {
				if( s.matches(".*['\"]+.*" )) {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrog parameter " + s);					
				}
				this.mappingMode = MappingMode.ATTRIBUTE;
				ah.setNameattr(s);
				ah.setNameorg(s);
			}
			this.attributeHandlers.add(ah);
		}
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
	public AttributeHandler getValue() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			return attributeHandlers.get(0);
		}
	}
	/**
	 * @return
	 */
	public List<AttributeHandler> getValues() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			return attributeHandlers;
		}
	}
	
	public String toString() {
		String retour = "Mode " + this.mappingMode + "\n";
		for( AttributeHandler ah: this.attributeHandlers ) {
			retour += " " + ah + "\n";
		}
		return retour;
	}
	
}
