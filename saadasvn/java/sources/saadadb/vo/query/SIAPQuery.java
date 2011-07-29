/**
 * 
 */
package saadadb.vo.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.executor.Query;
import saadadb.query.parser.PositionParser;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.util.Merger;
import saadadb.vo.PseudoTableParser;
import saadadb.vo.formator.version;

/**
 * @author laurentmichel
 *@version $Id$
 */
public class SIAPQuery extends VOQuery {
	public static final int I_COVERS = 1, I_ENCLOSED = 2, I_CENTER = 3, I_OVERLAPS = 4;
	private int intersect;
	public static final int MODE_CUTOUT=1, MODE_POINTED=2;
	private int mode;
	public static final int METADATA=1, QUERY=2;
	public ArrayList<String> formatTypes = new ArrayList<String>();
	private int data;
	private OidsaadaResultSet resultSet;


	private Map<String, String> params;
	private String queryString;

	SIAPQuery() {
		formatAllowedValues = new ArrayList<String>( 
				Arrays.asList(new String[]{"image/fits", "image/jpeg", "text/html", "ALL", "GRAPHIC", "METADATA", "GRAPHIC-ALL", "GRAPHIC-jpeg", "GRAPHIC-fits", "jpeg", "fits"}));
		mandatoryParams = new String[]{"pos", "size", "collection"};
		formatTypes.add("ALL");
		mode = MODE_POINTED;
		data = QUERY;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#setDM(saadadb.meta.VOResource)
	 */
	public void setDM(VOResource dm) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#buildQuery()
	 */
	public void buildQuery()  throws Exception{
		double posRa = 1000.0, posDec = 1000.0;
		double sizeRa = -1.0, sizeDec = -1.0;
		String value;
		/*
		 * Mandatory params
		 */
		PseudoTableParser ptp = new PseudoTableParser(params.get("collection"));
		queryString = "Select IMAGE From " + Merger.getMergedArray(ptp.getclasses()) 
		+ " In "  + Merger.getMergedArray(ptp.getCollections()) + " WhereAttributeSaada{";
		PositionParser pp = new PositionParser(this.params.get("pos"));

		posRa = pp.getRa(); 
		posDec = pp.getDec();

		String[] sz = this.params.get("size").split(",");
		try {
			switch( sz.length) {
			case 1: sizeRa = sizeDec = Double.parseDouble(sz[0]);
			break;
			case 2: sizeRa = Double.parseDouble(sz[0]);
			sizeDec = Double.parseDouble(sz[1]);
			break;
			default: QueryException.throwNewException(SaadaException.WRONG_PARAMETER,  "size=" + this.params.get("size"));

			}
		} catch (NumberFormatException nfe) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,  nfe);
		}
		if (sizeRa <= 0.0) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"RA angular SIZE should be greater than 0");
		}
		if (sizeDec <= 0.0) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Dec angular SIZE should be greater than 0");
		}
		else {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Pos parameter missing");
		}
		/*
		 * OPtional parameters
		 */
		value = this.params.get("format");
		if( value != null ) {
			formatTypes = getFormatValues(value);
		}

		value = this.params.get("intersect");
		if( value == null ) {
			intersect = I_OVERLAPS;
		} else if (value.equals("COVERS")) {
			intersect = I_COVERS;
		} else if (value.equals("ENCLOSED")) {
			intersect = I_ENCLOSED;
		} else if (value.equals("CENTER")) {
			intersect = I_CENTER;
		} else if (value.equals("OVERLAPS")) {
			intersect = I_OVERLAPS;
		} else {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"unknown intersection  type " + intersect );
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
		value = this.params.get("mode");
		if( "cutout".equalsIgnoreCase(value) ) {
			this.mode = MODE_CUTOUT;
		}
		else {
			this.mode = MODE_POINTED;
		}
		queryString += "}";
		value = this.params.get("limit");
		if( value != null ) {
			// throws Exception in case of failure
			Integer.parseInt(value);
			queryString += "\nLimit " + value;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#runQuery()
	 */
	public void runQuery() throws QueryException {
		resultSet = (new Query().runBasicQuery(queryString));
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#initReport(java.lang.String)
	 */
	public void initReport(String filename) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#writeComments(java.util.Map)
	 */
	public void writeComments(Map<String, String> comments) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#writeSTC()
	 */
	public void writeSTC() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#writeInputs()
	 */
	public void writeInputs() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#writeFields()
	 */
	public void writeFields() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#writeData()
	 */
	public void writeData() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.query.VOQuery#finish()
	 */
	public void finish() {
		// TODO Auto-generated method stub

	}

	private ArrayList<String> getFormatValues(String s) throws SaadaException {
		ArrayList<String> values = new ArrayList<String>();

		if (s == null) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER," FORMAT parameter should have a value");
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
						data = METADATA;
					}
				} else {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Unsupported FORMAT value : " + prm);
				}
				indStart = indEnd + 1;
			}
			prm = s.substring(indStart, s.length());
			if (formatAllowedValues.contains(prm)) {
				values.add(prm);
				if (prm.equals("METADATA")) {
					data = METADATA;
				}
			} else {
				QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Unsupported FORMAT value : " + prm);
			}
		}
		return values;
	}


}
