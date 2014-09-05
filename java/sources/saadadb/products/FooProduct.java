package saadadb.products;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import saadadb.collection.Category;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.enums.DataFileExtensionType;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.validation.JsonKWSet;
import saadadb.products.validation.KeywordsBuilder;
import saadadb.util.Messenger;

public class FooProduct implements DataFile {
	private int pointer = 0;
	private int size = 0;
	private Map<String, AttributeHandler> attributeHandlers = null;
	private Map<String, AttributeHandler> entryAttributeHandlers = null;
	protected Map<String, DataFileExtension> productMap;
	private ProductBuilder productBuilder;
	private List<String> comments = new ArrayList<String>();

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
	public FooProduct(JSONObject jsonObject, int size) throws Exception {
		this(JsonKWSet.getInstance(jsonObject), size);
	}
	
	/**
	 * @param keyWordBuilder
	 * @param size
	 */
	public FooProduct(KeywordsBuilder keyWordBuilder, int size) {
		this.size = size;
		this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		this.entryAttributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		List<AttributeHandler> lst = new ArrayList<AttributeHandler>();
		productMap = new LinkedHashMap<String, DataFileExtension> ();
		for( AttributeHandler ah: keyWordBuilder.headerKWs){
			this.attributeHandlers.put(ah.getNameattr(), ah);
			lst.add(ah);
		}
		productMap.put("#0 Header", new DataFileExtension(0, "1stHDU", DataFileExtensionType.BASIC, lst));

		if( keyWordBuilder.columnKWs != null) { 
			lst = new ArrayList<AttributeHandler>();
			for( AttributeHandler ah: keyWordBuilder.columnKWs){
				this.entryAttributeHandlers.put(ah.getNameattr(), ah);
				lst.add(ah);
			}		
			productMap.put("#0 Data", new DataFileExtension(0, "1stHDU Data", DataFileExtensionType.ASCIITABLE, lst));

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

	public double[] getExtrema(String key) throws Exception {
		if( this.entryAttributeHandlers.get(key) != null ) {
			double v = Double.parseDouble(this.entryAttributeHandlers.get(key).getValue());
			return new double[]{v,v};
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
	public String getName() {
		return "Foo";
	}
	@Override
	public Map<String, AttributeHandler> getEntryAttributeHandler()
			throws SaadaException {
		return this.entryAttributeHandlers;
	}
	@Override
	public Map<String, AttributeHandler> getAttributeHandler()
			throws SaadaException {
		return this.attributeHandlers;
	}


	@Override
	public Map<String, List<AttributeHandler>> getProductMap(int category)
			throws IgnoreException {
		LinkedHashMap<String, List<AttributeHandler>> retour = new LinkedHashMap<String, List<AttributeHandler>>();
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
		this.productBuilder.productAttributeHandler = this.getAttributeHandler();		
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return "no file";
	}

	@Override
	public String getAbsolutePath() {
		return "no file";
	}

	@Override
	public String getParent() {
		return "no file";
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public boolean delete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public QuantityDetector getQuantityDetector(ProductMapping productMapping) throws SaadaException {
		if( productMapping.getCategory() == Category.ENTRY ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Only the " + this.getEntryAttributeHandler().size() + " table columns taken in account");
			return new QuantityDetector(this.getEntryAttributeHandler(), this.comments, productMapping);			
		} else if( this.getEntryAttributeHandler().size() > 0  ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.getEntryAttributeHandler().size() + " table columns taken in account");
			return  new QuantityDetector(this.getAttributeHandler(), this.getEntryAttributeHandler(), this.comments, productMapping, this);
		} else {
			return new QuantityDetector(this.getAttributeHandler(), this.comments, productMapping);
		}		
	}

	@Override
	public List<String> getComments() throws SaadaException {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}
	
	

}
