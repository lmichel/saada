package saadadb.vo.request.query;

import java.util.ArrayList;
import java.util.Arrays;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.executor.Query;
import saadadb.query.parser.PositionParser;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Merger;
import saadadb.util.Messenger;
import saadadb.vo.PseudoTableParser;
import saadadb.vo.request.query.pql.PQLBandParser;
import saadadb.vo.request.query.pql.PQLParamParser;
import saadadb.vo.request.query.pql.PQLTimeParser;

/**
 * @author laurent
 * @version 07/2011
 */
public class SSAPQuery extends VOQuery {
	public ArrayList<String> formatTypes = new ArrayList<String>();
	private OidsaadaResultSet resultSet;

	public SSAPQuery() {
		formatAllowedValues = new ArrayList<String>(Arrays.asList(new String[] {
				"application/fits", "application/xml",
				"application/x-votable+xml", "all", "graphic", "metadata",
				"METADATA", "compliant", "xml", "votable", "fits" }));
		mandatoryDataParams = new String[0];
		mandatoryMetaParams = new String[] { "format" };
	}

	@Override
	public ArrayList<Long> getOids() throws Exception {
		ArrayList<Long> retour = new ArrayList<Long>();
		while (this.resultSet.next()) {
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
		Boolean saadattributeAdded = false;
		String cooPos = "", cooSys = "";
		/*
		 * Mandatory param
		 */
		PseudoTableParser ptp = new PseudoTableParser(
				queryParams.get("collection"));
		queryString = "Select SPECTRUM From "
				+ Merger.getMergedArray(ptp.getclasses()) + " In "
				+ Merger.getMergedArray(ptp.getCollections());
		protocolParams.put("category", "SPECTRUM");
		protocolParams.put("class", Merger.getMergedArray(ptp.getclasses()));
		protocolParams.put("collection",
				Merger.getMergedArray(ptp.getCollections()));

		// POSITION parameter
		String value = this.queryParams.get("pos");
		/*
		 * Mandatory param
		 */
		if (value != null) {
			try {
				String pv[] = value.split(";");
				new PositionParser(pv[0]);
				protocolParams.put("pos", value);
				cooPos = value.replaceAll(",", " ");
				;
				if (pv.length > 1 && pv[1].length() > 0) {
					cooSys = pv[1];
					protocolParams.put("coosys", pv[1]);
				} else {
					cooSys = "ICRS";
					protocolParams.put("coosys", "ICRS");
				}
			} catch (Exception e) {
				e.printStackTrace();
				QueryException.throwNewException("ERROR",
						"Unrecognized format for position parameter "
								+ this.queryParams.get("pos"));
			}
		}
		// SIZE parameter
		value = this.queryParams.get("size");
		/*
		 * Mandatory param
		 */
		if (value != null) {
			try {
				size = Double.parseDouble(value);
				protocolParams.put("size", Double.toString(size));
			} catch (NumberFormatException nfe) {
				QueryException.throwNewException("ERROR", nfe);
			}
			if (size <= 0.0 || size > 1) {
				QueryException.throwNewException("OVERFLOW",
						"Angular SIZE should be between 0 and 1");
			}
			if (cooPos.length() == 0) {
				QueryException
						.throwNewException("ERROR",
								"Cone search size given without position: makes no sense");
			}
			queryString += "\nWherePosition{isInCircle(\"" + cooPos + "\","
					+ value + ",J2000," + cooSys + ")}";

		} else if (cooPos.length() > 0) {
			QueryException.throwNewException("ERROR",
					"Position given without size: make no sense");
		}
		// Perform searches that require "WhereAttributeSaada{...}" in the query
		// here

		// BAND parameter
		value = this.queryParams.get("band");
		/*
		 * Mandatory param
		 */
		if (value != null) {
			try {
				if (!saadattributeAdded) {

					queryString += "\nWhereAttributeSaada{"
							+ getBandQueryPart(value);
					saadattributeAdded = true;

				} else {
					queryString += " AND " + getBandQueryPart(value);
				}
			} catch (Exception e) {
				QueryException.throwNewException(
						SaadaException.WRONG_PARAMETER,
						"'band' parameter should have a value: Xmin/Xmax");
			}
		}

		// TIME parameter
		value = this.queryParams.get("time");
		/*
		 * Mandatory param
		 */
		if (value != null) {
			try {
				if (!saadattributeAdded) {
					queryString += "\nWhereAttributeSaada{"
							+ getTimeQueryPart(value);
					saadattributeAdded = true;
				} else {
					queryString += " AND " + getTimeQueryPart(value);
				}
			} catch (Exception e) {
				QueryException.throwNewException(
						SaadaException.WRONG_PARAMETER,
						"'time' parameter should have a value: Xmin/Xmax");
			}
		}
		// min SPECTRAL RESOLVING POWER parameter
		value = this.queryParams.get("specrp");
		if (value != null) {
			if (!saadattributeAdded) {
				queryString += "\nWhereAttributeSaada{"
						+ getSpecRPQueryPart(value);
				saadattributeAdded = true;
			} else {
				queryString += " AND " + getSpecRPQueryPart(value);
			}
		}

		value = this.queryParams.get("timeres");
		if (value != null) {
			// NOT SUPPORTED FOR NOW
			// if (!saadatributeAdded) {
			// queryString += "\nWhereAttributeSaada{"
			// + getTimeResQueryPart(value);
			// saadatributeAdded = true;
			// } else {
			// queryString += " AND " + getTimeResQueryPart(value);
			// }

			Messenger
					.printMsg(
							Messenger.DEBUG,
							"Time resolution parameter is not supported for now : obscore field \"t_resolution\" not present");
		}
		// TARGETNAME parameter
		value = this.queryParams.get("targetname");
		if (value != null) {
			if (!saadattributeAdded) {
				queryString += "\nWhereAttributeSaada{"
						+ getTargetNameQueryPart(value);
				saadattributeAdded = true;
			} else {
				queryString += " AND " + getTargetNameQueryPart(value);
			}
		}
		// PUBLISHER_DID parameter
		value = this.queryParams.get("pubdid");
		if (value != null) {
			if (!saadattributeAdded) {
				queryString += "\nWhereAttributeSaada{"
						+ getPubDIDQueryPart(value);
				saadattributeAdded = true;
			} else {
				queryString += " AND " + getPubDIDQueryPart(value);
			}
		}

		// Close the WhereAttributeSaada part, don't perform searches that
		// require a "WhereAttributeSaada{...}" after this
		if (saadattributeAdded) {
			queryString += "}";
		}
		// ===========================================================================

		value = this.queryParams.get("top");
		if (value != null) {
			try {
				int top = Integer.parseInt(value);
				if (top <= 0) {
					QueryException.throwNewException(
							SaadaException.WRONG_PARAMETER,
							"TOP parameter must be a positive integer");
				}
				queryString += "\nLimit " + top;
			} catch (Exception e) {
				QueryException.throwNewException(
						SaadaException.WRONG_PARAMETER,
						"TOP parameter must be a positive integer");
			}
			Messenger.printMsg(Messenger.WARNING,
					"Time constraint not processed yet");
		}
		/*
		 * OPtional parameters
		 */
		value = this.queryParams.get("format");
		if (value != null) {
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
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,
					"FORMAT parameter should have a value");
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
						// setMetadataRequired(true);
					}
				} else {
					QueryException.throwNewException(
							SaadaException.WRONG_PARAMETER,
							"Unsupported FORMAT value : " + prm);
				}
				indStart = indEnd + 1;
			}
			prm = s.substring(indStart, s.length());
			if (formatAllowedValues.contains(prm.toLowerCase())
					|| formatAllowedValues.contains(prm.toUpperCase())) {
				values.add(prm);
				if (prm.equals("METADATA")) {
					// setMetadataRequired(true);
				}
			} else {
				QueryException.throwNewException(
						SaadaException.WRONG_PARAMETER,
						"Unsupported FORMAT value : " + prm);
			}

		}
		return values;
	}

	/**
	 * Creates a Query part that allow a search by Band range
	 * 
	 * @param value
	 * @return
	 * @throws SaadaException
	 * @throws Exception
	 */
	private String getBandQueryPart(String value) throws Exception {
		String retour;

		PQLParamParser paramParser = new PQLBandParser(value);
		retour = encapsulateQueryPart(paramParser.getQueryPart("em_min",
				"em_max"));
		return retour;
	}

	/**
	 * Creates a Query part that allow a search by time range
	 * 
	 * @param value
	 *            Range format : "MinDate"/"MaxDate" (e.g;
	 *            1993-06-20/2014-06-20)
	 * @return the QueryPart to insert in the Query
	 * @throws Exception
	 */
	private String getTimeQueryPart(String value) throws Exception {

		String retour;
		PQLParamParser paramParser = new PQLTimeParser(value);
		retour = encapsulateQueryPart(paramParser
				.getQueryPart("t_min", "t_max"));

		return retour;
	}

	/**
	 * Creates a query part that allow a search by Spectral resolving power
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String getSpecRPQueryPart(String value) throws Exception {
		String retour;
		PQLParamParser paramParser = new PQLParamParser(value);
		retour = encapsulateQueryPart(paramParser.getQueryPart("em_res_power"));

		return retour;
	}

	/**
	 * creates a query part that allow a search by Time resolution
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String getTimeResQueryPart(String value) throws Exception {
		// DOES NOT WORK BECAUSE OBSCORE FIELD "t_resolution" IS NOT PRESENT
		String retour;
		PQLParamParser paramParser = new PQLParamParser(value);
		retour = encapsulateQueryPart(paramParser.getQueryPart("t_resolution"));

		return retour;
	}

	/**
	 * Creates a query part that allow a search by Target name
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String getTargetNameQueryPart(String value) throws Exception {
		String retour;
		PQLParamParser paramParser = new PQLParamParser(value);
		retour = encapsulateQueryPart(paramParser
				.getExactQueryPart("target_name"));

		return retour;
	}

	/**
	 * Creates a query part that allow a search by Publisher DID
	 * 
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private String getPubDIDQueryPart(String value) throws Exception {
		String retour;
		PQLParamParser paramParser = new PQLParamParser(value);
		retour = encapsulateQueryPart(paramParser
				.getExactQueryPart("obs_publisher_did"));

		return retour;
	}



	/*
	 * //DEBUG Purpose only
	 * 
	 * collection=[VizierData] size=1 pos=185.07541649999996,6.6877777
	 * time=1000-12-12 specrp=30 band=0/100 targetname='p2x263'
	 * pubdid='grandmechantloup' timeres=0 saadaObscore
	 */
}
