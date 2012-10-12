package ajaxservlet.formator;

/**
 * 
 * @author Clémentine Frère
 * 
 * contact : frere.clementine[at]gmail.com
 *
 */

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import saadadb.util.Messenger;

public class StoredFilter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String rawjson;
	private String  saadaclass;
	private ArrayList<String> collection;
	private String category;
	private ArrayList<String> relationship_show;
	private ArrayList<String> relationship_query;
	private boolean ucd_show;
	private boolean ucd_query;
	private ArrayList<String> specialField;
	private ArrayList<String> collection_show;
	private ArrayList<String> collection_query;

	/** * @version $Id$

	 * creates a StoredFilter from a file containing
	 * the JSONstring of the said filter
	 * @param json
	 */
	public StoredFilter(FileReader json)  throws Exception{
		String rawContent = "";
		collection = new ArrayList<String>();
		relationship_show = new ArrayList<String>();
		relationship_query = new ArrayList<String>();
		specialField = new ArrayList<String>();
		collection_show = new ArrayList<String>();
		collection_query = new ArrayList<String>();

		// remplissage du String avec le contenu du fichier
		char tmp;
		while (json.ready()) {
			tmp = (char) json.read();
			rawContent += tmp;
		}
		init(rawContent);
	}
	public StoredFilter(String rawContent)  throws Exception{
		collection = new ArrayList<String>();
		relationship_show = new ArrayList<String>();
		relationship_query = new ArrayList<String>();
		specialField = new ArrayList<String>();
		collection_show = new ArrayList<String>();
		collection_query = new ArrayList<String>();
		init(rawContent);
	}

	public void init(String rawContent) throws Exception {
		rawjson = rawContent;
		// initialisation des variable non-listes
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(rawContent);
		JSONObject obj1 = (JSONObject) obj;
		category = (String) obj1.get("category");
		String tmp2 = (String) obj1.get("ucd.show");
		ucd_show = (tmp2.compareTo("true") == 0) ? true : false;
		tmp2 = (String) obj1.get("ucd.query");
		ucd_query = (tmp2.compareTo("true") == 0) ? true : false;

		// initialisation des attributs liste
		JSONArray array;
		JSONObject obj2;

		String sc = (String) obj1.get("saadaclass");
		/*
		 * Backward compatibilty
		 */
		if( sc == null ) {
			saadaclass = FilterKeys.ANY_CLASS;

		} else {
			saadaclass = sc;
		}

		array = (JSONArray) obj1.get("collection");

		for (int i = 0; i < array.size(); i++) {
			String var = (String) array.get(i);
			collection.add(var);
		}
		array = (JSONArray) obj1.get("specialField");

		for (int i = 0; i < array.size(); i++) {
			String var = (String) array.get(i);
			specialField.add(var);
		}

		obj2 = (JSONObject) obj1.get("relationship");
		array = (JSONArray) obj2.get("show");

		for (int i = 0; i < array.size(); i++) {
			String var = (String) array.get(i);
			relationship_show.add(var);
		}

		array = (JSONArray) obj2.get("query");

		for (int i = 0; i < array.size(); i++) {
			String var = (String) array.get(i);
			if (var != null) {
				relationship_query.add(var);
			} else {
				relationship_query.add(FilterKeys.ANY_RELATION);
			}
		}


		obj2 = (JSONObject) obj1.get("collections");
		array = (JSONArray) obj2.get("show");

		for (int i = 0; i < array.size(); i++) {
			String var = (String) array.get(i);
			collection_show.add(var);
		}

		array = (JSONArray) obj2.get("query");

		for (int i = 0; i < array.size(); i++) {
			String var = (String) array.get(i);
			collection_query.add(var);
		}

	}

	public void store(String filename) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Save filter " + filename);
		FileWriter fw = new FileWriter(filename);
		fw.write(this.rawjson);
		fw.close();
	}

	public String getSaadaclass() {
		return saadaclass;
	}

	public ArrayList<String> getCollection() {
		return collection;
	}

	public String getFirstCollection() {
		return collection.get(0);
	}

	public String getCategory() {
		return category;
	}

	public ArrayList<String> getRelationship_show() {
		return relationship_show;
	}

	public ArrayList<String> getRelationship_query() {
		return relationship_query;
	}

	public boolean getUcd_show() {
		return ucd_show;
	}

	public boolean getUcd_query() {
		return ucd_query;
	}

	public ArrayList<String> getSpecialField() {
		return specialField;
	}

	public ArrayList<String> getCollection_show() {
		return collection_show;
	}

	public ArrayList<String> getCollection_query() {
		return collection_query;
	}

	public String getRawJSON() {
		return rawjson;
	}

	public String getTreepath() {
		String retour = this.getFirstCollection() + "." + category;
		if( isCLassSpecific() )  {
			retour += "." + saadaclass;
		}
		return retour;
	}
	
	public boolean isCLassSpecific() {
		if(saadaclass != null && saadaclass.length() > 0 && !saadaclass.equals(FilterKeys.ANY_CLASS) && !saadaclass.equals("*")) {
			return  true;
		} else {
			return false;
		}
	}

	public String toString() {
		return ("Filtre : [" + this.getCollection()+ "," + this.getCategory() + "," + this.getSaadaclass() + "]");
	}

}
