package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.ArrayList;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
/**
 * <p>Title: SAADA </p>
 * <p>Description: Automatic Archival System For Astronomical Data -
    This is framework of a PhD funded by the CNES and by the Region Alsace.</p>
 * <p>Director of research: L.Michel and C. Motch.</p>
 * <p>Copyright: Copyright (c) 2002-2005</p>
 * <p>Company: Observatoire Astronomique Strasbourg-CNES</p>
 * @version SAADA 1.0
 * @author: MICHEL Laurent
 * E-Mail: michel@astro.u-strasbg.fr</p>
 */
public class UCDTableHandler{
	private String table_ucd = "";
	private String classname = "";
	private String collection = "";
	private ArrayList<AttributeHandler> att_handler;

	public UCDTableHandler(String classname, String collname, int category, ArrayList<AttributeHandler> att_handler) {
		this.classname = classname;
		this.collection = collname;
		this.att_handler = att_handler;
		switch( category ) {
		case Category.IMAGE:
			this.table_ucd = "saada_metaclass_image";
			break;        	
		case Category.SPECTRUM:
			this.table_ucd = "saada_metaclass_spectrum";
			break;
		case Category.TABLE:
			this.table_ucd = "saada_metaclass_table";
			break;
		case Category.ENTRY:
			this.table_ucd = "saada_metaclass_entry";
			break;
		case Category.MISC:
			this.table_ucd = "saada_metaclass_misc";
			break;
		}
	}

	/**
	 * @param classname
	 * @throws AbortException
	 * @throws SaadaException 
	 */
	public void updateUCDTable(int class_id) throws AbortException, SaadaException{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Updating the UCD table " + this.table_ucd + " for class " + this.classname);
		/*
		 * class_id is computed after everyhng is OK.
		 * That is why it is only set here
		 */
		int coll_id = Database.getCachemeta().getCollection(this.collection).getId();
		String sql="";
		for(int i=0 ; i<att_handler.size(); i++){
			AttributeHandler hdl = ((att_handler.get(i)));
			hdl.setClassname(classname);
			hdl.setCollname(collection);
			hdl.setCollid(coll_id);
			hdl.setClassid(class_id);
			hdl.setLevel('B');
			sql +=  ", " + hdl.getNameattr();
		}
		removeClassFromUCDTable();
		insertClassIntoUCDTable();
		/*
		 * TODO This strange operation  makes sure than SQL columns have the same order as the declared fields.
		 * That might be done with more efficiency and with keeping the constraint on oidsaada
		 */
		/*
		 * Must unlock table otherwise mySQL ask to lock a non existent table (tmp_ +  classname)!!
		 * http://bugs.mysql.com/bug.php?id=12472
		 */
		SQLTable.unlockTables();	
		
		SQLTable.addQueryToTransaction("DROP TABLE IF EXISTS  tmp_" + classname ) ;
		SQLTable.addQueryToTransaction(Database.getWrapper().getCreateTableFromSelectStatement("tmp_" +  classname
				, "SELECT oidsaada, namesaada, md5keysaada" + sql + " FROM " + classname)) ;
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		/*
		 * SQLITE cannot drop a table during a transaction containing others db updates.
		 * The taht is renamed. TIt must be dropped in a further atomic transaction
		 */
		SQLTable.addQueryToTransaction("DROP TABLE  " + classname) ;
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("ALTER TABLE  tmp_" + classname + " RENAME TO " + classname) ;
		/*
		 * Index name in lower case to avoid case issues
		 */
		SQLTable.addQueryToTransaction("CREATE UNIQUE INDEX " + classname.toLowerCase() + "_oidsaada ON " + classname + "(oidsaada)") ;
		SQLTable.lockTables(Database.getWrapper().getUserTables(), null);
		SQLTable.addQueryToTransaction(Database.getWrapper().grantSelectToPublic(classname));	
	}
	

	/**
	 * @throws AbortException
	 */
	private void removeClassFromUCDTable() throws AbortException{
		String query;
		for(int i=0 ; i<att_handler.size(); i++){
			query = "DELETE FROM " + this.table_ucd 
			+ " WHERE class_id = " 
			+ ((att_handler.get(i))).getClassid() ;
			SQLTable.addQueryToTransaction(query,  this.table_ucd);
			break;
		}
	}

	/**
	 * @throws AbortException
	 */
	private void insertClassIntoUCDTable()throws SaadaException{   
		for(int i=0 ; i<att_handler.size(); i++){
			AttributeHandler hdl = (att_handler.get(i));
			String query = "INSERT INTO " + this.table_ucd + " " + AttributeHandler.getInsertStatement() + "VALUES\n";
			query +=  hdl.getInsertValues(i);
			SQLTable.addQueryToTransaction(query, this.table_ucd);
		}
	}

	private ArrayList<String> getClassesOfCollection(String coll_name, String table){
		String query = "select distinct name_class from " + table  + " where name_coll = '" + coll_name + "'";
		ArrayList<String> retour = new ArrayList<String>();
		try{
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run(query);
			while(rs.next()){
				retour.add(rs.getString("name_class").trim());
			}
			squery.close();
			return retour;    
		}catch(Exception e){
			Messenger.printMsg(Messenger.ERROR, e.getMessage() + "(" + query + ")");
			Messenger.printStackTrace(e);
			return null;    
		}
	}

	public ArrayList<String> getTableClassesOfCollection(String coll_name){
		return this.getClassesOfCollection(coll_name, "saada_metaclass_table");
	}

	public ArrayList<String> getEntryClassesOfCollection(String coll_name){
		return this.getClassesOfCollection(coll_name, "saada_metaclass_entry");
	}

	public ArrayList<String> getImageClassesOfCollection(String coll_name){
		return this.getClassesOfCollection(coll_name, "saada_metaclass_image");
	}

	public ArrayList<String> getSpectraClassesOfCollection(String coll_name){
		return this.getClassesOfCollection(coll_name, "saada_metaclass_spectrum");
	}

	public ArrayList<String> getSpectraClassesOfCollection(String coll_name, String type){
		return this.getClassesOfCollection(coll_name, "saada_metaclass_"+type);
	}
}
