package saadadb.query.merger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class CollectionQNode extends QNode {
	private  MetaCollection metacoll= null;
	private LinkedHashMap<String, ClassQNode> class_nodes;
	private final int category;

	/**
	 * @param collection
	 * @throws FatalException
	 */
	CollectionQNode(String collection, int category, Merger merger) throws FatalException {
		super(Database.getCachemeta().getCollectionTableName(collection, category), merger);
		class_nodes = new LinkedHashMap<String, ClassQNode>();
		this.category = category;		
		metacoll = Database.getCachemeta().getCollection(collection);

	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#hasUCD(java.lang.String)
	 */
	public boolean hasUCD(String ucd) {
		return false;
	}
	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#getAttributesHandlers()
	 */
	public Map<String, AttributeHandler> getBusinessAttributesHandlers() {
		return metacoll.getAttribute_handlers(category);
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#getFields()
	 */
	public Field[] getFields() throws FatalException, ClassNotFoundException {
		return Class.forName("generated." + Database.getDbname() + "." + Database.getCachemeta().getCollectionTableName(this.name, category)).getFields();
	}

	/**
	 * @param classname
	 * @throws FatalException
	 */
	protected void addClass(String classname) throws FatalException {
		class_nodes.put(classname, new ClassQNode(classname, this, this.merger));
	}

	/**
	 * Add all classes of the collection to the tree if at least one constraint is mapped.
	 * The control whether classes are really mapped is done while the SQL building
	 * @param builders
	 * @throws Exception 
	 */
	private void addMappedClasses(LinkedHashMap<String, SaadaQLConstraint> builders) throws Exception {
		boolean mapped_query = false;
		String dm_name = "";
		if( this.merger.getVor() == null ) {
			if( builders != null ) {
				for(SaadaQLConstraint e: builders.values()) {
					if( e.isColMapped() || e.isDMMapped() ) {
						mapped_query = true;
						break;
					}
				}
			}
		}
		else {
			dm_name = this.merger.getVor().getName();
			mapped_query = true;
		}
		/*
		 * If no class is specified here, the query covers all class of the collection for mapped constraints.
		 */
		if( mapped_query ) {
			if( builders != null ) {
				for(SaadaQLConstraint e: builders.values()) {
					if( e.isDMMapped() && !this.metacoll.implementsDM(this.merger.getVor(), this.category) ) {
						QueryException.throwNewException(SaadaException.METADATA_ERROR, "Data Model " + dm_name + " not implemented  in collection  " + this.name);							
					}
				}
			}

			if( this.class_nodes.size() == 0 ) {
				for( String mc: Database.getCachemeta().getClassesOfCollection(this.metacoll.getName(), this.category)) {
					this.addClass(mc);
				}	
			}

			ArrayList<String> ucd_not_found_in_class = new ArrayList<String> ();
			if( builders != null ) {
				for(SaadaQLConstraint e: builders.values()) {
					/*
					 * Only keep classes with the requested UCD
					 */
					if( e.isColMapped()) {
						String ucd = e.getMetacolname();
						ArrayList<String> toremove = new ArrayList<String> ();
						for( Entry<String, ClassQNode> ec : this.class_nodes.entrySet() ) {
							ClassQNode cn = ec.getValue();
							if( !cn.hasUCD(ucd) ) {
								if (Messenger.debug_mode)
									Messenger.printMsg(Messenger.DEBUG, "Remove class " + cn.name + " from query coverage: has no queriable UCD :" + ucd);
								toremove.add(ec.getKey());
								ucd_not_found_in_class.add(ucd);
							}
						}
						for( String str: toremove) {
							this.class_nodes.remove(str);						
						}
					}
				}
			}
			/*
			 * Look for UCDs not defined at all in order to avoid late confusing error messages.
			 */
			if( this.class_nodes.size() == 0 ) {
				for(String e: ucd_not_found_in_class) {
					if( this.metacoll.getUCDField(e, true) == null ) {
						QueryException.throwNewException(SaadaException.METADATA_ERROR, "UCD " + e + " not found in collection (or subclasses covered by the query) " + this.name);							
					}
				}
			}
		}
	}

	/**
	 * Used in  case where no class nodes are present. Usually, the select columns are taken from class nodes.
	 * When the is no class nodes, we take columns directly from the SaadaQLConstraints
	 * @param builders
	 * @return
	 * @throws QueryException
	 */
	public String getSelect(LinkedHashMap<String, SaadaQLConstraint> builders) throws QueryException{
		if( builders != null ) {
			for( Entry<String, SaadaQLConstraint>e: builders.entrySet()) {
				SaadaQLConstraint scb = e.getValue();
				String result_colname = e.getKey();
				for(String sql_colname: scb.getSqlcolnames())  {
					String rcn;
					if( scb.isNative() ) {
						rcn = SaadaQLConstraint.getNativeResultColName(result_colname, sql_colname); 
					}
					else if( scb.isPosition() ) {
						rcn = SaadaQLConstraint.getPositionResultColName(sql_colname); 
					}
					else {
						rcn = result_colname;
					}
					ColumunSelectDef csd = new ColumunSelectDef(sql_colname, rcn, ColumunSelectDef.STANDARD);
					this.select.put(rcn, csd);	
				}
			}
		}
		if( this.merger.isAllcolumns()) {
			for(Entry<String, AttributeHandler> ah: this.getBusinessAttributesHandlers().entrySet() ) {
				this.select.put(ah.getKey(), new ColumunSelectDef(ah.getKey(), ah.getKey(), ColumunSelectDef.STANDARD));
			}	
		}
		return this.getSelect();
	}

	/**
	 * Returns an SQL statement related to the input builder set
	 * @param builders
	 * @return
	 * @throws SaadaException
	 */
	public String getSQL(LinkedHashMap<String, SaadaQLConstraint> builders) throws Exception {
		String retour = "";
		this.addMappedClasses(builders);
		if( builders == null || builders.size() == 0 ) {
			if( class_nodes.size() == 0 ) {				
				String select_coll = this.getSelect(builders);
				if( select_coll.indexOf("oidsaada") == -1) {
				//@@@ parenthesis removed
					/*
					 * Avoid having oidsaada twice, SQLite hates that
					 */
					String[] sc = ((select_coll.indexOf("oidsaada") == -1)? new String[]{"oidsaada", select_coll}: new String[]{select_coll});
					return "  SELECT " + saadadb.util.Merger.getMergedArray(sc) 
					+   "\n   FROM " + this.name + "";	
				} else {
					return "  SELECT " + select_coll
					+   "\n   FROM " + this.name + "";	
					
				}
			}
			else if( class_nodes.size() == 1 ) {				
				int cpt=0;
				boolean both = false;
				for( Entry<String, ClassQNode>e: class_nodes.entrySet()) {
					ClassQNode cqn = e.getValue();
					if( this.merger.isAllcolumns() ) {
						cqn.setAllcolumnsSelect();
						this.setAllcolumnsSelect();
						both = true;
					}						
					if( retour.length() != 0 ) {
						retour += "\n    UNION\n";
					}
					if( both ) {
						retour += this.getBothSQL(cqn, cpt);
					}
					else {
						retour += this.getClassSQL(cqn, cpt);
					}
					cpt++;
				}
				return retour;
			}
			else {
				int cpt=0;
				for( Entry<String, ClassQNode>e: class_nodes.entrySet()) {
					if( retour.length() != 0 ) {
						retour += "\n    UNION\n";
					}
					ClassQNode cqn = e.getValue();
					if( this.merger.isAllcolumns() && (this.merger.getVor() != null || class_nodes.size() == 1)) {
						cqn.setAllcolumnsSelect();
						retour += this.getBothSQL(cqn, cpt);
					}
					else {
						retour += this.getCollectionSQL(cqn, cpt);
					}
					cpt++;
				}
				return retour;
			}
		}
		else {
			this.checkUnreadAttributes(builders);
			Set<String> nat_atts = this.getBusinessAttributesHandlers().keySet();
			this.addMappedClasses(builders);
			for( Entry<String, ClassQNode>e: class_nodes.entrySet()) {
				ClassQNode cqn = e.getValue();
				cqn.readSaadaQLConstraints(builders);
				/*
				 * All class column can  be returned if only one class is covered by the query
				 */
				if( this.merger.isAllcolumns() && (this.merger.getVor() != null || class_nodes.size() == 1)) {
					cqn.setAllcolumnsSelect();
				}
			}
			boolean both = false;
			this.readSaadaQLConstraints(builders);
			if( this.merger.isAllcolumns()) {
				this.setAllcolumnsSelect();
				both = true;
			}
			/*
			 *  Check that all builders have been processed. Normally errors must be risen before
			 */
			for( SaadaQLConstraint sqc: builders.values()) {
				/*
				 * DM mapping can contain expressions which are not parsed. 
				 * They can include atts from both classes and collections
				 */
				if( sqc.isDMMapped() ) {
					both = true;
				}
				if( !sqc.isTakenByClass() && !sqc.isTakenByCollection() ) {
					QueryException.throwNewException(SaadaException.METADATA_ERROR, "Can not interpret constraint on " + sqc.getSqlcolnames()[0] + " in collection " + this.name);
				}
			}

			int cpt = 0;
			if( class_nodes.size() > 0 ) {
				TreeSet<String> cq = new TreeSet<String>();
				/*
				 * If there are no constraint on classes, we can get several time the same collection query. 
				 * We filter them with then treset
				 */
				for( Entry<String, ClassQNode>e: class_nodes.entrySet()) {
					ClassQNode cqn = e.getValue();
					//cqn.set_of_selected_columns.setEitherColumns();
					if( !both && cqn.set_of_selected_columns.hasOnlyCollectionColumns() ) {
						cq.add(this.getCollectionSQL(cqn, cpt));
					}
					else if( !both && cqn.set_of_selected_columns.hasOnlyClassColumns() ) {
						cq.add(this.getClassSQL(cqn, cpt));
					}
					else  {
						cq.add(this.getBothSQL(cqn, cpt));
					}
				}
				for(String q : cq) {
					if( retour.length() != 0 ) {
						retour += "\n    UNION\n";
					}
					retour += q;
				}
				cpt++;
			}
			else {
				String select_coll = this.getSelect(builders);
				String where_coll =  insertAlias(this.getWhere(), nat_atts.toArray(new String[0]), this.name); 
				if( where_coll.length() > 0 ) {
					where_coll = "\n   WHERE "  +where_coll;
				}
				//@@@ parenthesis removed
				/*
				 * Avoid having oidsaada twice, SQLite hates that
				 */
				String[] sc = ((select_coll.indexOf("oidsaada") == -1)? new String[]{"oidsaada", select_coll}: new String[]{select_coll});
				retour =  "  SELECT " + saadadb.util.Merger.getMergedArray(sc) 
				+   "\n   FROM " + this.name + " " + where_coll + "";				

			}
			return retour;
		}
	}

	/**
	 * @param cqn
	 * @param cpt
	 * @return
	 * @throws QueryException 
	 */
	private String getClassSQL(ClassQNode cqn, int cpt) throws QueryException {
		String select_class = cqn.set_of_selected_columns.getClassSelect(cqn.name);
		String where_class = cqn.getWhere();
		if( where_class.length() > 0 ) {
			where_class = "\n   WHERE "  +where_class;
		}
		where_class = insertAlias(where_class, cqn.getBusinessAttributesHandlers().keySet().toArray(new String[0]), cqn.name)	;
		where_class = insertAlias(where_class, new String[]{"oidsaada", "namesaada"}, cqn.name)	;
		return "  SELECT " + saadadb.util.Merger.getMergedArray(new String[]{"oidsaada", select_class}) 
		+  "\n   FROM " + cqn.name + " " + where_class ;
	}

	/**
	 * @param cqn
	 * @param cpt
	 * @return
	 * @throws QueryException
	 */
	private String getCollectionSQL(ClassQNode cqn, int cpt) throws QueryException {
		String select_coll = cqn.set_of_selected_columns.getCollectionSelect(this.name);
		String where_coll = cqn.getWhere();
		if( where_coll.length() > 0 ) {
			where_coll = "\n   WHERE "  +where_coll;
		}
		/*
		 * We domn't need alias for collection based queries
		 */
		where_coll = insertAlias(where_coll, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
		return "  SELECT " + saadadb.util.Merger.getMergedArray(new String[]{"oidsaada", select_coll}) 
		+   "\n   FROM " + this.name + " " + where_coll ;				
	}

	/**
	 * @param cqn
	 * @param cpt
	 * @return
	 * @throws QueryException
	 */
	private String getBothSQL(ClassQNode cqn, int cpt) throws QueryException {
		String select_class = cqn.set_of_selected_columns.getClassSelect(cqn.name);
		String where_class = cqn.getWhere();
		String select_coll = cqn.set_of_selected_columns.getCollectionSelect(this.name);
		String where = this.getWhere();
		if( where_class.length() > 0 ) {
			if( where.length() > 0 ) {
				where += " AND ";
			}
			where += where_class;
		}
		if( where.length() > 0 ) {
			where =  "\n   WHERE " + where;
		}
		where = insertAlias(where, cqn.getBusinessAttributesHandlers().keySet().toArray(new String[0]), cqn.name)	;	
		where = insertAlias(where, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
		String cols = saadadb.util.Merger.getMergedArray(new String[]{select_coll, select_class});
		if( cols.trim().length() == 0 ) {
			cols = this.name + ".oidsaada ";			
		}
		else if( !cols.matches("(?i)(.*oidsaada.*)")) {
			cols = this.name + ".oidsaada, "  + cols;
		}
		return "  SELECT " + cols
		+ "\n   FROM " + this.name + " INNER JOIN " + cqn.name + " USING (oidsaada) "  
		+ where ;
	}

	/**
	 * Check that all SQL column names are known by the collection and sub classe3s
	 * @param builders
	 * @throws QueryException
	 */
	private void checkUnreadAttributes(LinkedHashMap<String, SaadaQLConstraint> builders) throws QueryException {
		TreeSet<String> unread_attributes = new TreeSet<String>();
		for( SaadaQLConstraint sqc: builders.values()) {
			if( sqc.isNative() || sqc.isPosition()) {
				for(String a: sqc.getSqlcolnames()) {
					unread_attributes.add(a);
				}
			}
		}
		for(QNode qn: this.class_nodes.values()) {
			for(AttributeHandler ah : qn.getBusinessAttributesHandlers().values()) {
				unread_attributes.remove(ah.getNameattr());
			}
		}
		for(AttributeHandler ah : this.getBusinessAttributesHandlers().values()) {
			unread_attributes.remove(ah.getNameattr());
		}
		if( unread_attributes.size() > 0 ) {
			String retour = "";
			for( String str:unread_attributes ) {
				if( retour.length() > 0 ) {
					retour += ",";
				}
				retour += str;
			}
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Attribute(s) " + retour + " not found in collection (or subclasses covered by the query) " + this.name);	
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#updateSQL(java.util.LinkedHashMap)
	 */
	public void readSaadaQLConstraints(LinkedHashMap<String, SaadaQLConstraint> builders) throws Exception {
		for( Entry<String, SaadaQLConstraint>eb: builders.entrySet()) {
			SaadaQLConstraint scb = eb.getValue();
			if( !scb.isTakenByClass() ) {
				if( scb.isDMMapped()) {
					this.readDMMappedSaadaQLConstraint(scb);				
				}
				else if( scb.isColMapped()) {
					this.readColMappedSaadaQLConstraint(scb);				
				}
				else {
					this.readSaadaQLConstraint(eb.getKey(), scb);	
					scb.takeByCollection();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#updateDMSQL(saadadb.query.constbuilders.SQLConstraint)
	 */
	public void readDMMappedSaadaQLConstraint(SaadaQLConstraint scb) throws QueryException {
		/*
		 * Ici rechercher le mapping
		 */
		String mapping_expression = "2*xyz_csa/log(qwery)";
		Pattern p = Pattern.compile("@@@(" + RegExp.UTYPE + ")@@@", Pattern.DOTALL);
		Matcher m = p.matcher(scb.getWhere());
		String new_where = scb.getWhere();
		while( m.find()  ) {
			if( this.where.length() != 0 ) {
				this.where += " AND "; 
			}
			new_where = new_where.replaceAll(m.group(1), "(" + mapping_expression + ")");
		}
		this.where = new_where.replaceAll("@@@", "");
		this.insertAliases() ;	
		for( String sql_colname: scb.getSqlcolnames()) {
			this.select.put(sql_colname, new ColumunSelectDef(sql_colname, insertAlias(mapping_expression, new String[]{"xyz_csa"}, this.name), ColumunSelectDef.STANDARD));	
		}
		scb.takeByCollection();
	}

	/* (non-Javadoc)
	 * @see saadadb.query.merger.QNode#updateColSQL(saadadb.query.constbuilders.SQLConstraint)
	 */
	public void readColMappedSaadaQLConstraint(SaadaQLConstraint scb) throws QueryException {
		/*
		 * Ici rechercher le mapping
		 */
		String ucd = scb.getMetacolname();
		AttributeHandler ah = this.metacoll.getUCDField(ucd, true);
		if( ah != null ) {
			Pattern p = Pattern.compile("@@@(" + RegExp.UTYPE + ")@@@", Pattern.DOTALL);
			Matcher m = p.matcher(scb.getWhere());
			String new_where = scb.getWhere();
			while( m.find()  ) {
				if( this.where.length() != 0 ) {
					this.where += " AND "; 
				}
				new_where = new_where.replaceAll(m.group(1), "(" + ah.getNameattr() + ")");
			}
			this.where = new_where.replaceAll("@@@", "");
			for( String sql_colname: scb.getSqlcolnames()) {
				this.select.put(sql_colname, new ColumunSelectDef(sql_colname, insertAlias(ah.getNameattr(), new String[]{"xyz_csa"}, this.name), ColumunSelectDef.STANDARD));	
			}
			this.insertAliases() ;
			scb.takeByCollection();
		}
		else {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "UCD  " + ucd + " cannot be solved in collection " + this.name);
		}
	}

	/**
	 * @return
	 */
	public ArrayList<String> getCoveredClasses() {
		ArrayList<String> retour = new ArrayList<String>();
		for( ClassQNode cn: this.class_nodes.values()) {
			retour.add(cn.name);
		}
		return retour;

	}
}
