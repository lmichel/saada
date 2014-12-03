package saadadb.products.datafile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import saadadb.exceptions.IgnoreException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.ProductBuilder;
import saadadb.products.validation.JsonKWSet;
import saadadb.products.validation.KeywordsBuilder;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.DataFileExtensionType;

/**
 * Can be set either with a JSON file or a JSON object
 * @author michel
 * @version $Id$
 */
public class JsonDataFile extends DataFile {
	private int pointer = 0;
	private int size = 0;
	private File file;

	/**
	 * Json format
	  {
      header: [[name, type, unit, ucd, value (optional)],......]
      columns: [[name, type, unit, ucd, value (optional)],......]
      }

	 * @param jsonObject
	 * @param size
	 * @throws Exception
	 */
	public JsonDataFile(String jsonFileName) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Building DataFile from " + jsonFileName);
		JSONParser parser = new JSONParser();  
		this.file = new File(jsonFileName);
		JSONObject jsonObject = (JSONObject)parser.parse(new FileReader(this.file));  
		this.init(JsonKWSet.getInstance((JSONObject) jsonObject.get("fields")), size);
	}
	
	/**
	 * @param jsonObject
	 * @param size
	 * @throws Exception
	 */
	public JsonDataFile(JSONObject jsonObject, int size) throws Exception {
		this(JsonKWSet.getInstance(jsonObject), size);
	}
	
	/**
	 * @param keyWordBuilder
	 * @param size
	 */
	public JsonDataFile(KeywordsBuilder keyWordBuilder, int size) {
		this.init(keyWordBuilder, size);
	}
	
	/**
	 * @param keyWordBuilder
	 * @param size
	 */
	protected void init(KeywordsBuilder keyWordBuilder, int size){
		this.size = size;
		this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		this.entryAttributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		List<AttributeHandler> lst = new ArrayList<AttributeHandler>();
		this.productMap = new LinkedHashMap<String, DataFileExtension> ();
		for( AttributeHandler ah: keyWordBuilder.headerKWs){
			this.attributeHandlers.put(ah.getNameattr(), ah);
			lst.add(ah);
		}
		this.productMap.put("#0 Header", new DataFileExtension(0, "1stHDU", DataFileExtensionType.BASIC, lst));

		if( keyWordBuilder.columnKWs != null) { 
			lst = new ArrayList<AttributeHandler>();
			for( AttributeHandler ah: keyWordBuilder.columnKWs){
				this.entryAttributeHandlers.put(ah.getNameattr(), ah);
				lst.add(ah);
			}		
			this.productMap.put("#0 Data", new DataFileExtension(0, "1stHDU Data", DataFileExtensionType.ASCIITABLE, lst));
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#closeStream()
	 */
	public void closeStream() {
	}
	
	public boolean hasMoreElements() {
		return (pointer < size);
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement() throws NumberFormatException, NullPointerException{
		if( this.entryAttributeHandlers == null ) return null;
		List<String> retour = new ArrayList<String>();
		for( AttributeHandler ah: this.entryAttributeHandlers.values()){
			String v = ah.getValue();
			if( ah.getType().equals("String") ) v = v+pointer;
			else if( ah.getType().equals("double") ) v = Double.toString(Double.parseDouble(v) + pointer);
			retour.add(v);
		}
		pointer ++;
		return retour.toArray(new String[0]);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWValueQuickly(java.lang.String)
	 */
	public String getKWValueQuickly(String key) {
		return null;
	}

	public Object[] getExtrema(String key) throws Exception {
		if( this.entryAttributeHandlers.get(key) != null ) {
			Object v = this.entryAttributeHandlers.get(key).getValue();
			return new Object[]{v,v};
		}
		return null;
	}

	public int getNRows() throws IgnoreException {
		return size;
	}

	public int getNCols() throws IgnoreException {
		return entryAttributeHandlers.size();
	}

	public void initEnumeration() throws IgnoreException {
		this.pointer = 0;
	}

	public LinkedHashMap<String, ArrayList<AttributeHandler>> getProductMap(
			String category) throws IgnoreException {
		LinkedHashMap<String, ArrayList<AttributeHandler>> retour = new LinkedHashMap<String, ArrayList<AttributeHandler>>();
		retour .put("HEADER", new ArrayList<AttributeHandler>(attributeHandlers.values()));
		retour .put("TABLE", new ArrayList<AttributeHandler>(entryAttributeHandlers.values()));
		return retour;
	}



	@Override
	public Map<String, DataFileExtension> getProductMap() throws Exception {
		return this.productMap;
	}

	@Override
	public List<ExtensionSetter> reportOnLoadedExtension() {
		List<ExtensionSetter> retour = new ArrayList<ExtensionSetter>();
		retour.add(new ExtensionSetter());
		return retour;
	}

	@Override
	public void bindBuilder(ProductBuilder builder) throws Exception {
		this.productBuilder = builder;
		this.productBuilder.productAttributeHandler = this.getAttributeHandlerCopy();		
	}
	@Override
	public void bindEntryBuilder(ProductBuilder builder) throws Exception {
		//this.productBuilder = builder;
		builder.productAttributeHandler = this.getEntryAttributeHandlerCopy();		
	}
	
	@Override
	public String getName() {
		return (this.file != null)? this.file.getName():  "no file";
	}


	@Override
	public String getCanonicalPath() throws IOException {
		return (this.file != null)? this.file.getCanonicalPath():  "no file";
	}

	@Override
	public String getAbsolutePath() {
		return (this.file != null)? this.file.getAbsolutePath():  "no file";
	}
	
	@Override
	public String getParent() {
		return (this.file != null)? this.file.getParent():  "no file";
	}

	@Override
	public long length() {
		return (this.file != null)? this.file.length(): -1;
	}

	@Override
	public boolean delete() {
		return (this.file != null)? this.file.delete(): false;
	}

	@Override
	public void mapAttributeHandler() throws IgnoreException {}

	@Override
	public void mapEntryAttributeHandler() throws IgnoreException {}

}
