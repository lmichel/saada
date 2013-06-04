package saadadb.meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.SaadaConstant;

public class MetaRelation extends MetaObject {

	String      primary_coll;
	int         primary_category;
	String      secondary_coll;
	int         secondary_category;
	String      correlator;
	String      description;
	boolean 	indexed;
	int         stat;
	
	int size = -1;
	
	ArrayList<String> 	qualifier_names = new ArrayList<String>();

	public MetaRelation(ResultSet rs) throws Exception {
		super(rs);
		this.primary_coll       = rs.getString("primary_coll").trim();
		this.primary_category   = Category.getCategory(rs.getString("primary_cat").trim());
		this.secondary_coll     = rs.getString("secondary_coll").trim();
		this.secondary_category = Category.getCategory(rs.getString("secondary_cat").trim());
		this.correlator         = rs.getString("correlator");
		this.description        = rs.getString("description");
		try {
		this.stat               = rs.getInt("stat");
		} catch (SQLException e) {
			this.stat = SaadaConstant.INT;
		}
		if( this.correlator == null ) {
			this.correlator = "";
		}
		SQLQuery squery = new SQLQuery();
		ResultSet qrs = squery.run("select qualifier from saada_qualifier where name = '" + this.name + "'");
		while(qrs.next()) {
			qualifier_names.add(qrs.getString(1));
		}	
		squery.close();
		this.indexed = Table_Saada_Relation.isIndexed(this.name);

	}

	public MetaRelation(String name, int id) {
		super(name, id);
	}

	public int getStat() {
		return stat;
	}

	/**
	 * @return Returns the primary_category.
	 */
	public int getPrimary_category() {
		return primary_category;
	}

	/**
	 * @return Returns the primary_coll.
	 */
	public String getPrimary_coll() {
		return primary_coll;
	}

	/** * @version $Id$

	 * @return Returns the secondary_category.
	 */
	public int getSecondary_category() {
		return secondary_category;
	}

	/**
	 * @return Returns the secondary_coll.
	 */
	public String getSecondary_coll() {
		return secondary_coll;
	}


	/**
	 * @return Returns the correlator.
	 */
	public String getCorrelator() {
		return correlator;
	}

	/**
	 * @return Returns the qualifier_names.
	 */
	public ArrayList<String> getQualifier_names() {
		return qualifier_names;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param rel_name
	 * @return
	 * @throws FatalException 
	 */
	public boolean isIndexed() throws FatalException {
		return this.indexed;
	}

	/**
	 * @return
	 */
	public String getLabel() {
		if( this.getDescription() != null && this.getDescription().length() > 0 ) {
			return this.getDescription();
		}
		else {
			return this.getName();
		}
	}
	/**
	 * Returns ythe number of links
	 * @return
	 * @throws Exception
	 */
	public int getSize() throws Exception {
		if( this.size == -1 ) {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT count(oidprimary) FROM " + this.name);
			while( rs.next() ) {
				this.size = rs.getInt(1);
				break;
			}
			squery.close();			
		}
		return this.size ;
	}

	/**
	 * Returns an HTML formated text describing the relationship
	 * If classe is not null, the number of links targeting that class a added to the description
	 * @param classe
	 * @return
	 * @throws Exception
	 */
	public String getHTMLSummary(String classe) throws Exception {

		String content = "<LI><B>Content </B>" + this.getSize() + " links ";
		if( classe != null ) {
			MetaClass mc = Database.getCachemeta().getClass(classe);
			String starting = "oidsecondary";
			if( mc.getCollection_name().equals(this.getPrimary_coll()) && mc.getCategory() == this.getPrimary_category()) {
				starting = "oidprimary";
			}
			content += " including ";
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT count(*) FROM " + this.name + " WHERE " + SaadaOID.getSQLClassFilter(starting) + " = " + mc.getId());
			while( rs.next() ) {
				content += rs.getString(1);
				break;
			}	
			squery.close();
			if( starting.equals("oidprimary")) {
				content += " starting from ";					
			}
			else {
				content += " coming to ";										
			}
			content += " class <I>" + classe + "</I>";
		}
		String[] qls = this.getQualifier_names().toArray(new String[0]);
		String quals = "<LI>";
		if( qls.length == 0 ) {
			quals += "<B>No Qualifier</B>";
		}
		else {
			quals += "<B>Qualifiers </B>";	
			for( String q: qls) {
				quals += q + " ";
			}
		}
		return "<B>Relationship </B>" + this.getName() 
		+ "<UL><LI><B>From </B><I>" + Category.explain(this.getPrimary_category()) + "</I> of collection <I>" + this.getPrimary_coll()+ "</I>"
		+ "<LI><B>To </B><I>" + Category.explain(this.getSecondary_category()) + "</I> of collection <I>" + this.getSecondary_coll()+ "</I>"
		+ quals
		+ "<LI><B>Stored Correlator</B><PRE>" +  this.getCorrelator() +"</pre>"
		+ content
		+ "<LI><B>Description</B><PRE>" + this.getDescription().trim()+"</pre></UL>";
	}

	@Override
	public String toString() {
		try {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT count(*) FROM " + this.name);
			String content = "";
			while( rs.next() ) {
				content += rs.getString(1);
				break;
			}
			squery.close();
			content += " links ";

			String[] qls = this.getQualifier_names().toArray(new String[0]);
			String quals = "";
			if( qls.length == 0 ) {
				quals += "No Qualifier\n";
			}
			else {
				for( String q: qls) {
					quals += q + " ";
				}
			}
			return "Relationship " + this.getName() + "\n"
			+ "- From " + Category.explain(this.getPrimary_category()) + " of collection " + this.getPrimary_coll()+ "\n"
			+ "- To " + Category.explain(this.getSecondary_category()) + " of collection " + this.getSecondary_coll()+ "\n"
			+ "- Qualifiers  :" + quals +"\n"
			+ "- Stored Correlator:\n" +  this.getCorrelator() +"\n"
			+ "- Content     :" + content +"\n"
			+ "- Description :" + this.getDescription().trim()+"\n"
			+ "- Indexed     :" + this.isIndexed();
		} catch(Exception e ) {
			e.printStackTrace();
			return null;
		}
	}

}
