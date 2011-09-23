package saadadb.query.constbuilders;
import saadadb.exceptions.SaadaException;

public class UCDField extends MappedField {
	
	public UCDField(String ucd,String operateur,String value,String unit) throws SaadaException{
 		super(ucd, operateur, value, unit, SaadaQLConstraint.COL_MAPPED);
	}

}

 