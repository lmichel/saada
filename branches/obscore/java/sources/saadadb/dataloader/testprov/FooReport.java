package saadadb.dataloader.testprov;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.validation.FooProduct;
import saadadb.products.validation.ObscoreKWSet;

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
public class FooReport {
	private ArgsParser ap ;
	JSONObject jsonAhs;
	FooProduct fooProduct;

	@SuppressWarnings({ "unchecked", "unused" })
	FooReport(String jsonFilename) throws Exception{
		String fn = jsonFilename.replace("json:", "");
		if( fn.charAt(0) != File.separatorChar ){
			fn = Database.getRoot_dir() + File.separator + "datatest" + File.separator + fn;
		}
		JSONParser parser = new JSONParser();  
		JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(fn));  
		this.jsonAhs = (JSONObject) jsonObject.get("fields");  
		JSONArray parameters = (JSONArray) jsonObject.get("parameters");  
		Iterator<String> iterator = parameters.iterator();  
		List<String> params = new ArrayList<String>();
		while (iterator.hasNext()) {  
			params.add(iterator.next());  
		}  
		this.ap = new ArgsParser(params.toArray(new String[0]));
		this.fooProduct = new FooProduct(this.jsonAhs, 0);

	}	
	FooReport(String[] args) throws Exception{
		this.ap = new ArgsParser(args);
		String fn = this.ap.getFilename();
		if( fn.equalsIgnoreCase("obscore")){
			this.fooProduct = new FooProduct(ObscoreKWSet.getInstance(null), 0);
		} else {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Unknown foo type");
		}
	}

	/**
	 * @throws Exception
	 */
	private void process() throws Exception {
		ProductBuilder product = null;
		switch( Category.getCategory(ap.getCategory()) ) {
		case Category.TABLE: product = new TableBuilder(this.fooProduct, new ProductMapping("mapping", this.ap));
		break;
		case Category.MISC : product = new MiscBuilder(this.fooProduct, new ProductMapping("mapping", this.ap));
		break;
		case Category.SPECTRUM: product = new SpectrumBuilder(this.fooProduct, new ProductMapping("mapping", this.ap));
		break;
		case Category.IMAGE: product = new Image2DBuilder(this.fooProduct, new ProductMapping("mapping", this.ap));
		break;
		}
		product.initProductFile();
		product.printReport();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ArgsParser ap = new ArgsParser(args);			
			Database.init(ap.getDBName());
			FooReport fr;
			String fn = ap.getFilename();
			System.out.println(fn);
			if( fn.startsWith("json:") ){
				fr = new FooReport(new ArgsParser(args).getFilename());
			} else {
				fr = new FooReport(args);
			}
			fr.process();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(1);
		}
	}
}
