package adqlParser;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.query.result.ADQLResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.sqltable.SQLLargeQuery;
import saadadb.sqltable.SQLQuery;
import adqlParser.parser.AdqlParser;
import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLOrder;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;
import adqlParser.query.ColumnReference;

public class SaadaADQLQuery extends ADQLQuery {
	private SQLQuery sqlquery;
	public SaadaADQLQuery() { super(new SaadaSQLTranslator()); }
	
	public SaadaADQLQuery(SQLTranslator trans) { super(trans); }
	
	public HashMap<String, AttributeHandler> getColumnsMeta(){
		HashMap<String,AttributeHandler> hm = new HashMap<String,AttributeHandler>();
		
		for(ADQLOperand col : lstColumns){
			if (col instanceof ADQLColumnAndMeta){
				ADQLColumnAndMeta col2 = (ADQLColumnAndMeta)col;
				if (col2.getMeta() != null) {
					hm.put(col2.getColumn(), col2.getMeta());
				}
			}
		}
		
		return hm;
	}
	
	public ResultSet runQuery() throws Exception{
		sqlquery = new SQLLargeQuery(toSQL());
		return sqlquery.run();
	}
	
	/**
	 * Close the sql statement, autrement SQLITE est pas content
	 * @throws QueryException
	 */
	public void close() throws QueryException {
		if( sqlquery != null ) {
			sqlquery.close();
		}
	}
	
	public ADQLObject getCopy() throws ParseException {
		ADQLQuery copy = (translator == null)?(new SaadaADQLQuery()):(new SaadaADQLQuery(translator.getCopy()));
		
		// SELECT:
		for(ADQLOperand op: lstColumns)
			copy.addSelectColumn((ADQLOperand)op.getCopy());
		
		// DISTINCT:
		copy.setDistinct(distinct);
		
		// FROM:
		for(ADQLTable table : lstTables)
			copy.addTable((ADQLTable)table.getCopy());
		
		// WHERE:
		if (where != null)
			copy.addConstraint((ADQLConstraint)where.getCopy());
		
		// GROUP BY:
		for(ColumnReference colRef : lstGroupBy)
			copy.addGroupBy((ColumnReference)colRef.getCopy());
		
		// HAVING:
		if (having != null)
			copy.addHaving((ADQLConstraint)having.getCopy());
		
		// ORDER BY:
		for(ADQLOrder orderItem : order)
			copy.addOrder((ADQLOrder)orderItem.getCopy());
		
		// LIMIT:
		copy.setLimit(limit);
		
		return copy;
	}
	
	@Override
	public String toSQL() throws ParseException {
		return toSQL(false, translator);
	}

	@Override
	public String toSQL(boolean end) throws ParseException {
		return toSQL(end, translator);
	}
	
	private String superToSQL(boolean end, SQLTranslator altTranslator) throws ParseException {
		return super.toSQL(end, altTranslator);
	}

	@Override
	public String toSQL(boolean end, SQLTranslator altTranslator) throws ParseException {
		SmartJoin smartJoin = new SmartJoin();
		Vector<SaadaADQLQuery> unionItems = smartJoin.readUcdsAndTransformQuery(this);
		if (unionItems.isEmpty()){
			SaadaADQLQuery copy = (SaadaADQLQuery)getCopy();
			// @@@@ smartJoin suppressed from 1.6.5
			//copy = smartJoin.applySmartJoin(copy);
			return copy.superToSQL(end, altTranslator);
		}else{
			String sql = null;
			for(SaadaADQLQuery q : unionItems){
				sql = ((sql==null)?"":(sql+"\nUNION\n"))+smartJoin.applySmartJoin(q).superToSQL(end, altTranslator);
			}
			// ORDER BY:
			String orderList = null;
			for(ADQLOrder colOrder : order)
				orderList = (orderList == null)?colOrder.toSQL(altTranslator):(orderList + ", "+colOrder.toSQL(altTranslator));
			if (orderList != null)
				sql += "\nORDER BY "+orderList;
			
			// LIMIT:
			if( limit < 0 || limit > NO_LIMIT)
				sql += "\nLimit "+NO_LIMIT;
			else
				sql += "\nLimit "+limit;
			
			if (end)
				sql += ";";
			
			return sql;
		}
	}

	public static final void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
			
	//	ADQLQuery q = new ADQLQuery();
	//	
	//	q.addSelectColumn(new ADQLColumn("oidsaada", true, "t", "OID"));
	//	q.addSelectColumn(new ADQLColumn("_s_ra", true, "t", "RA"));
	//	q.addSelectColumn(new ADQLColumn("_s_dec", true, "t", "DEC"));
	//	
	//	q.addTable("ObsCore", "t");
	//	
	//	q.setConstraint("_s_ra >= 7");
	//	
	//	q.setLimit(30);
	
		String str = "SELECT TOP 10 oidsaada, UCD(ObsCore, 'em.wl')\nFROM ObsCore WHERE \"_s_ra\" > 7;";
		str = ap.getQuery();
		SaadaDBConsistency dbConsistency = new SaadaDBConsistency();
		AdqlParser parser = new AdqlParser(new ByteArrayInputStream(str.getBytes()), null, dbConsistency, new SaadaQueryBuilderTools(dbConsistency));
		SaadaADQLQuery q = (SaadaADQLQuery)parser.Query();
		
		System.out.println("\n### BEGIN ADQL ###\n"+str+"\n### END SQL ###\n");
		System.out.println("### BEGIN SQL ###\n"+q.toSQL()+"\n### END SQL ###\n");
		
		SaadaQLResultSet rs = new ADQLResultSet(q.runQuery(), q.getColumnsMeta());
		q.close();
		System.out.println("\n### Nb rows: "+rs.getSize()+" ; Nb columns: "+rs.getMeta().size()+"\n");
		
		System.out.println("\nColumns:");
		Iterator<AttributeHandler> it = rs.getMeta().iterator();
		rs.next();
		while(it.hasNext()){
			AttributeHandler tmp = it.next();
			System.out.println("\t- "+tmp.getNameattr()+" ("+tmp.getUtype()+", "+tmp.getUnit()+", "+tmp.getType()+") = \""+rs.getObject(tmp.getNameattr())+"\"");
		}
		
		System.out.println("\nTables & Meta:");
		Iterator<ADQLTable> itTables = q.getTables();
		while(itTables.hasNext()){
			SaadaADQLTable table = (SaadaADQLTable)itTables.next();
			if (table.isSubQuery())
				System.out.println("\t- "+table.getAlias()+" [SUB QUERY]");
			else
				System.out.println("\t- "+table+" ["+(table.isClass()?("class="+table.getMetaClass().getName()+" & collection="+table.getMetaCollection().getName()+"_"+table.getMetaClass().getCategory_name()):("collection="+table.getMetaCollection().getName()))+"]");
		}
		Database.close();
	}
	


}