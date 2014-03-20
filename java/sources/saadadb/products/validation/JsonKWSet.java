package saadadb.products.validation;

import org.json.simple.JSONObject;


/**
 * Format of the JSONObject
  	{
    header: [[name, unit, ucd, value (optional)],......]
     columns: [[name, unit, ucd, value (optional)],......]
     }

 * @author michel
 * @version $Id$
 */
public class JsonKWSet extends KeywordsBuilder {

	public JsonKWSet(JSONObject jsonObject) throws Exception {
		super(jsonObject);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.validation.KeywordsBuilder#getInstance()
	 */
	public static KeywordsBuilder getInstance(Object object) throws Exception {
		return new JsonKWSet((JSONObject)object);
	}

}
