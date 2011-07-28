package saadadb.query.parser;

import java.text.ParseException;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.unit.Unit;
import saadadb.util.Messenger;

public class UnitHandler {
	public static final boolean isValid(String unit) {
		if(unit.equals("") || unit.equals("none")){return true;}
		try {
			(new Unit()).setUnit(unit);
			return true;
		} catch (ParseException e){
			return false;
		}
	}

	/**
	 * @param userValue
	 * @param userUnit
	 * @param ahUnit
	 * @return
	 * @throws SaadaException
	 */
	public static final String computeValue(String userValue,String userUnit,String ahUnit) throws SaadaException {
		if(userUnit.equals("none") && !ahUnit.replace("none","").equals("")){
			Messenger.printMsg(Messenger.WARNING,"No unit defined for the value \""+userValue+"\"! Database unit=\""+ahUnit+"\". No conversion perform!");// The query may not be meanfull!");
			return userValue;
		}
		if((ahUnit.equals("none") || ahUnit.equals("")) && !userUnit.replace("none","").equals("")){
			Messenger.printMsg(Messenger.WARNING,"No unit given in the query ! No conversion perform!");
			return userValue;
		}
		/*
		 * nor unit in the query neither in the AH
		 */
		if((ahUnit.equals("none") || ahUnit.equals("")) && userUnit.replace("none","").equals("")){
			return userValue;
		}
		Unit inUnit=null;
		Unit outUnit=null;
		/*
		 * Units with a constant factor != 1 (e.g 12/s) canot be handled by the converter
		 */
		if(  ahUnit.matches("^(1[eE.]|0|[2-9]|\\d{2,}).*")) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The column unit\""+ahUnit+"\" starts with a digit! If" +
				"the conversion is perfomr with it, digit could be false! ");
		}
		try {
			inUnit  = new Unit(userValue + "(" +userUnit +")");
		} catch (ParseException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Unit \""+userUnit+"\" not valid!");
		}
		try {
			outUnit = new Unit(ahUnit);
		} catch (ParseException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Unit \""+ahUnit+"\" not valid!");
		}
		// Particular case h:m:s <-> deg	
		if(inUnit.getUnit().compareTo("\"h:m:s\"")==0 && outUnit.getUnit().compareTo("deg")==0      )return Double.toString(inUnit.getValue()*15.0);
		if(inUnit.getUnit().compareTo("deg")==0       && outUnit.getUnit().compareTo("\"h:m:s\"")==0)return Double.toString(inUnit.getValue()/15.0);
		// General case
		if(inUnit.isCompatibleWith(outUnit) && ahUnit.compareTo("[---]")!=0){
			outUnit.convertFrom(inUnit);
			if( Double.isNaN(outUnit.value)  || Double.isInfinite(outUnit.value) ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Operand " + userValue + " cannot be converted from " + inUnit.getUnit() + " to " + outUnit.getUnit());
			}
			return String.valueOf(outUnit.value);
		}else{
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Unit \""+userUnit+"\" of the query and unit \""+ahUnit+"\" of the classe are incompatibles or not supported");
		}
		return ahUnit;
	}

	/**
	 * @param userUnit
	 * @param ahUnit
	 * @param attrName
	 * @return
	 * @throws SaadaException
	 */
	public static final String getConvFunction(String userUnit,String ahUnit,String attrName) throws QueryException, ArithmeticException {
		if(userUnit.equals("none")){
			Messenger.printMsg(Messenger.WARNING,"No unit define for the attribute! No conversion perform!");
			return attrName;
		}
		if(ahUnit.length() == 0 || ahUnit.equals("none")){
			Messenger.printMsg(Messenger.WARNING,"No unit define for the attribute \""+attrName+"\"! No conversion perform!");
			return attrName;
		}
		/*
		 * Units with a constant factor != 1 (e.g 12/s) canot be handled by the converter
		 */
		if(  ahUnit.matches("^(1[eE.]|0|[2-9]|\\d{2,}).*")) {
		//if(userUnit.matches("^\\d.*")) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The user unit\""+ahUnit+"\" starts with a digit! If the conversion was performed, it could be false! ");
		}
		if(ahUnit.matches("^\\d.*"))   QueryException.throwNewException(SaadaException.SYNTAX_ERROR, "The attribute handler unit\""+ahUnit+"\" start with a digit!  If the conversion was perform, it could be false! ");
		ahUnit   = "1"+ahUnit;
		try {
			return (new Unit(userUnit)).convertFromStr(new Unit(ahUnit)).replaceAll("UNIT",attrName);
		} catch (ParseException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Unit \""+userUnit+"\" or \""+ahUnit+"\" not valid!");
		}
		return attrName;
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println((new Unit("arcsec")).convertFromStr(new Unit("100deg")));
		System.out.println(UnitHandler.getConvFunction("m/s", "km/s", "AAAA"));
		System.out.println(UnitHandler.computeValue("10", "m/s", "km/h"));
	}
}
