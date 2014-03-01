package saadadb.vo.request.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.query.executor.Query;
import saadadb.query.parser.PositionParser;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Merger;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import saadadb.vo.PseudoTableParser;

/**
 * @author laurent
 * @version 07/2011
 */
public class SSAPQuery extends VOQuery {
	public ArrayList<String>  formatTypes = new ArrayList<String>();
	private OidsaadaResultSet resultSet;

	public SSAPQuery() {
		formatAllowedValues = new ArrayList<String>( 
				Arrays.asList(new String[]{"application/fits", "application/xml", "application/x-votable+xml", "all"
						, "graphic", "metadata", "METADATA", "compliant", "xml", "votable", "fits"}));
		mandatoryDataParams = new String[0];
		mandatoryMetaParams = new String[]{"format"};
	}
	@Override
	public ArrayList<Long> getOids() throws Exception {
		ArrayList<Long> retour = new ArrayList<Long>();
		while( this.resultSet.next()) {
			retour.add(this.resultSet.getOId());
		}
		return retour;
	}

	@Override
	public SaadaInstanceResultSet getSaadaInstanceResultSet() {
		return resultSet;
	}

	@Override
	public void close() throws QueryException {
		this.resultSet.close();
		this.resultSet = null;
	}

	@Override
	public void buildQuery() throws Exception {
		double size = -1.0;
		String cooPos = "", cooSys = "";
		/*
		 * Mandatory params
		 */
		PseudoTableParser ptp = new PseudoTableParser(queryParams.get("collection"));
		queryString = "Select SPECTRUM From " + Merger.getMergedArray(ptp.getclasses()) 
		+ " In "  + Merger.getMergedArray(ptp.getCollections()) ;
		protocolParams.put("category", "SPECTRUM");
		protocolParams.put("class", Merger.getMergedArray(ptp.getclasses()));
		protocolParams.put("collection", Merger.getMergedArray(ptp.getCollections()));

		String value = this.queryParams.get("pos");
		if( value != null ) {
			// position parameter
			try {
				String pv[] = value.split(";"	);
				new PositionParser(pv[0]);
				protocolParams.put("pos", value);
				cooPos = value.replaceAll(",", " "); ;
				if( pv.length > 1 &&  pv[1].length() > 0 ) {
					cooSys = pv[1];
					protocolParams.put("coosys",pv[1]);
				}
				else {
					cooSys = "ICRS";
					protocolParams.put("coosys","ICRS");
				}
			} catch (Exception e) {e.printStackTrace();
				QueryException.throwNewException("ERROR",   "Unrecognized format for position parameter " + this.queryParams.get("pos"));
			}
		}

		value = this.queryParams.get("size");
		if( value != null ) {
			try {
				size = Double.parseDouble(value);
				protocolParams.put("size", Double.toString(size));
			} catch (NumberFormatException nfe) {
				QueryException.throwNewException("ERROR",  nfe);
			}
			if (size <= 0.0 || size > 1) {
				QueryException.throwNewException("OVERFLOW" ,"Angular SIZE should be between 0 and 1");
			}
			if( cooPos.length() == 0 ) {
				QueryException.throwNewException("ERROR",  "Cone search size given without position: make no sense");			
			}
			queryString  += "\nWherePosition{isInCircle(\"" + cooPos + "\"," + value + ",J2000," + cooSys + ")}";

		}
		else if( cooPos.length() > 0 ) {
			QueryException.throwNewException("ERROR",  "Position given without size: make no sense");						
		}


		value = this.queryParams.get("band");
		if( value != null ) {
			if (value.length() == 0) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"'band' parameter should have a value: Xmin/Xmax[unit]");
			}
			else {
				queryString  +=  "\nWhereAttributeSaada{" + getBandQueryPart(value) + "}";
			}
		}

		value = this.queryParams.get("time");
		if( value != null ) {
			Messenger.printMsg(Messenger.WARNING, "Time constraint not porcessed yet");
		}

		value = this.queryParams.get("top");
		if( value != null ) {	
			try {
				int top = Integer.parseInt(value);
				if( top <= 0 ) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"TOP parameter must be a positive integer");
				}
				queryString  +=  "\nLimit " + top;
			}catch (Exception e) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"TOP parameter must be a positive integer");
			}
			Messenger.printMsg(Messenger.WARNING, "Time constraint not porcessed yet");
		}
		/*
		 * OPtional parameters
		 */
		value = this.queryParams.get("format");
		if( value != null ) {
			formatTypes = getFormatValues(value);
		}

		protocolParams.put("query", queryString);
	}

	@Override
	public void runQuery() throws Exception {
		resultSet = (new Query()).runBasicQuery(queryString);
	}


	/**
	 * @param s
	 * @return
	 * @throws SaadaException
	 */
	private ArrayList<String> getFormatValues(String s) throws SaadaException {
		ArrayList<String> values = new ArrayList<String>();

		if (s == null) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"FORMAT parameter should have a value");
		} else {
			int indStart = 0;
			int indEnd, indComma = 0;
			String prm;
			while((indEnd = s.indexOf(',', indStart + 1)) != -1) {
				indComma++;
				prm = s.substring(indStart, indEnd);
				if (formatAllowedValues.contains(prm)) {
					values.add(prm);
					if (prm.equals("METADATA")) {
					//	setMetadataRequired(true);
					}
				} else {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Unsupported FORMAT value : " + prm);
				}
				indStart = indEnd + 1;
			}
			prm = s.substring(indStart, s.length());
			if (formatAllowedValues.contains(prm.toLowerCase()) || formatAllowedValues.contains(prm.toUpperCase()) ) {
				values.add(prm);
				if (prm.equals("METADATA")) {
					//setMetadataRequired(true);
				}
			} else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Unsupported FORMAT value : " + prm);
			}

		}
		return values;
	}

	/**
	 * @param value
	 * @return
	 * @throws SaadaException 
	 * @throws SaadaException 
	 */
	private static String getBandQueryPart(String value) throws SaadaException, SaadaException {
		String unit="m", retour = "";
		String regexp = "(" + RegExp.NUMERIC + ")/("+ RegExp.NUMERIC + ")(?:\\[(.*)\\])?";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(value);
		double v0 = -1, v1=-1;
		if( m.find()  ) {
			switch(m.groupCount()) {
			case 2: v0 = Double.parseDouble(m.group(1).trim());
			v1 = Double.parseDouble(m.group(2).trim());
			break;
			case 3: v0 = Double.parseDouble(m.group(1).trim());
			v1 = Double.parseDouble(m.group(2).trim());
			unit = m.group(3).trim();
			break;
			default: QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Band Parameter badly formed");
			}
		}
		v0 = SpectralCoordinate.convertSaada(unit, Database.getSpect_unit(), v0);
		v1 = SpectralCoordinate.convertSaada(unit, Database.getSpect_unit(), v1);
		if( v0 == SaadaConstant.DOUBLE || v1 == SaadaConstant.DOUBLE ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Band parameters cannot be converted from " + unit + " to " + Database.getSpect_unit());
		}
		if( v0 > v1 ) {
			double x = v0;
			v0 = v1;
			v1 = x;
		}
		if( v0 > 0 ) {
			retour = v0 + " BETWEEN x_min_csa  AND  x_min_csa";
		}
		if( v1 > 0 ) {
			retour = v0 + " BETWEEN x_min_csa  AND  x_max_csa OR "
			+ v1 + " BETWEEN x_min_csa  AND  x_max_csa OR "
			+ "(x_min_csa < " + v0 + " and  x_max_csa  >" +  v1 + ")" ;

		}

		return retour;
	}

}
