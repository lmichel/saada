package saadadb.query.merger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.query.parser.SelectFromIn;

public class Merger {
	private final LinkedHashMap<String, CollectionQNode> collection_nodes;
	private final int category; 
	private /*final*/ LinkedHashMap<String, SaadaQLConstraint>	builders;
	private ArrayList<String> covered_classes = null;
	private VOResource vor;
	private boolean allcolumns;
	/**
	 * @param sfi
	 * @throws FatalException
	 */
	public Merger(SelectFromIn sfi, boolean all_columns) throws FatalException {
		this(sfi.getCatego(), all_columns);
		if( sfi.getListColl()[0].equals("*")) {
			for( String coll: Database.getCachemeta().getCollection_names()) {
				this.addCollection(coll);
			}
		}
		else if( sfi.getListColl().length == 1) {
			String collectionname = sfi.getListColl()[0];
			this.addCollection(collectionname);
			String classename = sfi.getListClass()[0];
			if( !classename.equals("*") ) {
				for( String classe: sfi.getListClass() ) {
					this.addClass(collectionname, classe);
				}
			}
		}
		else {
			for( String coll: sfi.getListColl() ) {
				this.addCollection(coll);					
			}
		}
	}
	/**
	 * @param category
	 */
	public Merger(int category, boolean all_columns) {
		this.collection_nodes = new LinkedHashMap<String, CollectionQNode>();
		this.builders = new LinkedHashMap<String, SaadaQLConstraint>();
		this.category = category;
		this.allcolumns = all_columns;
	}

	/**, VOResource vor
	 * @param category
	 * @param builders
	 * @throws SaadaException 
	 */
	public Merger(int category, LinkedHashMap<String, SaadaQLConstraint> builders, boolean all_columns) throws SaadaException {
		this.allcolumns = all_columns;
		this.collection_nodes = new LinkedHashMap<String, CollectionQNode>();
		this.category = category;
		this.setBuilderList(builders);
	}

	/**
	 * @return
	 */
	public Set<String> getCollectionNames() {
		return collection_nodes.keySet();
	}
	/**
	 * @param builders
	 * @throws SaadaException 
	 */
	public void setBuilderList(LinkedHashMap<String, SaadaQLConstraint>	builders) throws SaadaException {
		this.builders = builders;		
		this.setDM();
	}

	/**
	 * COnnect the merger on the DM found in constraints.
	 * @throws SaadaException when 2 different DMs are found
	 * 
	 */
	private void setDM() throws SaadaException {
		for( SaadaQLConstraint sqc: builders.values() ) {
			if( sqc.getDM() != null ) {
				String dm_name = sqc.getDM().getName();
				if( this.vor != null && !this.vor.getName().equals(dm_name)) {
					QueryException.throwNewException(SaadaException.INTERNAL_ERROR, "Can not build a query with 2 different DMs (" 
						+ this.vor.getName() + " and " + dm_name + ")");
				}
				else {
					this.vor =  Database.getCachemeta().getVOResource(dm_name);
				}
			}
		}		
	}
	
	/**
	 * Force to use a DM even if there is no reference to it in constraints
	 * @param vor
	 */
	public void setDM(VOResource vor) {
		this.vor = vor;
	}
	/**
	 * @param collectionname
	 * @throws FatalException
	 */
	public void addCollection(String collectionname) throws FatalException {
		collection_nodes.put(collectionname, new CollectionQNode(collectionname, category, this));
	}

	/**
	 * @return
	 */
	public VOResource getVor() {
		return vor;
	}
	/**
	 * @return
	 */
	public boolean isAllcolumns() {
		return allcolumns;
	}
	/**
	 * @param collectionname
	 * @param classname
	 * @throws FatalException
	 */
	public void addClass(String collectionname, String classname) throws FatalException {
		collection_nodes.get(collectionname).addClass(classname);
	}

	/**
	 * @return
	 * @throws QueryException 
	 */
	public String getSQL() throws Exception {
		String retour = "";
		String select = "oidsaada";
		LinkedHashSet<String> skws = new LinkedHashSet<String>();
		/*
		 * Get unique select keywords
		 */
		for( Entry<String, SaadaQLConstraint>e: builders.entrySet()) {
			String key = e.getKey();
			SaadaQLConstraint sqc = e.getValue();
			for( String rcn: sqc.getResultColNames(key) ) {
				skws.add(rcn);
			}
		}
		for( String skw: skws ){
			if( !skw.endsWith("oidsaada")) {
				select +=  ", " + skw;
			}
		}
		int cpt=0;
		/*
		 * Takes all fields of collection level
		 */
		if( builders.size() == 0 ) select = "*";
		/*
		 * Make an union when there are multiple collections
		 */
		if( collection_nodes.size() > 1 ) {
			for( Entry<String, CollectionQNode>e: collection_nodes.entrySet()) {
				cpt++;
				if( retour.length() != 0 ) {
					retour += "\nUNION ";
				}
				//@@@ parenthesis removed
				retour += "\n" + e.getValue().getSQL(builders) + "\n   " ;
			}
			
			retour =  "SELECT "+ select + " FROM (\n" + retour  + "\n) AS tcoll";
		}
		/*
		 * One collection: just one query
		 */
		else {
			for( Entry<String, CollectionQNode>e: collection_nodes.entrySet()) {
				retour =  e.getValue().getSQL(builders) ;
				break;
			}	
		}
		/*
		 * Store the list of classes really covered by the query
		 */
		covered_classes = new ArrayList<String>();
		for( Entry<String, CollectionQNode>e: collection_nodes.entrySet()) {
			covered_classes.addAll(e.getValue().getCoveredClasses());
		}
		
		for( SaadaQLConstraint sqc: this.builders.values()) {
			if( sqc.isGlobal() ) {
				retour += "\n" + sqc.getWhere();
			}
		}
		return retour;
	}
	
	/**
	 * @return
	 */
	public String[] getCoveredClasses() {
		return covered_classes.toArray(new String[0]);
	}
}
