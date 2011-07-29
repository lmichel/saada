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
import java.io.FileOutputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class SaadaDBXML {
  private static String separ = System.getProperty("file.separator");
  public SaadaDBXML() {
  }

  public static void save(saadadb.configuration.SaadaDBConf config_db, String SAADA_HOME) throws Exception
  {
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
      Document doc = docbuilder.newDocument();


      Element composant_root = doc.createElement("saada_db");
      Element node_database = doc.createElement("database");
      Element Value_name = doc.createElement("name");
      Value_name.appendChild(doc.createTextNode(config_db.getNamedb()));
      node_database.appendChild(Value_name);

      Value_name = doc.createElement("administrator");
      Value_name.appendChild(doc.createTextNode(config_db.getAdministrator()));
      node_database.appendChild(Value_name);

      Value_name = doc.createElement("password");
      Value_name.appendChild(doc.createTextNode(config_db.getPassword()));
      node_database.appendChild(Value_name);

      Value_name = doc.createElement("sql_driver");
      Value_name.appendChild(doc.createTextNode(config_db.getSql_driver()));
      node_database.appendChild(Value_name);

      Value_name = doc.createElement("jdbc_url");
      Value_name.appendChild(doc.createTextNode(config_db.getJdbc_url()));
      node_database.appendChild(Value_name);

      Value_name = doc.createElement("db_root_dir");
      Value_name.appendChild(doc.createTextNode(config_db.getDb_root_dir()));
      node_database.appendChild(Value_name);
      Value_name = doc.createElement("repository_root_dir");
      Value_name.appendChild(doc.createTextNode(config_db.getRepository_root_dir()));
      node_database.appendChild(Value_name);

      composant_root.appendChild(node_database);

      Element node_web = doc.createElement("interface_web");
      Value_name = doc.createElement("url_root");
      Value_name.appendChild(doc.createTextNode(config_db.getUrl_root()));
      node_web.appendChild(Value_name);
      composant_root.appendChild(node_web);

      Element node_coord = doc.createElement("coordinate");

      Value_name = doc.createElement("system");
      Value_name.appendChild(doc.createTextNode(config_db.getSystem()));
      node_coord.appendChild(Value_name);
      Value_name = doc.createElement("equinox");
      Value_name.appendChild(doc.createTextNode(config_db.getEquinox()));
      node_coord.appendChild(Value_name);

      composant_root.appendChild(node_coord);


     try{

                      TransformerFactory xformFactory = TransformerFactory.newInstance();
                      Transformer idTransform = xformFactory.newTransformer();
                      doc.appendChild(composant_root);
                      DOMSource source = new DOMSource (doc);


                      if( doc.getDoctype() != null) {

                              String systemValue = (new File (doc.getDoctype().getSystemId())).getName();
                              idTransform.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM, systemValue);
                      }
                      else {
                              idTransform.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "saadadb.dtd");
                      }
                      idTransform.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
                      idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
                      idTransform.setOutputProperty(OutputKeys.METHOD, "xml");
                      idTransform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                      idTransform.setOutputProperty(OutputKeys.STANDALONE, "no");

                      idTransform.setOutputProperty(OutputKeys.VERSION, "1.0");
                      idTransform.transform( source, new StreamResult(new FileOutputStream(SAADA_HOME+separ+"config"+separ+"saadadb.xml",false)) );
       } catch (Exception e) {
                           e.getMessage();
                           Messenger.printStackTrace(e);
                     }


}


  public static void add_list_saadadb(String name, String home, String SAADA_HOME) throws Exception
  {
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
      String namelist=SAADA_HOME+separ+"config"+separ+"list_saadadb.xml";
      Document doc = docbuilder.parse(new File(namelist));

      Element root=doc.getDocumentElement();
      Element saada_db = doc.createElement("saada_db");
      saada_db.setAttribute("name",name);
      saada_db.setAttribute("db_home",home);
      root.appendChild(saada_db);

     try{

                      TransformerFactory xformFactory = TransformerFactory.newInstance();
                      Transformer idTransform = xformFactory.newTransformer();

                      DOMSource source = new DOMSource (doc);


                      if( doc.getDoctype() != null) {

                              String systemValue = (new File (doc.getDoctype().getSystemId())).getName();
                              idTransform.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM, systemValue);
                      }
                      else {
                             // idTransform.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "saadadb.dtd");
                      }
                      idTransform.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
                      idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
                      idTransform.setOutputProperty(OutputKeys.METHOD, "xml");
                      idTransform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                      idTransform.setOutputProperty(OutputKeys.STANDALONE, "no");

                      idTransform.setOutputProperty(OutputKeys.VERSION, "1.0");
                      idTransform.transform( source, new StreamResult(new FileOutputStream(SAADA_HOME+separ+"config"+separ+"list_saadadb.xml",false)) );
       } catch (Exception e) {
                           e.getMessage();
                           Messenger.printStackTrace(e);
                     }


     }

  public static void remove_list_saadadb(String name, String home, String SAADA_HOME) throws Exception
  {
      DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
      String namelist=SAADA_HOME+separ+"config"+separ+"list_saadadb.xml";
      Document doc = docbuilder.parse(new File(namelist));

      Element root = doc.getDocumentElement();
      NodeList nodes=root.getElementsByTagName("saada_db");
      //System.out.println(root.getAttribute("name"));
      for (int i=0; i<nodes.getLength();i++)
      {
        Node node =nodes.item(i);
        Element res =(Element)node;
        if (res.getAttribute("name").equals(name))
         root.removeChild(node);
        }
     try{

                      TransformerFactory xformFactory = TransformerFactory.newInstance();
                      Transformer idTransform = xformFactory.newTransformer();

                      DOMSource source = new DOMSource (doc);


                      if( doc.getDoctype() != null) {

                              String systemValue = (new File (doc.getDoctype().getSystemId())).getName();
                              idTransform.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM, systemValue);
                      }
                      else {
                             // idTransform.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "saadadb.dtd");
                      }
                      idTransform.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
                      idTransform.setOutputProperty(OutputKeys.INDENT, "yes");
                      idTransform.setOutputProperty(OutputKeys.METHOD, "xml");
                      idTransform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                      idTransform.setOutputProperty(OutputKeys.STANDALONE, "no");

                      idTransform.setOutputProperty(OutputKeys.VERSION, "1.0");
                      idTransform.transform( source, new StreamResult(new FileOutputStream(SAADA_HOME+separ+"config"+separ+"list_saadadb.xml",false)) );
       } catch (Exception e) {
                           e.getMessage();
                           Messenger.printStackTrace(e);
                     }


     }

  public static Hashtable getListSaadaDB(String list_saadadb_xmm)
   {
     Hashtable vt=new Hashtable();
     try
     {
       DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
       Document doc = docbuilder.parse(new File(list_saadadb_xmm));
       Element root = doc.getDocumentElement();
       NodeList nodes=root.getElementsByTagName("saada_db");
       //System.out.println(root.getAttribute("name"));
       for (int i=0; i<nodes.getLength();i++)
       {
         Node node =nodes.item(i);
         Element res =(Element)node;
         vt.put(res.getAttribute("name"),res.getAttribute("db_home"));
         }
       return vt;
     }catch (Exception e)
     {
       Messenger.printStackTrace(e);
       return null;
     }
   }

}
  
