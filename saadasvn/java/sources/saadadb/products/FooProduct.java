package saadadb.products;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.meta.AttributeHandler;

public class FooProduct implements ProductFile {
	private int pointer = 0;
	private int size = 0;
	private int gridWidth = 0;
	private int gridStep = 0;

	private LinkedHashMap<String, AttributeHandler> kws;
	private LinkedHashMap<String, AttributeHandler> ekws;
	private double ra0=0, dec0=-10.;
	private double raMax, ra, dec;
	public static final double STEP=0.01;
	private SpaceFrame frame;
	
	FooProduct(int size){
		this.size = size;
		this.gridWidth = (int) Math.sqrt(size);
		if(this.gridWidth == 0)  this.gridWidth = 1;
		this.kws = new LinkedHashMap<String, AttributeHandler>();
		this.ekws = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr("RA"); ah.setNameattr("_ra"); ah.setType("double");
		kws.put(ah.getNameattr(), ah); ekws.put(ah.getNameattr(), ah);
		
		ah.setNameattr("DEC"); ah.setNameattr("_dec"); ah.setType("double");
		kws.put(ah.getNameattr(), ah); ekws.put(ah.getNameattr(), ah);
		
		ah.setNameattr("SYSTEM"); ah.setNameattr("_system"); ah.setType("String");
		ah.setUcd("pos.frame"); ah.setValue("ICRS");
		kws.put(ah.getNameattr(), ah); ekws.put(ah.getNameattr(), ah);
		
		ah.setNameattr("name"); ah.setNameattr("_name"); ah.setType("String");
		kws.put(ah.getNameattr(), ah); ekws.put(ah.getNameattr(), ah);
		
		ah.setNameattr("COL1"); ah.setNameattr("_col1"); ah.setType("int");
		kws.put(ah.getNameattr(), ah); ekws.put(ah.getNameattr(), ah);
		
		ah.setNameattr("COL2"); ah.setNameattr("_col2"); ah.setType("int");
		kws.put(ah.getNameattr(), ah); ekws.put(ah.getNameattr(), ah);
		
		this.ra = this.ra0; this.dec = this.dec0;
	}
	
	/**
	 * 
	 */
	private void incrementePos(){
		ra += STEP;
		this.gridStep ++;
		if( this.gridStep >= this.gridWidth  ){
			ra = ra0;
			dec += STEP;
		}
		
	}
	public boolean hasMoreElements() {
		return (pointer < size);
	}

	public Object nextElement() throws NumberFormatException, NullPointerException{
		Object[] retour = {ra, dec, "name" + pointer, ("col_1_" + pointer), ("col_2_" + pointer)};
		pointer ++;
		this.incrementePos();
		return retour;
	}

	public String getKWValueQuickly(String key) {
		return null;
	}

	public void setKWEntry(LinkedHashMap<String, AttributeHandler> tah)
			throws IgnoreException {
		this.ekws = tah;
	}

	public double[] getExtrema(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getRow(int index, int numHDU) throws IgnoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNRows() throws IgnoreException {
		return size;
	}

	public int getNCols() throws IgnoreException {
		return kws.size();
	}

	public void initEnumeration() throws IgnoreException {
		this.pointer = 0;
	}

	public LinkedHashMap<String, ArrayList<AttributeHandler>> getMap(
			String category) throws IgnoreException {
		LinkedHashMap<String, ArrayList<AttributeHandler>> retour = new LinkedHashMap<String, ArrayList<AttributeHandler>>();
		retour .put("HEADER", new ArrayList<AttributeHandler>(kws.values()));
		retour .put("TABLE", new ArrayList<AttributeHandler>(ekws.values()));
		return retour;
	}

	public SpaceFrame getSpaceFrame() {
		return frame;
	}

	public void setSpaceFrameForTable() throws IgnoreException {
		frame = new SpaceFrame(kws);
	}

	public void setSpaceFrame() {
		frame = new SpaceFrame(kws);
	}

}
