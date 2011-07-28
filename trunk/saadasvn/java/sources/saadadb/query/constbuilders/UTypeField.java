package saadadb.query.constbuilders;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;

public class UTypeField extends MappedField {
	protected VOResource vor;

	public UTypeField(VOResource vor, String utype,String operateur,String value,String unit) throws SaadaException{
		super(utype, operateur, value, unit, SaadaQLConstraint.DM_MAPPED);
		if( vor == null ) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "UType fields require a (not null) data model");							
		}
		this.vor  = vor;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.query.constbuilders.SaadaQLConstraint#getDM()
	 */
	public VOResource getDM(){
		return vor;
	}

}
 
