package saadadb.products.inference;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.enums.DispersionType;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.util.ChangeKey;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Unit;

/**
 * @author michel
 *
 */
public class SpectralCoordinate{

	public static final double h            = 6.626068e-34; // constante de Planck en m2.kg/s
	public static final double c            = 2.997925e8; // c en m/sec
	public static final double joule        = 1.60219e-19;

	private DispersionType type;
	/**
	 * Unit to convert to
	 */
	private String finalUnit = SaadaConstant.STRING;  // Unit to convert tpo
	private double minValue = SaadaConstant.DOUBLE;
	private double maxValue = SaadaConstant.DOUBLE;

	/**
	 * Unit to convert from
	 */
	private String mappedUnit;
	private double orgMin = SaadaConstant.DOUBLE;
	private double orgMax = SaadaConstant.DOUBLE;
	
	private double raWCSCenter= SaadaConstant.DOUBLE;
	private double decWCSCenter= SaadaConstant.DOUBLE;
	//	private int dispersionAxeNum;
	private int nbBins = SaadaConstant.INT;
	public String detectionMessage ="";

	private Map<String, AttributeHandler> attributesList;

	/**
	 * @param naxis
	 * @param num_axe
	 * @param type
	 * @param unit
	 */
	public SpectralCoordinate(String unit){
		String sunit;
		if(unit == null || unit.equals("") || "NULL".equals(unit)){
			sunit = "channel";
		}else{
			sunit = unit;
		}
		if(  UnitRef.getUniRef(sunit) == null ){
			Messenger.printMsg(Messenger.ERROR, " Spectral Coordinate not valid: Unknown unit <"+sunit+">"); 
		} else{
			this.finalUnit = sunit;
			this.type = UnitRef.getDispersionType(sunit);
		}
	}

	public SpectralCoordinate(){
		this(Database.getSpect_unit());
	}
	/**
	 * @return
	 */
	public String getMappedUnit(){
		return mappedUnit;
	}
	/**
	 * @return
	 */
	public double getOrgMax(){
		return orgMax;
	}
	/**
	 * @return
	 */
	public double getOrgMin(){
		return orgMin;
	}
	/**
	 * @param orgUnit
	 */
	public void setMappedUnit(String orgUnit){
		if( orgUnit.startsWith("ANGSTROM") ){
			this.mappedUnit = "Angstrom";
		} else {
			this.mappedUnit = orgUnit;
		}
	}
	/**
	 * @param nbBins
	 */
	public void setNbBins(int nbBins){
		this.nbBins = nbBins;
	}
	/**
	 * @return
	 */
	public int getNbBins(){
		return this.nbBins;
	}
	/**
	 * @param orgMax
	 */
	public void setOrgMax(double orgMax){
		this.orgMax = orgMax;
	}
	/**
	 * @param orgMin
	 */
	public void setOrgMin(double orgMin){
		this.orgMin = orgMin;
	}
	/**
	 * @param ds
	 */
	public void setOrgMinMax(double[] ds) {
		if( ds == null || ds.length != 2 ) {
			this.setOrgMax(SaadaConstant.DOUBLE);
			this.setOrgMin(SaadaConstant.DOUBLE);
		} else {
			this.setOrgMax(ds[1]);
			this.setOrgMin(ds[0]);		
		}
	}
	/**
	 * @return
	 */
	public String getFinalUnit(){
		return finalUnit;
	}

	/**
	 * @return
	 */
	public DispersionType getConvertedType(){
		return type;
	}

	/**
	 * @return
	 */
	public double getConvertedMax(){
		return maxValue;
	}
	/**
	 * @return
	 */
	public double getConvertedMin(){
		return minValue;
	}

	/**
	 * Returns the the dispersion name (energy, ...) for the type
	 * @param type
	 * @return
	 */
	public static DispersionType getDispersionName(DispersionType type) {
		return  type;
	}


	/**
	 * @return
	 */
	public boolean convert(){
		if( this.getMappedUnit() == null || this.getMappedUnit().equals(SaadaConstant.STRING) ){
			Messenger.printMsg(Messenger.TRACE, "No spectral unit found: cannot achieve the conversion");
			return false;
		} else{
			return this.convert(this.getMappedUnit(), this.orgMin, this.orgMax);
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
			if( this.finalUnit.equalsIgnoreCase("channel")){
				this.minValue = minOrg;
				this.maxValue = maxOrg;
				this.checkMinMax();
				Messenger.printMsg(Messenger.TRACE, "Spectral range not converted, take <" + minValue + ":" + maxValue + " " + this.finalUnit + ">");
				return true;
			} else {
				Messenger.printMsg(Messenger.TRACE, "Can not convert channels to " + this.finalUnit);
				return false;
			}
		}
		if(  Double.isNaN((minValue = convertSaada(unitOrg, this.finalUnit, minOrg))) ) {
			Messenger.printMsg(Messenger.ERROR, "Conversion <"+ minOrg+unitOrg+"> into <"+this.finalUnit+"> failed");
			return false;
		}
		if( Double.isNaN((maxValue = convertSaada(unitOrg, this.finalUnit, maxOrg))) ) {
			Messenger.printMsg(Messenger.ERROR, "Conversion <"+ maxOrg+unitOrg+"> into <"+this.finalUnit+"> failed");
			return false;
		}
		this.checkMinMax();
		Messenger.printMsg(Messenger.TRACE, "Spectral range: <" + minOrg + ":" + maxOrg + " " + unitOrg + "> converted to <" + minValue + ":" + maxValue + " " + this.finalUnit + ">");
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
		return "Range in SaadaDB units:  Min <"+this.minValue+" "+this.finalUnit+"> Max <"+this.maxValue+" "+this.finalUnit+">";
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
		DispersionType vTypeOrg;
		DispersionType vTypeNew;
		UnitRef ur;

		if( "AutoDetect".equals(unitOrg) ) {
			Messenger.printMsg(Messenger.DEBUG, "No unit given");
			return SaadaConstant.DOUBLE;
		}
		if((ur = UnitRef.getUniRef(unitOrg)) != null){
			vTypeOrg = ur.type;
			unitOrg = ur.unit;
		}else{
			if( "".equals(unitOrg)) {
				Messenger.printMsg(Messenger.ERROR, "No valid unit <"+unitOrg+"> ");
			}
			return SaadaConstant.DOUBLE;
		}
		if((ur = UnitRef.getUniRef(unitNew)) != null){
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
		} else if (vTypeNew == DispersionType.CHANNEL) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "SaadaDB in channel mode: no conversion to do");
			return value;
		}else{
			switch(vTypeOrg){
			case WAVELENGTH:
				if(vTypeNew == DispersionType.FREQUENCY){
					return convertWaveLengthIntoFrequency(value, unitOrg, unitNew);
				}else{
					if(vTypeNew == DispersionType.ENERGY){
						return convertWaveLengthIntoEnergy(value, unitOrg, unitNew);
					}
				}
				break;
			case FREQUENCY:
				if(vTypeNew == DispersionType.WAVELENGTH){
					return convertFrequencyIntoWaveLength(value, unitOrg, unitNew);
				}else{
					if(vTypeNew == DispersionType.ENERGY){
						return convertFrequencyIntoEnergy(value, unitOrg, unitNew);
					}
				}
				break;
			case ENERGY:
				if(vTypeNew == DispersionType.FREQUENCY){
					return convertEnergyIntoFrequency(value, unitOrg, unitNew);
				}else{
					if(vTypeNew == DispersionType.WAVELENGTH){
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
		int dispersionAxeNum = 0;
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
			dispersionAxeNum = dan; 
			this.nbBins = wm.getNAXISi(dan);
			UnitRef unit_class = UnitRef.getUniRef(wm.getCUNIT(dispersionAxeNum));
			if(unit_class == null) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Can not get unit from WCS keywords: Use " + this.mappedUnit + " as unit (given by the configuration)");
			} else  {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Take " + unit_class.unit  + " as unit");
				this.mappedUnit = unit_class.unit;
			}
			this.detectionMessage = wm.detectionMessage;
		}
		/*
		 * No dispersion axe: we consider the longer one as a good candidate
		 */
		else {
			return false;
/*			int old_max = 0;
			for( int axe=0 ; axe<wm.getNAXIS() ; axe++) {
				if( wm.getNAXISi(axe) > old_max ) {
					old_max = wm.getNAXISi(axe);
					dispersionAxeNum = axe;
					this.nbBins = wm.getNAXISi(axe);
				}
				AttributeHandler ah;
				if( (ah = attributesList.get(ChangeKey.changeKey("DISPERS").toLowerCase())) != null || 
						(ah = attributesList.get(ChangeKey.changeKey("DISP").toLowerCase())) != null ) {		

					String dispers = ah.getValue();	
					
					 * Dispersion is often a string from which we attempt to extract the unit 
					 
					Matcher m   = (Pattern.compile("[^\\s]*\\/[^\\s,\\']*")).matcher(dispers);
					while( m.find() ) {
						String disp_unit = m.group(0);
						unitOrg = disp_unit.substring(0, disp_unit.indexOf("/"));
						
						 * "Angtroem" is often written like "Ampere"
						 
						if( unitOrg.equals("A") ) {
							unitOrg = "Angstrom";
						}

						UnitRef unit_class = UnitRef.getUniRef(unitOrg);
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
*/		}
		this.orgMin = wm.getMinValue(dispersionAxeNum);
		this.orgMax = wm.getMaxValue(dispersionAxeNum);
		ColumnSetter[]cs = wm.getRadecCenter();
		if( !cs[0].notSet())
			this.raWCSCenter = Double.parseDouble(cs[0].getValue());
		if( !cs[1].notSet())
			this.decWCSCenter = Double.parseDouble(cs[1].getValue());
		return true;
//		if( this.convert(this.mappedUnit,this.orgMin, this.orgMax ) ) {
//			Messenger.printMsg(Messenger.TRACE, this.getRange());
//			return true;
//		} else {
//			this.detectionMessage = "";
//			return false;
//		}
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

