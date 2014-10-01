package saadadb.products;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import saadadb.collection.Category;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.QuantityDetector;
import saadadb.util.ChangeKey;
import saadadb.util.DefineType;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.DataFileExtensionType;
import saadadb.vocabulary.enums.ExtensionSetMode;
import cds.astro.Astrocoo;
import cds.savot.binary.DataBinaryReader;
import cds.savot.model.FieldSet;
import cds.savot.model.GroupSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotBinary;
import cds.savot.model.SavotCoosys;
import cds.savot.model.SavotField;
import cds.savot.model.SavotGroup;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

/**
 * New VOTable interface multi extension and multitable
 * @author laurent
 * @version $Id$
 */
@SuppressWarnings("deprecation")
public class VOTableDataFile extends File implements DataFile {

	private static final long serialVersionUID = 1L;
	/*
	 * Attributes used for readout
	 */
	/** Global parser: used to read data, not to read metadata*/
	public SavotPullParser parser;
	/** pointer on the VOTable read by the parser: used to get some extra info */
	public SavotVOTable voTable;
	/** Number of the mast row read */
	public int rowNum = 0;
	/** pointer on the current data row */
	private SavotTR savotTR;
	/*
	 * High level field
	 */
	/** global map of the product */
	protected Map<String, DataFileExtension> productMap;
	/** report on the selection of the extension */
	protected ExtensionSetter  extensionSetter = new ExtensionSetter();
	/** ref on the attached builder */
	private ProductBuilder productBuilder;
	/** Comment read within the header */
	private List<String> comments = new ArrayList<String>();
	/*
	 * Internal descriptors
	 */
	/** AttributeHandler of the header of the selected table */
	private Map<String, AttributeHandler> attributeHandlers = null;
	/** AttributeHandler of the data table of the selected table */
	private Map<String, AttributeHandler> entryAttributeHandlers = null;	
	/** Pseudo AHs containing info possibly referenced by fields */
	private Map<String, AttributeHandler> idForRef = new LinkedHashMap<String, AttributeHandler>();

	/** reference of the table containing the data */
	private DataFileExtension dataExtension;
	/** reference of the table containing the table header */
	private DataFileExtension headerExtension;
	/** Numeric code of the data column types (optimization)*/
	private ArrayList<Integer> entryTypeCode;
	/**array of the data column types (optimization)*/	
	private ArrayList<String> entryTypeString;
	/*
	 * internal pointer used to read internal data
	 */
	/** Flag for switching in bin mode */
	private boolean binaryMode = false;
	/** Pointer to the select binary data blob */
	private SavotBinary binaryData;
	/** Fields stored with the binary blob */
	private FieldSet binaryFields;
	/** Binary data reader */
	private DataBinaryReader binaryParser;

	/**
	 * This creator musn't be sed to load data but just to build a map of the porduct 
	 * @param filename
	 * @throws IgnoreException
	 */
	public VOTableDataFile(String filename) throws Exception{
		super(filename);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Modeling the VOTable " + filename);
		this.parser = new SavotPullParser(getCanonicalPath(), SavotPullEngine.ROWREAD);	    
		this.voTable = parser.getVOTable();
		this.getProductMap();
	}

	/**
	 * @param product
	 * @throws AbortException 
	 * @throws AbortException 
	 */
	public VOTableDataFile(ProductBuilder product) throws Exception{		
		super(product.getName());
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Modeling the VOTable and binding it with the builder");
		this.bindBuilder(product);
	}

	/**
	 * Look for a param having the coosys (VOTable 1.2 and after)
	 * Put in the params map or update the map if it already exists
	 * @param params
	 * @throws Exception 
	 */
	private void searchForCooSysInParams(Map<String, AttributeHandler> params) throws Exception{
		String coosys = null;
		String equ = null;
		for( AttributeHandler ah: params.values()){
			if( "stc:AstroCoordSystem.href".equals(ah.getUtype()) ) {
				coosys = ah.getValue();
				break;
			}
		}
		AttributeHandler ah;
		if( coosys != null ){        
			if( coosys.indexOf("FK4") >= 0 ) {
				coosys = "eq_FK4";
				equ = "1950";
			} else if( coosys.indexOf("FK5") >= 0 ) {
				coosys = "eq_FK5";
				equ = "J2000";
			}  else if( coosys.indexOf("ICRS") >= 0 ) {
				coosys = "ICRS";
			} else if( coosys.indexOf("galactic") >= 0 ) {
				coosys = "Galactic";
			} else if( coosys.indexOf("ecl_FK") >= 0 ) {
				coosys = "Ecliptic";
			} else {
				IgnoreException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Coos sys " + coosys + " not supported");
			}

			if( (ah = params.get(ChangeKey.changeKey("SYSTEM"))) == null)  {
				ah = new AttributeHandler();
				ah.setNameorg("SYSTEM");
				String keyChanged = ChangeKey.changeKey("SYSTEM");
				ah.setNameattr(keyChanged);
				params.put(keyChanged, ah);
				ah.setComment("Added by Saada");
				ah.setType("String");
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Override COOSYS with value found in params: " + coosys);
			}
			ah.setValue(coosys);
			if( equ != null ) {
				if( (ah = params.get(ChangeKey.changeKey("EQUONIX"))) == null)  {
					ah = new AttributeHandler();
					ah.setNameorg("EQUONIX");
					String keyChanged = ChangeKey.changeKey("EQUONIX");
					ah.setNameattr(keyChanged);
					params.put(keyChanged, ah);
					ah.setComment("Added by Saada");
					ah.setType("String");
				}
				ah.setValue(equ);
			}
		}
	}

	/**
	 * Format a description text in order to be acceptable for SQL
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

	private void addIdForRef(SavotCoosys infoCooSys ){
		String system, equinox;
		system = infoCooSys.getSystem();
		equinox = infoCooSys.getEquinox();
		AttributeHandler attributeCoosys = new AttributeHandler();
		attributeCoosys.setNameorg("COOSYS");
		attributeCoosys.setNameattr(ChangeKey.changeKey("COOSYS"));
		attributeCoosys.setType("String");
		attributeCoosys.setValue(system + " " + equinox);
		this.idForRef.put(infoCooSys.getId(), attributeCoosys);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Add " + attributeCoosys + " as referenced COOSYS");
	}
	/**
	 * returns a map of pseudo parameters built from description of both resource and table
	 * @param savotResource
	 * @param savotTable
	 * @return
	 */
	private Map<String, AttributeHandler> createTableAttributeHandlerFromResourceDesc(SavotResource savotResource, SavotTable savotTable){
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Build pseudo params from various table infos");
		Map<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		this.idForRef = new LinkedHashMap<String, AttributeHandler>();
		String keyChanged = "";
		SavotCoosys infoCooSys = null;
		if( voTable.getCoosys() != null && voTable.getCoosys().getItems() != null){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take the Coosys at resource level  ");
			infoCooSys = (SavotCoosys) voTable.getCoosys().getItems().get(0);	
			this.addIdForRef(infoCooSys);
		} 
		if (voTable.getDefinitions() != null && voTable.getDefinitions().getCoosys() != null && voTable.getDefinitions().getCoosys() != null) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take the Coosys at VOtable definition level");
			infoCooSys = (SavotCoosys) voTable.getDefinitions().getCoosys().getItems().get(0);
			this.addIdForRef(infoCooSys);
		} 
		if( savotResource.getCoosys() != null && savotResource.getCoosys().getItems() != null) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take the Coosys at resource level");
			infoCooSys = (SavotCoosys) savotResource.getCoosys().getItems().get( (savotResource.getCoosys().getItems().size()) - 1);
			this.addIdForRef(infoCooSys);
		}

		AttributeHandler attributeResource = new AttributeHandler();
		// String name = infoResource.getId();
		String name = "Resource_Name";
		attributeResource.setNameorg(name);
		keyChanged = ChangeKey.changeKey(name);
		attributeResource.setNameattr(keyChanged);
		retour.put(keyChanged, attributeResource);
		//	if( this.product.mapping != null ) attributeResource.setCollname(this.product.mapping.getCollection());
		attributeResource.setComment(getStandardDescription(savotResource.getDescription()));
		attributeResource.setType("String");
		attributeResource.setValue(savotResource.getName());

		AttributeHandler attributeDescription = new AttributeHandler();
		name = "Description_Name";
		attributeDescription.setNameorg(name);
		keyChanged = ChangeKey.changeKey(name);
		attributeDescription.setNameattr(keyChanged);
		retour.put(keyChanged, attributeDescription);
		//if( this.product.mapping != null ) attributeDescription.setCollname(this.product.mapping.getCollection());
		attributeDescription.setType("String");
		attributeDescription.setValue(getStandardDescription(savotResource.getDescription()));

		AttributeHandler attributeTable = new AttributeHandler();
		name = "Table_Name";
		attributeTable.setNameorg(name);
		keyChanged = ChangeKey.changeKey(name);
		attributeTable.setNameattr(keyChanged);
		//	if( this.product.mapping != null ) attributeTable.setCollname(this.product.mapping.getCollection());
		attributeTable.setComment(getStandardDescription(savotTable.getDescription()));
		attributeTable.setType("String");
		attributeTable.setValue(savotTable.getName());
		attributeTable.setUcd(savotTable.getUcd());
		retour.put(keyChanged, attributeTable);
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Resource/Description/table names added to header parameters");
		return retour;
	}

	/**
	 * Add VOTable Params (flat and from Groups) of the table #num_table in the attribute handler list
	 * @param num_table
	 */
	private LinkedHashMap<String, AttributeHandler> readParams(SavotTable savotTable){
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Read params and groups");
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();		
		if( savotTable != null ) {
			ParamSet param = savotTable.getParams();
			for(int j=0; j<param.getItemCount(); j++){
				SavotParam savotParam = (SavotParam) param.getItemAt(j);
				if( savotParam.getName().length() == 0 && savotParam.getId().length() == 0 ) {
					Messenger.printMsg(Messenger.TRACE, "Param without name or id: ignored");
				} else {
					AttributeHandler attributeParam = new AttributeHandler(savotParam);
					String name = savotParam.getName();
					retour.put(attributeParam.getNameattr(), attributeParam);
				}
			}
			GroupSet groups = savotTable.getGroups();
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
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, retour.size() + " params found");
		return retour;
	}

	/**
	 * @param extId
	 * @throws Exception
	 */
	private void selectResourceAndTable(String extId) throws SaadaException {
		this.dataExtension = null;
		this.headerExtension = null;	
		ExtensionSetMode	esm = ExtensionSetMode.NOT_SET;;
		if( extId != null && !extId.equals("") ){	//if it is defined
			/*
			 * Extension can be given as a name or as number like #res.table
			 */
			for( Entry<String,DataFileExtension> e: this.productMap.entrySet() ) {
				String key = e.getKey();
				DataFileExtension value = e.getValue();
				if( key.startsWith(extId) || key.matches(".*\\s" + extId + "\\s.*")) {
					if(value.isDataTable() ) {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Take data table " + extId + " given by the mapping");
						this.dataExtension = value;
					} else {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Take table header " + extId + " given by the mapping");
						this.headerExtension = value;
					}
				}
			}
			if( this.dataExtension != null && this.headerExtension == null ) {
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Found header matching " + extId + " but no data table");
			} else if( this.dataExtension == null && this.headerExtension != null ) {
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Found data table matching " + extId + " but table header");
			} else if( this.dataExtension == null && this.headerExtension == null ) {
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Found neither data table nor table header matching " + extId);
			} else {
				esm = ExtensionSetMode.GIVEN;
			}
		} else {
			/*
			 * Search both header extension and data extension with the same id
			 */
			for( Entry<String,DataFileExtension> e: this.productMap.entrySet() ) {
				String key = e.getKey().split(" ")[0]; //take the first field #a.b
				DataFileExtension value = e.getValue();
				if( !value.isDataTable()) {
					this.headerExtension = value;
					this.dataExtension = null;
					for( Entry<String,DataFileExtension> e2: this.productMap.entrySet() ) {
						String key2 = e2.getKey().split(" ")[0]; //take the first field #a.b
						DataFileExtension value2 = e2.getValue();
						if( value2.isDataTable() && key2.equals(key)) {
							this.dataExtension = value2;
							if (Messenger.debug_mode)
								Messenger.printMsg(Messenger.DEBUG, "Take table header + data " + key + ", the first available");
							break;
						}
					}
					if(this.dataExtension != null  ) {
						break;
					}
				}
			}
			if( this.dataExtension != null && this.headerExtension == null ) {
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Cannot found header + data with the same id");
			} else if( this.dataExtension == null && this.headerExtension != null ) {
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Cannot found header + data with the same id");
			} else if( this.dataExtension == null && this.headerExtension == null ) {
				IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Found data table found");
			} else {
				esm = ExtensionSetMode.DETECTED;
			}	
		}
		/*
		 * Stores te nueric code of the type to speed up the data readout
		 */
		this.entryTypeCode = new ArrayList<Integer>();
		for( AttributeHandler ah: this.dataExtension.attributeHandlers ){
			this.entryTypeCode.add(DefineType.getType(ah.getType()));
		}
		this.entryTypeString = new ArrayList<String>();
		for( AttributeHandler ah: this.dataExtension.attributeHandlers ){
			this.entryTypeString.add(ah.getType());
		}
		if( this.dataExtension.type == DataFileExtensionType.BINTABLE){
			this.setPointerOnBinaryData();
		}
		this.extensionSetter = new ExtensionSetter("#" + this.headerExtension.resourceNum + "." + this.headerExtension.tableNum
				, esm
				, "Given by the mapping");
	}

	private void  setPointerOnBinaryData() throws IgnoreException {
		SavotResource savotResource;
		int rCpt=1;
		int tCpt=1;// tables are tagged without regards for the resource to be usable for the parser
		this.binaryMode = true;
		try {
			this.parser = new SavotPullParser(getCanonicalPath(), SavotPullEngine.ROWREAD);
		} catch (IOException e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, e);
		}	    
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Switch on binary mode");
		while ((savotResource = this.parser.getNextResource()) != null) {
			if( savotResource.getTables().getItems() == null ) {
				Messenger.printMsg(Messenger.TRACE, "No table in resource #" + rCpt);
				continue;
			}
System.out.println(rCpt + " " + this.dataExtension.resourceNum  + " " +  tCpt + " " +  this.dataExtension.tableNum);
			for( SavotTable savotTable: savotResource.getTables().getItems()) {
				if( rCpt == this.dataExtension.resourceNum && tCpt == this.dataExtension.tableNum ) {
					if( (this.binaryData = savotTable.getData().getBinary()) == null  ) {
						IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "The table #" + rCpt + " is not binary as declared in the porduct map");
					}
					this.binaryFields = savotTable.getFields();			
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Binary data found");
					return;
				}
				tCpt++;
			}
			rCpt++;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#closeStream()
	 */
	@Override
	public void closeStream() {
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWValueQuickly(java.lang.String)
	 */
	@Override
	public String getKWValueQuickly(String keyOrg) {
		if( this.headerExtension == null || keyOrg == null ) {
			return null;
		}
		for( int i=0 ; i<this.dataExtension.attributeHandlers.size() ; i++ ){
			AttributeHandler ah = this.dataExtension.attributeHandlers.get(i);
			/*
			 * Column found: readout the data and take the xtrmum of the column if numeric
			 */
			if( ah.getNameorg().equals(keyOrg) || ah.getNameattr().equals(keyOrg) ) {
				return ah.getValue();
			}
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getRow(int)
	 */
	@Override
	public double[] getExtrema(String keyOrg) throws Exception {
		if( this.dataExtension == null || keyOrg == null ) {
			return null;
		}
		double[] retour = new double[3];
		/*
		 * Identify the column number matching keyOrg
		 */
		for( int i=0 ; i<this.dataExtension.attributeHandlers.size() ; i++ ){
			AttributeHandler ah = this.dataExtension.attributeHandlers.get(i);
			/*
			 * Column found: readout the data and take the xtrmum of the column if numeric
			 */
			if( ah.getNameorg().equals(keyOrg) || ah.getNameattr().equals(keyOrg) ) {
				this.initEnumeration();
				double nbLine = 0;
				boolean numeric = true;
				while( this.hasMoreElements()){
					double val = SaadaConstant.DOUBLE;
					String sv = ((Object[])(this.nextElement()))[i] .toString();
					try {
						if( numeric ) {
							val = Double.parseDouble( sv );
							if( nbLine == 0 ){
								retour[0] = val;
								retour[1] = val;						
							}
							if( val < retour[0] ) {
								retour[0] = val;
							} else if( val > retour[1] ) {
								retour[1] = val;
							}
						}
					} catch (Exception e) {	
						retour[0] = SaadaConstant.DOUBLE;
						retour[1] = SaadaConstant.DOUBLE;	
						numeric = false;
					}
					nbLine++;
				}
				retour[2] = nbLine;
				return retour;				
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Field <" + keyOrg + "> not found");
		return null;
	}


	/* 
	 * Does nothing because Savot runs in streamng mode: no rewind possible
	 * (non-Javadoc)
	 * @see saadadb.products.ProductFile#initEnumeration()
	 */
	@Override
	public void initEnumeration() {
		if( this.binaryMode ) {
			try {
				this.binaryParser = new DataBinaryReader(this.binaryData.getStream(), this.binaryFields, false, this.getParent());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			try {
				this.parser = new SavotPullParser(getCanonicalPath(), SavotPullEngine.ROWREAD);
				this.voTable = parser.getVOTable();
				this.rowNum = 0;
			} catch (IOException e) {}	    

			while( parser.getNextTR() != null) {
				if( parser.getTableCount() == this.headerExtension.tableNum ){
					return;
				}
			}
		}
	}
	/*
	 * Set the TR cursor on the next line. Return true if the operation succeed.
	 *  (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements() {
		if( this.binaryMode ) {
			try {
				return this.binaryParser.next();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		} else {
			if( (this.savotTR = parser.getNextTR()) != null )  {
				return ( parser.getTableCount() == this.headerExtension.tableNum );
			} else {
				return  false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public Object nextElement() throws NumberFormatException, NullPointerException {
		Vector<Object> line = new Vector<Object>();
		if( this.binaryMode ) {
			for( int f=0 ; f< this.binaryFields.getItemCount() ; f++ ){
				Object o = this.binaryParser.getCellAsString(f);
				line.add(o);
			}
		} else {

			TDSet td = this.savotTR.getTDSet();
			if( td.getItemCount() != this.dataExtension.attributeHandlers.size()) {
				throw new NullPointerException("Line #" + this.rowNum + ": More <TD> elements than declared <FIELDS>");
			}
			for (int k = 0; k < td.getItemCount(); k++) {
				int typeCode = this.entryTypeCode.get(k);
				String tdContent = td.getContent(k).trim();
				Object obj = null;
				/*
				 * Il est des gens qui mettent NULL pour signifier qu'un champ n'est pas affect� au lieu de mettre un champs vide
				 */
				if( (typeCode == DefineType.FIELD_STRING || !tdContent.equals("")) && !tdContent.equalsIgnoreCase("null") ) {
					switch (typeCode) {
					case DefineType.FIELD_DATE:
						obj = tdContent;
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
						String unit = (String) this.entryTypeString.get(k);
						if ( unit.equals("h:m:s") || unit.equals("d:m:s") || unit.equals("hours") /* || tdContent.matches("[^\\s]+[:\\s]+[^\\s]+.*") */) {
							Astrocoo coord = (Astrocoo) this.productBuilder.astroframeSetter.storedValue;
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
		}
		// Messenger.printMsg(Messenger.TRACE, message);
		this.rowNum++;
		return line.toArray();
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getNRows()
	 */
	@Override
	public int getNRows() {
		return SaadaConstant.INT;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getNCols()
	 */
	@Override
	public int getNCols() {
		return (this.dataExtension != null )? this.dataExtension.attributeHandlers.size(): SaadaConstant.INT;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getEntryAttributeHandler()
	 */
	@Override
	public Map<String, AttributeHandler> getEntryAttributeHandler()
	throws SaadaException {
		if( this.headerExtension == null ) {
			this.selectResourceAndTable(null);
		}
		if( this.entryAttributeHandlers == null ){
			this.entryAttributeHandlers = new LinkedHashMap<String, AttributeHandler>();
			for( AttributeHandler ah: this.dataExtension.attributeHandlers ) {
				this.entryAttributeHandlers.put(ah.getNameattr(), ah);
			}
		}
		return this.entryAttributeHandlers;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getAttributeHandler()
	 */
	@Override
	public Map<String, AttributeHandler> getAttributeHandler() throws SaadaException {
		if( this.headerExtension == null ) {
			this.selectResourceAndTable(null);
		}
		if( this.attributeHandlers == null ){
			this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
			for( AttributeHandler ah: this.headerExtension.attributeHandlers ) {
				this.attributeHandlers.put(ah.getNameattr(), ah);
			}
		}
		return this.attributeHandlers;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getProductMap(int)
	 */
	@Override
	public Map<String, List<AttributeHandler>> getProductMap(int category)
	throws IgnoreException {
		try {
			LinkedHashMap<String, List<AttributeHandler>> retour = new LinkedHashMap<String, List<AttributeHandler>>();
			Map<String, DataFileExtension> mapOrg = this.getProductMap();
			boolean taken = false;
			for( Entry<String, DataFileExtension> entry : mapOrg.entrySet()) {
				DataFileExtension extension = entry.getValue();
				if( category == Category.TABLE /* || checkExtensionCategory(extension, category)*/ ) {
					retour.put(entry.getKey(), extension.attributeHandlers);
					taken = true;
				} else {
					taken = false;
				}
				if( taken && extension.isDataTable() ) {
					retour.put(entry.getKey(), extension.attributeHandlers);
				}
			}
			return retour;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
			return null;
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getProductMap()
	 */
	@Override
	public Map<String, DataFileExtension> getProductMap() throws Exception {
		if( this.productMap == null ) { 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Build product map");
			this.productMap = new LinkedHashMap<String, DataFileExtension>();

			SavotResource savotResource;
			ArrayList<AttributeHandler> attrs = new ArrayList<AttributeHandler>();
			int rCpt=1;
			int tCpt=1;// tables are tagged without regards for the resource to be usable for the parser
			while ((savotResource = this.parser.getNextResource()) != null) {
				if( savotResource.getTables().getItems() == null ) {
					Messenger.printMsg(Messenger.TRACE, "No table in resource #" + rCpt);
					continue;
				}
				for( SavotTable savotTable: savotResource.getTables().getItems()) {
					DataFileExtensionType det = DataFileExtensionType.UNSUPPORTED;
					if( savotTable.getData().getBinary() != null  ) {
						det = DataFileExtensionType.BINTABLE;
					} else if( savotTable.getData().getTableData() != null  ) {
						det = DataFileExtensionType.ASCIITABLE;
					} else {
						Messenger.printMsg(Messenger.TRACE, "Data format not supported");
						continue;
					}
					Map<String, AttributeHandler> tahe = new LinkedHashMap<String, AttributeHandler>();
					tahe = this.createTableAttributeHandlerFromResourceDesc(savotResource, savotTable);
					tahe.putAll(this.readParams(savotTable));
					this.searchForCooSysInParams(tahe);
					attrs = new ArrayList<AttributeHandler>(tahe.values());					
					this.productMap.put("#" + rCpt + "." + tCpt + " " + savotTable.getId()+ " (" + DataFileExtensionType.TABLE_COLUMNS + ")"
							, new DataFileExtension(rCpt, savotResource.getId(), tCpt, savotTable.getName(), DataFileExtensionType.TABLE_COLUMNS, attrs));    	
					FieldSet fields = savotTable.getFields();
					attrs = new ArrayList<AttributeHandler>();					
					for( int i=0 ; i<fields.getItemCount() ; i++ ) {
						SavotField sf = (SavotField) fields.getItemAt(i);
						AttributeHandler att = new AttributeHandler(sf);						
						attrs.add(att);
						AttributeHandler assAh;
						if( (assAh = this.idForRef.get(sf.getRef())) != null ) {
							att.setAssociateAttribute(assAh);
							if (Messenger.debug_mode)
								Messenger.printMsg(Messenger.DEBUG, "Bind column " + att.getNameorg() + " with " + assAh);
						}
					}
					this.productMap.put("#" + rCpt + "." + tCpt + " " + savotTable.getId() + " (" + det + ")"
							, new DataFileExtension(rCpt, savotResource.getId(), tCpt,savotTable.getName(),det,attrs));
					tCpt++;
				}
				rCpt++;

			}
		}
		return this.productMap;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getQuantityDetector(saadadb.dataloader.mapping.ProductMapping)
	 */
	@Override
	public QuantityDetector getQuantityDetector(ProductMapping productMapping) throws SaadaException {
		if( productMapping.getCategory() == Category.ENTRY ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Only the " + this.getEntryAttributeHandler().size() + " table columns taken in account");
			return new QuantityDetector(this.getEntryAttributeHandler(), this.comments, productMapping);			
		} else if( this.getEntryAttributeHandler().size() > 0  ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.getEntryAttributeHandler().size() + " table columns taken in account");
			return  new QuantityDetector(this.getAttributeHandler(), this.getEntryAttributeHandler(), this.comments, productMapping, this);
		} else {
			return new QuantityDetector(this.getAttributeHandler(), this.comments, productMapping);
		}		
	}


	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#bindBuilder(saadadb.products.ProductBuilder)
	 */
	@Override
	public void bindBuilder(ProductBuilder builder) throws Exception {
		this.productBuilder = builder;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for the appropriate resource/table.");
		String ext_name = null;
		if( this.productBuilder.getMapping() != null ) {
			ext_name = this.productBuilder.mapping.getExtension();
		}
		this.selectResourceAndTable(ext_name);
		//		if( this.productBuilder.mapping != null )
		//			this.productBuilder.mapping.getHeaderRef().setNumber(this.goodResourceNum);	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Creation of the tableAttributHandler...");
		this.productBuilder.productAttributeHandler = this.getAttributeHandler();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The tableAttributeHandler is OK.");	
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#reportOnLoadedExtension()
	 */
	@Override
	public List<ExtensionSetter> reportOnLoadedExtension() {
		List<ExtensionSetter> retour = new ArrayList<ExtensionSetter>();
		if( this.extensionSetter != null )retour.add(this.extensionSetter);
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getComments()
	 */
	@Override
	public List<String> getComments() throws SaadaException {
		return this.comments;
	}

	public static void main(String[] args ) {
		try {
			Messenger.debug_mode = true;
			VOTableDataFile fp = new VOTableDataFile(args[0]);
			Map<String, List<AttributeHandler>> retour = fp.getProductMap(Category.TABLE);
			for( String en: retour.keySet() ) {
				System.out.println(en);
				for( AttributeHandler ah: retour.get(en)) {
					System.out.println("   -" + ah.getNameorg() + " = " + ah.getValue());
				}
			}
			fp.selectResourceAndTable(null);
			System.out.println(fp.headerExtension);
			System.out.println(fp.dataExtension);
			fp.initEnumeration();
			//			while (fp.hasMoreElements()) {
			//				System.out.println(fp.savotTR.getTDSet().getItemCount());
			//			}

			//			fp.selectResourceAndTable("#2.3");
			//			System.out.println(fp.headerExtension);
			//			System.out.println(fp.dataExtension);
			//			fp.initEnumeration();
			//			while (fp.hasMoreElements()) {
			//				System.out.println(fp.savotTR.getTDSet().getItemCount());
			//			}
			//
			//			fp.selectResourceAndTable("II_86_suppl");
			//			System.out.println(fp.headerExtension);
			//			System.out.println(fp.dataExtension);		
			while (fp.hasMoreElements()) {
				Object[] v =  (Object[]) fp.nextElement();
				for( Object o: v){
					System.out.print(o + "\t");
				}
				System.out.println();
			}

			for( AttributeHandler ah: fp.dataExtension.attributeHandlers ){
				double[] a = fp.getExtrema(ah.getNameorg());
				System.out.println(a[0] + " " + a[1] + " "  + a[2]);
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
		}
	}
}
