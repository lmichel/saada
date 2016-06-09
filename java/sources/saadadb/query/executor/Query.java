package saadadb.query.executor;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.meta.VOResource;
import saadadb.query.constbuilders.MatchPattern;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.query.constbuilders.UCDField;
import saadadb.query.constbuilders.UTypeField;
import saadadb.query.constbuilders.WhereRelation;
import saadadb.query.matchpattern.CounterpartSelector;
import saadadb.query.matchpattern.EPOnePattern;
import saadadb.query.matchpattern.PatternSQLBuilder;
import saadadb.query.matchpattern.Qualif;
import saadadb.query.matchpattern.Qualifier;
import saadadb.query.merger.Merger;
import saadadb.query.parser.AttributeFilter;
import saadadb.query.parser.Limit;
import saadadb.query.parser.OrderBy;
import saadadb.query.parser.SelectFromIn;
import saadadb.query.parser.Utils;
import saadadb.query.parser.WhereAttributeSaada;
import saadadb.query.parser.WhereDM;
import saadadb.query.parser.WherePosition;
import saadadb.query.parser.WhereUCD;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.relationship.KeyIndex;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.util.TimeSaada;

/**
 * @author laurent
 * 05/2011: add method getUCDColumns
 * * @version $Id$

 */
public class Query extends Query_Report{
	private SelectFromIn        sfiClause;        
	private WhereAttributeSaada wasClause;
	private WherePosition       wpClause;
	private WhereUCD			wuClause;
	private WhereDM             wdmClause ;
	private WhereRelation       wrClause;	
	private Limit               limitClause ;
	private OrderBy             obClause;
	private AttributeFilter      afClause;

	private String query_string;
	private long index_owner_key;
	private Merger merger;
	private HashMap<String, CounterpartSelector> matchingCounterpartQueries;
	/*
	 * Current DM used by queries using WhereDM
	 */
	private VOResource vor;

	/**
	 * 
	 */
	public Query() {
		super(null);
	}

	/**
	 * @param query_string
	 */
	public Query(String query_string) {
		super(null);
		this.query_string = query_string;
	}

	/**
	 * Returns the query execution report
	 * @return
	 */
	public final String explain() {
		return this.report.getReport();
	}
	/**
	 * @return
	 */
	public SelectFromIn getSfiClause() {
		return sfiClause;
	}

	/**
	 * @return
	 */
	public WherePosition getWpClause() {
		return wpClause;
	}

	/**
	 * @return
	 */
	public WhereUCD getWuClause() {
		return wuClause;
	}

	/**
	 * @return
	 */
	public Limit getLimitClause() {
		return limitClause;
	}

	/**
	 * @return
	 */
	public String getQuery_string() {
		return query_string;
	}

	/**
	 * @param dm_name
	 * @throws Exception
	 */
	public void setDM(String dm_name) throws Exception {
		this.vor = Database.getCachemeta().getVOResource(dm_name);
	}

	/**
	 * @return
	 */
	public VOResource getDM() {
		return this.vor;
	}	

	/**
	 * @return
	 */
	public HashMap<String, CounterpartSelector> getMatchingCounterpartQueries() {
		return matchingCounterpartQueries;
	}

	/**
	 * Returns an SQL constraints selecting from oids all collections listClass belong to
	 * @param tabColName
	 * @param listClass
	 * @return
	 * @throws SaadaException
	 */
	public static final String getCollectionConstraintOnClass(String tabColName,String[] listClass) throws SaadaException {
		StringBuffer collConstr = new StringBuffer();
		int idClass;
		for (String className : listClass){
			if (( idClass = Database.getCachemeta().getClass(className).getId() ) == 0) {
				QueryException.throwNewException(SaadaException.METADATA_ERROR, "The Classe " + className + " is unknowed! Id return = 0!");
			}
			//collConstr.append(tabColName).append(".class_id_csa=").append(String.valueOf(idClass)).append(" OR ");
			collConstr.append("((").append(tabColName).append(".oidsaada >> 32) & 65535)=").append(String.valueOf(idClass)).append(" OR ");
		}
		return (listClass.length>1)?"("+collConstr.substring(0,collConstr.length()-4)+")":collConstr.substring(0,collConstr.length()-4);
	}

	/**
	 * Return a list of attributeHandler matching the UCDs used in the the query
	 * @return
	 */
	public final AttributeHandler[] getUCDColumns() {
		if( this.wuClause != null ) {
			return this.wuClause.getUCDColumns();
		}
		else {
			return new AttributeHandler[0];
		}
	}
	/**
	 * Returns the list of AttributeHandlers used in the principal part of the query.
	 * If no AttributeHandler is used, return an empty Set
	 * @return a set empty or containing the list of AttributeHandlers used in the principal part of the query
	 * @throws SaadaException 
	 */
	public final Set<AttributeHandler> buildListAttrHandPrinc() throws SaadaException, NullPointerException{
		Set<AttributeHandler> alAH = new LinkedHashSet<AttributeHandler>();

		Map<String,AttributeHandler> mAH = MetaCollection.getAttribute_handlers(this.sfiClause.getCatego());
		if( this.afClause != null  ){
			for( String c: this.afClause.getGolumns()) {
				AttributeHandler ah = mAH.get(c);
				if( ah != null ) {
					alAH.add(ah);
				}
			}
		}
		if( this.obClause != null ) {
			AttributeHandler ah = mAH.get(this.obClause.getAttr());
			if( ah != null ) {
				alAH.add(ah);
			}			
		}
		for(String colName:this.merger.getCollectionNames()){
			//** Add collection attribute
			if(this.wasClause!=null) {
				for(String attrName:this.wasClause.getAttributeList()) {
					AttributeHandler ah = mAH.get(attrName);
					if( ah != null ) {
						alAH.add(ah);
					}
				}
			}
		}
		// UCD columns arfe processed separately because of their specific formatting
		//		for( AttributeHandler ah : getUCDColumns() ) {
		//			alAH.add(ah);
		//		}
		//		if(this.obClause!=null && (this.obClause.getType()==OrderBy.ON_COLL || this.obClause.getType()==OrderBy.BOTH)) {
		//			alAH.add(mAH.get(this.obClause.getTheStatement()));
		//		}

		//** Add class attribute
		for(String className:this.merger.getCoveredClasses()){
			Map<String,AttributeHandler> mAHC = Database.getCachemeta().getClass(className).getAttributes_handlers();
			if(this.wasClause!=null) {
				for(String attrName:this.wasClause.getAttributeList()) {
					AttributeHandler ah = mAHC.get(attrName);
					if( ah != null ) {
						alAH.add(ah);
					}
				}
			}
			if(this.obClause!=null && !this.obClause.haveSpecialKey() && (this.obClause.getType()==OrderBy.ON_CLASS || this.obClause.getType()==OrderBy.BOTH)) {
				AttributeHandler ah = mAHC.get(this.obClause.getAttr());
				if( ah != null ) {
					alAH.add(ah);
				}
			}
		}
		return alAH;
	}

	/**
	 * 
	 */
	private final void init() {
		query_string = null ;
		sfiClause = null ;        
		afClause = null ;        
		//		wpClause  = null ;
		wasClause = null ;       
		wdmClause = null;
		//		wacClause = null ;
		//		wuClause  = null ;
		//		wUtClause  = null ;
		wrClause  = null ;
		obClause  = null ;
		limitClause = null;
		//		wuManager = null;
		//		wUtManager = null;
		index_owner_key = -1 ;

		setReport(new Report());        
	}

	/**
	 * @throws SaadaException 
	 */
	private void buildMerger(boolean all_columns) throws Exception {
		LinkedHashMap<String, SaadaQLConstraint>builders = new LinkedHashMap<String, SaadaQLConstraint>();
		this.merger = new Merger(this.sfiClause, all_columns);
		if( this.vor != null ) {
			this.merger.setDM(this.vor);
		}
		if( this.wasClause != null ) {
			builders.put("native", this.wasClause.getSaadaQLConstraint());
		}
		if( this.wpClause != null ) {
			builders.put("coord", this.wpClause.getSaadaQLConstraint());
		}
		if( this.obClause != null ) {
			SaadaQLConstraint sqc = this.obClause.getSaadaQLConstraint();
			builders.put("orderby", sqc);
		}
		if( this.wuClause != null ) {
			for( UCDField field: this.wuClause.getSaadaQLConstraints()) {
				builders.put(field.getSqlcolnames()[0], field);				
			}
		}
		if( this.wdmClause != null ) {
			for( UTypeField field: this.wdmClause.getSaadaQLConstraints()) {
				builders.put(field.getSqlcolnames()[0], field);				
			}
		}
		this.merger.setBuilderList(builders);
	}

	/**
	 * @throws SaadaException
	 */
	public final void parse() throws Exception {
		String strQtemp = this.query_string;
		this.addSentence("Query to process: "+ this.query_string);
		Utils.checkParentheseAccolade(strQtemp);
		/*
		 * Parse the Select From In clause
		 */
		this.sfiClause = new SelectFromIn(this.query_string);
		strQtemp = strQtemp.replace(this.sfiClause.getStrMatch(), "");
		if(Limit.isIn(strQtemp)){
			this.limitClause = new Limit(strQtemp);
			strQtemp = strQtemp.replace(this.limitClause.getStrMatch(), "");
		}
		if (AttributeFilter.isIn(strQtemp)) {
			this.afClause = new AttributeFilter(strQtemp);
			strQtemp = strQtemp.replace(this.afClause.getStrMatch(), "");
		}
		/*
		 * Parse the WhereAttributeSaada clause
		 */
		if (WhereAttributeSaada.isIn(strQtemp)) {
			this.wasClause = new WhereAttributeSaada(strQtemp);
			strQtemp = strQtemp.replace(this.wasClause.getStrMatch(), "");
		}
		if (WherePosition.isIn(strQtemp)) {
			this.wpClause = new WherePosition(strQtemp);
			strQtemp = strQtemp.replace(this.wpClause.getStrMatch(), "");
		}
		if (WhereUCD.isIn(strQtemp)) {
			this.wuClause  = new WhereUCD(strQtemp);
			strQtemp = strQtemp.replace(this.wuClause.getStrMatch(), "");
		}
		if (WhereDM.isIn(strQtemp)) {
			if( vor == null ) {
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "WhereDM clause can not be used because there is no DM set");				
			}
			this.wdmClause  = new WhereDM(strQtemp, vor);
			strQtemp = strQtemp.replace(this.wdmClause.getStrMatch(), "");
		}		
		if(OrderBy.isIn(strQtemp)){
			this.obClause = new OrderBy(strQtemp, this.sfiClause);
			switch(this.sfiClause.getMode()){
			case SelectFromIn.ONE_COL_ONE_CLASS: this.obClause.setType(OrderBy.BOTH); break;
			default: this.obClause.setType(OrderBy.ON_COLL); break;
			}
			strQtemp = strQtemp.replace(this.obClause.getStrMatch(), "");
			//			this.checkOrderBy();
		}
		if (WhereRelation.isIn(strQtemp)) {
			this.wrClause = new WhereRelation(strQtemp, this.vor);
			strQtemp = strQtemp.replace(this.wrClause.getStrMatch(), "");
		}		
		if (!strQtemp.trim().equals("")){
			Messenger.printMsg(Messenger.ERROR, "In query " + this.query_string);
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "No all the query has been parsed!! There is still: \"" + strQtemp.trim() + "\".\nCheck it to find the error!");
		}
		/*
		 * For the default order by when there is a limt, otherwise, 
		 * the result depends on the limit value or it can change from one query execution to another
		 * to be checked
		if( this.limitClause != null && this.obClause == null ){
			this.obClause = new OrderBy(this.sfiClause);			
		}
		*/

	}

	/**
	 * @param sqlPrinc
	 * @param epopTab
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	private final SaadaQLResultSet exec(String sqlPrinc,EPOnePattern[] epopTab) throws Exception{
		TimeSaada timeTot = new TimeSaada();
		timeTot.start();
		//--Exec Relation
		KeyIndex ki = null;
		int cpt = 0;
		if( epopTab != null ) {
			/*
			 * Necessary to flush the index cache for large query while it does not rest on WeakReferences
			 */
			Database.getCacheindex().flush();
			for(EPOnePattern epop:epopTab){
				TimeSaada time = new TimeSaada();
				time.start();
				this.addSentence(" - Processing pattern");
				ki=epop.execPattern(ki);
				cpt++;
				Database.getCacheindex().freeIndexes(this.index_owner_key);
				time.stop();
				this.addSentence(ki.selectedKeysSize() + " OIDs selected by pattern in "+time.check()+" ms");
			}
		}
		/*
		 * Append the limit clause in case of no match pattern
		 */
		if( (epopTab == null || epopTab.length == 0) && limitClause!=null && limitClause.getLimit() != Integer.MAX_VALUE ) {
			sqlPrinc += " limit " + limitClause.getLimit(); 
		}
		// Exec principal
		TimeSaada time = new TimeSaada();
		time.start();
		//		if( sqlPrinc == null ) {
		//			return null;
		//		}
		AttributeHandler[] ucols=null;
		if( this.wuClause != null ) {
			ucols = this.wuClause.getUCDColumns();
		}
		SaadaQLResultSet retour = new SaadaQLResultSet(sqlPrinc
				, (ki!=null)?ki.getSELECTEDKeySet():null
						, (limitClause!=null)?limitClause.getLimit():Integer.MAX_VALUE
								, ucols
								, this.buildListAttrHandPrinc());
		time.stop();
		this.addSentence(" - Execution of principal query done in "+time.check()+" ms");
		this.addSentence("* Final result:  " + retour.getSize() +  " found in "+timeTot.check()+"ms.");

		return retour;
	}

	public final OidsaadaResultSet getOids(String sqlPrinc,EPOnePattern[] epopTab) throws Exception{
		TimeSaada timeTot = new TimeSaada();
		timeTot.start();
		//--Exec Relation
		KeyIndex ki = null;
		int cpt = 0;
		if( epopTab != null ) {
			/*
			 * Necessary to flush the index cache for large query while it does not rest on WeakReferences
			 */
			Database.getCacheindex().flush();
			for(EPOnePattern epop:epopTab){
				TimeSaada time = new TimeSaada();
				time.start();
				this.addSentence(" - Processing pattern");
				ki=epop.execPattern(ki);
				cpt++;
				Database.getCacheindex().freeIndexes(this.index_owner_key);
				time.stop();
				this.addSentence(ki.selectedKeysSize() + " OIDs selected by pattern in "+time.check()+" ms");
			}
		}
		/*
		 * Append the limit clause in case of no match pattern
		 */
		if( (epopTab == null || epopTab.length == 0) && limitClause!=null && limitClause.getLimit() != Integer.MAX_VALUE ) {
			sqlPrinc += " limit " + limitClause.getLimit(); 
		}
		boolean with_computed_column = false;
		if( this.wuClause != null ) {
			with_computed_column = true;
		}
		OidsaadaResultSet retour = new OidsaadaResultSet(sqlPrinc
				, ki
				, (limitClause!=null)?limitClause.getLimit():Integer.MAX_VALUE
						,with_computed_column);
		return retour;
	}
	/**
	 * @param str
	 * @return
	 * @throws Exception 
	 */
	public final OidsaadaResultSet runBasicQuery(String str) throws QueryException{
		if( str == null || str.length() == 0){
			this.addError("Run Query Error: Query null");
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Empty query");
		}
		TimeSaada time = new TimeSaada();
		time.start();
		this.init();
		this.addSentence("Run query: " + str);
		this.index_owner_key = (new Date()).getTime();
		this.query_string = str.replaceAll("\\[unit", "[none");
		/*
		 * No query on an empty database: avoids foo messages
		 */
		if( Database.getCachemeta().getCollection_names().length == 0 ) {
			this.addError("Datase is Empty");
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Empty database");
			return null;
		}

		try {
			this.report.addSentence("A) PARSING OF THE SAADAQL QUERY");
			this.parse();
			/*
			 * The merger is in charge of building the SQL query
			 */
			this.buildMerger(false);
			String sql_query = merger.getSQL();
			/*
			 * match patterns
			 */
			MatchPattern[] mpTab = (this.wrClause==null)?new MatchPattern[0]:this.wrClause.getPattern();
			EPOnePattern[] epopTab = new EPOnePattern[mpTab.length];
			this.matchingCounterpartQueries = new HashMap<String, CounterpartSelector>();
			for(int i=0;i<mpTab.length;i++){
				this.report.addSentence("A) PROCESSING MATCH PATTERN #" + (i+1));
				Qualifier[] qualTab = mpTab[i].getQualTab();
				Qualif[] qT = null;
				if(qualTab!=null){
					qT = new Qualif[qualTab.length];
					for(int j=0;j<qualTab.length;j++){
						qT[j] = qualTab[j].getQual();
					}
				}
				PatternSQLBuilder psb = new PatternSQLBuilder(mpTab[i], this.vor);
				epopTab[i] = new EPOnePattern(mpTab[i].getRelation()
						,(mpTab[i].getCard()==null)?null:mpTab[i].getCard().getCard()
								,qT
								,psb.getCoveredClasses()
								,psb.getSQLquery()
								,this.index_owner_key
								,this.getReport());
				this.matchingCounterpartQueries.put(mpTab[i].getRelation(), new CounterpartSelector(epopTab[i], qT));

			}
			/*
			 * remove external () for SQLITE
			 */
			sql_query = sql_query.trim();
			if( sql_query.startsWith("(") ) {
				sql_query = sql_query.substring(1);
				if( sql_query.endsWith(")") ) {
					sql_query = sql_query.substring(0, sql_query.length()-1);
				}
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "SQL QUERY: "+sql_query+"\n");
			OidsaadaResultSet result = this.getOids(sql_query, epopTab);
			
			this.isDone();
			Database.getCacheindex().freeIndexes(this.index_owner_key);
			time.stop();
			this.addSentence( "Final result: + " + result.size() +" oids found in "+time.check()+" ms");
			/*
			 * Make sure to free index meory
			 */
			epopTab = null;

			Database.gc();
			return  result;
		} catch(SaadaException e1) {
			e1.printStackTrace();
			QueryException.throwNewException(SaadaException.DB_ERROR, e1);
			return null;
		} catch(Exception e) {
			Messenger.printMsg(Messenger.ERROR, "ERROR running the query: "+ str);
			Messenger.printStackTrace(e);
			this.addError("Run query parsing error: " + SaadaException.getExceptionMessage(e));
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	}

	public final SaadaInstanceResultSet getInstances(SaadaInstance si, String sqlPrinc,EPOnePattern[] epopTab) throws Exception{
		TimeSaada timeTot = new TimeSaada();
		timeTot.start();
		//--Exec Relation
		KeyIndex ki = null;
		int cpt = 0;
		if( epopTab != null ) {
			/*
			 * Necessary to flush the index cache for large query while it does not rest on WeakReferences
			 */
			Database.getCacheindex().flush();
			for(EPOnePattern epop:epopTab){
				TimeSaada time = new TimeSaada();
				time.start();
				this.addSentence(" - Processing pattern");
				ki=epop.execPattern(ki);
				cpt++;
				Database.getCacheindex().freeIndexes(this.index_owner_key);
				time.stop();
				this.addSentence(ki.selectedKeysSize() + " OIDs selected by pattern in "+time.check()+" ms");
			}
		}
		/*
		 * Append the limit clause in case of no match pattern
		 */
		if( (epopTab == null || epopTab.length == 0) && limitClause!=null && limitClause.getLimit() != Integer.MAX_VALUE ) {
			sqlPrinc += " limit " + limitClause.getLimit(); 
		}
		SaadaInstanceResultSet retour = new SaadaInstanceResultSet(si
				, sqlPrinc
				, ki
				, (limitClause!=null)?limitClause.getLimit():Integer.MAX_VALUE
						,  SaadaConstant.INT);
		return retour;
	}

	public final SaadaInstanceResultSet runAllColumnsQuery(SaadaInstance si, String str) throws QueryException{
		if( str == null || str.length() == 0){
			this.addError("Run Query Error: Query null");
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Empty query");
		}
		TimeSaada time = new TimeSaada();
		time.start();
		this.init();
		this.addSentence("Run query: " + str);
		this.index_owner_key = (new Date()).getTime();
		this.query_string = str.replaceAll("\\[unit", "[none");
		/*
		 * No query on an empty database: avoids foo messages
		 */
		if( Database.getCachemeta().getCollection_names().length == 0 ) {
			this.addError("Datase is Empty");
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Empty database");
			return null;
		}

		try {
			this.report.addSentence("A) PARSING OF THE SAADAQL QUERY");
			this.parse();
			/*
			 * The merger is in charge of building the SQL query
			 */
			this.buildMerger(true);
			String sql_query = merger.getSQL();
			/*
			 * match patterns
			 */
			MatchPattern[] mpTab = (this.wrClause==null)?new MatchPattern[0]:this.wrClause.getPattern();
			EPOnePattern[] epopTab = new EPOnePattern[mpTab.length];
			this.matchingCounterpartQueries = new HashMap<String, CounterpartSelector>();
			for(int i=0;i<mpTab.length;i++){
				this.report.addSentence("A) PROCESSING MATCH PATTERN #" + (i+1));
				Qualifier[] qualTab = mpTab[i].getQualTab();
				Qualif[] qT = null;
				if(qualTab!=null){
					qT = new Qualif[qualTab.length];
					for(int j=0;j<qualTab.length;j++){
						qT[j] = qualTab[j].getQual();
					}
				}
				PatternSQLBuilder psb = new PatternSQLBuilder(mpTab[i], this.vor);
				epopTab[i] = new EPOnePattern(mpTab[i].getRelation()
						,(mpTab[i].getCard()==null)?null:mpTab[i].getCard().getCard()
								,qT
								,psb.getCoveredClasses()
								,psb.getSQLquery()
								,this.index_owner_key
								,this.getReport());
				this.matchingCounterpartQueries.put(mpTab[i].getRelation(), new CounterpartSelector(epopTab[i], qT));

			}
			/*
			 * remove external () for SQLITE
			 */
			sql_query = sql_query.trim();
			if( sql_query.startsWith("(") ) {
				sql_query = sql_query.substring(1);
				if( sql_query.endsWith(")") ) {
					sql_query = sql_query.substring(0, sql_query.length()-1);
				}
			}

			int size = SaadaConstant.INT;
			if( Database.getWrapper().forwardOnly ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Not scrollable DB wrapper: must run the qyery twice to get its length\n");				
				OidsaadaResultSet result = this.getOids(sql_query, epopTab);
				size = result.size();
			}


			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "SQL QUERY: "+sql_query+"\n");	
			SaadaInstanceResultSet result = this.getInstances(si, sql_query, epopTab);
			this.isDone();
			Database.getCacheindex().freeIndexes(this.index_owner_key);
			time.stop();
			this.addSentence( "Final result: + " + size +" oids found in "+time.check()+" ms");
			Database.gc();
			return  result;

		} catch(Exception e) {
			Messenger.printMsg(Messenger.ERROR, "ERROR running the query: "+ str);
			e.printStackTrace();
			this.addError("Run query parsing error: " + SaadaException.getExceptionMessage(e));
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	}

	/**
	 * @param str
	 * @return
	 * @throws Exception 
	 */
	public final SaadaQLResultSet runQuery(String str, String dm_name, boolean all_columns) throws QueryException{
		if( str == null || str.length() == 0){
			this.addError("Run Query Error: Query null");
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Empty query");
		}
		if( dm_name != null && dm_name.length() > 0 ) {
			try {
				this.setDM(dm_name);
			}
			catch(Exception e ) {
				QueryException.throwNewException(SaadaException.METADATA_ERROR, e);
				return null;
			}
		}
		TimeSaada time = new TimeSaada();
		time.start();
		this.init();
		this.addSentence("Run query: " + str);
		this.index_owner_key = (new Date()).getTime();
		this.query_string = str.replaceAll("\\[unit", "[none");
		/*
		 * No query on an empty database: avoids foo messages
		 */
		if( Database.getCachemeta().getCollection_names().length == 0 ) {
			this.addError("Datase is Empty");
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Empty database");
			return null;
		}

		try {
			this.report.addSentence("A) PARSING OF THE SAADAQL QUERY");
			this.parse();
			/*
			 * The merger is in charge of building the SQL query
			 */
			this.buildMerger(all_columns);
			String sql_query = merger.getSQL();
			/*
			 * match patterns
			 */
			MatchPattern[] mpTab = (this.wrClause==null)?new MatchPattern[0]:this.wrClause.getPattern();
			EPOnePattern[] epopTab = new EPOnePattern[mpTab.length];
			this.matchingCounterpartQueries = new HashMap<String, CounterpartSelector>();
			for(int i=0;i<mpTab.length;i++){
				this.report.addSentence("A) PROCESSING MATCH PATTERN #" + (i+1));
				Qualifier[] qualTab = mpTab[i].getQualTab();
				Qualif[] qT = null;
				if(qualTab!=null){
					qT = new Qualif[qualTab.length];
					for(int j=0;j<qualTab.length;j++){
						qT[j] = qualTab[j].getQual();
					}
				}
				PatternSQLBuilder psb = new PatternSQLBuilder(mpTab[i], this.vor);
				epopTab[i] = new EPOnePattern(mpTab[i].getRelation()
						,(mpTab[i].getCard()==null)?null:mpTab[i].getCard().getCard()
								,qT
								,psb.getCoveredClasses()
								,psb.getSQLquery()
								,this.index_owner_key
								,this.getReport());
				this.matchingCounterpartQueries.put(mpTab[i].getRelation(), new CounterpartSelector(epopTab[i], qT));

			}
			/*
			 * remove external () for SQLITE
			 */
			sql_query = sql_query.trim();
			if( sql_query.startsWith("(") ) {
				sql_query = sql_query.substring(1);
				if( sql_query.endsWith(")") ) {
					sql_query = sql_query.substring(0, sql_query.length()-1);
				}
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "SQL QUERY: "+sql_query+"\n");
			SaadaQLResultSet result = this.exec(sql_query, epopTab);
			this.isDone();
			Database.getCacheindex().freeIndexes(this.index_owner_key);
			time.stop();
			this.addSentence( "Final result: + " + result.getSize() +" oids found in "+time.check()+" ms");
			Database.gc();
			return  result;

		} catch(Exception e) {
			Messenger.printMsg(Messenger.ERROR, "ERROR running the query: "+ str);
			e.printStackTrace();
			this.addError("Run query parsing error: " + SaadaException.getExceptionMessage(e));
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	}

	/*
	public final SaadaQLResultSet runADQLQuery(String str, String dm_name, boolean all_columns) throws QueryException{
		if( str == null || str.length() == 0){
			this.addError("Run Query Error: Query null");
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Empty query");
		}
		if( dm_name != null && dm_name.length() > 0 ) {
			try {
				this.setDM(dm_name);
			}
			catch(Exception e ) {
				QueryException.throwNewException(SaadaException.METADATA_ERROR, e);
				return null;
			}
		}
		TimeSaada time = new TimeSaada();
		time.start();
		this.init();
		this.addSentence("Run query: " + str);
		this.index_owner_key = (new Date()).getTime();
		this.query_string = str.replaceAll("\\[unit", "[none");

	 * No query on an empty database: avoids foo messages

		if( Database.getCachemeta().getCollection_names().length == 0 ) {
			this.addError("Datase is Empty");
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Empty database");
			return null;
		}

		try {
			this.report.addSentence("A) PARSING OF THE SAADAQL QUERY");

			AdqlParser parser = new AdqlParser(new ByteArrayInputStream(str.getBytes()));
			parser.Query();
			sfiClause = parser.getSFI();
			if (parser.getTopLimit() > -1)
				limitClause = new Limit("LIMIT "+parser.getTopLimit(), parser.getTopLimit());


	 * The merger is in charge of building the SQL query

			LinkedHashMap<String, SaadaQLConstraint> builders = new LinkedHashMap<String, SaadaQLConstraint>();
			merger = new Merger(sfiClause, all_columns);
			if (parser.getWhereConstraint() != null)
				builders.put("native", parser.getWhereConstraint());
			merger.setBuilderList(builders);
			String sql_query = merger.getSQL();

	 * match patterns

			MatchPattern[] mpTab = (this.wrClause==null)?new MatchPattern[0]:this.wrClause.getPattern();
			EPOnePattern[] epopTab = new EPOnePattern[mpTab.length];
			this.matchingCounterpartQueries = new HashMap<String, CounterpartSelector>();
			for(int i=0;i<mpTab.length;i++){
				this.report.addSentence("A) PROCESSING MATCH PATTERN #" + (i+1));
				Qualifier[] qualTab = mpTab[i].getQualTab();
				Qualif[] qT = null;
				if(qualTab!=null){
					qT = new Qualif[qualTab.length];
					for(int j=0;j<qualTab.length;j++){
						qT[j] = qualTab[j].getQual();
					}
				}
				PatternSQLBuilder psb = new PatternSQLBuilder(mpTab[i], this.vor);
				epopTab[i] = new EPOnePattern(mpTab[i].getRelation()
						                    ,(mpTab[i].getCard()==null)?null:mpTab[i].getCard().getCard()
						                    ,qT
						                    ,psb.getCoveredClasses()
						                    ,psb.getSQLquery()
						                    ,this.index_owner_key
						                    ,this.getReport());
				this.matchingCounterpartQueries.put(mpTab[i].getRelation(), new CounterpartSelector(epopTab[i], qT));

			}
			SaadaQLResultSet result = this.exec(sql_query, epopTab);
			System.out.println("\nSQL:"+sql_query+"\n");
			this.isDone();
			Database.getCacheindex().freeIndexes(this.index_owner_key);
			time.stop();
			this.addSentence( "Final result: + " + result.getSize() +" oids found in "+time.check()+" ms");
			System.gc();
			return  result;

		} catch(Exception e) {
			Messenger.printMsg(Messenger.ERROR, "ERROR running the query: "+ str);
			e.printStackTrace();
			this.addError("Run query parsing error: " + SaadaException.getExceptionMessage(e));
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	}

	public final SaadaQLResultSet runADQLQuery(String str) throws QueryException {
		return this.runADQLQuery(str, "", false);
	}*/

	/**
	 * Returns oids and columns used in the WHERE/ORDER
	 * @param str : query 
	 * @return
	 * @throws QueryException
	 */
	public final SaadaQLResultSet runQuery(String str) throws QueryException {
		return this.runQuery(str, "", false);
	}

	/**
	 * Returns all columns (class + collection) if flag is set
	 * @param str : query
	 * @param all_columns : flag asking for all columns
	 * @return
	 * @throws QueryException
	 */
	public final SaadaQLResultSet runQuery(String str, boolean all_columns) throws QueryException{
		return this.runQuery(str, "", all_columns);		
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());

		Query q = new Query();
		//		q.runQuery("Select ENTRY From * In *");
		//		q.runQuery("Select ENTRY From UCDTester1_UCDTagged1Entry  In UCDTester1");
		//		q.runQuery("Select ENTRY From UCDTester1_UCDTagged1Entry  In UCDTester1 WhereAttributeSaada{ _c1t1c1_sansu > pos_ra_csa }");
		//		q.runQuery("Select ENTRY From *  In UCDTester1 WhereAttributeSaada{ _c1t1c1_sansu > pos_ra_csa }");
		//		q.runQuery("Select ENTRY From *  In UCDTester1 WherePosition{ isInBox(\"M34\",0.1,J2000,FK5)}");
		//		q.runQuery("Select ENTRY From *  In UCDTester1 WherePosition{ isInCircle(\"M34\",0.1,J2000,FK5)}");
		//		q.runQuery("Select ENTRY From *  In UCDTester1 WherePosition{ isInCircle(\"M33\",0.1,J2000,FK5) isInBox(\"M34\",0.1,J2000,FK5)}");
		//		q.runQuery("Select ENTRY From UCDTester1_UCDTagged1Entry  In UCDTester1 WhereAttributeSaada{ _c1t1c1_sansu > pos_ra_csa } WherePosition{ isInCircle(\"M33\",0.1,J2000,FK5) isInBox(\"M34\",0.1,J2000,FK5)}");
		//		q.runQuery("Select ENTRY From *  In UCDTester1 WhereUCD{ [num.unit] > 2000 [km/s]}");
		//      q.runQuery("Select ENTRY From *  In UCDTester1 WhereUCD{ [num.unit] > 2000 [km/s] and [string.alphanum.wounit] > 3000 [km/s]}");
		//SaadaQLResultSet srs = q.runQuery("Select ENTRY From * In SpectroscopicSample WhereDM {[r.Magnitude] > 0 [none] }");
		//q.runQuery("Select ENTRY From * In SpectroscopicSample WhereDM {    [E.Magnitude] > 18 [none]  AND [Program.Name] = 'XBS' [none]   }");
		SaadaQLResultSet srs ;
		//		srs = q.runQuery("Select ENTRY From IAP_WFIEntry In SpectroscopicSample WhereAttributeSaada {_b_world = 12 AND (SOURCE_CLASS = 'AC' or SOURCE_CLASS = 'ACe')} Limit 10", true);
		//System.out.println(q.runQuery("Select ENTRY From * In SpectroscopicSample WhereAttributeSaada {(SOURCE_CLASS = 'AC' or SOURCE_CLASS = 'ACe')} Limit 10", "XIDSrcModel", true).getSize() + " objets effectivement retournes");
		// q.runQuery("Select ENTRY From * In SpectroscopicSample WherePosition{ isInCircle(\"poslist:list.agn\",1.0,J2000,ICRS)}", "XIDSrcModel");
		//SaadaQLResultSet srs = q.runQuery("Select ENTRY From * In SpectroscopicSample WherePosition{ isInCircle(\"poslist:list.agn\",1.0,J2000,ICRS)}", "XIDSrcModel", true);
		//		for( AttributeHandler ah: srs.getMeta()) {
		//			System.out.println(ah);
		//		}
		//		srs = q.runQuery("Select ENTRY From * In WideFieldData WherePosition{ isInCircle(\"poslist:list.agn\",1.0,J2000,ICRS)}", "WFSrcModel", true);
		//		for( AttributeHandler ah: srs.getMeta()) {
		//			System.out.println(ah);
		//		}
		//srs = q.runQuery("Select ENTRY From * In SpectroscopicSample ", "XIDSrcModel", true);
		//		srs = q.runQuery("Select ENTRY From TXMMiEntry In TXMMiData", false);
		//		for( AttributeHandler ah: srs.getMeta()) {
		//			System.out.println(ah);
		//		}

		//		srs = q.runQuery("Select ENTRY From TXMMiEntry In TXMMiData", true);
		//		for( AttributeHandler ah: srs.getMeta()) {
		//			System.out.println(ah);
		//		}
		//		srs = q.runQuery("Select SPECTRUM From EPICSpectrum In XMMData", true);
		//		srs = q.runADQLQuery("SELECT oidsaada, namesaada FROM EPICSpectrum WHERE pos_ra_csa > 8 AND \"_naxis\" > 0;", "", false);
		srs = q.runQuery("Select SPECTRUM From * In SpectroscopicSample WherePosition{isInCircle(\"06:14:13.12 70:52:02.7\",6.0,J2000,FK5)} WhereAttributeSaada{350.0 BETWEEN x_min_csa  AND  x_max_csa OR 900.0 BETWEEN x_min_csa  AND  x_max_csa OR (x_min_csa < 350.0 and  x_max_csa  >900.0)}");
		//srs = q.runQuery("Select SPECTRUM From EpicSrcSpect In EPIC WherePosition{isInCircle(\"12 12\",10800.0,J2000,FK5)} Limit 1000", "", false);
		System.out.println("NB COLUMNS = "+srs.getCol_names().size());
		System.out.println(srs.getSize()+" LINES MATCH !");
		for( AttributeHandler ah: srs.getMeta()) {
			System.out.println(ah);
		}
		//System.out.println(q.runAllDMColumnsQuery("Select ENTRY From * In SpectroscopicSample WhereAttributeSaada {(SOURCE_CLASS = 'AC' or SOURCE_CLASS = 'ACe')} Limit 10").getSize() + " objets effectivement retournes");
		Database.close();
	}

}
