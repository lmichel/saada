/**
 * 
 */
package saadadb.vo.cart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Merger;
import saadadb.util.zip.ZipEntryRef;
import saadadb.util.zip.ZipMap;
import saadadb.vocabulary.RegExp;


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
 * 01/2014 : support OIDs of individual sources : All are merged in a query and processed as any query result
 */
public class CartDecoder  {
	private ZipMap zipMap = new ZipMap();

	public ZipMap getZipMap() {
		return zipMap;
	}

	/**
	 * @param args
	 * @throws QueryException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public  void decode(String jsonString) throws QueryException {
		JSONObject obj=(JSONObject) JSONValue.parse(jsonString);
		Set<String> ks = obj.keySet();
		/*
		 * Loop on the data tree path
		 */
		for( String folder: ks) {
			Set<ZipEntryRef> entries; 
			if( (entries =  this.zipMap.get(folder)) == null ) {
				entries =  new LinkedHashSet<ZipEntryRef>();
				this.zipMap.put(folder, entries);
			}
			JSONObject obj2=(JSONObject) JSONValue.parse(obj.get(folder).toString());
			Set<String> ks2 = obj2.keySet();
			/*
			 * Loop on the entry on one datatree path
			 */
			List<Long> singleEntries = new ArrayList<Long>();
			boolean entriesWithRel = false;
			for( String s2: ks2) {
				int type;
				if( s2.equals("queries") ) {
					type = ZipEntryRef.QUERY_RESULT;
				} else {
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
					String uri = jso.get("uri").toString();
					String name = jso.get("name").toString();
					if( uri.matches(RegExp.FITS_INT_VAL)) {
						long oid = Long.parseLong(uri);
						if( SaadaOID.getCategoryNum(oid) == Category.ENTRY) {
							singleEntries.add(oid);
							if( options == ZipEntryRef.WITH_REL ) entriesWithRel = true;
						} else {
							entries.add(new ZipEntryRef(type, name,uri, options)) ;									
						}
					} else {
						entries.add(new ZipEntryRef(type, name, uri, options)) ;		
					}
				}
			}
			addSingleEntries(entries, singleEntries, entriesWithRel);
		}
	}

	/**
	 * Transform all oids given in singleEntries as a query result and put that query as ZipRef which is added to zipRefs
	 * @param zipRefs
	 * @param singleEntries
	 * @param entriesWithRel
	 * @throws QueryException 
	 */
	private void addSingleEntries(Set<ZipEntryRef> zipRefs, Collection<Long>  singleEntries, boolean entriesWithRel) throws QueryException{
		if( singleEntries.size() == 0 ){
			return;
		}
		String collection = "";
		String classe = "";
		String classes = "*";
		for( Long oid: singleEntries ) {
			String lcoll = SaadaOID.getCollectionName(oid);
			if( collection.length() > 0 && !lcoll.equals(collection)) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "The node contains catalogue entries from differents collections");
			} else {
				collection = lcoll;
			}
			
			String lclass = SaadaOID.getClassName(oid);
			if( classe.length() > 0 && !lclass.equals(classe)) {
				classes = "*";
			} else {
				classes = lclass;
			}
		}
		String query = "Select ENTRY From " + classes + " In " + collection + " WhereAttributeSaada { ";
		query += Merger.getMergedCollection(singleEntries, "oidsaada = ", " or ") + "}";
		zipRefs.add(new ZipEntryRef(ZipEntryRef.QUERY_RESULT, "IndividualSourceSelection", query, ((entriesWithRel)? ZipEntryRef.WITH_REL: 0))) ;		
	}
}

