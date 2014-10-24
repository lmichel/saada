package saadadb.query.parser;

import saadadb.collection.Category;
import saadadb.vocabulary.RegExp;

/**
 * @author F.X. Pineau
 */
public final class SaadaQLRegex {

	public static final String ReqWS  = "\\s+";  // Required whitepace  
	public static final String FacWS  = "\\s*";  // Facultatif whitespace  
	public static final String ArgSep = ",";     // Arguments separator
	//rejete [ + | - ] . [ e... ] et [+ | - ] 0[0-9]+
	public static final String   NumericValue = "[+\\-]?(?:\\d*\\.)?\\d+(?:[Ee][+\\-]?\\d*)?"; //[Ee]<=>(?i)e
	public static final String       IntValue = "[+\\-]?[1-9]\\d*";
	public static final String    PosIntValue = "\\+?[1-9]\\d*";
	public static final String      TextValue = "[^\\']+";
	public static final String   UCD1p        = "(?:(?:[\\w\\-]+\\.)*(?:[\\w\\-]+);)*(?:(?:[\\w\\-]+\\.)*(?:[\\w\\-]+))";
//	public static final String   UType        = "(?:\\w+\\.)*\\w+";
	public static final String   UType        = RegExp.UTYPE;
	public static final String   UNIT         = "[+\\-/\\.\\[\\]\\w]+";
	public static final String   SAADA_OPs    = computeFromTable(Operator.RegexTab).replace("[","\\[").replace("]","\\]");;


	public static final String   ALL_KEYWORD = "*";
	public static final String   ALL    = "\\*";
	
	public static       String   CATEGORIES = Category.buildRegExp();
	public static final String   CLASS = "(?:\\D|_)\\w*"; // Begin with a lettre or a _ an can contain lettres digit and _
	public static final String   COLL  = "\\D\\w*"; // Begin with a lettre an can contain lettres digit and _
	public static final String   ATTRIBUTE = "\\w+";
	public static final String   QUAL      = "\\w+";
	
	
	// Syntaxe: SELECT category FROM * | class1 [,...] IN * | coll [,...]
	public static final String SELECT = "Select";//"(?i)(?:SELECT)";
	public static final String FROM = "From";//"(?i)(?:FROM)";
	public static final String IN = "In";//(?i)(?:IN)";

	// Syntaxe: ORDER BY attr [ ASC | DESC ]
	public static final String ORDER_BY = "Order By";//"(?i)(?:ORDER\\s+BY)";
	public static final String ORDER_BY_OPTION = "(?:\\s+(?:(?:desc)|(?:asc)))?";//"(?:\\s+(?i)(?:(?:DESC)|(?:ASC)))?";

	public static final String WHERE_CLAUSE_LD = "\\{"; // Left Delimiter
	public static final String WHERE_CLAUSE_RD = "\\}"; // Right Delimiter
	public static final String ASS_CLAUSE_LD = "\\{"; // Left Delimiter
	public static final String ASS_CLAUSE_RD = "\\}"; // Right Delimiter

	//Syntaxe: WhereUCD { \\[ucd\\] op 'text'|number|(number,number) [\\[unit\\]] [ and|or ...] }
	public static final String WHERE_UCD        = "WhereUCD"           ;
	public static final String WHERE_UType      = "WhereUType"         ;
	public static final String WHERE_DM         = "WhereDM"         ;
	public static final String WHERE_ATTR_SAADA = "WhereAttributeSaada";
	public static final String WHERE_ATTR_CLASS = "WhereAttributeClass";
	public static final String WHERE_POSITION   = "WherePosition"      ;
	public static final String WHERE_RELATION   = "WhereRelation"      ;
	public static final String ATTR_FILTER      = "AttributeFilter"    ;
	
	public static final String MATCH_PATTERN    = "matchPattern"      ;
	
	public static final String LIMIT   = "(?i)(?:LIMIT)";

	public static final String[] POS_QUERY_KEY_TAB  = {"isInBox","isInCircle","isInRegion"};
	public static        String  POS_QUERY_KEYS;
	
	public static final String COORD_EQU = "(?i)(?:(?:J1950)|(?:J2000)|(?:null)|(?:-))";
	public static final String COORD_SYS = "(?i)(?:(?:FK4)|(?:FK5)|(?:ICRS)|(?:GALACTIC)|(?:ECLIPTIC))";

	public static final String ASS_OBJ_ATT_CLASS = "AssObjAttClass";
	public static final String ASS_OBJ_ATT_SAADA = "AssObjAttSaada";
	public static final String ASS_OBJ_CLASS     = "AssObjClass"   ;
	public static final String ASS_UCD           = "AssUCD"        ;
	public static final String ASS_UType         = "AssUType"      ;
	public static final String ASS_DM            = "AssDM"      ;
	public static final String CARDINALITY       = "Cardinality"   ;
	public static final String QUALIFIER         = "Qualifier"     ;
	
	public static final String UcdSyntax       = "\\["+UCD1p+"\\]";
	public static final String UcdSyntaxCaptG  = "\\[("+UCD1p+")\\]";
	
	public static final String UTypeSyntax       = "\\["+UType+"\\]";
	public static final String UTypeSyntaxCaptG  = "\\[("+UType+")\\]";
	
	public static final String ValueSyntax     = "(?:(?:\\'"+TextValue+"\\')|(?:"+NumericValue+")|(?:\\("+FacWS+NumericValue+FacWS+ArgSep+FacWS+NumericValue+FacWS+"\\)))";
	public static final String UnitSyntax      = "(?:\\[\\s*"+UNIT+"\\s*\\])?";  // Pourrait peut etre etre affine
	public static final String UnitSyntaxCaptG = "(?:\\[(\\s*"+UNIT+"\\s*)\\])?"; 
	public static final String OpLogSyntax     = "(?i)(?:(?:and)|(?:or))";

	public static final String OpCompatiblesValue = "(:?(?:"+NumericValue+")|(?:\\("+FacWS+NumericValue+FacWS+ArgSep+FacWS+NumericValue+FacWS+"\\)))";
	
	public static final String ConstraintSyntax = UcdSyntax + FacWS +  SAADA_OPs + FacWS + "(?:\\*)?"  + FacWS + ValueSyntax + FacWS + UnitSyntax; // const = constraint
	public static final String InUCDStat      = "(?:" + ConstraintSyntax + ReqWS + OpLogSyntax + ReqWS + ")*(" + ConstraintSyntax + ")";
	
	public static final String UtypeConstraintSyntax = UTypeSyntax + FacWS +  SAADA_OPs + FacWS + "(?:\\*)?"  + FacWS + ValueSyntax + FacWS + UnitSyntax; // const = constraint
	public static final String InTYPEStat      = "(?:" + UtypeConstraintSyntax + ReqWS + OpLogSyntax + ReqWS + ")*(" + UtypeConstraintSyntax + ")";
	
	//private static final String COORD_DECIMAL = "([+\\-]?\\d+(?:\\.\\d*)?)[,:;\\s\\-]+(\\d+(\\.\\d*)?)";

	@SuppressWarnings("unused")
	private static final SaadaQLRegex sqlr = new SaadaQLRegex();

	private SaadaQLRegex() {
		//SAADA_OPs = computeFromTable(Operator.OpTab).replace("[","\\[").replace("]","\\]");
		//computeSaadaOPs();
		computePosQuerKeys();
	}

	/*private static final void computeSaadaOPs(){	 
		System.out.println("COUCOU: "+SAADA_OPs);
	}*/
	private static void computePosQuerKeys(){
		POS_QUERY_KEYS = computeFromTable(POS_QUERY_KEY_TAB);
	}
	private static final String computeFromTable(String[] st) {
		StringBuffer strBuf = new StringBuffer("(?:");
		for(String key:st){
			strBuf.append("(?:").append(key).append(")|");
		}
		return strBuf.substring(0,strBuf.length()-1)+")";
	} 
	
}

