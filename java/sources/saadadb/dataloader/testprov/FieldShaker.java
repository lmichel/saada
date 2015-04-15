package saadadb.dataloader.testprov;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.JsonDataFile;
import saadadb.products.reporting.MappingReport;
import saadadb.products.reporting.TableMappingReport;
import saadadb.products.setter.ColumnSetter;
import saadadb.util.Messenger;

/**
 * class  testing different formats of fields
 * Should be parametred in auto dectection
 * The input JSON files gives a set of KWs set with different way to encode values
 {
			"parameters": [ 
				"-category=misc" , 
				"-collection=XMM", 
				"-repository=no"
			], 
            "fields": ["s_ra",  "s_dec"],
			"cases": [ {"name" : name,
			            "header": [ "
			                       ["RA"        , "double", "deg"   , ""              , "10"], "
							       ["DEC"       , "double", "deg"   , ""              , "+20"] "
					               ],
				         "columns": []               
					    },
                      {"name" : name,
			            "header": [ "
			                       ["RA"        , "double", "deg"   , ""              , "10"], "
							       ["DEC"       , "double", "deg"   , ""              , "+20"] "
					               ],					               
				         "columns": []               
					    }
 					]
			    }
			}
 * 
 * @author michel
 * @version $Id$
 */
public class FieldShaker {
	/** Saada data product built with the JSON object */
	protected  Map<String, JsonDataFile>  fooProducts;
	/** Current args parser read into the JSON modified during the test */
	protected ArgsParser argsParser;
	/** Test report, one entry er test, and several  lines par entry*/
	protected Map<String, List<String>> reports;
	/** pointer on the retpor current of the current test*/ 
	protected List<String> currentReport;
	protected JsonDataFile currentProduct;
	/** List of Obscore fields of interest: they are reported */
	protected Set<String> paramsOfInterest;

	/**
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
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
						Database.exit();
					}
					System.out.println(ffn);
				}
			}
		} else {
			ffn = jsonFilename;
		}
		JSONParser parser = new JSONParser();  
		JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(ffn)); 
		/*
		 * 
		 */
		JSONArray jfields = (JSONArray) jsonObject.get("fields");  
		this.paramsOfInterest = new TreeSet<String>();
		Iterator<String> iterator = jfields.iterator();  
		while (iterator.hasNext()) {  
			this.paramsOfInterest.add(iterator.next());  
		}  

		/*
		 * Extract data loader params
		 */
		JSONArray parameters = (JSONArray) jsonObject.get("parameters");  
		iterator = parameters.iterator();  
		List<String> params = new ArrayList<String>();
		while (iterator.hasNext()) {  
			params.add(iterator.next());  
		}  
		this.argsParser = new ArgsParser(params.toArray(new String[0]));
		/*
		 * Extraction des differents cas
		 */
		this.fooProducts = new LinkedHashMap<String, JsonDataFile>();
		this.reports = new LinkedHashMap<String, List<String>>();
		JSONArray cases = (JSONArray) jsonObject.get("cases");  
		Iterator casesIt = cases.iterator();  
		try {
			while (casesIt.hasNext()) { 
				JSONObject cas = (JSONObject) casesIt.next();
				String casName = (String) cas.get("name");
				this.fooProducts.put(casName, new JsonDataFile(cas, 0, new ProductMapping("Field Mapping", this.argsParser)));
				this.reports.put(casName, new ArrayList<String>());
			}
		} catch (ParseException e) {
			e.printStackTrace();
			Database.exit();
		}  
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
		MappingReport tmr = new MappingReport(product);
		Map<String, ColumnSetter> r = tmr.getReport();
		//Map<String, ColumnSetter> er = product.getEntryReport();
		this.currentReport.add(this.argsParser.toString());
		for( java.util.Map.Entry<String, ColumnSetter> e:r.entrySet()){
			if( this.paramsOfInterest.contains(e.getKey())) {
				String str = "";
				str = String.format("%20s",e.getKey()) + "     ";
				ColumnSetter ah = e.getValue();
				str += ah.getSettingMode() + " " + ah.getFullMappingReport();
				if( !ah.isNotSet() ) 
					str += " storedValue=" + ah.storedValue + ((ah.getUnit() != null)? ah.getUnit(): "");
				this.currentReport.add(str);
			}
		}
	}

	/**
	 * Process all test
	 * @throws Exception
	 */
	protected void processAll() throws Exception {
		for( String key: this.fooProducts.keySet() ) {
			Messenger.printMsg(Messenger.TRACE, "************ Processing " + key);
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
	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) {
		try {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		FieldShaker ps = 	(new FieldShaker(ap.getFilename()));
		ps.processAll();
		ps.showReport();
		} catch(Exception e){
			e.printStackTrace();
		}
		Database.exit();
	}

}
