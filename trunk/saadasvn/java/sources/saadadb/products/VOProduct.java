package saadadb.products;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Vector;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.ChangeKey;
import saadadb.util.DefineType;
import saadadb.util.Messenger;
import cds.astro.Astrocoo;
import cds.savot.model.CoosysSet;
import cds.savot.model.FieldSet;
import cds.savot.model.GroupSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotCoosys;
import cds.savot.model.SavotField;
import cds.savot.model.SavotGroup;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.model.TableSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

public class VOProduct extends File implements ProductFile {

	private static final long serialVersionUID = 1L;

	public String md5;

	public String md5Entry;

	public LinkedHashMap<String, Integer> entry;

	public LinkedHashMap<Integer, String> typeEntry;

	public LinkedHashMap<Integer, String> typeUnit;

	//public LinkedHashMap<String, AttributeHandler> tableAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
	FieldSet fields;
	public SavotTable savotTable;

	public SavotPullParser parser;

	public SavotVOTable voTable;

	public SavotResource currentResource;

	public SavotTR currentTR;

	public int tableCount = 0;
	
	private Product product;

	private boolean onfirstline = true;
	protected SpaceFrame space_frame;


	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWValueQuickly(java.lang.String)
	 */
	public String getKWValueQuickly(String key) {

		if (voTable.getDefinitions() == null || voTable.getDefinitions().getCoosys() == null) {
			if (key.equals("EQUINOX")) {
				Messenger.printMsg(Messenger.WARNING,
						"No COOSYS set, using default : EQUINOX=J2000");
				return "J2000";
			} else if (key.equals("SYSTEM")) {
				Messenger.printMsg(Messenger.WARNING,
						"No COOSYS set, using default : SYSTEM=eq_FK5");
				return "eq_FK5";
			}
		}

		Vector coosys = voTable.getDefinitions().getCoosys().getItems();
		SavotCoosys infoCooSys = (SavotCoosys) coosys.elementAt(0);
		if (key.equals("EQUINOX")) {
			return infoCooSys.getEquinox();
		} else {
			if (key.equals("SYSTEM")) {
				return infoCooSys.getSystem();
			} else {
				if (key.indexOf("Table_Name_") >= 0) {
					return savotTable.getName();
				}
			}
		}
		Vector resources = voTable.getResources().getItems();
		Enumeration enumerate = resources.elements();
		while (enumerate.hasMoreElements()) {
			SavotResource infoResource = (SavotResource) enumerate.nextElement();
			if (key.indexOf("Resource_Name_") >= 0) {
				return infoResource.getName();
			} else {
				if (key.indexOf("Description_Name_") >= 0) {
					return infoResource.getDescription();
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getTableEntry()
	 */
	public LinkedHashMap<String, Integer> getTableEntry() throws IgnoreException {
		LinkedHashMap<String, Integer> entry = new LinkedHashMap<String, Integer>();
		/*
		 * A table can have not fields but just some params
		 */
		if( fields != null )  {
			for (int i = 0; i < fields.getItemCount(); i++) {
				SavotField sf = ((SavotField) fields.getItemAt(i));
				String id = sf.getId();
				// Messenger.printMsg(Messenger.TRACE, "get N"+i+" "+sf.getId()+"
				// "+sf.getName());
				if (id == null || id.equals("")) {
					id = sf.getName();
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "field # " + i + " has no ID, takes the name: <" + id + ">");
				}
				if( entry.get(id) != null ) {
					IgnoreException.throwNewException(SaadaException.VOTABLE_FORMAT, "Duplicated field id <" + id + ">");
				}
				entry.put(id, new Integer(i));
			}
		}
		return entry;
	}

	/**
	 * @param numHDU
	 * @return
	 * @throws AbortException 
	 */
	public LinkedHashMap<String, Integer> getTableEntry(int numHDU) throws IgnoreException {
		return getTableEntry();
	}

	/**
	 * @return
	 */
	public LinkedHashMap<String, Integer> getEntry() {
		return entry;
	}



	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWEntry(java.util.LinkedHashMap)
	 */
	public void getKWEntry(LinkedHashMap<String, AttributeHandler> tableAttributeHandlerEntry) { 

		typeEntry = new LinkedHashMap<Integer, String> ();
		typeUnit = new LinkedHashMap<Integer, String> ();

		AttributeHandler attribute;
		ArrayList<String> kWIgnored = null;
		if( this.product.configuration != null ) this.product.configuration.getMapping().getIgnoredAtt();
		String name = "";
		for( int f=0 ; f<fields.getItemCount() ; f++ ) {
			SavotField field = (SavotField) fields.getItemAt(f);
			/*
			 * Looks fiorst for a filed ID and takes the name if no id
			 */
			name = field.getId();
			if (name == null || name.equals("")) {
				name = field.getName();
			}
			if ( kWIgnored == null || !kWIgnored.contains(name)) {
				attribute = new AttributeHandler(field);
				if( this.product.configuration != null ) attribute.setCollname(this.product.configuration.getCollectionName());
				attribute.setComment(getStandardDescription(field.getDescription()));
				/*
				 * Entry can be null when the product is just read to build a map
				 */
				if( entry != null ) {
					Integer index = (Integer) entry.get(name);
					typeUnit.put(index, attribute.getUnit());
					typeEntry.put(index, attribute.getType());
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, " getKWEntry #" + index + " ("  + attribute.getNameorg() + "  " + attribute.getNameattr()+ " "  +attribute.getUnit() + " " +  attribute.getType() + " " + attribute.getUcd() + " " + attribute.getUtype() + ")");
				}
				tableAttributeHandlerEntry.put(attribute.getNameattr(), attribute);
			}
		}
	}



	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getRow(int)
	 */
	public double[] getExtrema(String key) throws Exception {
		/*
		 * Key can be null for FITS spectra with channels stored in image pixels.
		 * That case doesn't concern VOTables.
		 */
		if( key == null ) {
			return null;
		}
		double[] retour = new double[2];
		for( int i=0 ; i<fields.getItemCount() ; i++ ) {
			SavotField field = (SavotField)fields.getItemAt(i);
			if( field.getName().equals(key) || field.getId().equals(key)) {
				while( this.hasMoreElements() ) {
					double val = Double.parseDouble(currentTR.getTDSet().getContent(i).trim());
					if( onfirstline ) {
						retour[0] = val;
						retour[1] = val;
					}
					if( val < retour[0] ) {
						retour[0] = val;
					}
					else if( val > retour[1] ) {
						retour[1] = val;
					}
				}
				return retour;
			}
		}
		Messenger.printMsg(Messenger.WARNING, "Field <" + key + "> not found");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getRow(int)
	 */
	public Object[] getRow(int index) {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getRow(int, int)
	 */
	public Object[] getRow(int index, int numHDU) {
		return getRow(index);
	}

	/* 
	 * Does nothing because Savot runs in streamng mode: no rewind possible
	 * (non-Javadoc)
	 * @see saadadb.products.ProductFile#initEnumeration()
	 */
	public void initEnumeration() {

	}
	/*
	 * Set the TR cursor on the next line. Return true if the operation succeed.
	 *  (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		if( currentTR == null ) {
			return false;
		}
		else if( onfirstline ) {
			onfirstline = false;
			return true;
		}
		else {
			currentTR = parser.getNextTR();  
			if( currentTR == null || parser.getStatistics().iTRLocalGet() == 1 ) {
				return false;
			}
			else {
				return true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement() throws NumberFormatException, NullPointerException {
		Vector<Object> line = new Vector<Object>();
		// String message = "";
		TDSet td = currentTR.getTDSet();
		// message = "N"+(tableCount+1)+" ";
		for (int k = 0; k < td.getItemCount(); k++) {
			if( k >= typeEntry.size() ) {
				throw new NullPointerException("Line #" + tableCount + ": More <TD> elements than declared <FIELDS>");
			}
			String type = (String) typeEntry.get(new Integer(k));
			String tdContent = td.getContent(k).trim();
			Object obj = null;
			/*
			 * Il est des gens qui mettent NULL pour signifier qu'un champ n'est pas affect� au lieu de mettre un champs vide
			 */
			if( (DefineType.getType(type) == DefineType.FIELD_STRING || !tdContent.equals("")) && !tdContent.equalsIgnoreCase("null") ) {
				switch (DefineType.getType(type)) {
				case DefineType.FIELD_DATE:
					obj = new Date(tdContent);
					break;
				case DefineType.FIELD_STRING:
					if( tdContent.startsWith("<![CDATA[")) {
						tdContent = tdContent.substring(9, tdContent.length() - 3);
					}
					if (tdContent.equals("") || tdContent == null) {
						obj = " ";
					} else {
						obj = tdContent;
					}
					break;
				case DefineType.FIELD_INT:
					if( tdContent == null)
						obj = null;
					else
					obj = new Integer(tdContent);
					break;
				case DefineType.FIELD_DOUBLE:
					String unit = (String) typeUnit.get(new Integer(k));
					if ( unit.equals("h:m:s") || unit.equals("d:m:s") || unit.equals("hours") /* || tdContent.matches("[^\\s]+[:\\s]+[^\\s]+.*") */) {
						Astrocoo coord = new Astrocoo(this.product.astroframe);
						try {
							if( tdContent.startsWith("+") || tdContent.startsWith("-")) {
								coord.set("0 0 0 " + tdContent) ;
								obj = new Double(coord.getLat());
							}
							else  {
								coord.set(tdContent + " +0 0 0") ;
								obj = new Double(coord.getLon());
							}
						} catch (Exception e) {
							Messenger.printStackTrace(e);
							obj = "null";
						}
					} else {
						if( tdContent.equalsIgnoreCase("null")) {
							obj = new Double(Double.POSITIVE_INFINITY);
						}
						else {
							obj = new Double(tdContent);
						}
					}
					break;
				case DefineType.FIELD_FLOAT:
					obj = new Float(tdContent);
					break;
				case DefineType.FIELD_LONG:
					obj = new Long(tdContent);
					break;
				case DefineType.FIELD_SHORT:
					obj = new Short(tdContent);
					break;
				case DefineType.FIELD_BOOLEAN:
					boolean[] boo = new boolean[1];
					boo[0] = Boolean.getBoolean(tdContent);
					obj = boo;
					break;
				case DefineType.FIELD_BYTE:
					obj = new Byte(tdContent);
					break;
				case DefineType.FIELD_CHAR:
					obj = new Character((tdContent.toCharArray())[0]);
					break;
				default:
					obj = new Object();
					break;		
			}
		} else {
			obj = "";
		}
			line.add(obj);
			// message += "<"+type+" / "+tdContent+">";
		}
		// Messenger.printMsg(Messenger.TRACE, message);
		tableCount++;
		return line.toArray();
	}


	/**
	 * @param description
	 * @return
	 */
	public static String getStandardDescription(String description) {
		String standard = description;
		if (standard.indexOf('-') >= 0) {
			standard = standard.replace('-', '_');
		}
		if (standard.indexOf(',') >= 0) {
			standard = standard.replace(',', ' ');
		}
		if (standard.indexOf('.') >= 0) {
			standard = standard.replace('.', ' ');
		}
		if (standard.indexOf('\'') >= 0) {
			standard = standard.replace('\'', ' ');
		}
		return standard;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getNRows()
	 */
	public int getNRows() {
		return savotTable.getNrowsValue();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getNCols()
	 */
	public int getNCols() {
		return fields.getItemCount();
	}

	/**
	 * Create the SaadaTable attribute handlers. Resource description + params of the 1st table 
	 * + params of the selected table
	 * Create the table attribute handler table with 
	 */
	public LinkedHashMap<String, AttributeHandler> createTableAttributeHandler(){
		LinkedHashMap<String, AttributeHandler> retour = this.createTableAttributeHandlerFromResourceDesc();
		retour.putAll(this.getParam(0));
		retour.putAll(this.getParam(savotTable));
		return retour;
	}

	/**
	 * 
	 */
	public LinkedHashMap<String, AttributeHandler> createTableAttributeHandlerFromResourceDesc(){
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		String keyChanged = "";
		
		String system, equinox, id;
		if (voTable.getDefinitions() == null|| voTable.getDefinitions().getCoosys() == null) {
			system = "J2000";
			equinox = "eq_FK5";
			id = "Missing value taken by default";
		} else {
			Vector coosys = voTable.getDefinitions().getCoosys().getItems();
			SavotCoosys infoCooSys = (SavotCoosys) coosys.elementAt(0);
			system = infoCooSys.getSystem();
			equinox = infoCooSys.getEquinox();
			id = infoCooSys.getId();
		}
		
		AttributeHandler attributeEquinox = new AttributeHandler();
		attributeEquinox.setNameorg("EQUINOX");
		keyChanged = ChangeKey.changeKey("EQUINOX");
		attributeEquinox.setNameattr(keyChanged);
		retour.put(keyChanged, attributeEquinox);
		if( this.product.configuration != null ) attributeEquinox.setCollname(this.product.configuration.getCollectionName());
		attributeEquinox.setComment(id);
		attributeEquinox.setType("String");
		String valueEquinox = equinox;
		int indexJ = valueEquinox.indexOf("J");
		if (indexJ >= 0) {
			valueEquinox = valueEquinox.substring(indexJ + 1);
		}
		attributeEquinox.setValue(valueEquinox);
		
		AttributeHandler attributeSystem = new AttributeHandler();
		attributeSystem.setNameorg("SYSTEM");
		keyChanged = ChangeKey.changeKey("SYSTEM");
		attributeSystem.setNameattr(keyChanged);
		retour.put(keyChanged, attributeSystem);
		if( this.product.configuration != null ) attributeSystem.setCollname(this.product.configuration.getCollectionName());
		attributeSystem.setComment(id);
		attributeSystem.setType("String");
		attributeSystem.setValue(system);
		
		AttributeHandler attributeResource = new AttributeHandler();
		// String name = infoResource.getId();
		String name = "Resource_Name";
		attributeResource.setNameorg(name);
		keyChanged = ChangeKey.changeKey(name);
		attributeResource.setNameattr(keyChanged);
		retour.put(keyChanged, attributeResource);
		if( this.product.configuration != null ) attributeResource.setCollname(this.product.configuration.getCollectionName());
		attributeResource.setComment(getStandardDescription(currentResource.getDescription()));
		attributeResource.setType("String");
		attributeResource.setValue(currentResource.getName());
		
		AttributeHandler attributeDescription = new AttributeHandler();
		name = "Description_Name";
		attributeDescription.setNameorg(name);
		keyChanged = ChangeKey.changeKey(name);
		attributeDescription.setNameattr(keyChanged);
		retour.put(keyChanged, attributeDescription);
		if( this.product.configuration != null ) attributeDescription.setCollname(this.product.configuration.getCollectionName());
		attributeDescription.setType("String");
		attributeDescription.setValue(getStandardDescription(currentResource.getDescription()));
		
		AttributeHandler attributeTable = new AttributeHandler();
		name = "Table_Name";
		attributeTable.setNameorg(name);
		keyChanged = ChangeKey.changeKey(name);
		attributeTable.setNameattr(keyChanged);
		if( this.product.configuration != null ) attributeTable.setCollname(this.product.configuration.getCollectionName());
		attributeTable.setComment(getStandardDescription(savotTable.getDescription()));
		attributeTable.setType("String");
		attributeTable.setValue(savotTable.getName());
		attributeTable.setUcd(savotTable.getUcd());
		retour.put(keyChanged, attributeTable);
		return retour;
	}

	public void printKW() {
	}


	public Image getBitMapImage() {
		return null;
	}

	public Image getSpectraImage() {
		return null;
	}



						/* ######################################################
						 * 
						 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
						 * 
			
						 *#######################################################*/
	
	/**
	 * This creator musn't be sed to load data but just to build a map of the porduct 
	 * @param filename
	 * @throws IgnoreException
	 */
	public VOProduct(String filename) throws Exception{
		super(filename);
		this.product = new Product(new File(filename), null);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The XML product configuration is starting...");
		parser = new SavotPullParser(getCanonicalPath(), SavotPullEngine.ROWREAD);	
		currentTR = parser.getNextTR();       
		voTable = parser.getVOTable();
		/*
		 * Current Saada Release only read the first resource
		 */
		currentResource = (SavotResource) voTable.getResources().getItemAt(0);
		if( currentResource == null ) {
			IgnoreException.throwNewException(SaadaException.VOTABLE_FORMAT, "File <" + filename + "> is not a VOTable");
		}
	}
	
	/**
	 * @param product
	 * @throws AbortException 
	 * @throws AbortException 
	 */
	public VOProduct(Product product) throws Exception{
		
		super(product.file.getAbsolutePath());
		this.product = product;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The XML product configuration is starting...");
		parser = new SavotPullParser(getCanonicalPath(), SavotPullEngine.ROWREAD);		
		currentTR = parser.getNextTR();  
		onfirstline  = true;
		voTable = parser.getVOTable();
		/*
		 * Current Saada Release only read the first resource
		 */
		currentResource = (SavotResource) voTable.getResources().getItemAt(0);
		if( currentResource == null ) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "not a VOTable");
		}
		boolean table_found = false;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for the table to uses.");
		String table_name = product.getConfiguration().getExtensionName();
		/*
		 * no table name given within the configuration: First table with data taken
		 */
		if( table_name.equals("") ){
			Messenger.printMsg(Messenger.TRACE, "Name of the table not specified : Take the first with data");
			for (int m = 0; m < currentResource.getTableCount(); m++) {
				savotTable = (SavotTable) (currentResource.getTables().getItemAt(m));
				/*
				 * Takes the first table with fields (then possibly with data)
				 */
				if( savotTable.getFields().getItemCount() > 0 ) {
						if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Take table #" + m + " <" + savotTable.getName() + ">");
					table_found = true;
					break;
				}				
			}
			if( !table_found ) { 
				IgnoreException.throwNewException(SaadaException.VOTABLE_FORMAT, "Cannot found table with data");
				return;
			}
			
		}
		else {
			for (int m = 0; m < currentResource.getTableCount(); m++) {
				savotTable = (SavotTable) (currentResource.getTables().getItemAt(0));
				/*
				 * Table name given: search itCa marche super.
				 */
				Messenger.printMsg(Messenger.TRACE, "Name of the table load specified : "+table_name);
				for( int t=0 ; t<currentResource.getTableCount() ; t++ ) {
					savotTable = (SavotTable) (currentResource.getTables().getItemAt(t));
					if( savotTable.getId().equals(table_name) ) {
						Messenger.printMsg(Messenger.TRACE, "Table with id <" + table_name + "> found");
						table_found = true;
						break;
					}
				}
				if( !table_found ) { 
					IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Table <" + table_name + "> not found in <" + this.getName() + ">");
					return;
				}
			}
		}
				//if it is defined
		fields = savotTable.getFields();
		this.entry = this.getTableEntry();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Creation of the tableAttributHandler...");
		this.product.tableAttributeHandler = this.createTableAttributeHandler();
	}


	


	/**
	 * Add VOTable Params (flat and from Groups) of the table #num_table in the attribute handler list
	 * @param num_table
	 */
	public  LinkedHashMap<String, AttributeHandler>  getParam(int num_table){
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();		
		
		TableSet tableSet = currentResource.getTables();
		if( num_table >= 0 && num_table < tableSet.getItemCount()) {
			retour.putAll(this.getParam((SavotTable) tableSet.getItemAt(num_table)));
		}
		return retour;
	}
	/**
	 * Add VOTable Params (flat and from Groups) of the table #num_table in the attribute handler list
	 * @param num_table
	 */
	public LinkedHashMap<String, AttributeHandler> getParam(SavotTable stable){
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();		
		if( stable != null ) {
			ParamSet param = stable.getParams();
			for(int j=0; j<param.getItemCount(); j++){
				SavotParam savotParam = (SavotParam) param.getItemAt(j);
				if( savotParam.getName().length() == 0 && savotParam.getId().length() == 0 ) {
					Messenger.printMsg(Messenger.WARNING, "Param without name or id: ignored");
				}
				else {
					AttributeHandler attributeParam = new AttributeHandler(savotParam);
					String name = savotParam.getName();
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Params " + name + " ( " + attributeParam.getNameattr() + ")");
					retour.put(attributeParam.getNameattr(), attributeParam);
				}
			}
			GroupSet groups = stable.getGroups();
			for(int g=0; g<groups.getItemCount(); g++){
				SavotGroup group = (SavotGroup)groups.getItemAt(g);
				param = group.getParams();
				for(int j=0; j<param.getItemCount(); j++){
					SavotParam savotParam = (SavotParam) param.getItemAt(j);
					
					AttributeHandler attributeParam = new AttributeHandler(savotParam);
					String name = savotParam.getName();
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Params from group " + name + " ( " + attributeParam.getNameattr() + ")");
					retour.put(attributeParam.getNameattr(), attributeParam);
				}				
			}
		}
		return retour;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getMap(java.lang.String)
	 */
	public LinkedHashMap<String, ArrayList<AttributeHandler>> getMap(String category) throws IgnoreException {
		try {
			LinkedHashMap<String, ArrayList<AttributeHandler>> retour = new LinkedHashMap<String, ArrayList<AttributeHandler>>();	
			Vector<Object> resources = voTable.getResources().getItems();
			int nb_ressources = resources.size();
			for( int r=0 ; r<nb_ressources ; r++ ) {
				currentResource = (SavotResource) voTable.getResources().getItemAt(r);
				
				for (int m = 0; m < currentResource.getTables().getItemCount(); m++) {
					savotTable = (SavotTable) (currentResource.getTables().getItemAt(m));
					this.product.tableAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
					if( m == 0 ) {
						this.createTableAttributeHandlerFromResourceDesc();
					}
					this.getParam(m);
					retour.put("#" + m + " " + savotTable.getId()+ " (TABLE)"
							, new ArrayList<AttributeHandler>(this.product.getTableAttributeHandler().values()));    	
					LinkedHashMap<String, AttributeHandler>  entr_att = new LinkedHashMap<String, AttributeHandler>();
					fields = savotTable.getFields();
					if( fields.getItemCount() > 0 ) {
						getKWEntry(entr_att) ;
						retour.put("#" + m + " " + savotTable.getId() + " (TABLE COLUMNS)"
								, new ArrayList<AttributeHandler>(entr_att.values()));  
					}
	                /*
                     * Just one table loaded right now
                     */
                    break;
				}
			}
			return retour;
		} catch(Exception e) {
			IgnoreException.throwNewException(SaadaException.VOTABLE_FORMAT, e);
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#setSpaceFrameForTable()
	 */
	public void setSpaceFrameForTable(){
		LinkedHashMap<String, AttributeHandler> lhm = new LinkedHashMap<String, AttributeHandler>();	
		this.getKWEntry(lhm);
		/*
		 * Try first to take the resource coosys
		 */
		CoosysSet css = this.currentResource.getCoosys();
		SavotCoosys infoCooSys = null;
		if( css != null && css.getItemCount() > 0) {
			infoCooSys = (SavotCoosys) css.getItemAt(0);
			
		}
		/*
		 * Otherwise take this of the 
		 */
		if( infoCooSys == null ) {
			if( voTable.getDefinitions() != null && voTable.getDefinitions().getCoosys() != null && voTable.getDefinitions().getCoosys().getItemCount() > 0 ) {
				Vector coosys = voTable.getDefinitions().getCoosys().getItems();
				infoCooSys = (SavotCoosys) coosys.elementAt(0);
			}
		}
		space_frame = new SpaceFrame(infoCooSys, lhm);
	}
	

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#setSpaceFrame()
	 */
	public void setSpaceFrame(){
		LinkedHashMap<String, AttributeHandler> lhm = this.createTableAttributeHandler();
		/*
		 * Try first to take the resource coosys
		 */
		CoosysSet css = this.currentResource.getCoosys();
		SavotCoosys infoCooSys = null;
		if( css != null && css.getItemCount() > 0) {
			infoCooSys = (SavotCoosys) css.getItemAt(0);
			
		}
		/*
		 * Otherwise take this of the 
		 */
		if( infoCooSys == null ) {
			if( voTable.getDefinitions() != null && voTable.getDefinitions().getCoosys() != null && voTable.getDefinitions().getCoosys().getItemCount() > 0 ) {
				Vector coosys = voTable.getDefinitions().getCoosys().getItems();
				infoCooSys = (SavotCoosys) coosys.elementAt(0);
			}
		}
		space_frame = new SpaceFrame(infoCooSys, lhm);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getSpaceFrame()
	 */
	public SpaceFrame getSpaceFrame() {
		return space_frame;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args ) {
		try {
//			VOProduct fp = new VOProduct("/home/michel/saada/deploy/TestBench1_5/015.load_xml_spec/data/vospectre015.xml");
			VOProduct fp = new VOProduct("/home/michel/Desktop/vizier_votable.ecl.vot");
			LinkedHashMap<String, ArrayList<AttributeHandler>> retour = fp.getMap(null);
			System.out.println(fp.getName());
			for( String en: retour.keySet() ) {
				System.out.println(en);
				for( AttributeHandler ah: retour.get(en)) {
					System.out.println("   -" + ah.getNameorg() + " value=" + ah.getValue() + " type=" + ah.getType() + " unit=" + ah.getUnit());
				}
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
		}
		
		
	}
}
