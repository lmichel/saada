package saadadb.configuration; 

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.util.Messenger;

public class CollectionAttributeExtend extends Hashtable<String,LinkedHashMap<String, String>>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String separ = System.getProperty("file.separator");
	public static final String ATTR_NAME = "name";
	public static final String ATTR_TYPE = "type";
	public static final String TAG_NAME_ATTR = "attr";
	public static final String TAG_NAME_LIST = "list";
	public static final String document = separ+ "config" + separ+ "collection_attr";	
	
	public CollectionAttributeExtend() throws Exception{
		boolean isFileConfig = true;
		Document doc = null;
		for(int i=1; i<Category.NB_CAT; i++){
			put(Category.explain(i), new LinkedHashMap<String, String>());
		}
		try{
			/*
			 * Must use a file but not a name, because Java 1.6 cannot understand it for Windows path.
			 * c:\... is taken as a URL bug #6506304
			 */
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(Database.getRoot_dir()+document+".xml"));
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
					getAttrSaada(res.getAttribute(ATTR_NAME)).put(res_ch.getAttribute(ATTR_NAME), res_ch.getAttribute(ATTR_TYPE));
				}
			}
		}
	}
	
	public LinkedHashMap<String, String> getAttrSaada(String type){
		try{
			return (LinkedHashMap<String, String>) get(type);
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, String> getAttrTableSaada(){
		try{
			return (LinkedHashMap<String, String>)get(Category.explain(Category.TABLE));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, String> getAttrImageSaada(){
		try{
			return (LinkedHashMap<String, String>)get(Category.explain(Category.IMAGE));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, String> getAttrEntrySaada(){
		try{
			return (LinkedHashMap<String, String>)get(Category.explain(Category.ENTRY));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, String> getAttrSpectreSaada(){
		try{
			return (LinkedHashMap<String, String>)get(Category.explain(Category.SPECTRUM));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, String> getAttrMiscSaada(){
		try{
			return (LinkedHashMap<String, String>)get(Category.explain(Category.MISC));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, String> getAttrFlatfileSaada(){
		try{
			return (LinkedHashMap<String, String>)get(Category.explain(Category.FLATFILE));
		}catch(Exception e){
			return null;
		}
	}
}

