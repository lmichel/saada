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
import saadadb.util.SaadaConstant;
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
	public List<Object[]> tableData;

	/**
	 * Json format
"data": {
     "header": [["RA", "double", "", "", "23.67"],
				.......
		]
		,
    "table": {header: [["RA2000", "double", "", "", "10."],.......
                      ]
                      ,
             data: [[ "10.", "45", "lui"],
                    ......]
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
		this.init(JsonKWSet.getInstance((JSONObject) jsonObject.get("data")), size);
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
		this.tableData = keyWordBuilder.tableData;
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
	public void closeStream() {}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements() {
		return (this.tableData != null && this.pointer < this.tableData.size());
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public Object nextElement() throws NumberFormatException, NullPointerException{
		if( this.entryAttributeHandlers == null || this.tableData == null || this.pointer >= this.tableData.size()) {
			Messenger.printMsg(Messenger.TRACE, "Attempt to read a data row out of range (pointer=" + pointer + " data size=" + this.tableData.size() + ")");
			return null;
		}
		Object retour = this.tableData.get(pointer);
		this.pointer ++;
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWValueQuickly(java.lang.String)
	 */
	@Override
	public String getKWValueQuickly(String key) {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getExtrema(java.lang.String)
	 */
	@Override
	public Object[] getExtrema(String key) throws Exception {
		if( this.entryAttributeHandlers.get(key) != null ) {
			Object v = this.entryAttributeHandlers.get(key).getValue();
			return new Object[]{v,v};
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getNRows()
	 */
	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getNRows()
	 */
	@Override
	public int getNRows() throws IgnoreException {
		return (this.tableData != null)?this.tableData.size(): SaadaConstant.INT;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getNCols()
	 */
	@Override
	public int getNCols() throws IgnoreException {
		return entryAttributeHandlers.size();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#initEnumeration()
	 */
	@Override
	public void initEnumeration() throws IgnoreException {
		this.pointer = 0;
	}

	/**
	 * @param category
	 * @return
	 * @throws IgnoreException
	 */
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
