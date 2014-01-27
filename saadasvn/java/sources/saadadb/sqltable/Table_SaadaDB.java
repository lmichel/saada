package saadadb.sqltable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Merger;
import saadadb.util.Messenger;


public class Table_SaadaDB extends SQLTable {

	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saadadb"
				, "name text ,"
				+ "root_dir text , "
				+ "repository text , "
				+ "webapp_home text , "
				+ "url_root text , "
				+ "jdbc_driver text , "
				+ "jdbc_url text , "
				+ "jdbc_reader text , "
				+ "jdbc_reader_password text , "
				+ "jdbc_administrator text , "
				+ "coord_sys text , "
				+ "coord_equi double precision , "
				+ "spect_coord_unit text , "
				+ "spect_coord_type text , "
				+ "spect_flux_unit text , "
				+ "healpix_level int , "
		        + "description text "
				, null
				, false);
		SQLTable.addQueryToTransaction(
				Database.getWrapper().getInsertStatement("saadadb",
						new String[] {"name", "root_dir" , "repository", "webapp_home", "url_root" , "jdbc_driver",
						"jdbc_url", "jdbc_reader", "jdbc_reader_password", "jdbc_administrator", "coord_sys",
						"coord_equi", "spect_coord_unit", "spect_coord_type", "spect_flux_unit", "description"},
						new String[] {Merger.quoteString(Database.getConnector().getDbname())
						, Merger.quoteString(Database.getConnector().getRoot_dir().replaceAll("\\\\", "\\\\\\\\").replaceAll(" ", "\\ "))
						, Merger.quoteString(Database.getConnector().getRepository().replaceAll("\\\\", "\\\\\\\\").replaceAll(" ", "\\ ")  )
						, Merger.quoteString(Database.getConnector().getWebapp_home().replaceAll("\\\\", "\\\\\\\\").replaceAll(" ", "\\ "))
						, Merger.quoteString(Database.getConnector().getUrl_root())
						, Merger.quoteString(Database.getConnector().getJdbc_driver())
						, Merger.quoteString(Database.getConnector().getJdbc_url())
						, Merger.quoteString(Database.getConnector().getJdbc_reader())
						, Merger.quoteString(Database.getConnector().getJdbc_reader_password())
						, Merger.quoteString(Database.getConnector().getJdbc_administrator())
						, Merger.quoteString(Database.getConnector().getCoord_sys())
						, Double.toString(Database.getConnector().getCoord_equi())
						, Merger.quoteString(Database.getConnector().getSpect_unit())
						, Merger.quoteString(Database.getConnector().getSpect_type())
						, Merger.quoteString(Database.getConnector().getFlux_unit())
						, Integer.toString(Database.getConnector().getHealpix_level())
						, Merger.quoteString(Database.getConnector().getDescription() )}

				)
		        ,  "saadadb");
		//SQLTable.runQueryUpdateSQL("insert into saadadb DEFAULT VALUES", false, null);
	}
	
	/**
	 * Change the URL root of the database.
	 * Param is normally checked by @see saadadb.command.ArgsParser
	 * @param param_value
	 * @throws AbortException
	 */
	public static void changeURL(String param_value) throws AbortException {
		SQLTable.addQueryToTransaction("UPDATE saadadb SET url_root = '" +  param_value + "'", "saadadb");		
	}

	/**
	 * Change the base root directory of the database.
	 * Param is normally checked by @see saadadb.command.ArgsParser
	 * @param param_value
	 * @throws AbortException
	 */
	public static void changeBasedir(String param_value) throws SaadaException {
		SQLTable.addQueryToTransaction("UPDATE saadadb SET root_dir = '" +  param_value.replaceAll("\\\\", "\\\\\\\\").replaceAll(" ", "\\ ") + "'", "saadadb");		
		generateAntPropertiesFile(true)	;
	}
	/**
	 * Change the repository dir of the database.
	 * Param is normally checked by @see saadadb.command.ArgsParser
	 * @param param_value
	 * @throws AbortException
	 */
	public static void changeRepdir(String param_value) throws AbortException {
		SQLTable.addQueryToTransaction("UPDATE saadadb SET repository = '" +  param_value.replaceAll("\\\\", "\\\\\\\\").replaceAll(" ", "\\ ") + "'", "saadadb");		
	}
	/**
	 * Rename 
	 * the database.
	 * Param is normally checked by @see saadadb.command.ArgsParser
	 * @param param_value
	 * @throws AbortException
	 */
	public static void rename(String param_value) throws SaadaException {
		SQLTable.addQueryToTransaction("UPDATE saadadb SET name = '" +  param_value + "'", "saadadb");		
	}

	public static void changeTomcatdir(String param_value) throws SaadaException {
		SQLTable.addQueryToTransaction("UPDATE saadadb SET webapp_home = '" +  param_value.replaceAll("\\\\", "\\\\\\\\") + "'", "saadadb");	
		generateAntPropertiesFile(false)	;
	}
	
	/**
	 * Synchronize the ant resource file saadadb.properties with the saadadb table
	 * @param newfile: add .new to the file name if true
	 * @throws SaadaException
	 */
	private static void generateAntPropertiesFile(boolean newfile) throws SaadaException {
		String prop_filename =  Database.getRoot_dir() + Database.getSepar() + "bin" + Database.getSepar() + "saadadb.properties";
		//Database.getCachemeta().reload(true);
		FileWriter writer;
		try {
			if( Database.getConnector() == null ) {
				Database.initConnector(Database.getDbname(), true);
			}
			StringBuffer sb = new StringBuffer();
			FileReader in = new FileReader(prop_filename);
			BufferedReader reader =new BufferedReader(in);
			String str="";
			while ((str=reader.readLine())!=null) {
				if( str.startsWith("TOMCAT_HOME=")) {
					str = "TOMCAT_HOME=" + Database.getConnector().getWebapp_home() ;					
				}
				else if( str.startsWith("SAADA_DB_HOME=")) {
					str = "SAADA_DB_HOME=" + Database.getConnector().getRoot_dir() ;					
				}
				/*
				 * Faudra ajouter les autres champs plus tard
				 */
				sb.append(str + "\n");
			}
			reader.close();	
			if( newfile) {
				prop_filename += ".new";
			}
			writer = new FileWriter(prop_filename);
			writer.write(sb.toString());
			writer.close();
			Messenger.printMsg(Messenger.TRACE, "File <saadadb.properties> updated");
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.FILE_ACCESS, e);
		}
	}	
	
	/**
	 * Add the Healpix level
	 * @throws Exception
	 */
	public static final void addHealpixColumn() throws Exception {
		if( SQLTable.addColumn("saadadb", "healpix_level", "int") ) {
			SQLTable.beginTransaction();
			SQLTable.addQueryToTransaction("UPDATE saadadb SET healpix_level = 15");
			SQLTable.commitTransaction();
		} else {
			Messenger.printMsg(Messenger.TRACE, "Column healpix_level already exists in table saadadb");
		}
	}


}

