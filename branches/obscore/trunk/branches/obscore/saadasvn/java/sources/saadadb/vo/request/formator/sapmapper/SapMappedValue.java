package saadadb.vo.request.formator.sapmapper;

/**
 * This class is carrying values of S*AP field computed by {@link SapFieldMapper}
 * fieldId: reminder of the identifier of the requested field (UCD or UTYPE);
 * fieldValue value for that field
 * 
 * @author michel
 * @version $Id$
 *
 */
public class SapMappedValue {
	/**
	 * eminder of the identifier of the requested field (UCD or UTYPE);
	 */
	public String fieldId;
	/**
	 * value for that field
	 */
	public String fieldValue;
	/**
	 * true if the field is not set (WCS field for an entry e.g.)
	 */
	public boolean isNotSet;
	/**
	 * True if the value must be wrapped in a CDATA XML modifier
	 */
	public boolean isCdata;
	
	/**
	 * For perf reasons, we do not make e new instance for each field of each row of the query response.
	 * SO we prefer init again and again the same instance
	 * @param fieldId
	 */
	void init(String fieldId){
		this.fieldId = fieldId;
		this.fieldValue = null;
		this.isNotSet = true;
		this.isCdata = false;
	}
}
