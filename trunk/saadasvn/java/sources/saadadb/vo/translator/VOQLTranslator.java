package saadadb.vo.translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import saadadb.collection.Category;
import saadadb.collection.EntrySaada;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;
import saadadb.vo.formator.QueryInfos;
import saadadb.vo.formator.VOTableField;

/**
 * Parser which translates a VOQL/x file into a SaadaQL/s String
 *
 * This parser implements the 1.01 version of ADQL which can be found at :
 *         http://www.ivoa.net/Documents/WD/ADQL/ADQL-20050624.doc
 * All the constructs implemented by the SQLtoADQL Translator, found at 
 *          http://openskyquery.net/AdqlTranslator/ADQLTrans.asmx?op=SQLtoADQL
 * have been taken into account in this parser. We used this translator to produce ADQL/x queries 
 * in order to test the parser.
 * It means that the parts of the ADQL grammar that are not implemented in the SQLtoADQL Translator
 * are not implemented too in this parser.
 */
public class VOQLTranslator extends VOTranslator {
	
	private Document doc;
	
	// buffers to produce the SaadaQL query
	private String wherePositionBuffer;
	private String whereAttributeClassBuffer;
	private String whereAttributeSaadaBuffer;
	private String whereRelationBuffer;
	private String buffer = "";
	
	private boolean outOfWhereClause = true; // flag for parsing
	
	private Hashtable attributesTable = new Hashtable(200); 
	
	private String[] lst = {"rand", "atan2", "log10", "power"};
	public ArrayList<String> unsupportedFunctions = new ArrayList<String>(Arrays.asList(lst));
	
	public boolean warningOn = false;
	
	// Union types
	public static final int UT_POSITION = 1, UT_SAADA = 2, UT_CLASS = 3, UT_RELATION = 4, UT_MIXED = 5, UT_UNKNOWN = 6;
	
	public static final int NO_CONDITION = 0, AND_CONDITION = 1, OR_CONDITION = 2;
	
	/**
	 * Constructor
	 * @param xmlString a String containing the ADQL/x query
	 */
	public VOQLTranslator(String xmlString) {
		this.xmlString = xmlString;
		Messenger.printMsg(Messenger.DEBUG, "Server : ADQL String received : " + xmlString);
		
		queryInfos = new QueryInfos();
		saadaString = "";
	}
	
	
	/**
	 * Constructor
	 * @param file a File containing the ADQL/x query
	 */
	public VOQLTranslator(File file) throws Exception {
		String fileName = file.getPath();
		try {
			this.xmlString = getXMLFromFile(fileName);
		} catch(IOException e) {
			QueryException.throwNewException(SaadaException.FILE_ACCESS, e);
		}
		queryInfos = new QueryInfos();
		saadaString = "";
	}
	
	/**
	 * This method should only be called after a call to parseXXX() or translate()
	 */
	public String getTranslation() {
		return saadaString;
	}
	
	/**
	 * The parsing function
	 * @return a SaadaQL/s String if parsing succeded.
	 */
	@Override
	public String translate() throws SaadaException {
		
		saadaString = "";
		wherePositionBuffer = "";
		whereAttributeClassBuffer = "";
		whereAttributeSaadaBuffer = "";
		whereRelationBuffer = "";
		
		initParseFile();
		// Parsing begins :
		parseRestrictClause();
		parseAllowClause();
		parseSelectClause();
		parseIntoClause();
		parseFromClause();
		parseWhereClause();
		parseOrderByClause();
		parseGroupByClause();
		parseHavingClause();
		
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, queryInfos.toString());
		return saadaString;
	}
	
	/**
	 * Load the XML file in memory using a DocumentBuilder
	 */
	public void initParseFile() throws SaadaException {
		File recup = null;
		try {
			recup = File.createTempFile("tmp" + System.currentTimeMillis(), ".xml");
			FileWriter writer = new FileWriter(recup);
			writer.write(xmlString);
			writer.close();
		} catch (IOException e) {
			QueryException.throwNewException(SaadaException.FILE_FORMAT, e);
		} 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(true);
		dbf.setNamespaceAware(true);
		dbf.setIgnoringElementContentWhitespace(false);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler( new org.xml.sax.ErrorHandler() {
				// we ignore fatal errors
				public void fatalError(SAXParseException err) throws SAXException {
					Messenger.printMsg(Messenger.DEBUG, "** Fatal Error , line " + err.getLineNumber() + ", uri " + err.getSystemId());
					Messenger.printMsg(Messenger.DEBUG, "   " + err.getMessage());
				}
				// we ignore validation errors as well
				public void error(SAXParseException err) throws SAXParseException {
					Messenger.printMsg(Messenger.DEBUG, "** Validation Error , line " + err.getLineNumber() + ", uri " + err.getSystemId());
					Messenger.printMsg(Messenger.DEBUG, "   " + err.getMessage());
					// We don't transmit parsing errors, so that it is not interrupted.
				}
				// we dump warnings too
				public void warning(SAXParseException err) throws SAXParseException {
					Messenger.printMsg(Messenger.DEBUG, "** Warning , line " + err.getLineNumber() + ", uri " + err.getSystemId());
					Messenger.printMsg(Messenger.DEBUG, "   " + err.getMessage());
				}
			} );
			
			doc = db.parse(recup);
		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
		recup.delete();
	}
	
	/*
	 All the parsing methods return their type as an integer. See Union types.
	 The type can only correspond to one of the 4 WhereXXX SaadaQL clauses, 
	 or to a mixed type (due to unions or intersections).
	 It allows to perform restrictive checks on unions, necessary because of the structure of a SaadaQL query.
	 */
	
	/**
	 * Parsing of the Restrict Clause ( 'top' keyword)
	 */
	public void parseRestrictClause() {
		debugPrint("Restrict element");
		NodeList list = doc.getElementsByTagName("Restrict");
		for (int i = 0; i < list.getLength(); ++i) {
			NamedNodeMap att = list.item(i).getAttributes();
			for(int k = 0 ; k < att.getLength() ; ++k) { 
				if(att.item(k).getNodeName().equals("Top")) {
					queryInfos.setTopRestriction(Integer.parseInt(att.item(k).getNodeValue()));
				}
			}
		}
	}
	
	/**
	 * Parsing of the 'all' and 'distinct' keywords after 'select'
	 * @throws DOMException 
	 * @throws QueryException 
	 */
	public void parseAllowClause() throws QueryException, DOMException  {
		debugPrint("Allow element");
		String keyword;
		NodeList list = doc.getElementsByTagName("Allow");
		for (int i = 0; i < list.getLength(); ++i) {
			NamedNodeMap att = list.item(i).getAttributes();
			for(int k = 0 ; k < att.getLength() ; ++k) { 
				if (att.item(k).getNodeName().equals("Option")) {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,att.item(k).getNodeValue());
				}
			}
		}
	}
	
	
	/**
	 * Parsing of the Having Clause ('having' keyword)
	 */
	public void parseHavingClause() throws SaadaException {
		debugPrint("Having element");
		NodeList list = doc.getElementsByTagName("Having");
		boolean firstHaving = true;
		for (int i = 0; i < list.getLength(); ++i) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Having");
			/*NodeList children = list.item(i).getChildNodes();
			 if (firstHaving == true) {
			 saadaString += " having ";
			 firstHaving = false;
			 }
			 for (int j = 0 ; j < children.getLength(); ++j) {
			 if (children.item(j).getNodeName().equals("Condition")) {
			 parseCondition(children.item(j), NO_CONDITION);
			 }
			 }*/
		}
	}
	
	
	public void parseSelectClause() throws SaadaException {
		debugPrint("Select");
		String name;
		boolean starMet = false;
		NodeList list = doc.getElementsByTagName("SelectionList");
		for (int i = 0; i < list.getLength(); ++i) {
			boolean item_found = false;
			NodeList children = list.item(i).getChildNodes();
			for (int j = 0 ; j < children.getLength(); ++j) {
				if (children.item(j).getNodeName().equals("Item")) {
					// Named fields will not be treated by the parser, but stored so that 
					// the parsing back from SaadaQL results to VOTable can use them. 
					if (item_found == false ) {
						// For the moment, we just implement ENTRY from SaadaQL
						saadaString += "Select ENTRY ";
						queryInfos.setCategory(Category.ENTRY);
					}
					item_found = true;
					NamedNodeMap att = children.item(j).getAttributes();
					for(int k = 0 ; k < att.getLength() ; ++k) { 
						if (att.item(k).getNodeName().equals("xsi:type")) {
							name = att.item(k).getNodeValue();
							if (name.equals("allSelectionItemType")) {
								// we translate 'select *' to 'select ENTRY'
								
							} else if (name.equals("columnReferenceType")) {
								for(int l = 0 ; l < att.getLength() && starMet == false; ++l) { 
									if(att.item(l).getNodeName().equals("Name")) {
										name = att.item(l).getNodeValue();
										// we store the select attribute names for further use
										if (name.equals("*")) {
											starMet = true;
										} else {
											queryInfos.addSelectAttribute(name);
										}
									}
								}
								
							} else if (name.equals("aggregateFunctionType")) {
								QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Aggregate functions");
								
							} else {
								QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,name);
							}
						}
					}
				}
			}
		}
		if (starMet == true) {
			queryInfos.resetSelectAttributes();
		}
	}
	
	
	public void parseIntoClause() throws SaadaException {
		debugPrint("Into");
		NodeList list = doc.getElementsByTagName("InTo");
		if (list.getLength() > 0) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"'into' ");
		}
	}
	
	
	/*
	 * Parsing of ADQL 'From' clause
	 */
	public void parseFromClause() throws SaadaException {
		debugPrint("From element");
		String attribut;
		int nbTableItems = 0;
		NodeList list = doc.getElementsByTagName("From");
		for (int i = 0; i < list.getLength(); ++i) {
			NodeList children = list.item(i).getChildNodes();
			for(int j = 0; j < children.getLength(); ++j) {
				// 'Table' element is necessary in any correct SaadaQL query.
				// Note: this element refers to a SQL table and not necessarily to an astronomical table.
				if (children.item(j).getNodeName().equals("Table")) {
					if (nbTableItems > 0) {
						QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Multiple Table items");
					}
					nbTableItems++;
					NamedNodeMap att = children.item(j).getAttributes();
					for(int k = 0 ; k < att.getLength() ; ++k) { 
						attribut = att.item(k).getNodeName();
						if (attribut.equals("Alias")) {
							// alias is not used in SaadaQL
							
						} else if(attribut.equals("Name")) {
							/*
							 Syntax used :
							 Name = any                     ==> Select ENTRY From * In *
							 Name = [any]                   ==> Select ENTRY From * In *
							 Name = [coll1,coll2,coll3]     ==> Select ENTRY From * In col1,coll2,coll3
							 Name = [coll1(class1, class2)] ==> Select ENTRY From class1,class2 In col1
							 */
							parseTableItem(att.item(k));
							
						} else if (attribut.equals("xsi:type")) {
							if( att.item(k).getNodeValue().equals("tableType") == false ) {
								QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Bad type : xsi:type: " + att.item(k).getNodeValue());
							}
						}
					}
				}
			}
		}
		if (nbTableItems == 0) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Missing 'Table' tag in 'From' clause");
		}
		// If we do a query on more than one class, we must not have Class attributes in the select clause
		ArrayList selectAttr = queryInfos.getSelectAttributes();
		int type;
		for(int i = 0; i < selectAttr.size(); ++i) {
			String name = (String)selectAttr.get(i);
			type = getAttributeSaadaType(name);
			if (type == VOTableField.T_CLASS && queryInfos.getQueryTarget() != QueryInfos.ONE_COLL_ONE_CLASS) {
				QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"You can select Class attributes only in queries on one class.");
			}
		}
	}
	
	
	/**
	 * @return the type of Saada Attribute. See VOTableField's constants.
	 * @param attribute the attribute's name 
	 */
	public int getAttributeSaadaType(String attribute) throws SaadaException {
		// We first get all the saada or collection attributes
		if (attributesTable.isEmpty()) {
			// we build, once and for all, the list of available attributes.
			
			for( String name: Database.getCachemeta().getAtt_extend_entry_names() ) {
				Messenger.printMsg(Messenger.TRACE, "Adding user-defined attribute : " + name);
				attributesTable.put(name, new Integer(VOTableField.T_USER_DEFINED));
			}
			// We now add the collection attributes
			try {
				EntrySaada entryClass = new EntrySaada();
				Field[] fields = entryClass.getClass().getFields();
				for(int i = 0; i < fields.length; ++i) {
					String name = fields[i].getName();
					Messenger.printMsg(Messenger.TRACE, "Adding collection attribute : " + name);
					attributesTable.put(name, new Integer(VOTableField.T_SAADA));
				}
			} catch (Exception e) {
				QueryException.throwNewException(SaadaException.METADATA_ERROR,"Could not get the Saada collection attributes" + e);
			}
			
		}
		if (attributesTable.containsKey(attribute)) {
			// This is either a Saada attribute or a collection attribute
			return ((Integer)attributesTable.get(attribute)).intValue();
		} else {
			if (queryInfos.getQueryTarget() != QueryInfos.ONE_COLL_ONE_CLASS) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Class attribute not allowed in queries on more than one class : " + attribute);
				
			} else {
				// Finally, we check if this is a class attribute
				ResultSet result = null;
				boolean res = false;
				try {
					String query = "select name_attr from saada_metaclass_entry where name_class='" + nameClass + "' and name_attr = '" + attribute.toLowerCase() + "';";
					SQLQuery squery = new SQLQuery();
					result = squery.run(query);
					res = result.next();
				} catch (Exception e) {
					QueryException.throwNewException(SaadaException.INTERNAL_ERROR,"Running query : " + e.toString());
				}   
				if (res == false) {
					QueryException.throwNewException(SaadaException.METADATA_ERROR,"Attribute '" + attribute + "' doesn't exist in class " + nameClass);
				} else {
					try {
						Messenger.printMsg(Messenger.TRACE, "Request result : " + result.getString(1));
					} catch (Exception e) {
						QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Getting query result : " + e.toString());
					} 
				}
			}
			return VOTableField.T_CLASS;
		}
	}
	
	/**
	 * Parsing of the 'Where' clause.
	 * As SaadaQL's 'Where' clause is organised in 4 items, we use 4 buffers
	 * in which we put the relevant information.
	 */
	public void parseWhereClause() throws SaadaException {
		debugPrint("Where element");
		int type;
		NodeList list = doc.getElementsByTagName("Where");
		outOfWhereClause = false;
		for (int i = 0; i < list.getLength(); i++) {
			NodeList children = list.item(i).getChildNodes();
			for(int j = 0 ; j < children.getLength(); j++) {
				if(children.item(j).getNodeName().equals("Condition")) {
					type = parseCondition(children.item(j), NO_CONDITION);
				}
			}
		}
		// we add the parsed SaadaQL conditions
		if (wherePositionBuffer.length() > 0) {
			wherePositionBuffer += "}";
		}
		saadaString += wherePositionBuffer;
		
		if (whereAttributeClassBuffer.length() > 0) {
			whereAttributeClassBuffer += "}";
		}
		saadaString += whereAttributeClassBuffer;
		
		if (whereAttributeSaadaBuffer.length() > 0) {
			whereAttributeSaadaBuffer += "}";
		}
		saadaString += whereAttributeSaadaBuffer;
		
		saadaString += whereRelationBuffer; // not implemented yet
		outOfWhereClause = true;
	}
	
	/*
	 * The following methods explore locally the DOM tree's nodes
	 */
	
	private void parseGroupByClause() throws SaadaException {
		debugPrint("GroupBy");
		String attribut;
		boolean firstGroupBy = true;
		NodeList list = doc.getElementsByTagName("GroupBy");
		for (int i = 0; i < list.getLength(); ++i) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Group by");
			/*NodeList children = list.item(i).getChildNodes();
			 for(int j = 0; j < children.getLength(); ++j) {
			 if (children.item(j).getNodeName().equals("Column")) {
			 if (firstGroupBy == true) {
			 saadaString += "\n group by ";
			 firstGroupBy = false;
			 } else {
			 saadaString += ", ";
			 }
			 NamedNodeMap att = children.item(j).getAttributes();
			 for(int l = 0 ; l < att.getLength() ; ++l) {
			 if (att.item(l).getNodeName().equals("Name")) {
			 saadaString += att.item(l).getNodeValue();
			 }
			 }
			 }
			 }*/
		}
	}
	
	
	private void parseOrderByClause() throws SaadaException {
		debugPrint("Order By element");
		String attribut = "";
		boolean firstItem = true;
		NodeList list = doc.getElementsByTagName("OrderBy");
		for (int i = 0; i < list.getLength(); ++i) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Order by");
			/*NodeList children = list.item(i).getChildNodes();
			 for(int j = 0; j < children.getLength(); ++j) {
			 if (children.item(j).getNodeName().equals("Item")) {
			 NodeList children2 = children.item(j).getChildNodes();
			 for(int k = 0; k < children2.getLength(); ++k) {
			 if (children2.item(k).getNodeName().equals("Expression")) {
			 NamedNodeMap att = children2.item(k).getAttributes();
			 for(int l = 0 ; l < att.getLength() ; ++l) {
			 if (att.item(l).getNodeName().equals("Name")) {
			 if (firstItem == false) {
			 attribut += ", ";
			 }
			 firstItem = false;
			 attribut += att.item(l).getNodeValue();
			 }
			 }
			 } else if (children2.item(k).getNodeName().equals("Order")) {
			 NamedNodeMap att = children2.item(k).getAttributes();
			 for(int l = 0 ; l < att.getLength() ; ++l) {
			 if (att.item(l).getNodeName().equals("Direction")) {
			 attribut += " " + att.item(l).getNodeValue().toLowerCase();
			 }
			 }
			 }
			 }
			 }
			 }*/
		}
		/*if (list.getLength() > 0 && attribut.length() == 0) {
		 ParsingException.throwException("Parsing of 'OrderBy' item failed");
		 }
		 queryInfos.addOrderAttribute(attribut);
		 */
	}
	
	
	private int parseRegionSearch(Node start_node) throws SaadaException {
		debugPrint("Region");
		String name, sname;
		int count = 0;
		if (wherePositionBuffer.length() == 0) {
			wherePositionBuffer += "\n WherePosition {";
		} else {
			wherePositionBuffer += ", ";
		}
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			if (children.item(i).getNodeName().equals("Region")) {
				NamedNodeMap att = children.item(i).getAttributes();
				for(int k = 0 ; k < att.getLength() ; ++k) {
					if( att.item(k).getNodeName().equals("xsi:type")) {
						sname = att.item(k).getNodeValue();
						name = sname.substring(0, sname.indexOf(":"));
						if (sname.endsWith("circleType") || sname.endsWith("smallCircleType")) {
							parsePos(children.item(i), name);
						} else {
							QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE," 'Region' : " + sname);
						}
					}
				}
			}
		}
		return UT_POSITION;
	}
	
	private void parseTableItem(Node name) throws SaadaException  {
		debugPrint("TableItem :" + name);
		String s = name.getNodeValue();
		saadaString = this.parseTableItem(s);
	}
	
	private void parsePos(Node start_node, String name) throws SaadaException {
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals(name + ":Center")){
				parseType(children.item(i));
			} else {
				if (children.item(i).getNodeName().equals(name + ":Radius")) {
					parseRadius(children.item(i));
				}
			}
		}
	}
	
	private void parseRadius(Node start_node) {
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			String radius = children.item(i).getNodeValue();
			wherePositionBuffer += ", " + radius + ", J2000, FK5)";
		}
	}
	
	
	private void parseType(Node start_node) throws SaadaException {
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Pos2Vector")){
				parseIsInCircle(children.item(i));
			} else if (children.item(i).getNodeName().equals("Pos3Vector")){
				QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Circle Cartesian : Pos3Vector");
			}
		}
	}
	
	private void parseIsInCircle(Node start_node) {
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("CoordValue")){
				wherePositionBuffer += "isInCircle(";
				parseCoord(children.item(i));
			}
		}
	}
	
	private void parseCoord(Node start_node) {
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Value")){
				parseValue(children.item(i));
			}
		}
	}
	
	private void parseValue(Node start_node) {
		int nb_values = 0;
		String value;
		NodeList children = start_node.getChildNodes();
		wherePositionBuffer += "\" ";
		for (int i = 0; i < children.getLength(); ++i) {
			if(children.item(i).getNodeName().equals("double")) {
				value = children.item(i).getFirstChild().getNodeValue();
				if (nb_values > 0 && !value.startsWith("+") && !value.startsWith("-")) {
					wherePositionBuffer += "+";
				}
				wherePositionBuffer += value;
				if (nb_values ==0) {
					wherePositionBuffer += " ";
					nb_values++;
				}
			}
		}
		wherePositionBuffer += "\"";
	}
	
	/**
	 *
	 */
	private int parseComparison(Node start_node) throws SaadaException {
		debugPrint("comparison");
		int type1 = 0, type2 = 0;
		String comparisonOperator = "";
		String comparison = "";
		NamedNodeMap att = start_node.getAttributes();
		for(int k = 0 ; k < att.getLength() ; ++k) {
			if(att.item(k).getNodeName().equals("Comparison")) {
				comparisonOperator = att.item(k).getNodeValue();
				// we tolerate the '<>' operator 
				break;
			}
		}
		if (comparisonOperator.length() == 0) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"No comparison operator.");
		}
		buffer += "(";
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Arg")) {
				if (type1 == 0) {
					type1 = parseArg(children.item(i));
					buffer += " " + comparisonOperator + " ";
				} else {
					type2 = parseArg(children.item(i));
				}
			}
		}
		if ((type1 == UT_CLASS && type2 == UT_SAADA) || (type2 == UT_CLASS && type1 == UT_SAADA)) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Comparison between Saada and Class attributes");
		}
		buffer += ")";
		if (type1 == UT_CLASS || type1 == UT_SAADA) {
			return type1;
		}
		if (type2 == UT_CLASS || type2 == UT_SAADA) {
			return type2;
		}
		if (type1 != type2) {
			return UT_MIXED;
		}
		return UT_UNKNOWN;
	}
	
	
	private int parseArg(Node start_node) throws SaadaException {
		debugPrint("Arg");
		String value;
		NamedNodeMap att = start_node.getAttributes();
		for(int j = 0 ; j < att.getLength() ; ++j) {
			if (att.item(j).getNodeName().equals("xsi:type")) {
				value = att.item(j).getNodeValue();
				if (value.equals("atomType")) {
					// we get the literal argument's value
					buffer += parseLiteralArgument(start_node);
					return UT_UNKNOWN;
					
				} else if (value.equals("columnReferenceType")) {
					return parseColumnName(start_node);
					
				} else if (value.equals("trigonometricFunctionType")) {
					return parseFunction(start_node);
					
				} else if (value.equals("mathFunctionType")) {
					return parseFunction(start_node);
					
				} else if (value.equals("aggregateFunctionType")) {
					return parseFunction(start_node);
					
				} else if (value.equals("unaryExprType")) {
					return parseUnaryExpression(start_node);
					
				} else if (value.equals("binaryExprType")) {
					return parseBinaryExpression(start_node);
					
				} else {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,value);
				}
			}
		}
		return UT_UNKNOWN;
	}
	
	private int parseUnaryExpression(Node start_node) throws SaadaException {
		NamedNodeMap att = start_node.getAttributes();
		for(int j = 0 ; j < att.getLength() ; ++j) {
			if (att.item(j).getNodeName().equals("Oper")) {
				buffer += att.item(j).getNodeValue();
			}
		}
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Arg")) {
				return parseArg(children.item(i));
			}
		}
		return UT_UNKNOWN;
	}
	
	private int parseBinaryExpression(Node start_node) throws SaadaException {
		int type1 = 0, type2 = 0, type;
		String operator = "";
		NamedNodeMap att = start_node.getAttributes();
		for(int j = 0 ; j < att.getLength() ; ++j) {
			if (att.item(j).getNodeName().equals("Oper")) {
				operator = att.item(j).getNodeValue();
			}
		}
		if (operator.length() == 0) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Operator unknown in binary Expression");
		}
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Arg")) {
				type = parseArg(children.item(i));
				if (type1 == 0) {
					type1 = type;
					buffer += operator;
				} else {
					type2 = type;
				}
			}
		}
		if (type2 == 0) {
			QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"Could find no second argument in binary expression");
		}
		if ((type1 == UT_CLASS && type2 == UT_SAADA) || (type2 == UT_CLASS && type1 == UT_SAADA)) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Use of Saada and Class attributes in the same expression");
		}
		if (type1 == UT_CLASS || type1 == UT_SAADA) {
			return type1;
		}
		if (type2 == UT_CLASS || type2 == UT_SAADA) {
			return type2;
		}
		if (type1 != type2) {
			return UT_MIXED;
		}
		return UT_UNKNOWN;
	}
	
	
	private int parseFunction(Node start_node) throws SaadaException {
		debugPrint("Function");
		String functionName;
		int type1 = 0, type2 = 0, type;
		NamedNodeMap att = start_node.getAttributes();
		for(int j = 0 ; j < att.getLength() ; ++j) {
			if (att.item(j).getNodeName().equals("Name")) {
				functionName = att.item(j).getNodeValue().toLowerCase();
				buffer += functionName + "(";
				
				if (unsupportedFunctions.contains(functionName)) {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Function : " + functionName);
					
				} else if (functionName.equals("ceiling")) {
					warning("Using 'ceil' in SaadaQL instead of 'ceiling' in VOQL.");
					functionName = "ceil";
				} else if (functionName.equals("truncate")) {
					warning("Using 'trunc' in SaadaQL instead of 'truncate' in VOQL.");
					functionName = "trunc";
				}
				
				NodeList children = start_node.getChildNodes();
				for (int i = 0; i < children.getLength(); ++i) {
					if (children.item(i).getNodeName().equals("Arg")) {
						if (type1 != 0) {
							buffer += ",";
						}
						type = parseArg(children.item(i));
						if (type1 == 0) {
							type1 = type;
						} else {
							type2 = type;
						}
					} else if (children.item(i).getNodeName().equals("Allow")) {
						QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"'Allow' : " + children.item(i).getNodeValue());
					}
				}
				buffer += ")";
				if (type2 == 0) {
					return type1;
				}
				if ((type1 == UT_CLASS && type2 == UT_SAADA) || (type2 == UT_CLASS && type1 == UT_SAADA)) {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Use of Saada and Class attributes in the same function");
				}
				if (type1 == UT_CLASS || type1 == UT_SAADA) {
					return type1;
				}
				if (type2 == UT_CLASS || type2 == UT_SAADA) {
					return type2;
				}
				if (type1 != type2) {
					return UT_MIXED;
				}
				return UT_UNKNOWN;
			}
		}
		return UT_UNKNOWN;
	}
	
	/**
	 * @return the value of the literal argument.
	 */
	private String parseLiteralArgument(Node start_node) throws SaadaException {
		debugPrint("literal argument");
		Node child;
		String result = "";
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			child = children.item(i);
			if (child.getNodeName().equals("Literal")) {
				NamedNodeMap att = child.getAttributes();
				boolean isString = false; // default value
				for(int k = 0 ; k < att.getLength() && isString == false; ++k) {
					if (att.item(k).getNodeName().equals("xsi:type") && att.item(k).getNodeValue().equals("stringType")) {
						isString = true;
					}
				}
				for(int k = 0 ; k < att.getLength() ; ++k) {
					if (att.item(k).getNodeName().equals("Value")) {
						String resultString = att.item(k).getNodeValue();
						if (isString == true) {
							// we "encapsulate" the result
							result += "'" + resultString + "'";
						} else {
							result += resultString;
						}
					}
				}
			} else if (child.getNodeName().equals("Unit")) {
				QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Units : " + child.getNodeValue());
			}
		}
		if (result.length() == 0) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Literal argument");
		}
		return result;
	}
	
	/**
	 * @param notLike true if the condition is 'not like'
	 */
	private int parseLikeCondition(Node start_node, boolean notLike) throws SaadaException {
		debugPrint("Like condition");
		int type = UT_UNKNOWN;
		Node child;
		buffer += "(";
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			child = children.item(i);
			if (child.getNodeName().equals("Arg")) {
				type = parseArg(child);
				
			} else if (child.getNodeName().equals("Pattern")) {
				String argString = "";
				argString = parseLiteralArgument(child);
				if (notLike == true) {
					buffer += " not like " + argString;
				} else {
					buffer += " like " + argString;
				}
			}
		}
		buffer += ")";
		return type;
	}
	
	
	private int parseColumnName(Node start_node) throws SaadaException {
		String columnName;
		int type;
		boolean validAttr;
		NamedNodeMap att = start_node.getAttributes();
		for(int i = 0 ; i < att.getLength() ; ++i) {
			if (att.item(i).getNodeName().equals("Name")) {
				columnName = att.item(i).getNodeValue();
				buffer += columnName;
				
				type = getAttributeSaadaType(columnName);
				if (type != VOTableField.T_CLASS) {
					return UT_SAADA;
					
				} else {
					return UT_CLASS;
				}
			}
		}
		return UT_UNKNOWN;
	}
	
	
	/**
	 * @return the type of union. See XXX_UNION constants
	 * @param condition the type of condition
	 */
	private int parseUnion(Node start_node, int condition) throws SaadaException {
		debugPrint("Union (OR)");
		String firstType, secondType;
		int type1 = 0, type2 = 0;
		Node child;
		// Unions are valid in SaadaQL only if they involve members of a same type
		// (PositionCondition OR PositionCondition, SaadaAttributeCondition OR SaadaAttributeCondition, etc).
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			child = children.item(i);
			if (child.getNodeName().equals("Condition")) {
				if (type1 == 0) {
					// first condition
					type1 = parseCondition(child, condition);
				} else {
					// second condition
					type2 = parseCondition(child, OR_CONDITION);
				}
			}
		}
		if (type1 != type2 || type1 == UT_MIXED || type1 == UT_UNKNOWN) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"union");
		}
		// the union is SaadaQL-compatible
		return type1;
	}
	
	
	private int parseCondition(Node start_node, int condition) throws SaadaException {
		debugPrint("condition");
		int tp = UT_UNKNOWN;
		String type;
		NamedNodeMap att = start_node.getAttributes();
		for(int i = 0 ; i < att.getLength() ; ++i) {
			if (att.item(i).getNodeName().equals("xsi:type")) {
				type = att.item(i).getNodeValue();
				if (type.equals("regionSearchType")) {
					tp = parseRegionSearch(start_node);
					
				} else if (type.equals("intersectionSearchType")) {
					tp = parseIntersection(start_node, condition);
					
				} else if (type.equals("unionSearchType")) {
					tp = parseUnion(start_node, condition);
					
				} else if (type.equals("comparisonPredType")) {
					tp = parseComparison(start_node);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("betweenPredType")) {
					tp = parseBetweenCondition(start_node, false);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("notBetweenPredType")) {
					tp = parseBetweenCondition(start_node, true);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("likePredType")) {
					tp = parseLikeCondition(start_node, false);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("notLikePredType")) {
					tp = parseLikeCondition(start_node, true);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("closedSearchType")) {
					tp = parseClosedSearch(start_node);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("inverseSearchType")) {
					tp = parseInverseType(start_node);
					produceSaadaQLItem(tp, condition);
					
				} else if (type.equals("xMatchType")) {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"'xMatch' clause");
					
				} else {
					QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"'Condition' item: " + type);
				}
			}
		}
		if (tp == UT_UNKNOWN) {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE,"Constant condition");
		}
		return tp;
	}
	
	/**
	 * Parsing parenthesized expression
	 */
	private int parseClosedSearch(Node start_node) throws SaadaException {
		int type = UT_UNKNOWN;
		boolean ok = ! isRegionInside(start_node);
		if (ok == true) {
			buffer += "(";
		}
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Condition")) {
				type = parseCondition(children.item(i), NO_CONDITION);
			}
		}
		if (ok == true) {
			buffer += ")";
		}
		return type;
	}
	
	/**
	 * Useful to know if we must output parenthesis
	 */
	private boolean isRegionInside(Node start_node) {
		String value;
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Condition")) {
				NamedNodeMap att = children.item(i).getAttributes();
				for(int j = 0 ; j < att.getLength() ; ++j) {
					if (att.item(j).getNodeName().equals("xsi:type")) {
						value = att.item(j).getNodeValue();
						if (value.equals("regionSearchType")) {
							return true;
						} else if (value.equals("unionSearchType")) {
							return isRegionInside(children.item(i));
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Parse the 'not' keyword
	 */
	private int parseInverseType(Node start_node) throws SaadaException {
		debugPrint("Inverse");
		buffer += "not ";
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			if (children.item(i).getNodeName().equals("Condition")) {
				return parseCondition(children.item(i), NO_CONDITION);
			}
		}
		return UT_UNKNOWN;
	}
	
	
	/**
	 * @return the type of intersection. See XXX_UNION constants
	 */
	private int parseIntersection(Node start_node, int condition) throws SaadaException {
		debugPrint("Intersection (AND)");
		int type1 = 0, type2 = 0;
		Node child;
		NodeList children = start_node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			child = children.item(i);
			if (child.getNodeName().equals("Condition")) {
				if (type1 == 0) {
					// first condition
					type1 = parseCondition(child, condition);
					debugPrint("Intersection Condition type : " + type1);
				} else {
					// second condition
					type2 = parseCondition(child, AND_CONDITION);
				}
			}
		}
		if (type1 == UT_POSITION && type2 == UT_POSITION) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Position Conditions are ORed in SaadaQL");
		}
		// the intersection is SaadaQL-compatible
		if (type1 != type2) {
			return UT_MIXED;
		}
		return type1;
	}
	
	/**
	 * @param isNotBetween true if condition is 'not between ...'
	 */
	private int parseBetweenCondition(Node start_node, boolean isNotBetween) throws SaadaException {
		Node child;
		int colType = UT_UNKNOWN, type, nbArgs = 0;
		String colName = "";
		NodeList children = start_node.getChildNodes();
		buffer += "(" + colName;
		for (int i = 0; i < children.getLength(); ++i) {
			child = children.item(i);
			if (child.getNodeName().equals("Arg")) {
				type = parseArg(child);
				if (type == UT_SAADA || type == UT_CLASS) {
					colType = type;
					if (isNotBetween == true) {
						buffer += " not between ";
					} else {
						buffer += " between ";
					}
				} else {
					if (nbArgs == 0) {
						buffer += " and ";
					}
					nbArgs++;
				}
			} else {
				warning("Element " + child.getNodeName() + " not supported.");
			}
		}
		if (colType == UT_UNKNOWN || nbArgs != 2) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"'between'");
		}
		buffer += ")";	
		return colType;
	}
	
	
	private String getQueryType(String query) {
		String s = query.substring(query.indexOf("Select") + 6,query.indexOf("From"));
		return s.trim().toUpperCase();
	}
	
	
	private void warning(String msg) {
		if (warningOn == true) {
			Messenger.printMsg(Messenger.TRACE, "**** ADQL Parser Warning : " + msg);
		}
	}
	
	
	private void produceSaadaQLItem(int type, int condition) {
		switch(type) {
		case UT_SAADA:
			if (outOfWhereClause == false) {
				if (whereAttributeSaadaBuffer.length() == 0) {
					whereAttributeSaadaBuffer += "\n WhereAttributeSaada {";
					
				} else if (condition == OR_CONDITION) {
					if (!whereAttributeSaadaBuffer.equals("\n WhereAttributeSaada {") && !buffer.equals(")")) {
						whereAttributeSaadaBuffer += " or ";
					}
				} else if (condition == AND_CONDITION) {
					if (!whereAttributeSaadaBuffer.equals("\n WhereAttributeSaada {") && !buffer.equals(")")) {
						whereAttributeSaadaBuffer += " and ";
					}
				}
				whereAttributeSaadaBuffer += buffer;
				break;
			} else {
				saadaString += buffer;
			}
			break;
		case UT_CLASS:
			if (outOfWhereClause == false) {
				if (whereAttributeClassBuffer.length() == 0) {
					whereAttributeClassBuffer += "\n WhereAttributeClass {\"";
				} else if (condition == OR_CONDITION) {
					if (!whereAttributeClassBuffer.equals("\n WhereAttributeClass {\"") && !buffer.equals(")")) {
						whereAttributeClassBuffer += " or ";
					}
				} else if (condition == AND_CONDITION) {
					if (!whereAttributeClassBuffer.equals("\n WhereAttributeClass {\"") && !buffer.equals(")")) {
						whereAttributeClassBuffer += " and ";
					}
				}
				whereAttributeClassBuffer += buffer;
			} else {
				saadaString += buffer;
			}
			break;
		default:
			saadaString += buffer;
		}
		buffer = "";
	}
	
	
	private void debugPrint(String s) {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Parsing " + s + " ...");
	}


	@Override
	public boolean isMetadataRequired() {
		return false;
	}
	
}

