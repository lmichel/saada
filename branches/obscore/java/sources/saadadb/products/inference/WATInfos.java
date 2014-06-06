package saadadb.products.inference;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * Extract relavant data from the WAT IRAF keywords and makes them accessible through accessors
 * @see http://iraf.net/irafdocs/specwcs.php
 * @author michel
 * @version $Id$
 */
public class WATInfos {
	private String[] WATInfos = null;
	private String   WATSystem = null;
	private int      NAXIS;
	private WATAxe[] WATAxes = null;

	/**
	 * @param NAXIS number of axes within the WCS matrix
	 * @param attributesList
	 * @throws Exception
	 */
	public WATInfos(int NAXIS, Map<String, AttributeHandler> attributesList) throws Exception{
		this.NAXIS = NAXIS;
		this.WATInfos = new String[this.NAXIS];
		this.WATAxes = new WATAxe[this.NAXIS];
		/*
		 * Extract data from WAP kws
		 */
		for( int i=0 ; i<=this.NAXIS ; i++ ) {
			String radix = "_wat" + i + "_";
			Set<String> ahn = new TreeSet<String>();
			if( i == 0 )  this.WATSystem = ""; else this.WATInfos[i-1] = "";
			for( String k: attributesList.keySet())  {
				if( k.startsWith(radix)) {
					ahn.add(k);
				}
			}
			for( String wah: ahn){
				if( i == 0 ) {
					this.WATSystem += attributesList.get(wah).getValue();
				} else {
					this.WATInfos[i-1] += attributesList.get(wah).getValue();
				}
			}			
			if(ahn.size() > 0 && Messenger.debug_mode) {
				if( i == 0 )  Messenger.printMsg(Messenger.DEBUG, "WAT System " + this.WATSystem);
				else          Messenger.printMsg(Messenger.DEBUG, "WAT info axe " + i + " "  + this.WATInfos[i-1]);
			}
		}
		/*
		 * Parse data extracted from WAP KWs
		 */
		for( int i=0 ; i<this.NAXIS ; i++ ) {
			this.WATAxes[i] = new WATAxe(this.WATInfos[i]);
		}
	}
	
	/******************************************
	 * Accessors for the global WCS set
	 ******************************************/
	/**
	 * @return
	 */
	public boolean isEquispec() {
		return (WATSystem.equalsIgnoreCase("system=equispec"));
	}
	/**
	 * @return
	 */
	public boolean isMultspec() {
		return (WATSystem.equalsIgnoreCase("system=multispec"));
	}
	/**
	 * @return
	 */
	public boolean isWorld() {
		return (WATSystem.equalsIgnoreCase("system=world"));
	}
	
	/******************************************
	 * Getter for the per-axe parameters
	 ******************************************/
	/**
	 * @param axe
	 * @return
	 */
	public  boolean isPixel(int axe){
		return (this.WATAxes[axe].label.equalsIgnoreCase("pixel"));
	}
	/**
	 * @param axe
	 * @return
	 * @throws Exception
	 */
	public  boolean isWavelength(int axe) throws Exception{ 
		return (this.WATAxes[axe].label.equalsIgnoreCase("wavelength"));
	}
	/**
	 * @param axe
	 * @return
	 * @throws Exception
	 */
	public  boolean isLinear(int axe) throws Exception{
		return (this.WATAxes[axe].wtype.equalsIgnoreCase("linear"));
	}
	/**
	 * @param axe
	 * @return
	 * @throws Exception
	 */
	public String getUnit(int axe) throws Exception{
		return this.WATAxes[axe].units;
	}
	/**
	 * @return  return {@link SaadaConstant#INT} if nothing found
	 * @throws Exception
	 */
	public int getDispersionAxes()  throws Exception{
		for( int i=0 ; i<this.NAXIS ; i++ ){
			if( this.isWavelength(i) ) {
				return i;
			}
		}
		return SaadaConstant.INT;
	}

	/**
	 * Inner class modeling
	 * @author michel
	 * @version $Id$
	 */
	class WATAxe {
		String wtype = "";
		String label = "";
		String units = "";
		WATAxe(String kwValue){
			String[] fields = kwValue.split(" ");
			for( String f: fields) {
				f = f.trim();
				if( f.startsWith("wtype=") ) {
					this.wtype = f.replace("wtype=", "");
				} else if( f.startsWith("label=") ) {
					this.label = f.replace("label=", "");
				} else if( f.startsWith("units=") ) {
					this.units = f.replace("units=", "");
				}
			}
		}
	}
}
