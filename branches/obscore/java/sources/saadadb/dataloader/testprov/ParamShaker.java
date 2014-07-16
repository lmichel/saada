package saadadb.dataloader.testprov;

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
import saadadb.products.ColumnSetter;
import saadadb.products.FooProduct;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.util.Messenger;

/**
 * Superclass of test for the fields attached to one axis.
 * Take a JSON object as input, and modify it as long as the priority during test
 * There 12 tests: 
 * 	2 priority level LAST/FIRST 
 * 		good mapped parameters, good inferred parameters 
 * 		wrong mapped parameters, wrong inferred parameters 
 * 		partially wrong mapped parameters, partially wrong inferred parameters 
 * @author michel
 * @version $Id$
 */
public abstract class ParamShaker {
	/** JSON template: set in sublcasses*/
	protected static String TEMPLATE ;
	/** JSON instance of the product in the current state */
	protected static JSONObject jsonObject;
	/** Saada data product built with the JSON object */
	protected FooProduct fooProduct;
	/** Current args parser read into the JSON modified during the test */
	protected ArgsParser argsParser;
	/** List of Obscore fields of interest: theu are reported */
	protected Set<String> paramsOfInterest;
	/** Test report, one entry er test, and several  lines par entry*/
	protected Map<String, List<String>> report;
	/** pointer on the retpor current of the current test*/ 
	protected List<String> currentReport;

	/**
	 * @throws Exception
	 */
	ParamShaker() throws Exception{
		JSONParser parser = new JSONParser();  
		try {
			this.report = new LinkedHashMap<String, List<String>>();
			jsonObject = (JSONObject)parser.parse(TEMPLATE);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}  
	}

	/**
	 * Set all priorities to FIRST ion the arg parser
	 * @throws Exception
	 */
	protected void setFirst() throws Exception{
		JSONArray jsa = (JSONArray) jsonObject.get("parameters");
		List<String> params = new ArrayList<String>();
		for( int i=0 ; i<jsa.size() ; i++ ){
			String s = (String) jsa.get(i);
			s = s.replace("mapping=last", "mapping=first");
			params.add(s);
		}		
		this.argsParser = new ArgsParser(params.toArray(new String[0]));
	}

	/**
	 * Set all priorities to FISR ion the arg parser
	 * @throws Exception
	 */
	protected void setLast() throws Exception{
		JSONArray jsa = (JSONArray) jsonObject.get("parameters");
		List<String> params = new ArrayList<String>();
		for( int i=0 ; i<jsa.size() ; i++ ){
			String s = (String) jsa.get(i);
			s = s.replace("mapping=first", "mapping=last");
			params.add(s);
		}		
		this.argsParser = new ArgsParser(params.toArray(new String[0]));
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
		this.report.put("FirstWithGoodMParams", this.currentReport);
	}
	/**
	 * Priority first, good inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithGoodIParams() throws Exception{
		readJson();
		setFirst();	
		this.currentReport = new ArrayList<String>();
		this.report.put("FirstWithGoodIParams", this.currentReport);
	}
	/**
	 * Priority first, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongMParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.report.put("FirstWithWrongMParams", this.currentReport);
	}
	/**
	 * Priority first, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.report.put("FirstWithWrongIParams", this.currentReport);
	}
	/**
	 * Priority first, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.report.put("FirstWithPWrongMParams", this.currentReport);
	}
	/**
	 * Priority first, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		readJson();
		setFirst();
		this.currentReport = new ArrayList<String>();
		this.report.put("FirstWithPWrongIParams", this.currentReport);
	}
	/**
	 * Priority Last, good mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodMParams() throws Exception{
		readJson();
		setLast();		
		this.currentReport = new ArrayList<String>();
		this.report.put("LastWithGoodMParams", this.currentReport);
	}
	/**
	 * Priority Last, good inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodIParams() throws Exception{
		readJson();
		setLast();	
		this.currentReport = new ArrayList<String>();
		this.report.put("LastWithGoodIParams", this.currentReport);
	}
	/**
	 * Priority Last, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongMParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.report.put("LastWithWrongMParams", this.currentReport);
	}
	/**
	 * Priority Last, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongIParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.report.put("LastWithWrongIParams", this.currentReport);
	}
	/**
	 * Priority Last, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.report.put("LastWithPWrongMParams", this.currentReport);
	}
	/**
	 * Priority Last, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		readJson();
		setLast();
		this.currentReport = new ArrayList<String>();
		this.report.put("LastWithPWrongIParams", this.currentReport);
	}

	/**
	 * run the mapping on the current product and stores the result within the report
	 * @throws Exception
	 */
	protected void process() throws Exception {
		ProductBuilder product = null;
		switch( Category.getCategory(argsParser.getCategory()) ) {
		case Category.TABLE: product = new TableBuilder(this.fooProduct, new ProductMapping("mapping", this.argsParser));
		break;
		case Category.MISC : product = new MiscBuilder(this.fooProduct, new ProductMapping("mapping", this.argsParser));
		break;
		case Category.SPECTRUM: product = new SpectrumBuilder(this.fooProduct, new ProductMapping("mapping", this.argsParser));
		break;
		case Category.IMAGE: product = new Image2DBuilder(this.fooProduct, new ProductMapping("mapping", this.argsParser));
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
				str += ah.getMode() + " " + ah.message;
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
		runFirstWithGoodMParams();
		runFirstWithGoodIParams();
		runFirstWithWrongMParams();
		runFirstWithWrongIParams();
		runFirstWithPWrongMParams();
		runFirstWithPWrongIParams();
		runLastWithGoodMParams();
//		runLastWithGoodIParams();
//		runLastWithWrongMParams();
//		runLastWithWrongIParams();
//		runLastWithPWrongMParams();
//		runLastWithPWrongIParams();
	}

	/**
	 * Print out the test report
	 */
	protected void showReport(){
		for( Entry<String, List<String>>entry: this.report.entrySet()){
			System.out.println(entry.getKey());
			for( String s: entry.getValue()){
				System.out.println("  " + s);
			}
		}
	}
}
