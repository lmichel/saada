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
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class CollectionAttributeExtend extends Hashtable<String,LinkedHashMap<String, AttributeHandler>>{
	
	/**
	 *  * @version $Id$

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
	
	public CollectionAttributeExtend() throws Exception{
		boolean isFileConfig = true;
		Document doc = null;
		for(int i=1; i<Category.NB_CAT; i++){
			put(Category.explain(i), new LinkedHashMap<String, AttributeHandler>());
		}
		try{
			/*
			 * Must use a file but not a name, because Java 1.6 cannot understand it for Windows path.
			 * c:\... is taken as a URL bug #6506304
			 */
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(Database.getRoot_dir()+document+".xml"));
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
					getAttrSaada(res.getAttribute(ATTR_NAME)).put(res_ch.getAttribute(ATTR_NAME), ah);
				}				
			}
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrSaada(String type){
		try{
			return (LinkedHashMap<String, AttributeHandler>) get(type);
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrTableSaada(){
		try{
			return (LinkedHashMap<String, AttributeHandler>)get(Category.explain(Category.TABLE));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrImageSaada(){
		try{
			return (LinkedHashMap<String, AttributeHandler>)get(Category.explain(Category.IMAGE));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrEntrySaada(){
		try{
			return (LinkedHashMap<String, AttributeHandler>)get(Category.explain(Category.ENTRY));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrSpectreSaada(){
		try{
			return (LinkedHashMap<String, AttributeHandler>)get(Category.explain(Category.SPECTRUM));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrMiscSaada(){
		try{
			return (LinkedHashMap<String, AttributeHandler>)get(Category.explain(Category.MISC));
		}catch(Exception e){
			return null;
		}
	}
	
	public LinkedHashMap<String, AttributeHandler> getAttrFlatfileSaada(){
		try{
			return (LinkedHashMap<String, AttributeHandler>)get(Category.explain(Category.FLATFILE));
		}catch(Exception e){
			return null;
		}
	}
	
	 public static void main(String[] args) throws Exception {
		 Database.init("ThreeXMM");
		 new CollectionAttributeExtend();
	 }
}

