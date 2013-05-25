package saadadb.admintool.utils;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.database.Database;


public class AntDesk {
	public static final Map<String, String[]> map;
	public static final String HEADER = "ANT FILE HEADER";
	static{
		map = new LinkedHashMap<String, String[]>();
		map.put(HEADER, new String[] {
				"<project name=\"userTask\" default=\"user.task\">"
				, "  <!--"
				, "     saadadb.properties file is setup at SaadaDB creation time with pathes matching"
				, "     the installation location"
				, "     This file must be modified by hand if the SaadaDB is moved or other Java tools are used"
				, "    -->"
				, "  <property file=\"" + Database.getUrl_root() + "/bin/saadadb.properties\"/>"
				, "  <property name=\"jvm_initial_size\" value=\"-Xms64m\" />"
				, "  <property name=\"jvm_max_size\"     value=\"-Xmx1024m\" />"

				, "    <!--"
				, "      This classpath is used by all java calls."
				, "      Classes or jar files specifiv for an application must be added here"
				, "     -->"
				, "  <path id=\"saadadb.classpath\">"
				, "    <pathelement location=\"${SAADA_DB_HOME}/class_mapping/\"/>"
				, "    <fileset dir=\"${SAADA_DB_HOME}/lib/\">"
				, "      <include name=\"**/*.jar\" />"
				, "    </fileset>"
				, "    <fileset dir=\"${SAADA_DB_HOME}/jtools/\">"
				, "      <include name=\"**/*.jar\" />"
				, "    </fileset>"
				, "  </path>"
		});
		map.put(AdminComponent.LOAD_DATA, new String[] {
				"  <target name=\"user.task\">"
				,"      <java  classname=\"saadadb.dataloader.Loader\" fork=\"true\" failonerror=\"true\">"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"

		});	
		map.put(AdminComponent.CREATE_COLLECTION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageCollection\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.EMPTY_COLLECTION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageCollection\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.DROP_COLLECTION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageCollection\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.EMPTY_CATEGORY, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageCollection\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.MANAGE_PRODUCT, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageProduct\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.CREATE_RELATION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageRelation\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.DROP_RELATION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageRelation\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.POPULATE_RELATION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageRelation\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.EMPTY_RELATION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageRelation\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.INDEX_RELATION, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageRelation\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.SQL_INDEX, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageTableIndex\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
		map.put(AdminComponent.REMOVE_PRODUCT, new String[] {
				"  <target name=\"user.task\">"
		    	,"      <java  classname=\"saadadb.command.ManageProduct\" fork=\"true\"  failonerror=\"true\"  >"
				,"        <classpath refid=\"saadadb.classpath\"/>\n"
		});
	}

	/**
	 * @param key
	 * @return
	 */
	public static final String[] get(String key){
		return map.get(key);
	}

	public static final String[] getHeader() {
		return get(HEADER);
	}

	/**
	 * @return
	 */
	public static final String  getHeadComment(String panelTitle) {
		String retour = "";
		retour += "\n";
		retour += "    <!--\n";
		retour += "      Task generated by the Saada admintool\n";
		retour += "      - Command Panel: " + panelTitle + "\n";
		try {
			retour += "      - Author: " + System.getProperty("user.name") 
			+ "@" + java.net.InetAddress.getLocalHost().getHostName()+ "\n";
		} catch (UnknownHostException e) {}
		retour += "      - Date: " + (new Date()) + "\n";
		retour += "\n";
		retour += "      Can run by the command: ant -f [thisFileName]\n";
		retour += "     -->\n";
		retour += "\n";
		return retour;
	}
	/**
	 * @param key
	 * @param xmlParams XML encoded params (<param ...>)
	 * @return
	 */
	public static final String getAntFile(String key, String panelTitle, String xmlParams) {
		String retour = getHeadComment(panelTitle);
		for( String s: AntDesk.getHeader() ) {
			retour += s + "\n";
		}

		for( String s: AntDesk.get(AdminComponent.LOAD_DATA) ) {
			retour += s + "\n";
		}
		retour +=  xmlParams;
		retour += "    </java>\n";
		retour += "  </target>\n";
		retour += "</project>\n";
		return retour;
	}
	/**
	 * @param key
	 * @param params
	 * @return
	 */
	public static final String getAntFile(String key, String panelTitle, String[] params) {
		String retour = getHeadComment(panelTitle);
		for( String s: AntDesk.getHeader() ) {
			retour += s + "\n";
		}

		for( String s: AntDesk.get(AdminComponent.LOAD_DATA) ) {
			retour += s + "\n";
		}
		for( String p: params ) {
			retour += "        <arg value=\"" + p + "\"/>\n";
		}
		retour += "        <arg value=\"${SAADA_DB_NAME}\"/>\n";
		retour += "    </java>\n";
		retour += "  </target>\n";
		retour += "</project>\n";
		return retour;
	}
}
