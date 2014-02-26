package saadadb.configuration;

/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */

import java.io.File;
import java.io.FileWriter;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import saadadb.collection.Category;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;

public class ParserRelationConf extends DefaultHandler {
  static private FileWriter dest;
  static String str="";
  static String _name="",_type="";
  static String key="";
  static String name_attr="";

  static RelationConf saadaConf =new RelationConf();

  static TreeMap<String, RelationConf> product=new TreeMap<String, RelationConf>();

  /** * @version $Id: ParserRelationConf.java 118 2012-01-06 14:33:51Z laurent.mistahl $

  * constructor object ParserRelationConf
  */
  public ParserRelationConf() {
     product=new TreeMap<String, RelationConf>();
     product.clear();
     name_attr="";
     String _name="",_type="";String str="";
  }


  /**
  * return a vector content the objects RelationConfs
  * @param nameXML  name of file xml of declaration
  * @exception SaadaException
  */
  public  TreeMap<String, RelationConf> ParserRelation(String nameXML)  throws FatalException
  {

	  try
	  {
		  DefaultHandler handler = new ParserRelationConf();
		  SAXParserFactory factory = SAXParserFactory.newInstance();
		  factory.setValidating(true);
		  SAXParser saxParser = factory.newSAXParser();
		  saxParser.parse( new File(nameXML), handler );
		  // ValidationRelationConf.validVectorRelationConf(product,meta);
		  return product;
		  
	  }
	  catch (Exception e)
	  { 
		  e.printStackTrace();
	  FatalException.throwNewException(SaadaException.FILE_FORMAT, e);
	  }
	  return null;
	  
  }

  @Override
public void error(SAXParseException e)
  throws SAXParseException
  {
    throw e;
  }

  @Override
public void startDocument ()
  throws SAXException {


  }

  @Override
public void endDocument ()
  throws SAXException {


  }

  @Override
public void startElement (String namespaceURI,
                             String simpleName,
                             String qualifiedName,
                             Attributes attrs)
    throws SAXException {
    String  elementName = simpleName;
    if (elementName.equals(""))
      elementName = qualifiedName;
    str=elementName; 

    if (attrs != null)
    {
      for (int i = 0; i < attrs.getLength(); i++) {

        String attName = attrs.getLocalName(i);
        name_attr = attName;
        if (attName.equals("")) {
          attName = attrs.getQName(i);

        }

      }
      // traitement des attributes ici
      try {
    	  if (elementName.equals("primary_coll"))
    	  {
    		  saadaConf.setColPrimary_name(attrs.getValue("name").trim());
    		  saadaConf.setColPrimary_type(Category.getCategory(attrs.getValue("type").trim()));
    	  }
    	  
    	  else if (elementName.equals("secondary_coll"))
    	  {
    		  saadaConf.setColSecondary_name(attrs.getValue("name").trim());
    		  saadaConf.setColSecondary_type(Category.getCategory(attrs.getValue("type").trim()));
    	  }
      } catch(SaadaException e){
    	  throw new SAXException(e.getMessage());
       }
      if (elementName.equals("qualifier"))
      {
        saadaConf.setQualifier(attrs.getValue("name").trim(),attrs.getValue("type").trim());
      }
 
    }
  }

  @Override
public void endElement (String namespaceURI,
                           String simpleName,
                           String qualifiedName)
  throws SAXException {
    String  elementName = simpleName;
    if (elementName.equals(""))
      elementName = qualifiedName;

    key=elementName;

    if (elementName.equals("N_M"))
    {
      if (!validSyntax(saadaConf))
      {
        System.exit(1);
      }
      product.put(saadaConf.getNameRelation(), saadaConf);
      saadaConf =new RelationConf();
    }

  }

  //�crire les values
  @Override
public void characters (char buf [], int offset, int len)
  throws SAXException {
    String s = new String(buf, offset, len);
 
    if (s.equals("")) {
     // throw new SAXException(" value of " +str  +  " empty in file  configuration");
    }
    else if (str.equals("relation_name"))  {
     saadaConf.setNameRelation(s.trim());
    }
    else if (str.equals("description"))  {
        saadaConf.setDescription(s.trim());
       }
   else if (str.equals("class_name")) {
      saadaConf.setClass_name(s.trim());
    }
     else if (str.equals("query")) {
        saadaConf.appendToQuery(s);
    }


  }

  public boolean validSyntax(RelationConf relation)
  {
    boolean loi=true;
    if (relation.getColPrimary_name() ==  null || relation.getColPrimary_name().equals(""))
    {
      System.out.println("ERROR :Name of collection primary is null");
      loi=false;
    }

    if (!Category.isValid(relation.getColPrimary_type()) )
    {
      System.out.println("ERROR :Type of collection primary  is null");
      loi=false;
    }

    if (relation.getColSecondary_name() == null || relation.getColSecondary_name().equals(""))
    {
      System.out.println("ERROR :Name of collection secondary is null");
      loi=false;
    }
    if (!Category.isValid(relation.getColSecondary_type()))
    {
      System.out.println("ERROR :type of collection  is null");
      loi=false;
    }




    return loi;
  }


}
  
