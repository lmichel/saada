package saadadb.query.matchpattern;

import java.util.LinkedHashMap;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.meta.MetaRelation;
import saadadb.meta.VOResource;
import saadadb.query.constbuilders.MatchPattern;
import saadadb.query.constbuilders.SaadaQLConstraint;
import saadadb.query.constbuilders.UCDField;
import saadadb.query.constbuilders.UTypeField;
import saadadb.query.merger.Merger;
import saadadb.query.parser.SelectFromIn;

/** * @version $Id$

 * @author michel
 * Build the SQL query requested by the pattern
 */
public class PatternSQLBuilder {
	private String sqlquery = null;
	private Merger merger = null;

	/**
	 * @param mp
	 * @throws Exception 
	 */
	public PatternSQLBuilder(MatchPattern mp, VOResource vor) throws Exception  {
		AssObjAttSaada aoasClause = mp.getAoasClause();
		AssObjClass    aocClause  = mp.getAocClause();
		AssObjAttClass aoacClause = mp.getAoacClause();
		AssUCD         auClause   = mp.getAuClause();
		AssUType       autClause  = mp.getAutClause();
		AssDM          dmClause   = mp.getDmClause();
		if(aoasClause != null || aocClause != null || aoacClause != null||  auClause != null || autClause!= null ) {
			MetaRelation   mr = Database.getCachemeta().getRelation(mp.getRelation());
			LinkedHashMap<String, SaadaQLConstraint>builders = new LinkedHashMap<String, SaadaQLConstraint>();
			SelectFromIn sfiClause = new SelectFromIn("Select " 
					+ Category.explain(mr.getSecondary_category() ) 
					+ " From " 
					+ ((aocClause == null)?"*" : saadadb.util.Merger.getMergedArray(aocClause.getlistClass()) )
					+ " In " + mr.getSecondary_coll());
			merger = new Merger(sfiClause, false);
			if( aoasClause != null ) {
				builders.put("native", aoasClause.getSaadaQLConstraint());
			}
			if( aoacClause != null ) {
				builders.put("native", aoacClause.getSaadaQLConstraint());
			}
			if( auClause != null ) {
				for( UCDField field: auClause.getSaadaQLConstraints()) {
					builders.put(field.getSqlcolnames()[0], field);				
				}
			}
			if( dmClause != null ) {
				for( UTypeField field: dmClause.getSaadaQLConstraints()) {
					builders.put(field.getSqlcolnames()[0], field);				
				}
			}
			merger.setBuilderList(builders);
			sqlquery = merger.getSQL();	
		}
	}
	
	/**
	 * @return
	 */
	public String getSQLquery() {
		return sqlquery;
	}
	
	/**
	 * @return
	 */
	public String[] getCoveredClasses() {
		if( this.merger == null) {
			return null;
		}
		else {
			return merger.getCoveredClasses();
		}
	}
}
