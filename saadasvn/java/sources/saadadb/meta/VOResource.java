package saadadb.meta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.ChangeKey;
import saadadb.util.Messenger;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

/** * @version $Id$

 * @author laurent
 * @version 07/2011 Make sure that position UCDs are set
 */
public class VOResource {
	private  String name;
	private boolean native_mode = false;
	private Map<String, String> class_mapping;	
	private int category = -1;
	private String collection;

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	/**
	 * @return the native_mode
	 */
	public boolean isNative_mode() {
		return native_mode;
	}

	LinkedHashMap<String, Set<UTypeHandler>> groups;

	/**
	 * @param groups
	 */
	public VOResource(String name, LinkedHashMap<String, Set<UTypeHandler>> groups) {
		this.groups = groups;
		this.name = name;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	@SuppressWarnings({ "rawtypes" })
	public VOResource(String configfile) throws Exception {
		Messenger.printMsg(Messenger.DEBUG, "Loading DM file <" + configfile + ">");
		SavotPullParser parser = new SavotPullParser(configfile, SavotPullEngine.ROWREAD);	
		/*
		 * Requested to init Savot
		 */
		SavotTR currentTR = parser.getNextTR(); 

		SavotVOTable   voTable = parser.getVOTable();
		SavotResource currentResource = (SavotResource) (voTable.getResources().getItemAt(0));
		SavotTable stable = (SavotTable) currentResource.getTables().getItemAt(0);
		ParamSet ps = stable.getParams();
		String resource_name = "";
		for( int i=0 ; i<ps.getItemCount() ; i++ ) {
			SavotParam sp = (SavotParam) ps.getItemAt(i);
			if( sp.getName().equals("dm")  ) {
				resource_name = sp.getValue();
				break;
			}
		}
		if( resource_name.equals("") ) {
			FatalException.throwNewException(SaadaException.FILE_FORMAT, "DM table <" + configfile + "> param <dm> not exist or has no value");
			return;
		}
		this.name = resource_name;
		this.groups = new LinkedHashMap<String, Set<UTypeHandler>>();;
		int cpt = 1;
		do {  
			if( currentTR == null ) {
				Messenger.printMsg(Messenger.WARNING, "DM table <" + configfile + "> has no data");
				break;
			}
			TDSet tds = currentTR.getTDSet();
			Vector tdv = tds.getItems();
			if( tdv.size() != 11 ) {
				FatalException.throwNewException(SaadaException.FILE_FORMAT, "DM file <" + configfile + "> badly formated at <TR> #" + cpt); 
				return;
			}
			String group_name = ((SavotTD)(tdv.get(0))).getContent();
			Set<UTypeHandler> guth = this.groups.get(group_name);
			if( guth == null ) {
				guth = new LinkedHashSet<UTypeHandler>();
				this.groups.put(group_name, guth);
			}
			UTypeHandler uth = new UTypeHandler(tdv);
			guth.add(uth);			
			cpt++;
		}while( (currentTR = parser.getNextTR()) != null );

	}

	/**
	 * @return
	 */
	public ArrayList<UTypeHandler> getUTypeHandlers() {
		ArrayList<UTypeHandler> retour = new ArrayList<UTypeHandler>();
		for( String g: this.getGroups().keySet() ) {
			for(UTypeHandler uth: this.getGroups().get(g) ) {
				retour.add(uth);
			}	
		}
		return retour;
	}

	/**
	 * @param utype_or_nickame
	 * @return utype or null
	 */
	public UTypeHandler getUTypeHandler(String utype_or_nickame) {
		for( Set<UTypeHandler> g: this.getGroups().values() ) {
			for(UTypeHandler uth: g ) {
				if( uth.getUtype().equals(utype_or_nickame) || uth.getNickname().equals(utype_or_nickame)) {
					return uth;
				}
			}	
		}
		return null;
	}
	/**
	 * @param name
	 * @return
	 * @throws Exception 
	 * @throws QueryException 
	 */
	public static VOResource getResource(String name) throws Exception {
		if( name == null ) {
			return null;
		}
		int category = -1;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Looking for VOResource <" + name + ">" );
		String lc_name = name.toLowerCase();
		/*
		 * If the resource name looks like "native category", a resource made with native attributes at collection level is returned
		 */
		if( lc_name.startsWith("native") ) {
			LinkedHashMap<String, AttributeHandler> lah = null;
			if( lc_name.endsWith("entry") ) {
				lah = MetaCollection.getAttribute_handlers_entry();
				category = Category.ENTRY;
			}
			else if( lc_name.endsWith("table") ) {
				lah = MetaCollection.getAttribute_handlers_table();
				category = Category.TABLE;
			}
			else if( lc_name.endsWith("spectrum") ) {
				lah = MetaCollection.getAttribute_handlers_spectrum();
				category = Category.SPECTRUM;
			}
			else if( lc_name.endsWith("image") ) {
				lah = MetaCollection.getAttribute_handlers_image();
				category = Category.ENTRY;
			}
			else if( lc_name.endsWith("misc") ) {
				lah = MetaCollection.getAttribute_handlers_misc();
				category = Category.IMAGE;
			}
			else if( lc_name.endsWith("flatfile") ) {
				lah = MetaCollection.getAttribute_handlers_flatfile();
				category = Category.FLATFILE;
			}

			if( lah != null ) {
				return VOResource.getNativeVOResource(name, filterCollectionColumns(lah));
			}
			else{
				Messenger.printMsg(Messenger.ERROR, "VOResource <" + name + "> not found");
				return null;
			}
		}
		/*
		 * If the resource name looks like "native class", a resource made with native attributes at collection + class 
		 * level is returned
		 */
		else if( lc_name.startsWith("native class ") ) {
			String classname = name.substring(lc_name.indexOf(" ")).trim();
			MetaClass mc = Database.getCachemeta().getClass(classname);
			int cat = mc.getCategory();

			LinkedHashMap<String, AttributeHandler> lah = null;
			switch (cat ) {
			case Category.IMAGE:
				lah = MetaCollection.getAttribute_handlers_image();				
				category = Category.IMAGE;
				break;
			case Category.TABLE:
				lah = MetaCollection.getAttribute_handlers_table();				
				category = Category.TABLE;
				break;
			case Category.ENTRY:
				lah = MetaCollection.getAttribute_handlers_entry();				
				category = Category.ENTRY;
				break;
			case Category.SPECTRUM:
				lah = MetaCollection.getAttribute_handlers_spectrum();				
				category = Category.SPECTRUM;
				break;
			case Category.MISC:
				lah = MetaCollection.getAttribute_handlers_misc(); 				
				category = Category.MISC;
				break;
			default:
				break;
			}
			/*
			 * The map returned by the cache is cloned to avoid the alteration of the cache
			 */
			LinkedHashMap<String, AttributeHandler> lahc = filterCollectionColumns( lah);
			lahc.putAll(mc.getAttributes_handlers());
			return VOResource.getNativeVOResource(name, lahc);
		}		
		else if( !lc_name.endsWith(" default") ) {

			/*
			 * Read resource from the file instead of the database while columns unit and req_level haven't been added tpo the SQL table
			 * That must be don for the next major release after 1.5.1
			 */
			String configfile  = Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "vodm." + name + ".xml";
			if( (new File(configfile)).exists() ) {
				return new VOResource(configfile);
			}
			else {
				Messenger.printMsg(Messenger.WARNING, "VOResource <" + configfile + "> not found, try to look at the repository");
				configfile  = Repository.getDmsPath()  + Database.getSepar() + "vodm." + name + ".xml";
				if( (new File(configfile)).exists() ) {
					return new VOResource(configfile);
				}
				Messenger.printMsg(Messenger.ERROR, "VOResource <" + configfile + "> not found");
				return null;
			}
		}
		/*
		 * Otherwise, we look into the VO resource table
		 */
		else {

			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT * FROM saada_vo_resources WHERE resource = '" + name + "' order by pk");
			LinkedHashMap<String, Set<UTypeHandler>> groups = new LinkedHashMap<String, Set<UTypeHandler>>();
			Set<UTypeHandler>uhl=null;
			String old_group=null;
			boolean found = false;

			while( rs.next() ) {
				found = true;
				String group = rs.getString("field_group");
				if( !group.equals(old_group) ) {
					old_group = group;
					uhl = new LinkedHashSet<UTypeHandler>();
					groups.put(old_group, uhl);
				}
				uhl.add(new UTypeHandler(rs));
			}
			squery.close();
			if( found ) {
				VOResource retour = new VOResource(name, groups);
				retour.setCategory(category);
				return retour;
			}
			else {
				Messenger.printMsg(Messenger.ERROR, "VOResource <" + name + "> not found");
				return null;
			}
		}
	}

	/**
	 * Make a copy of the map in order to avoid a cache alteration. This copy is filtered to discard internal keywords
	 * @param lah
	 * @return
	 */
	private static LinkedHashMap<String, AttributeHandler> filterCollectionColumns(LinkedHashMap<String, AttributeHandler> lah) {
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		for( Entry<String, AttributeHandler> e: lah.entrySet()) {
			String key = e.getKey();
			if( !key.equals("oidtable") && !key.equals("pos_x_csa") && !key.equals("pos_y_csa") && !key.equals("pos_z_csa") 
					&& !key.equals("date_load") ) {
				retour.put(key, e.getValue());
			}
		}
		return retour;

	}

	/**
	 * Build a pseudo VO  resource from the attribute handler map given in parameter.
	 * @param lah
	 * @return
	 * @throws Exception 
	 */
	public static VOResource getNativeVOResource(String name, LinkedHashMap<String, AttributeHandler> lah)  {
		return getNativeVOResource(name, lah.values());
	}
	/**
	 * Build a pseudo VO  resource from the attribute handler collection given in parameter.
	 * One group for collection attributes and another for class attributes
	 * @param lah
	 * @return
	 * @throws Exception 
	 */
	public static VOResource getNativeVOResource(String name, Collection<AttributeHandler> lah)  {
		LinkedHashMap<String, Set<UTypeHandler>> groups = new LinkedHashMap<String, Set<UTypeHandler>>();
		Set<UTypeHandler>uhl = new LinkedHashSet<UTypeHandler>();
		groups.put("Native Collection Attributes", uhl);
		for( AttributeHandler ah: lah) {
			String att_name = ah.getNameattr();
			/*
			 * Make sure that position UCDs are set
			 */
			if( ah.getUcd() == null || ah.getUcd().length() == 0 ) {
				if( ah.getNameattr().equals("pos_ra_csa")){
					ah.setUcd("pos.eq.ra;meta.main");
				}
				else if( ah.getNameattr().equals("pos_dec_csa")){
					ah.setUcd("pos.eq.dec;meta.main");
				}
				else if( ah.getNameattr().equals("error_maj_csa")){
					ah.setUcd("stat.error");
				}
				else if( ah.getNameattr().equals("error_min_csa")){
					ah.setUcd("stat.error");
				}
				else if( ah.getNameattr().equals("error_angle_csa")){
					ah.setUcd("stat.error;phys.angSize");
				}
			}
			if( !att_name.equals("contentsignature") && !att_name.equals("loaded") && 
					!att_name.equals("oidproduct") && !att_name.equals("access_right") && 
					!att_name.equals("sky_pixel_csa") && !att_name.equals("group_oid_csa") && 
					!att_name.equals("group_oid_csa") && !att_name.equals("nb_rows_csa") && 
					!ah.getNameattr().equals("md5keysaada")) {
				if( !att_name.startsWith("_") ) {
					UTypeHandler o = new UTypeHandler(ah);
					uhl.add(o);
				}
			}
		}
		uhl = new LinkedHashSet<UTypeHandler>();
		groups.put("Native Class Attributes", uhl);
		for( AttributeHandler ah: lah) {
			String att_name = ah.getNameattr();
			if( att_name.startsWith("_") ) {
				uhl.add(new UTypeHandler(ah));
			}
		}
		VOResource retour =  new VOResource(name, groups);	
		retour.native_mode = true;
		return retour;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a name understandable for Java compiler
	 * @return
	 */
	public String getJavaName() {
		return ChangeKey.changeKey(name);
	}

	/**
	 * Returns a name understandable for Java compiler
	 * @return
	 */
	public static String getJavaName(String name) {
		return ChangeKey.changeKey(name);
	}

	public LinkedHashMap<String, Set<UTypeHandler>> getGroups() {
		return groups;
	}

	/**
	 * @return
	 */
	public String[] groupNames() {
		return groups.keySet().toArray(new String[0]);
	}

	/**
	 * @param group
	 * @return
	 */
	public UTypeHandler[] getGroupUtypeHandlers(String group) {
		Set<UTypeHandler> uths = groups.get(group);
		if( uths == null ) {
			Messenger.printMsg(Messenger.WARNING, "can't get find group <" + group + "> in VO resoure <" + name + ">");
			return null;
		}
		else {
			return uths.toArray(new UTypeHandler[0]);
		}
	}



	/**
	 * @param filename
	 * @throws IOException
	 */
	public void saveInFile(String filename) throws IOException {
		File out = new File(filename );
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<VOTABLE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xmlns:http://www.ivoa.net/xml/VOTable-1.1.xsd\" version=\"1.1\">\n");
		bw.write("<RESOURCE type=\"VO Protocol Result Description\">\n");
		bw.write(" <DESCRIPTION>Saada VO service mapping</DESCRIPTION>\n");
		bw.write(" <INFO name=\"" + this.name + "\" value=\"OK\"/>\n");
		bw.write(" <TABLE name=\"Results\">\n");
		bw.write("  <PARAM name=\"dm\" value=\"" + this.name + "\" datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"group\"          datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"name\"           datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"ucd\"            datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"utype\"          datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"type\"           datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"arraysize\"      datatype=\"int\" />\n");
		bw.write("  <FIELD name=\"unit\"           datatype=\"char\" arraysize=\"*\"/>\n");
		bw.write("  <FIELD name=\"hidden\"         datatype=\"boolean\" />\n");
		bw.write("  <FIELD name=\"default_value\"  datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"desciption\"     datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"requ_level\"     datatype=\"char\" arraysize=\"*\">\n");
		bw.write("    <DESCRIPTION>MAN=1 REC=2 OPT=3</DESCRIPTION>\n");
		bw.write("  </FIELD>\n");

		bw.write("  <DATA>\n");
		bw.write("  <TABLEDATA>\n");
		String[] groups = this.groupNames();
		for( String group: groups ) {
			UTypeHandler[] uths = this.getGroupUtypeHandlers(group);
			for( UTypeHandler uth: uths) {
				String td = "    <TR>\n";
				td += "     <TD>" + group + "</TD>\n";
				td += "     <TD>" + uth.getNickname() + "</TD>\n";
				td += "     <TD>" + uth.getUcd() + "</TD>\n";
				td += "     <TD>" + uth.getUtype() + "</TD>\n";
				td += "     <TD>" + uth.getType() + "</TD>\n";
				td += "     <TD>" + uth.getArraysize() + "</TD>\n";
				td += "     <TD>" + uth.getUnit() + "</TD>\n";
				td += "     <TD>" + uth.isHidden() + "</TD>\n";
				td += "     <TD>" + uth.getValue() + "</TD>\n";
				td += "     <TD>" + uth.getComment() + "</TD>\n";
				td += "     <TD>" + uth.getRequ_level() + "</TD>\n";
				td += "    </TR>\n";
				bw.write(td);					
			}
		}
		bw.write("   </TABLEDATA>\n");
		bw.write("  </DATA>\n");
		bw.write(" </TABLE>\n");
		bw.write("</RESOURCE>\n");
		bw.write("</VOTABLE>\n");
		bw.close();
	}

	@Override
	public String toString() {
		String retour = this.name + "\n";
		if( this.groups == null ) {
			return retour + "No groups set";
		}
		String[] groups = this.groupNames();
		for( String group: groups ) {
			retour += "GROUP " + group + "\n";
			UTypeHandler[] uths = this.getGroupUtypeHandlers(group);
			for( UTypeHandler uth: uths) {
				retour += " name=" + uth.getNickname()
				+ " ucd=" + uth.getUcd()
				+ " utype=" + uth.getUtype()
				+ " type=" + uth.getType()
				+ " asize=" + uth.getArraysize()
				+ " hidden=" + uth.isHidden()
				+ " unit=" + uth.getUnit()
				+ " value=" + uth.getValue()
				+ " desc=" + uth.getComment()
				+ " reqlevel=" + uth.getRequ_level() + "\n";
			}
		}
		return retour;
	}

	/**
	 * @param class_name
	 * @return
	 */
	public String getMappingFilepath(String class_name) {
		return "dmmap." + this.getJavaName() + "." + class_name + ".xml" ;
	}
	/**
	 * @param filename
	 * @throws IOException
	 */
	public void saveClassMapping(String class_name, Map<String, String> mapping) throws IOException {
		String filename = Database.getConnector().getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + getMappingFilepath(class_name) ;
		File out = new File(filename );
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<VOTABLE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"xmlns:http://www.ivoa.net/xml/VOTable-1.1.xsd\" version=\"1.1\">\n");
		bw.write("<RESOURCE type=\"VO Protocol Result Description\">\n");
		bw.write(" <DESCRIPTION>Saada VO service mapping</DESCRIPTION>\n");
		bw.write(" <INFO name=\"" + this.name + "\" value=\"OK\"/>\n");
		bw.write(" <TABLE name=\"Results\">\n");
		bw.write("  <PARAM name=\"dm\" value=\"" + this.name + "\" datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <PARAM name=\"classname\" value=\"" + class_name + "\" datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"dmfield\"          datatype=\"char\" arraysize=\"*\" />\n");
		bw.write("  <FIELD name=\"class_mapping\"           datatype=\"char\" arraysize=\"*\" />\n");

		bw.write("  <DATA>\n");
		bw.write("  <TABLEDATA>\n");
		for( Entry<String, String> e: mapping.entrySet()) {
			String td = "    <TR>\n";
			td += "     <TD>" + e.getKey() + "</TD>\n";
			td += "     <TD><![CDATA[ " + e.getValue() + "]]></TD>\n";
			td += "    </TR>\n";
			bw.write(td);	 				
		}

		bw.write("   </TABLEDATA>\n");
		bw.write("  </DATA>\n");
		bw.write(" </TABLE>\n");
		bw.write("</RESOURCE>\n");
		bw.write("</VOTABLE>\n");
		bw.close();
	}

	/**
	 * @param mapping
	 */
	public void storeClassMapping(Map<String, String> mapping) {
		this.class_mapping = mapping;
	}

	/**
	 * @return
	 */
	public String getDMInterfaceCode() {
		if( this.class_mapping == null ) {
			return null;
		}
		String retour;
		retour  = "    // DM <" + this.name + "> begins (do not remove!)\n";
		retour += "    /*\n";
		retour += "     * Class generated by Saada to implement the DM " + this.name + "\n";
		retour += "     */\n";
		retour += "    public class " + this.getJavaName() + " implements DMInterface {\n";
		retour += "        // Code generated by Saada\n";
		retour += "        public String getDMName()  throws FatalException{ return \"" + this.getJavaName() + "\";}\n";
		retour += "        // Code generated by Saada\n";
		retour += "        public String getSQLField(String utype_or_nickname) {\n";
		retour += "            if( utype_or_nickname == null ) {\n"; 
		retour += "              return \"'null'\";\n"; 
		retour += "            }\n"; 
		for( UTypeHandler uth: getUTypeHandlers()) {
			String mapping_text = this.class_mapping.get(uth.getNickname());
			retour += "            else if(utype_or_nickname.equals(\"" +  uth.getUtype() + "\") || utype_or_nickname.equals(\"" + uth.getNickname() + "\") ) {\n"; 
			if( mapping_text == null || mapping_text.length() == 0  ) {
				retour += "              return \"'null'\";\n"; 
			}
			else {
				retour += "              return \"" + mapping_text + "\";\n"; 
			}
			retour += "            }\n"; 
		}
		retour += "            else  {\n"; 
		retour += "              return \"null\";\n"; 
		retour += "            }\n"; 
		retour += "        }\n";
		retour += "        public Object getDMFieldValue(String utype_or_nickname) throws FatalException {\n";
		retour += "            if( utype_or_nickname == null ) {\n"; 
		retour += "              return null;\n"; 
		retour += "            }\n"; 
		for( UTypeHandler uth: getUTypeHandlers()) {
			String mapping_text = this.class_mapping.get(uth.getNickname());
			retour += "            else if(utype_or_nickname.equals(\"" +  uth.getUtype() + "\") || utype_or_nickname.equals(\"" + uth.getNickname() + "\") ) {\n"; 
			if( mapping_text.length() == 0  ) {
				retour += "              return null;\n"; 
			}
			else {
				retour += "              return " + mapping_text.replaceAll("'", "\"") + ";\n"; 
			}
			retour += "            }\n"; 
		}
		retour += "            else  {\n"; 
		retour += "              return null;\n"; 
		retour += "            }\n"; 
		retour += "        }\n";
		retour += "        // Code generated by Saada\n";
		retour += "        public String getSQLAlias(String utype) {\n";
		retour += "            if( utype == null ) {\n"; 
		retour += "              return \"null\";\n"; 
		retour += "            }\n"; 
		for( UTypeHandler uth: getUTypeHandlers()) {
			retour += "            else if(utype.equals(\"" +  uth.getUtype() + "\") ) {\n"; 
			retour += "              return \"" +  uth.getNickname() + "\";\n"; 
			retour += "            }\n"; 
		}
		retour += "            else  {\n"; 
		retour += "              return \"null\";\n"; 
		retour += "            }\n"; 
		retour += "        }\n";
		retour += "        // Code generated by Saada\n";
		retour += "        public  LinkedHashMap<String, String> getSQLFields()  throws FatalException{\n";
		retour += "            LinkedHashMap<String, String> retour = new LinkedHashMap<String, String>();\n";
		for( UTypeHandler uth: getUTypeHandlers()) {
			String mapping_text = this.class_mapping.get(uth.getNickname());
			if(mapping_text.length() == 0  ) {
				retour += "            retour.put(\"" + uth.getNickname() + "\", \"'null'\");\n";
			}
			else {
				retour += "            retour.put(\"" + uth.getNickname() + "\", \"" + mapping_text + "\");\n";
			}
		}
		retour += "            return  retour;\n";
		retour += "        }\n";
		retour += "        // Code generated by Saada\n";
		retour += "        public Object getFieldValue(String utype_or_nickname, SaadaQLResultSet rs)  throws FatalException{\n";
		retour += "          try {\n";
		retour += "        	   return rs.getObject(utype_or_nickname);\n";
		retour += "          } catch (SQLException e) {\n";
		retour += "        	   FatalException.throwNewException(SaadaException.DB_ERROR, e);\n";
		retour += "        	   return null;\n";
		retour += "          }\n";
		retour += "        }\n";
		retour += "    }\n";
		retour += "    // DM <" + this.name + "> ends (do not remove!)\n";
		return retour;		
	}

	/**SELECT * FROM saada_vo_resources WHERE resource = '" + name + "' order by pk
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws QueryException 
	 */
	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		Database.init("DEVBENCH1_5_1");
		//		 System.out.println(VOResource.getResource("class CatalogueEntry").toString());
		//		 VOResource vor = VOResource.getResource("SIA default");
		//		 if( vor != null ) {
		//			 String[] groups = vor.groupNames();
		//			 for( String group: groups ) {
		//				 System.out.println("GROUP " + group);
		//				 UTypeHandler[] uths = vor.getGroupUtypeHandlers(group);
		//				 for( UTypeHandler uth: uths) {
		//					 System.out.println(" " + uth.getType() + " " + uth.getUcd( ) + " " + uth.getRequ_level());						
		//				 }
		//			 }
		//		 }
		//		 vor.saveInFile(Database.getConnector().getRoot_dir() + Database.getSepar() + "config" 
		//					+ Database.getSepar() + "vodm.SIA.xml" );
		//
		new VOResource("/home/michel/saada/deploy/TestBench1_5_1/saadadbs/DEVBENCH1_5_1/config/vodm.SSA.xml");
		//		 vor.saveInFile(Database.getConnector().getRoot_dir() + Database.getSepar() + "config" 
		//					+ Database.getSepar() + "vodm.SSA.xml" );
		//		 vor = VOResource.getResource("Cone Search default");
		//		 vor.saveInFile(Database.getConnector().getRoot_dir() + Database.getSepar() + "config" 
		//					+ Database.getSepar() + "vodm.CS.xml" );
		//		 vor = VOResource.getResource("SSA EPIC Spectra");
		//		 vor.saveInFile(Database.getConnector().getRoot_dir() + Database.getSepar() + "config" 
		//					+ Database.getSepar() + "vodm.EPIC_SSA.xml" );
	}


}
