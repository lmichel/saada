package saadadb.vo.translator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;
import saadadb.vo.formator.SiaToVOTableFormator;

/**
 * Parser which translates a URL representing a HTTP GET request into a SaadaQL/s String
 *
 * This parser implements the 1.0 version of SIA which can be found at :
 *         http://www.ivoa.net/Documents/WD/SIA/sia-20040524.html
 * The parser is compliant with the specification given above.
 */
public class SiaTranslator extends VOTranslator {

	public int intersectionType;
	public static final int I_COVERS = 1, I_ENCLOSED = 2, I_CENTER = 3, I_OVERLAPS = 4;


	public ArrayList<String> formatTypes = new ArrayList<String>();
	private static String fAV[] = {"image/fits", "image/jpeg", "text/html", "ALL", "GRAPHIC", "METADATA", "GRAPHIC-ALL", "GRAPHIC-jpeg", "GRAPHIC-fits", "jpeg", "fits"};
	public static ArrayList<String> formatAllowedValues = new ArrayList<String>(Arrays.asList(fAV));

	/**
	 * @param req
	 */
	public SiaTranslator(HttpServletRequest req) {
		super(req);
	}


	/**
	 * @param hand_params
	 */
	public SiaTranslator(LinkedHashMap<String, String> hand_params) {
		super(hand_params);
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.VOTranslator#isMetadataRequired()
	 */
	public boolean isMetadataRequired() {
		String value = this.getParam("format");
		if( "metadata".equalsIgnoreCase(value) ) {
			metadataRequired = true;
		}
		else {
			metadataRequired = false;
		}
		return metadataRequired;
	}
	/**
	 * The parsing function
	 * @return a SaadaQL/s String if parsing succeded.
	 */
	public String translate() throws SaadaException {
		double posRa = 1000.0, posDec = 1000.0;
		double sizeRa = -1.0, sizeDec = -1.0;
		String fromString = "From * In * ";
		intersectionType = I_COVERS;
		formatTypes.add("ALL");
		String param, value;
		queryInfos.setCategory(Category.IMAGE);

		if( params != null ) {
			String query;
			if( (query = params.get("query")) != null ) {
				return query;
			}
			value = this.getParam("format");
			if( value != null ) {
				formatTypes = getFormatValues(value);
			}
			value = this.getParam("pos");
			if( value != null ) {
				PositionParser pp = new PositionParser(value);
				posRa = pp.getRa();
				posDec = pp.getDec();
			}
			else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Pos parameter missing");
			}
			value = this.getParam("size");
			if( value != null) {
				String[] sz = value.split(",");
				try {
					switch( sz.length) {
					case 1: sizeRa = sizeDec = Double.parseDouble(sz[0]);
					break;
					case 2: sizeRa = Double.parseDouble(sz[0]);
					sizeDec = Double.parseDouble(sz[1]);
					break;
					default: QueryException.throwNewException(SaadaException.WRONG_PARAMETER,  "size=" + value);

					}
				} catch (NumberFormatException nfe) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,  nfe);
				}
			this.queryInfos.setSize(sizeRa, sizeDec);
			if (sizeRa <= 0.0) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"RA angular SIZE should be greater than 0");
				}
				if (sizeDec <= 0.0) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Dec angular SIZE should be greater than 0");
				}
			}
			else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Pos parameter missing");
			}
			value = this.getParam("intersect");
			if( value == null ) {
				intersectionType = I_OVERLAPS;
			} else if (value.equals("COVERS")) {
				intersectionType = I_COVERS;
			} else if (value.equals("ENCLOSED")) {
				intersectionType = I_ENCLOSED;
			} else if (value.equals("CENTER")) {
				intersectionType = I_CENTER;
			} else if (value.equals("OVERLAPS")) {
				intersectionType = I_OVERLAPS;
			} else {
				QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"This type of intersection is unknown");
			}
			value = this.getParam("mode");
			if( "cutout".equalsIgnoreCase(value) ) {
				this.queryInfos.setCutoutMode();
			}
			else {
				this.queryInfos.setPointedMode();
			}

			value = params.get("collection");
			if (value == null || value.length() == 0) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"'collection' parameter should have a value : collection=coll1[class1,class2]");
			}
			fromString = getFromQueryPart(value);


		}
		if (isMetadataRequired()) {
			return "";
		}

		if (posRa == 1000.0) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"There must be a POS argument in the URL");
		}
		if (sizeRa == -1.0) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"There must be a SIZE argument in the URL");
		}

		// Now we build the SaadaQL query
		saadaString = "Select IMAGE " + fromString + " WhereAttributeSaada{";

		switch (intersectionType) {
		case I_CENTER:
			saadaString += Database.getWrapper().getImageCenterConstraint("", posRa, posDec, sizeRa, sizeDec);
			break;
		case I_ENCLOSED:
			saadaString += Database.getWrapper().getImageEnclosedConstraint("", posRa, posDec, sizeRa);
			break;
		case I_COVERS:
			saadaString += Database.getWrapper().getImageCoverConstraint("", posRa, posDec, sizeRa, sizeDec);
			break;
			/*
			 * overlaps mode taken by default
			 */
		default:
			saadaString += Database.getWrapper().getImageOverlapConstraint("", posRa, posDec, sizeRa);
			break;

		}
		saadaString += "}";
		saadaString += "\nLimit 1000";
		queryInfos.setSaadaqlQuery(saadaString );
		return saadaString;
	}


	/**
	 * @param s
	 * @return
	 * @throws SaadaException
	 */
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
						setMetadataRequired(true);
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
					setMetadataRequired(true);
				}
			} else {
				QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Unsupported FORMAT value : " + prm);
			}
		}
		return values;
	}


	public static void main(String[] args) throws FatalException {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		LinkedHashMap<String, String> hand_params = new LinkedHashMap<String, String>();
		Messenger.debug_mode = false;
		hand_params.put("size", "0.07");

		hand_params.put("POS", "04:08:00.76+43:00:34.6");
		hand_params.put("collection", "[any]");
		//hand_params.put("FORMAT", "METADATA");

		try {
			String rf = (new SiaToVOTableFormator("SIA EPIC Charac")).processVOQuery(new SiaTranslator(hand_params));
			BufferedReader br = new BufferedReader(new FileReader(rf));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/michel/Desktop/sia_charac.xml"));
			String str;
			while( (str = br.readLine()) != null  ) {
				System.out.println(str.replaceAll("<TD", "\n<TD"));
				bw.write(str);
			}
			bw.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
//		hand_params = new LinkedHashMap<String, String>();
//		Messenger.debug_mode = false;
//		hand_params.put("size", "0.7");
//
//		hand_params.put("POS", "Abell 2690");
//		hand_params.put("collection", "[XMM_Data]");
//		hand_params.put("FORMAT", "METADATA");
//		hand_params.put("INTERSECT", "COVER");
//
//		try {
//			String rf = (new SiaToVOTableFormator("SIA EPIC Charac")).processVOQuery(new SiaTranslator(hand_params));
//			BufferedReader br = new BufferedReader(new FileReader(rf));
//			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/michel/Desktop/sia_charac_meta.xml"));
//			String str;
//			while( (str = br.readLine()) != null  ) {
//				System.out.println(str.replaceAll("<TD", "\n<TD"));
//				bw.write(str);
//			}
//			bw.close();
//			br.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}


}


