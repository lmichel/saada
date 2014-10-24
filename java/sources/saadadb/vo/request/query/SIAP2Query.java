/**
 * 
 */
package saadadb.vo.request.query;

import java.util.ArrayList;
import java.util.Arrays;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.executor.Query;
import saadadb.query.parser.PositionParser;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Merger;
import saadadb.vo.PseudoTableParser;
import saadadb.vo.request.query.pql.PQLParamParser;
import saadadb.vo.request.query.pql.PQLTimeParser;

/**
 * Translate SIAP parameters in a SAADAQL query and run it
 * and build the response files
 * @author laurent
 * 
 * @version 06/2011
 *
 */
public class SIAP2Query extends VOQuery {
	public static final int I_COVERS = 1, I_ENCLOSED = 2, I_CENTER = 3, I_OVERLAPS = 4;
	private int intersect;
	public static final int MODE_CUTOUT = 1, MODE_POINTED = 2;
	public static final int METADATA = 1, QUERY = 2;
	public ArrayList<String> formatTypes = new ArrayList<String>();
	private OidsaadaResultSet resultSet;
	private String queryString;

	public SIAP2Query() {
		formatAllowedValues = new ArrayList<String>(Arrays.asList(new String[] { "image/fits", "image/jpeg", "text/html", "ALL", "GRAPHIC",
				"METADATA", "GRAPHIC-ALL", "GRAPHIC-jpeg", "GRAPHIC-fits", "jpeg", "fits" }));
		mandatoryDataParams = new String[] { "pos", "size", "collection" };
		mandatoryMetaParams = new String[] { "format" };
		formatTypes.add("ALL");
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#setDM(saadadb.meta.VOResource)
	 */
	public void setDM(VOResource dm) {
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#buildQuery()
	 */
	@SuppressWarnings("static-access")
	public void buildQuery() throws Exception {
		PQLParamParser paramParser;
		double posRa = 1000.0, posDec = 1000.0;
		double sizeRa = -1.0, sizeDec = -1.0;
		String value;
		/*
		 * Mandatory params
		 */
		PseudoTableParser ptp = new PseudoTableParser(queryParams.get("collection"));
		queryString = "Select IMAGE From " + Merger.getMergedArray(ptp.getclasses()) + " In " + Merger.getMergedArray(ptp.getCollections())
				+ " WhereAttributeSaada{";
		protocolParams.put("category", "IMAGE");
		protocolParams.put("class", Merger.getMergedArray(ptp.getclasses()));
		protocolParams.put("collection", Merger.getMergedArray(ptp.getCollections()));

		// position parameter
		try {
			PositionParser pp = new PositionParser(this.queryParams.get("pos"));
			posRa = pp.getRa();
			posDec = pp.getDec();
			protocolParams.put("ra", Double.toString(posRa));
			protocolParams.put("dec", Double.toString(posDec));
		} catch (Exception e) {
			QueryException.throwNewException("ERROR", "Unrecognized format for position parameter" + this.queryParams.get("pos"));
		}

		String[] sz = this.queryParams.get("size").split(",");
		try {
			switch (sz.length) {
			case 1:
				sizeRa = sizeDec = Double.parseDouble(sz[0]);
				break;
			case 2:
				sizeRa = Double.parseDouble(sz[0]);
				sizeDec = Double.parseDouble(sz[1]);
				break;
			default:
				QueryException.throwNewException("ERROR", "Unrecognized format for parameter size" + this.queryParams.get("size"));
				protocolParams.put("size_ra", Double.toString(sizeRa));
				protocolParams.put("size_dec", Double.toString(sizeDec));
			}
		} catch (NumberFormatException nfe) {
			QueryException.throwNewException("ERROR", nfe);
		}
		if (sizeRa <= 0.0 || sizeRa > 1) {
			QueryException.throwNewException("OVERFLOW", "RA angular SIZE should be between 0 and 1");
		}
		if (sizeDec <= 0.0 || sizeDec > 1) {
			QueryException.throwNewException("OVERFLOW", "Dec angular SIZE should be between 0 and 1");
		}
		/*
		 * OPtional parameters
		 */
		value = this.queryParams.get("format");
		if (value != null) {
			formatTypes = getFormatValues(value);
		} else {
			protocolParams.put("format", "ALL");
		}

		value = this.queryParams.get("intersect");
		if (value == null) {
			intersect = I_OVERLAPS;
			protocolParams.put("intersect", "OVERLAPS");
		} else {
			protocolParams.put("intersect", value);
			if (value.equals("COVERS")) {
				intersect = I_COVERS;
			} else if (value.equals("ENCLOSED")) {
				intersect = I_ENCLOSED;
			} else if (value.equals("CENTER")) {
				intersect = I_CENTER;
			} else if (value.equals("OVERLAPS")) {
				intersect = I_OVERLAPS;
			} else {
				QueryException.throwNewException("ERROR", "unknown intersection  type " + intersect);
			}
		}
		switch (intersect) {
		case I_CENTER:
			queryString += Database.getWrapper().getImageCenterConstraint("", posRa, posDec, sizeRa, sizeDec);
			break;
		case I_ENCLOSED:
			queryString += Database.getWrapper().getImageEnclosedConstraint("", posRa, posDec, sizeRa);
			break;
		case I_COVERS:
			queryString += Database.getWrapper().getImageCoverConstraint("", posRa, posDec, sizeRa, sizeDec);
			break;
		/*
		 * overlaps mode taken by default
		 */
		default:
			queryString += Database.getWrapper().getImageOverlapConstraint("", posRa, posDec, sizeRa);
			break;
		}
		value = this.queryParams.get("mode");
		if ("cutout".equalsIgnoreCase(value)) {
			protocolParams.put("mode", "cutout");
		} else {
			protocolParams.put("mode", "pointed");
		}

		value = this.queryParams.get("band");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND " + encapsulateQueryPart(paramParser.getQueryPart("em_min", "em_max"));
		}

		value = this.queryParams.get("time");
		if (value != null) {
			paramParser = new PQLTimeParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getQueryPart("t_min", "t_max"));
		}

		value = this.queryParams.get("pol");
		if (value != null) {
//TODO pol can be multi valued. Not sure if queryParams can handle it
		}

		value = this.queryParams.get("fov");
		if (value != null) {
paramParser = new PQLParamParser(value);
queryString +=" AND "+encapsulateQueryPart(paramParser.getQueryPart("s_fov", "s_fov"));
		}

		value = this.queryParams.get("spatres");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString +=" AND "+encapsulateQueryPart(paramParser.getQueryPart("s_resolution", "s_resolution,"));
		}

		value = this.queryParams.get("exptime");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString +=" AND "+encapsulateQueryPart(paramParser.getQueryPart("t_exptime", "t_exptime,"));
		}

		value = this.queryParams.get("id");
		if (value != null) {
paramParser = new PQLParamParser(value);
queryString += " AND "+encapsulateQueryPart(paramParser.getExactQueryPart("obs_publisher_did"));
		}

		value = this.queryParams.get("facility");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getExactQueryPart("facility_name"));
		}

		value = this.queryParams.get("instrument");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getExactQueryPart("instrument_name"));
		}

		value = this.queryParams.get("dptype");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getExactQueryPart("dataproduct_type"));
		}

		value = this.queryParams.get("calib");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getQueryPart("calib_level","calib_level"));
		}

		value = this.queryParams.get("target");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getExactQueryPart("target_name"));
		}

		value = this.queryParams.get("timeres");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getQueryPart("t_resolution","t_resolution"));
		}

		value = this.queryParams.get("specrp");
		if (value != null) {
			paramParser = new PQLParamParser(value);
			queryString += " AND "+encapsulateQueryPart(paramParser.getQueryPart("em_res_power","em_res_power"));
		}

		queryString += "}";
		value = this.queryParams.get("limit");
		if (value != null) {
			// throws Exception in case of failure
			Integer.parseInt(value);
			queryString += "\nLimit " + value;
		}
		protocolParams.put("query", queryString);
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#runQuery()
	 */
	public void runQuery() throws QueryException {
		resultSet = (new Query()).runBasicQuery(queryString);
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getOids()
	 */
	public ArrayList<Long> getOids() throws Exception {
		ArrayList<Long> retour = new ArrayList<Long>();
		while (this.resultSet.next()) {
			retour.add(this.resultSet.getOId());
		}
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getSaadaInstanceResultSet()
	 */
	public SaadaInstanceResultSet getSaadaInstanceResultSet() {
		return this.resultSet;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#close()
	 */
	public void close() throws QueryException {
		// this.resultSet.close();
		this.resultSet = null;

	}

	private ArrayList<String> getFormatValues(String s) throws SaadaException {
		ArrayList<String> values = new ArrayList<String>();

		if (s == null) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, " FORMAT parameter should have a value");
		} else {
			int indStart = 0;
			int indEnd, indComma = 0;
			String prm;
			while ((indEnd = s.indexOf(',', indStart + 1)) != -1) {
				indComma++;
				prm = s.substring(indStart, indEnd);
				if (formatAllowedValues.contains(prm)) {
					values.add(prm);
					if (prm.equals("METADATA")) {
						// data = METADATA;
					}
				} else {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unsupported FORMAT value : " + prm);
				}
				indStart = indEnd + 1;
			}
			prm = s.substring(indStart, s.length());
			if (formatAllowedValues.contains(prm)) {
				values.add(prm);
				if (prm.equals("METADATA")) {
					// data = METADATA;
				}
			} else {
				QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unsupported FORMAT value : " + prm);
			}
		}
		return values;
	}
}
