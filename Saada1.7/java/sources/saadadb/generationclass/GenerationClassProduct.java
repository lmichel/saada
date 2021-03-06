package saadadb.generationclass;

/**
 * <p>Title: SAADA </p>
 * <p>Description: Automatic Archival System For Astronomical Data -
 This is framework of a PhD funded by the CNES and by the Region Alsace.</p>
 * <p>Director of research: L.Michel and C. Motch.</p>
 * <p>Copyright: Copyright (c) 2002-2005</p>
 * <p>Company: Observatoire Astronomique Strasbourg-CNES</p>
 * @version SAADA 1.0
 * @author: NGUYEN Ngoc Hoan
 * E-Mail: nguyen@saadadb.u-strasbg.fr</p>
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.sqltable.SQLTable;
import saadadb.util.ChangeKey;
import saadadb.util.ChangeType;
import saadadb.util.DefineType;
import saadadb.util.Messenger;

public class GenerationClassProduct{
	private static String separ = System.getProperty("file.separator");
	
	/** * @version $Id$

	 * @param writer
	 * @throws GenerateClassException
	 * @throws IOException 
	 * @throws SaadaException 
	 */
	public static void createImport(FileWriter writer) throws IOException{
		writer.write("package generated." + Database.getName()+";\n"
				+"/**Code generated**/\n"
				+"import saadadb.collection.*;\n"
				+"import saadadb.exceptions.*;\n"
				+"import java.sql.ResultSet;\n"
				+"import java.sql.SQLException;\n"
				+"\n");
	}
	
	/**
	 * @param writer
	 * @param type
	 * @param key
	 * @throws IOException 
	 */
	public static void createType(FileWriter writer, String type, String key) throws IOException {
		String pack = " = saadadb.util.SaadaConstant.";
		String pub = "\n public "+type+"  "+key ;
		String vType = ChangeType.getType(type);
		if(!vType.equals("")){
			switch(DefineType.getType(vType)){
			case DefineType.FIELD_LONG:
				pub += pack+"LONG";
				break;
			case DefineType.FIELD_INT:
				pub += pack+"INT";
				break;
			case DefineType.FIELD_DOUBLE:
				pub += pack+"DOUBLE";
				break;
			case DefineType.FIELD_FLOAT:
				pub += pack+"FLOAT";
				break;
			case DefineType.FIELD_STRING:
				pub += pack+"STRING";	
				break;
			case DefineType.FIELD_SHORT:
				pub += pack+"SHORT";
				break;
			case DefineType.FIELD_BYTE:
				pub += pack+"BYTE";
				break;
			case DefineType.FIELD_CHAR:
				pub += pack+"CHAR";
				break;
			case DefineType.FIELD_BOOLEAN:
				pub += " ";
				break;
			default:
				pub += " ";
			Messenger.printMsg(Messenger.ERROR, "GenerationClassProduct : Unknow type " + type);
			break;
			}
		}
		pub += ";  \n";
		writer.write(pub);
	}
	
	/**
	 * @param writer
	 * @param type
	 * @param key
	 * @throws IOException 
	 */
	public static void createSetAndGet(FileWriter writer, String type, String key) throws IOException{
		writer.write("\n  public void set"+key+"(" +type+" "+key+"){\n"
				+"  	this."+key+" = "+key+";\n"
				+"  }\n"
				+"  public "+type+" get"+key+"() throws SaadaException{\n"
				+"     if(loaded==false){\n"
				+"         loadBusinessAttribute();\n"
				+"     }\n"
				+"     return "+key+";\n"
				+"  }\n");
	}
	
	/**
	 * @param writer
	 * @param name
	 * @throws IOException 
	 */
	public static void createConstructor(FileWriter writer, String name) throws IOException{
		writer.write("\n  public "+name+"(){\n"
				   + "    super();\n"
				   + "  }\n");
	}
	
	/**
	 * @param classHeader
	 * @param classpath
	 * @param classname
	 * @param superclass
	 * @throws SaadaException
	 * @throws IOException
	 */
	public static final void buildJavaClass(LinkedHashMap<String, AttributeHandler> classHeader, String classpath, String classname, String superclass)throws Exception{
		File nameFile = new File(classpath+separ+classname+".java");
		FileWriter writer = new FileWriter(nameFile, false);
		createImport(writer);
		writer.write("public class "+classname+"  extends "+superclass+"{  \n");
		Iterator it = classHeader.keySet().iterator();
		while( it.hasNext()) {
			AttributeHandler ah = classHeader.get(it.next());
			String type = ah.getType();
			String key = ah.getNameattr();
			if(key.indexOf("_HIERARCH")==0){
				key = ChangeKey.changeKeyHIERARCH(key);
			}
			createType(writer, type , key);
		}
		createConstructor(writer, classname);
		it = classHeader.keySet().iterator();
		while( it.hasNext()) {
			AttributeHandler ah = classHeader.get(it.next());
			String key = ah.getNameattr();
			String type = ah.getType();
			if(key.indexOf("_HIERARCH")==0){
				key = ChangeKey.changeKeyHIERARCH(key);
			}
			createSetAndGet(writer, type, key);
		}
		writer.write("}\n");
		writer.flush();
		writer.close();
		Database.gc();
		Compile.compileItWithAnt(Database.getRoot_dir(), classname);
	}
		
	public static final void renameJavaClass(String classpath, String classname , String newName)throws Exception{
		File nameFile = new File(classpath+separ+classname+".java");
		BufferedReader br = new BufferedReader(new FileReader(nameFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(classpath+separ+newName+".java"));
		String buff;
		while( (buff = br.readLine()) != null  ){
			bw.write(buff.replaceAll(classname, newName)+ "\n");
		}
		bw.close();
		br.close();
		nameFile.delete();
		Compile.compileItWithAnt(Database.getRoot_dir(), newName);
	}
	
}

