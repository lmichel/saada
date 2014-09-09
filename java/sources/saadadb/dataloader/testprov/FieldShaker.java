package saadadb.dataloader.testprov;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.products.FooProduct;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.setter.ColumnSetter;
import saadadb.products.setter.ColumnSingleSetter;
import saadadb.util.Messenger;

/**
 * Superclass of test for the fields attached to one axis.
 * Take a JSON object as input, and modify it as long as the priority during test
 * There 12 tests: 
 * 	2 priority level LAST/FIRST 
 * 		good mapped parameters, good inferred parameters 
 * 		wrong mapped parameters, wrong inferred parameters 
 * 		partially wrong mapped parameters, partially wrong inferred parameters 
 * 
  		TEMPLATE = "{"

			"parameters": [ 
				"-category=misc" , 
				"-collection=XMM", 
				"-filename=obscore", 
				"-repository=no", 
				"-posmapping=first" , 
				"-position=alpha,delta"  "
				"-system='FK5'"  "
			], "
			"cases": [ {"name" : name,
			            "fields": [ "
			                       ["RA"        , "double", "deg"   , ""              , "10"], "
							       ["DEC"       , "double", "deg"   , ""              , "+20"] "
			             ["alpha"       , "double", "deg"   , ""              , "-10"], "
							["delta"       , "double", "deg"   , ""              , "-20"] "
					]"
					,"
			    "columns": []"
			    }"
			}";

 * 
 * @author michel
 * @version $Id$
 */
public abstract class FieldShaker {
	/** JSON template: set in sublcasses*/
	protected static Map<String, String> TEMPLATE ;
	/** Saada data product built with the JSON object */
	protected static Map<String, FooProduct>  fooProducts;
	/** Current args parser read into the JSON modified during the test */
	protected ArgsParser argsParser;
	/** Test report, one entry er test, and several  lines par entry*/
	protected Map<String, List<String>> reports;
	/** pointer on the retpor current of the current test*/ 
	protected List<String> currentReport;
	protected FooProduct currentProduct;
	/** List of Obscore fields of interest: theu are reported */
	protected Set<String> paramsOfInterest;

	/**
	 * @throws Exception
	 */
	FieldShaker(String jsonFilename) throws Exception{
		String fn = jsonFilename.replace("json:", "");
		String ffn = "";
		if( fn.charAt(0) != File.separatorChar ){
			ffn = Database.getRoot_dir() + File.separator + "datatest" + File.separator + fn;
			if( !(new File(ffn)).exists() ) {
				System.out.println(ffn + " does not exist 1");
				ffn = ffn.replace(Database.getRoot_dir() + File.separator + "datatest" + File.separator, "/home/michel/workspace/SaadaObscore/datatest/");
				if( !(new File(ffn)).exists() ) {
					System.out.println(ffn + " does not exist 2");
					ffn = "/Users/laurentmichel/Documents/workspace/SaadaObscore/datatest/" + fn;
					if( !(new File(ffn)).exists() ) {
						System.out.println(ffn + " does not exist 3");
						System.exit(1);
					}
					System.out.println(ffn);
				}
			}
		} else {
			ffn = jsonFilename;
		}
		JSONParser parser = new JSONParser();  
		JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(ffn));  
		this.jsonAhs = (JSONObject) jsonObject.get("fields");  
		/*
		 * Extract data loader params
		 */
		JSONArray parameters = (JSONArray) jsonObject.get("parameters");  
		Iterator<String> iterator = parameters.iterator();  
		List<String> params = new ArrayList<String>();
		while (iterator.hasNext()) {  
			params.add(iterator.next());  
		}  
		this.argsParser = new ArgsParser(params.toArray(new String[0]));

		JSONArray cases = (JSONArray) jsonObject.get("cases");  

		try {
			this.reports = new LinkedHashMap<String, List<String>>();
			for( Entry<String, String> e: TEMPLATE.entrySet()) {
				JSONObject jsonObject = (JSONObject) parser.parse(e.getValue());
				this.fooProducts.put(e.getKey()
						, new FooProduct((JSONObject) ((JSONObject)(parser.parse(e.getValue())).get("fields"), 0));
				this.reports.put(e.getKey(), new ArrayList<String>());
			}
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}  
	}

	/**
	 * @param param starts  with '-'
	 * @param value
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void setArgParam(String param, String value) throws Exception{
		JSONArray jsa = (JSONArray) jsonObject.get("parameters");
		List<String> params = new ArrayList<String>();
		for( int i=0 ; i<jsa.size() ; i++ ){
			String s = (String) jsa.get(i);
			if( s.startsWith(param) ) {
				s = param + "=" + value;
				jsa.set(i, s);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Set  " +s);
				break;
			}
		}	
		for( int i=0 ; i<jsa.size() ; i++ ){
			params.add((String) jsa.get(i));
		}	
		this.argsParser = new ArgsParser(params.toArray(new String[0]));
		
	}

	/**
	 * Change the parameters for the field "name"
	 * only non nul parameters are set
	 * @param name
	 * @param type
	 * @param unit
	 * @param ucd
	 * @param value
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void setField(String name, String type, String unit, String ucd, String value) throws Exception{
		JSONObject jso = (JSONObject) jsonObject.get("fields");
		JSONArray jsa = (JSONArray) jso.get("header");
		for( int i=0 ; i<jsa.size() ; i++ ){
			JSONArray jsah = (JSONArray) jsa.get(i);
			if( ((String)(jsah.get(0))).equals(name) ) {
				if( type != null) jsah.set(1, type);
				if( unit != null) jsah.set(2, unit);
				if( ucd != null) jsah.set(3, ucd);
				if( value != null) jsah.set(4, value);
				Messenger.printMsg(Messenger.TRACE, "Attribute " + name + " changed");
				break;
			}
		}
		this.fooProduct = new FooProduct((JSONObject) jsonObject.get("fields"), 0);
		//throw new Exception("Param " + name + " not found" );
	}

	/**
	 * Build the productBuilder from the JSON object
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void readJson() throws Exception {
		JSONParser parser = new JSONParser();  
		jsonObject = (JSONObject)parser.parse(TEMPLATE);
		JSONArray parameters = (JSONArray) jsonObject.get("parameters");  
		Iterator<String> iterator = parameters.iterator();  
		List<String> params = new ArrayList<String>();
		while (iterator.hasNext()) {  
			params.add(iterator.next());  
		}  
		params.add(Database.getDbname());  
		this.argsParser = new ArgsParser(params.toArray(new String[0]));
		this.fooProduct = new FooProduct((JSONObject) jsonObject.get("fields"), 0);
	}

	/**
	 * Priority first, good mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithGoodMParams() throws Exception{
		readJson();
		setFirst();	
		this.currentReport = new ArrayList<String>();
		this.reports.put("FirstWithGoodMParams", this.currentReport);
	}
	/**
	 * Priority first, good inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithGoodIParams() throws Exception{
		readJson();
		setFirst();	
		this.currentReport = new ArrayList<String>();
		this.reports.put("FirstWithGoodIParams", this.currentReport);
	}
	/**
	 * Priority first, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongMParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.reports.put("FirstWithWrongMParams", this.currentReport);
	}
	/**
	 * Priority first, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.reports.put("FirstWithWrongIParams", this.currentReport);
	}
	/**
	 * Priority first, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.reports.put("FirstWithPWrongMParams", this.currentReport);
	}
	/**
	 * Priority first, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.reports.put("FirstWithPWrongIParams", this.currentReport);
	}
	/**
	 * Priority Last, good mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodMParams() throws Exception{
		readJson();
		setLast();		
		this.currentReport = new ArrayList<String>();
		this.reports.put("LastWithGoodMParams", this.currentReport);
	}
	/**
	 * Priority Last, good inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodIParams() throws Exception{
		readJson();
		setLast();	
		this.currentReport = new ArrayList<String>();
		this.reports.put("LastWithGoodIParams", this.currentReport);
	}
	/**
	 * Priority Last, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongMParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.reports.put("LastWithWrongMParams", this.currentReport);
	}
	/**
	 * Priority Last, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongIParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.reports.put("LastWithWrongIParams", this.currentReport);
	}
	/**
	 * Priority Last, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.reports.put("LastWithPWrongMParams", this.currentReport);
	}
	/**
	 * Priority Last, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.reports.put("LastWithPWrongIParams", this.currentReport);
	}

	/**
	 * run the mapping on the current product and stores the result within the report
	 * @throws Exception
	 */
	protected void process(String key) throws Exception {
		ProductBuilder product = null;
		this.currentProduct = this.fooProducts.get(key);
		this.currentReport = this.reports.get(key);
		switch( Category.getCategory(argsParser.getCategory()) ) {
		case Category.TABLE: product = new TableBuilder(this.currentProduct, new ProductMapping("mapping", this.argsParser));
		break;
		case Category.MISC : product = new MiscBuilder(this.currentProduct, new ProductMapping("mapping", this.argsParser));
		break;
		case Category.SPECTRUM: product = new SpectrumBuilder(this.currentProduct, new ProductMapping("mapping", this.argsParser));
		break;
		case Category.IMAGE: product = new Image2DBuilder(this.currentProduct, new ProductMapping("mapping", this.argsParser));
		break;
		}
		Map<String, ColumnSetter> r = product.getReport();
		Map<String, ColumnSetter> er = product.getEntryReport();
		this.currentReport.add(this.argsParser.toString());
		for( java.util.Map.Entry<String, ColumnSetter> e:r.entrySet()){
			if( this.paramsOfInterest.contains(e.getKey())) {
				String str = "";
				str = String.format("%20s",e.getKey()) + "     ";
				ColumnSetter ah = e.getValue();
				str += ah.getSettingMode() + " " + ah.message;
				if( !ah.notSet() ) 
					str += " storedValue=" + ah.storedValue;
				this.currentReport.add(str);
			}
		}
	}

	/**
	 * Process all test
	 * @throws Exception
	 */
	protected void processAll() throws Exception {
		Messenger.debug_mode = true;
		for( String key: this.fooProducts.keySet() ) {
			this.process(key);
		}
	}

	/**
	 * Print out the test report
	 */
	protected void showReport(){
		for( Entry<String, List<String>>entry: this.reports.entrySet()){
			System.out.println(entry.getKey());
			for( String s: entry.getValue()){
				System.out.println("  " + s);
			}
		}
	}
}
