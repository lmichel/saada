package adqlParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import saadadb.cache.CacheMeta;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.VOResource;
import adqlParser.parser.DBConsistency;
import adqlParser.parser.ParseException;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLComparison;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;
import adqlParser.query.ComparisonOperator;

/**
 * Checks whether each reference to the database generated by Saada is correct (columns and tables existence)
 * and sets the meta data of each selected column.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see DBConsistency
 */
public class SaadaDBConsistency extends DBConsistency {

	/** List of columns which can be found either in class or collection of a SaadaDB. */
	public final static String[] MIXED_COLUMNS = new String[]{"oidsaada", "namesaada"};

	public final static CacheMeta cache = Database.getCachemeta();

	protected HashMap<String, AttributeHandler> columnsMeta;

	public SaadaDBConsistency(){
		columnsMeta = new HashMap<String, AttributeHandler>();
	}


	public HashMap<String, AttributeHandler> getColumnsMeta() {
		return columnsMeta;
	}


	public boolean columnExists(String columnName, String tableAlias) throws ParseException {
		return columnName != null && (super.columnExists(columnName, tableAlias) || !columnName.startsWith("_"));
	}

	public boolean selectedColumnExists(ADQLColumn column) throws ParseException {
		if (column == null)
			return false;

		String colName = column.getColumn();

		if (debug) System.out.print("### SELECTED COLUMN EXISTS: "+colName+" [in "+column.getPrefix()+"] ? ");

		boolean result = super.columnExists(colName, column.getPrefix());
		if (result){
			addColumnAlias((column.getAlias() != null)?column.getAlias():colName, colName);
			((ADQLColumnAndMeta)column).setMeta(columnsMeta.get(colName));
		}

		if (debug) System.out.println(result?"YES !":"NO !");

		return result || !colName.startsWith("_");
	}

	@Override
	public boolean tableExists(String tableName) {
		boolean exists = false;

		String tableAlias = getTableAlias(tableName);
		if (tableAlias == null || tableAlias.trim().length() == 0)
			tableAlias = tableName;

		if (getClassName(tableName) != null)
			exists = true;
		else
			try {
				/*
				 * In this case, TAP request access table which are not in then Saada schema
				 */
				if( tableName.startsWith("tables") || tableName.startsWith("schemas") || 
						tableName.startsWith("columns") || tableName.startsWith("keys") ||	
						tableName.startsWith("key_columns") || VOResource.getResource(tableName) != null ){
					exists = true;
				}
				else{
					int ind = tableName.lastIndexOf('_');
					if (ind > 0){
						String collectionName = tableName.substring(0, ind);
						if (getCollectionName(collectionName) != null){
							Category.getCategory(tableName);
							exists = true;
						}
					}
				}
			} catch (Exception e) { ;}

			return exists;
	}

	/**
	 * <p>Gets the former class name if the specified class exists.</p>
	 * <p><i><u>Note:</u><br />
	 * The function {@link CacheMeta#classExists(String)} checks also the existence of the given class but
	 * take care of the case of the given class name.
	 * However {@link SaadaDBConsistency#getClassName(String)} checks the existence of the given class
	 * whatever the case of the given name and returns the class name (with its "true" case) found in the cache.
	 * </i></p>
	 * 
	 * @param className	The name of the class whose the existence must be check.
	 * @return			<i>null</i> if the specified class doesn't exist, the former class name otherwise.
	 * 
	 * @see CacheMeta#getClass_names()
	 */
	public final static String getClassName(String className){
		String[] classes = cache.getClass_names();
		String name = null;

		for(int i=0; i<classes.length && name == null; i++)
			if (classes[i].equalsIgnoreCase(className))
				name = classes[i];

		return name;
	}

	public final static MetaClass getMetaClass(String tableName){
		String className = getClassName(tableName);

		if (className == null)
			return null;

		try {
			return cache.getClass(className);
		} catch (FatalException e) {
			System.out.println("WARNING: Impossible to get the meta information of the class \""+className+"\" !");
			return null;
		}
	}

	public final static String extractCollection(String tableName){
		try {
			Category.getCategory(tableName);
			return tableName.substring(0, tableName.lastIndexOf('_'));
		} catch (FatalException e) {
			return tableName;
		}
	}

	/**
	 * <p>Gets the former collection name if the specified collection exists.</p>
	 * <p><i><u>Note:</u><br />
	 * The function {@link CacheMeta#collectionExists(String)} checks also the existence of the given collection but
	 * take care of the case of the given collection name.
	 * However {@link SaadaDBConsistency#getCollectionName(String)} checks the existence of the given collection
	 * whatever the case of the given name and returns the collection name (with its "true" case) found in the cache.
	 * </i></p>
	 * 
	 * @param collName	The name of the collection whose the existence must be check.
	 * @return			<i>null</i> if the specified collection doesn't exist, the former collection name otherwise.
	 * 
	 * @see CacheMeta#getCollection_names()
	 */
	public final static String getCollectionName(String collName){
		String[] collections = cache.getCollection_names();
		String name = null;

		for(int i=0; i<collections.length && name == null; i++)
			if (collections[i].equalsIgnoreCase(collName))
				name = collections[i];

		return name;
	}

	public final static MetaCollection getMetaCollection(String tableName){
		String collectionName = getCollectionName(extractCollection(tableName));

		if (collectionName == null)
			return null;

		try {
			return cache.getCollection(collectionName);
		} catch (FatalException e) {
			System.out.println("WARNING: Impossible to get the meta information of the collection \""+collectionName+"\" !");
			return null;
		}
	}

	public final static MetaClass[] getMetaClassesOfCollection(String tableName){
		int category;
		try{
			category = Category.getCategory(tableName);
		}catch(FatalException fe){
			System.out.println("WARNING: Impossible to get the category of \""+tableName+"\" !");
			return new MetaClass[0];
		}
		String collName = getCollectionName(extractCollection(tableName));
		String[] classesName = cache.getClassesOfCollection(collName, category);
		MetaClass[] classesMeta = new MetaClass[classesName.length];
		for(int i=0; i<classesMeta.length; i++){
			try {
				classesMeta[i] = cache.getClass(classesName[i]);
			} catch (FatalException e) {
				System.out.println("WARNING: Impossible to get the meta of the class \""+classesName[i]+"\" !");
				return new MetaClass[0];
			}
		}
		return classesMeta;
	}

	@Override
	public void addColumns(String tableAlias) {
		if (tableAlias == null || tableAlias.trim().length() == 0)
			return;

		String tableName = getTableName(tableAlias);
		if (tableName == null || tableName.trim().length() == 0)
			return;

		try {
			// If it corresponds to a class:
			if (getClassName(tableName) != null){
				// add all the columns of the class:
				AttributeHandler[] ahs = cache.getClassAttributes(getClassName(tableName));
				for(int i=0; i<ahs.length; i++){
					addColumn(ahs[i].getNameattr(), tableAlias);
					columnsMeta.put(ahs[i].getNameattr(), ahs[i]);
					if (debug) System.out.println("### ADDED: "+ahs[i].getNameattr()+" [in "+tableAlias+"]");
				}

			}else{
				int ind = tableName.lastIndexOf('_');
				// If it corresponds to a collection_category:
				if (ind > 0){
					String collectionName = tableName.substring(0, ind);
					if (getCollectionName(collectionName) != null){
						int cat = Category.getCategory(tableName);
						// add all the columns of the category:
						Iterator<Map.Entry<String,AttributeHandler>> it = MetaCollection.getAttribute_handlers(cat).entrySet().iterator();
						while(it.hasNext()){
							Map.Entry<String,AttributeHandler> item = it.next();
							addColumn(item.getKey(), tableAlias);
							columnsMeta.put(item.getKey(), item.getValue());
							if (debug) System.out.println("### ADDED: "+item.getKey()+" [in "+tableAlias+"]");
						}
					}
				}
			}
		} catch (FatalException e) {/* The table doesn't exist. */;}
	}

	/* ************** */
	/* * SMART JOIN * */
	/* ************** */

	private static class JoinIndication {
		public String collectionAlias = "";
		public boolean classUsed = false;
		public boolean collectionUsed = false;
	}

	/**
	 * (PSEUDO MERGER of SAADA)<br />
	 * For each class found in the FROM list, this function adds its corresponding collection (if it's not already done)
	 * with the appropriate join on the field <i>oidsaada</i>.
	 * All references to columns of class or of collection are updated if necessary (that is to say if there is ambiguity since the last modifications).
	 * 
	 * @see SaadaDBConsistency#classifyImpliedColumns(Vector, Vector, HashMap, HashMap)
	 * @see SaadaDBConsistency#lookForJoin(Iterator, HashMap, HashMap, HashMap)
	 * @see SaadaDBConsistency#checkUnknownColumns(HashMap)
	 * @see SaadaDBConsistency#classifyMixedColumns(Vector, HashMap, HashMap, ADQLQuery)
	 * @see SaadaDBConsistency#updateFromAndJoin(HashMap, HashMap, ADQLQuery)
	 * 
	 * @see adqlParser.parser.DBConsistency#queryVerif(adqlParser.query.ADQLQuery)
	 */
	public boolean queryVerif(ADQLQuery q) throws ParseException {
		//		Vector<ADQLColumn> mixedCols = new Vector<ADQLColumn>();
		//		HashMap<String, Vector<ADQLColumn>> mapKnownCols = new HashMap<String, Vector<ADQLColumn>>();
		//		HashMap<ADQLColumn, Vector<String>> mapUnknownCols = new HashMap<ADQLColumn, Vector<String>>();
		//		HashMap<String, JoinIndication> joinIndications = new HashMap<String, JoinIndication>();
		//		
		//	// STEP 1: SORT IMPLIED COLUMNS BY CLASS:
		//		classifyImpliedColumns(q.getAll(ColumnSearchHandler.getInstance()), mixedCols, mapKnownCols, mapUnknownCols);
		//		
		//	// STEP 2: LOOK IF IT IS NEEDED TO MAKE A JOIN WITH COLLECTIONS:
		//		int nbTablesToUse = 0;
		//		if (mapUnknownCols.size() > 0)
		//			nbTablesToUse = lookForJoin(q.getTables(), mapKnownCols, mapUnknownCols, joinIndications);
		//		else
		//			nbTablesToUse = q.getNbTables();
		//		
		//	// STEP 3: CHECK THE UNKWOWN COLUMNS LIST:
		//		checkUnknownColumns(mapUnknownCols);
		//		
		//	// JUST A LITTLE DEBUGGING MESSAGE:
		//		if (debug){
		//			System.out.println("### KNOWN COLUMNS ###");
		//			Iterator<String> itKnown = mapKnownCols.keySet().iterator();
		//			while(itKnown.hasNext()){
		//				String item = itKnown.next();
		//				Vector<ADQLColumn> cols = mapKnownCols.get(item);
		//				System.out.println("*** Class \""+item+"\" ***");
		//				for(ADQLColumn col : cols)
		//					System.out.println("\t- "+col);
		//			}
		//			System.out.println("\n### UNKNOWN COLUMNS ###");
		//			Iterator<ADQLColumn> itUnknown = mapUnknownCols.keySet().iterator();
		//			while(itUnknown.hasNext()){
		//				ADQLColumn item = itUnknown.next();
		//				Vector<String> tables = mapUnknownCols.get(item);
		//				System.out.print("\t- "+item.getColumn()+" in (");
		//				for(String table : tables)
		//					System.out.print(table+" ");
		//				System.out.println();
		//			}
		//			System.out.println("\n### MIXTES COLUMNS ###");
		//			for(ADQLColumn col : mixedCols){
		//				System.out.println("\t- "+col);
		//			}
		//			System.out.println("\n### TABLES TO USE AND THEIR JOIN ###");
		//			Iterator<String> it2 = joinIndications.keySet().iterator();
		//			while(it2.hasNext()){
		//				String className = it2.next();
		//				JoinIndication indic = joinIndications.get(className);
		//				System.out.println("\t- "+className+" ["+indic.classUsed+"] <-> "+indic.collectionAlias+" ["+indic.collectionUsed+"]");
		//			}
		//			System.out.println("\n### NB TABLES TO USE = "+nbTablesToUse+" ###");
		//			System.out.println();
		//		}
		//		
		//	// STEP 4: SORT ALL MIXED COLUMNS:
		//		if (nbTablesToUse > 1)
		//			classifyMixedColumns(mixedCols, mapKnownCols, joinIndications, q);
		//		
		//	// STEP 5: UPDATE THE CLAUSE FROM AND MAKE REQUIRED JOINS:
		//		updateFromAndJoin(joinIndications, mapKnownCols, q);
		//		
		//		if (debug)
		//			System.out.println("### ADQL QUERY AFTER MERGING ###\n"+q+"\n################################\n");
		//		
		return true;
	}

	/**
	 * Classifies the given columns in three categories:
	 * <ul>
	 * 	<li>mixed (can be found either in class or collection)</li>
	 * 	<li>known (the column exists in one of the selected tables)</li>
	 * 	<li>unknown (all other columns; for instance, columns of the collection, if only the class table is in the FROM).</li>
	 * </ul>
	 * 
	 * @param impliedColumns	The list of columns to classify.
	 * @param mixedCols			The list of mixed columns. <b>UPDATED in this method.</b>
	 * @param mapKnownCols		The list of known columns. <b>UPDATED in this method.</b>
	 * @param mapUnknownCols	The list of unknown columns. <b>UPDATED in this method.</b>
	 * 
	 * @throws ParseException	If one column can be in more than one table.
	 * 
	 * @see SaadaDBConsistency#queryVerif(ADQLQuery)
	 */
	private void classifyImpliedColumns(Vector<ADQLObject> impliedColumns, Vector<ADQLColumn> mixedCols, HashMap<String, Vector<ADQLColumn>> mapKnownCols, HashMap<ADQLColumn, Vector<String>> mapUnknownCols) throws ParseException {
		// Sort them in function of their class:
		for(ADQLObject colItem : impliedColumns){
			ADQLColumn col = (ADQLColumn)colItem;
			// If this column can be found both in a class and a collection:
			boolean mixed = false;
			for(int i=0; !mixed && i<MIXED_COLUMNS.length; i++)
				mixed = col.getColumn().equalsIgnoreCase(MIXED_COLUMNS[i]);
			if (mixed)
				// add it to the mixed columns list:
				mixedCols.add(col);

			else {
				Vector<String> classesName = lstColumns().get(col.getColumn());
				// if it doesn't correspond to an existing class...
				if (classesName == null || classesName.size() <= 0){
					// ...add the current column to the UNKNOWN class:
					mapUnknownCols.put(col, new Vector<String>());
					// it it corresponds to more than one class...
				}else if (classesName.size() > 1){
					// ...add it to its class if a table prefix is given:
					if (col.getPrefix() != null){
						if (mapKnownCols.get(col.getPrefix()) == null)
							mapKnownCols.put(col.getPrefix(), new Vector<ADQLColumn>());
						mapKnownCols.get(col.getPrefix()).add(col);

						// ...else throw an error:
					}else{
						String[] tables = new String[classesName.size()];
						int i=0;
						for(String table : classesName)
							tables[i++] = table;
						throw new ParseException("Ambiguous column reference: \""+col+"\" may reference to either "+tables+" !");
					}
					// else...
				}else{
					// ...add the current column to its class:
					if (!mapKnownCols.containsKey(classesName.get(0)))
						mapKnownCols.put(classesName.get(0), new Vector<ADQLColumn>());
					mapKnownCols.get(classesName.get(0)).add(col);
				}
			}
		}
	}

	/**
	 * Determines, for each table selected in the clause FROM (joined tables included), whether it is used in the query and
	 * whether a join to its collection is needed.
	 * 
	 * @param itTables			Iterator on the list of tables selected in the clause FROM of a query.
	 * @param mapKnownCols		The list of all known columns. <b>UPDATED in this method.</b>
	 * @param mapUnknownCols	The list of all unknown columns.
	 * @param joinIndications	All join indications (tells whether a join between a class and collection is needed and whether they are used in the query). <b>UPDATED in this method.</b>
	 * 
	 * @return					The number of tables which must be REALLY used in the query.
	 * 
	 * @throws ParseException	NONE
	 * 
	 * @see SaadaDBConsistency#queryVerif(ADQLQuery)	
	 */
	private int lookForJoin(Iterator<ADQLTable> itTables, HashMap<String, Vector<ADQLColumn>> mapKnownCols, HashMap<ADQLColumn, Vector<String>> mapUnknownCols, HashMap<String, JoinIndication> joinIndications) throws ParseException {
		int nbTablesToUse = 0;
		while(itTables.hasNext()){
			// Gets the table and its alias:
			ADQLTable table = itTables.next();
			while(table != null){
				String tableAlias = (table.getAlias()==null)?table.getTable():table.getAlias();

				// Don't consider tables which reference sub-queries:
				if (table.isSubQuery()){
					table = (table.getJoin() == null)?null:table.getJoin().getJoinedTable();
					continue;
				}

				// If this table corresponds to a class of Saada:
				String className = getClassName(table.getTable());
				if (className != null){
					JoinIndication joinIndic = new JoinIndication();
					// are some columns associated with this class ?
					joinIndic.classUsed = mapKnownCols.containsKey(tableAlias);
					if (joinIndic.classUsed) nbTablesToUse++;

					try{
						MetaClass meta = cache.getClass(className);
						// get the name of its collection:
						int indCollection = 0;
						String collectionName = Database.getCachemeta().getCollectionTableName(meta.getCollection_name(), meta.getCategory()) +"_"+indCollection;
						// if the collection name can't be used as alias, change its name:
						while (mapKnownCols.containsKey(collectionName))
							collectionName = Database.getCachemeta().getCollectionTableName(meta.getCollection_name(), meta.getCategory()) +"_"+indCollection;
						joinIndic.collectionAlias = collectionName;
						// get all columns used by this category of data:
						Map<String, AttributeHandler> mapCollectionColumns = MetaCollection.getAttribute_handlers(meta.getCategory());

						// for each unknown column...
						Iterator<Map.Entry<ADQLColumn, Vector<String>>> itUnknown = mapUnknownCols.entrySet().iterator();
						while(itUnknown.hasNext()){
							Map.Entry<ADQLColumn, Vector<String>> item = itUnknown.next();
							ADQLColumn col = item.getKey();
							// ...if it has no prefix or if its prefix corresponds to the alias of this table....
							if (col.getPrefix() == null || col.getPrefix().equals(tableAlias)){
								// ...and is contained within the columns list of the collection...
								if (mapCollectionColumns.containsKey(col.getColumn())){
									// ...the collection must be used in the query:
									joinIndic.collectionUsed = true;
									// ...add the collection to the list of tables which use this unknown column:
									item.getValue().add(collectionName);
									// ...add the column in the collection into the known columns map:
									if (!mapKnownCols.containsKey(collectionName))
										mapKnownCols.put(collectionName, new Vector<ADQLColumn>());
									mapKnownCols.get(collectionName).add(col);
								}
							}
						}
						// update the joins map:
						joinIndications.put(tableAlias, joinIndic);
						if (joinIndic.collectionUsed)
							nbTablesToUse++;
					}catch(FatalException ex){
						System.err.println("ERROR: Impossible to get the MetaClass of \""+className+"\" !");
					}			
				}
				// do the same things for its joined table if any:
				table = (table.getJoin() == null)?null:table.getJoin().getJoinedTable();
			}
		}
		return nbTablesToUse;
	}

	/**
	 * Ensures that all columns of the unknown columns list are now known.
	 * 
	 * @param mapUnknownCols	The list of all unknown columns.
	 * 
	 * @throws ParseException	If a column has not been classify in a class or a collection or
	 * 							if a column is in more than one table and doesn't specify the table to use.
	 * 
	 * @see SaadaDBConsistency#queryVerif(ADQLQuery)
	 */
	private void checkUnknownColumns(HashMap<ADQLColumn, Vector<String>> mapUnknownCols) throws ParseException {
		// For each unknown columns ensure they are...
		Iterator<Map.Entry<ADQLColumn, Vector<String>>> itUnknown = mapUnknownCols.entrySet().iterator();
		while(itUnknown.hasNext()){
			Map.Entry<ADQLColumn, Vector<String>> item = itUnknown.next();
			// ...associated with at least one table, else throw an error:
			if (item.getValue().size() == 0)
				throw new ParseException("The column \""+item.getKey()+"\" doesn't exist in any selected table !");

			// ...associated with only one table or they have a prefix, else throw an error:
			else if (item.getValue().size() != 1 && item.getKey().getPrefix() == null){
				String[] tables = new String[item.getValue().size()];
				int i=0;
				for(String table : item.getValue())
					tables[i++] = table;
				throw new ParseException("Ambiguous column reference: "+item.getKey()+" may reference to either "+tables+" !");
			}
		}
	}

	/**
	 * In function of the joins to make, this method classify all the mixed columns.
	 * 
	 * @param mixedCols			The mixed columns to classify.
	 * @param mapKnownCols		The list of all known columns.
	 * @param joinIndications	All join indications.
	 * @param q					The ADQL query.
	 * 
	 * @throws ParseException	If a column can't be classify in a class or a collection or
	 * 							if a column is in more than one table and doesn't specify the table to use.
	 * 
	 * @see SaadaDBConsistency#queryVerif(ADQLQuery)
	 */
	private void classifyMixedColumns(Vector<ADQLColumn> mixedCols, HashMap<String, Vector<ADQLColumn>> mapKnownCols, HashMap<String, JoinIndication> joinIndications, ADQLQuery q) throws ParseException {
		for(ADQLColumn col : mixedCols){
			String tableName = "";
			// If this column has no prefix...
			if (col.getPrefix() == null || col.getPrefix().trim().length() == 0){
				// ...throw an error if there is more than one selected tables:
				if (q.getNbTables() > 1)
					throw new ParseException("Ambiguous column reference: \""+col+"\" !");
				// ...or get the alias of the only selected table:
				else{
					ADQLTable table = q.getTables().next();
					if (table != null)
						tableName = lstTablesAlias().get(table.getTable());
				}
				// else get directly the prefix of the table:
			}else
				tableName = col.getPrefix();

			// Change the prefix of the current mixed column...
			JoinIndication joinIndic = joinIndications.get(tableName);
			if (joinIndic != null){
				// ...if the collection is used...
				if (joinIndic.collectionUsed){
					/* ...into the class alias if the class won't be used in the query
					(the table name will change but not its alias) or into the collection alias otherwise: */
					col.setPrefix(joinIndic.classUsed?joinIndic.collectionAlias:tableName);
					// the mixed column must be added into the columns list of the collection
					tableName = joinIndic.collectionAlias;
				}else
					// ...into the class alias if the collection is not used:
					col.setPrefix(tableName);
			}

			// Add the current mixed column in the corresponding table (class or collection, it depends of the join indication):
			Vector<ADQLColumn> vTemp = mapKnownCols.get(tableName);
			if (vTemp == null){
				vTemp = new Vector<ADQLColumn>();
				mapKnownCols.put(tableName, vTemp);
			}
			vTemp.add(col);
		}
	}

	/**
	 * Update the query in a function of each join indication:
	 * <ul>
	 * 	<li><u>Class is used but not collection:</u> nothing to do !</li>
	 * 	<li><u>Class and collection are used:</u> <ul>
	 * 			<li>1. add the table corresponding to the collection into the clause FROM.</li>
	 * 			<li>2. add the join condition between the class and the collection.</li>
	 * 			<li>3. update all columns classified into the class and those of the collection.</li></ul></li>
	 * 	<li><u>Collection is used but not class:</u> change the name of the table (which was the name of the class) into the name of the collection (the table alias is not change).</li>
	 * 	<li><u>Neither the class or the collection are used:</u> nothing done, but a warning message could be printed and/or the table could be removed from the query.</li>
	 * </ul>
	 * 
	 * @param joinIndications	All join indications.
	 * @param mapKnownCols		The list of known columns.
	 * @param q					The query to update.
	 * 
	 * @throws ParseException	NONE
	 * 
	 * @see SaadaDBConsistency#queryVerif(ADQLQuery)
	 */
	private void updateFromAndJoin(HashMap<String, JoinIndication> joinIndications, HashMap<String, Vector<ADQLColumn>> mapKnownCols, ADQLQuery q) throws ParseException {
		Iterator<Map.Entry<String, JoinIndication>> itJoins = joinIndications.entrySet().iterator();
		while(itJoins.hasNext()){
			Map.Entry<String, JoinIndication> item = itJoins.next();
			String className = item.getKey();
			JoinIndication indic = item.getValue();

			// CASE: class and collection used:
			if (indic.classUsed && indic.collectionUsed){
				// ...add the collection into the clause FROM:
				ADQLTable collection = new ADQLTable(indic.collectionAlias.substring(0, indic.collectionAlias.lastIndexOf('_')));
				collection.setAlias(indic.collectionAlias);
				q.addTable(collection);
				// ...make the join with its class:
				q.addConstraint(new ADQLComparison(new ADQLColumn("oidsaada", indic.collectionAlias), ComparisonOperator.EQUAL, new ADQLColumn("oidsaada", className)));
				// ...update the table reference of the columns of the collection:
				Vector<ADQLColumn> collectionColumns = mapKnownCols.get(indic.collectionAlias);
				for(ADQLColumn col : collectionColumns)
					col.setPrefix(indic.collectionAlias);

				// CASE: collection used but not class:
			}else if (!indic.classUsed && indic.collectionUsed){
				// ...look for the corresponding table in the clause FROM:
				Iterator<ADQLTable> itTables = q.getTables();
				while(itTables.hasNext()){
					ADQLTable table = itTables.next();
					while(table != null){
						String tableAlias = (table.getAlias()==null)?table.getTable():table.getAlias();
						// ...once found, change its name for the collection name and stop searching:
						if (tableAlias.equals(className)){
							table.setTable(indic.collectionAlias.substring(0, indic.collectionAlias.lastIndexOf('_')));
							break;
						}
						table = (table.getJoin() != null)?table.getJoin().getJoinedTable():null;
					}
				}

				// CASE: neither class or collection are used (CASE THEORATICALLY IMPOSSIBLE AT THIS PARSING STEP):
			}else if (!indic.classUsed && !indic.collectionUsed){
				System.out.println("WARNING: the table \""+className+"\" is never used !");
				//				q.removeTable(className);
			}
		}
	}

}
