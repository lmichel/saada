package saadadb.query.merger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.util.RegExp;


public abstract class QNode {
	protected LinkedHashMap<String, ColumunSelectDef> select = new LinkedHashMap<String, ColumunSelectDef>();
	protected String  where = "";
	protected final String  name;
	protected Merger merger;
	public static final String SEPAR_ON = "[,\\s\\(\\+\\-\\*/]";
	public static final String SEPAR_OFF = "[,\\s\\)\\+\\-\\*/]";

	/**
	 * @param name
	 */
	public QNode(String name, Merger merger) {
		this.name = name;
		this.merger = merger;
	}

	/**
	 * @param alias
	 * @return
	 */
	public String getSQL(String alias) {
		String retour = "";
		if( select.size() == 0 ) {
			retour = this.name + ".*"; 
		}
		else {
			for( Entry<String, ColumunSelectDef> e: select.entrySet()) {
				if( retour.length()  !=  0) {
					retour += ", ";
				}
				retour += this.name + "." + e.getValue() + " as " + e.getKey();
			}
		}
		if( where.length() == 0 ) {
			return   "SELECT " + retour + " FROM " + this.name ;
		}
		else {
			return  "SELECT " + retour + " FROM " + this.name + " WHERE " + where;
		}

	}

	/**
	 * @return
	 */
	public String getSelect(){
		String retour = "";
		if( select.size() != 0 )  {
			for( Entry<String, ColumunSelectDef> e: select.entrySet()) {
				if( retour.length()  !=  0) {
					retour += ", ";
				}
				ColumunSelectDef value = e.getValue();
				retour += value.getSQLColumnDef(this.name);
			}
		}
		return retour ;
	}
	/**
	 * @return
	 */
	public String getWhere(){
		return this.where;
	}


	/**
	 * @param result_colname
	 * @param scb
	 * @param selectColSet 
	 * @param unreadAttributes 
	 * @throws QueryException
	 */
	public void readSaadaQLConstraint(String result_colname, SaadaQLConstraint scb) throws QueryException {
		for(String sql_colname: scb.getSqlcolnames())  {

			String rcn;
			if( scb.isNative() || scb.isGlobal() ) {
				rcn = SaadaQLConstraint.getNativeResultColName(result_colname, sql_colname); 
			}
			else if( scb.isPosition() ) {
				rcn = SaadaQLConstraint.getPositionResultColName(sql_colname); 
			}
			else {
				rcn = result_colname;
			}
			//sql_colname = insertAlias(sql_colname, this.getBusinessAttributesHandlers().keySet().toArray(new String[0]), this.name)	;	
			ColumunSelectDef csd = new ColumunSelectDef(sql_colname, rcn, ColumunSelectDef.STANDARD);
			this.addSelectedColumn(rcn, csd);
		}
		if( !scb.isGlobal() ) {
			String where = scb.getWhere();
			if( this.where.length() != 0 ) {
				this.where += " AND "; 
			}
			this.where += where;
		}
	}

	/**
	 * Add all native fields to the select clause
	 * Does nothing but for classes
	 * @throws QueryException
	 * @throws Exception 
	 */
	public void setAllcolumnsSelect() throws QueryException, Exception {}

	/**
	 * Does nothing but for classes
	 * @param result_column_name
	 * @param csd
	 */
	protected void addSelectedColumn(String result_column_name, ColumunSelectDef csd ){};

	/**
	 * @param builders
	 * @param unreadAttributes
	 * @throws QueryException
	 * @throws SaadaException
	 * @throws Exception 
	 */
	public abstract void readSaadaQLConstraints(LinkedHashMap<String, SaadaQLConstraint> builders) throws Exception ;
	/**
	 * @param scb
	 * @throws QueryException
	 * @throws SaadaException 
	 */
	public abstract void readColMappedSaadaQLConstraint(SaadaQLConstraint scb) throws QueryException, SaadaException ;

	/**
	 * @param scb
	 * @throws Exception 
	 */
	public abstract void readDMMappedSaadaQLConstraint(SaadaQLConstraint scb) throws Exception ;

	/**
	 * Adds 'alias.' before each  attribute of the list atts in the SQL statement source
	 * @param source
	 * @param atts
	 * @param alias
	 * @return
	 */
	public static String insertAlias(String source, String[] atts, String alias) {
		String retour = source.trim();
		/*
		 * Parcourir les attribut de classe 
		 */
		for( String att: atts) {
			/*
			 * In cas of DM mapping, the att can be a constant value: no alias in that case.
			 */
			boolean is_const = false;
			if( att.indexOf("'") >= 0 || att.matches(".*[\\(]null[\\):].*") || att.matches(".*[\\(']" + RegExp.NUMERIC + "[\\)':].*") ){
				is_const = true;
			}
			/*
			 * AS regexp operations are quite slow, we do them only for attributes possibly in the query
			 */
			if( !is_const &&  retour.indexOf(att) >= 0 ) {
				Pattern p = Pattern.compile("(?:^|" + SEPAR_ON + ")(" + att + ")(?:$|" + SEPAR_OFF + ")", Pattern.DOTALL | Pattern.MULTILINE);
				//				Pattern p = Pattern.compile("([\\s\\(\\+\\-\\*/]?|^)(" + att + ")[\\s\\)\\+\\-\\*/]?", Pattern.DOTALL);
				Matcher m = p.matcher(retour);
				ArrayList<Integer> insert = new ArrayList<Integer>();
				boolean found = false;
				while( m.find()  ) {
					insert.add(m.start(1));
					found = true;
				}
				if( found ) {
					for( int i=(insert.size()-1) ; i>=0 ; i--) {
						retour  = retour.substring(0, insert.get(i)) + alias + "." + retour.substring(insert.get(i));
					}
				}
			}
			else{

			}
		}
		return retour;
	}

	public static void main(String[] args) {
		System.out.println(QNode.insertAlias("WHERE ( namesaada - 2*(_z)) > 0 + R_MAG", new String[]{"_z"}, "POUET"));
	}
	public void insertAliases() {
		this.where = insertAlias(this.where, new String[]{"_qwery"}, this.name);
		this.where = insertAlias(this.where, new String[]{"xyz_csa"}, "MaCollection_SQLtable");
		//		/*
		//		 * Parcourir les attribut de classe 
		//		 */
		//		String atts[] = new String[]{"_qwery"};
		//		String new_where = "";
		//		for( String att: atts) {
		//			Pattern p = Pattern.compile("[\\s\\(\\+\\-\\*/]+(" + att + ")[\\s\\)\\+\\-\\*/]+", Pattern.DOTALL);
		//			Matcher m = p.matcher(this.where);
		//			ArrayList<Integer> insert = new ArrayList<Integer>();
		//			while( m.find()  ) {
		//				insert.add(m.start(1));
		//			}
		//			for( int i=(insert.size()-1) ; i>=0 ; i--) {
		//				this.where = this.where.substring(0, insert.get(i)) + this.name + "." + this.where.substring(insert.get(i));
		//			}
		//		}
		//		/*
		//		 * Parcourir les attribut de collection 
		//		 */
		//		String coll_name = "MaCollection_SQLtable";
		//		atts = new String[]{"xyz_csa"};
		//		for( String att: atts) {
		//			Pattern p = Pattern.compile("[\\s\\(\\+\\-\\*/]+(" + att + ")[\\s\\)\\+\\-\\*/]+", Pattern.DOTALL);
		//			Matcher m = p.matcher(this.where);
		//			ArrayList<Integer> insert = new ArrayList<Integer>();
		//			while( m.find()  ) {
		//				insert.add(m.start(1));
		//			}
		//			for( int i=(insert.size()-1) ; i>=0 ; i--) {
		//				this.where = this.where.substring(0, insert.get(i)) + coll_name + "." + this.where.substring(insert.get(i));
		//			}
		//		}
		//		return this.where;
	}

	/**
	 * @return
	 */
	abstract public Map<String, AttributeHandler> getBusinessAttributesHandlers() ;
	/**
	 * @return
	 * @throws FatalException
	 * @throws ClassNotFoundException
	 */
	abstract public Field[] getFields() throws FatalException, ClassNotFoundException ;

	/**
	 * @param ucd
	 * @return
	 */
	abstract public boolean hasUCD(String ucd);


}
