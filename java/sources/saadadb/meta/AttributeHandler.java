package saadadb.meta;

import hecds.wcs.descriptors.CardDescriptor;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import nom.tam.fits.HeaderCard;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.Table_Saada_Metacat;
import saadadb.util.ChangeKey;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.RegExp;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;

/**
 * @author michel
 * @version $Id$
 * 
 * 02/1014: constantValue Flag used by  {@link ColumnMapping}
 * 03/1014: Accessors give the existence of a valid value for the field unit value, ucd and utype
 * 02/2015: Check the consistency of the value set in the object
 */
public class AttributeHandler implements Serializable , Cloneable, CardDescriptor, Comparable{
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
	/** Used for associate error or to implement ref in VOTables */
	private AttributeHandler associateAttribute;
	private boolean queriable;
	private String unit = "";
	private String comment = "";
	public String value = "";
	public double numValue = SaadaConstant.DOUBLE;
	public String format = "";
	// not stored in the DB currently but may be useful to feed some JSON message
	public RangeValue range;
	/**
	 * Used by {@link ColumnMapping} for switch from mapped to constant values
	 */
	public boolean constantValue = false; 
	public AttributeHandler(){}

	public static final Pattern NUMERIC_PATTERN	= Pattern.compile("^" + RegExp.FITS_KEYWORD 
			+ "((?:"  + RegExp.FITS_STR_VAL 
			+ ")|(?:" + RegExp.FITS_BOOLEAN_VAL
			+ ")|(?:" + RegExp.FITS_FLOAT_VAL
			+ ")|(?:" + RegExp.FITS_INT_VAL
			+ "))\\s*"
			+ "((?:"  + RegExp.FITS_COMMENT + ")?)");

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
		else if( !this.getType().equals("boolean") && !this.getType().equals("String") && !value.matches(RegExp.FITS_FLOAT_VAL) && !value.matches(RegExp.FITS_INT_VAL) ) {
			if ( Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Param <" + this.getNameorg() + "> is declared as numeric ( " + this.getType() + ") but contains a none numeric value: " + value + ": type forced to String");
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
		this.setComment(VOTableDataFile.getStandardDescription(savotField.getDescription()));
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
		/*
		 * Preliminary test to discard trivial bad cards
		 * save time and avoid un-relevant messages
		 */
		if( strcard.length() == 0 || strcard.indexOf("=") == -1 ||strcard.startsWith("HISTORY ") ||strcard.startsWith("COMMENT ")) {
			return;
		}
		String key =  card.getKey();
		/*
		 * Extract manulay HIERACH KW which bloack seprated lists
		 */
		if( key.startsWith("HIERARCH") ) {
			String s =card.toString();
			int pos = s.indexOf("=");
			if( pos == -1 ) {
				return;					
			}else {
				key = card.toString().substring(0 , pos).trim();
			}
		}
		/*
		 * Value of WAT cards must be kept not trimmed because they can be
		 * merged to build longer strings/ fits.tam does the trim by default, so do it by hand
		 */
		if( key.startsWith("WAT")) {
			this.parseIrafCard(card);
		} else {
			String value   =  card.getValue();
			String comment =  card.getComment();
			/*
			 * value can be null for cards with long comment?
			 * e.g.  System.out.println("---------------\n" + card + "\n-----------------------\n");

				ORIGIN  = 'ADC     '           / This spectrum is part of catalog 3114,         
                        = ' generated at Astronomical Data Center, NASA/GSFC' /               
			 */
			if( value == null ){
				value = "";
			}
			boolean isNotString = true;
			if( value.startsWith("'") ) {
				isNotString = false;
			}
			value = value.replaceAll("'", "");
			/*
			 * TODO Penser a traiter le cas de value = "" avec un type numerique
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
			this.setComment(comment);
			if( isNotString && value.equals("T") ) {
				this.setType("boolean");
				this.value = "true";
			} else if( isNotString && value.equals("F") ) {
				this.setType("boolean");
				this.value = "false";
			} else if( isNotString && value.matches(RegExp.FITS_FLOAT_VAL) ) {
				this.setType("double");
				this.setValue(value);
			} else if( isNotString && value.matches(RegExp.FITS_INT_VAL) ) {
				this.setType("int");
				this.setValue(value);
			} else  {
				this.setType("String");
				this.setValue(value);
			}
		}
	}

	/**
	 * @param rs
	 * @throws FatalException
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone(){  
		try{  
			return super.clone();  
		}catch(Exception e){ 
			return null; 
		}
	}	 

	/**
	 * Parse by hand WAT card because those card's values must not be trimmed 
	 * in order to keep consistent when they are merged 
	 * @param stringCard
	 */
	private void  parseIrafCard(final HeaderCard card){
		String stringCard = card.toString();
		final int id1 = stringCard.indexOf('=');
		final int lgth = stringCard.length();
		if( id1 > 0 ) {
			String key = stringCard.substring(0, id1).trim();
			this.setNameorg(key.replaceAll("'", " "));
			this.setNameattr(ChangeKey.changeKeyHIERARCH(ChangeKey.changeKey(key)));
			int id2 = stringCard.lastIndexOf('/');
			if( id2 == -1){
				id2 = lgth ;
			}
			if( id1 <= (lgth-2)) {
				String desc;
				this.value = stringCard.substring(id1+1, id2).replaceAll("'", "");
				if( this.value.startsWith(" ")) {
					this.value = this.value.substring(1);
				}
				if( id2 <= (lgth-2)) {
					desc = stringCard.substring(id2+1).trim();
				} else {
					desc = "";
				}
				this.setComment(desc);				
				this.setType("String");
				return;
			}
		}
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
		this.classname =  (classname == null)?null: classname.trim();
	}

	public void setCollname(String collname){
		this.collname = (collname == null)?null: collname.trim();
	}

	public void setNameorg (String nameorg) {
		if(nameorg != null){
			this.nameorg =  (nameorg == null)?null: nameorg.trim();
		}
	}

	public void setUcd(String ucd){
		if(ucd != null){
			this.ucd =  (ucd == null)?null: ucd.trim();
		}
	}

	public void setType(String type){
		if(type != null){
			this.type = type.trim();
			if( "char".equals(this.type) ) {
				this.type = "String";
			}

		}
	}

	public void setUnit(String unit){
		if(unit != null){
			this.unit = unit.trim().replaceAll("\"",  "").replace("'",  "") ;
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

	public void setValue(double value){
		this.numValue = value;
		this.value = String.valueOf(value);
	}

	/**
	 * Check that the value represented by "value" is consistent with the type
	 * Set as {@link SaadaConstant.STRING} otherwise
	 * TODO This operation slow down the data loading, it should be made optional
	 * @param value
	 */
	public final void setValue(String value) {
		String tvalue = ( value == null )?"": value;
		if( this.type.equals("float")) {
			try { 
				this.numValue = Float.parseFloat(tvalue);
				this.value =  tvalue;
			} catch(Exception e){
				this.value =   SaadaConstant.STRING;
				this.numValue = SaadaConstant.DOUBLE;
			}
		} else if( this.type.equals("double")) {
			try { 
				this.numValue = Double.parseDouble(tvalue);
				this.value =  tvalue;
				/*
				 * PSQL does not support double < 1e-307
				 */
				if( (this.numValue > 0 && this.numValue < 1E-100) || (this.numValue < 0 && this.numValue > -1E-100) ) {
					Messenger.debug_mode = true;
					this.numValue = 0;
					this.value = "0";
				}

			} catch(Exception e){
				this.value =   SaadaConstant.STRING;
				this.numValue = SaadaConstant.DOUBLE;
			}
		}  else if( this.type.equals("short")) {
			try { 
				this.numValue = Short.parseShort(tvalue);
				this.value =  tvalue;
			} catch(Exception e){
				this.value =   SaadaConstant.STRING;
				this.numValue = SaadaConstant.DOUBLE;
			}
		}  else if( this.type.equals("int")) {
			try { 
				this.numValue = Integer.parseInt(tvalue);
				this.value =  tvalue;
			} catch(Exception e){
				this.value =   SaadaConstant.STRING;
				this.numValue = SaadaConstant.DOUBLE;
			}
		} else if( this.type.equals("long")) {
			try { 
				this.numValue = Long.parseLong(tvalue);
				this.value =  tvalue;
			} catch(Exception e){
				this.value =   SaadaConstant.STRING;
				this.numValue = SaadaConstant.DOUBLE;
			}
		} else {
			this.value =  tvalue;
			this.numValue = SaadaConstant.DOUBLE;
		}
	}



	public String getNameorg(){
		return this.nameorg;
	}        

	public String getNameattr(){
		return this.nameattr;
	}


	public String getUntrimmedValue(){
		return this.value;
	}
	public String getValue(){
		return this.value.trim();
	}
	/* (non-Javadoc)
	 * @see hecds.wcs.descriptors.CardDescriptor#trimValue()
	 */
	public void trimValue(){
		this.value = this.value.trim();
	}
	public String getTrimedValue(){
		return this.value.trim();
	}
	public double getNumValue(){
		return this.numValue;
	}

	/* (non-Javadoc)
	 * @see hecds.wcs.descriptors.CardDescriptor#getUcd()
	 */
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

	public boolean isNamedLike(String name) {
		return (this.nameattr.equals(name) || this.nameorg.equals(name));
	}

	public boolean isNameMatch(String regex) {
		return (this.nameattr.matches(regex) || this.nameorg.matches(regex));
	}

	/*
	 * The following accessors give the existence of a valid value for the field
	 * Lately added must be inserted within the code by opportunity
	 */
	/**
	 * @return
	 */
	public boolean hasUnit() {
		return !(this.unit == null || this.unit.length() == 0);
	}
	/**
	 * @return
	 */
	public boolean hasUcd() {
		return !(this.ucd == null || this.ucd.length() == 0);
	}
	/**
	 * @return
	 */
	public boolean hasUtype() {
		return !(this.utype == null || this.utype.length() == 0);
	}
	/**
	 * @return
	 */
	public boolean hasValue() {
		return !(this.value == null || this.value.length() == 0);
	}

	/**
	 * Used to populate the meta category tables {@link Table_Saada_Metacat}
	 * @param subkey
	 * @return
	 * @throws FatalException
	 */
	public String getDumpLine(int subkey) throws FatalException {

		return  subkey
				+ "\t"  + this.level
				+ "\t"  + this.classid
				+ "\t"  + this.classname
				+ "\t" + this.nameattr
				+ "\t" + this.type
				+ "\t" + Database.getWrapper().getEscapeQuote(this.nameorg)
				+ "\t" + this.ucd
				+ "\t" + this.utype
				+ "\t" + this.vo_dm
				+ "\t-1" 
				+ "\t"   + Database.getWrapper().getBooleanAsString(this.queriable)
				+ "\t"  + this.unit
				+ "\t" + this.comment.replaceAll("'", "")
				+ "\t" + this.collname 
				+ "\t"   + this.collid 
				+ "\t"  + this.format ;
	}

	/**
	 * @param subkey no longer used
	 * @return
	 * @throws FatalException
	 */
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
	public AttributeHandler getAssociateAtttribute() {
		return associateAttribute;
	}

	/**
	 * @param ass_error The ass_error to set.
	 */
	public void setAssociateAttribute(AttributeHandler associateAttribute) {
		this.associateAttribute = associateAttribute;
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
			return SaadaConstant.NOTSET;
		}
		String strval = value.toString();
		if( strval.length() == 0 || strval.equals("Infinity") || strval.equals("NaN") || 
				strval.equals("") || strval.equals("NULL") || strval.equals("2147483647") || 
				strval.equals("-2147483648") ||  strval.equals("9223372036854775807") ) {
			return SaadaConstant.NOTSET;
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

	/**
	 * 
	 */
	public void setAsConstant() {
		this.nameattr = this.nameorg = ColumnMapping.NUMERIC;
		this.constantValue = true;
	}
	public boolean isConstantValue() {
		return this.constantValue;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.nameorg + "(" + this.nameattr + "," + this.type + "," + this.unit + ","+ this.ucd + ","+ this.utype+ "," + this.value+ "," + this.comment + ")";
	}

	/*
	 * CardDescriptor implementation
	 */
	@Override
	public String getName() {
		return this.getNameorg();
	}

	@Override
	public String getDbName() {
		return this.getNameattr();
	}

	@Override
	public String getDescription() {
		return this.getComment();
	}

	@Override
	public double getDoubleValue() throws Exception {
		if( this.value == null || "NULL".equals(this.value) || "null".equalsIgnoreCase(this.value)){
			return SaadaConstant.DOUBLE;
		}
		/**
		 * Take care strange FITS number formats such as 123-311
		 * which are taken as string
		 */
		else if( this.type.equals("String") ) {
			if( this.value == null ) {
				return SaadaConstant.DOUBLE;
			}
			double retour = Double.NaN;
			try {
				retour = Double.parseDouble(this.value);
			} catch (NumberFormatException e) {
				String[] sv = this.value.trim().toString().split("-");
				if("-Infinity".equals(this.value.trim()) ) {
					retour = 0;
				} else if( sv.length == 1) {
					return Double.NaN;
				} else if( sv.length == 2) {
					if( sv[0].length() == 0 ) {
					} else {
						try {
							retour = Double.parseDouble(sv[0] + "E-" +  sv[1]);
						}  catch (Exception e1) {
							retour =  SaadaConstant.DOUBLE;
						}
					}
				} else if( sv.length == 3) {
					if( sv[0].length() != 0 ) {
					} else {
						try {
							retour = Double.parseDouble("-" + sv[1] + "E-" +  sv[2]);
						}  catch (Exception e1) {
							retour =  SaadaConstant.DOUBLE;
						}
					}
				}
			}
			return retour;			
		}
		if( this.type.equals("double") || this.type.equals("float")|| this.type.equals("int")|| this.type.equals("short")){
			return  Double.parseDouble(this.value);
		}
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot convert " + this.nameorg + " as " + this.type + " in double");
		return SaadaConstant.DOUBLE;
	}

	@Override
	public int geIntValue() throws Exception {
		if( this.type.equals("int")|| this.type.equals("short")){
			return Integer.parseInt(this.value);
		}
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot convert a " + this.type + " in integer");
		return SaadaConstant.INT;
	}

	@Override
	public int compareTo(Object o) {
		AttributeHandler ah = (AttributeHandler) o;
		if(ah.getNameattr().equals(this.getNameattr()) && ah.getNameorg().equals(this.getNameorg())){
			return 0;
		} else {
			return this.nameorg.compareTo(ah.getNameorg());
		}
	}

	public static void main( String[] args) {
		AttributeHandler ah = new AttributeHandler();
		ah.setType("double");
		ah.setValue("1.89e-321");
		System.out.println(ah + " " + ah.getNumValue());
	}
}


