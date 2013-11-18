package saadadb.classmapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.products.Product;
import saadadb.query.parser.PositionParser;
import saadadb.util.DefineType;
import saadadb.util.Messenger;


/**
 * @author laurentmichel
 * * @version $Id$
 * 11/2013: Position keywords ALPHA,DELTA were taken as an object name (Object 'ALPHA DELTA' really exist)

 */
public class Mapping {
	
	private static final long serialVersionUID = 1L;

	/**Configuration of this mapping**/
	protected ConfigurationDefaultHandler configuration;

	/**The object of the mapping type used in this configuration**/
	protected TypeMapping mapping;

	/**Class name of products in this mapping configuration of generated class Saada**/
	protected String className;

	/**Right ascencion in the equatorial system of coordinates of a celestial body**/
	protected String pos_ra;
	protected String pos_dec;
	private double dec_value;
	private double ra_value;
	private int posPriority=DefineType.FIRST;
	/*
	 * Error mapping: Errors are supposed to modeled by ellipse with oriented from North
	 * to major axis with and angle expressed in degree
	 */
	protected String maj_axis_error;
	protected String min_axis_error;
	protected String angle_ellipse_error;
	protected double maj_axis_error_value;
	protected double min_axis_error_value;
	protected double angle_ellipse_error_value;
	protected String error_unit;
	private int poserrorPriority=DefineType.FIRST;


	/**List of the ignored attributes in the products of this configuration**/
	protected ArrayList<String> ignored;

	/**List of the instanced names in the products of this configuration**/
	protected ArrayList<String> name_components;
	
	protected LinkedHashMap<String, String> extend_attr;


	/**Identification name of the xml element in the xml file of this configuration for the instanced name**/
	public static final String INSTANCE_NAME = "instance_name";

	/**Identification name of the xml element in the xml file of this configuration for the ignored attributes**/
	public static final String IGNORED_ATT = "ignored_att";

	/**Identification name of the xml element in the xml file of this configuration for the extended attributes**/
	public static final String ATTR_EXT = "attribute_extend";

	/**Constructor.
	 *@param Configuration the configuration of this mapping. 
	 * @throws IgnoreException 
	 * @throws FatalException 
	 */
	public Mapping(ConfigurationDefaultHandler configuration, ArgsParser tabArg) throws IgnoreException, FatalException {
		//Super constructor in hashtable class
		//Constructs a new, empty hashtable with a default initial capacity (11) and load factor, wich is 0.75
		super();
		this.configuration   = configuration;
		
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Setting the instance name...");
		this.name_components = new ArrayList<String>();
		String[] tabInstance;
		if( this.configuration.getCategorySaada() == Category.ENTRY ) {
			tabInstance= tabArg.getEntryNameComponents();
		}
		else {
			tabInstance= tabArg.getNameComponents();			
		}
		if( tabInstance.length != 0 ) {
			for(int i=0; i<tabInstance.length; i++)
				this.name_components.add(tabInstance[i].trim());			
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The instance name components are : "+this.getName_components());
				
		//setting the ignored attributes
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Setting the ignored attributes...");
		this.ignored   = new ArrayList<String>();
		String[]  tabIgnored;
		if( this.configuration.getCategorySaada() == Category.ENTRY ) {
			tabIgnored= tabArg.getEntryIgnoredAttributes();
		}
		else {
			tabIgnored= tabArg.getIgnoredAttributes();		
		}
		for( int j=0; j<tabIgnored.length; j++) {
			/*
			 * Ignored attribute can contain UNIX wild cards '*' which must be replaced 
			 * with RegExp wildcards '.*'
			 */
			this.ignored.add(tabIgnored[j].trim().replaceAll("\\*", ".*"));
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The ignored attributes are : "+this.getIgnoredAtt());    	
		
		/*
		 * Set extended attributes mapping
		 */
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Setting extended attributes");
		this.extend_attr = new LinkedHashMap<String,String>();
		String existing_att[] = Database.getCachemeta().getAtt_extend_names(this.configuration.getCategorySaada());
		for( int i=0 ; i<existing_att.length ; i++ ) {
			String mapped_att;
			/*
			 * Command line parameters for extend KW are different for entries
			 */
			if( this.configuration.getCategorySaada() == Category.ENTRY ) {
				mapped_att = tabArg.getEntryUserKeyword(existing_att[i]);
			}
			else {
				mapped_att = tabArg.getUserKeyword(existing_att[i]);				
			}
			if( mapped_att != null ) {
				if( mapped_att.length() == 0 ) {					
					IgnoreException.throwNewException(SaadaException.MAPPING_FAILURE, "There is no KW mapping the extended attribute <" + existing_att[i] + ">");
				}
				else {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Extended attribute <" + existing_att[i] + "> mapped with KW <"+ mapped_att + ">");
					this.extend_attr.put(existing_att[i],mapped_att );
				}
			}
		}

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Setting the mapping type...");
		this.className = tabArg.getClassName();

		if( this.className == null ) {
			this.className = "";
		}
		//test if the mapping type is defined
		switch(tabArg.getMappingType()) {
		case DefineType.TYPE_MAPPING_CLASSIFIER:
			if( this.className == null || this.className.length() == 0 ) {
				this.setTypeMapping("MAPPING_CLASSIFIER_SAADA");
			}
			else {
				this.setTypeMapping("MAPPING_CLASSIFIER_SAADA", this.className);			
			}
			break;
		case DefineType.TYPE_MAPPING_USER:
			if( this.className == null || this.className.length() == 0 ) {
				this.setTypeMapping("MAPPING_USER_SAADA");
			}
			else {
				this.setTypeMapping("MAPPING_USER_SAADA", this.className);			
			}
			break;
		default: 
			this.setTypeMapping("MAPPING_CLASSIFIER_SAADA", this.className);			
		}	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The mapping configuration is over.");
	}

	/**Sets the class name of products in this mapping configuration of generated class Saada,
	 * and creates the object modelling the mapping type used in this configuration.
	 * If the mapping type (the value in parameter) is the name of the loaded product in configuration file,
	 * the used mode is the user mode.
	 *@param String The name of mapping type, or the class name in user mode. 
	 *@return void.
	 */
	public void setTypeMapping(String typeMapping) {
  		if (typeMapping.equals("MAPPING_1_1_SAADA")) {
  			this.className = typeMapping;
  			mapping = new Mapping_1_1(configuration, typeMapping, className);
 			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_1_1_SAADA class: " + className);
 		} else if (typeMapping.equals("MAPPING_CLASSIFIER_SAADA")) {
  				this.className = "";
  				mapping = new Mapping_Classifier(configuration, typeMapping,
  						className);
 				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_CLASSIFIER_SAADA class: " + className);
 		} else {
  				//If the mapping type is the name of the loaded product in configuration file, the used mode is the user mode
  				this.className = "";
  				mapping = new Mapping_User(configuration, "MAPPING_USER_SAADA",
  						className);
 				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_USER_SAADA class: " + className);
  		}
	}
	
	/**
	 * @param typeMapping
	 * @param classname
	 */
	public void setTypeMapping(String typeMapping, String classname) {		//before : public void setTypeMapping(String typeMapping){...
		if (typeMapping.equals("MAPPING_1_1_SAADA")) {
			this.className = classname;		//before : this.className = typeMapping;
			mapping = new Mapping_1_1(configuration, typeMapping, className);
 			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_1_1_SAADA class: " + className);
		} else if (typeMapping.equals("MAPPING_CLASSIFIER_SAADA")) {
				this.className = classname;
				mapping = new Mapping_Classifier(configuration, typeMapping, className);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_CLASSIFIER_SAADA class: " +className );
		} else {
				//If the mapping type is the name of the loaded product in configuration file, the used mode is the user mode
				this.className = classname;
				mapping = new Mapping_User(configuration, "MAPPING_USER_SAADA", className);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_USER_SAADA class: " + className);
			
		}
	}




	/**This default method validates the mapping of the product in parameter.
	 * She tests if the product contains the key word defined in the list of instanced name.
	 *@param Product the tested product.
	 *@return boolean true or false if the tested product is valid.
	 */
	public boolean isProductValid(Product product) {
		/*
		 * The product validation should only concern the filename and the presence of
		 * requested keywords but not the mapping
		 * T
		 */
//		for( String comp: name_components) {
//			if (comp.equals("FILENAME") == false && product.hasValuedKW(comp)) {
//				Messenger.printMsg(Messenger.WARNING, "Attribute <" + comp + " requested to build the instance name has not been found");
//			}
//		}
		return true;
	}

	/**Sets the right ascencion in the equatorial system of coordinates of a celestial body.
	 *@param String The right ascencion in the equatorial system of coordinates of a celestial body.
	 *@return void.
	 */
	public void setPosRa(String pos_ra) {
		this.pos_ra = pos_ra;
	}
	
	public void setPosRaValue(double ra) {
		this.ra_value = ra;
	}

	/**Sets the declension in the equatorial system of coordinates of a celestial body.
	 *@param String The declension in the equatorial system of coordinates of a celestial body.
	 *@return void.
	 */
	public void setPosDec(String pos_dec) {
		this.pos_dec = pos_dec;
	}
	public void setPosDecValue(double dec) {
		this.dec_value = dec;
	}

	/**Returns the class name of products in this mapping configuration of generated class Saada.
	 *@return String The class name of products in this mapping configuration of generated class Saada.
	 */
	public String getClassName() {
		return className;
	}

	/**Returns the object of the mapping type used in this configuration.
	 *@return TypeMapping the object of the mapping type used in this configuration.
	 */
	public TypeMapping getTypeMapping() {
		return this.mapping;
	}

	/**Returns the right ascencion in the equatorial system of coordinates of a celestial body.
	 *@return String The right ascencion in the equatorial system of coordinates of a celestial body.
	 */
	public String getPosRa() {
		return this.pos_ra;
	}
	public double getPosRaValue() {
		return this.ra_value;
	}

	/**Returns the declension in the equatorial system of coordinates of a celestial body.
	 *@return String The declension in the equatorial system of coordinates of a celestial body.
	 */
	public String getPosDec() {
		return this.pos_dec;
	}
	public double getPosDecValue() {
		return this.dec_value;
	}

	/**Returns the list of the instanced names in the products of this configuration.
	 *@return Vector The list of the instanced names in the products of this configuration.
	 */
	public ArrayList<String> getName_components() {
		return name_components;
	}
	
	public void setName_components(ArrayList<String> vInstance){
		this.name_components = vInstance;
	}

	/**Returns the list which maps the name of extended attributes name to their mapping values.
	 * The list is this class.
	 *@return Hashtable The list which maps the name of extended attributes name to their mapping values.
	 */
	public LinkedHashMap<String, String> getAttrExt() {
		return this.extend_attr;
	}

	/**Returns the list of the ignored attributes in the products of this configuration.
	 *@return Vector The list of the ignored attributes in the products of this configuration.
	 */
	public ArrayList<String> getIgnoredAtt() {
		return ignored;
	}

	/**Returns the list of the names corresponding to the parameter in the products of this configuration.
	 *(The list of the ignored attributes or instanced names in the products of this configuration).
	 *@param String the standard name of the xml element corresponding to ignored attributes or instanced name.
	 *@return Vector The list of the ignored attributes or instanced names in the products of this configuration, or null (if no corresponding parameter).
	 */
	public ArrayList<String> getVector(String value) {
		//if the parameter is the standard name for the instanced name, returns the corresponding list.
		if (value.equals(INSTANCE_NAME)) {
			return name_components;
		} else {
			//if the parameter is the standard name for the ignored name, returns the corresponding list.
			if (value.equals(IGNORED_ATT)) {
				return ignored;
			}
		}
		//Return null
		return null;
	}

	public void setIgnoredAtt(ArrayList<String> ignored) {
		this.ignored = ignored;
	}
	
	
							/* ######################################################
							 * 
							 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
							 * 
							 *#######################################################*/
	
		//String represents the priority for attributes RA and DEC
	/**
	 * @return
	 */
	public int getPosPriority() {
		return posPriority;
	}

	/**
	 * @param posPriority
	 */
	public void setPosPriority(int posPriority) {
		this.posPriority = posPriority;
	}

	/**
	 * @param posPriority
	 */
	public void setPoserrorPriority(int posPriority) {
		this.poserrorPriority = posPriority;
	}
	/**
	 * @param posPriority
	 */
	public int getPoserrorPriority() {
		return this.poserrorPriority ;
	}
	
	
	/**
	 * @return Returns the angle_ellipse_error.
	 */
	public String getAngle_ellipse_error() {
		return angle_ellipse_error;
	}

	/**
	 * @return Returns the angle_ellipse_error_value.
	 */
	public double getAngle_ellipse_error_value() {
		return angle_ellipse_error_value;
	}
	/**
	 * @return Returns the maj_axis_error.
	 */
	public String getMaj_axis_error() {
		return maj_axis_error;
	}

	/**
	 * @return Returns the maj_axis_error_value.
	 */
	public double getMaj_axis_error_value() {
		return maj_axis_error_value;
	}

	/**
	 * @return Returns the min_axis_error.
	 */
	public String getMin_axis_error() {
		return min_axis_error;
	}

	/**
	 * @return Returns the min_axis_error_value.
	 */
	public double getMin_axis_error_value() {
		return min_axis_error_value;
	}

	/**
	 * Set the position mapping
	 * @param tabArg
	 * @throws SaadaException 
	 */
	public void mapPosition(ArgsParser tabArg) throws SaadaException {


		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set up position mapping");
		String[] tabRa_dec;
		tabRa_dec = tabArg.getPositionMapping();
		/*
		 * One position parameter: must be an object name
		 */
		if( tabRa_dec.length == 1 ) {
			String av = tabRa_dec[0];
			/*
			 * Object name can be in '' or n ""
			 */
			if( (av.startsWith("'") && av.endsWith("'")) || (av.startsWith("\"") && av.endsWith("\"")) ) {
				av= av.substring(1, av.length() -1);
			}
			PositionParser pp = new PositionParser(av);
			if( pp.getFormat() == PositionParser.NOFORMAT ) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not resolve name <" + av + ">");
			}
			else {
				this.setPosRaValue(pp.getRa());
				this.setPosDecValue(pp.getDec());				
				this.setPosRa("Numeric");
				this.setPosDec("Numeric");				
			}
		}
		/*
		 * 2 position parameters: can be either a numerical position or a couple of keywords
		 */
		else if( tabRa_dec.length == 2 ) {
			PositionParser pp = null;
			try {
				pp = new PositionParser(tabRa_dec[0] + " " + tabRa_dec[1]);		
				// Position keywords ALPHA,DELTA were taken as an object name, make a test to keep nulerical value only
				if( pp.getFormat() == PositionParser.HMS ||  pp.getFormat() == PositionParser.DECIMAL ) {
					this.setPosRaValue(pp.getRa());
					this.setPosDecValue(pp.getDec());				
					this.setPosRa("Numeric");
					this.setPosDec("Numeric");	
				} else {
					this.setPosRa(tabRa_dec[0]);
					this.setPosDec(tabRa_dec[1]);									
				}
			} catch(QueryException e) {
				this.setPosRa(tabRa_dec[0]);
				this.setPosDec(tabRa_dec[1]);				
			}
			/*
			 * 2 parameters not interpretable as a position: that are keywords
			 *
			if( pp.getFormat() == PositionParser.NOFORMAT ) {
				this.setPosRa(tabRa_dec[0]);
				this.setPosDec(tabRa_dec[1]);
			}*/
			//else {
			//}
		}
		/*
		 * No position parameter: position mustfound out automaticaly
		 */
		else {
			this.setPosDec("CoordAutoDetect");
			this.setPosRa("CoordAutoDetect");
		}
		/*
		 * Set up the mapping priority
		 */
		String priority=null;
		priority = tabArg.getPositionMappingPriority();
		if( priority == null  ){	
			priority = "last";		
		}
		else if( priority.equals("only") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position Priority : only. The values you've specified'll be taken.");
			this.setPosPriority(DefineType.ONLY);
			if( this. pos_ra.equals("CoordAutoDetect") ) {
				Messenger.printMsg(Messenger.WARNING, "Position won't be set because it is required to be computed from mapping parameters, but mapping is not set.");
			}
		}
		else if( priority.equals("first") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position Priority : first. The values you've specified'll be taken.");
			this.setPosPriority(DefineType.FIRST);
		}
		else if( priority.equals("last") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position Priority : last. The values you've specified'll be taken if there are not already defined in the file.");
			this.setPosPriority(DefineType.LAST);
		}
		else {			
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Unknown position mapping priority <" + priority + ">");
		}
	}
	

	/**
	 * Set the position mapping
	 * @param tabArg
	 * @throws FatalException 
	 */
	public void mapPoserror(ArgsParser tabArg) throws FatalException {

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set up position error mapping");
		String[] tabRa_dec = tabArg.getPoserrorMapping();
		/*
		 * One poserror parameter: error is supposed to be a circle
		 */
		if( tabRa_dec.length == 1 ) {
			this.angle_ellipse_error_value = 0.0;
			this.angle_ellipse_error = "Numeric";
			if( tabRa_dec[0].matches("[0-9]+(\\.[0-9]+)?") ){
				this.maj_axis_error_value = Double.parseDouble(tabRa_dec[0]);
				this.min_axis_error_value = this.maj_axis_error_value;
				this.maj_axis_error = "Numeric";
				this.min_axis_error = "Numeric";
			}
			else {
				this.maj_axis_error = tabRa_dec[0];
				this.min_axis_error = tabRa_dec[0];				
			}
		}
		/*
		 * 2 poserror parameters: Error is supposed to be a canonical ellipse
		 */
		else if( tabRa_dec.length == 2 ) {
			this.angle_ellipse_error_value = 0.0;
			this.angle_ellipse_error = "Numeric";
			if( tabRa_dec[0].matches("[0-9]+(\\.[0-9]+)?") ){
				this.maj_axis_error_value = Double.parseDouble(tabRa_dec[0]);
				this.maj_axis_error = "Numeric";
			}
			else {
				this.maj_axis_error = tabRa_dec[0];
			}
			if( tabRa_dec[1].matches("[0-9]+(\\.[0-9]+)?") ){
				this.min_axis_error_value = Double.parseDouble(tabRa_dec[1]);
				this.min_axis_error = "Numeric";
			}
			else {
				this.min_axis_error = tabRa_dec[1];
			}
		}
		/*
		 * 3 poserror parameters: Error is supposed to be an oriented ellipse
		 * angle is taken between north and maj axis (see class Coord)
		 */
		else if( tabRa_dec.length >= 3 ) {
			this.angle_ellipse_error_value = 0.0;
			this.angle_ellipse_error = "Numeric";
			if( tabRa_dec[0].matches("[0-9]+(\\.[0-9]+)?") ){
				this.maj_axis_error_value = Double.parseDouble(tabRa_dec[0]);
				this.maj_axis_error = "Numeric";
			}
			else {
				this.maj_axis_error = tabRa_dec[0];
			}
			if( tabRa_dec[1].matches("[0-9]+(\\.[0-9]+)?") ){
				this.min_axis_error_value = Double.parseDouble(tabRa_dec[1]);
				this.min_axis_error = "Numeric";
			}
			else {
				this.min_axis_error = tabRa_dec[1];
			}
			if( tabRa_dec[2].matches("[0-9]+(\\.[0-9]+)?") ){
				this.angle_ellipse_error_value = Double.parseDouble(tabRa_dec[2]);
				this.angle_ellipse_error = "Numeric";
			}
			else {
				this.angle_ellipse_error = tabRa_dec[2];
			}
		}
		/*
		 * No position parameter: position mustfound out automaticaly
		 */
		else {
			this.maj_axis_error = "CoordAutoDetect";
			this.min_axis_error = "CoordAutoDetect";
			this.angle_ellipse_error = "CoordAutoDetect";
		}
		this.error_unit = tabArg.getPoserrorUnit();
		/*
		 * Set up the mapping priority
		 */
		String priority="null";
		priority = tabArg.getPoserrorMappingPriority();
		
		if(priority == null ) {
			/*
			 * If a mapping is defined, the default priority is first (use the mapping first)
			 * Otherwise it is last
			 */
			if( this.maj_axis_error != null ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Pos Error Priority : last. The values you've specified'll be taken if there are not already defined in the file.");
				this.setPoserrorPriority(DefineType.FIRST);
			}
			else {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Pos Error Priority : last. The values you've specified'll be taken if there are not already defined in the file.");
				this.setPoserrorPriority(DefineType.LAST);				
			}
		}
		else if( priority.equals("only") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Pos Error Priority : only. The values you've specified'll be taken.");
			this.setPoserrorPriority(DefineType.ONLY);
			if( this.maj_axis_error.equals("CoordAutoDetect") ) {
				Messenger.printMsg(Messenger.WARNING, "Position error won't be set because it is required to be computed from mapping parameters, but mapping is not set.");
			}
		}
		else if( priority.equals("first") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Pos Error Priority : first. The values you've specified'll be taken.");
			this.setPoserrorPriority(DefineType.FIRST);
		}
	}


	/**
	 * @param error_unit The error_unit to set.
	 */
	public void setError_unit(String error_unit) {
		this.error_unit = error_unit;
	}

	/**
	 * @return Returns the error_unit.
	 */
	public String getError_unit() {
		return error_unit;
	}
}
