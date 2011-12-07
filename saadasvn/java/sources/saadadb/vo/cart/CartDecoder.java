/**
 * 
 */
package saadadb.vo.cart;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import saadadb.util.zip.ZipEntryRef;
import saadadb.util.zip.ZipMap;


/**
 *  Transforms a JSON description of the shopping car in a {@link saadadb.util.zip.ZipMap ZipMap}
 *  used by the archive builder
 *  JSON format:
 
{
nodename:{
	"jobs":[entryrefs...]
   ,"urls":[entryrefs...]}
,
nodename:{
	"jobs":[entryrefs...]
   ,"urls":[entryrefs...]}
}

Where nodename is directory where the enclosed entry refs will be put. That is usually a data treepath.
entryrefs have the following form:

{"name":"...","uri":"....", "relations": [relations....]}

name: name of the entry within the archive
uri : entry identifier (oid or query)
relations: List of relation whose links must be added to the archive. "any-relations' means no filtering

 * @author laurent
 * @version @Id@
 */
public class CartDecoder  {
	private ZipMap zipMap = new ZipMap();

	public ZipMap getZipMap() {
		return zipMap;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public  void decode(String jsonString) {
		JSONObject obj=(JSONObject) JSONValue.parse(jsonString);
		Set<String> ks = obj.keySet();
		for( String folder: ks) {
			Set<ZipEntryRef> entries; 
			if( (entries =  this.zipMap.get(folder)) == null ) {
				entries =  new LinkedHashSet<ZipEntryRef>();
				this.zipMap.put(folder, entries);
			}
			JSONObject obj2=(JSONObject) JSONValue.parse(obj.get(folder).toString());
			Set<String> ks2 = obj2.keySet();
			for( String s2: ks2) {
				int type;
				if( s2.equals("queries") ) {
					type = ZipEntryRef.QUERY_RESULT;
				}
				else {
					type = ZipEntryRef.SINGLE_FILE;
				}
				JSONArray jsa = (JSONArray) JSONValue.parse(obj2.get(s2).toString());
				Iterator it = jsa.iterator();
				while( it.hasNext()) {
					JSONObject jso = (JSONObject) it.next();
					JSONArray jsra ;
					int options = 0;
					if( (jsra = (JSONArray) jso.get("relations")) != null && jsra.size() > 0 && jsra.get(0).toString().equals(ZipEntryRef.ANY_REL)) {
						options = ZipEntryRef.WITH_REL;
					}
					entries.add(new ZipEntryRef(type, jso.get("name").toString(), jso.get("uri").toString(), options)) ;		
				}
			}
		}
	}

	public static void main(String[] args) {
		CartDecoder cd = new CartDecoder();
		cd.decode("{\"cadc\":{\"jobs\":[{\"name\": \"name1\", \"uri\": \"p2j0wdixj65m1omy\"}],\"urls\":[]},  \"cadc2\":{\"jobs\":[{\"name\": \"name2\", \"uri\": \"p2j0wdixj65m1omy\"}],\"urls\":[]} }");
	}
}

