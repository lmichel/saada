package saadadb.generationclass;
/**
 * <p>Title: SAADA </p>
 * <p>Description: Automatic Archival System For Astronomical Data -
    This is framework of a PhD funded by the CNES and by the Region Alsace.</p>
 * <p>Copyright: Copyright (c) 2002-2006</p>
 * <p>Company: Observatoire Astronomique Strasbourg-CNES</p>
 * @author: MILLAN Patrick
 */
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.configuration.CollectionAttributeExtend;
import saadadb.database.Repository;
import saadadb.database.SaadaDBConnector;
import saadadb.meta.AttributeHandler;
import saadadb.util.DefineType;
import saadadb.util.Messenger;

/** 
 * @author michel
 * @version $Id: GenerationClassCollection.java 865 2013-12-04 15:25:26Z laurent.mistahl $
 *
 */
public class GenerationClassCollection{


	/**
	 * @param connector 
	 * @throws Exception
	 */
	public static void Generation(SaadaDBConnector connector) throws Exception{
		for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
			Generation(connector, cat);
		}
	}
    

	/**
	 * @param connector
	 * @param category
	 * @return the name of the generated class
	 * @throws Exception
	 */
	public static String Generation(SaadaDBConnector connector, int category) throws Exception{
		String name = Category.explain(category) + DefineType.TYPE_EXTEND_COLL;
		String separ = System.getProperty("file.separator");
		Map<String, AttributeHandler> keys = (new CollectionAttributeExtend(connector.getRoot_dir())).getAttrSaada(Category.explain(category));
		File name_file = new File(connector.getRoot_dir() 
		+ separ + "class_mapping"  
		+ separ + name + ".java");
		FileWriter writer = new FileWriter(name_file, false);
		writer.write("package generated." + connector.getDbname() + ";\n"
		           + "/** Generated Code **/\n"
		           + "import saadadb.collection.obscoremin.*;\n");
		writer.write("public class " + name + " extends "+ Category.getCollectionExtension(category)+" {  \n");
		if(keys!=null){
			if(!keys.isEmpty()){
				for( String element: keys.keySet() ) {
					GenerationClassProduct.createType(writer, keys.get(element).getType(), element);
				}
			}
		}
		GenerationClassProduct.createConstructor(writer, name);
		if(keys!=null){
			if(!keys.isEmpty()){
				for( String element: keys.keySet() ) {
					createSetAndGet(writer, (String)keys.get(element).getType(), element);
				}
			}
		}
		writer.write("}");
		writer.flush();
		writer.close();
	   	Compile.compileItWithAnt(connector.getRoot_dir()
    			, name
    			, (connector.getRepository() + separ + Repository.TMP).replaceAll("\\\\", "\\\\"+"\\\\"));    
		Messenger.printMsg(Messenger.TRACE, "Class <"+name+"> created ");	
		return name;
	}
	
    /**
     * @param writer
     * @param type
     * @param key
     * @throws Exception
     */
    public static void createSetAndGet(FileWriter writer, String type, String key) throws Exception{
	    writer.write("  public void set"+key+"(" +type+" "+key+"){\n"
			 +"  	this."+key+" = "+key+";\n"
			 +"  }\n"
			 +"  public "+type+" get"+key+"(){\n"
			 +"        return "+key+";\n"
			 +"  }\n");
    }
}
  
