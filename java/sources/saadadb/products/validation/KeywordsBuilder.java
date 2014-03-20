package saadadb.products.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.meta.AttributeHandler;
import saadadb.util.ChangeKey;

/**
 * @author michel
 * @version $Id$
 */
public abstract class KeywordsBuilder {

	public List<AttributeHandler> headerKWs;
	public List<AttributeHandler> columnKWs;
	
	
	/**
	 * @throws Exception
	 */
	public KeywordsBuilder() throws Exception {
		this.headerKWs = null;
		this.columnKWs = null;
	}
	public KeywordsBuilder(String[][] ahDef) throws Exception {
		this.headerKWs = buildAttributeHandler(ahDef);
		this.columnKWs = null;
	}
	public KeywordsBuilder(String[][] hahDef, String[][] cahDef) throws Exception {
		this.headerKWs = buildAttributeHandler(hahDef);
		this.columnKWs = buildAttributeHandler(hahDef);
	}
	/**
	 * Format of the JSONObject
  	  {
      header: [[name, unit, ucd, value (optional)],......]
      columns: [[name, unit, ucd, value (optional)],......]
      }
	 * @param jsonObject
	 * @throws Exception
	 */
	public KeywordsBuilder(JSONObject jsonObject) throws Exception {
		this.headerKWs = buildAttributeHandler((JSONArray) jsonObject.get("header"));
		this.columnKWs = buildAttributeHandler((JSONArray) jsonObject.get("columns"));
	}
	/**
	 * Buid an AH list for a 2dim array of strings;
	 * 0: name, 1: unit; 2: ucd, 3(optional): value
	 * @param ahDef
	 * @return
	 */
	public static List<AttributeHandler > buildAttributeHandler(String[][] ahDef) throws Exception {
		if(ahDef == null){
			return null;
		}
		List<AttributeHandler> retour = new ArrayList<AttributeHandler>();
		for( String[] ahs: ahDef){
			AttributeHandler ah = new AttributeHandler();
			ah.setNameorg(ahs[0]);
			ah.setNameattr(ChangeKey.changeKey(ahs[0]));
			ah.setUnit(ahs[1]);
			ah.setUcd(ahs[2]);
			if( ahs.length > 3 ){
				ah.setValue(ahs[3]);
			}
			retour.add(ah);	
		}
		return retour;
	}
	/**
	 * Build an AH list for a JSON array
	 * [[name, unit, ucd, value (optional)],......]
	 * @param ahDef
	 * @return
	 */	
	@SuppressWarnings("unchecked")
	public static List<AttributeHandler > buildAttributeHandler(JSONArray  ahDef) throws Exception {
		if(ahDef == null){
			return null;
		}
		Iterator<JSONArray> iterator = ahDef.iterator();  
		List<AttributeHandler> retour = new ArrayList<AttributeHandler>();
		while (iterator.hasNext()) {  
			JSONArray jsonah = iterator.next();
			AttributeHandler ah = new AttributeHandler();
			ah.setNameorg(jsonah.get(0).toString());
			ah.setNameattr(ChangeKey.changeKey(jsonah.get(0).toString()));
			ah.setUnit(jsonah.get(1).toString());
			ah.setUcd(jsonah.get(2).toString());
			if( jsonah.size() > 3 ){
				ah.setValue(jsonah.get(3).toString());
			}
			retour.add(ah);	
		}
		return retour;
	}
	
	/**
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static List<AttributeHandler > scrambleList(List<AttributeHandler > list) throws Exception{
		if(list == null){
			return null;
		}
		List<AttributeHandler> retour = new ArrayList<AttributeHandler>();
		for( AttributeHandler ah: list){
			retour.add(ah);
		}
		int size = retour.size();
		for( int i=0 ; i<10 ; i++ ) {
			int p1 = (int)(Math.random() * size);
			int p2 = (int)(Math.random() * size);
			AttributeHandler ah = retour.get(p1);
			retour.set(p1, retour.get(p2));
			retour.set(p2, ah);
		}
		return retour;

	}
	/**
	 * Returns the same but with KS in a different order
	 * @return
	 * @throws Exception
	 */
	public KeywordsBuilder getScrambledClone() throws Exception{
		KeywordsBuilder retour = (KeywordsBuilder) this.clone();
		retour.headerKWs = scrambleList(this.headerKWs);
		retour.columnKWs = scrambleList(this.columnKWs);
		return retour;
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	public static KeywordsBuilder getInstance(Object param) throws Exception{ return null; };
}
