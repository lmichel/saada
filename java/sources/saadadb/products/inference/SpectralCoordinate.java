package saadadb.products.inference;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import saadadb.meta.AttributeHandler;
import saadadb.util.ChangeKey;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Unit;

/**
 * @author michel
 *
 */
public class SpectralCoordinate{
	public static final int WAVELENGTH      = 0;
	public static final int FREQUENCY       = 1;
	public static final int ENERGY          = 2;
	public static final int CHANNEL         = 3;
	public static final double h            = 6.626068e-34; // constante de Planck en m2.kg/s
	public static final double c            = 2.997925e8; // c en m/sec
	public static final double joule        = 1.60219e-19;
	public static Map<String, UnitRef> allowed_units = initUnits();

	private int type;
	private int naxis;
	private String unit = SaadaConstant.STRING;
	private double minValue = SaadaConstant.DOUBLE;
	private double maxValue = SaadaConstant.DOUBLE;

	private double orgMin = SaadaConstant.DOUBLE;
	private double orgMax = SaadaConstant.DOUBLE;
	private double raWCSCenter= SaadaConstant.DOUBLE;
	private double decWCSCenter= SaadaConstant.DOUBLE;
	private int dispersionAxeNum;
	private int nbBins = SaadaConstant.INT;
	public String detectionMessage ="";

	private Map<String, AttributeHandler> attributesList;
	private String mappedUnit;

	/**
	 * @param naxis
	 * @param num_axe
	 * @param type
	 * @param unit
	 */
	public SpectralCoordinate(int naxis, int num_axe, int type, String unit){
		if( type < WAVELENGTH || type > CHANNEL ){
			Messenger.printMsg(Messenger.ERROR, "Unknown Coordinate Type <"+type+">");
		}else if( naxis != 1 && naxis != 2 ) {
			Messenger.printMsg(Messenger.ERROR, "num_axe must equals 1 or 2");		
		}else if( num_axe != 1 && num_axe != 2 ){
			Messenger.printMsg(Messenger.ERROR, "naxis must equals 1 or 2");		
		}else{
			UnitRef o;
			String sunit;
			this.dispersionAxeNum = num_axe - 1;
			this.naxis   = naxis;
			if( unit == null || unit.equals("") || "NULL".equals(unit)){
				sunit = "channel";
			}else{
				sunit = unit;
			}
			if( (o = SpectralCoordinate.getUniRef(sunit)) == null ){
				Messenger.printMsg(Messenger.ERROR, " Spectral Coordinate not valid: Unknown unit <"+sunit+">"); 
			}else if( o.type != type ){
				Messenger.printMsg(Messenger.ERROR, "Spectral Coordinate not valid: Unit <"+sunit 
						+ "> doesn't match type <" + SpectralCoordinate.getDispersionName(type)
						+ "> Allowed units are <" + SpectralCoordinate.getStringUnitsForType(type)+">");
			}else{
				this.unit = sunit;
				this.type = type;
			}
		}
	}

	public String getOrgUnit(){
		return mappedUnit;
	}

	public double getOrgMax(){
		return orgMax;
	}

	public double getOrgMin(){
		return orgMin;
	}

	public void setOrgUnit(String orgUnit){
		if( orgUnit.startsWith("ANGSTROM") ){
			this.mappedUnit = "Angstrom";
		}
		else {
			this.mappedUnit = orgUnit;
		}
	}
	public void setNbBins(int nbBins){
		this.nbBins = nbBins;
	}
	public int getNbBins(){
		return this.nbBins;
	}

	public void setOrgMax(double orgMax){
		this.orgMax = orgMax;
	}

	public void setOrgMin(double orgMin){
		this.orgMin = orgMin;
	}

	public String getConvertedUnit(){
		return unit;
	}

	public int getConvertedType(){
		return type;
	}

	public int getConvertedNaxis(){
		return naxis;
	}

	public double getConvertedMax(){
		return maxValue;
	}

	public double getConvertedMin(){
		return minValue;
	}

	private static Map<String, UnitRef> initUnits() {
		Map<String, UnitRef> sc =  new LinkedHashMap<String, UnitRef>();
		sc.put("angstrom", new UnitRef("Angstrom" , new Integer(WAVELENGTH)));
		sc.put("angstroms", new UnitRef("Angstrom" , new Integer(WAVELENGTH)));
		sc.put("a"       , new UnitRef("Angstrom" , new Integer(WAVELENGTH)));
		sc.put("nm"      , new UnitRef("nm"       , new Integer(WAVELENGTH)));
		sc.put("um"      , new UnitRef("um"       , new Integer(WAVELENGTH))); 
		sc.put("m"       , new UnitRef("m"        , new Integer(WAVELENGTH)));
		sc.put("mm"      , new UnitRef("mm"       , new Integer(WAVELENGTH)));
		sc.put("cm"      , new UnitRef("cm"       , new Integer(WAVELENGTH)));
		sc.put("km"      , new UnitRef("km"       , new Integer(WAVELENGTH)));

		sc.put("hz" , new UnitRef("Hz" , new Integer(FREQUENCY)));
		sc.put("khz", new UnitRef("KHz", new Integer(FREQUENCY)));
		sc.put("mhz", new UnitRef("MHz", new Integer(FREQUENCY)));
		sc.put("ghz", new UnitRef("GHz", new Integer(FREQUENCY)));

		sc.put("ev" , new UnitRef("eV"  , new Integer(ENERGY)));
		sc.put("kev", new UnitRef("keV" , new Integer(ENERGY)));
		sc.put("mev", new UnitRef("MeV" , new Integer(ENERGY)));
		sc.put("gev", new UnitRef("GeV" , new Integer(ENERGY)));
		sc.put("tev", new UnitRef("TeV" , new Integer(ENERGY)));

		sc.put("", new UnitRef("channel", new Integer(CHANNEL)));
		return sc;
	}
	/**
	 * Implement a map access case unsensitive
	 * @param unit
	 * @return
	 */
	private static UnitRef getUniRef(String unit){
		if( unit == null){
			return null;
		} else {
			return allowed_units.get(unit.toLowerCase());
		}
	}

	/**
	 * Returns the the dispersion name (energy, ...) for the type
	 * @param type
	 * @return
	 */
	public static String getDispersionName(int type) {
		if( type == WAVELENGTH ){
			return "WAVELENGTH";
		}else if( type == FREQUENCY ){
			return "FREQUENCY";
		}else if( type == ENERGY ){
			return "ENERGY";
		}else if( type == CHANNEL ){
			return "CHANNEL";
		}else{
			return "Unknown type <"+type+">";
		}
	}


	/**
	 * Returns the numeric code of the dispersion type (energy, ...)
	 * @param type
	 * @return
	 */
	public static int getDispersionCode(String type) {
		if(type.equals("WAVELENGTH")){
			return WAVELENGTH;
		}else if(type.equals("FREQUENCY")){
			return FREQUENCY;
		}else if(type.equals("ENERGY")){
			return ENERGY;
		}else if(type.equals("CHANNEL")){
			return CHANNEL;
		}else{
			return -1;
		}
	}

	/**
	 * Return a table of available units for the given type
	 * @param type
	 * @return
	 */
	public static String[] getUnitsForType(int type) {
		Vector<String> v = new Vector<String>();
		for(UnitRef ur: allowed_units.values()){
			if( ur.type == type ){
				v.add(ur.unit);
			}
		}
		return (String[])(v.toArray(new String[0]));
	}

	/**
	 * Returns a string merging all units available for the type
	 * @param type
	 * @return
	 */
	public static String getStringUnitsForType(int type) {
		String[] lstr = SpectralCoordinate.getUnitsForType(type);
		StringBuffer retour = new StringBuffer();
		for( int i=0 ; i<lstr.length ; i++ ){
			retour.append(lstr[i] + " ");
		}
		return retour.toString();
	}

	/**
	 * @return
	 */
	public boolean convert(){
		System.out.println("=================== " + this.unit);
		if( this.getOrgUnit() == null || this.getOrgUnit().equals(SaadaConstant.STRING) ){
			Messenger.printMsg(Messenger.TRACE, "No spectral unit found: cannot achieve the conversion");
			return false;
		} else{
			return this.convert(this.getOrgUnit(), this.orgMin, this.orgMax);
		}
	}

	/**
	 * @param unitOrg
	 * @param minOrg
	 * @param maxOrg
	 * @return
	 */
	private boolean convert(String unitOrg, double minOrg, double maxOrg){
		/*
		 * Input spectra has no unit as the SaadaDB itself. 
		 */
		if( (unitOrg == null|| unitOrg.equals("NULL") || unitOrg.equals("") || unitOrg.equalsIgnoreCase("channel")) ) {
			if( this.unit.equalsIgnoreCase("channel")){
				this.minValue = minOrg;
				this.maxValue = maxOrg;
				this.checkMinMax();
				Messenger.printMsg(Messenger.TRACE, "Spectral range not converted, take <" + minValue + ":" + maxValue + " " + this.unit + ">");
				return true;
			} else {
				Messenger.printMsg(Messenger.TRACE, "Can not convert channels to " + this.unit);
				return false;
			}
		}
		if(  Double.isNaN((minValue = convertSaada(unitOrg, this.unit, minOrg))) ) {
			Messenger.printMsg(Messenger.ERROR, "Conversion <"+ minOrg+unitOrg+"> into <"+this.unit+"> failed");
			return false;
		}
		if( Double.isNaN((maxValue = convertSaada(unitOrg, this.unit, maxOrg))) ) {
			Messenger.printMsg(Messenger.ERROR, "Conversion <"+ maxOrg+unitOrg+"> into <"+this.unit+"> failed");
			return false;
		}
		this.checkMinMax();
		Messenger.printMsg(Messenger.TRACE, "Spectral range: <" + minOrg + ":" + maxOrg + " " + unitOrg + "> converted to <" + minValue + ":" + maxValue + " " + this.unit + ">");
		return true;
	} 



	/**
	 * 
	 */
	public void checkMinMax(){
		if(this.minValue > this.maxValue){
			double x = this.minValue;
			this.minValue = this.maxValue;
			this.maxValue = x;
		}
	}

	/**
	 * @return
	 */
	public String getRange(){
		return "Range in SaadaDB units:  Min <"+this.minValue+" "+this.unit+"> Max <"+this.maxValue+" "+this.unit+">";
	}

	/**
	 * @param value
	 * @param unit_org
	 * @param new_unit
	 * @return
	 * @throws Exception
	 */
	public static double convertValue(double value, String unit_org, String new_unit) throws Exception{
		Unit un = new Unit(new_unit);
		un.convertFrom(new Unit(value + unit_org));
		return un.value;
	}

	/**
	 * @param unitOrg
	 * @param unitNew
	 * @param value
	 * @return
	 */
	public static double convertSaada(String unitOrg, String unitNew, double value){
		int vTypeOrg = 0;
		int vTypeNew = 0;
		UnitRef ur;

		if( "AutoDetect".equals(unitOrg) ) {
			Messenger.printMsg(Messenger.DEBUG, "No unit given");
			return SaadaConstant.DOUBLE;
		}
		if((ur = getUniRef(unitOrg)) != null){
			vTypeOrg = ur.type;
			unitOrg = ur.unit;
		}else{
			if( "".equals(unitOrg)) {
				Messenger.printMsg(Messenger.ERROR, "No valid unit <"+unitOrg+"> ");
			}
			return SaadaConstant.DOUBLE;
		}
		if((ur = getUniRef(unitNew)) != null){
			vTypeNew = ur.type;
			unitNew = ur.unit;
		}else{
			new Exception().printStackTrace();
			Messenger.printMsg(Messenger.ERROR, "No new valid unit <"+unitNew+"> set for the SaadaDB");
			return SaadaConstant.DOUBLE;
		}
		if(vTypeOrg == vTypeNew){
			if( unitOrg.equals(unitNew) ) {
				return value;
			}
			try{
				return convertValue(value, unitOrg, unitNew);
			}catch(Exception e){
				e.printStackTrace();
				Messenger.printMsg(Messenger.ERROR, "Can not convert <"+unitOrg+"> into <"+unitNew+">");
				return SaadaConstant.DOUBLE;
			}
		} else if (vTypeNew == CHANNEL) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "SaadaDB in channel mode: no conversion to do");
			return value;
		}else{
			switch(vTypeOrg){
			case WAVELENGTH:
				if(vTypeNew == FREQUENCY){
					return convertWaveLengthIntoFrequency(value, unitOrg, unitNew);
				}else{
					if(vTypeNew == ENERGY){
						return convertWaveLengthIntoEnergy(value, unitOrg, unitNew);
					}
				}
				break;
			case FREQUENCY:
				if(vTypeNew == WAVELENGTH){
					return convertFrequencyIntoWaveLength(value, unitOrg, unitNew);
				}else{
					if(vTypeNew == ENERGY){
						return convertFrequencyIntoEnergy(value, unitOrg, unitNew);
					}
				}
				break;
			case ENERGY:
				if(vTypeNew == FREQUENCY){
					return convertEnergyIntoFrequency(value, unitOrg, unitNew);
				}else{
					if(vTypeNew == WAVELENGTH){
						return convertEnergyIntoWaveLength(value, unitOrg, unitNew);
					}
				}
				break;
			case CHANNEL:
				Messenger.printMsg(Messenger.ERROR, "Can not convert channels to something else");
				return SaadaConstant.DOUBLE;
			default:
				Messenger.printMsg(Messenger.ERROR, "Unknown spectral axis type <"+vTypeOrg+"> of the SaadaDB");
				return SaadaConstant.DOUBLE;
			}
			Messenger.printMsg(Messenger.ERROR, "Unknown spectrail axis type <"+vTypeNew+"> read into the product");
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param value
	 * @param unitOrg
	 * @param unitNew
	 * @return
	 */
	public static double convertWaveLengthIntoEnergy(double value, String unitOrg, String unitNew){
		if( value == SaadaConstant.DOUBLE) {
			return SaadaConstant.DOUBLE;
		}
		try{
			if(!unitOrg.equals("m")){
				value = convertValue(value, unitOrg, "m");
			}
			double energySI = (c*h)/value;
			double energy = energySI/joule;
			if(!unitNew.equals("eV")){
				return convertValue(energy, "eV", unitNew);
			}
			return energy;
		}catch(Exception e){
			Messenger.printStackTrace(e);
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param value
	 * @param unitOrg
	 * @param unitNew
	 * @return
	 */
	public static double convertEnergyIntoWaveLength(double value, String unitOrg, String unitNew){
		try{
			if(!unitOrg.equals("eV")){
				value = convertValue(value, unitOrg, "eV");
			}
			if( value == 0 ){
				Messenger.printMsg(Messenger.TRACE, "Cannot convert energy=0 to a wavelength (div by 0)");
				return SaadaConstant.DOUBLE;				
			}
			double energySI = value*joule;
			double waveLength = (c*h)/energySI;
			if(!unitNew.equals("m")){
				return convertValue(waveLength, "m", unitNew);
			}
			return waveLength;
		}catch(Exception e){
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param value
	 * @param unitOrg
	 * @param unitNew
	 * @return
	 */
	public static double convertWaveLengthIntoFrequency(double value, String unitOrg, String unitNew){
		try{
			if(!unitOrg.equals("m")){
				value = convertValue(value, unitOrg, "m");
			}
			if( value == 0 ){
				Messenger.printMsg(Messenger.TRACE, "Cannot convert wavelength=0 to a frequency");
				return SaadaConstant.DOUBLE;				
			}
			double frequency = c/value;
			if(!unitNew.equals("Hz")){
				return convertValue(frequency, "Hz", unitNew);
			}
			return frequency;
		}catch(Exception e){
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param value
	 * @param unitOrg
	 * @param unitNew
	 * @return
	 */
	public static double convertFrequencyIntoWaveLength(double value, String unitOrg, String unitNew){
		try{
			if(!unitOrg.equals("Hz")){
				value = convertValue(value, unitOrg, "Hz");
			}
			if( value == 0 ){
				Messenger.printMsg(Messenger.TRACE, "Cannot convert an frequency NULL to a wavelength");
				return SaadaConstant.DOUBLE;				
			}
			double waveLength = c/value;
			if(!unitNew.equals("m")){
				return convertValue(waveLength, "m", unitNew);
			}
			return waveLength;
		}catch(Exception e){
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param value
	 * @param unitOrg
	 * @param unitNew
	 * @return
	 */
	public static double convertEnergyIntoFrequency(double value, String unitOrg, String unitNew){
		try{
			if(!unitOrg.equals("eV")){
				value = convertValue(value, unitOrg, "eV");
			}
			double energySI = value*joule;
			double frequency = energySI/h;
			if(!unitNew.equals("Hz")){
				return convertValue(frequency, "Hz", unitNew);
			}
			return frequency;
		}catch(Exception e){
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param value
	 * @param unitOrg
	 * @param unitNew
	 * @return
	 */
	public static double convertFrequencyIntoEnergy(double value, String unitOrg, String unitNew){
		try{
			if(!unitOrg.equals("Hz")){
				value = convertValue(value, unitOrg, "Hz");
			}
			double energySI = value*h;
			double energy = energySI/joule;
			if(!unitNew.equals("eV")){
				return convertValue(energy, "eV", unitNew);
			}
			return energy;
		}catch(Exception e){
			return SaadaConstant.DOUBLE;
		}
	}

	/**
	 * @param reference
	 * @param test
	 * @return
	 */
	public static boolean isSynonymous(String reference, String test){
		if(test!=null){
			if(reference.equals("WAVELENGTH")){
				return (test.equals("WAVE") || test.equals("em.wl"));
			}else if(reference.equals("FREQUENCY")){
				return (test.equals("FREQ") || test.equals("em.freq"));
			}else if(reference.equals("ENERGY")){
				return (test.equals("ENER") || test.equals("em.energy"));
			}else if(reference.equals("CHANNEL")){
				return (test.equals("No units") || test.equals(""));
			}
		}
		return false;
	}


	/**
	 * @param tableAttributeHandler
	 * @return
	 * @throws Exception 
	 */
	public boolean convertWCS(Map<String, AttributeHandler> tableAttributeHandler) throws Exception{
		this.attributesList = tableAttributeHandler;
		WCSModel wm = null;
		try {
			wm = new WCSModel(tableAttributeHandler);
		} catch (Exception e) {
			return false;
		}
		if( !wm.isKwset_ok() ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "WCS readout failed");
			return false;
		}
		String unitOrg = this.mappedUnit;
		wm.projectAllAxesToRealWord();
		if( !wm.isKwset_ok() ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Cannot achieve the WCS projection");
			return false;
		}
		/*
		 * A dispersion axe is found but unit can be not set or not valid
		 * In this case, we take unit given by the configuration
		 */
		int dan;
		if( (dan = wm.getDispersionAxe()) != SaadaConstant.INT) {
			this.dispersionAxeNum = dan; 
			this.nbBins = wm.getNAXISi(dan);
			UnitRef unit_class = getUniRef(wm.getCUNIT(dispersionAxeNum));
			if(unit_class == null) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Can not get unit from WCS keywords: Use " + this.mappedUnit + " as unit (given by the configuration)");
			} else  {
				this.mappedUnit = unit_class.unit;
			}
			this.detectionMessage = wm.detectionMessage;
		}
		/*
		 * No dispersion axe: we consider the longer one as a good candidate
		 */
		else {
			int old_max = 0;
			for( int axe=0 ; axe<wm.getNAXIS() ; axe++) {
				if( wm.getNAXISi(axe) > old_max ) {
					old_max = wm.getNAXISi(axe);
					this.dispersionAxeNum = axe;
					this.nbBins = wm.getNAXISi(axe);
				}
				AttributeHandler ah;
				if( (ah = attributesList.get(ChangeKey.changeKey("DISPERS").toLowerCase())) != null || 
						(ah = attributesList.get(ChangeKey.changeKey("DISP").toLowerCase())) != null ) {		

					String dispers = ah.getValue();	
					/*
					 * Dispersion is often a string from which we attempt to extract the unit 
					 */
					Matcher m   = (Pattern.compile("[^\\s]*\\/[^\\s,\\']*")).matcher(dispers);
					while( m.find() ) {
						String disp_unit = m.group(0);
						unitOrg = disp_unit.substring(0, disp_unit.indexOf("/"));
						/*
						 * "Angtroem" is often written like "Ampere"
						 */
						if( unitOrg.equals("A") ) {
							unitOrg = "Angstrom";
						}

						UnitRef unit_class = getUniRef(unitOrg);
						if(unit_class == null) {
							if (Messenger.debug_mode)
								Messenger.printMsg(Messenger.DEBUG, "Can not get unit from DISPERS keywords: Use " + this.mappedUnit + " as unit (given by the configuration)");
						} else  {
							this.mappedUnit = unit_class.unit;
						}
						this.detectionMessage = "DISP(ERS) keyword found expressed in " 
							+  m.group(0) + ": try to take " + this.mappedUnit + " as dispersion unit";
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, this.detectionMessage);
					}
				}
			}			
		}
		this.orgMin = wm.getMinValue(dispersionAxeNum);
		this.orgMax = wm.getMaxValue(dispersionAxeNum);
		this.raWCSCenter = wm.getCenterRa();
		this.decWCSCenter = wm.getCenterDec();
		if( this.convert(this.mappedUnit,this.orgMin, this.orgMax ) ) {
			Messenger.printMsg(Messenger.TRACE, this.getRange());
			return true;
		} else {
			this.detectionMessage = "";
			return false;
		}
	}

	/**
	 * @return
	 */
	double getRaWCSCenter() {
		return this.raWCSCenter;
	}
	/**
	 * @return
	 */
	double getDecWCSCenter() {
		return this.decWCSCenter;
	}
}

