package saadadb.products.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import saadadb.dataloader.mapping.PriorityMode;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ProductFile;
import saadadb.products.inference.EnergyKWDetector;
import saadadb.products.inference.ObservationKWDetector;
import saadadb.products.inference.SpaceKWDetector;
import saadadb.products.inference.TimeKWDetector;

public class FooProduct implements ProductFile {
	private int pointer = 0;
	private int size = 0;
	private Map<String, AttributeHandler> attributeHandlers = null;
	private Map<String, AttributeHandler> entryAttributeHandlers = null;
	
	/**
	 * Json format
	  {
      header: [[name, unit, ucd, value (optional)],......]
      columns: [[name, unit, ucd, value (optional)],......]
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
		for( AttributeHandler ah: keyWordBuilder.headerKWs){
			this.attributeHandlers.put(ah.getNameattr(), ah);
		}
		if( keyWordBuilder.columnKWs != null) {
			for( AttributeHandler ah: keyWordBuilder.columnKWs){
				this.entryAttributeHandlers.put(ah.getNameattr(), ah);
			}		
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
			retour.add(ah.getValue());
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
	public ObservationKWDetector getObservationKWDetector(boolean entryMode) throws SaadaException{
		if( entryMode ){
			return  new ObservationKWDetector(this.getAttributeHandler(), this.getEntryAttributeHandler());
		} else {
			return new ObservationKWDetector(this.getAttributeHandler());
		}		
	}
	@Override
	public SpaceKWDetector getSpaceKWDetector(boolean entryMode) throws SaadaException{
		if( entryMode ){
			return  new SpaceKWDetector(this.getAttributeHandler(), this.getEntryAttributeHandler());
		} else {
			return new SpaceKWDetector(this.getAttributeHandler());
		}		
	}
	@Override
	public EnergyKWDetector getEnergyKWDetector(boolean entryMode, PriorityMode priority, String defaultUnit) throws SaadaException{
		return new EnergyKWDetector(this, priority, defaultUnit);		
	}
	@Override
	public TimeKWDetector getTimeKWDetector(boolean entryMode) throws SaadaException{
		if( entryMode ){
			return  new TimeKWDetector(this.getAttributeHandler(), this.getEntryAttributeHandler());
		} else {
			return new TimeKWDetector(this.getAttributeHandler());
		}		
	}
	@Override
	public Map<String, ArrayList<AttributeHandler>> getProductMap(int category)
			throws IgnoreException {
		LinkedHashMap<String, ArrayList<AttributeHandler>> retour = new LinkedHashMap<String, ArrayList<AttributeHandler>>();
		retour .put("HEADER", new ArrayList<AttributeHandler>(attributeHandlers.values()));
		retour .put("TABLE", new ArrayList<AttributeHandler>(entryAttributeHandlers.values()));
		return retour;
	}

}
