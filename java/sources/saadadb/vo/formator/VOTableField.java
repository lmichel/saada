package saadadb.vo.formator;

import cds.savot.model.SavotField;

/**
 * @author laurentmichel
 *@version $Id: VOTableField.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class VOTableField extends SavotField {
    
    // Stores th type of field: saada (or collection) field, class field or user-defined field
    private int saadaType;
    public static final int T_USER_DEFINED = 1, T_CLASS = 2, T_SAADA = 3;
    
    public  VOTableField(int saadaType) {
	super();
	this.saadaType = saadaType;
    }
    
    public int getSaadaType() {
	return saadaType;
    }
    
}
  
