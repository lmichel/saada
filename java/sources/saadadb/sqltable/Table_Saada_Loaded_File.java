package saadadb.sqltable;

import java.io.BufferedWriter;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.ProductBuilder;
import saadadb.util.Messenger;

public class Table_Saada_Loaded_File {

	/**
	 * @throws AbortException
	 */
	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saada_loaded_file"
				, "filename " + Database.getWrapper().getIndexableTextType() 
				+ ", oidsaada bigint, repositoryname text,collection " + Database.getWrapper().getIndexableTextType() 
				+ ", classname " + Database.getWrapper().getIndexableTextType() + ", category text, md5_config text,md5_config_withoutcoll text, content_sign text"
				, ""
				, false);
		SQLTable.addQueryToTransaction("create index saada_loaded_file_filename on saada_loaded_file(filename)", "saada_loaded_file");
		SQLTable.addQueryToTransaction("create index saada_loaded_file_oidsaada on saada_loaded_file(oidsaada)", "saada_loaded_file");
		SQLTable.addQueryToTransaction("create index saada_loaded_file_classname on saada_loaded_file(classname)", "saada_loaded_file");
		SQLTable.addQueryToTransaction("create index saada_loaded_file_collection on saada_loaded_file(collection)", "saada_loaded_file");
	}

	/**
	 * Record the file in the saada_loaded_file table and returns the filename 
	 *  used by the repository. if rep_name is null, a name is derived from the porduct name. 
	 *  Rep_name is take otherwise
	 * @param prd
	 * @param rep_name
	 * @return
	 * @throws Exception 
	 */
	public static String recordLoadedFile(ProductBuilder prd, String rep_name) throws Exception{
		MetaClass mc = prd.getMetaclass();
		ProductMapping cdh = prd.getMapping();
		String coll_name = cdh.getCollection();
		String category = Category.explain(cdh.getCategory());
		String class_name;
		/*
		 * prd name can be either a filename or a full path.
		 * We need to work with the filename, not the path
		 */
		String prdname = (new File(prd.getName())).getName();

		/*
		 * No class for flatfiles
		 */
		if( mc == null ) {
			class_name = "FLATFILEUserColl";
		}
		else {
			class_name = mc.getName();
		}
		String repository_name;
		if( rep_name == null || rep_name.length() == 0 ) {
			repository_name = getRepositoryname(prdname, coll_name, category);
		}
		else {
			repository_name = rep_name;
		}

		String 		sql = "insert into saada_loaded_file  values('" 
			+ prdname + "','"
			+ prd.getActualOidsaada() + "','"
			+ repository_name + "','"
			+ coll_name + "','"
			+ class_name + "','"
			+ category + "','"
			+ cdh.getSignature() + "','" 
			+ cdh.getSignatureWithoutColl() + "','"
			+ prd.getContentSignature() + "')";
		SQLTable.addQueryToTransaction(sql, "saada_loaded_file");
		return repository_name;
	}

	/**
	 * Record the file in the saada_loaded_file table and returns the filename 
	 *  used by the repository. if rep_name is null, a name is derived from the porduct name. 
	 *  Rep_name is take otherwise loadedfilewriter
	 *  Store the query into the 
	 * @param prd
	 * @param rep_name
	 * @param loadedfilewriter
	 * @return
	 * @throws Exception
	 */
	public static String recordLoadedFile(ProductBuilder prd, String rep_name, BufferedWriter loadedfilewriter) throws Exception{
		MetaClass mc = prd.getMetaclass();
		ProductMapping cdh = prd.getMapping();
		String coll_name = cdh.getCollection();
		String category = Category.explain(cdh.getCategory());
		String class_name;
		/*
		 * No class for flatfiles
		 */
		if( mc == null ) {
			class_name = "FLATFILEUserColl";
		}
		else {
			class_name = mc.getName();
		}
		String repository_name;
		if( rep_name == null || rep_name.length() == 0 ) {
			repository_name = getRepositoryname(prd.getName(), coll_name, category);
		}
		else {
			repository_name = rep_name;
		}

		String 		sql =
			prd.getName() + "\t"
			+ prd.getActualOidsaada() + "\t"
			+ repository_name + "\t"
			+ coll_name + "\t"
			+ class_name + "\t"
			+ category + "\t"
			+ cdh.getSignature() + "'\t'" 
			+ cdh.getSignatureWithoutColl() + "'\t'"
			+ prd.getContentSignature() + "')\n";
		loadedfilewriter.write(sql);

		return repository_name;
	}

	/**
	 * Add to the current transaction a query removing all references to loaded file
	 * @param collection  : Collection of files to be removed
	 * @param category    : category of files to be removed
	 * @throws Exception
	 */
	public static void removeLoadedFiles(String collection, int category) throws Exception {
		SQLTable.addQueryToTransaction("DELETE FROM saada_loaded_file WHERE collection = '" + collection + "' AND category = '" + Category.explain(category) + "'");
	}
	/**
	 * Add to the current transaction a query removing all references to loaded file
	 * @param classname : class of product files to be removed
	 * @throws AbortException
	 */
	public static void removeLoadedFiles(String classname) throws AbortException {
		SQLTable.addQueryToTransaction("DELETE FROM saada_loaded_file WHERE classname = '" + classname + "'");
	}
	/**
	 * Append a suffix (.inst#) to the filename in the repository so that products
	 * with the same filename are not overidden
	 * @param prd_name
	 * @return
	 * @throws FatalException 
	 * @throws SaadaException
	 * @throws SQLException
	 */
	private static String getRepositoryname(String prd_name, String coll, String cat) throws Exception  {
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select count(filename) from saada_loaded_file where collection = '" 
				+ coll + "' and category = '" 
				+ cat  + "' and filename = '" 
				+ prd_name + "'");
		try {
			while( rs.next() ) {
				/*
				 * New file: keep the original name in the repository
				 */
				if( rs.getInt(1) == 0 ) {
					squery.close();
					return prd_name;
				}
				/*
				 * Same file name already recorded : add .inst# after the name
				 */
				else {
					squery.close();
					SQLQuery squery2 = new SQLQuery();
					rs = squery2.run("select count(filename) from saada_loaded_file where collection = '" 
							+ coll + "' and category = '" 
							+ cat  + "' and  repositoryname like '" 
							+ "inst%." + prd_name + "'");
					int num =0;
					if( rs.next() ) {
						num = rs.getInt(1);
						squery2.close();
						return "inst" + (num+1) + "." + prd_name;
					}
					squery2.close();
				}
			}
		} catch ( SQLException e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return prd_name;
	}

	/**
	 * @param prd
	 * @return
	 * @throws Exception
	 */
	public static boolean productAlreadyExistsInDB(ProductBuilder prd) throws Exception{
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select count(classname) from saada_loaded_file where md5_config='"
				+ prd.getMapping().getSignature() + "' and content_sign='"
				+ prd.getContentSignature() + "'");
		while( rs.next() ) {
			if( rs.getInt(1) == 0 ) {
				squery.close();
				return false;
			}
			else {
				squery.close();
				return true;
			}
		}
		squery.close();
		return false;   
	}
}
