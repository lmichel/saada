package saadadb.dataloader.testprov;

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
import saadadb.exceptions.FatalException;
import saadadb.products.ColumnSetter;
import saadadb.products.ExtensionSetter;
import saadadb.products.FooProduct;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.util.Messenger;

public abstract class ParamShaker {
	protected static String TEMPLATE ;
	protected static JSONObject jsonObject;
	protected FooProduct fooProduct;
	protected ArgsParser argsParser;
	protected Set<String> paramsOfInterest;
	protected Map<String, List<String>> report;
	protected List<String> currentReport;

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

	protected void setFirst() throws Exception{
		JSONArray jsa = (JSONArray) jsonObject.get("parameters");
		for( int i=0 ; i<jsa.size() ; i++ ){
			String s = (String) jsa.get(i);
			s = s.replace("mapping=last", "mapping=first");
		}		
	}

	protected void setLast() throws Exception{
		JSONArray jsa = (JSONArray) jsonObject.get("parameters");
		for( int i=0 ; i<jsa.size() ; i++ ){
			String s = (String) jsa.get(i);
			s = s.replace("mapping=first", "mapping=last");
		}		
	}

	@SuppressWarnings("unchecked")
	protected void setFields(String name, String type, String unit, String ucd, String value) throws Exception{
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
				return;
			}
		}
		throw new Exception("Param " + name + " not found" );
	}

	@SuppressWarnings("unchecked")
	protected void initProduct() throws Exception {
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
		setFirst();	
		this.currentReport = new ArrayList<String>();
		this.report.put("FirstWithGoodMParams", this.currentReport);
	}
	/**
	 * Priority first, good inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithGoodIParams() throws Exception{
		setFirst();	
		this.report.put("FirstWithGoodIParams", this.currentReport);
	}
	/**
	 * Priority first, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongMParams() throws Exception{
		setFirst();
		this.report.put("FirstWithWrongMParams", this.currentReport);
	}
	/**
	 * Priority first, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithWrongIParams() throws Exception{
		setFirst();
		this.report.put("FirstWithWrongIParams", this.currentReport);
	}
	/**
	 * Priority first, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongMParams() throws Exception{
		setFirst();
		this.report.put("FirstWithPWrongMParams", this.currentReport);
	}
	/**
	 * Priority first, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runFirstWithPWrongIParams() throws Exception{
		setFirst();
		this.report.put("FirstWithPWrongIParams", this.currentReport);
	}
	/**
	 * Priority Last, good mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodMParams() throws Exception{
		setFirst();		
		this.report.put("LastWithGoodMParams", this.currentReport);
	}
	/**
	 * Priority Last, good inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithGoodIParams() throws Exception{
		setFirst();	
		this.report.put("LastWithGoodIParams", this.currentReport);
	}
	/**
	 * Priority Last, wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongMParams() throws Exception{
		setFirst();
		this.report.put("LastWithWrongMParams", this.currentReport);
	}
	/**
	 * Priority Last, wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithWrongIParams() throws Exception{
		setFirst();
		this.report.put("LastWithWrongIParams", this.currentReport);
	}
	/**
	 * Priority Last, partially wrong mapped parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongMParams() throws Exception{
		setFirst();
		this.report.put("LastWithPWrongMParams", this.currentReport);
	}
	/**
	 * Priority Last, partially wrong inferred parameters
	 * @throws Exception 
	 */
	protected void runLastWithPWrongIParams() throws Exception{
		setFirst();
	}

	protected void process() throws Exception {
		this.initProduct();
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
//		System.out.println(this.argsParser);
//
//		System.out.println("======== ");	
//		System.out.println("      -- Loaded extensions");	
//		for( ExtensionSetter es: product.getReportOnLoadedExtension()) {
//			System.out.println(es);
//		}
//		System.out.println("      -- Field values");	
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
	 * @throws Exception
	 */
	protected void processAll() throws Exception {
		Messenger.debug_mode = true;
		runFirstWithGoodMParams();
//		runFirstWithGoodIParams();
//		runFirstWithWrongMParams();
//		runFirstWithWrongIParams();
//		runFirstWithPWrongMParams();
//		runFirstWithPWrongIParams();
//		runLastWithGoodMParams();
//		runLastWithGoodIParams();
//		runLastWithWrongMParams();
//		runLastWithWrongIParams();
//		runLastWithPWrongMParams();
//		runLastWithPWrongIParams();
	}

	protected void showReport(){
		for( Entry<String, List<String>>entry: this.report.entrySet()){
			System.out.println(entry.getKey());
			for( String s: entry.getValue()){
				System.out.println("  " + s);
			}
		}

	}

}
