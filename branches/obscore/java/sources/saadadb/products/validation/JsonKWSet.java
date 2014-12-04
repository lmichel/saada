package saadadb.products.validation;

import org.json.simple.JSONObject;


/**
 * Format of the JSONObject
  {
     "header": [["RA", "double", "", "", "23.67"],
				.......
		]
		,
    "table": {header: [["RA2000", "double", "", "", "10."],.......
                      ]
                      ,
             data: [[ "10.", "45", "lui"],
                    ......]
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
