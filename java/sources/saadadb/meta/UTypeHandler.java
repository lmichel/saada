package saadadb.meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.ChangeKey;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotTD;

public class UTypeHandler {
	public final static int MANDATORY = 1;
	public final static int RECOMMENDED = 2;
	public final static int OPTIONAL  = 3;
	private String nickname;
	private String utype;
	private String value;
	private String ucd;
	private int requ_level;
	private String type;
	private String unit;
	private int arraysize;
	private String comment;
	private boolean hidden=false;
	private String expression;

	/** * @version $Id$

	 * @param resultSet
	 * @throws SQLException 
	 */
	public UTypeHandler()  {
		this.utype   = "";
		this.ucd     = "";
		this.value   = "";
		this.comment = "";
		this.type    = "char";
		this.hidden  = false;
		this.arraysize = -1;
		this.value = "";
		this.unit = "";
		this.expression="";
	}

	/**
	 * @param std
	 * @throws Exception
	 */
	public UTypeHandler(AttributeHandler ah) {
		this.nickname  = ah.getNameattr();
		this.ucd       = ah.getUcd();
		this.utype     = ah.getUtype();
		this.type      = ah.getType();
		if( this.type.equals("String")) {
			this.type = "char";
			this.arraysize = -1;
		}
		else {
			this.arraysize = 1;
		}
		this.hidden    = false;
		this.value     = "";
		this.unit      = ah.getUnit();
		this.comment   = ah.getComment();
		this.requ_level  = MANDATORY;
		// By default we assumes that the attribute handler refer to a database field
		this.expression="";
	}
	/**
	 * @param std
	 * @throws Exception
	 */
	public UTypeHandler(AttributeHandler ah, String expression) {
		this(ah);
		this.expression=expression;
	}

	/**
	 * @param std
	 * @throws Exception
	 */
	public UTypeHandler(List<SavotTD> std) throws Exception {
		this.nickname  = (std.get(1)).getContent();
		this.ucd       = (std.get(2)).getContent();
		this.utype     = (std.get(3)).getContent();
		this.type      = (std.get(4)).getContent();
		this.arraysize = Integer.parseInt(((SavotTD)std.get(5)).getContent());
		this.unit      = (std.get(6)).getContent();
		this.hidden    = Boolean.parseBoolean(((SavotTD)std.get(7)).getContent());
		this.value     = ((SavotTD)std.get(8)).getContent();;
		this.comment   = ((SavotTD)std.get(9)).getContent();
		int val = Integer.parseInt(((SavotTD)std.get(10)).getContent());
		switch(val) {
		case 1: this.requ_level  = MANDATORY; break;
		case 2: this.requ_level  = RECOMMENDED; break;
		default: this.requ_level = OPTIONAL; break;
		}
		this.expression= std.get(11).getContent();
	}

	/**
	 * @param rs
	 * @throws SQLException 
	 */
	public UTypeHandler(ResultSet rs) throws SQLException {
		this.utype   = rs.getString("utype");
		this.ucd     = rs.getString("ucd");
		this.value   = rs.getString("value");
		this.comment = rs.getString("description");
		this.type    = rs.getString("type");
		this.hidden  = rs.getBoolean("hidden");
		String as    = rs.getString("arraysize");
		if( as.equals("*")) {
			this.arraysize = -1;
		}
		else {
			this.arraysize = Integer.parseInt(as);
		}
		this.encodeName(rs.getString("nickname"));
	}

	/**
	 * @param nickname
	 * @param utype
	 * @param ucd
	 * @param requ_level
	 * @param type
	 * @param arraysize
	 * @param comment
	 * @param expression
	 */
	public UTypeHandler(String nickname, String utype, String ucd, String requ_level, String type, int arraysize, String unit, String comment, String expression){
		this.utype = utype;
		this.ucd = ucd;
		this.setReqLevel(requ_level);
		this.type = type;
		this.arraysize = arraysize;
		this.unit = unit;
		this.comment = comment;
		this.encodeName(nickname);
		this.expression =expression;
	}

	/**
	 * @param nickname
	 * @param utype
	 * @param ucd
	 * @param type
	 * @param arraysize
	 * @param hidden
	 * @param value
	 * @param comment
	 * @param expression
	 */
	public UTypeHandler(String nickname, String utype, String ucd, String type, int arraysize, boolean hidden, String value, String comment,String expression){
		this.utype = utype;
		this.ucd = ucd;
		this.value = value;
		this.type = type;
		this.arraysize = arraysize;
		this.comment = comment;
		this.hidden = hidden;
		this.encodeName(nickname);
		this.expression =expression;
	}

	

	/**
	 * Parameters in the same order than Jaiwon XLS sheet
	 * @param group
	 * @param utype
	 * @param value
	 * @param requ_level
	 * @param ucd
	 * @param type
	 * @param arraysize
	 */
	public UTypeHandler(String group, String utype, String value, String requ_level, String ucd, String type, int arraysize){
		this.utype = utype;
		this.ucd = ucd;
		this.setReqLevel(requ_level);
		this.value = value;
		this.type = type;
		this.arraysize = arraysize;
		this.encodeName("");
	}
	
	/**
	 * Returns an attribute handler matching the instance parameters
	 * @throws QueryException 
	 */
	public AttributeHandler getAttributeHandler() throws QueryException {
		AttributeHandler retour = new AttributeHandler();
		if( this.nickname == null || this.nickname.length() == 0 ) {
			QueryException.throwNewException(SaadaException.METADATA_ERROR, "Attempt to build an attribute handler without name");
		}
		retour.setNameorg(this.nickname);
		retour.setNameattr(ChangeKey.changeKey(this.nickname));
		retour.setUcd(this.ucd);
		retour.setUtype(this.utype);
		/*
		 * The value for array data are made with concatenation of individual values.
		 * Which always are strings
		 */
		if( this.arraysize != 1 ){
			retour.setType("String");
		}
//		if( this.type.equals("char") && this.arraysize == -1 ) {
//			retour.setType("String");
//		}
		else {
			retour.setType(this.type);
		}
		retour.setUnit(this.unit);
		retour.setComment(this.comment);
		return retour;
	}	
	
	/**
	 * @param req_level
	 */
	private void setReqLevel(String req_level) {
		if( req_level.startsWith("MAN")) {
			this.requ_level = MANDATORY;
		}
		else if( req_level.startsWith("REC")) {
			this.requ_level = RECOMMENDED;
		}
		else {
			this.requ_level = OPTIONAL;
		}

	}
	/**
	 * Build a nickname from the utype;
	 */
	public void encodeName(String def) {
		if( def != null && def.length() > 0) {
			this.nickname = def;
		}
		else {
			String[] meta_comp;
			if( utype.length() > 0 ) {
				meta_comp = utype.split("[\\.:]");
			}
			else if( ucd.length() > 0 ) {
				meta_comp = ucd.split("[\\.:]");
			}
			else {
				return;
			}
			String name="";
			for( int i=0 ; i<meta_comp.length ; i++ ) {
				if( i == 0 ) {
					name = meta_comp[0] + "_";
				}
				else if( meta_comp[i].length() <= 3 ) {
					name += meta_comp[i];
				}
				else {
					name += meta_comp[i].substring(0,3);
					
				}
			}
			this.nickname = name;
		}
	}

	/**
	 * Returns a Savot field compliant with a VOTable
	 * @return
	 */
	public SavotField getSavotField(int rank)throws Exception  {
		SavotField field = new SavotField();
		field.setName(nickname); 
		field.setUtype(utype);
		if( this.ucd != null && this.ucd.length() > 0 ) {
			field.setUcd(this.ucd);
		}
		/*
		 * We consider Votable datatypes similar to Java type: true for supported types but not for all datatypes
		 * CLOBs are interpreted as Strings
		 */
		if( this.type.equalsIgnoreCase("CLOB")){
			field.setUnit(this.unit);
			field.setDataType(Database.getWrapper().getVotableDataTypeFromAnyType(""));
			field.setArraySize("*");								
		} else {
			field.setDataType(Database.getWrapper().getVotableDataTypeFromAnyType(this.type));
			field.setUnit(this.unit);
			if( this.arraysize > 1 ) {
				field.setArraySize(Integer.toString(this.arraysize));								
			}
			else if( this.arraysize == -1 ) {
				field.setArraySize("*");								
			}
		}
		if( this.comment != null && this.comment.length() > 0 ) {
			field.setDescription(this.comment);
		}	
		//field.setRef(this.nickname + "_" + rank);				
		field.setId(this.nickname + "_" + rank);				
		if( this.isHidden() ) {
			field.setType("hidden");
		}
		/*
		 * Patch before the DM can handle units
		 */
		//TODO TO REMOVE
		/*
		if( "Char.TimeAxis.Coverage.Bounds.Extent".equals(this.utype)) {
			field.setUnit("s");
		}
		else if( "Char.TimeAxis.Coverage.Support.Extent".equals(this.utype)) {
			field.setUnit("s");
		}
		else if( "Access.Size".equals(this.utype)) {
			field.setUnit("kb");
		}*/
		return field;
	}
	
	/**
	 * @param value
	 * @param meta
	 * @return
	 */
	public SavotParam getSavotParam(Object value, String prefix) {
		SavotParam param = new SavotParam();
		param.setName(prefix + nickname); 
		param.setUtype(utype);
		if( this.ucd != null && this.ucd.length() > 0 ) {
			param.setUcd(this.ucd);
		}
		param.setDataType(this.type);
		if( this.arraysize > 1 ) {
			param.setArraySize(Integer.toString(this.arraysize));								
		}
		else if( this.arraysize == -1 ) {
			param.setArraySize("*");								
		}
		if( this.comment != null && this.comment.length() > 0 ) {
			param.setDescription(this.comment);
		}	
		param.setValue(value.toString());
		return param;
	}
	
	/**
	 * @return Returns the arraysize.
	 */
	public int getArraysize() {
		return arraysize;
	}
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return Returns the nickname.
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @return Returns the requ_level.
	 */
	public int getRequ_level() {
		return requ_level;
	}

	public boolean isMandatory() {
		return ( this.requ_level == MANDATORY ) ;
	}
	
	public boolean isRequested() {
		return ( this.requ_level == RECOMMENDED ) ;
	}
	
	public boolean isOptional() {
		return ( this.requ_level == OPTIONAL ) ;
	}
	
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return Returns the ucd.
	 */
	public String getUcd() {
		return ucd;
	}

	/**
	 * @return Returns the utype.
	 */
	public String getUtype() {
		return utype;
	}

	/**
	 * Utypes or nickname can be either used to identify a field. At least one of both must be populated
	 * UCDs are not used because they have no reason to be unique
	 * @return Returns the utype.
	 */
	public String getUtypeOrNickname() {
		if( this.utype != null && this.utype.length() > 0 ) {
			return utype;
		}
		else {
			return nickname;			
		}
	}

	/**
	 * @return Returns the hidden.
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return Returns the value.
	 */
	public String getUnit() {
		return unit;
	}
	/*
	 * Utypehandler are used by DM. They must be unique in a DM. They are handle by Set to do so.
	 * Set classes must be able to check the equality of UTypehandler. They first check the hascode, and in case of equality, the methods equals
	 * is invoked. 
	 * UTypeHandler are idemtified with 2 attributes (nickname and utype) which are not necessary set simultaneously.
	 */
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int retour = 0;
		if( this.nickname != null && this.nickname.length() > 0 ) {
			retour += this.nickname.hashCode();
		}
		if( this.utype != null && this.utype.length() > 0 ) {
			retour += this.utype.hashCode();
		}
		return retour;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if( obj instanceof UTypeHandler) {
			UTypeHandler u = (UTypeHandler)obj;
			if( this.nickname != null && this.nickname.length() > 0 ) {
				if( this.nickname.equals(u.getNickname()) ) {
					return true;
				}
				else {
					return false;
				}
			}
			if( this.utype != null && this.utype.length() > 0 ) {
				if( this.utype.equals(u.getUtype()) ) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	
	public String toString() {
		return nickname
		+ "\nutype: " + utype
		+ "\nvalue: " + value
		+ "\nucd: " + ucd
		+ "\nrequ_level: " + requ_level
		+ "\ntype: " + type
		+ "\nunit: " + unit
		+ "\narraysize: " + arraysize
		+ "\ncomment: " + comment
		+ "\nhidden: " + hidden
		+ "\nexpression: "+expression;
	}

}
