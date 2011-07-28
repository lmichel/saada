package saadadb.query.executor;


public class QueryOrg extends Query_Report{

	public QueryOrg(Report report) {
		super(report);
	}
//	private String              queryStr ;
//	private SelectFromIn        sfiClause;        
//	private WherePosition       wpClause ;
//	private WhereAttributeSaada wasClause;        
//	private WhereAttributeClass wacClause;
//	private WhereUCD            wuClause ;
//	private WhereUType          wUtClause ;
//	private WhereDM             wdmClause ;
//	private WhereRelation       wrClause ;
//	private OrderBy             obClause ;
//	private Limit               limitClause ;
//
//	private UCDsManager         wuManager;
//	private UTypesManager       wUtManager;
//	private DMManager           wdmManager;
//
//	private long                index_owner_key;
//	private int                 unlimited_result_size;
//	private HashMap<String, CounterpartSelector> matchingCounterpartQueries;
//	/*
//	 * Current DM used by queries using WhereDM
//	 */
//	private VOResource vor;
//	private String principal_sql="";
//	
//	public QueryOrg() {
//		super(null);
//	}
//
//	public QueryOrg(String query) {
//		super(null);
//		queryStr = query;
//	}
//	
//	private final void init() {
//		queryStr = null ;
//		sfiClause = null ;        
//		wpClause  = null ;
//		wasClause = null ;        
//		wacClause = null ;
//		wuClause  = null ;
//		wUtClause  = null ;
//		wrClause  = null ;
//		obClause  = null ;
//		limitClause = null;
//		wuManager = null;
//		wUtManager = null;
//		index_owner_key = -1 ;
//
//		setReport(new Report());        
//	}
//
//	public String getPrincipal_sql() {
//		return this.principal_sql;
//	}
//	/**
//	 * Connect the Query on a DM which will be used by WhereDM statements
//	 * @param dm_name
//	 * @throws Exception
//	 */
//	public void setDM(String dm_name) throws Exception {
//		this.vor = Database.getCachemeta().getVOResource(dm_name);
//	}
//	
//	/**
//	 * @return
//	 */
//	public VOResource getDM() {
//		return this.vor;
//	}
//	/**
//	 * Returns the list of AttributeHandlers used in the principal part of the query.
//	 * If no AttributeHandler is used, return an empty Set
//	 * @return a set empty or containing the list of AttributeHandlers used in the principal part of the query
//	 * @throws SaadaException 
//	 */
//	public final Set<AttributeHandler> buildListAttrHandPrinc() throws SaadaException, NullPointerException{
//		Set<AttributeHandler> alAH = new LinkedHashSet<AttributeHandler>();
//		for(String colName:this.listColl()){
//			//** Add collection attribute
//			Map<String,AttributeHandler> mAH = MetaCollection.getAttribute_handlers(this.sfiClause.getCatego());
//			if(this.wasClause!=null)
//				for(String attrName:this.wasClause.getAttributeList())
//					alAH.add(mAH.get(attrName));
//			if(this.obClause!=null && (this.obClause.getType()==OrderBy.ON_COLL || this.obClause.getType()==OrderBy.BOTH))
//				alAH.add(mAH.get(this.obClause.getTheStatement()));
//			//** Add class attribute
//			for(String className:this.listClass(colName)){
//				if(!className.equals("*")){
//					Map<String,AttributeHandler> mAHC = Database.getCachemeta().getClass(className).getAttributes_handlers();
//					if(this.wacClause!=null)
//						for(String attrName:this.wacClause.getAttributeList())
//							alAH.add(mAHC.get(attrName));
//					if(this.wuManager!=null) {
//						for(String attrName:this.wuManager.getAttributeList(this.sfiClause.getCatego(),colName,className)) {
//							alAH.add(mAHC.get(attrName));
//						}
//					}
//					if(this.wUtManager!=null)
//						for(String attrName:this.wUtManager.getAttributeList(this.sfiClause.getCatego(),colName,className))
//							alAH.add(mAHC.get(attrName));
//					if(this.obClause!=null && !this.obClause.haveSpecialKey() && (this.obClause.getType()==OrderBy.ON_CLASS || this.obClause.getType()==OrderBy.BOTH))
//						alAH.add(mAHC.get(this.obClause.getTheStatement()));
//				}
//			}
//		}
//		return alAH;
//	}
//
//
//	/**
//	 * @param str
//	 * @return
//	 */
//	public final SaadaQLResultSet runQuery(String str){
//		if( str == null ){
//			this.addError("Run Query Error: Query null");
//			return null;
//		}
//		TimeSaada time = new TimeSaada();
//		time.start();
//		this.init();
//		this.addSentence("Run query: " + str);
//		this.index_owner_key = (new Date()).getTime();
//		this.queryStr = str.replaceAll("\\[unit", "[none");
//		/*
//		 * No query on an empty database: avoids foo messages
//		 */
//		if( Database.getCachemeta().getCollection_names().length == 0 ) {
//			this.addError("Datase is Empty");
//			return null;
//		}
//		try {
//			this.getReport().addSentence("A) PARSING OF THE SAADAQL QUERY");
//			this.parse();
//		} catch(Exception e) {
//			Messenger.printMsg(Messenger.ERROR, "ERROR running the query: "+ str);
//			Messenger.printStackTrace(e);
//			this.addError("Run query parsing error: " + SaadaException.getExceptionMessage(e));
//			return null;
//		}
//		principal_sql = null;
//		EPOnePattern[] epopTab = null;
//		this.matchingCounterpartQueries = new HashMap<String, CounterpartSelector>();
//		try {
//			this.addSentence("B) COMPUTING SQL QUERIES FROM THE SAADAQL QUERY");
//			this.addSentence( " - Computing SQL queries of the principal part");
//			principal_sql = this.makePrincipalSQL();
//			this.addSentence( "   - Result: "+((principal_sql==null)?"No query:!":principal_sql));
//			this.addSentence( " - Computing SQL queries of the WhereRelation part");
//			
//			if(principal_sql==null && this.wrClause==null) {
//				QueryException.throwNewException(SaadaException.NO_QUERIED_CLASS, "No query! No collection and classe must have all UCDs or all UTypes!");
//			}
//			
//			
//			MatchPattern[] mpTab = (this.wrClause==null)?new MatchPattern[0]:this.wrClause.getPattern();
//			epopTab = new EPOnePattern[mpTab.length];
//			for(int i=0;i<mpTab.length;i++){
//				Qualifier[] qualTab = mpTab[i].getQualTab();
//				Qualif[] qT = null;
//				if(qualTab!=null){
//					qT = new Qualif[qualTab.length];
//					for(int j=0;j<qualTab.length;j++){
//						qT[j] = qualTab[j].getQual();
//					}
//				}
//				String matchPatternSQLs = this.makeMatchPatternSQLs(mpTab[i]);
//				epopTab[i] = new EPOnePattern(mpTab[i].getRelation()
//						                    ,(mpTab[i].getCard()==null)?null:mpTab[i].getCard().getCard()
//						                    ,qT
//						                    ,this.getListClassFromMatchPattern(mpTab[i])
//						                    ,matchPatternSQLs
//						                    ,this.index_owner_key
//						                    ,this.getReport());
//				//this.matchingCounterpartQueries.put(mpTab[i].getRelation(), epopTab[i].getCounterPartsSQL());
//				this.matchingCounterpartQueries.put(mpTab[i].getRelation(), new CounterpartSelector(epopTab[i], qT));
//			}
//		} catch(Exception e) {
//			Messenger.printStackTrace(e);
//			this.addError("Run query parsing error: " + SaadaException.getExceptionMessage(e));
//			return null;
//		}
//		try{
//			SaadaQLResultSet result = this.exec(principal_sql,epopTab);
//			this.getReport().isDone();
//			Database.getCacheindex().freeIndexes(this.index_owner_key);
//			time.stop();
//			this.addSentence( "Final result: + " + result.getSize() +" oids found in "+time.check()+" ms");
//			System.gc();
//			return  result;
//		} catch(Exception e) {
//			Messenger.printStackTrace(e);
//			this.addError("Run Query Error: " + SaadaException.toString(e));
//			return null;
//		}
//	}
//	
//	/**
//	 * @param str
//	 * @return
//	 * @throws Exception
//	 */
//	public final SQLLikeResultSet runAllColumnsQuery(String str) throws Exception{
//		if( this.vor != null ) {
//			return runAllDMColumnsQuery(str);
//		}
//		SaadaQLResultSet srs = this.runQuery(str);
//		if( srs == null ) {
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, this.getErrorReport());
//		}
//		else if( srs.getSize() == 0 ) {
//			srs.getOidSQL().flush();
//			return srs.getOidSQL();
//		}
//		String tmptable = Database.getWrapper().getTempoTableName( srs.storeInTempTable());
//		String query = "";
//		/*
//		 * Replace "*" with an array of all collection names
//		 */
//		String[] colls = this.sfiClause.getListColl();
//		if( colls.length == 1 && colls[0].equals("*")) {
//			colls = Database.getCachemeta().getCollection_names();
//		}
//		/*
//		 * Build a query joining selected OIDs with all other columns of the collection table 
//		 * or class tables if the query cover one class
//		 */
//		for( String coll: colls) {
//			String second_join;
//			if( this.sfiClause.getMode()  == SelectFromIn.ONE_COL_ONE_CLASS ) {
//				String class_table = this.sfiClause.getListClass()[0];
//				second_join = "(" + tmptable + " JOIN " + class_table + " USING (oidsaada))";
//			}
//			else {
//				second_join = tmptable;
//			}
//			
//			String subquery = "(SELECT * FROM " + Database.getWrapper().getCollectionTableName(coll, this.sfiClause.getCatego()) + " JOIN " + second_join + " USING (oidsaada))";
//			if( query.length() > 0 ) {
//				query += "\nUNION\n";
//			}
//			query += subquery;
//		}
//		/*
//		 * Be sure to no have to lock table with MySQL
//		 */
//		SQLTable.commitTransaction();
//		return this.exec(query,null).getOidSQL();
//	}
//	/**
//	 * @param str
//	 * @return
//	 * @throws Exception
//	 */
//	public final SQLLikeResultSet runAllDMColumnsQuery(String str) throws Exception{
//		if( this.vor == null ) {
//			return runAllColumnsQuery(str);
//		}
//		SaadaQLResultSet srs = this.runQuery(str);
//		if( srs == null ) {
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, this.getErrorReport());
//		}
//		else if( srs.getSize() == 0 ) {
//			if( srs.getOidSQL() != null ) {
//				srs.getOidSQL().flush();
//				return srs.getOidSQL();
//			}
//			else {
//				return new SQLLikeResultSet();
//			}
//		}
//		String tmptable = Database.getWrapper().getTempoTableName( srs.storeInTempTable());
//		String query = "";
//		/*
//		 * Replace "*" with an array of all collection names
//		 */
//		String[] colls = this.sfiClause.getListColl();
//		if( colls.length == 1 && colls[0].equals("*")) {
//			colls = Database.getCachemeta().getCollection_names();
//		}
//		/*
//		 * Build a query joining selected OIDs with all other columns of the collection table 
//		 * or class tables if the query cover one class
//		 */
//		int category = this.sfiClause.getCatego();
//		for( String coll: colls) {
//			String[] classes = Database.getCachemeta().getClassesOfCollection(coll, category);
//			for( String classe: classes) {
//				try {
//					SaadaInstance si = (SaadaInstance) Class.forName("generated." + Database.getDbname() + "." + classe).newInstance();			
//					si.activateDataModel(vor.getName());
//					String class_select = "SELECT oidsaada ";
//					Map<String, String> ma = si.getSQLFields();
//					for( Entry<String, String> e: ma.entrySet()) {
//						class_select += ", " + e.getValue() + " as " + e.getKey();
//					}
//					class_select += " FROM " + classe;
//					if( query.length() > 0 ) {
//						query += "UNION\n";
//					}
//					//second_join = "(" + tmptable + " JOIN " + class_table + " USING (oidsaada))";
//					//query += class_select + "\n";
//					query += "(" + class_select + " JOIN " + tmptable + " USING (oidsaada))";
//					/*
//					 * Takes mapped classes only
//					 */
//				} catch (Exception e) {
//					this.getReport().addError(e.toString());
//				}
//			}
//		}
//		return this.exec(query,null).getOidSQL();
//	}
//	/**
//	 * Return all the collection affected by the query
//	 * Replace the "*" by all the collection the database contains
//	 */
//	private final String[] listColl() throws NullPointerException {
//		return (this.sfiClause.getListColl()[0].equals("*"))? Database.getCachemeta().getCollection_names():this.sfiClause.getListColl();
//	}
//	/**
//	 * Return - if there is no WhereUCD clause: the Select From In list of class
//	 *        - if there is a  WhereUCD clause: the list of class having all the UCDs in the clause (among the SFI list of class if not *)
//	 */
//	private final String[] listClass(String collName) {
//		return (this.wuManager!=null)?(String[])this.wuManager.getClassNamesHavingAllUCD_In(this.sfiClause.getCatego(),collName,this.sfiClause.getListClass()).toArray(new String[0])
//			  :(this.wUtClause!=null)?(String[])this.wUtManager.getClassNamesHavingAllUType_In(this.sfiClause.getCatego(),collName,this.sfiClause.getListClass()).toArray(new String[0])
//			  :(this.wdmClause!=null)?(String[])this.wdmManager.getClassNamesHavingAllDM_In(this.sfiClause.getCatego(),collName).toArray(new String[0])
//			  :this.sfiClause.getListClass();
//	}
//	/**
//	 * @param collName
//	 * @param catego
//	 * @param aocClause
//	 * @param auManager
//	 * @param autManager
//	 * @return
//	 */
//	private static final String[] listClassCP(String collName,int catego,AssObjClass aocClause,UCDsManager auManager,UTypesManager autManager) {
//		String[] listClassCP = (aocClause==null)?new String[]{"*"}:aocClause.getlistClass();
//		return (auManager!=null)?(String[])auManager.getClassNamesHavingAllUCD_In(catego,collName,listClassCP).toArray(new String[0]):(autManager!=null)?(String[])autManager.getClassNamesHavingAllUType_In(catego,collName,listClassCP).toArray(new String[0]):listClassCP;
//	}
//	/**
//	 * @param mp
//	 * @return
//	 * @throws SaadaException
//	 */
//	private final String[] getListClassFromMatchPattern(MatchPattern mp) throws SaadaException{
//		AssObjClass    aocClause  = mp.getAocClause();
//		AssUCD         auClause   = mp.getAuClause();
//		AssUType       autClause  = mp.getAutClause();
//		if(aocClause==null && auClause==null && autClause==null) return null;
//		UCDsManager auManager = (auClause!=null)?new UCDsManager(auClause.getConstraintTab()):null;
//		UTypesManager autManager = (autClause!=null)?new UTypesManager(autClause.getConstraintTab()):null;
//		MetaRelation   mr = Database.getCachemeta().getRelation(mp.getRelation());
//		String cpCollName = mr.getSecondary_coll()    ; 
//		int cpCategory    = mr.getSecondary_category();
//		return listClassCP(cpCollName,cpCategory,aocClause,auManager,autManager);
//	}
//	/**
//	 * Detect the presence of an order by clause
//	 *  
//	 * - All collections of same type (Image, Spectre,...) have same attributes
//	 * - If a query have a WhereAttriClass clause, it concern only one collection and one class
//	 * - WhereUCD = several colection,several classes
//	 * 
//	 * @return
//	 * @throws QuerySaadaQLException 
//	 * @throws SyntaxSaadaQLException 
//	 */
//	public final String makePrincipalSQL() throws SaadaException{
//		List<QuerySQL> colSQLs = new ArrayList<QuerySQL>();
//		// Added to solve the problem of cast with text in uType unions...
//		if(wUtManager!=null) this.wUtManager.prepare(this.sfiClause.getCatego(),this.listColl(),this.sfiClause.getListClass());
//		if(wuManager!=null) this.wuManager.prepare(this.sfiClause.getCatego(),this.listColl(),this.sfiClause.getListClass());
//		if(wdmManager!=null) this.wdmManager.prepare(this.sfiClause.getCatego(),this.listColl(),this.sfiClause.getListClass());
//		for(String colName:this.listColl()){
//			QuerySQL sql = this.computeCollectionSQL(colName,this.sfiClause.getCatego(),this.listClass(colName),this.wasClause,this.wacClause,this.wuManager,this.wUtManager,this.wdmManager,true);
//			if(sql!=null) colSQLs.add(sql);
//		}
//		if(colSQLs.size()==0){
//			return null;
//		}
//		String query = QuerySQL.union(colSQLs).buildQuery();
//		if(this.obClause!=null)                           query += "\nORDER BY "+this.obClause.getTheStatement()+" "+this.obClause.getOrder();
//		if(this.limitClause!=null && this.wrClause==null) query += "\nLIMIT "+this.limitClause.getLimit();
//		return query;
//	}
//
//	/**
//	 * @param mp
//	 * @return
//	 * @throws SaadaException
//	 */
//	private final String makeMatchPatternSQLs(MatchPattern mp) throws SaadaException{
//		AssObjAttSaada aoasClause = mp.getAoasClause();
//		AssObjClass    aocClause  = mp.getAocClause();
//		AssObjAttClass aoacClause = mp.getAoacClause();
//		AssUCD         auClause   = mp.getAuClause();
//		AssUType       autClause  = mp.getAutClause();
//		AssDM          dmClause   = mp.getDmClause();
//		if(aoasClause==null && aoacClause==null && auClause==null && autClause==null) return null;
//		UCDsManager    auManager = (auClause!=null)?new UCDsManager(auClause.getConstraintTab()):null;
//		UTypesManager autManager = (autClause!=null)?new UTypesManager(autClause.getConstraintTab()):null;
//		DMManager      dmManager = (dmClause!=null)?new DMManager(this.vor, dmClause.getConstraintTab()):null;
//		MetaRelation   mr = Database.getCachemeta().getRelation(mp.getRelation());
//		String cpCollName = mr.getSecondary_coll()    ; 
//		int cpCategory    = mr.getSecondary_category();
//		String[] listClassCP = listClassCP(cpCollName,cpCategory,aocClause,auManager,autManager);
//		if(listClassCP.length == 0) {
//			QueryException.throwNewException(SaadaException.NO_QUERIED_CLASS, "There is no class having all the UCD of the AssociatedUCD clause!");
//		}
//		return this.computeCollectionSQL(cpCollName,cpCategory,listClassCP,aoasClause,aoacClause,auManager,autManager,dmManager, false).buildQuery();
//	}
//
//	/**
//	 * @param colName
//	 * @param catNbr
//	 * @param listClass
//	 * @param attrSaadaClause
//	 * @param attrClassClause
//	 * @param ucManager
//	 * @param utManager
//	 * @param princ
//	 * @return
//	 * @throws SaadaException
//	 */
//	private final QuerySQL computeCollectionSQL(String colName,int catNbr,String[] listClass,ClauseSQL attrSaadaClause,ClauseSQL attrClassClause,UCDsManager ucManager,UTypesManager utManager,DMManager dmManager,boolean princ) throws SaadaException{
//		String colTabName = Database.getCachemeta().getCollectionTableName(colName,catNbr);
//		if(listClass.length==0){
//			Messenger.printMsg(Messenger.WARNING,"No classe concerned by the query in the collection \""+ colName+ "\"! (It's surely because there is no wanted ucd(s) in wanted classe(s) of that collection)");
//			return null;
//		}
//		String colConstraint = null;
//		if(princ) colConstraint = this.getCollectionConstraintPrinc(colTabName,(WhereAttributeSaada)attrSaadaClause);
//		else colConstraint=(attrSaadaClause != null)?"(" + getStatementWithName(colTabName,attrSaadaClause.getTheStatement(),attrSaadaClause.getAttributeList()) + ")":"";
//		QuerySQL classesQuery = null;
//		if(!listClass[0].equals("*")){
//			if( (colConstraint.equals("") && (this.obClause==null || this.obClause.getType()!=OrderBy.ON_COLL)) 
//				|| attrClassClause!=null || ucManager!=null || utManager!=null || dmManager!=null){
//				QuerySQL[]  classQueryTab = new QuerySQL[listClass.length];
//				for(int i=0;i<listClass.length;i++){
//					classQueryTab[i] = getClassQuery(catNbr,colName,listClass[i],attrClassClause,ucManager,utManager, dmManager);
//					if(princ && this.obClause!=null && ((colConstraint.equals("") && this.obClause.getType()==OrderBy.BOTH) ||  this.obClause.getType()==OrderBy.ON_CLASS)){
//						classQueryTab[i].addAttribute(this.obClause.getTheStatement());
//					}
//				}
//				classesQuery = QuerySQL.union(classQueryTab);
//				System.out.println("-------------" + classesQuery.buildQuery() + "------------------");
//			}else{
//				if(!colConstraint.equals("")) colConstraint += " AND ";
//				colConstraint += QueryOrg.getCollectionConstraintOnClass(colTabName, listClass);
//			}
//		}
//		Set<String> colAttributes = new LinkedHashSet<String>();
//		if(attrSaadaClause != null){
//			for(String collAttr:attrSaadaClause.getAttributeList()){
//				colAttributes.add(collAttr);
//			}
//		}
//
//		if(princ){
//			if(this.wpClause!=null){
//				colAttributes.add("pos_ra_csa");
//				colAttributes.add("pos_dec_csa");
//			}
//			/*
//			 * Demander à fx pourquoi il y a cette condition !colConstraint.equals("")
//			 */
//			if(this.obClause!=null && ((/*!colConstraint.equals("") && */this.obClause.getType()==OrderBy.BOTH) || this.obClause.getType()==OrderBy.ON_COLL)){
//				if(!colAttributes.contains(this.obClause.getTheStatement()) && !this.obClause.getTheStatement().equals("oidsaada")) colAttributes.add(this.obClause.getTheStatement());
//			}
//		}
//
//		if(colConstraint.equals("") && (!princ || this.obClause==null || this.obClause.getType()!=OrderBy.ON_COLL)){
//			if(classesQuery==null){
//				return (this.wrClause==null || this.cardEq0())?new QuerySQL(null,colTabName,"oidsaada",(String[])colAttributes.toArray(new String[0]),colConstraint):null;
//			}else{
//				return classesQuery;
//			}
//		}else{
//			if(classesQuery==null){
//				return new QuerySQL(null,colTabName,"oidsaada",(String[])colAttributes.toArray(new String[0]),colConstraint);
//			}else{
//				return (new QuerySQL(null,colTabName,"oidsaada",(String[])colAttributes.toArray(new String[0]),colConstraint)).intersection(classesQuery,"oidsaada");
//			}
//		}
//	}
//	/**
//	 * @return
//	 * @throws NumberFormatException
//	 */
//	private final boolean cardEq0() throws NumberFormatException{
//		if(this.wrClause == null) return false;
//		Cardinality c = (this.wrClause.getPattern())[0].getCard();
//		if(c==null) return false;
//		return c.getCard().card_0();
//	}
////	Peut poser problement si un nom d'attribut contient un autre non d'attribut!
//	/**
//	 * @param name
//	 * @param statement
//	 * @param listAtt
//	 * @return
//	 */
//	private static final  String getStatementWithName(String name,String statement,String[] listAtt){
//		for(String attr : listAtt){
//			statement = statement.replaceAll(attr,name+"."+attr);
//		}
//		// To be sure, if listAtt containts more that one time the same attribute
//		return statement.replaceAll("("+name+"\\.){2,}",name+".");
//	}
//	
//	/**
//	 * @param tabColName
//	 * @param asClause
//	 * @return
//	 * @throws SaadaException
//	 */
//	private final String getCollectionConstraintPrinc(String tabColName,WhereAttributeSaada asClause) throws SaadaException{
//		String colConstr = "";
//		if(asClause != null) colConstr += "(" + getStatementWithName(tabColName,asClause.getTheStatement(),asClause.getAttributeList()) + ")";
//		if(this.wpClause!=null){
//			if (!colConstr.equals("")) {colConstr += " AND ";}
//			colConstr += "(" + this.wpClause.getSqlConstraint(tabColName) + ")";
//		}
//		return colConstr;
//	}
//
//	/**
//	 * @param tabColName
//	 * @param listClass
//	 * @return
//	 * @throws SaadaException
//	 */
//	public static final String getCollectionConstraintOnClass(String tabColName,String[] listClass) throws SaadaException {
//		StringBuffer collConstr = new StringBuffer();
//		int idClass;
//		for (String className : listClass){
//			if (( idClass = Database.getCachemeta().getClass(className).getId() ) == 0) {
//				QueryException.throwNewException(SaadaException.METADATA_ERROR, "The Classe " + className + " is unknowed! Id return = 0!");
//			}
//			//collConstr.append(tabColName).append(".class_id_csa=").append(String.valueOf(idClass)).append(" OR ");
//			collConstr.append("((").append(tabColName).append(".oidsaada >> 32) & 65535)=").append(String.valueOf(idClass)).append(" OR ");
//		}
//		return (listClass.length>1)?"("+collConstr.substring(0,collConstr.length()-4)+")":collConstr.substring(0,collConstr.length()-4);
//	}
//	/**
//	 * Return class constraint + liste colNames
//	 * @param catNbr
//	 * @param colName
//	 * @param className
//	 * @param acClause
//	 * @param ucdManager
//	 * @param uTypeManager
//	 * @return
//	 * @throws SaadaException
//	 */
//	private static final QuerySQL getClassQuery(int catNbr,String colName,String className,ClauseSQL acClause,UCDsManager ucdManager,UTypesManager uTypeManager,DMManager dmManager) throws SaadaException{
//		String classConstr = "";
//		Set<String> attrNames = new LinkedHashSet<String>();
//		if(acClause!=null){
//			classConstr +=  "(" + getStatementWithName(className,acClause.getTheStatement(),acClause.getAttributeList()) + ")";
//			for(String attrName:acClause.getAttributeList()) attrNames.add(attrName);
//		}
//		if(ucdManager!=null){
//			String ucdConstraint = ucdManager.getSqlConstraintsFor(catNbr,colName,className);
//			if(ucdConstraint!=null){
//				classConstr += classConstr.equals("")?ucdConstraint:" AND "+ucdConstraint;
//				for(String attrNameWA:ucdManager.getAttributeListWithFunctionAndAlias(catNbr,colName,className)) attrNames.add(attrNameWA);
//			}
//		}
//		if(uTypeManager!=null){
//			String uTypeConstraint = uTypeManager.getSqlConstraintsFor(catNbr,colName,className);
//			if(uTypeConstraint!=null){
//				classConstr += classConstr.equals("")?uTypeConstraint:" AND "+uTypeConstraint;
//				for(String attrNameWA:uTypeManager.getAttributeListWithFunctionAndAlias(catNbr,colName,className)) attrNames.add(attrNameWA);
//			}
//		}
//		if(dmManager!=null){
//			String dmConstraint = dmManager.getSqlConstraintsFor(catNbr,colName,className);
//			if(dmConstraint!=null){
//				classConstr += classConstr.equals("")?dmConstraint:" AND "+dmConstraint;
//				for(String attrNameWA:dmManager.getAttributeListWithFunctionAndAlias(catNbr,colName,className)) {
//					attrNames.add(attrNameWA);
//				}
//			}
//		}
//		return new QuerySQL(null,className,"oidsaada",(String[])attrNames.toArray(new String[0]),classConstr);
//	}
//
//	/**
//	 * @param sqlPrinc
//	 * @param epopTab
//	 * @return
//	 * @throws Exception
//	 */
//	private final SaadaQLResultSet exec(String sqlPrinc,EPOnePattern[] epopTab) throws Exception{
//		TimeSaada timeTot = new TimeSaada();
//		timeTot.start();
//		//--Exec Relation
//		KeyIndex ki = null;
//		int cpt = 0;
//		if( epopTab != null ) {
//			/*
//			 * Necessary to flush the index cache for large query while it does not rest on WeakReferences
//			 */
//			Database.getCacheindex().flush();
//			for(EPOnePattern epop:epopTab){
//				TimeSaada time = new TimeSaada();
//				time.start();
//				this.addSentence(" - Processing pattern");
//				ki=epop.execPattern(ki);
//				cpt++;
//				Database.getCacheindex().freeIndexes(this.index_owner_key);
//				time.stop();
//				this.addSentence(ki.selectedKeysSize() + " OIDs selected by pattern in "+time.check()+" ms");
//			}
//		}
//		// Exec principal
//		TimeSaada time = new TimeSaada();
//		time.start();
////		if( sqlPrinc == null ) {
////			return null;
////		}
//		AttributeHandler[] ucols=null;
//		if( this.wuClause != null ) {
//			ucols = this.wuClause.getUCDColumns();
//		}
//		SaadaQLResultSet retour = new SaadaQLResultSet(sqlPrinc
//				                  , (ki!=null)?ki.getSELECTEDKeySet():null
//				                  , (limitClause!=null)?limitClause.getLimit():Integer.MAX_VALUE
//				                  , ucols
//				                  , this.buildListAttrHandPrinc());
//		time.stop();
//		this.addSentence(" - Execution of principal query done in "+time.check()+" ms");
//		this.addSentence("* Final result: + toReturn.size() +  found in "+timeTot.check()+"ms.");
//
//		return retour;
//	}
//
//	
//	/**
//	 * @throws SaadaException
//	 */
//	public final void parse() throws SaadaException {
//		String strQtemp = this.queryStr;
//		this.addSentence("Query to parse: "+this.queryStr);
//		checkParentheseAccolade(strQtemp);
//		// Parse the query
//		this.sfiClause = new SelectFromIn(this.queryStr);
//		strQtemp = strQtemp.replace(this.sfiClause.getStrMatch(), "");
//		if (WhereAttributeSaada.isIn(strQtemp)) {
//			this.wasClause = new WhereAttributeSaada(strQtemp);
//		}
//		if (WherePosition.isIn(strQtemp)) {
//			this.wpClause = new WherePosition(strQtemp);
//			strQtemp = strQtemp.replace(this.wpClause.getStrMatch(), "");
//		}
//		if (WhereAttributeClass.isIn(strQtemp)) {
//			this.wacClause = new WhereAttributeClass(strQtemp);
//			strQtemp = strQtemp.replace(this.wacClause.getStrMatch(), "");
//		}
//		if (WhereUCD.isIn(strQtemp)) {
//			this.wuClause  = new WhereUCD(strQtemp);
//			this.wuManager = new UCDsManager(this.wuClause.getConstraintTab());
//			strQtemp = strQtemp.replace(this.wuClause.getStrMatch(), "");
//		}
//		if (WhereUType.isIn(strQtemp)) {
//			if(this.wuClause!=null) QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "WhereUCD and WhereUType in the same query not yet supported!");
//			this.wUtClause  = new WhereUType(strQtemp);
//			this.wUtManager = new UTypesManager(this.wUtClause.getConstraintTab());
//			strQtemp = strQtemp.replace(this.wUtClause.getStrMatch(), "");
//		}
//		if (WhereDM.isIn(strQtemp)) {
//			this.wdmClause  = new WhereDM(strQtemp);
//			this.wdmManager = new DMManager(this.vor, this.wdmClause.getConstraintTab());
//			strQtemp = strQtemp.replace(this.wdmClause.getStrMatch(), "");
//		}
//		if (WhereRelation.isIn(strQtemp)) {
//			this.wrClause = new WhereRelation(strQtemp);
//			strQtemp = strQtemp.replace(this.wrClause.getStrMatch(), "");
//		}
//		if(OrderBy.isIn(strQtemp)){
//			this.obClause = new OrderBy(strQtemp);
//			strQtemp = strQtemp.replace(this.obClause.getStrMatch(), "");
//			this.checkOrderBy();
//		}
//		if(Limit.isIn(strQtemp)){
//			this.limitClause = new Limit(strQtemp);
//			strQtemp = strQtemp.replace(this.limitClause.getStrMatch(), "");
//		}
//		// Check if all the Query has been parsed
//		if (!strQtemp.trim().equals("")){
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "No all the query has been parsed!! There is still: \"" + strQtemp.trim() + "\".\nCheck it to find the error!");
//		}
//		// Check the validity of the structure of Query
//		this.checkQueryCoverage();
//		
//		// Check consistency with database metadata
//		this.checkPrincipalCollsClassAndAttr();
//		this.checkRelationCollClassAttr();
//	}
//	
//	/**
//	 * @throws QueryException 
//	 * 
//	 */
//	private void checkQueryCoverage() throws QueryException {
//		int mode = this.sfiClause.getMode();
//		if( this.wdmClause != null ) {
//			if( mode != MULT_COL && mode != ONE_COL_ANY_CLASS) {
//				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Class can not be specified in a query using an imported Data Model");				
//			}
//			if( this.wuClause != null || this.wUtClause != null || this.wacClause != null ) {
//				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Queries using an imported Data Model do not support WhereAttributeClass or WhereUtype/Ucd statements");								
//			}
//			if( this.obClause != null ) {
//				QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Queries using an imported Data Model do not support Order By statement");								
//			}
//		}
//		if(this.wacClause!=null && mode != ONE_COL_ONE_CLASS){
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The class concerned in the WhereAttributeClass clause must be precised, and only that one!");
//		}
//		if(this.wrClause!=null && this.sfiClause.getMode()==MULT_COL){
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The collection concerned in the WhereRelation clause must be precised, and only that one!");
//		}
//		
//	}
//	/**
//	 * @param attr
//	 * @param category
//	 * @param colNames
//	 * @return
//	 */
//	private final boolean attrInAllCol(String attr,int category,String[] colNames){
//		for(String colName:colNames)
//			if(!MetaCollection.getAttribute_handlers_names(category).contains(attr))
//				return false;
//		return true;
//	}
//	/**
//	 * @param attr
//	 * @param category
//	 * @param colNames
//	 * @return
//	 */
//	private final boolean attrInOneCol(String attr,int category,String[] colNames){
//		for(String colName:colNames)
//			if(MetaCollection.getAttribute_handlers_names(category).contains(attr))
//				return true;
//		return false;
//	}
//
//	/**
//	 * @throws SaadaException
//	 */
//	private final void checkOrderBy()throws SaadaException{
//		String obAttr = this.obClause.getTheStatement();
//		boolean isInCol = false;
//		String[] colNames = this.listColl();
//		if(this.attrInOneCol(obAttr,this.sfiClause.getCatego(),colNames)){
//			if(!this.attrInAllCol(obAttr,this.sfiClause.getCatego(),colNames)){
//				QueryException.throwNewException(SaadaException.METADATA_ERROR, "\"order by\" attribute (\""+obAttr+"\") not present in all collections!");
//			}
//			isInCol = true;
//		}
//		boolean isInClass = false;
//		// look if the attribute is in class or both
//		if(!isInCol && this.sfiClause.getMode()==ONE_COL_MULT_CLASS){
//			QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "\"order by\" with mutliple class is not implemented! Sorry... :o(");
//		} else if(this.sfiClause.getMode()==ONE_COL_ONE_CLASS){
//			isInClass = Database.getCachemeta().getClass(this.sfiClause.getListClass()[0]).getClassAttribute_names().contains(obAttr);
//		}
//		//decision
//		if(!isInCol & !isInClass){
//			QueryException.throwNewException(SaadaException.METADATA_ERROR, "The \"order by\" attribute \""+obAttr+"\"not found in the collection and in the class of the query!");
//		}
//		if(isInCol & isInClass){
//			if(this.obClause.haveSpecialKey()){
//				this.obClause.setType(OrderBy.BOTH);
//			}else{
//				QueryException.throwNewException(SaadaException.METADATA_ERROR, "Ambiguity: both the collection and the class have the same attribute!");
//			}
//		}
//		else if(isInCol){this.obClause.setType(OrderBy.ON_COLL);}
//		else if(isInClass){this.obClause.setType(OrderBy.ON_CLASS);}
//	}
//
//	/**
//	 * @param str
//	 * @throws QueryException
//	 */
//	private static final void checkParentheseAccolade(String str) throws QueryException {
//		if (str.concat(" ").split("\\}").length != str.concat(" ").split("\\{").length)
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Error!  number of \"{\" different from the number of \"}\" !! Check your query to fix it!");
//		if (str.concat(" ").split("\\)").length != str.concat(" ").split("\\(").length)
//			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "Error! number of \"(\" different from the number of \")\" !! Check your query to fix it!");
//	}
//	/**
//	 * @throws SaadaException
//	 */
//	private final void checkPrincipalCollsClassAndAttr() throws SaadaException{
//		String[] listCollQ  = this.sfiClause.getListColl();
//		if(listCollQ[0].equals("*")) return ;
//		String[] listCollDB = Database.getCachemeta().getCollection_names();
//		for(String colName:listCollQ){
//			// Verif if collection existsAll
//			if(!QEToolBox.isInList(colName,listCollDB)) QueryException.throwNewException(SaadaException.METADATA_ERROR, "Collection \""+colName+"\" not present in the metadata cache");
//			if(this.wasClause!=null){
//				// Verif if attributes exist for the collection
//				Set<String> attrCollDB = MetaCollection.getAttribute_handlers_names(this.sfiClause.getCatego());
//				for(String attrCollQ:this.wasClause.getAttributeList()){
//					if(!attrCollDB.contains(attrCollQ)) QueryException.throwNewException(SaadaException.METADATA_ERROR, "Attribute \""+attrCollQ+"\" not found in the collection \""+colName+"\"");
//				}
//			}
//			checkClassesAndAttr(colName,this.sfiClause.getCatego(),this.sfiClause.getListClass(),(this.wacClause!=null)?this.wacClause.getAttributeList():null);
//		}
//	}
//	/**
//	 * @throws SaadaException
//	 */
//	private final void checkRelationCollClassAttr() throws SaadaException{
//		if(this.wrClause==null) return;
//		for(MatchPattern mp:this.wrClause.getPattern()){
//			String relationName = mp.getRelation();
//			MetaRelation mr = Database.getCachemeta().getRelation(relationName);
//			// Check if the relation exists
//			if(mr==null){
//				QueryException.throwNewException(SaadaException.METADATA_ERROR, "The relation \""+relationName+"\" not found in the metadata cache");
//				}
//			else if( !mr.isIndexed() ){
//				FatalException.throwNewException(SaadaException.METADATA_ERROR, "The relation \""+relationName+"\" can not be used because it is not indexed");
//				}
//			String colName = mr.getSecondary_coll();
//			int catego  = mr.getSecondary_category();
//			// Check if attributes of the collection exist
//			if(mp.getAoasClause()!=null){
//				Set<String> attrCollDB = MetaCollection.getAttribute_handlers_names(catego);
//				for(String attrCollQ:mp.getAoasClause().getAttributeList()){
//					if(!attrCollDB.contains(attrCollQ)) QueryException.throwNewException(SaadaException.METADATA_ERROR,"Attribute \""+attrCollQ+"\" not found in the collection \""+colName+"\"");
//				}
//			}
//			// Check if classes and associated attributes exist
//			if(mp.getAocClause()!=null){
//				checkClassesAndAttr(colName,catego,mp.getAocClause().getlistClass(),(mp.getAoacClause()!=null)?mp.getAoacClause().getAttributeList():null);
//			} 
//			// Verif if qualifier exist
//			if(mp.getQualTab()!=null){
//				String[] qualsDB = mr.getQualifier_names().toArray(new String[0]);
//				for(Qualifier qual:mp.getQualTab()){
//					if(!QEToolBox.isInList(qual.getQual().getName(),qualsDB)){QueryException.throwNewException(SaadaException.METADATA_ERROR,"Qualifier \""+qual.getQual().getName()+"\" not found in the metadata cache for the relation \""+relationName+"\" ");}    
//				}
//			}  
//		}
//	}
//	/**
//	 * @param colName
//	 * @param category
//	 * @param classNames
//	 * @param attr_names
//	 * @throws SaadaException
//	 */
//	private static final void checkClassesAndAttr(String colName,int category,String[] classNames,String[] attr_names) throws SaadaException{
//		if(classNames[0].equals("*")) return;
//		String[] listClassInColl = Database.getCachemeta().getClassesOfCollection(colName,category);
//		for(String className:classNames){
//			// Verif if classe exists
//			if(!QEToolBox.isInList(className,listClassInColl))QueryException.throwNewException(SaadaException.METADATA_ERROR,"Classe \""+className+"\" not found in the metadata cache for collection \""+colName+"\"");
//			if(attr_names!=null){
//				// Verif if attributes exist for the class
//				Set<String> attrClassDB = Database.getCachemeta().getClass(className).getClassAttribute_names();
//				for(String attrClassQ:attr_names){
//					if(!attrClassDB.contains(attrClassQ)) QueryException.throwNewException(SaadaException.METADATA_ERROR,"Attribute \""+attrClassQ+"\" not found in the classe \""+className+"\"");
//				}
//			}
//		}
//	}
//
//	/**
//	 * @return
//	 */
//	public final String explain() {
//		return this.getReport();
//	}
//	/**
//	 * @return Returns the queryStr.
//	 */
//	public final String getQueryStr() {
//		return queryStr;
//	}
//	/**
//	 * @return Returns the sfiClause.
//	 */
//	public final SelectFromIn getSfiClause() { 
//		return sfiClause;
//	}
//	/**
//	 * @return Returns the wacClause.
//	 */
//	public WhereAttributeClass getWacClause() {
//		return wacClause;
//	}
//	/**
//	 * @return Returns the wasClause.
//	 */
//	public final WhereAttributeSaada getWasClause() {
//		return wasClause;
//	}
//	/**
//	 * @return Returns the wpClause.
//	 */
//	public final WherePosition getWpClause() {
//		return wpClause;
//	}
//	/**
//	 * @return Returns the wrClause.
//	 */
//	public final WhereRelation getWrClause() {
//		return wrClause;
//	}
//	/**
//	 * @return Returns the wuClause.
//	 */
//	public final WhereUCD getWuClause() {
//		return wuClause;
//	}
//	/**
//	 * @return Returns the index_owner_key.
//	 */
//	public final long getIndex_owner_key() {
//		return index_owner_key;
//	}
//
//
//	/**
//	 * @return Returns the matchCounterpartQueries.
//	 */
//	public HashMap<String, CounterpartSelector> getMatchingCounterpartQueries() {
//		return matchingCounterpartQueries;
//	}
//	
//	/**
//	 * @return Returns the unlimited_result_size.
//	 */
//	public int getUnlimited_result_size() {
//		return unlimited_result_size;
//	}
//	
//	public static void main(String[] args) throws Exception {
//		Messenger.debug_mode = false;
//		Messenger.debug_mode = false;
//		ArgsParser ap = new ArgsParser(args);
//		Database.init(ap.getDBName());
//		Database.getConnector().setAdminMode(null);
//		String[] queries = {
//				/*"Select ENTRY From * In *"
//			  , "Select ENTRY  From * In SelectedXGPS WhereAttributeSaada{ namesaada like '2XMM%' }"
//			  , "Select ENTRY From * In SelectedXGPS WhereRelation { matchPattern { Qualificateur } }"
//			  , "Select ENTRY  From * In SelectedXGPS WhereAttributeSaada{ namesaada like '2XMM%' } WhereRelation { matchPattern { Qualificateur }}"
//			  , "Select IMAGE From * In VoDMinPractice WhereAttributeSaada { namesaada like 'D%' } WhereUType {[SpectralAxis.coverage.location.coord.Spectral.Value] > 0 [unit] } Order By pos_ra_csa"
//			  , "Select IMAGE From * In * WhereUType {  [SpectralAxis.coverage.location.coord.Spectral.Value] != 1 [none] }"
//			  , "Select IMAGE From * In * WhereUType { [SpectralAxis.coverage.location.coord.Spectral.Value] [] (1,2) [unit] }"
//				/*, "Select ENTRY From * In MyCollection WhereRelation { matchPattern { FindigCharts }}"
//			  , "Select ENTRY From * In * Limit 5 Order By  namesaada"
//				"Select ENTRY  From CatalogueEntry In CATALOGUE WhereAttributeSaada{XCAT_OBSID = 'qqqqqq'}"
//				"Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation{ matchPattern{CatSrcToArchSrc, Qualifier{identif_proba [] (1,2) }} } "
//				*/
//				/*"Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation{ matchPattern{CatSrcToArchSrc, AssObjClass{arch_2282AEntry}, Qualifier{epic_cat_dist < 5} , Qualifier{identif_proba > 0.8} } }"*/
//				/*"Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation{ matchPattern{CatSrcToArchSrc, AssObjClass{arch_2282AEntry}, Qualifier{identif_proba > 0.8} } }"*/
//				//"Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation{ matchPattern{CatSrcToEpicSpect} matchPattern{CatSrcToArchSrc, Cardinality = 0, AssObjClass{arch_2282AEntry}} }"
//				//"Select ENTRY From CatalogueEntry In CATALOGUE WhereRelation{  matchPattern{CatSrcToArchSrc, Cardinality = 0, AssObjClass{arch_2282AEntry}} matchPattern{CatSrcToEpicSpect}}"
//				//"Select ENTRY From * In VIZIERData WhereUCD {[phys.veloc;pos.heliocentric] > 1000 [m/s]}" 
//		   //"Select ENTRY  From * In * WherePosition{ isInCircle(\"-45-45\",100.0,J2000,ECLIPTIC)}" 
////				"Select ENTRY  From CatalogueEntry In CATALOGUE "
////				+ " WhereRelation{"
////				+   " matchPattern{ObjClass,"
////				+       " Cardinality = 0,"
////				+       " Qualifier{sample = 1 }}"
////				+  " matchPattern{CatSrcToArchSrc,"
////				+       " AssObjClass{arch_2282AEntry}}"
////				+ " }"
//				"Select ENTRY  From * In PatternPrimaire "
//				+ " WhereRelation{"
//				+   " matchPattern{PatternTesteur,"
//				+       " Cardinality = 0,"
//				+       " AssObjClass{classe_s1Entry}}"
//				+ " }"
//				};
//		QueryOrg q = new QueryOrg();
//		q.runQuery(queries[0]);
//		System.out.println(q.getReport());
//		System.exit(1);
//		for( String query: queries) {
//			System.out.println("Query " + query);
//			SaadaQLResultSet srs = null;
//			try {
//				srs = q.runQuery(query);
//			} catch(Exception e) {
//				e.printStackTrace();
//				System.exit(1);
//			}
//			if( srs == null ) {
//				System.out.println("!!!! Retour null" + q.getErrorReport());
//				System.exit(1);
//			}
//			ArrayList<Long> sequ = new ArrayList<Long>();
//			ArrayList<Long> rand = new ArrayList<Long>();
//			while( srs.next()) {
//				long oid = srs.getOid();
//				sequ.add(oid);
//				for( String s: srs.getCol_names().keySet()) {
//					AttributeHandler col = srs.getCol_names().getSQLColumnHandler(s);
//					System.out.print(col.getNameattr() + "=" + srs.getObject(col.getNameattr()) + " " + srs.getSize());
//				}
//				System.out.print("\n");
//				System.exit(1);
//			}
//			int lg = srs.getSize();
//			if( lg != sequ.size()) {
//				System.out.println("sequ size = " + sequ.size() + " rand size = " + lg);
//				System.exit(1);
//			}
//			System.out.println(" " + lg + " rows returned");
//			for( int i=0 ; i<lg ; i++ ) {
//				if( !srs.setCursor(i) ) {
//					System.out.println(" cannot go in row #" + i);
//					System.exit(1);					
//				}
//				long oid = srs.getOid();
//				rand.add(oid);
//			}		
//			boolean failed = false;
//			for( int i=0 ; i<lg ; i++ ) {
//				long s = sequ.get(i);
//				long r = rand.get(i);
//				if( s != r) {
//					failed = true;
//					System.out.println("  row #" + i + " seq=" + s + " <> rand=" + r);
//				}
//			}
//			if( !failed ) {
//				System.out.println("  OK");
//				System.out.print("  Returned AH: ");
//				for( AttributeHandler ah: q.buildListAttrHandPrinc()) {
//					System.out.print(ah.getNameattr() + " " );
//					
//				}
//				System.out.print("\n  Returned columns: ");
//				for( String s: srs.getCol_names().keySet()) {
//					AttributeHandler col = srs.getCol_names().getSQLColumnHandler(s);
//					System.out.print(col.getNameattr() + " " );
//				}
//				System.out.println("");
//				System.out.println(q.getReport());
//			}
//		}
//		
//	}

}