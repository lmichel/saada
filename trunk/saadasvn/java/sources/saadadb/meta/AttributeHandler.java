package saadadb.meta;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nom.tam.fits.HeaderCard;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.products.VOTableProduct;
import saadadb.sqltable.SQLQuery;
import saadadb.util.ChangeKey;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;

/** * @version $Id$

 * @author michel
 * 01/2010 method toString
 */
public class AttributeHandler implements Serializable{
	static final DecimalFormat exp =  new DecimalFormat("0.00E00");
	static final  DecimalFormat deux = new DecimalFormat("0.000");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int pk = -1;// SQL prim key used to make links with ass_err
	private int classid = -1;
	private char level;
	private String classname = "";
	private int collid = -1;
	private String collname = "";
	private String nameorg = "";
	private String nameattr = "";
	private String type = "";
	private String ucd = "";
	private String utype = "";
	private String vo_dm = "";
	private AttributeHandler ass_error;
	private boolean queriable;
	private String unit = "";
	private String comment = "";
	public String value = "";
	public String format = "";
	// not stored in the DB currently but may be useful to feed some JSON message
	public RangeValue range;

	public AttributeHandler(){}

	/**
	 * Build the attribute handler from a SAVoT field
	 * @param savotParam
	 */
	public AttributeHandler(SavotParam savotParam) {
		String name = savotParam.getName();
		if( name.length() == 0 ) {
			name = savotParam.getId();
		}
		this.setNameorg(name);
		String keyChanged = ChangeKey.changeKey(name);
		String value = savotParam.getValue().trim();
		this.setNameattr(keyChanged);
		this.setType(savotParam.getDataType());
		this.setValue(value);
		this.setUcd(savotParam.getUcd());
		this.setUtype(savotParam.getUtype());
		this.setUnit(savotParam.getUnit());
		if( "".equals(this.getType()) ) {			
			if ( Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Param <" + this.getNameorg() + "> has no declared type: type forced to String");
			this.setType("String");
		}
		/*
		 * Position can be declared as numeric and set with a couple of values
		 */
		else if( !this.getType().equals("boolean") && !value.matches(RegExp.FITS_FLOAT_VAL) && !value.matches(RegExp.FITS_INT_VAL) ) {
			if ( Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Param <" + this.getNameorg() + "> is declared as numeric but contains a none numeric value: " + value + ": type forced to String");
			this.setType("String");
		}
	}
	/**
	 * Build the attribute handler from a SAVoT field
	 * @param card
	 */
	public AttributeHandler(SavotField savotField) {
		String name = savotField.getId();
		if (name == null || name.equals("")) {
			name = savotField.getName();
		}
		this.setNameorg(name);
		String keyChanged = ChangeKey.changeKey(name);
		this.setNameattr(keyChanged);
		this.setType(savotField.getDataType());
		if( this.type == null || this.type.length() == 0 ) {
			this.setType("String");
		}
		this.setUcd(savotField.getUcd());
		this.setUtype(savotField.getUtype());
		this.setUnit(savotField.getUnit());
		this.setComment(VOTableProduct.getStandardDescription(savotField.getDescription()));
		if( (savotField.getArraySize() != null && !savotField.getArraySize().equals(1) && !savotField.getArraySize().equals("")) 
				|| this.getUnit().matches(".*(?i)(d:m:s).*") || this.getUnit().matches(".*(?i)(h:m:s).*")) {
			this.setType("String");
		}
		if (type.equals("unsignedByte")) {
			this.setType("int");
		}
	}
	/**
	 * Build the attribute handler from a FITS header card
	 * @param card
	 */
	public AttributeHandler(HeaderCard card) {
		String strcard = card.toString().trim();
		String regexp = "^" + RegExp.FITS_KEYWORD 
		+ "((?:"  + RegExp.FITS_STR_VAL 
		+ ")|(?:" + RegExp.FITS_BOOLEAN_VAL
		+ ")|(?:" + RegExp.FITS_FLOAT_VAL
		+ ")|(?:" + RegExp.FITS_INT_VAL
		+ "))\\s*"
		+ "((?:"  + RegExp.FITS_COMMENT + ")?)";
		/*
		 * Preliminary test to discard trivial bad cards
		 * save time and avoid unrelevant smessages
		 */
		if( strcard.length() == 0 || strcard.indexOf("=") == -1 ||strcard.startsWith("HISTORY ") ||strcard.startsWith("COMMENT ")) {
			return;
		}
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(strcard);
		if( m.find() && m.groupCount() == 3 ) {
			String key     = m.group(1).trim();
			String value   = m.group(2).trim();
			String comment = m.group(3).replaceFirst("/", "").trim();
			boolean is_not_string = true;
			if( value.startsWith("'") ) {
				is_not_string = false;
			}
			value = value.replaceAll("'", "");
			/*
			 * Penser a traiter le cas de value = "" avec un type num�rique
			 * Implementer les NaN
			 */
			if( key == null || key.equals("") || value == null  ) {
				this.setNameorg("");
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "FITS card <" + strcard + "> can not be interpreted: ignored");
			}
			if( comment == null ) {
				comment = "";
			}
			/*
			 * "'" must be removed to avoid SQL errors
			 */
			this.setNameorg(key.replaceAll("'", " "));
			this.setNameattr(ChangeKey.changeKeyHIERARCH(ChangeKey.changeKey(key)));
			this.setValue(value);
			this.setComment(comment);
			if( is_not_string && value.equals("T") ) {
				this.setType("boolean");
				this.value = "true";
			}
			else if( is_not_string && value.equals("F") ) {
				this.setType("boolean");
				this.value = "false";
			}
			else if( is_not_string && value.matches(RegExp.FITS_FLOAT_VAL) ) {
				this.setType("double");
			}
			else if( is_not_string && value.matches(RegExp.FITS_INT_VAL) ) {
				this.setType("int");
			}
			else  {
				this.setType("String");
			}
		}
		else {
			this.setNameorg("");
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "FITS card <" + strcard + "> can not be interpreted: ignored");  
		}
	}
	//    public static void main(String[] args) {
	//    AttributeHandler a = new AttributeHandler("HIERARCH ESO INS PATH        = '        '   / Optical path used. " );
	//    System.out.println(a.getNameorg());
	//    }    public AttributeHandler(){}

	public AttributeHandler(ResultSet rs) throws FatalException{
		if( rs != null ){
			try{
				this.pk        = rs.getInt("pk");
				this.classid   = rs.getInt("class_id");
				this.level     = rs.getString("level").charAt(0);
				this.classname = parseString(rs.getString("name_class"));
				this.nameattr  = parseString(rs.getString("name_attr"));
				this.nameorg   = parseString(rs.getString("name_origin"));
				this.ucd       = parseString(rs.getString("ucd"));
				this.utype     = parseString(rs.getString("utype"));
				this.vo_dm     = parseString(rs.getString("vo_datamodel"));
				this.queriable = Database.getWrapper().getBooleanValue(rs.getObject("queriable"));
				this.type      = parseString(rs.getString("type_attr"));
				this.unit      = parseString(rs.getString("unit"));
				this.comment   = parseString(rs.getString("comment"));		
				if( this.comment.length() > 128 ) {
					this.comment = this.comment.substring(114) + "(truncated)...";
				}
				this.collname = rs.getString("name_coll").trim();
				this.collid   = rs.getInt("id_collection");
				this.format   = parseString(rs.getString("format"));
			} catch (SQLException e) {
				FatalException.throwNewException(SaadaException.DB_ERROR, e);
			}

		}
	} 

	/**
	 * Build an AH from the column i if a JDBC ResultSetMetaData
	 * @param md
	 * @param i
	 * @throws SQLException 
	 */
	public AttributeHandler(ResultSetMetaData md, int i) throws Exception {
		this.setNameattr(md.getColumnName(i));
		this.setNameorg(this.getNameattr());
		this.setType(Database.getWrapper().getJavaTypeFromSQL(md.getColumnTypeName(i)));
	}

	/**
	 * Build a name acceptable as Java field name or SQL column name from UCD
	 * @param ucd
	 * @return
	 */
	static public String getUCDNickname(String ucd) {
		if( ucd == null || ucd.length() == 0 ) {
			return "no_ucd";
		}
		else {
			return "ucd_" + ucd.replaceAll("[;\\.]","_");
		}
	}
	/**
	 * Build a name acceptable as Java field name or SQL column name from the instance ucd
	 * @return
	 */
	public String getUCDNickname() {
		return getUCDNickname(this.ucd);
	}
	/**
	 * Build a name acceptable as Java field name or SQL column name from UCD
	 * @param uutypecd
	 * @return
	 */
	static public String getUTYPENickname(String utype) {
		if( utype == null || utype.length() == 0 ) {
			return "no_utype";
		}
		/*
		 * PSQL does not support column name > 64. 
		 * If the Utype is longer than 64, we just keep the 64 last char, which are supposed to be
		 * the mots discriminent
		 */
		else if( utype.length() > (63-6) ) {
			return "utype_" +  utype.toLowerCase().substring(utype.length() - 57).replaceAll("[;\\.]","_");
		}
		else {
			return "utype_" + utype.toLowerCase().replaceAll("[;\\.]","_");
		}
	}
	/**
	 * Build a name acceptable as Java field name or SQL column name from the instance utype
	 * @return
	 */
	public String getUTYPENickname() {
		return getUTYPENickname(this.utype);
	}
	/**
	 * @param obj
	 * @return
	 */
	public static double parseDouble(Object obj) {
		try {
			return Double.parseDouble(obj.toString());
		}
		catch(NumberFormatException e) {
			return Double.NaN; 
		}
	}     

	/**
	 * @param obj
	 * @return
	 */
	public static String parseString(String str) {
		if( str == null ) {
			return "";
		}
		else {
			return str.trim();
		}
	}     
	/**
	 * @param classid
	 */
	public void setClassid(int classid){
		this.classid = classid;
	}

	public void setCollid(int collid){
		this.collid = collid;
	}

	public void setClassname(String classname){
		this.classname = classname.trim();
	}

	public void setCollname(String collname){
		this.collname = collname.trim();
	}

	public void setNameorg (String nameorg) {
		if(nameorg != null){
			this.nameorg = nameorg.trim();
		}
	}

	public void setUcd(String ucd){
		if(ucd != null){
			this.ucd = ucd.trim();
		}
	}

	public void setType(String type){
		if(type != null){
			this.type = type.trim();
		}
	}

	public void setUnit(String unit){
		if(unit != null){
			this.unit = unit.trim();
		}
		else {
			this.unit = "";
		}
	}

	public void setComment(String comment){
		if(comment != null){
			this.comment = comment.trim();
		}
		if( this.comment.length() > 128 ) {
			this.comment = this.comment.substring(0,114) + "(truncated)...";
		}
	}

	public void setValue(String value){
		if(value != null){
			this.value = value.trim();
		}
	}

	public String getNameorg(){
		return this.nameorg;
	}        

	public String getNameattr(){
		return this.nameattr;
	}

	public String getValue(){
		return this.value;
	}

	public String getUcd(){
		return this.ucd;
	}

	public String getType(){
		return this.type ;
	}

	public String getUnit(){
		return this.unit;
	}

	public String getComment(){
		return this.comment;
	}

	public int getClassid(){
		return this.classid;
	}

	public int getCollid(){
		return this.collid;
	}

	public String getClassname(){
		return this.classname;
	}

	public String getCollname(){
		return this.collname;
	}


	public String getInsertValues(int subkey) throws FatalException {
		return  "( " + Database.getWrapper().getInsertAutoincrementStatement()
				+ ", '"  + this.level
				+ "', "  + this.classid
				+ ", '"  + this.classname
				+ "', '" + this.nameattr
				+ "', '" + this.type
				+ "', '" + Database.getWrapper().getEscapeQuote(this.nameorg)
				+ "', '" + this.ucd
				+ "', '" + this.utype
				+ "', '" + this.vo_dm
				+ "', "  + "null"
				+ ", "   + Database.getWrapper().getBooleanAsString(this.queriable)
				+ ", '"  + this.unit
				+ "', '" + this.comment.replaceAll("'", "")
				+ "', '" + this.collname 
				+ "',"   + this.collid 
				+ ", '"  + this.format 
				+ "')";
	}

	public static String getInsertStatement() {
		return "(pk, level, class_id, name_class, name_attr, type_attr, name_origin, ucd, utype, vo_datamodel, ass_error, queriable, unit, comment, name_coll, id_collection, format) \n";
	}

	/**
	 * @return Returns the serialVersionUID.
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @return Returns the ass_error.
	 */
	public AttributeHandler getAss_error() {
		return ass_error;
	}

	/**
	 * @param ass_error The ass_error to set.
	 */
	public void setAss_error(AttributeHandler ass_error) {
		this.ass_error = ass_error;
	}


	/**
	 * @param nameattr The nameattr to set.
	 */
	public void setNameattr(String nameattr) {
		this.nameattr = nameattr;
	}

	/**
	 * @return Returns the queriable.
	 */
	public boolean isQueriable() {
		return queriable;
	}

	/**
	 * @param queriable The queriable to set.
	 */
	public void setQueriable(boolean queriable) {
		this.queriable = queriable;
	}

	/**
	 * @return Returns the utype.
	 */
	public String getUtype() {
		return utype;
	}

	/**
	 * @param utype The utype to set.
	 */
	public void setUtype(String utype) {
		this.utype = utype;
	}

	/**
	 * @return Returns the vo_dm.
	 */
	public String getVo_dm() {
		return vo_dm;
	}

	/**
	 * @param vo_dm The vo_dm to set.
	 */
	public void setVo_dm(String vo_dm) {
		this.vo_dm = vo_dm;
	}

	/**
	 * @return Returns the level.
	 */
	public char getLevel() {
		return level;
	}

	/**
	 * @param level The level to set.
	 */
	public void setLevel(char level) {
		this.level = level;
	}

	/**
	 * @return
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return
	 */
	public Object getFormatedValue(Object value) {
		if( value == null ) {
			return "not set";
		}
		String strval = value.toString();
		if( strval.length() == 0 || strval.equals("Infinity") || strval.equals("NaN") || 
				strval.equals("") || strval.equals("NULL") || strval.equals("2147483647") || 
				strval.equals("-2147483648") ||  strval.equals("9223372036854775807") ) {
			return "not set";
		}
		try {
			if( this.format == null || this.format.length() == 0 ) {
				if( this.type.equals("real") || this.type.equals("double") ) {
					double val = Double.parseDouble(value.toString());
					if( val == 0 || (val < -1e-2 || val > 1e-2) ) {
						return deux.format(val);
					}
					else {
						return exp.format(val);
					}
				}
				else {
					return value;
				}
			}
			/*
			 * If the format looks like a URL, an HREF tag is built
			 */
			else if( this.format.startsWith("http://")) {
				return "<A HREF=\"" + this.format.replaceAll("\\$", URLEncoder.encode(value.toString(), "iso-8859-1"))
				+ "\">" + value + "</A>";
			}
			else {
				return this.format.replaceAll("\\$", URLEncoder.encode(value.toString(), "iso-8859-1"));
			}
		} catch (UnsupportedEncodingException e) {
			Messenger.printStackTrace(e);
			return value;
		}

	}

	/**
	 * @return
	 */
	public Object getFormatedValue() {
		return this.getFormatedValue(this.value);
	}
	/*
	 * @param new_type
	 * @return
	 */
	public boolean typeStrongerThan(String new_type) throws FatalException  {
		return JavaTypeUtility.strongerThan(this.type, new_type);
	
	}
	/**
	 * @param new_att
	 * @throws FatalException 
	 */
	public void mergeAttribute(AttributeHandler new_att) throws FatalException {
		String new_type = new_att.getType();
		// System.out.println("@@ MERGE " + this.nameorg + " " + this.type + " <> "+ new_att.getType());
		if( this.unit.length() != 0 && !this.unit.equals(new_att.getUnit())) {
			Messenger.printMsg(Messenger.WARNING, "Merge attribute <" + this.nameorg + "> expressed in \"" + this.unit + "\" with an attribute expressed in \"" + new_att.getUnit() + "\". Saada dataloader doesn't support automatic unit conversion");
		}
		if( this.nameorg.equals(new_att.getNameorg()) ) {
			if( this.type.equals(new_type) ) {
				return;
			}
			else if( this.typeStrongerThan(new_type)){
				return;
			}
			else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "attribute <" + this.nameorg + "> converted from \"" + this.type + "\" to \"" + new_type + "\"");
				this.type = new_type;
			}
		}
	} 
	
	
	/**
	 * Return a range value (enum if less than 10 values) for the attribute
	 * @throws Exception
	 */
	public void setRange() throws Exception {
		if( this.classid == -1 ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "getRange functionnality cannot be used out of a class context");
			return ;
		}
		String table;
		if( this.nameattr.startsWith("_")) {
			table = this.classname; 
		} else {
			table = Database.getCachemeta().getCollectionTableName(this.getCollid(), Database.getCachemeta().getClass(this.classid).getCategory());
		}
		SQLQuery query = new SQLQuery();
		this.range = new RangeValue();
		ResultSet rs = query.run("SELECT DISTINCT " + this.nameattr + " FROM " + table + " LIMIT 11");
		int cpt = 0 ;
		while(rs.next() ) {
			this.range.addToList(rs.getObject(1));
			cpt++;
			if( cpt > 10 ) {
				break;
			}
		}
		if( cpt > 10 ) {
			rs = query.run("SELECT MIN(" + this.nameattr + "), MAX(" + this.nameattr + ") FROM " + table );			
			while(rs.next() ) {
				this.range.setMin(rs.getObject(1));
				this.range.setMax(rs.getObject(2));
			}
		}
		query.close();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.nameorg + "(" + this.nameattr + "," + this.type + "," + this.unit + ","+ this.ucd + ","+ this.utype+ "," + this.comment + ")";
	}
}



