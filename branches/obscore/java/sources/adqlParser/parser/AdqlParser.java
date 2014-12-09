package adqlParser.parser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import saadadb.database.Database;

import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLComparison;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLJoin;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLOrder;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;
import adqlParser.query.ADQLType;
import adqlParser.query.ColumnReference;
import adqlParser.query.ComparisonOperator;
import adqlParser.query.JoinType;
import adqlParser.query.OperationType;
import adqlParser.query.function.ADQLFunction;
import adqlParser.query.function.InFunction;
import adqlParser.query.function.MathFunction;
import adqlParser.query.function.MathFunctionType;
import adqlParser.query.function.SQLFunction;
import adqlParser.query.function.SQLFunctionType;
import adqlParser.query.function.UserFunction;
import adqlParser.query.function.geometry.GeometryFunction;
import adqlParser.query.function.geometry.PointFunction;

/**
 * <p>Parses an ADQL query thanks to the {@link AdqlParser#Query()} function. </p>
 * 
 * <p>This parser is able, thanks to a {@link DBConsistency} object, to check the consistency between the ADQL query to parse and the "database" on which the query must be executed.
 * However the default {@link DBConsistency} object is an instance of {@link DefaultDBConsistency} which does no verification. Thus you must
 * extend {@link DBConsistency} to ensure the consistency with the "database" on which the query must be executed.</p>
 * 
 * <p>To create an object representation of the given ADQL query, this parser uses a {@link QueryBuilderTools} object. So if you want customize some object (ie. CONTAINS) of this representation
 * you just have to extend the corresponding default object (ie. ContainsFunction) and to extend the corresponding function of {@link QueryBuilderTools} (ie. createContains(...)).</p>
 * 
 * <p><b><u>WARNING:</u> To modify this class it's strongly encouraged to modify the .jj file in the section between <i>PARSER_BEGIN</i> and <i>PARSER_END</i> and to re-compile it with JavaCC.</b></p>
 *
 * @see DBConsistency
 * @see QueryBuilderTools
 *
 * @author Gregory Mantelet (CDS) - gregory.mantelet@astro.unistra.fr
 * @version June 2010
 */
public class AdqlParser implements AdqlParserConstants {

	/** Tools to build the object representation of the ADQL query. */
	private QueryBuilderTools buildTools = new QueryBuilderTools();

	/** Used to ensure the consistency between the ADQL query and the database. */
	private DBConsistency dbCheck = new DefaultDBConsistency();

	/** The object representation of the ADQL query to parse. (ONLY USED DURING THE PARSING, else it is always <i>null</i>). */
	private ADQLQuery query = null;

	/** Indicates whether some debugging messages must be printed or not. */
	private boolean debug = false;

	/**
	 * Builds an ADQL parser without a query to parse.
	 */
	public AdqlParser(){
		this(new java.io.ByteArrayInputStream("".getBytes()));
	}

	/**
	 * Builds a parser with a stream containing the query to parse.
	 *
	 * @param stream	The stream in which the ADQL query to parse is given.
	 * @param dbcons	The object to use to ensure the consistency with the database.
	 * @param tools	The object to use to build an object representation of the given ADQL query.
	 */
	public AdqlParser(java.io.InputStream stream, DBConsistency dbcons, QueryBuilderTools tools) {
		this(stream);
		dbCheck = dbcons;
		buildTools = tools;
		setDebug(false);
	}

	/**
	 * Builds a parser with a stream containing the query to parse.
	 *
	 * @param stream		The stream in which the ADQL query to parse is given.
	 * @param encoding	The supplied encoding.
	 * @param dbcons		The object to use to ensure the consistency with the database.
	 * @param tools		The object to use to build an object representation of the given ADQL query.
	 */
	public AdqlParser(java.io.InputStream stream, String encoding, DBConsistency dbcons, QueryBuilderTools tools) {
		this(stream, encoding);
		dbCheck = dbcons;
		buildTools = tools;
		setDebug(false);
	}

	/**
	 * Builds a parser with a reader containing the query to parse.
	 *
	 * @param reader		The reader in which the ADQL query to parse is given.
	 * @param dbcons		The object to use to ensure the consistency with the database.
	 * @param tools		The object to use to build an object representation of the given ADQL query.
	 */
	public AdqlParser(java.io.Reader reader, DBConsistency dbcons, QueryBuilderTools tools) {
		this(reader);
		dbCheck = dbcons;
		buildTools = tools;
		setDebug(false);
	}

	/**
	 * Builds a parser with another token manager.
	 *
	 * @param tm			The manager which associates a token to a numeric code.
	 * @param dbcons		The object to use to ensure the consistency with the database.
	 * @param tools		The object to use to build an object representation of the given ADQL query.
	 */
	public AdqlParser(AdqlParserTokenManager tm, DBConsistency dbcons, QueryBuilderTools tools) {
		this(tm);
		dbCheck = dbcons;
		buildTools = tools;
		setDebug(false);
	}

	public final boolean isDebugging(){
		return debug;
	}

	public final void setDebug(boolean on){
		debug = on;
		dbCheck.setDebug(debug);
		if (on) enable_tracing();
		else disable_tracing();
	}

	/**
	 * Parses the query given at the creation of this parser or in the <i>ReInit</i> functions.
	 *
	 * @return 					The object representation of the given ADQL query.
	 * @throws ParseException	If there is at least one syntactic error.
	 *
	 * @see AdqlParser#Query()
	 */
	public final ADQLQuery parseQuery() throws ParseException {
		return Query();
	}

	/**
	 * Parses the query given in parameter.
	 *
	 * @param q					The ADQL query to parse.
	 * @return 					The object representation of the given ADQL query.
	 * @throws ParseException	If there is at least one syntactic error.
	 *
	 * @see AdqlParser#ReInit(java.io.InputStream)
	 * @see AdqlParser#setDebug(boolean)
	 * @see AdqlParser#Query()
	 */
	public final ADQLQuery parseQuery(String q) throws ParseException {
		boolean debugging = debug;

		ReInit(new java.io.ByteArrayInputStream(q.getBytes()));
		setDebug(debugging);

		return Query();
	}

	/**
	 * Parses the query contained in the stream given in parameter.
	 *
	 * @param stream				The stream which contains the ADQL query to parse.
	 * @return 					The object representation of the given ADQL query.
	 * @throws ParseException	If there is at least one syntactic error.
	 *
	 * @see AdqlParser#ReInit(java.io.InputStream)
	 * @see AdqlParser#setDebug(boolean)
	 * @see AdqlParser#Query()
	 */
	public final ADQLQuery parseQuery(java.io.InputStream stream) throws ParseException {
		boolean debugging = debug;

		ReInit(stream);
		setDebug(debugging);

		return Query();
	}

	/**
	 * Creates the object representation of a column reference: {@link ADQLColumn}.
	 * Before the creation this method checks whether the column exists in the specified table (if any) or in all selected table.
	 *
	 * @param colName			The name of the column to transform in ADQLColumn. <i>Note: if there is a table precision, it must be put as prefix of the columnName.</i>
	 * @return 					The corresponding object representation.
	 * @throws ParseException	If the given column name doesn't correspond to an existing column or if the table prefix doesn't correspond to a selected table.
	 *
	 * @see DBConsistency#getNbAliasedTables()
	 * @see DBConsistency#columnExists(String, String)
	 *
	 * @see QueryBuilderTools#createColumn(String, String, String)
	 */
	private ADQLColumn createColumn(String colName) throws ParseException {
		ADQLColumn column = null;
		int ind = colName.lastIndexOf('.');

		// If there is a table precision...
		if (ind > 0){
			// extract the table name/alias:
			String tableRef = colName.substring(0, ind);
			colName = colName.substring(ind+1);
			// check whether the column exists or not (ONLY IF the clause FROM has already been evaluated):
			if (dbCheck.getNbAliasedTables() > 0 && !dbCheck.columnExists(colName, tableRef))
				throw new ParseException("The column \u005c""+colName+"\u005c" doesn't exist in the selected table \u005c""+tableRef+"\u005c" !");
			// create the corresponding object representation of this column:
			column = buildTools.createColumn(colName, tableRef, null);

			// Else...
		}else{
			// check whether the column exists or not (ONLY IF the clause FROM has already been evaluated):
			if (dbCheck.getNbAliasedTables() > 0 && !dbCheck.columnExists(colName))
				throw new ParseException("The column \u005c""+colName+"\u005c" doesn't exist in any selected table !");
			// create the corresponding object representation of this column:
			column = buildTools.createColumn(colName, null, null);
		}
		return column;
	}

	/**
	 * <p>Create the object representation of a table reference in function of all given parameters.</p>
	 *
	 * <p>If there is no subquery, the table name must corresponds to an existing table of the "database".</p>
	 *
	 * @param table				The table name or <i>null</i> if the table is a subquery.
	 * @param alias				The alias of the table (!= <i>null</i> if the table is a subquery).
	 * @param q					The subquery or <i>null</i> if the table is only a reference to an existing table.
	 * @return					The object representation of the table described by the given parameters.
	 * @throws ParseException	If the table name doesn't correspond to an existing table or if there is no alias for the subquery.
	 *
	 * @see DBConsistency#tableExists(String)
	 * @see DBConsistency#addTableAlias(String, String)
	 * @see DBConsistency#addColumns(String)
	 * @see DBConsistency#addColumn(String, String)
	 *
	 * @see QueryBuilderTools#createTable(String, String)
	 * @see QueryBuilderTools#createTable(ADQLQuery, String)
	 */
	private final ADQLTable createTable(String table, String alias, ADQLQuery q) throws ParseException {
		ADQLTable t = null;

		// TABLE REFERENCE case:
		if (q == null){
			// Check whether the table exists:
			if (dbCheck.tableExists(table)){
				// create its object representation:
				t = buildTools.createTable(table, alias);

				// add an alias mapping for this table:
				alias = dbCheck.addTableAlias(alias, table);
				// list all columns of this table:
				dbCheck.addColumns(alias);
			}else
				throw new ParseException("The table \u005c""+table+"\u005c" doesn't exist !");

			// SUBQUERY case:
		}else{
			if (alias == null || alias.trim().length() == 0)
				throw new ParseException("A subquery item of the FROM clause must have an alias !");

			// create its object representation:
			t = buildTools.createTable(q, alias);

			// add an alias mapping for this table:
			alias = dbCheck.addTableAlias(alias, t.toString());
			// list all columns of this table:
			Iterator<ADQLOperand> it = q.getColumns();
			while(it.hasNext()){
				ADQLOperand op = it.next();
				if (op.getAlias() != null)
					dbCheck.addColumn(op.getAlias(), alias);
				else if (op instanceof ADQLColumn)
					dbCheck.addColumn(((ADQLColumn)op).getColumn(), alias);
			}
		}

		return t;
	}

	/**
	 * Checks whether the non-null parameter is correct and creates the corresponding object representation: {@link ColumnReference}.
	 * <b>ONLY one parameter must be non-null.</b>
	 *
	 * @param colName			The name/alias of the column.
	 * @param colIndice			The indice of the column.
	 * @return					The corresponding object representation.
	 * @throws ParseException	If the given column name/alias doesn't correspond to a selected or an existing column
	 *							or if the given column indice is out of limit.
	 *
	 * @see DBConsistency#columnExists(String)
	 *
	 * @see ADQLQuery#getNbColumns()
	 *
	 * @see QueryBuilderTools#createColRef(String)
	 * @see QueryBuilderTools#createColRef(int)
	 */
	private final ColumnReference createGroupBy(String colName, Token colIndice) throws ParseException {
		ColumnReference colRef = null;

		// COLUMN NAME case:
		if (colName != null){
			// Check whether the column exists:
			if (!dbCheck.columnExists(colName))
				throw new ParseException("The column \u005c""+colName+"\u005c" doesn't exist in any selected table !");
			// Create the corresponding object representation:
			colRef = buildTools.createColRef(colName);

			// COLUMN INDICE case:
		}else if (colIndice != null){
			try{
				// Check whether the indice is correct:
				int ind = Integer.parseInt(colIndice.image);
				if (ind <= 0 || ind > query.getNbColumns())
					throw new ParseException("The column indice \u005c""+ind+"\u005c" in the GROUP BY clause is incorrect: it must be contain between 1 and "+query.getNbColumns()+" (both included)");
				// Create the corresponding object representation:
				colRef = buildTools.createColRef(ind);
			}catch(NumberFormatException nfe){
				throw new ParseException("CRITICAL ERROR: A column indice (\u005c""+colIndice.image+"\u005c" in the GROUP BY clause) isn't a regular unsigned integer !");
			}

			// ELSE:
		}else
			throw new ParseException("A GROUP BY can only be put on column names or on column indices !");

		return colRef;
	}

	/**
	 * Checks whether the non-null parameter of the two first one is correct and creates the corresponding object representation: {@link ADQLOrder}.
	 * <b>ONLY one of the two first parameters must be non-null.</b>
	 *
	 * @param colName			The name/alias of the column.
	 * @param colIndice			The indice of the column.
	 * @param descSorting		The sort order (can be <i>null</i>).
	 * @return					The corresponding object representation.
	 * @throws ParseException	If the given column name/alias doesn't correspond to a selected or an existing column
	 *							or if the given column indice is out of limit.
	 *
	 * @see DBConsistency#columnExists(String, String)
	 *
	 * @see ADQLQuery#getNbColumns()
	 *
	 * @see QueryBuilderTools#createColRef(String)
	 * @see QueryBuilderTools#createColRef(int)
	 */
	private final ADQLOrder createOrder(String colName, Token colIndice, Token descSorting) throws ParseException {
		ADQLOrder order = null;
		boolean desc = (descSorting != null);

		// COLUMN INDICE case:
		if (colIndice != null){
			try{
				// Check whether the indice is correct:
				int ind = Integer.parseInt(colIndice.image);
				if (ind < 0 || ind > query.getNbColumns())
					throw new ParseException("The column indice \u005c""+ind+"\u005c" in the ORDER BY clause is incorrect: it must be contain between 1 and "+query.getNbColumns()+" (both included)");
				// Create the corresponding object representation:
				order = buildTools.createOrder(ind, desc);
			}catch(NumberFormatException nfe){
				throw new ParseException("CRITICAL ERROR: A column indice (\u005c""+colIndice.image+"\u005c" in the GROUP BY clause) isn't a regular unsigned integer !");
			}

			// COLUMN NAME case:
		}else if (colName != null){
			// Extract the table name if specified:
			int ind = colName.lastIndexOf('.');
			String column = colName, prefix = null;
			if (ind > 0){
				prefix = colName.substring(0, ind);
				column = colName.substring(ind+1);
			}
			// Check whether the column exists:
				if (!dbCheck.columnExists(column, prefix)){
					if (prefix == null)
						throw new ParseException("The column \u005c""+colName+"\u005c" doesn't exist in any selected table !");
					else
						throw new ParseException("The column \u005c""+colName+"\u005c" doesn't exist in the selected table \u005c""+prefix+"\u005c" !");
				}
				order = buildTools.createOrder(colName, desc);

				// ELSE:
		}else
			throw new ParseException("An ORDER can only be put on column names or on column indices !");

		return order;
	}

	/**
	 * Checks whether each selected column corresponds to a column of the selected tables.
	 * If they exists they are mapped with their alias (if existing).  
	 *
	 * @throws ParseException If a column reference doesn't correspond to an existing column.
	 * 
	 * @see ADQLQuery#getColumns()
	 *
	 * @see DBConsistency#selectedColumnExists(ADQLColumn)
	 * @see DBConsistency#addColumnAlias(String, String)
	 */
	private final void checkSelectedColumnsExistence() throws ParseException {
		// For each selected column:
		Iterator<ADQLOperand> it = query.getColumns();
		while(it.hasNext()){
			ADQLOperand col = it.next();

			// if it is a column reference...
			if (col instanceof ADQLColumn){
				ADQLColumn column = (ADQLColumn)col;
				// check whether the column exists (if yes, the corresponding alias mapping is automatically added by {@link DBConsistency#selectedColumnExists(ADQLColumn)}):
				if (!dbCheck.selectedColumnExists(column)){
					if (column.getPrefix() != null)
						throw new ParseException("The column \u005c""+column.getColumn()+"\u005c" doesn't exist in the table \u005c""+column.getPrefix()+"\u005c" !");
					else
						throw new ParseException("The column \u005c""+column.getColumn()+"\u005c" doesn't exist in any selected table !");
				}

				// else add an alias mapping if there is an alias for this operand:
			}else if (col.getAlias() != null)
				dbCheck.addColumnAlias(col.getAlias(), col.toString());
		}
	}

	/**
	 * <p>Gets the specified ADQL query and parses the given ADQL query. The SQL translation is then printed if the syntax is correct.</p>
	 * <p><b>ONLY the syntax is checked: the query is NOT EXECUTED !</b></p>
	 * <p>Supplied parameters are: <ul><li>[-debug] -url http://...</li><li>[-debug] -file ...</li><li>[-debug] -query SELECT...</li></ul></p>
	 *
	 * @param args
	 * @throws Exception
	 */
	public static final void main(String[] args) throws Exception {
//		final String USAGE = "Usage:\u005cn\u005ctAdqlParser [-debug] -url http://...\u005cn\u005ctAdqlParser [-debug] -file /home/...\u005cn\u005ctAdqlParser [-debug] -query\u005cnIMPORTANT: the query must be finished by a ; !";
//
//		AdqlParser parser;
//		int indParam = 0;
//		boolean debug = false;
//
//		// debug ?
//				if (args.length > 0 && args[indParam].equalsIgnoreCase("-debug")){
//					debug = true;
//					indParam++;
//				}
//
//				// Parameters reading:
//				if (args.length == indParam+1 && args[indParam].equalsIgnoreCase("-query"))
//					parser = new AdqlParser(System.in);
//				else if (args.length < indParam+2){
//					System.err.println("Parameters missing !\u005cn"+USAGE);
//					return;
//				}else{
//					if (args[indParam].equalsIgnoreCase("-url"))
//						parser = new AdqlParser((new java.net.URL(args[indParam+1])).openStream());
//					else if (args[indParam].equalsIgnoreCase("-file"))
//						parser = new AdqlParser(new FileReader(args[indParam+1]));
//					else{
//						System.err.println("Wrong parameters !\u005cn"+USAGE);
//						return;
//					}
//				}
//
//				// Query parsing:
//				try{
//					parser.setDebug(debug);
//					ADQLQuery q = parser.parseQuery();
//					System.out.println("\u005cn### CORRECT SYNTAX ###\u005cn");
//					System.out.println("### SQL translation ###\u005cn"+q.toSQL()+"\u005cn#######################");
//				}catch(ParseException pe){
//					System.err.println("### BAD SYNTAX ###\u005cn"+pe.getMessage());
//				}
		Database.init("saadaObscore");
		String query = "SELECT * FROM tap_schema.truc.test WHERE CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', 13, 2, 10)) = 1";
	AdqlParser parser = new AdqlParser();
	parser.setDebug(false);
ADQLQuery adqlQuery = parser.parseQuery(query);
String result = adqlQuery.toSQL(new SQLTranslator());
System.out.println(result);
	Database.close();
	
	}

	/* ########## */
	/* # SYNTAX # */
	/* ########## */

	/* ******************* */
	/* GENERAL ADQL SYNTAX */
	/* ******************* */
	/**
	 * Parses the ADQL query given at the parser creation or in the {@link AdqlParser#ReInit(java.io.InputStream)}
	 * or in the <i>parseQuery</i> functions.
	 *
	 * @return	The object representation of the query.
	 * @throws ParseException	If the query syntax is incorrect.
	 */
	final public ADQLQuery Query() throws ParseException {
		trace_call("Query");
		try {
			ADQLQuery q = null;
			if (debug) System.out.println("### START PARSING...");
			q = QueryExpression();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case 0:
				jj_consume_token(0);
				break;
			case EOQ:
				jj_consume_token(EOQ);
				break;
			default:
				jj_la1[0] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			if (debug) System.out.println("### END PARSING !"); {if (true) return q;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Query");
		}
	}

	final public ADQLQuery QueryExpression() throws ParseException {
		trace_call("QueryExpression");
		try {
			query = buildTools.createQuery(); 
			dbCheck.addContext(query);
			Select();
			From();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case WHERE:
				Where();
				break;
			default:
				jj_la1[1] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case GROUP_BY:
				GroupBy();
				break;
			default:
				jj_la1[2] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case HAVING:
				Having();
				break;
			default:
				jj_la1[3] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ORDER_BY:
				OrderBy();
				break;
			default:
				jj_la1[4] = jj_gen;
				;
			}
			if (!dbCheck.queryVerif(query))
			{if (true) throw new ParseException("ERROR: The query doesn't satisfy the last verifications !");}
			else {
				ADQLQuery newQuery = dbCheck.removeContext();
				query = dbCheck.getCurrentQuery();
				{if (true) return newQuery;}
			}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("QueryExpression");
		}
	}

	final public ADQLQuery SubQueryExpression() throws ParseException {
		trace_call("SubQueryExpression");
		try {
			ADQLQuery q = null;
			jj_consume_token(LEFT_PAR);
			q = QueryExpression();
			jj_consume_token(RIGHT_PAR);
			{if (true) return q;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("SubQueryExpression");
		}
	}

	final public void Select() throws ParseException {
		trace_call("Select");
		try {
			ADQLOperand item=null; Token t = null;
			jj_consume_token(SELECT);
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case QUANTIFIER:
				t = jj_consume_token(QUANTIFIER);
				query.setDistinct(t.image.equalsIgnoreCase("DISTINCT"));
				break;
			default:
				jj_la1[5] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case TOP:
				jj_consume_token(TOP);
				t = jj_consume_token(UNSIGNED_INTEGER);
				try{
					query.setLimit(Integer.parseInt(t.image));
				}catch(NumberFormatException nfe){
					{if (true) throw new ParseException("CRITICAL ERROR: The TOP limit (\u005c""+t.image+"\u005c") isn't a regular unsigned integer !");}
				}
				break;
			default:
				jj_la1[6] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ASTERISK:
				jj_consume_token(ASTERISK);
				break;
			case LEFT_PAR:
			case PLUS:
			case MINUS:
			case AVG:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case BOX:
			case CENTROID:
			case CIRCLE:
			case POINT:
			case POLYGON:
			case REGION:
			case CONTAINS:
			case INTERSECTS:
			case AREA:
			case COORD1:
			case COORD2:
			case COORDSYS:
			case DISTANCE:
			case ABS:
			case CEILING:
			case DEGREES:
			case EXP:
			case FLOOR:
			case LOG:
			case LOG10:
			case MOD:
			case PI:
			case POWER:
			case RADIANS:
			case RAND:
			case ROUND:
			case SQRT:
			case TRUNCATE:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case COS:
			case COT:
			case SIN:
			case TAN:
			case STRING_LITERAL:
			case DELIMITED_IDENTIFIER:
			case REGULAR_IDENTIFIER:
			case SCIENTIFIC_NUMBER:
			case UNSIGNED_FLOAT:
			case UNSIGNED_INTEGER:
				item = SelectItem();
				query.addSelectColumn(item);
				label_1:
					while (true) {
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case COMMA:
							;
							break;
						default:
							jj_la1[7] = jj_gen;
							break label_1;
						}
						jj_consume_token(COMMA);
						item = SelectItem();
						query.addSelectColumn(item);
					}
				break;
			default:
				jj_la1[8] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
		} finally {
			trace_return("Select");
		}
	}

	final public ADQLOperand SelectItem() throws ParseException {
		trace_call("SelectItem");
		try {
			String expr="", tmp, label; ADQLOperand op = null;
			if (jj_2_1(7)) {
				expr = Identifier();
				expr+=".";
				jj_consume_token(DOT);
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case DELIMITED_IDENTIFIER:
				case REGULAR_IDENTIFIER:
					tmp = Identifier();
					expr+=tmp+".";
					jj_consume_token(DOT);
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
						tmp = Identifier();
						expr+=tmp+".";
						jj_consume_token(DOT);
						break;
					default:
						jj_la1[9] = jj_gen;
						;
					}
					break;
				default:
					jj_la1[10] = jj_gen;
					;
				}
				jj_consume_token(ASTERISK);
				expr+="*"; op = buildTools.createColumn(expr, null, null);
			} else {
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
				case PLUS:
				case MINUS:
				case AVG:
				case MAX:
				case MIN:
				case SUM:
				case COUNT:
				case BOX:
				case CENTROID:
				case CIRCLE:
				case POINT:
				case POLYGON:
				case REGION:
				case CONTAINS:
				case INTERSECTS:
				case AREA:
				case COORD1:
				case COORD2:
				case COORDSYS:
				case DISTANCE:
				case ABS:
				case CEILING:
				case DEGREES:
				case EXP:
				case FLOOR:
				case LOG:
				case LOG10:
				case MOD:
				case PI:
				case POWER:
				case RADIANS:
				case RAND:
				case ROUND:
				case SQRT:
				case TRUNCATE:
				case ACOS:
				case ASIN:
				case ATAN:
				case ATAN2:
				case COS:
				case COT:
				case SIN:
				case TAN:
				case STRING_LITERAL:
				case DELIMITED_IDENTIFIER:
				case REGULAR_IDENTIFIER:
				case SCIENTIFIC_NUMBER:
				case UNSIGNED_FLOAT:
				case UNSIGNED_INTEGER:
					op = ValueExpression();
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case AS:
						jj_consume_token(AS);
						label = Identifier();
						op.setAlias(label);
						break;
					default:
						jj_la1[11] = jj_gen;
						;
					}
					break;
				default:
					jj_la1[12] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
			{if (true) return op;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("SelectItem");
		}
	}

	final public void From() throws ParseException {
		trace_call("From");
		try {
			ADQLTable t = null;
			jj_consume_token(FROM);
			t = TableRef();
			query.addTable(t);
			label_2:
				while (true) {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case COMMA:
						;
						break;
					default:
						jj_la1[13] = jj_gen;
						break label_2;
					}
					jj_consume_token(COMMA);
					t = TableRef();
					query.addTable(t);
				}
			checkSelectedColumnsExistence();
		} finally {
			trace_return("From");
		}
	}

	final public void Where() throws ParseException {
		trace_call("Where");
		try {
			ADQLConstraint condition;
			jj_consume_token(WHERE);
			condition = SearchCondition();
			query.addConstraint(condition);
		} finally {
			trace_return("Where");
		}
	}

	final public void GroupBy() throws ParseException {
		trace_call("GroupBy");
		try {
			String colName = null; Token ind = null;
			jj_consume_token(GROUP_BY);
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case DELIMITED_IDENTIFIER:
			case REGULAR_IDENTIFIER:
				colName = Identifier();
				query.addGroupBy(createGroupBy(colName, null));
				break;
			case UNSIGNED_INTEGER:
				ind = jj_consume_token(UNSIGNED_INTEGER);
				query.addGroupBy(createGroupBy(null, ind));
				break;
			default:
				jj_la1[14] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			label_3:
				while (true) {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case COMMA:
						;
						break;
					default:
						jj_la1[15] = jj_gen;
						break label_3;
					}
					jj_consume_token(COMMA);
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
						colName = Identifier();
						query.addGroupBy(createGroupBy(colName, null));
						break;
					case UNSIGNED_INTEGER:
						ind = jj_consume_token(UNSIGNED_INTEGER);
						query.addGroupBy(createGroupBy(null, ind));
						break;
					default:
						jj_la1[16] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
				}
		} finally {
			trace_return("GroupBy");
		}
	}

	final public void Having() throws ParseException {
		trace_call("Having");
		try {
			ADQLConstraint condition = null;
			jj_consume_token(HAVING);
			condition = SearchCondition();
			query.addHaving(condition);
		} finally {
			trace_return("Having");
		}
	}

	final public void OrderBy() throws ParseException {
		trace_call("OrderBy");
		try {
			String colName = null; Token ind = null; Token desc = null;
			jj_consume_token(ORDER_BY);
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case DELIMITED_IDENTIFIER:
			case REGULAR_IDENTIFIER:
				colName = Identifier();
				break;
			case UNSIGNED_INTEGER:
				ind = jj_consume_token(UNSIGNED_INTEGER);
				break;
			default:
				jj_la1[17] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ASC:
			case DESC:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case ASC:
					jj_consume_token(ASC);
					break;
				case DESC:
					desc = jj_consume_token(DESC);
					break;
				default:
					jj_la1[18] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;
			default:
				jj_la1[19] = jj_gen;
				;
			}
			query.addOrder(createOrder(colName, ind, desc)); colName = null; ind = null; desc = null;
			label_4:
				while (true) {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case COMMA:
						;
						break;
					default:
						jj_la1[20] = jj_gen;
						break label_4;
					}
					jj_consume_token(COMMA);
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
						colName = Identifier();
						break;
					case UNSIGNED_INTEGER:
						ind = jj_consume_token(UNSIGNED_INTEGER);
						break;
					default:
						jj_la1[21] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case ASC:
					case DESC:
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case ASC:
							jj_consume_token(ASC);
							break;
						case DESC:
							desc = jj_consume_token(DESC);
							break;
						default:
							jj_la1[22] = jj_gen;
							jj_consume_token(-1);
							throw new ParseException();
						}
						break;
					default:
						jj_la1[23] = jj_gen;
						;
					}
					query.addOrder(createOrder(colName, ind, desc)); colName = null; ind = null; desc = null;
				}
		} finally {
			trace_return("OrderBy");
		}
	}

	/* *************************** */
	/* COLUMN AND TABLE REFERENCES */
	/* *************************** */
	final public String Identifier() throws ParseException {
		trace_call("Identifier");
		try {
			Token t; String id;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case REGULAR_IDENTIFIER:
				t = jj_consume_token(REGULAR_IDENTIFIER);
				id=t.image;
				break;
			case DELIMITED_IDENTIFIER:
				t = jj_consume_token(DELIMITED_IDENTIFIER);
				id=t.image.substring(1, t.image.length()-1);
				break;
			default:
				jj_la1[24] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}		
			
			{if (true) return id;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Identifier");
		}
	}

	final public String TableName() throws ParseException {
		trace_call("TableName");
		try {
			String table="";
			
			table = Identifier();
			System.out.println("#TableName#1 table"+table);
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case DOT:
				jj_consume_token(DOT);
				//table = Identifier();
				//TODO Patch created to allow the parser to work with Saada
				String id = Identifier();
				System.out.println("#TableName#1 id "+id);
				if( Database.getCachemeta().collectionExists(table) || table.equalsIgnoreCase("ivoa")|| table.equalsIgnoreCase("tap_schema")) {
					table = id; 
					System.out.println("#TableName#1 patch new table "+table);
				} else {
					table += "."+id;
					System.out.println("#TableName#1 new table "+table);
				}
				//\\//
				break;
			default:
				jj_la1[25] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case DOT:
				jj_consume_token(DOT);
				//table = Identifier();
				//TODO Patch created to allow the parser to work with Saada
				String id = Identifier();
				System.out.println("#TableName#2 id "+id);
				if( Database.getCachemeta().collectionExists(table) || table.equalsIgnoreCase("ivoa")|| table.equalsIgnoreCase("tap_schema")) {
					table = id; 
					System.out.println("#TableName#2 patch table "+table);
				} else {
					table += "."+id;
					System.out.println("#Tablename#2 new table "+table);
				}
				//\\//
				break;
			default:
				jj_la1[26] = jj_gen;
				;
			}
			{if (true) return table;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("TableName");
		}
	}

	final public ADQLColumn ColumnReference() throws ParseException {
		trace_call("ColumnReference");
		try {
			String col1="", col2 = null;
			col1 = Identifier();
			System.out.println("#ColumnReference col1 "+col1);
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case DOT:
				jj_consume_token(DOT);
				col2 = TableName();
				System.out.println("#ColumnReference col2 "+col2);
				if( Database.getCachemeta().collectionExists(col1) || col1.equalsIgnoreCase("ivoa")) {
					col1 = col2; 
					System.out.println("#ColumnReference Patch : new col 1 "+col1);
				} else {
					col1 += "."+col2;
					System.out.println("#ColumnReference new col1 "+col1);
				}
				//@@@@@@@@@@@@@ col1 += "."+col2;
				//col1 = col2;
				break;
			default:
				jj_la1[27] = jj_gen;
				;
			}
			{if (true) return createColumn(col1);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("ColumnReference");
		}
	}

	final public ADQLTable SimpleTableRef() throws ParseException {
		trace_call("SimpleTableRef");
		try {
			String table=null, alias=null; ADQLQuery q = null;
			if (jj_2_4(2)) {
				if (jj_2_2(2)) {
					table = TableName();
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case AS:
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case AS:
							jj_consume_token(AS);
							break;
						default:
							jj_la1[28] = jj_gen;
							;
						}
						alias = Identifier();
						break;
					default:
						jj_la1[29] = jj_gen;
						;
					}
				} else {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case LEFT_PAR:
						q = SubQueryExpression();
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case AS:
							jj_consume_token(AS);
							break;
						default:
							jj_la1[30] = jj_gen;
							;
						}
						alias = Identifier();
						break;
					default:
						jj_la1[31] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
				}
			} else {
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
					jj_consume_token(LEFT_PAR);
					if (jj_2_3(2)) {
						table = TableName();
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case AS:
						case DELIMITED_IDENTIFIER:
						case REGULAR_IDENTIFIER:
							switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
							case AS:
								jj_consume_token(AS);
								break;
							default:
								jj_la1[32] = jj_gen;
								;
							}
							alias = Identifier();
							break;
						default:
							jj_la1[33] = jj_gen;
							;
						}
					} else {
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case LEFT_PAR:
							q = SubQueryExpression();
							switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
							case AS:
								jj_consume_token(AS);
								break;
							default:
								jj_la1[34] = jj_gen;
								;
							}
							alias = Identifier();
							break;
						default:
							jj_la1[35] = jj_gen;
							jj_consume_token(-1);
							throw new ParseException();
						}
					}
					jj_consume_token(RIGHT_PAR);
					break;
				default:
					jj_la1[36] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
			{if (true) return createTable(table, alias, q);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("SimpleTableRef");
		}
	}

	final public ADQLTable TableRef() throws ParseException {
		trace_call("TableRef");
		try {
			String table = null, alias = null; ADQLQuery q = null; ADQLTable t = null; ADQLJoin join = null;
			if (jj_2_7(2)) {
				if (jj_2_5(2)) {
					table = TableName();
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case AS:
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case AS:
							jj_consume_token(AS);
							break;
						default:
							jj_la1[37] = jj_gen;
							;
						}
						alias = Identifier();
						break;
					default:
						jj_la1[38] = jj_gen;
						;
					}
					t = createTable(table, alias, null);
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case NATURAL:
					case INNER:
					case RIGHT:
					case LEFT:
					case FULL:
					case JOIN:
						join = Join();
						t.setJoin(join);
						break;
					default:
						jj_la1[39] = jj_gen;
						;
					}
				} else {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case LEFT_PAR:
						q = SubQueryExpression();
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case AS:
							jj_consume_token(AS);
							break;
						default:
							jj_la1[40] = jj_gen;
							;
						}
						alias = Identifier();
						t = createTable(table, alias, q);
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case NATURAL:
						case INNER:
						case RIGHT:
						case LEFT:
						case FULL:
						case JOIN:
							join = Join();
							t.setJoin(join);
							break;
						default:
							jj_la1[41] = jj_gen;
							;
						}
						break;
					default:
						jj_la1[42] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
				}
			} else {
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
					jj_consume_token(LEFT_PAR);
					if (jj_2_6(2)) {
						table = TableName();
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case AS:
						case DELIMITED_IDENTIFIER:
						case REGULAR_IDENTIFIER:
							switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
							case AS:
								jj_consume_token(AS);
								break;
							default:
								jj_la1[43] = jj_gen;
								;
							}
							alias = Identifier();
							break;
						default:
							jj_la1[44] = jj_gen;
							;
						}
						t = createTable(table, alias, null);
					} else {
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case LEFT_PAR:
							q = SubQueryExpression();
							switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
							case AS:
								jj_consume_token(AS);
								break;
							default:
								jj_la1[45] = jj_gen;
								;
							}
							alias = Identifier();
							t = createTable(table, alias, q);
							break;
						default:
							jj_la1[46] = jj_gen;
							jj_consume_token(-1);
							throw new ParseException();
						}
					}
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case NATURAL:
					case INNER:
					case RIGHT:
					case LEFT:
					case FULL:
					case JOIN:
						join = Join();
						t.setJoin(join);
						break;
					default:
						jj_la1[47] = jj_gen;
						;
					}
					jj_consume_token(RIGHT_PAR);
					break;
				default:
					jj_la1[48] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
			{if (true) return t;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("TableRef");
		}
	}

	final public ADQLJoin Join() throws ParseException {
		trace_call("Join");
		try {
			boolean natural = false; JoinType type = JoinType.INNER; ADQLTable table; ADQLJoin join = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case NATURAL:
				jj_consume_token(NATURAL);
				natural=true;
				break;
			default:
				jj_la1[49] = jj_gen;
				;
			}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case INNER:
			case RIGHT:
			case LEFT:
			case FULL:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case INNER:
					jj_consume_token(INNER);
					break;
				case RIGHT:
				case LEFT:
				case FULL:
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case LEFT:
						jj_consume_token(LEFT);
						type = JoinType.OUTER_LEFT;
						break;
					case RIGHT:
						jj_consume_token(RIGHT);
						type = JoinType.OUTER_RIGHT;
						break;
					case FULL:
						jj_consume_token(FULL);
						type = JoinType.OUTER_FULL;
						break;
					default:
						jj_la1[50] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case OUTER:
						jj_consume_token(OUTER);
						break;
					default:
						jj_la1[51] = jj_gen;
						;
					}
					break;
				default:
					jj_la1[52] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;
			default:
				jj_la1[53] = jj_gen;
				;
			}
			jj_consume_token(JOIN);
			table = SimpleTableRef();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ON:
			case USING:
				join = JoinSpecification(natural, type, table);
				break;
			default:
				jj_la1[54] = jj_gen;
				;
			}
			if (join == null){
				if (!natural)
				{if (true) throw new ParseException("JOIN syntax incorrect: you must either use the keywork NATURAL or put a condition (keyword ON) or a list of columns (keyword USING) !");}
				else
				{if (true) return buildTools.createJoin(type, table);}
			}else
			{if (true) return join;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Join");
		}
	}

	final public ADQLJoin JoinSpecification(boolean natural, JoinType type, ADQLTable table) throws ParseException {
		trace_call("JoinSpecification");
		try {
			ADQLConstraint condition = null; ADQLJoin join = null; ArrayList<String> lstColumns = new ArrayList<String>(); String ident;
			if (natural)
			{if (true) throw new ParseException("JOIN syntax incorrect: you must either use the keywork NATURAL or put a condition (keyword ON) or a list of columns (keyword USING) !");}
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ON:
				jj_consume_token(ON);
				condition = SearchCondition();
				join = buildTools.createJoin(type, table, condition);
				break;
			case USING:
				jj_consume_token(USING);
				jj_consume_token(LEFT_PAR);
				ident = Identifier();
				lstColumns.add(ident);
				label_5:
					while (true) {
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case COMMA:
							;
							break;
						default:
							jj_la1[55] = jj_gen;
							break label_5;
						}
						jj_consume_token(COMMA);
						ident = Identifier();
						lstColumns.add(ident);
					}
				jj_consume_token(RIGHT_PAR);
				join = buildTools.createJoin(type, table, lstColumns);
				break;
			default:
				jj_la1[56] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return join;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("JoinSpecification");
		}
	}

	/* ****** */
	/* STRING */
	/* ****** */
	final public String String() throws ParseException {
		trace_call("String");
		try {
			Token t; String str="";
			label_6:
				while (true) {
					t = jj_consume_token(STRING_LITERAL);
					str += t.image;
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case STRING_LITERAL:
						;
						break;
					default:
						jj_la1[57] = jj_gen;
						break label_6;
					}
				}
			{if (true) return (str!=null)?str.substring(1,str.length()-1):str;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("String");
		}
	}

	/* ************* */
	/* NUMERIC TYPES */
	/* ************* */
	final public String UnsignedNumeric() throws ParseException {
		trace_call("UnsignedNumeric");
		try {
			Token t;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case SCIENTIFIC_NUMBER:
				t = jj_consume_token(SCIENTIFIC_NUMBER);
				break;
			case UNSIGNED_FLOAT:
				t = jj_consume_token(UNSIGNED_FLOAT);
				break;
			case UNSIGNED_INTEGER:
				t = jj_consume_token(UNSIGNED_INTEGER);
				break;
			default:
				jj_la1[58] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return t.image;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("UnsignedNumeric");
		}
	}

	final public String UnsignedFloat() throws ParseException {
		trace_call("UnsignedFloat");
		try {
			Token t;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case UNSIGNED_INTEGER:
				t = jj_consume_token(UNSIGNED_INTEGER);
				break;
			case UNSIGNED_FLOAT:
				t = jj_consume_token(UNSIGNED_FLOAT);
				break;
			default:
				jj_la1[59] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return t.image;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("UnsignedFloat");
		}
	}

	final public String SignedInteger() throws ParseException {
		trace_call("SignedInteger");
		try {
			Token sign=null, number;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case PLUS:
			case MINUS:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case PLUS:
					sign = jj_consume_token(PLUS);
					break;
				case MINUS:
					sign = jj_consume_token(MINUS);
					break;
				default:
					jj_la1[60] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;
			default:
				jj_la1[61] = jj_gen;
				;
			}
			number = jj_consume_token(UNSIGNED_INTEGER);
			{if (true) return ((sign==null)?"":sign.image)+number.image;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("SignedInteger");
		}
	}

	/* *********** */
	/* EXPRESSIONS */
	/* *********** */
	final public ADQLOperand ValueExpressionPrimary() throws ParseException {
		trace_call("ValueExpressionPrimary");
		try {
			String expr; ADQLColumn column; ADQLOperand op;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case SCIENTIFIC_NUMBER:
			case UNSIGNED_FLOAT:
			case UNSIGNED_INTEGER:
				expr = UnsignedNumeric();
				{if (true) return buildTools.createConstant(expr, ADQLType.INTEGER);}
				break;
			case STRING_LITERAL:
				expr = String();
				{if (true) return buildTools.createConstant(expr, ADQLType.STRING);}
				break;
			case DELIMITED_IDENTIFIER:
			case REGULAR_IDENTIFIER:
				column = ColumnReference();
				{if (true) return column;}
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
				op = SqlFunction();
				{if (true) return op;}
				break;
			case LEFT_PAR:
				jj_consume_token(LEFT_PAR);
				op = ValueExpression();
				jj_consume_token(RIGHT_PAR);
				{if (true) return op;}
				break;
			default:
				jj_la1[62] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("ValueExpressionPrimary");
		}
	}

	final public ADQLOperand ValueExpression() throws ParseException {
		trace_call("ValueExpression");
		try {
			ADQLOperand valueExpr = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case BOX:
			case CENTROID:
			case CIRCLE:
			case POINT:
			case POLYGON:
			case REGION:
				valueExpr = GeometryValueFunction();
				break;
			default:
				jj_la1[63] = jj_gen;
				if (jj_2_8(2147483647)) {
					valueExpr = NumericExpression();
				} else if (jj_2_9(2147483647)) {
					valueExpr = StringExpression();
				} else if (jj_2_10(2147483647)) {
					valueExpr = StringExpression();
				} else {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case LEFT_PAR:
					case PLUS:
					case MINUS:
					case AVG:
					case MAX:
					case MIN:
					case SUM:
					case COUNT:
					case CONTAINS:
					case INTERSECTS:
					case AREA:
					case COORD1:
					case COORD2:
					case DISTANCE:
					case ABS:
					case CEILING:
					case DEGREES:
					case EXP:
					case FLOOR:
					case LOG:
					case LOG10:
					case MOD:
					case PI:
					case POWER:
					case RADIANS:
					case RAND:
					case ROUND:
					case SQRT:
					case TRUNCATE:
					case ACOS:
					case ASIN:
					case ATAN:
					case ATAN2:
					case COS:
					case COT:
					case SIN:
					case TAN:
					case STRING_LITERAL:
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
					case SCIENTIFIC_NUMBER:
					case UNSIGNED_FLOAT:
					case UNSIGNED_INTEGER:
						valueExpr = NumericExpression();
						break;
					default:
						jj_la1[64] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
				}
			}
			{if (true) return valueExpr;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("ValueExpression");
		}
	}

	final public ADQLOperand NumericExpression() throws ParseException {
		trace_call("NumericExpression");
		try {
			Token sign=null; ADQLOperand leftOp, rightOp=null;
			leftOp = NumericTerm();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case PLUS:
			case MINUS:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case PLUS:
					sign = jj_consume_token(PLUS);
					break;
				case MINUS:
					sign = jj_consume_token(MINUS);
					break;
				default:
					jj_la1[65] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				rightOp = NumericExpression();
				break;
			default:
				jj_la1[66] = jj_gen;
				;
			}
			if (sign == null)
			{if (true) return leftOp;}
			else
			{if (true) return buildTools.createOperation(leftOp, OperationType.getOperator(sign.image), rightOp);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("NumericExpression");
		}
	}

	final public ADQLOperand NumericTerm() throws ParseException {
		trace_call("NumericTerm");
		try {
			Token sign=null; ADQLOperand leftOp, rightOp=null;
			leftOp = Factor();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ASTERISK:
			case DIVIDE:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case ASTERISK:
					sign = jj_consume_token(ASTERISK);
					break;
				case DIVIDE:
					sign = jj_consume_token(DIVIDE);
					break;
				default:
					jj_la1[67] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				rightOp = NumericTerm();
				break;
			default:
				jj_la1[68] = jj_gen;
				;
			}
			if (sign == null)
			{if (true) return leftOp;}
			else
			{if (true) return buildTools.createOperation(leftOp, OperationType.getOperator(sign.image), rightOp);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("NumericTerm");
		}
	}

	final public ADQLOperand Factor() throws ParseException {
		trace_call("Factor");
		try {
			Token sign=null; ADQLOperand op;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case PLUS:
			case MINUS:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case PLUS:
					sign = jj_consume_token(PLUS);
					break;
				case MINUS:
					sign = jj_consume_token(MINUS);
					break;
				default:
					jj_la1[69] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				break;
			default:
				jj_la1[70] = jj_gen;
				;
			}
			if (jj_2_11(2)) {
				op = NumericFunction();
			} else {
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
				case AVG:
				case MAX:
				case MIN:
				case SUM:
				case COUNT:
				case STRING_LITERAL:
				case DELIMITED_IDENTIFIER:
				case REGULAR_IDENTIFIER:
				case SCIENTIFIC_NUMBER:
				case UNSIGNED_FLOAT:
				case UNSIGNED_INTEGER:
					op = ValueExpressionPrimary();
					break;
				default:
					jj_la1[71] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
			op.negativate(sign != null && sign.image.equalsIgnoreCase("-"));
			{if (true) return op;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Factor");
		}
	}

	final public ADQLOperand StringExpression() throws ParseException {
		trace_call("StringExpression");
		try {
			ADQLOperand leftOp; ADQLOperand rightOp = null;
			leftOp = StringFactor();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case CONCAT:
				jj_consume_token(CONCAT);
				rightOp = StringExpression();
				break;
			default:
				jj_la1[72] = jj_gen;
				;
			}
			if (rightOp == null)
			{if (true) return leftOp;}
			else
			{if (true) return buildTools.createConcatenation(leftOp, rightOp);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("StringExpression");
		}
	}

	final public ADQLOperand StringFactor() throws ParseException {
		trace_call("StringFactor");
		try {
			ADQLOperand op;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case COORDSYS:
				op = ExtractCoordSys();
				break;
			default:
				jj_la1[73] = jj_gen;
				if (jj_2_12(2)) {
					op = UserDefinedFunction();
				} else {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case LEFT_PAR:
					case AVG:
					case MAX:
					case MIN:
					case SUM:
					case COUNT:
					case STRING_LITERAL:
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
					case SCIENTIFIC_NUMBER:
					case UNSIGNED_FLOAT:
					case UNSIGNED_INTEGER:
						op = ValueExpressionPrimary();
						break;
					default:
						jj_la1[74] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
				}
			}
			{if (true) return op;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("StringFactor");
		}
	}

	final public ADQLOperand GeometryExpression() throws ParseException {
		trace_call("GeometryExpression");
		try {
			ADQLOperand op;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case LEFT_PAR:
			case AVG:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case STRING_LITERAL:
			case DELIMITED_IDENTIFIER:
			case REGULAR_IDENTIFIER:
			case SCIENTIFIC_NUMBER:
			case UNSIGNED_FLOAT:
			case UNSIGNED_INTEGER:
				op = ValueExpressionPrimary();
				break;
			case BOX:
			case CENTROID:
			case CIRCLE:
			case POINT:
			case POLYGON:
			case REGION:
				op = GeometryValueFunction();
				break;
			default:
				jj_la1[75] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return op;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("GeometryExpression");
		}
	}

	/* ********************************** */
	/* BOOLEAN EXPRESSIONS (WHERE clause) */
	/* ********************************** */
	final public ADQLConstraint SearchCondition() throws ParseException {
		trace_call("SearchCondition");
		try {
			ADQLConstraint const1 = null, const2 = null;
			const1 = BooleanTerm();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case OR:
				jj_consume_token(OR);
				const2 = SearchCondition();
				const1.addConstraint(const2, true);
				break;
			default:
				jj_la1[76] = jj_gen;
				;
			}
			{if (true) return const1;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("SearchCondition");
		}
	}

	/**
	 * @return
	 * @throws ParseException
	 */
	final public ADQLConstraint BooleanTerm() throws ParseException {
		trace_call("BooleanTerm");
		try {
			ADQLConstraint const1=null, const2=null;
			const1 = BooleanFactor();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case AND:
				jj_consume_token(AND);
				const2 = BooleanTerm();
				const1.addConstraint(const2);
				break;
			default:
				jj_la1[77] = jj_gen;
				;
			}
			{if (true) return const1;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("BooleanTerm");
		}
	}

	/**
	 * @return
	 * @throws ParseException
	 */
	final public ADQLConstraint BooleanFactor() throws ParseException {
		trace_call("BooleanFactor");
		try {
			boolean not = false; ADQLConstraint constraint = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case NOT:
				jj_consume_token(NOT);
				not = true;
				break;
			default:
				jj_la1[78] = jj_gen;
				;
			}
			if (jj_2_13(2147483647)) {
				constraint = Predicate();
			} else {
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
					jj_consume_token(LEFT_PAR);
					constraint = SearchCondition();
					jj_consume_token(RIGHT_PAR);
					constraint = buildTools.createGroup(constraint);
					break;
				default:
					jj_la1[79] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
			constraint.setNot(not);
			{if (true) return constraint;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("BooleanFactor");
		}
	}

	/**
	 * @return
	 * @throws ParseException
	 */
	final public ADQLConstraint Predicate() throws ParseException {
		trace_call("Predicate");
		try {
			ADQLQuery q=null; ADQLColumn column=null; ADQLOperand strExpr1=null, strExpr2=null; ADQLOperand op; Token notToken = null; ADQLConstraint constraint = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case EXISTS:
				jj_consume_token(EXISTS);
				q = SubQueryExpression();
				{if (true) return buildTools.createExists(q);}
				break;
			default:
				jj_la1[84] = jj_gen;
				if (jj_2_15(2147483647)) {
					column = ColumnReference();
					jj_consume_token(IS);
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case NOT:
						notToken = jj_consume_token(NOT);
						break;
					default:
						jj_la1[80] = jj_gen;
						;
					}
					jj_consume_token(NULL);
					{if (true) return buildTools.createIsNull(column,  (notToken!=null));}
				} else if (jj_2_16(2147483647)) {
					strExpr1 = StringExpression();
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case NOT:
						notToken = jj_consume_token(NOT);
						break;
					default:
						jj_la1[81] = jj_gen;
						;
					}
					jj_consume_token(LIKE);
					strExpr2 = StringExpression();
					{if (true) return buildTools.createComparison(strExpr1, (notToken==null)?ComparisonOperator.LIKE:ComparisonOperator.NOTLIKE, strExpr2);}
				} else {
					switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
					case LEFT_PAR:
					case PLUS:
					case MINUS:
					case AVG:
					case MAX:
					case MIN:
					case SUM:
					case COUNT:
					case BOX:
					case CENTROID:
					case CIRCLE:
					case POINT:
					case POLYGON:
					case REGION:
					case CONTAINS:
					case INTERSECTS:
					case AREA:
					case COORD1:
					case COORD2:
					case COORDSYS:
					case DISTANCE:
					case ABS:
					case CEILING:
					case DEGREES:
					case EXP:
					case FLOOR:
					case LOG:
					case LOG10:
					case MOD:
					case PI:
					case POWER:
					case RADIANS:
					case RAND:
					case ROUND:
					case SQRT:
					case TRUNCATE:
					case ACOS:
					case ASIN:
					case ATAN:
					case ATAN2:
					case COS:
					case COT:
					case SIN:
					case TAN:
					case STRING_LITERAL:
					case DELIMITED_IDENTIFIER:
					case REGULAR_IDENTIFIER:
					case SCIENTIFIC_NUMBER:
					case UNSIGNED_FLOAT:
					case UNSIGNED_INTEGER:
						op = ValueExpression();
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case EQUAL:
						case NOT_EQUAL:
						case LESS_THAN:
						case LESS_EQUAL_THAN:
						case GREATER_THAN:
						case GREATER_EQUAL_THAN:
							constraint = ComparisonEnd(op);
							break;
						default:
							jj_la1[82] = jj_gen;
							if (jj_2_14(2)) {
								constraint = BetweenEnd(op);
							} else {
								switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
								case NOT:
								case IN:
									constraint = InEnd(op);
									break;
								default:
									jj_la1[83] = jj_gen;
									jj_consume_token(-1);
									throw new ParseException();
								}
							}
						}
						break;
					default:
						jj_la1[85] = jj_gen;
						jj_consume_token(-1);
						throw new ParseException();
					}
				}
			}
			{if (true) return constraint;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Predicate");
		}
	}

	final public ADQLComparison ComparisonEnd(ADQLOperand leftOp) throws ParseException {
		trace_call("ComparisonEnd");
		try {
			Token comp; ADQLOperand rightOp;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case EQUAL:
				comp = jj_consume_token(EQUAL);
				break;
			case NOT_EQUAL:
				comp = jj_consume_token(NOT_EQUAL);
				break;
			case LESS_THAN:
				comp = jj_consume_token(LESS_THAN);
				break;
			case LESS_EQUAL_THAN:
				comp = jj_consume_token(LESS_EQUAL_THAN);
				break;
			case GREATER_THAN:
				comp = jj_consume_token(GREATER_THAN);
				break;
			case GREATER_EQUAL_THAN:
				comp = jj_consume_token(GREATER_EQUAL_THAN);
				break;
			default:
				jj_la1[86] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			rightOp = ValueExpression();
			{if (true) return buildTools.createComparison(leftOp, ComparisonOperator.getOperator(comp.image), rightOp);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("ComparisonEnd");
		}
	}

	final public ADQLComparison BetweenEnd(ADQLOperand leftOp) throws ParseException {
		trace_call("BetweenEnd");
		try {
			Token notToken=null; ADQLOperand min, max; ADQLComparison constraint;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case NOT:
				notToken = jj_consume_token(NOT);
				break;
			default:
				jj_la1[87] = jj_gen;
				;
			}
			jj_consume_token(BETWEEN);
			min = ValueExpression();
			jj_consume_token(AND);
			max = ValueExpression();
			if (notToken == null){
				constraint = buildTools.createComparison(leftOp, ComparisonOperator.GREATER_OR_EQUAL, min);
				constraint.addConstraint(buildTools.createComparison(leftOp, ComparisonOperator.LESS_OR_EQUAL, max));
			}else{
				constraint = buildTools.createComparison(leftOp, ComparisonOperator.LESS_THAN, min);
				constraint.addConstraint(buildTools.createComparison(leftOp, ComparisonOperator.GREATER_THAN, max));
			}
			{if (true) return constraint;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("BetweenEnd");
		}
	}

	/**
	 * @param leftOp
	 * @return
	 * @throws ParseException
	 */
	final public InFunction InEnd(ADQLOperand leftOp) throws ParseException{
		trace_call("InEnd");
		try {
			Token not=null; ADQLQuery q = null; ADQLOperand item; ArrayList<ADQLOperand> items = new ArrayList<ADQLOperand>();
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case NOT:
				not = jj_consume_token(NOT);
				break;
			default:
				jj_la1[88] = jj_gen;
				;
			}
			jj_consume_token(IN);
			if (jj_2_17(2)) {
				q = SubQueryExpression();
			} else {
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
					jj_consume_token(LEFT_PAR);
					item = ValueExpression();
					items.add(item);
					label_7:
						while (true) {
							switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
							case COMMA:
								;
								break;
							default:
								jj_la1[89] = jj_gen;
								break label_7;
							}
							jj_consume_token(COMMA);
							item = ValueExpression();
							items.add(item);
						}
					jj_consume_token(RIGHT_PAR);
					break;
				default:
					jj_la1[90] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
			if (q != null)
			{if (true) return buildTools.createIn(leftOp, q, not!=null);}
			else{
				ADQLOperand[] list = new ADQLOperand[items.size()];
				int i=0;
				for(ADQLOperand op : items)
					list[i++] = op;
				{if (true) return buildTools.createIn(leftOp, list, not!=null);}
			}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("InEnd");
		}
	}

	/* ************* */
	/* SQL FUNCTIONS */
	/* ************* */
	final public SQLFunction SqlFunction() throws ParseException {
		trace_call("SqlFunction");
		try {
			Token fct, all=null, distinct=null; ADQLOperand op=null; SQLFunction funct = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case COUNT:
				jj_consume_token(COUNT);
				jj_consume_token(LEFT_PAR);
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case QUANTIFIER:
					distinct = jj_consume_token(QUANTIFIER);
					break;
				default:
					jj_la1[91] = jj_gen;
					;
				}
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case ASTERISK:
					all = jj_consume_token(ASTERISK);
					break;
				case LEFT_PAR:
				case PLUS:
				case MINUS:
				case AVG:
				case MAX:
				case MIN:
				case SUM:
				case COUNT:
				case BOX:
				case CENTROID:
				case CIRCLE:
				case POINT:
				case POLYGON:
				case REGION:
				case CONTAINS:
				case INTERSECTS:
				case AREA:
				case COORD1:
				case COORD2:
				case COORDSYS:
				case DISTANCE:
				case ABS:
				case CEILING:
				case DEGREES:
				case EXP:
				case FLOOR:
				case LOG:
				case LOG10:
				case MOD:
				case PI:
				case POWER:
				case RADIANS:
				case RAND:
				case ROUND:
				case SQRT:
				case TRUNCATE:
				case ACOS:
				case ASIN:
				case ATAN:
				case ATAN2:
				case COS:
				case COT:
				case SIN:
				case TAN:
				case STRING_LITERAL:
				case DELIMITED_IDENTIFIER:
				case REGULAR_IDENTIFIER:
				case SCIENTIFIC_NUMBER:
				case UNSIGNED_FLOAT:
				case UNSIGNED_INTEGER:
					op = ValueExpression();
					break;
				default:
					jj_la1[92] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				jj_consume_token(RIGHT_PAR);
				funct = buildTools.createSQLFunction(SQLFunctionType.COUNT, op, distinct != null && distinct.image.equalsIgnoreCase("distinct"), all!=null);
				break;
			case AVG:
			case MAX:
			case MIN:
			case SUM:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case AVG:
					fct = jj_consume_token(AVG);
					break;
				case MAX:
					fct = jj_consume_token(MAX);
					break;
				case MIN:
					fct = jj_consume_token(MIN);
					break;
				case SUM:
					fct = jj_consume_token(SUM);
					break;
				default:
					jj_la1[93] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				jj_consume_token(LEFT_PAR);
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case QUANTIFIER:
					distinct = jj_consume_token(QUANTIFIER);
					break;
				default:
					jj_la1[94] = jj_gen;
					;
				}
				op = ValueExpression();
				jj_consume_token(RIGHT_PAR);
				funct = buildTools.createSQLFunction(SQLFunctionType.valueOf(fct.image.toUpperCase()), op, distinct != null && distinct.image.equalsIgnoreCase("distinct"), false);
				break;
			default:
				jj_la1[95] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return funct;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("SqlFunction");
		}
	}

	/* ************** */
	/* ADQL FUNCTIONS */
	/* ************** */
	final public ADQLOperand[] Coordinates() throws ParseException {
		trace_call("Coordinates");
		try {
			ADQLOperand[] ops = new ADQLOperand[2];
			ops[0] = NumericExpression();
			jj_consume_token(COMMA);
			ops[1] = NumericExpression();
			{if (true) return ops;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Coordinates");
		}
	}

	final public GeometryFunction GeometryFunction() throws ParseException {
		trace_call("GeometryFunction");
		try {
			Token t=null; ADQLOperand op1, op2; GeometryFunction gf = null; PointFunction p1=null, p2=null; ADQLColumn col=null; int indCoord = 1;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case CONTAINS:
			case INTERSECTS:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case CONTAINS:
					t = jj_consume_token(CONTAINS);
					break;
				case INTERSECTS:
					t = jj_consume_token(INTERSECTS);
					break;
				default:
					jj_la1[96] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				jj_consume_token(LEFT_PAR);
				op1 = GeometryExpression();
				jj_consume_token(COMMA);
				op2 = GeometryExpression();
				jj_consume_token(RIGHT_PAR);
				if (t.image.equalsIgnoreCase("contains"))
					gf = buildTools.createContains(op1, op2);
				else
					gf = buildTools.createIntersects(op1, op2);
				break;
			case AREA:
				jj_consume_token(AREA);
				jj_consume_token(LEFT_PAR);
				op1 = GeometryExpression();
				jj_consume_token(RIGHT_PAR);
				gf = buildTools.createArea(op1);
				break;
			case COORD1:
			case COORD2:
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case COORD1:
					jj_consume_token(COORD1);
					break;
				case COORD2:
					jj_consume_token(COORD2);
					indCoord = 2;
					break;
				default:
					jj_la1[97] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				jj_consume_token(LEFT_PAR);
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case POINT:
					p1 = Point();
					gf = buildTools.createExtractCoord(indCoord, p1);
					break;
				case DELIMITED_IDENTIFIER:
				case REGULAR_IDENTIFIER:
					col = ColumnReference();
					gf = buildTools.createExtractCoord(indCoord, col);
					break;
				default:
					jj_la1[98] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				jj_consume_token(RIGHT_PAR);
				break;
			case DISTANCE:
				jj_consume_token(DISTANCE);
				jj_consume_token(LEFT_PAR);
				p1 = Point();
				jj_consume_token(COMMA);
				p2 = Point();
				jj_consume_token(RIGHT_PAR);
				gf = buildTools.createDistance(p1, p2);
				break;
			default:
				jj_la1[99] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return gf;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("GeometryFunction");
		}
	}

	final public GeometryFunction GeometryValueFunction() throws ParseException {
		trace_call("GeometryValueFunction");
		try {
			ADQLOperand coordSys; ADQLOperand width, height; ADQLOperand[] coords, tmp; ADQLOperand op=null; GeometryFunction gf = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case BOX:
				jj_consume_token(BOX);
				jj_consume_token(LEFT_PAR);
				coordSys = StringExpression();
				jj_consume_token(COMMA);
				coords = Coordinates();
				jj_consume_token(COMMA);
				width = NumericExpression();
				jj_consume_token(COMMA);
				height = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				gf = buildTools.createBox(coordSys, coords[0], coords[1], width, height);
				break;
			case CENTROID:
				jj_consume_token(CENTROID);
				jj_consume_token(LEFT_PAR);
				op = GeometryExpression();
				jj_consume_token(RIGHT_PAR);
				gf = buildTools.createCentroid(op);
				break;
			case CIRCLE:
				jj_consume_token(CIRCLE);
				jj_consume_token(LEFT_PAR);
				coordSys = StringExpression();
				jj_consume_token(COMMA);
				coords = Coordinates();
				jj_consume_token(COMMA);
				width = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				gf = buildTools.createCircle(coordSys, coords[0], coords[1], width);
				break;
			case POINT:
				gf = Point();
				break;
			case POLYGON:
				jj_consume_token(POLYGON);
				jj_consume_token(LEFT_PAR);
				coordSys = StringExpression();
				coords = new ADQLOperand[4];
				jj_consume_token(COMMA);
				tmp = Coordinates();
				coords[0] = tmp[0]; coords[1] = tmp[1];
				jj_consume_token(COMMA);
				tmp = Coordinates();
				coords[2] = tmp[0]; coords[3] = tmp[1]; tmp = new ADQLOperand[0];
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case COMMA:
					jj_consume_token(COMMA);
					tmp = Coordinates();
					break;
				default:
					jj_la1[100] = jj_gen;
					;
				}
				jj_consume_token(RIGHT_PAR);
				if (tmp.length == 0) gf = buildTools.createPolygon(coordSys, coords[0], coords[1], coords[2], coords[3]);
				else gf = buildTools.createPolygon(coordSys, coords[0], coords[1], coords[2], coords[3], tmp[0], tmp[1]);
				break;
			case REGION:
				jj_consume_token(REGION);
				jj_consume_token(LEFT_PAR);
				op = StringExpression();
				jj_consume_token(RIGHT_PAR);
				gf = buildTools.createRegion(op);
				break;
			default:
				jj_la1[101] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return gf;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("GeometryValueFunction");
		}
	}

	final public PointFunction Point() throws ParseException {
		trace_call("Point");
		try {
			ADQLOperand coordSys; ADQLOperand[] coords;
			jj_consume_token(POINT);
			jj_consume_token(LEFT_PAR);
			coordSys = StringExpression();
			jj_consume_token(COMMA);
			coords = Coordinates();
			jj_consume_token(RIGHT_PAR);
			{if (true) return buildTools.createPoint(coordSys, coords[0], coords[1]);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("Point");
		}
	}

	final public GeometryFunction ExtractCoordSys() throws ParseException {
		trace_call("ExtractCoordSys");
		try {
			ADQLOperand param;
			jj_consume_token(COORDSYS);
			jj_consume_token(LEFT_PAR);
			param = GeometryExpression();
			jj_consume_token(RIGHT_PAR);
			{if (true) return buildTools.createExtractCoordSys(param);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("ExtractCoordSys");
		}
	}

	/* ***************** */
	/* NUMERIC FUNCTIONS */
	/* ***************** */
	final public ADQLFunction NumericFunction() throws ParseException {
		trace_call("NumericFunction");
		try {
			ADQLFunction fct;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ABS:
			case CEILING:
			case DEGREES:
			case EXP:
			case FLOOR:
			case LOG:
			case LOG10:
			case MOD:
			case PI:
			case POWER:
			case RADIANS:
			case RAND:
			case ROUND:
			case SQRT:
			case TRUNCATE:
				fct = MathFunction();
				break;
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case COS:
			case COT:
			case SIN:
			case TAN:
				fct = TrigFunction();
				break;
			case CONTAINS:
			case INTERSECTS:
			case AREA:
			case COORD1:
			case COORD2:
			case DISTANCE:
				fct = GeometryFunction();
				break;
			case REGULAR_IDENTIFIER:
				fct = UserDefinedFunction();
				break;
			default:
				jj_la1[102] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			{if (true) return fct;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("NumericFunction");
		}
	}

	final public MathFunction MathFunction() throws ParseException {
		trace_call("MathFunction");
		try {
			Token fct=null; ADQLOperand param1=null, param2=null; String integerValue = null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ABS:
				fct = jj_consume_token(ABS);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case CEILING:
				fct = jj_consume_token(CEILING);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case DEGREES:
				fct = jj_consume_token(DEGREES);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case EXP:
				fct = jj_consume_token(EXP);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case FLOOR:
				fct = jj_consume_token(FLOOR);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case LOG:
				fct = jj_consume_token(LOG);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case LOG10:
				fct = jj_consume_token(LOG10);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case MOD:
				fct = jj_consume_token(MOD);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(COMMA);
				param2 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case PI:
				fct = jj_consume_token(PI);
				jj_consume_token(LEFT_PAR);
				jj_consume_token(RIGHT_PAR);
				break;
			case POWER:
				fct = jj_consume_token(POWER);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(COMMA);
				param2 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case RADIANS:
				fct = jj_consume_token(RADIANS);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case RAND:
				fct = jj_consume_token(RAND);
				jj_consume_token(LEFT_PAR);
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case LEFT_PAR:
				case PLUS:
				case MINUS:
				case AVG:
				case MAX:
				case MIN:
				case SUM:
				case COUNT:
				case CONTAINS:
				case INTERSECTS:
				case AREA:
				case COORD1:
				case COORD2:
				case DISTANCE:
				case ABS:
				case CEILING:
				case DEGREES:
				case EXP:
				case FLOOR:
				case LOG:
				case LOG10:
				case MOD:
				case PI:
				case POWER:
				case RADIANS:
				case RAND:
				case ROUND:
				case SQRT:
				case TRUNCATE:
				case ACOS:
				case ASIN:
				case ATAN:
				case ATAN2:
				case COS:
				case COT:
				case SIN:
				case TAN:
				case STRING_LITERAL:
				case DELIMITED_IDENTIFIER:
				case REGULAR_IDENTIFIER:
				case SCIENTIFIC_NUMBER:
				case UNSIGNED_FLOAT:
				case UNSIGNED_INTEGER:
					param1 = NumericExpression();
					break;
				default:
					jj_la1[103] = jj_gen;
					;
				}
				jj_consume_token(RIGHT_PAR);
				break;
			case ROUND:
				fct = jj_consume_token(ROUND);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case COMMA:
					jj_consume_token(COMMA);
					integerValue = SignedInteger();
					param2 = buildTools.createOperation(buildTools.createConstant(integerValue, ADQLType.INTEGER), null, null);
					break;
				default:
					jj_la1[104] = jj_gen;
					;
				}
				jj_consume_token(RIGHT_PAR);
				break;
			case SQRT:
				fct = jj_consume_token(SQRT);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case TRUNCATE:
				fct = jj_consume_token(TRUNCATE);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
				case COMMA:
					jj_consume_token(COMMA);
					integerValue = SignedInteger();
					param2 = buildTools.createOperation(buildTools.createConstant(integerValue, ADQLType.INTEGER), null, null);
					break;
				default:
					jj_la1[105] = jj_gen;
					;
				}
				jj_consume_token(RIGHT_PAR);
				break;
			default:
				jj_la1[106] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			if (param1 != null)
			{if (true) return buildTools.createMathFunction(MathFunctionType.valueOf(fct.image.toUpperCase()), param1, param2);}
			else
			{if (true) return null;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("MathFunction");
		}
	}

	final public MathFunction TrigFunction() throws ParseException {
		trace_call("TrigFunction");
		try {
			Token fct=null; ADQLOperand param1=null, param2=null;
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case ACOS:
				fct = jj_consume_token(ACOS);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case ASIN:
				fct = jj_consume_token(ASIN);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case ATAN:
				fct = jj_consume_token(ATAN);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case ATAN2:
				fct = jj_consume_token(ATAN2);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(COMMA);
				param2 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case COS:
				fct = jj_consume_token(COS);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case COT:
				fct = jj_consume_token(COT);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case SIN:
				fct = jj_consume_token(SIN);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			case TAN:
				fct = jj_consume_token(TAN);
				jj_consume_token(LEFT_PAR);
				param1 = NumericExpression();
				jj_consume_token(RIGHT_PAR);
				break;
			default:
				jj_la1[107] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			if (param1 != null)
			{if (true) return buildTools.createMathFunction(MathFunctionType.valueOf(fct.image.toUpperCase()), param1, param2);}
			else
			{if (true) return null;}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("TrigFunction");
		}
	}

	/* /!\ WARNING: The function name may be prefixed by "udf_" but there is no way to check it here ! */
	final public UserFunction UserDefinedFunction() throws ParseException {
		trace_call("UserDefinedFunction");
		try {
			Token fct; ArrayList<ADQLOperand> params = new ArrayList<ADQLOperand>(); ADQLOperand op;
			fct = jj_consume_token(REGULAR_IDENTIFIER);
			jj_consume_token(LEFT_PAR);
			switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
			case LEFT_PAR:
			case PLUS:
			case MINUS:
			case AVG:
			case MAX:
			case MIN:
			case SUM:
			case COUNT:
			case BOX:
			case CENTROID:
			case CIRCLE:
			case POINT:
			case POLYGON:
			case REGION:
			case CONTAINS:
			case INTERSECTS:
			case AREA:
			case COORD1:
			case COORD2:
			case COORDSYS:
			case DISTANCE:
			case ABS:
			case CEILING:
			case DEGREES:
			case EXP:
			case FLOOR:
			case LOG:
			case LOG10:
			case MOD:
			case PI:
			case POWER:
			case RADIANS:
			case RAND:
			case ROUND:
			case SQRT:
			case TRUNCATE:
			case ACOS:
			case ASIN:
			case ATAN:
			case ATAN2:
			case COS:
			case COT:
			case SIN:
			case TAN:
			case STRING_LITERAL:
			case DELIMITED_IDENTIFIER:
			case REGULAR_IDENTIFIER:
			case SCIENTIFIC_NUMBER:
			case UNSIGNED_FLOAT:
			case UNSIGNED_INTEGER:
				op = ValueExpression();
				params.add(op);
				label_8:
					while (true) {
						switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
						case COMMA:
							;
							break;
						default:
							jj_la1[108] = jj_gen;
							break label_8;
						}
						jj_consume_token(COMMA);
						op = ValueExpression();
						params.add(op);
					}
				break;
			default:
				jj_la1[109] = jj_gen;
				;
			}
			jj_consume_token(RIGHT_PAR);
			if (debug) System.out.println("\u005c""+fct.image+"\u005c" is an user defined function !");
			ADQLOperand[] parameters = new ADQLOperand[params.size()];
			for(int i=0; i<params.size(); i++)
				parameters[i] = params.get(i);
			{if (true) return buildTools.createUserFunction(fct.image, parameters);}
			throw new Error("Missing return statement in function");
		} finally {
			trace_return("UserDefinedFunction");
		}
	}

	private boolean jj_2_1(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_1(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(0, xla); }
	}

	private boolean jj_2_2(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_2(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(1, xla); }
	}

	private boolean jj_2_3(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_3(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(2, xla); }
	}

	private boolean jj_2_4(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_4(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(3, xla); }
	}

	private boolean jj_2_5(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_5(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(4, xla); }
	}

	private boolean jj_2_6(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_6(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(5, xla); }
	}

	private boolean jj_2_7(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_7(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(6, xla); }
	}

	private boolean jj_2_8(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_8(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(7, xla); }
	}

	private boolean jj_2_9(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_9(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(8, xla); }
	}

	private boolean jj_2_10(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_10(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(9, xla); }
	}

	private boolean jj_2_11(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_11(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(10, xla); }
	}

	private boolean jj_2_12(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_12(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(11, xla); }
	}

	private boolean jj_2_13(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_13(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(12, xla); }
	}

	private boolean jj_2_14(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_14(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(13, xla); }
	}

	private boolean jj_2_15(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_15(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(14, xla); }
	}

	private boolean jj_2_16(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_16(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(15, xla); }
	}

	private boolean jj_2_17(int xla) {
		jj_la = xla; jj_lastpos = jj_scanpos = token;
		try { return !jj_3_17(); }
		catch(LookaheadSuccess ls) { return true; }
		finally { jj_save(16, xla); }
	}

	private boolean jj_3R_87() {
		if (jj_scan_token(ATAN)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_111() {
		if (jj_scan_token(FROM)) return true;
		if (jj_3R_130()) return true;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_131()) { jj_scanpos = xsp; break; }
		}
		return false;
	}

	private boolean jj_3R_86() {
		if (jj_scan_token(ASIN)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_85() {
		if (jj_scan_token(ACOS)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_156() {
		if (jj_3R_43()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_165()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_52() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_85()) {
			jj_scanpos = xsp;
			if (jj_3R_86()) {
				jj_scanpos = xsp;
				if (jj_3R_87()) {
					jj_scanpos = xsp;
					if (jj_3R_88()) {
						jj_scanpos = xsp;
						if (jj_3R_89()) {
							jj_scanpos = xsp;
							if (jj_3R_90()) {
								jj_scanpos = xsp;
								if (jj_3R_91()) {
									jj_scanpos = xsp;
									if (jj_3R_92()) return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3_1() {
		if (jj_3R_9()) return true;
		if (jj_scan_token(DOT)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_10()) jj_scanpos = xsp;
		if (jj_scan_token(ASTERISK)) return true;
		return false;
	}

	private boolean jj_3R_141() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_1()) {
			jj_scanpos = xsp;
			if (jj_3R_156()) return true;
		}
		return false;
	}

	private boolean jj_3R_84() {
		if (jj_scan_token(TRUNCATE)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_182()) jj_scanpos = xsp;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_83() {
		if (jj_scan_token(SQRT)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_82() {
		if (jj_scan_token(ROUND)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_181()) jj_scanpos = xsp;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_81() {
		if (jj_scan_token(RAND)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_180()) jj_scanpos = xsp;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_80() {
		if (jj_scan_token(RADIANS)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_79() {
		if (jj_scan_token(POWER)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_78() {
		if (jj_scan_token(PI)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_77() {
		if (jj_scan_token(MOD)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_128() {
		if (jj_scan_token(TOP)) return true;
		if (jj_scan_token(UNSIGNED_INTEGER)) return true;
		return false;
	}

	private boolean jj_3R_76() {
		if (jj_scan_token(LOG10)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_127() {
		if (jj_scan_token(QUANTIFIER)) return true;
		return false;
	}

	private boolean jj_3R_75() {
		if (jj_scan_token(LOG)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_74() {
		if (jj_scan_token(FLOOR)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_61() {
		if (jj_scan_token(SELECT)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_127()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_128()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(10)) {
			jj_scanpos = xsp;
			if (jj_3R_129()) return true;
		}
		return false;
	}

	private boolean jj_3R_73() {
		if (jj_scan_token(EXP)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_183() {
		if (jj_3R_138()) return true;
		return false;
	}

	private boolean jj_3R_72() {
		if (jj_scan_token(DEGREES)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_71() {
		if (jj_scan_token(CEILING)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_70() {
		if (jj_scan_token(ABS)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_26() {
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_46()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_51() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_70()) {
			jj_scanpos = xsp;
			if (jj_3R_71()) {
				jj_scanpos = xsp;
				if (jj_3R_72()) {
					jj_scanpos = xsp;
					if (jj_3R_73()) {
						jj_scanpos = xsp;
						if (jj_3R_74()) {
							jj_scanpos = xsp;
							if (jj_3R_75()) {
								jj_scanpos = xsp;
								if (jj_3R_76()) {
									jj_scanpos = xsp;
									if (jj_3R_77()) {
										jj_scanpos = xsp;
										if (jj_3R_78()) {
											jj_scanpos = xsp;
											if (jj_3R_79()) {
												jj_scanpos = xsp;
												if (jj_3R_80()) {
													jj_scanpos = xsp;
													if (jj_3R_81()) {
														jj_scanpos = xsp;
														if (jj_3R_82()) {
															jj_scanpos = xsp;
															if (jj_3R_83()) {
																jj_scanpos = xsp;
																if (jj_3R_84()) return true;
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_38() {
		if (jj_3R_21()) return true;
		return false;
	}

	private boolean jj_3R_37() {
		if (jj_3R_53()) return true;
		return false;
	}

	private boolean jj_3R_36() {
		if (jj_3R_52()) return true;
		return false;
	}

	private boolean jj_3R_35() {
		if (jj_3R_51()) return true;
		return false;
	}

	private boolean jj_3R_20() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_35()) {
			jj_scanpos = xsp;
			if (jj_3R_36()) {
				jj_scanpos = xsp;
				if (jj_3R_37()) {
					jj_scanpos = xsp;
					if (jj_3R_38()) return true;
				}
			}
		}
		return false;
	}

	private boolean jj_3R_115() {
		if (jj_3R_135()) return true;
		return false;
	}

	private boolean jj_3R_152() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_151()) return true;
		return false;
	}

	private boolean jj_3R_114() {
		if (jj_3R_134()) return true;
		return false;
	}

	private boolean jj_3R_113() {
		if (jj_3R_133()) return true;
		return false;
	}

	private boolean jj_3R_140() {
		if (jj_3R_43()) return true;
		return false;
	}

	private boolean jj_3R_112() {
		if (jj_3R_132()) return true;
		return false;
	}

	private boolean jj_3R_49() {
		if (jj_scan_token(COORDSYS)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_63()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_46() {
		if (jj_3R_61()) return true;
		if (jj_3R_111()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_112()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_113()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_114()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_115()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_138() {
		if (jj_scan_token(POINT)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_25()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_151()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_122() {
		if (jj_scan_token(REGION)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_25()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_121() {
		if (jj_scan_token(POLYGON)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_25()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_151()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_151()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_152()) jj_scanpos = xsp;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_120() {
		if (jj_3R_138()) return true;
		return false;
	}

	private boolean jj_3R_110() {
		if (jj_scan_token(COORD2)) return true;
		return false;
	}

	private boolean jj_3R_119() {
		if (jj_scan_token(CIRCLE)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_25()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_151()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_118() {
		if (jj_scan_token(CENTROID)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_63()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_137() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_43()) return true;
		return false;
	}

	private boolean jj_3R_117() {
		if (jj_scan_token(BOX)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_25()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_151()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_99() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_117()) {
			jj_scanpos = xsp;
			if (jj_3R_118()) {
				jj_scanpos = xsp;
				if (jj_3R_119()) {
					jj_scanpos = xsp;
					if (jj_3R_120()) {
						jj_scanpos = xsp;
						if (jj_3R_121()) {
							jj_scanpos = xsp;
							if (jj_3R_122()) return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_96() {
		if (jj_scan_token(DISTANCE)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_138()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_138()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_95() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(61)) {
			jj_scanpos = xsp;
			if (jj_3R_110()) return true;
		}
		if (jj_scan_token(LEFT_PAR)) return true;
		xsp = jj_scanpos;
		if (jj_3R_183()) {
			jj_scanpos = xsp;
			if (jj_3R_184()) return true;
		}
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_94() {
		if (jj_scan_token(AREA)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_63()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_93() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(58)) {
			jj_scanpos = xsp;
			if (jj_scan_token(59)) return true;
		}
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_63()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_63()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_53() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_93()) {
			jj_scanpos = xsp;
			if (jj_3R_94()) {
				jj_scanpos = xsp;
				if (jj_3R_95()) {
					jj_scanpos = xsp;
					if (jj_3R_96()) return true;
				}
			}
		}
		return false;
	}

	private boolean jj_3R_151() {
		if (jj_3R_100()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		return false;
	}

	private boolean jj_3R_166() {
		if (jj_3R_32()) return true;
		return false;
	}

	private boolean jj_3R_173() {
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_144()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_126() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(47)) {
			jj_scanpos = xsp;
			if (jj_scan_token(48)) {
				jj_scanpos = xsp;
				if (jj_scan_token(49)) {
					jj_scanpos = xsp;
					if (jj_scan_token(50)) return true;
				}
			}
		}
		if (jj_scan_token(LEFT_PAR)) return true;
		xsp = jj_scanpos;
		if (jj_scan_token(19)) jj_scanpos = xsp;
		if (jj_3R_43()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_125() {
		if (jj_scan_token(COUNT)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(19)) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(10)) {
			jj_scanpos = xsp;
			if (jj_3R_140()) return true;
		}
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_109() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_125()) {
			jj_scanpos = xsp;
			if (jj_3R_126()) return true;
		}
		return false;
	}

	private boolean jj_3R_158() {
		if (jj_3R_32()) return true;
		return false;
	}

	private boolean jj_3R_116() {
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_43()) return true;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_137()) { jj_scanpos = xsp; break; }
		}
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3_17() {
		if (jj_3R_26()) return true;
		return false;
	}

	private boolean jj_3R_98() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(35)) jj_scanpos = xsp;
		if (jj_scan_token(IN)) return true;
		xsp = jj_scanpos;
		if (jj_3_17()) {
			jj_scanpos = xsp;
			if (jj_3R_116()) return true;
		}
		return false;
	}

	private boolean jj_3_13() {
		if (jj_3R_22()) return true;
		return false;
	}

	private boolean jj_3R_23() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(35)) jj_scanpos = xsp;
		if (jj_scan_token(BETWEEN)) return true;
		if (jj_3R_43()) return true;
		if (jj_scan_token(AND)) return true;
		if (jj_3R_43()) return true;
		return false;
	}

	private boolean jj_3R_55() {
		if (jj_3R_98()) return true;
		return false;
	}

	private boolean jj_3_14() {
		if (jj_3R_23()) return true;
		return false;
	}

	private boolean jj_3R_97() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(12)) {
			jj_scanpos = xsp;
			if (jj_scan_token(13)) {
				jj_scanpos = xsp;
				if (jj_scan_token(14)) {
					jj_scanpos = xsp;
					if (jj_scan_token(15)) {
						jj_scanpos = xsp;
						if (jj_scan_token(16)) {
							jj_scanpos = xsp;
							if (jj_scan_token(17)) return true;
						}
					}
				}
			}
		}
		if (jj_3R_43()) return true;
		return false;
	}

	private boolean jj_3_16() {
		if (jj_3R_25()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(35)) jj_scanpos = xsp;
		if (jj_scan_token(LIKE)) return true;
		return false;
	}

	private boolean jj_3R_172() {
		if (jj_3R_22()) return true;
		return false;
	}

	private boolean jj_3R_54() {
		if (jj_3R_97()) return true;
		return false;
	}

	private boolean jj_3_15() {
		if (jj_3R_24()) return true;
		if (jj_scan_token(IS)) return true;
		return false;
	}

	private boolean jj_3R_168() {
		if (jj_scan_token(AND)) return true;
		if (jj_3R_159()) return true;
		return false;
	}

	private boolean jj_3R_42() {
		if (jj_3R_43()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_54()) {
			jj_scanpos = xsp;
			if (jj_3_14()) {
				jj_scanpos = xsp;
				if (jj_3R_55()) return true;
			}
		}
		return false;
	}

	private boolean jj_3R_41() {
		if (jj_3R_25()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(35)) jj_scanpos = xsp;
		if (jj_scan_token(LIKE)) return true;
		if (jj_3R_25()) return true;
		return false;
	}

	private boolean jj_3R_160() {
		if (jj_scan_token(OR)) return true;
		if (jj_3R_144()) return true;
		return false;
	}

	private boolean jj_3R_40() {
		if (jj_3R_24()) return true;
		if (jj_scan_token(IS)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(35)) jj_scanpos = xsp;
		if (jj_scan_token(NULL)) return true;
		return false;
	}

	private boolean jj_3R_105() {
		if (jj_3R_99()) return true;
		return false;
	}

	private boolean jj_3R_39() {
		if (jj_scan_token(EXISTS)) return true;
		if (jj_3R_26()) return true;
		return false;
	}

	private boolean jj_3R_22() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_39()) {
			jj_scanpos = xsp;
			if (jj_3R_40()) {
				jj_scanpos = xsp;
				if (jj_3R_41()) {
					jj_scanpos = xsp;
					if (jj_3R_42()) return true;
				}
			}
		}
		return false;
	}

	private boolean jj_3R_66() {
		if (jj_3R_108()) return true;
		return false;
	}

	private boolean jj_3R_171() {
		if (jj_scan_token(NOT)) return true;
		return false;
	}

	private boolean jj_3R_167() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_171()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_172()) {
			jj_scanpos = xsp;
			if (jj_3R_173()) return true;
		}
		return false;
	}

	private boolean jj_3R_159() {
		if (jj_3R_167()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_168()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_155() {
		if (jj_3R_50()) return true;
		return false;
	}

	private boolean jj_3R_144() {
		if (jj_3R_159()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_160()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_45() {
		if (jj_scan_token(CONCAT)) return true;
		if (jj_3R_25()) return true;
		return false;
	}

	private boolean jj_3R_18() {
		if (jj_3R_26()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		xsp = jj_scanpos;
		if (jj_3R_166()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_104() {
		if (jj_3R_50()) return true;
		return false;
	}

	private boolean jj_3R_63() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_104()) {
			jj_scanpos = xsp;
			if (jj_3R_105()) return true;
		}
		return false;
	}

	private boolean jj_3R_34() {
		if (jj_3R_50()) return true;
		return false;
	}

	private boolean jj_3_12() {
		if (jj_3R_21()) return true;
		return false;
	}

	private boolean jj_3R_103() {
		if (jj_scan_token(FULL)) return true;
		return false;
	}

	private boolean jj_3R_33() {
		if (jj_3R_49()) return true;
		return false;
	}

	private boolean jj_3R_19() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_33()) {
			jj_scanpos = xsp;
			if (jj_3_12()) {
				jj_scanpos = xsp;
				if (jj_3R_34()) return true;
			}
		}
		return false;
	}

	private boolean jj_3R_157() {
		if (jj_3R_26()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_25() {
		if (jj_3R_19()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_45()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_153() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(10)) {
			jj_scanpos = xsp;
			if (jj_scan_token(11)) return true;
		}
		if (jj_3R_123()) return true;
		return false;
	}

	private boolean jj_3R_136() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(8)) {
			jj_scanpos = xsp;
			if (jj_scan_token(9)) return true;
		}
		if (jj_3R_100()) return true;
		return false;
	}

	private boolean jj_3R_154() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(8)) {
			jj_scanpos = xsp;
			if (jj_scan_token(9)) return true;
		}
		return false;
	}

	private boolean jj_3_11() {
		if (jj_3R_20()) return true;
		return false;
	}

	private boolean jj_3R_139() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_154()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3_11()) {
			jj_scanpos = xsp;
			if (jj_3R_155()) return true;
		}
		return false;
	}

	private boolean jj_3R_16() {
		if (jj_3R_32()) return true;
		return false;
	}

	private boolean jj_3R_179() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_123() {
		if (jj_3R_139()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_153()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3_10() {
		if (jj_3R_19()) return true;
		if (jj_scan_token(CONCAT)) return true;
		return false;
	}

	private boolean jj_3_9() {
		if (jj_scan_token(COORDSYS)) return true;
		return false;
	}

	private boolean jj_3_8() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(8)) {
			jj_scanpos = xsp;
			if (jj_scan_token(9)) return true;
		}
		return false;
	}

	private boolean jj_3R_102() {
		if (jj_scan_token(RIGHT)) return true;
		return false;
	}

	private boolean jj_3R_100() {
		if (jj_3R_123()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_136()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_60() {
		if (jj_3R_100()) return true;
		return false;
	}

	private boolean jj_3R_59() {
		if (jj_3R_25()) return true;
		return false;
	}

	private boolean jj_3R_58() {
		if (jj_3R_25()) return true;
		return false;
	}

	private boolean jj_3R_57() {
		if (jj_3R_100()) return true;
		return false;
	}

	private boolean jj_3R_56() {
		if (jj_3R_99()) return true;
		return false;
	}

	private boolean jj_3R_43() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_56()) {
			jj_scanpos = xsp;
			if (jj_3R_57()) {
				jj_scanpos = xsp;
				if (jj_3R_58()) {
					jj_scanpos = xsp;
					if (jj_3R_59()) {
						jj_scanpos = xsp;
						if (jj_3R_60()) return true;
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_69() {
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_43()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_68() {
		if (jj_3R_109()) return true;
		return false;
	}

	private boolean jj_3R_176() {
		if (jj_3R_26()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_67() {
		if (jj_3R_24()) return true;
		return false;
	}

	private boolean jj_3R_65() {
		if (jj_3R_107()) return true;
		return false;
	}

	private boolean jj_3R_50() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_65()) {
			jj_scanpos = xsp;
			if (jj_3R_66()) {
				jj_scanpos = xsp;
				if (jj_3R_67()) {
					jj_scanpos = xsp;
					if (jj_3R_68()) {
						jj_scanpos = xsp;
						if (jj_3R_69()) return true;
					}
				}
			}
		}
		return false;
	}

	private boolean jj_3R_186() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(8)) {
			jj_scanpos = xsp;
			if (jj_scan_token(9)) return true;
		}
		return false;
	}

	private boolean jj_3R_185() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_186()) jj_scanpos = xsp;
		if (jj_scan_token(UNSIGNED_INTEGER)) return true;
		return false;
	}

	private boolean jj_3R_14() {
		if (jj_3R_26()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_101() {
		if (jj_scan_token(LEFT)) return true;
		return false;
	}

	private boolean jj_3R_17() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_62() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_101()) {
			jj_scanpos = xsp;
			if (jj_3R_102()) {
				jj_scanpos = xsp;
				if (jj_3R_103()) return true;
			}
		}
		xsp = jj_scanpos;
		if (jj_scan_token(25)) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_31() {
		if (jj_scan_token(DOT)) return true;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_107() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(99)) {
			jj_scanpos = xsp;
			if (jj_scan_token(100)) {
				jj_scanpos = xsp;
				if (jj_scan_token(101)) return true;
			}
		}
		return false;
	}

	private boolean jj_3R_13() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_48() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(24)) {
			jj_scanpos = xsp;
			if (jj_3R_62()) return true;
		}
		return false;
	}

	private boolean jj_3R_124() {
		if (jj_scan_token(STRING_LITERAL)) return true;
		return false;
	}

	private boolean jj_3R_15() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_108() {
		Token xsp;
		if (jj_3R_124()) return true;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_124()) { jj_scanpos = xsp; break; }
		}
		return false;
	}

	private boolean jj_3R_106() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_43()) return true;
		return false;
	}

	private boolean jj_3R_178() {
		if (jj_scan_token(USING)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_9()) return true;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_179()) { jj_scanpos = xsp; break; }
		}
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_177() {
		if (jj_scan_token(ON)) return true;
		if (jj_3R_144()) return true;
		return false;
	}

	private boolean jj_3R_12() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(22)) jj_scanpos = xsp;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_175() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_177()) {
			jj_scanpos = xsp;
			if (jj_3R_178()) return true;
		}
		return false;
	}

	private boolean jj_3R_164() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(45)) {
			jj_scanpos = xsp;
			if (jj_scan_token(46)) return true;
		}
		return false;
	}

	private boolean jj_3R_28() {
		if (jj_scan_token(DELIMITED_IDENTIFIER)) return true;
		return false;
	}

	private boolean jj_3_6() {
		if (jj_3R_11()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_17()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_149() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_scan_token(45)) {
			jj_scanpos = xsp;
			if (jj_scan_token(46)) return true;
		}
		return false;
	}

	private boolean jj_3R_29() {
		if (jj_3R_9()) return true;
		if (jj_scan_token(DOT)) return true;
		return false;
	}

	private boolean jj_3R_170() {
		if (jj_3R_175()) return true;
		return false;
	}

	private boolean jj_3R_47() {
		if (jj_scan_token(NATURAL)) return true;
		return false;
	}

	private boolean jj_3R_32() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_47()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_48()) jj_scanpos = xsp;
		if (jj_scan_token(JOIN)) return true;
		if (jj_3R_169()) return true;
		xsp = jj_scanpos;
		if (jj_3R_170()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3_3() {
		if (jj_3R_11()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_13()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_44() {
		if (jj_scan_token(DOT)) return true;
		if (jj_3R_11()) return true;
		return false;
	}

	private boolean jj_3R_143() {
		if (jj_scan_token(LEFT_PAR)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_6()) {
			jj_scanpos = xsp;
			if (jj_3R_157()) return true;
		}
		xsp = jj_scanpos;
		if (jj_3R_158()) jj_scanpos = xsp;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_30() {
		if (jj_scan_token(DOT)) return true;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3_5() {
		if (jj_3R_11()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_15()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_16()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3_7() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_5()) {
			jj_scanpos = xsp;
			if (jj_3R_18()) return true;
		}
		return false;
	}

	private boolean jj_3R_130() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_7()) {
			jj_scanpos = xsp;
			if (jj_3R_143()) return true;
		}
		return false;
	}

	private boolean jj_3R_174() {
		if (jj_scan_token(LEFT_PAR)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_3()) {
			jj_scanpos = xsp;
			if (jj_3R_176()) return true;
		}
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3_2() {
		if (jj_3R_11()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_12()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3_4() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_2()) {
			jj_scanpos = xsp;
			if (jj_3R_14()) return true;
		}
		return false;
	}

	private boolean jj_3R_169() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3_4()) {
			jj_scanpos = xsp;
			if (jj_3R_174()) return true;
		}
		return false;
	}

	private boolean jj_3R_142() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_141()) return true;
		return false;
	}

	private boolean jj_3R_64() {
		if (jj_3R_43()) return true;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_106()) { jj_scanpos = xsp; break; }
		}
		return false;
	}

	private boolean jj_3R_24() {
		if (jj_3R_9()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_44()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_182() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_185()) return true;
		return false;
	}

	private boolean jj_3R_11() {
		if (jj_3R_9()) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_30()) jj_scanpos = xsp;
		xsp = jj_scanpos;
		if (jj_3R_31()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_184() {
		if (jj_3R_24()) return true;
		return false;
	}

	private boolean jj_3R_27() {
		if (jj_scan_token(REGULAR_IDENTIFIER)) return true;
		return false;
	}

	private boolean jj_3R_181() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_185()) return true;
		return false;
	}

	private boolean jj_3R_9() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_27()) {
			jj_scanpos = xsp;
			if (jj_3R_28()) return true;
		}
		return false;
	}

	private boolean jj_3R_10() {
		if (jj_3R_9()) return true;
		if (jj_scan_token(DOT)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_29()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_163() {
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_150() {
		if (jj_scan_token(COMMA)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_163()) {
			jj_scanpos = xsp;
			if (jj_scan_token(101)) return true;
		}
		xsp = jj_scanpos;
		if (jj_3R_164()) jj_scanpos = xsp;
		return false;
	}

	private boolean jj_3R_148() {
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_135() {
		if (jj_scan_token(ORDER_BY)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_148()) {
			jj_scanpos = xsp;
			if (jj_scan_token(101)) return true;
		}
		xsp = jj_scanpos;
		if (jj_3R_149()) jj_scanpos = xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_150()) { jj_scanpos = xsp; break; }
		}
		return false;
	}

	private boolean jj_3R_134() {
		if (jj_scan_token(HAVING)) return true;
		if (jj_3R_144()) return true;
		return false;
	}

	private boolean jj_3R_165() {
		if (jj_scan_token(AS)) return true;
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_162() {
		if (jj_scan_token(UNSIGNED_INTEGER)) return true;
		return false;
	}

	private boolean jj_3R_161() {
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_147() {
		if (jj_scan_token(COMMA)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_161()) {
			jj_scanpos = xsp;
			if (jj_3R_162()) return true;
		}
		return false;
	}

	private boolean jj_3R_146() {
		if (jj_scan_token(UNSIGNED_INTEGER)) return true;
		return false;
	}

	private boolean jj_3R_21() {
		if (jj_scan_token(REGULAR_IDENTIFIER)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_64()) jj_scanpos = xsp;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_145() {
		if (jj_3R_9()) return true;
		return false;
	}

	private boolean jj_3R_133() {
		if (jj_scan_token(GROUP_BY)) return true;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_145()) {
			jj_scanpos = xsp;
			if (jj_3R_146()) return true;
		}
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_147()) { jj_scanpos = xsp; break; }
		}
		return false;
	}

	private boolean jj_3R_180() {
		if (jj_3R_100()) return true;
		return false;
	}

	private boolean jj_3R_132() {
		if (jj_scan_token(WHERE)) return true;
		if (jj_3R_144()) return true;
		return false;
	}

	private boolean jj_3R_92() {
		if (jj_scan_token(TAN)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_91() {
		if (jj_scan_token(SIN)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_129() {
		if (jj_3R_141()) return true;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_142()) { jj_scanpos = xsp; break; }
		}
		return false;
	}

	private boolean jj_3R_90() {
		if (jj_scan_token(COT)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_89() {
		if (jj_scan_token(COS)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	private boolean jj_3R_131() {
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_130()) return true;
		return false;
	}

	private boolean jj_3R_88() {
		if (jj_scan_token(ATAN2)) return true;
		if (jj_scan_token(LEFT_PAR)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(COMMA)) return true;
		if (jj_3R_100()) return true;
		if (jj_scan_token(RIGHT_PAR)) return true;
		return false;
	}

	/** Generated Token Manager. */
	public AdqlParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	/** Current token. */
	public Token token;
	/** Next token. */
	public Token jj_nt;
	private int jj_ntk;
	private Token jj_scanpos, jj_lastpos;
	private int jj_la;
	private int jj_gen;
	final private int[] jj_la1 = new int[110];
	static private int[] jj_la1_0;
	static private int[] jj_la1_1;
	static private int[] jj_la1_2;
	static private int[] jj_la1_3;
	static {
		jj_la1_init_0();
		jj_la1_init_1();
		jj_la1_init_2();
		jj_la1_init_3();
	}
	private static void jj_la1_init_0() {
		jj_la1_0 = new int[] {0x41,0x0,0x0,0x0,0x0,0x80000,0x100000,0x20,0x704,0x0,0x0,0x400000,0x304,0x20,0x0,0x20,0x0,0x0,0x0,0x0,0x20,0x0,0x0,0x0,0x0,0x10,0x10,0x10,0x400000,0x400000,0x400000,0x4,0x400000,0x400000,0x400000,0x4,0x4,0x400000,0x400000,0x3d800000,0x400000,0x3d800000,0x4,0x400000,0x400000,0x400000,0x4,0x3d800000,0x4,0x800000,0x1c000000,0x2000000,0x1d000000,0x1d000000,0xc0000000,0x20,0xc0000000,0x0,0x0,0x0,0x300,0x300,0x4,0x0,0x304,0x300,0x300,0xc00,0xc00,0x300,0x300,0x4,0x80,0x0,0x4,0x4,0x0,0x0,0x0,0x4,0x0,0x0,0x3f000,0x0,0x0,0x304,0x3f000,0x0,0x0,0x20,0x4,0x80000,0x704,0x0,0x80000,0x0,0x0,0x0,0x0,0x0,0x20,0x0,0x0,0x304,0x20,0x20,0x0,0x0,0x20,0x304,};
	}
	private static void jj_la1_init_1() {
		jj_la1_1 = new int[] {0x0,0x1,0x400,0x800,0x1000,0x0,0x0,0x0,0xffff8000,0x0,0x0,0x0,0xffff8000,0x0,0x0,0x0,0x0,0x0,0x6000,0x6000,0x0,0x0,0x6000,0x6000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0xf8000,0x3f00000,0x7c0f8000,0x0,0x0,0x0,0x0,0x0,0x0,0xf8000,0x0,0x80000000,0xf8000,0x3ff8000,0x4,0x2,0x8,0x0,0x8,0x8,0x0,0x108,0x200,0xffff8000,0x0,0x8,0x8,0x0,0x0,0x0,0xffff8000,0x78000,0x0,0xf8000,0xc000000,0x60000000,0x800000,0x7c000000,0x0,0x3f00000,0x7c000000,0x7c0f8000,0x0,0x0,0x0,0x0,0x0,0xffff8000,};
	}
	private static void jj_la1_init_2() {
		jj_la1_2 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x20ffffff,0x0,0x0,0x0,0x20ffffff,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x20000000,0x0,0x0,0x0,0x0,0x20000000,0x0,0x20ffffff,0x0,0x0,0x0,0x0,0x0,0x0,0x20000000,0x0,0x0,0x20000000,0x20000000,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x20ffffff,0x0,0x0,0x0,0x0,0x0,0x0,0x20ffffff,0x0,0x0,0x0,0x0,0x0,0x0,0x1,0x0,0x0,0xffffff,0x20ffffff,0x0,0x0,0xfffe,0xff0000,0x0,0x20ffffff,};
	}
	private static void jj_la1_init_3() {
		jj_la1_3 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x3b,0x3,0x3,0x0,0x3b,0x0,0x23,0x0,0x23,0x23,0x0,0x0,0x0,0x23,0x0,0x0,0x3,0x0,0x0,0x0,0x0,0x3,0x0,0x0,0x0,0x3,0x0,0x0,0x0,0x0,0x3,0x0,0x0,0x0,0x0,0x0,0x3,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x38,0x30,0x0,0x0,0x3b,0x0,0x3b,0x0,0x0,0x0,0x0,0x0,0x0,0x3b,0x0,0x0,0x3b,0x3b,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x3b,0x0,0x0,0x0,0x0,0x0,0x0,0x3b,0x0,0x0,0x0,0x0,0x0,0x3,0x0,0x0,0x0,0x2,0x3b,0x0,0x0,0x0,0x0,0x0,0x3b,};
	}
	final private JJCalls[] jj_2_rtns = new JJCalls[17];
	private boolean jj_rescan = false;
	private int jj_gc = 0;

	/** Constructor with InputStream. */
	public AdqlParser(java.io.InputStream stream) {
		this(stream, null);
	}
	/** Constructor with InputStream and supplied encoding */
	public AdqlParser(java.io.InputStream stream, String encoding) {
		try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
		token_source = new AdqlParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 110; i++) jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
	}

	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream) {
		ReInit(stream, null);
	}
	/** Reinitialise. */
	public void ReInit(java.io.InputStream stream, String encoding) {
		try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 110; i++) jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
	}

	/** Constructor. */
	public AdqlParser(java.io.Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new AdqlParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 110; i++) jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
	}

	/** Reinitialise. */
	public void ReInit(java.io.Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 110; i++) jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
	}

	/** Constructor with generated Token Manager. */
	public AdqlParser(AdqlParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 110; i++) jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
	}

	/** Reinitialise. */
	public void ReInit(AdqlParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 110; i++) jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
	}

	private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null) token = token.next;
		else token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind) {
			jj_gen++;
			if (++jj_gc > 100) {
				jj_gc = 0;
				for (int i = 0; i < jj_2_rtns.length; i++) {
					JJCalls c = jj_2_rtns[i];
					while (c != null) {
						if (c.gen < jj_gen) c.first = null;
						c = c.next;
					}
				}
			}
			trace_token(token, "");
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	static private final class LookaheadSuccess extends java.lang.Error { }
	final private LookaheadSuccess jj_ls = new LookaheadSuccess();
	private boolean jj_scan_token(int kind) {
		if (jj_scanpos == jj_lastpos) {
			jj_la--;
			if (jj_scanpos.next == null) {
				jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
			} else {
				jj_lastpos = jj_scanpos = jj_scanpos.next;
			}
		} else {
			jj_scanpos = jj_scanpos.next;
		}
		if (jj_rescan) {
			int i = 0; Token tok = token;
			while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
			if (tok != null) jj_add_error_token(kind, i);
		}
		if (jj_scanpos.kind != kind) return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
		return false;
	}


	/** Get the next Token. */
	final public Token getNextToken() {
		if (token.next != null) token = token.next;
		else token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		trace_token(token, " (in getNextToken)");
		return token;
	}

	/** Get the specific Token. */
	final public Token getToken(int index) {
		Token t = token;
		for (int i = 0; i < index; i++) {
			if (t.next != null) t = t.next;
			else t = t.next = token_source.getNextToken();
		}
		return t;
	}

	private int jj_ntk() {
		if ((jj_nt=token.next) == null)
			return (jj_ntk = (token.next=token_source.getNextToken()).kind);
		else
			return (jj_ntk = jj_nt.kind);
	}

	private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
	private int[] jj_expentry;
	private int jj_kind = -1;
	private int[] jj_lasttokens = new int[100];
	private int jj_endpos;

	private void jj_add_error_token(int kind, int pos) {
		if (pos >= 100) return;
		if (pos == jj_endpos + 1) {
			jj_lasttokens[jj_endpos++] = kind;
		} else if (jj_endpos != 0) {
			jj_expentry = new int[jj_endpos];
			for (int i = 0; i < jj_endpos; i++) {
				jj_expentry[i] = jj_lasttokens[i];
			}
			jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
				int[] oldentry = (int[])(it.next());
				if (oldentry.length == jj_expentry.length) {
					for (int i = 0; i < jj_expentry.length; i++) {
						if (oldentry[i] != jj_expentry[i]) {
							continue jj_entries_loop;
						}
					}
					jj_expentries.add(jj_expentry);
					break jj_entries_loop;
				}
			}
			if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
		}
	}

	/** Generate ParseException. */
	public ParseException generateParseException() {
		jj_expentries.clear();
		boolean[] la1tokens = new boolean[103];
		if (jj_kind >= 0) {
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (int i = 0; i < 110; i++) {
			if (jj_la1[i] == jj_gen) {
				for (int j = 0; j < 32; j++) {
					if ((jj_la1_0[i] & (1<<j)) != 0) {
						la1tokens[j] = true;
					}
					if ((jj_la1_1[i] & (1<<j)) != 0) {
						la1tokens[32+j] = true;
					}
					if ((jj_la1_2[i] & (1<<j)) != 0) {
						la1tokens[64+j] = true;
					}
					if ((jj_la1_3[i] & (1<<j)) != 0) {
						la1tokens[96+j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 103; i++) {
			if (la1tokens[i]) {
				jj_expentry = new int[1];
				jj_expentry[0] = i;
				jj_expentries.add(jj_expentry);
			}
		}
		jj_endpos = 0;
		jj_rescan_token();
		jj_add_error_token(0, 0);
		int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i = 0; i < jj_expentries.size(); i++) {
			exptokseq[i] = jj_expentries.get(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	private int trace_indent = 0;
	private boolean trace_enabled = false;

	/** Enable tracing. */
	final public void enable_tracing() {
		trace_enabled = true;
	}

	/** Disable tracing. */
	final public void disable_tracing() {
		trace_enabled = false;
	}

	private void trace_call(String s) {
		if (trace_enabled) {
			for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
			System.out.println("Call:   " + s);
		}
		trace_indent = trace_indent + 2;
	}

	private void trace_return(String s) {
		trace_indent = trace_indent - 2;
		if (trace_enabled) {
			for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
			System.out.println("Return: " + s);
		}
	}

	private void trace_token(Token t, String where) {
		if (trace_enabled) {
			for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
			System.out.print("Consumed token: <" + tokenImage[t.kind]);
			if (t.kind != 0 && !tokenImage[t.kind].equals("\"" + t.image + "\"")) {
				System.out.print(": \"" + t.image + "\"");
			}
			System.out.println(" at line " + t.beginLine + " column " + t.beginColumn + ">" + where);
		}
	}

	private void trace_scan(Token t1, int t2) {
		if (trace_enabled) {
			for (int i = 0; i < trace_indent; i++) { System.out.print(" "); }
			System.out.print("Visited token: <" + tokenImage[t1.kind]);
			if (t1.kind != 0 && !tokenImage[t1.kind].equals("\"" + t1.image + "\"")) {
				System.out.print(": \"" + t1.image + "\"");
			}
			System.out.println(" at line " + t1.beginLine + " column " + t1.beginColumn + ">; Expected token: <" + tokenImage[t2] + ">");
		}
	}

	private void jj_rescan_token() {
		jj_rescan = true;
		for (int i = 0; i < 17; i++) {
			try {
				JJCalls p = jj_2_rtns[i];
				do {
					if (p.gen > jj_gen) {
						jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
						switch (i) {
						case 0: jj_3_1(); break;
						case 1: jj_3_2(); break;
						case 2: jj_3_3(); break;
						case 3: jj_3_4(); break;
						case 4: jj_3_5(); break;
						case 5: jj_3_6(); break;
						case 6: jj_3_7(); break;
						case 7: jj_3_8(); break;
						case 8: jj_3_9(); break;
						case 9: jj_3_10(); break;
						case 10: jj_3_11(); break;
						case 11: jj_3_12(); break;
						case 12: jj_3_13(); break;
						case 13: jj_3_14(); break;
						case 14: jj_3_15(); break;
						case 15: jj_3_16(); break;
						case 16: jj_3_17(); break;
						}
					}
					p = p.next;
				} while (p != null);
			} catch(LookaheadSuccess ls) { }
		}
		jj_rescan = false;
	}

	private void jj_save(int index, int xla) {
		JJCalls p = jj_2_rtns[index];
		while (p.gen > jj_gen) {
			if (p.next == null) { p = p.next = new JJCalls(); break; }
			p = p.next;
		}
		p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
	}

	static final class JJCalls {
		int gen;
		Token first;
		int arg;
		JJCalls next;
	}

}
