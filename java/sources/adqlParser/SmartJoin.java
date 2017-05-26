package adqlParser;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import saadadb.cache.CacheMeta;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;
import adqlParser.parser.ParseException;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLComparison;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;
import adqlParser.query.ColumnSearchHandler;
import adqlParser.query.ComparisonOperator;

public class SmartJoin {

	/** List of columns which can be found either in class or collection of a SaadaDB. */
	public final static String[] MIXED_COLUMNS = new String[]{"oidsaada", "obs_id"};

	protected CacheMeta cache;

	protected HashMap<String, Vector<String>> lstColumns = new HashMap<String, Vector<String>>();

	protected HashMap<String,String> lstTablesAlias = new HashMap<String,String>();

	protected boolean debug = false;

	public SmartJoin(){
		cache = Database.getCachemeta();
	}

	/**
	 * @return The debug.
	 */
	public final boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug The debug to set.
	 */
	public final void setDebug(boolean debug) {
		this.debug = debug;
	}


	/* ****************** */
	/* * UCD MANAGEMENT * */
	/* ****************** */
	protected HashMap<String,SaadaADQLTable> getTableAliases(SaadaADQLQuery query){
		HashMap<String, SaadaADQLTable> aliases = new HashMap<String, SaadaADQLTable>();
		Iterator<ADQLTable> itTables = query.getTables();

		while(itTables.hasNext()){
			SaadaADQLTable item = (SaadaADQLTable)itTables.next();
			String alias = item.getAlias();
			if (alias == null || alias.trim().length()==0){
				alias = item.getTable();
				item.setAlias(alias);
			}
			aliases.put(alias.toLowerCase(), item); 
		}

		return aliases;
	}

	protected String getTableAlias(SaadaADQLQuery query, String tableName){
		String alias = null;

		Iterator<ADQLTable> itTables = query.getTables();
		while(alias == null && itTables.hasNext()){
			ADQLTable table = itTables.next();
			if (table.getTable().equalsIgnoreCase(tableName))
				alias = table.getAlias();
		}

		return alias;
	}

	protected static class UCDTransformation {
		public final AttributeHandler columnMeta;
		public final String columnName;
		public final String joinTable;
		public final boolean joinRequired;

		public UCDTransformation(AttributeHandler meta){
			columnMeta = meta;
			columnName= columnMeta.getNameattr();
			joinTable = null;
			joinRequired = false;
		}

		public UCDTransformation(AttributeHandler meta, String table){
			columnMeta = meta;
			columnName = columnMeta.getNameattr();
			joinTable = table;
			joinRequired = true;
		}
	}

	public Vector<SaadaADQLQuery> readUcdsAndTransformQuery(SaadaADQLQuery query) throws ParseException {
		Vector<SaadaADQLQuery> unionItems = new Vector<SaadaADQLQuery>();

		// Get tables alias:
		HashMap<String, SaadaADQLTable> lstTablesAlias = getTableAliases(query);

		// Get the first UCD function:
		ADQLObject ucdFunction = query.getFirst(UCDSearchHandler.getInstance());
		if (debug) System.out.println("### FOUND UCD: "+ucdFunction);
		String ucdName = null, tableName = null;

		// Return without modification if there is no UCD function:
		if (ucdFunction == null)
			return unionItems;
		else{
			UCDFunction ucd = (UCDFunction)ucdFunction;
			ucdName = ucd.getUCD();
			tableName = ucd.getTable();
		}

		// Get the list of all implied tables:
		Vector<UCDTransformation> vTransformations = new Vector<UCDTransformation>();
		SaadaADQLTable table = lstTablesAlias.get(tableName.toLowerCase());
		if (table == null)
			throw new ParseException("The table \""+tableName+"\" is not selected in this query !");
		if (table.isSubQuery())
			throw new ParseException("The table \""+tableName+"\" is a sub-query: the UCD function first parameter must be the name of a table of the database !");

		// CASE: class
		if (table.isClass()){
			AttributeHandler ucdField = table.getMetaCollection().getUCDField(ucdName, true);
			if (ucdField == null){
				ucdField = table.getMetaClass().getUCDField(ucdName, true);
				if (ucdField == null)
					throw new ParseException("The UCD \""+ucdName+"\" doesn't exist in the table \""+tableName+"\" (which corresponds to the class \""+table.getMetaClass().getName()+"\" of the collection \""+table.getMetaCollection().getName()+"\") !");
			}
			vTransformations.add(new UCDTransformation(ucdField));
			if (debug) System.out.println("### NEW TRANSFO: "+ucdName+" AS "+ucdField.getNameattr()+" IN "+tableName);

			// CASE: collection
		}else{
			AttributeHandler ucdField = table.getMetaCollection().getUCDField(ucdName, true);
			if (ucdField == null){
				int nbTransfo = 0;
				MetaClass[] metaClasses = SaadaDBConsistency.getMetaClassesOfCollection(table.getTable());
				for(int i=0; i<metaClasses.length; i++){
					ucdField = metaClasses[i].getUCDField(ucdName, true);
					if (ucdField != null){
						vTransformations.add(new UCDTransformation(ucdField, metaClasses[i].getName()));
						if (debug) System.out.println("### NEW TRANSFO: "+ucdName+" AS "+ucdField.getNameattr()+" IN "+metaClasses[i].getName());
						nbTransfo++;
					}
				}
				if (nbTransfo == 0)
					throw new ParseException("The UCD \""+ucdName+"\" doesn't exist in the table \""+tableName+"\" (which corresponds to the collection \""+table.getMetaCollection().getName()+"\") !");
			}else{
				vTransformations.add(new UCDTransformation(ucdField));
				if (debug) System.out.println("### NEW TRANSFO: "+ucdName+" AS "+ucdField.getNameattr()+" IN "+tableName);
			}
		}

		// For each implied table, replace the UCD function by the appropriate column reference and read the other UCD functions:
		for(UCDTransformation ucdTransfo : vTransformations){		
			// Copy the given query:
			SaadaADQLQuery copy = (SaadaADQLQuery)query.getCopy();
			copy.clearOrderBy();
			copy.setNoLimit();
			// Add a join if required:
			String joinedTableAlias = null;
			if (ucdTransfo.joinRequired){
				joinedTableAlias = getTableAlias(query, ucdTransfo.joinTable);
				if (joinedTableAlias == null){
					// Build the joined table alias:
					joinedTableAlias = ucdTransfo.joinTable; int i = 0;
					while(lstTablesAlias.containsKey(joinedTableAlias+"_"+i))
						i++;
					joinedTableAlias += "_"+i;
					// Create the new table:
					SaadaADQLTable joinedTable = new SaadaADQLTable(ucdTransfo.joinTable);
					joinedTable.setAlias(joinedTableAlias);
					// Add this new table:
					copy.addTable(joinedTable);
				}
				// Make the join:
				copy.addConstraint(new ADQLComparison(new ADQLColumnAndMeta("oidsaada", tableName), ComparisonOperator.EQUAL, new ADQLColumnAndMeta("oidsaada", joinedTableAlias)));
			}
			// Replace all UCDs by the appropriate column reference:
			copy.getAll(new UCDReplacer(ucdName, tableName, (ucdTransfo.joinRequired)?joinedTableAlias:tableName, ucdTransfo.columnName, ucdTransfo.columnMeta));
			// Read the other UCDs in this new query:
			Vector<SaadaADQLQuery> result = readUcdsAndTransformQuery(copy);
			if (result.isEmpty())
				unionItems.add(copy);
			else
				unionItems.addAll(result);
		}

		return unionItems;
	}

	/* ************** */
	/* * SMART JOIN * */
	/* ************** */

	private static class JoinIndication {
		public boolean classUsed = false;
		public boolean collectionUsed = false;
		public boolean addCollection = false;
		public String collectionName = "";
		public String collectionAlias = "";

		public void setCollection(String collAlias){
			addCollection = false;
			collectionAlias = collAlias;
		}

		public void setCollection(String collName, String collAlias){
			addCollection = true;
			collectionName = collName;
			collectionAlias = collAlias;
		}
	}

	protected void extractColumnsAndTableAlias(SaadaADQLQuery q){
		lstColumns.clear();
		lstTablesAlias.clear();

		Iterator<ADQLTable> itTables = q.getTables();
		while(itTables.hasNext()){
			SaadaADQLTable table = (SaadaADQLTable)itTables.next();
			String tableAlias = (table.getAlias()==null)?table.getTable():table.getAlias();
			lstTablesAlias.put(tableAlias, table.getTable());
			try {
				/*
				 * If classe is a VO model, we just publish the SQL table in the "ivoa" schema
				 */
				VOResource vor;
				ResultSet rs;
				DatabaseConnection connection = Database.getConnection();
				if( (vor = VOResource.getResource(table.getTable())) != null) {
					Messenger.printMsg(Messenger.TRACE, table.getTable() + " is a VO model");
					ArrayList<UTypeHandler> uths = vor.getUTypeHandlers();
					for( UTypeHandler uth: uths) {
						AttributeHandler ah = uth.getAttributeHandler();
						ah.setNameattr(ah.getNameorg());
						Vector<String> vTables = lstColumns.get(ah.getNameattr());
						if (vTables == null){
							vTables = new Vector<String>();
							lstColumns.put(ah.getNameattr(), vTables);
						}
						vTables.add(tableAlias);
						if (debug) System.out.println("### ADDED: "+ah.getNameattr()+" [in "+table.getAlias()+"]");

					}
					/*
					 * Look at a SQL table
					 */
				} else if( (rs = Database.getWrapper().getTableColumns(connection, table.getTable())) != null ) {
					while( rs.next()) {
						AttributeHandler ah = new AttributeHandler() ;
						ah.setNameattr(rs.getString("COLUMN_NAME"));
						ah.setNameorg(rs.getString("COLUMN_NAME"));
						ah.setType(rs.getString("TYPE_NAME"));
						Vector<String> vTables = lstColumns.get(ah.getNameattr());
						if (vTables == null){
							vTables = new Vector<String>();
							lstColumns.put(ah.getNameattr(), vTables);
						}
						vTables.add(tableAlias);
						if (debug) System.out.println("### ADDED: "+ah.getNameattr()+" [in "+table.getAlias()+"]");
					}
				}
				/*
				 * Access to a Saada resource
				 */
				else {
					// If it corresponds to a class:
					if (SaadaDBConsistency.getClassName(table.getTable()) != null){
						// add all the columns of the class:
						AttributeHandler[] ahs = cache.getClassAttributes(SaadaDBConsistency.getClassName(table.getTable()));
						for(int i=0; i<ahs.length; i++){
							Vector<String> vTables = lstColumns.get(ahs[i].getNameattr());
							if (vTables == null){
								vTables = new Vector<String>();
								lstColumns.put(ahs[i].getNameattr(), vTables);
							}
							vTables.add(tableAlias);
							if (debug) System.out.println("### ADDED: "+ahs[i].getNameattr()+" [in "+table.getAlias()+"]");
						}

					}else{
						int ind = table.getTable().lastIndexOf('_');
						// If it corresponds to a collection_category:
						if (ind > 0){
							String collectionName = table.getTable().substring(0, ind);
							if (SaadaDBConsistency.getCollectionName(collectionName) != null){
								int cat = Category.getCategory(table.getTable());
								// add all the columns of the category:
								Iterator<Map.Entry<String,AttributeHandler>> it = MetaCollection.getAttribute_handlers(cat).entrySet().iterator();
								while(it.hasNext()){
									Map.Entry<String,AttributeHandler> item = it.next();
									Vector<String> vTables = lstColumns.get(item.getKey());
									if (vTables == null){
										vTables = new Vector<String>();
										lstColumns.put(item.getKey(), vTables);
									}
									vTables.add(tableAlias);
									if (debug) System.out.println("### ADDED: "+item.getKey()+" [in "+tableAlias+"]");
								}
							}
						}
					}
					Database.giveConnection(connection);
				}
			} catch (Exception e) {/* The table doesn't exist. */;}
		}
	}

	/**
	 * (PSEUDO MERGER of SAADA)<br />
	 * For each class found in the FROM list, this function adds its corresponding collection (if it's not already done)
	 * with the appropriate join on the field <i>oidsaada</i>.
	 * All references to columns of class or of collection are updated if necessary (that is to say if there is ambiguity since the last modifications).
	 * 
	 * @return	The transformed ADQL query. (<i><u>note:</u> it is a copy of the given query !</i>)
	 * 
	 * @see SaadaDBConsistency#classifyImpliedColumns(Vector, Vector, HashMap, HashMap)
	 * @see SaadaDBConsistency#lookForJoin(Iterator, HashMap, HashMap, HashMap)
	 * @see SaadaDBConsistency#checkUnknownColumns(HashMap)
	 * @see SaadaDBConsistency#classifyMixedColumns(Vector, HashMap, HashMap, ADQLQuery)
	 * @see SaadaDBConsistency#updateFromAndJoin(HashMap, HashMap, ADQLQuery)
	 * 
	 * @see adqlParser.parser.DBConsistency#queryVerif(adqlParser.query.ADQLQuery)
	 */
	public SaadaADQLQuery applySmartJoin(SaadaADQLQuery query) throws ParseException {
		Vector<ADQLColumn> mixedCols = new Vector<ADQLColumn>();
		HashMap<String, Vector<ADQLColumn>> mapKnownCols = new HashMap<String, Vector<ADQLColumn>>();
		HashMap<ADQLColumn, Vector<String>> mapUnknownCols = new HashMap<ADQLColumn, Vector<String>>();
		HashMap<String, JoinIndication> joinIndications = new HashMap<String, JoinIndication>();

		// STEP 0: INITIALIZE COLUMNS LIST AND TABLES ALIAS LIST:
		extractColumnsAndTableAlias(query);

		// STEP 1: SORT IMPLIED COLUMNS BY CLASS:
		classifyImpliedColumns(query.getAll(ColumnSearchHandler.getInstance()), mixedCols, mapKnownCols, mapUnknownCols);

		// STEP 2: LOOK IF IT IS NEEDED TO MAKE A JOIN WITH COLLECTIONS:
		int nbTablesToUse = 0;
		//		if (mapUnknownCols.size() > 0)
		nbTablesToUse = lookForJoin(query, mapKnownCols, mapUnknownCols, joinIndications);
		//		else
		//			nbTablesToUse = query.getNbTables();

		// STEP 3: CHECK THE UNKWOWN COLUMNS LIST:
		checkUnknownColumns(mapUnknownCols);

		// JUST A LITTLE DEBUGGING MESSAGE:
		if (debug){
			System.out.println("### KNOWN COLUMNS ###");
			Iterator<String> itKnown = mapKnownCols.keySet().iterator();
			while(itKnown.hasNext()){
				String item = itKnown.next();
				Vector<ADQLColumn> cols = mapKnownCols.get(item);
				System.out.println("*** Class \""+item+"\" ***");
				for(ADQLColumn col : cols)
					System.out.println("\t- "+col);
			}
			System.out.println("\n### UNKNOWN COLUMNS ###");
			Iterator<ADQLColumn> itUnknown = mapUnknownCols.keySet().iterator();
			while(itUnknown.hasNext()){
				ADQLColumn item = itUnknown.next();
				Vector<String> tables = mapUnknownCols.get(item);
				System.out.print("\t- "+item.getColumn()+" in (");
				for(String table : tables)
					System.out.print(table+" ");
				System.out.println();
			}
			System.out.println("\n### MIXTES COLUMNS ###");
			for(ADQLColumn col : mixedCols){
				System.out.println("\t- "+col);
			}
			System.out.println("\n### TABLES TO USE AND THEIR JOIN ###");
			Iterator<String> it2 = joinIndications.keySet().iterator();
			while(it2.hasNext()){
				String className = it2.next();
				JoinIndication indic = joinIndications.get(className);
				System.out.println("\t- "+className+" ["+indic.classUsed+"] <-> "+indic.collectionAlias+" ["+indic.collectionUsed+"]");
			}
			System.out.println("\n### NB TABLES TO USE = "+nbTablesToUse+" ###");
			System.out.println();
		}

		// STEP 4: SORT ALL MIXED COLUMNS:
		if (nbTablesToUse > 1)
			classifyMixedColumns(mixedCols, mapKnownCols, joinIndications, query);

		// STEP 5: UPDATE THE CLAUSE FROM AND MAKE REQUIRED JOINS:
		updateFromAndJoin(joinIndications, mapKnownCols, query);

		if (debug)
			System.out.println("### ADQL QUERY AFTER MERGING ###\n"+query+"\n################################\n");

		return query;
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
				Vector<String> classesName = lstColumns.get(col.getColumn());
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
	 * @param query				The query on which the smart join is applied.
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
	private int lookForJoin(SaadaADQLQuery query, HashMap<String, Vector<ADQLColumn>> mapKnownCols, HashMap<ADQLColumn, Vector<String>> mapUnknownCols, HashMap<String, JoinIndication> joinIndications) throws ParseException {
		int nbTablesToUse = 0;
		Iterator<ADQLTable> itTables = query.getTables();
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
				String className = SaadaDBConsistency.getClassName(table.getTable());
				if (className != null){
					JoinIndication joinIndic = new JoinIndication();
					// are some columns associated with this class ?
					joinIndic.classUsed = mapKnownCols.containsKey(tableAlias);
					if (joinIndic.classUsed) nbTablesToUse++;

					try{
						MetaClass meta = cache.getClass(className);
						// get the name of its collection:
						int indCollection = 0;
						String collectionName = ((SaadaADQLTable)table).getCollectionTable();
						String collectionAlias = getTableAlias(query, collectionName);
						if (collectionAlias == null){
							// if the collection name can't be used as alias, change its name:
							while (mapKnownCols.containsKey(collectionName+"_"+indCollection))
								indCollection++;
							collectionAlias = collectionName+"_"+indCollection;
							joinIndic.setCollection(collectionName, collectionAlias);
						}else
							joinIndic.setCollection(collectionAlias);
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
									item.getValue().add(collectionAlias);
									// ...add the column in the collection into the known columns map:
									if (!mapKnownCols.containsKey(collectionAlias))
										mapKnownCols.put(collectionAlias, new Vector<ADQLColumn>());
									mapKnownCols.get(collectionAlias).add(col);
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
		System.out.println("### NbMixedCols = "+mixedCols.size());
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
						tableName = ((table.getAlias()==null)?table.getTable():table.getAlias());
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

			System.out.println("### MIXED COLUMN \""+col+"\" ADDED IN \""+tableName+"\"");
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
				if (indic.addCollection){
					SaadaADQLTable collection = new SaadaADQLTable(indic.collectionName);
					collection.setAlias(indic.collectionAlias);
					q.addTable(collection);
				}
				// ...make the join with its class:
				q.addConstraint(new ADQLComparison(new ADQLColumnAndMeta("oidsaada", indic.collectionAlias), ComparisonOperator.EQUAL, new ADQLColumnAndMeta("oidsaada", className)));
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
				//System.out.println("WARNING: the table \""+className+"\" is never used !");
				//q.removeTable(className);
			}
		}
	}
}
