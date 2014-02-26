package saadadb.configuration; 

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id: CollectionAttributeExtend.java 915 2014-01-29 16:59:00Z laurent.mistahl $
 *
 */
public class CollectionAttributeExtend {
	
	/**
	 *  * @version $Id: CollectionAttributeExtend.java 915 2014-01-29 16:59:00Z laurent.mistahl $

	 */
	private static final long serialVersionUID = 1L;
	public static String separ = System.getProperty("file.separator");
	public static final String ATTR_NAME = "name";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_UCD  = "ucd";
	public static final String TAG_NAME_ATTR = "attr";
	public static final String TAG_NAME_DESCRIPTION = "description";
	public static final String TAG_NAME_LIST = "list";
	public static final String document = separ+ "config" + separ+ "collection_attr";	
	private Map<String, Map<String, AttributeHandler>> attMap ;
	private String baseDir;
	
	/**
	 * Read the file in the baseDir/config/collection_attr.xml and stores its content in attMap
	 * @param baseDir SaadaDB installation directory
	 * @throws Exception 
	 */
	public CollectionAttributeExtend(String baseDir) throws Exception{
		this.attMap = new LinkedHashMap<String,Map<String, AttributeHandler>> ();
		this.baseDir = baseDir;
		boolean isFileConfig = true;
		Document doc = null;
		for(int i=1; i<Category.NB_CAT; i++){
			this.attMap.put(Category.explain(i), new LinkedHashMap<String, AttributeHandler>());
		}
		try{
			/*
			 * Must use a file but not a name, because Java 1.6 cannot understand it for Windows path.
			 * c:\... is taken as a URL bug #6506304
			 */
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.baseDir + document+".xml");
			Messenger.printMsg(Messenger.TRACE, "Reading " + Database.getRoot_dir()+document+".xml");
		}catch(Exception e){
			Messenger.printStackTrace(e);
			isFileConfig = false;
		}
		if(isFileConfig){
			NodeList nodes = doc.getDocumentElement().getElementsByTagName(TAG_NAME_LIST);
			for(int i=0; i<nodes.getLength();i++){
				Element res = (Element)nodes.item(i);
				NodeList nodes_ch = res.getElementsByTagName(TAG_NAME_ATTR);
				for (int j=0; j<nodes_ch.getLength(); j++){
					Element res_ch = (Element)nodes_ch.item(j);
					NodeList nodes_desc = res_ch.getElementsByTagName(TAG_NAME_DESCRIPTION);
					AttributeHandler ah = new AttributeHandler();
					ah.setNameorg(res_ch.getAttribute(ATTR_NAME));
					ah.setNameattr(res_ch.getAttribute(ATTR_NAME));
					ah.setType(res_ch.getAttribute(ATTR_TYPE));
					String ucd = res_ch.getAttribute(ATTR_UCD);
					if ( ucd.length() != 0  ) {
						ah.setUcd(ucd);						
					}
					String description= "No description given";
					if( nodes_desc.getLength() > 0 ) {
						description =  nodes_desc.item(0).getTextContent();
					}
					ah.setComment(description);
					this.getAttrSaada(res.getAttribute(ATTR_NAME)).put(res_ch.getAttribute(ATTR_NAME), ah);
				}				
			}
		}
	}
	
	
	/**
	 * Store a formatted copy of the map. Ready to be saved but not saved.
	 * @param basedir
	 * @param map <category , map<attrname, type>>
	 * @throws Exception
	 */
	public CollectionAttributeExtend(String baseDir,  Map<String,LinkedHashMap<String, String>> map) throws Exception{
		this.attMap = new LinkedHashMap<String, Map<String, AttributeHandler>> ();
		this.baseDir = baseDir;
		for( Entry<String,LinkedHashMap<String, String>> e1: map.entrySet()) {
			Map<String, AttributeHandler> catMap = new LinkedHashMap<String, AttributeHandler>();
			this.attMap.put(e1.getKey(), catMap);
			for( Entry<String,String> e2: e1.getValue().entrySet()) {
				AttributeHandler ah = new AttributeHandler();
				ah.setNameattr(e2.getKey());
				ah.setNameorg(e2.getKey());
				ah.setType(e2.getValue());
				ah.setComment("Extended Attribute");
				catMap.put(e2.getKey(), ah);
			}			
		}
	}

	/**
	 * @throws Exception
	 */
	public void save() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Save new extended attributes in " + this.baseDir+document+".xml");
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.baseDir+document+".xml"));
		bw.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>\n");
		bw.write("<!DOCTYPE collection_attr SYSTEM \"collection_attr.dtd\">\n");
		bw.write("<collection_attr>\n");
		for( Entry<String, Map<String, AttributeHandler>> entr:  this.attMap.entrySet() ) {
			String cat = entr.getKey();
			Map<String, AttributeHandler> lhm = entr.getValue();
			bw.write("    <list name=\"" + cat + "\">\n");
			for( Entry<String, AttributeHandler> entry : lhm.entrySet() ) {
				bw.write("        <attr name=\"" + entry.getKey().trim() + "\" type=\"" + entry.getValue().getType().trim() + "\"/>\n");					
			}			
			bw.write("    </list>\n");
		}
		bw.write("</collection_attr>\n");
		bw.close();
		
	}

	/**
	 * @param category
	 * @param ah
	 * @throws FatalException 
	 */
	public void addAttributeExtend(String category, AttributeHandler ah) throws FatalException{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Add new extended attribute " + ah + " to category " + category);
		if( ah.getType() == null ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Attribute handler " + ah + " has no type" );
		}
		this.getAttrSaada(category.toUpperCase()).put(ah.getNameattr(), ah);
	}
	/**
	 * @param category
	 * @param ah
	 */
	public void removeAttributeExtend(String category, AttributeHandler ah){
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "remove extended attribute " + ah + " from category " + category);
		this.getAttrSaada(category.toUpperCase()).remove(ah.getNameattr());
	}
	
	/**
	 * @param category
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrSaada(String category){
		try{
			return this.attMap.get(category);
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrTableSaada(){
		try{
			return this.attMap.get(Category.explain(Category.TABLE));
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrImageSaada(){
		try{
			return this.attMap.get(Category.explain(Category.IMAGE));
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrEntrySaada(){
		try{
			return this.attMap.get(Category.explain(Category.ENTRY));
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrSpectreSaada(){
		try{
			return this.attMap.get(Category.explain(Category.SPECTRUM));
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrMiscSaada(){
		try{
			return this.attMap.get(Category.explain(Category.MISC));
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Map<String, AttributeHandler> getAttrFlatfileSaada(){
		try{
			return this.attMap.get(Category.explain(Category.FLATFILE));
		}catch(Exception e){
			return null;
		}
	}
	
	 /**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		 Database.init("ThreeXMM");
		 new CollectionAttributeExtend(Database.getRoot_dir());
		Database.close();
	 }
}

