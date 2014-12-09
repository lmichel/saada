package saadadb.sqltable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.List;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * Badly named class updating the saada_metaclass_xxxx after a class has been changed or created
 * @author michel
 * @version $Id$
 */
public class Table_Saada_Metacat{
	private String tableName = "";
	private String className = "";
	private String collection = "";
	private List<AttributeHandler> att_handler;

	public Table_Saada_Metacat(String classname, String collname, int category, List<AttributeHandler> att_handler) {
		this.className = classname;
		this.collection = collname;
		this.att_handler = att_handler;
		switch( category ) {
		case Category.IMAGE:
			this.tableName = "saada_metaclass_image";
			break;        	
		case Category.SPECTRUM:
			this.tableName = "saada_metaclass_spectrum";
			break;
		case Category.TABLE:
			this.tableName = "saada_metaclass_table";
			break;
		case Category.ENTRY:
			this.tableName = "saada_metaclass_entry";
			break;
		case Category.MISC:
			this.tableName = "saada_metaclass_misc";
			break;
		}
	}

	/**
	 * @param className
	 * @throws AbortException
	 * @throws SaadaException 
	 */
	public void updateUCDTable(int class_id) throws AbortException, SaadaException{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Updating the UCD table " + this.tableName + " for class " + this.className);
		/*
		 * class_id is computed after everyhng is OK.
		 * That is why it is only set here
		 */
		int coll_id = Database.getCachemeta().getCollection(this.collection).getId();
		for(int i=0 ; i<att_handler.size(); i++){
			AttributeHandler hdl = ((att_handler.get(i)));
			hdl.setClassname(className);
			hdl.setCollname(collection);
			hdl.setCollid(coll_id);
			hdl.setClassid(class_id);
			hdl.setLevel('B');
		}
		removeClassFromUCDTable();
		insertClassIntoUCDTable();		
	}


	/**
	 * @throws AbortException
	 */
	private void removeClassFromUCDTable() throws AbortException{
		String query;
		for( AttributeHandler ah: att_handler){
			query = "DELETE FROM " + this.tableName 
			+ " WHERE class_id = " 
			+ ah.getClassid() ;
			SQLTable.addQueryToTransaction(query,  this.tableName);
			break;
		}
	}

	/**
	 * @throws AbortException
	 */
	private void insertClassIntoUCDTable()throws SaadaException{   
		String dumpfile = Repository.getTmpPath() + Database.getSepar()  + this.tableName + ".psql";
		try {
			SQLQuery q = new SQLQuery("SELECT max(pk) FROM " + this.tableName );
			ResultSet rs = q.run();
			int k=0;
			while( rs.next() ){
				k = rs.getInt(1);
				break;
			}
			q.close();
			BufferedWriter bustmpfile = new BufferedWriter(new FileWriter(dumpfile));
			for(AttributeHandler hdl: att_handler){
				k++;
				String s = hdl.getDumpLine(k) + "\n";
				bustmpfile.write(s);
			}
			bustmpfile.close();
			SQLTable.addQueryToTransaction("LOADTSVTABLE " + this.tableName + " -1 " + dumpfile.replaceAll("\\\\", "\\\\\\\\")) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//
//
//		for(int i=0 ; i<att_handler.size(); i++){
//			AttributeHandler hdl = (att_handler.get(i));
//			String query = "INSERT INTO " + this.tableName + " " + AttributeHandler.getInsertStatement() + "VALUES\n";
//			query +=  hdl.getInsertValues(i);
//			SQLTable.addQueryToTransaction(query, this.tableName);
//		}
	}

}
