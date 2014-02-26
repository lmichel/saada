package saadadb.query.result;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * This class manages a metadatree grouping the description if the SQL columns returned by the query engine 
 * with the attributes handlers if the touched Saada class attributes
 * The name of the SQL result column is taken
 * @author michel
 * * @version $Id: SaadaQLMetaSet.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class SaadaQLMetaSet {
	protected  LinkedHashMap<String, ColumnRef> metatree;

	/**
	 * @param colnames
	 */
	public SaadaQLMetaSet(Set <AttributeHandler> colnames, Set<AttributeHandler> const_attributes) {
		metatree = new LinkedHashMap<String, ColumnRef>();
		if( colnames != null ) {
			for(AttributeHandler ah: colnames) {

				ColumnRef cr = new ColumnRef(ah);
				if( const_attributes != null ) {
					for( AttributeHandler cah: const_attributes) {
						String selected_att_name;
						if( ah.getNameattr().startsWith("ucd_")) {
							selected_att_name = cah.getUCDNickname();
						}
						else if( ah.getNameattr().startsWith("utype_")) {
							selected_att_name = cah.getUTYPENickname();
						}
						else {
							selected_att_name = cah.getNameattr();
						}
						if( selected_att_name.equals(ah.getNameattr()) ){
							cr.addClassAttribute(cah);
						}
					}
				}
				metatree.put(ah.getNameattr(), cr);
			}
		}		
	}
	/**
	 * @param ah
	 */
	protected void add(AttributeHandler ah) {
		ColumnRef cr = new ColumnRef(ah);
		cr.addClassAttribute(ah);
		metatree.put(ah.getNameattr(), cr);
	}

	/**
	 * @param col_name
	 * @return
	 * @throws QueryException
	 */
	public AttributeHandler getSQLColumnHandler(String col_name) throws QueryException {
		ColumnRef retour;
		if( (retour = metatree.get(col_name)) == null) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "No column named <" + col_name  + "> in the query");
			return null;
		}
		else {
			return retour.SQLcolumn;
		}
	}

	/**
	 * @param col_name
	 * @return
	 * @throws QueryException
	 */
	public Set<AttributeHandler> getClassColumnHandlers(String col_name) throws QueryException {
		ColumnRef retour;
		if( (retour = metatree.get(col_name)) == null) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "No column named <" + col_name  + "> in the query");
			return null;
		}
		else {
			return retour.class_attributes;
		}
	}

	/**
	 * @param col_name
	 * @param class_name
	 * @return
	 * @throws QueryException
	 */
	public AttributeHandler getClassColumnHandler(String col_name, String class_name) throws QueryException {
		for( AttributeHandler ah:  getClassColumnHandlers(col_name) ) {
			if( ah.getClassname().equals(class_name)) {
				return ah;
			}
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "No data of class <" + class_name + "> in column <" + col_name + ">");
		return null; 
	}

	/**
	 * @return
	 */
	public Set<String> keySet() {
		return metatree.keySet();
	}
	/**
	 * @return
	 */
	public int size() {
		return metatree.size();
	}
	
	/**
	 * @return
	 */
	public Set<AttributeHandler> getHandlers() {
		LinkedHashSet<AttributeHandler> retour = new LinkedHashSet<AttributeHandler>();
		for( ColumnRef cr: this.metatree.values()) {
			retour.add(cr.SQLcolumn);
		}
		return retour;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		try {
			String retour = "";
			for( String s: this.keySet()) {
				retour += "key: " + s + "\n";
				AttributeHandler ah = this.getSQLColumnHandler(s);
				retour += "   Column: " + ah.getNameattr() + " " + ah.getType()+ "\n";
				for( AttributeHandler cah: this.getClassColumnHandlers(s)) {
					retour += "   Class: " + cah.getClassname() + " " + cah.getNameattr() + " " + cah.getType() + " " + cah.getUcd()+ "\n";

				}
			}
			return retour;
		} catch (QueryException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * @author michel
	 *
	 */
	class ColumnRef {
		private AttributeHandler SQLcolumn;
		private Set<AttributeHandler> class_attributes;

		private ColumnRef(AttributeHandler ah) {
			SQLcolumn = ah;
			class_attributes = new LinkedHashSet<AttributeHandler>();			
		}
		private ColumnRef(String colname, String unit) {
			SQLcolumn = new AttributeHandler();
			SQLcolumn.setNameattr(colname);
			SQLcolumn.setNameorg(colname);
			if( unit!= null ) {
				SQLcolumn.setUnit(unit);
			}
			class_attributes = new TreeSet<AttributeHandler>();			
		}
		private void addClassAttribute(AttributeHandler ah){
			class_attributes.add(ah);
		}
	}
}