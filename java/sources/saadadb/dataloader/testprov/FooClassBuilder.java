package saadadb.dataloader.testprov;

import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import saadadb.collection.Category;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.dataloader.SchemaFusionMapper;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.JsonDataFile;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * JSON format
{
"parameters": [
	"-category=misc" ,
	"-collection=XMM",
	"-filename=obscore",
	"-repository=no",
	"-spcmapping=first" ,
	"-spcunit=keV",
	"-timemapping=only" 	,	
	"-tmin=11 03 2013" 	,	
	"-tmax=12 03 2013"	
],
"fields": {
    "header": [	["RA", "double", "", "23.67"],
				["DEC", "double", "", "-56.9"],
				["eMin", "KeV", "em.wl;stat.min", "1."],
				["eMax", "KeV", "em.wl;stat.max", "2."],
				["obsStart", "", "", "2014-02-12"],
				["obsEnd", "", "", "2014-02-13"],
				["collection", "string", "", "3XMM"],
				["target", "string", "", "M33"],
				["instrume", "string", "", "MOS1"],
				["facility", "string", "", "XMM"]
		]
		,
    "columns": []
    }
}
 * @author michel
 * @version $Id$
 */
public class FooClassBuilder {
	private ArgsParser ap ;
	JSONObject[] jsonAhs;
	JsonDataFile[] fooProduct;

	@SuppressWarnings({ "unchecked" })
	public FooClassBuilder(String[] jsonFilenames) throws Exception{
		this.jsonAhs = new JSONObject[jsonFilenames.length];
		this.fooProduct = new JsonDataFile[jsonFilenames.length];
		for( int i=0 ; i<jsonFilenames.length ; i++ ){
			String fn = jsonFilenames[i].replace("json:", "");
			if( fn.charAt(0) != File.separatorChar ){
				fn = Database.getRoot_dir() + File.separator + "datatest" + File.separator + fn;
				if( !(new File(fn)).exists() ) {
					fn = fn.replace(Database.getRoot_dir() + File.separator + "datatest" + File.separator, "/home/michel/workspace/SaadaObscore/datatest/");
				}
			}
			JSONParser parser = new JSONParser();  
			JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(fn));  
			this.jsonAhs[i] = (JSONObject) jsonObject.get("fields");  

			JSONArray parameters = (JSONArray) jsonObject.get("parameters");  
			Iterator<String> iterator = parameters.iterator();  
			List<String> params = new ArrayList<String>();
			while (iterator.hasNext()) {  
				params.add(iterator.next());  
			}  
			if( this.ap == null ){
				this.ap = new ArgsParser(params.toArray(new String[0]));
			}
			//this.fooProduct[i] = new FooProduct(this.jsonAhs[i], 0);
			this.fooProduct[i] = new JsonDataFile(fn, (ProductMapping)null);
		}
	}	

	private void cleanDatabase() throws Exception {
		String collection = ap.getCollection();
		String classe = ap.getClassName();
		Database.setAdminMode("");
		if( !Database.getCachemeta().collectionExists(collection)) {
			CollectionManager cp = new CollectionManager(collection);
			SQLTable.beginTransaction();
			cp.create(ap);
			SQLTable.commitTransaction();
			Database.cachemeta.reload(true);
		} else {
			CollectionManager cp = new CollectionManager(collection);
			SQLTable.beginTransaction();
			cp.empty(ap);
			SQLTable.commitTransaction();
			Database.cachemeta.reload(true);
			
		}

	}
	/**
	 * @throws Exception
	 */
	public void process() throws Exception {
		cleanDatabase(); 
		String className = null;
		for( int i=0 ; i<this.fooProduct.length ; i++ ){
			System.out.println(i + "-------------------------------------------------------------");
			Messenger.debug_mode = false;
			List<String> prd2Load = new ArrayList<String>();
			prd2Load.add(this.fooProduct[i].getAbsolutePath());
			SchemaFusionMapper sfm = new SchemaFusionMapper(null, prd2Load, new ProductMapping("aaa", this.ap ));
			sfm.ingestProductSetByBurst();
			className = sfm.getCurrentClass().getName();
		}
		DatabaseConnection connection =  Database.getConnection();
		ResultSet rs = Database.getWrapper().getTableColumns(connection, className);
		System.out.println("");
		while( rs.next()) {
			System.out.print(" " + rs.getObject("COLUMN_NAME") + " (" + rs.getObject("TYPE_NAME") + ")\t");
		}
		System.out.println("");
		rs.close();
		Database.giveConnection(connection);
		SQLQuery q = new SQLQuery();
		rs = q.run("SELECT * from " +className);
		int nbc = rs.getMetaData().getColumnCount();
		while( rs.next() ){
			for( int i=1 ; i<=nbc ; i++ ){
				System.out.print("|" + rs.getObject(i) + "|\t");
			}
			System.out.println("");
		}
		q.close();
		if( ap.getCategory().equalsIgnoreCase("table") ){
			className += "Entry";
			rs = Database.getWrapper().getTableColumns(connection, className);
			System.out.println("");
			while( rs.next()) {
				System.out.print(" " + rs.getObject("COLUMN_NAME") + " (" + rs.getObject("TYPE_NAME") + ")\t");
			}
			System.out.println("");
			rs.close();
			Database.giveConnection(connection);
			q = new SQLQuery();
			rs = q.run("SELECT * from " +className);
			nbc = rs.getMetaData().getColumnCount();
			while( rs.next() ){
				for( int i=1 ; i<=nbc ; i++ ){
					System.out.print("|" + rs.getObject(i) + "|\t");
				}
				System.out.println("");
			}
			q.close();
			
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ArgsParser ap = new ArgsParser(args);			
			Database.init(ap.getDBName());
			FooClassBuilder fr;
			String[] fn = ap.getFilename().split("[,;\\s]");
			fr = new FooClassBuilder(fn);
			fr.process();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.exit();
	}
}
