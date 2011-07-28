package saadadb.classmapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.products.Product;
import saadadb.util.DefineType;
import saadadb.util.Messenger;

public class MappingSpectrum extends Mapping {
    
	private static final long serialVersionUID = 1L;
	/**The name describing the column for the spectrum abcisse**/
    protected String abcisseColumn;
    /**The projection type of the spectrum abcisse**/
    protected String abcisseType;
    /**The unit of the spectrum abcisse**/
    protected String abcisseUnit;
    /**The name describing the column for the spectrum ordinate**/
    protected String ordinateColumn;
    /**The unit of the spectrum ordinate**/
    protected String ordinateUnit;
    /**Constructor of the spectra mapping (Default constructor).
     *@param Configuration the configuration of this spectra mapping. 
     * @throws ParsingException 
     */    
	private int scPriority=DefineType.FIRST;

	public MappingSpectrum(ConfigurationDefaultHandler configuration) throws IgnoreException, FatalException{
	//See MappingDefault (Default constructor)
	super(configuration, null);
    }

    /**This method valides the mapping of the product in parameter by default, 
     * validates the right ascencion and the declension in the equatorial system of coordinates of a celestial body in the product,
     * and sends trace message if necessary. 
     *@param Product the tested product.
     *@return boolean true or false if the tested product is valid.
     */
    public boolean isProductValid(Product product){
		
    	boolean valid = true;
    	boolean findRA=true;
    	boolean findDEC=true;
		//Test by default
		if(!super.isProductValid(product)){
		    valid = false;
		}
			//Tests if the product contains the key word defined by this pos_ra
			
		if(!product.hasValuedKW(pos_ra)){
		    Messenger.printMsg(Messenger.ERROR, "Pos_ra <"+pos_ra+"> doesn't found in "+product.getName());
		    findRA = false;
		}
			//Tests if the product contains the key word defined by this pos_dec
		if(!product.hasValuedKW(pos_dec)){
		    Messenger.printMsg(Messenger.WARNING, "Pos_dec <"+pos_dec+"> doesn't found in "+product.getName());
		    findDEC = false;
		}
		if(!findRA || !findDEC){
			valid=false;
		}
		return valid;
    }
    /**Sets the name describing the column for the spectrum abcisse.
     *@param String The name describing the column for the spectrum abcisse.
     *@return void.
     */
    public void setAbcisseColumn(String abcisseColumn){
	this.abcisseColumn = abcisseColumn;
    }
    /**Returns the name describing the column for the spectrum abcisse.
     *@return String The name describing the column for the spectrum abcisse.
     */
    public String getAbcisseColumn(){
	return abcisseColumn;
    }
    /**Sets the unit of the spectrum abcisse.
     *@param String The unit of the spectrum abcisse.
     *@return void.
     */
    public void setAbcisseUnit(String abcisseUnit){
	this.abcisseUnit = abcisseUnit;
    }
    /**Returns the unit of the spectrum abcisse.
     *@return String The unit of the spectrum abcisse.
     */
    public String getAbcisseUnit(){
	return abcisseUnit;
    }
    /**Sets the projection type of the spectrum abcisse.
     *@param String The projection type of the spectrum abcisse.
     *@return void.
     */
    public void setAbcisseType(String abcisseType){
	this.abcisseType = abcisseType;
    }
    /**Returns the projection type of the spectrum abcisse.
     *@return String The projection type of the spectrum abcisse.
     */
    public String getAbcisseType(){
	return abcisseType;
    }
    /**Sets the name describing the column for the spectrum ordinate.
     *@param String The name describing the column for the spectrum ordinate.
     *@return void.
     */
    public void setOrdinateColumn(String ordinateColumn){
	this.ordinateColumn = ordinateColumn;
    }
    /**Returns the name describing the column for the spectrum ordinate.
     *@return String The name describing the column for the spectrum ordinate.
     */
    public String getOrdinateColumn(){
	return ordinateColumn;
    }
    /**Sets the unit of the spectrum ordinate.
     *@param String the unit of the spectrum ordinate.
     *@return void.
     */
    public void setOrdinateUnit(String ordinateUnit){
	this.ordinateUnit = ordinateUnit;
    }
    /**Returns the unit of the spectrum ordinate.
     *@return String The unit of the spectrum ordinate.
     */
    public String getOrdinateUnit(){
	return ordinateUnit;
    }
    /**Tests if the name describing the column, the projection type and the unit of the spectrum abcisse are not null.
     *@return true or null if these variables are null, or not.
     */
    public boolean isAbcisse(){
	return ((abcisseColumn==null || abcisseType==null || abcisseUnit==null) && (abcisseColumn==null && abcisseType==null && abcisseUnit==null));
    }
    /**Tests if the name describing the column and the unit of the spectrum ordinate are not null.
     *@return true or null if these variables are null, or not.
     */
    public boolean isOrdinate(){
	return ((ordinateColumn==null || ordinateUnit==null) && (ordinateColumn==null && ordinateUnit==null));
    }
    
				    /* ######################################################
					 * 
					 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
					 * 
					 *#######################################################*/
    
    /**
     * @param configuration
     * @param tabArg
     * @throws SaadaException 
     */
    public MappingSpectrum(ConfigurationDefaultHandler configuration, ArgsParser tabArg) throws SaadaException{
    	
    	super(configuration, tabArg);
    	
    	this.mapPosition(tabArg);
    	this.mapPoserror(tabArg);
    	
    	String spcol = tabArg.getSpectralColumn();
    	if( spcol == null || spcol.length() == 0 ) {
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set up spectral coordinate mapping: autodetect");
    		this.abcisseColumn = "CoordAutoDetect";
    	}
    	else {
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set up spectral coordinate mapping : <" + spcol + ">");
    		this.abcisseColumn = spcol;
    	}
    	
      	if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Looking for spectral coordinate's unit...");
    	String unit ;
    	unit = tabArg.getSpectralUnit();
    	
    	if( unit == null || unit.equals("") ){
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "You didn't specified spectral coordinate's unit. The loader'll try to detect them in the file.");
    		this.abcisseUnit = "AutoDetect";
    	}
    	else {	//if the option -spcunit is present we use the specified valu
    		this.abcisseUnit = unit;
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Spectral coordinate's unit found : "+unit);
    	}
    	
      	if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Setup spectral coordinate mapping priority");
      	String priority;
 
      	priority = tabArg.getSpectralMappingPriority();
     	if( priority == null ) {
    		priority = "first";  		
    	}
    	if(priority.equals("only") ){
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : only. The unit specified'll be taken.");
    		this.scPriority = DefineType.ONLY;
    		if( this. abcisseColumn.equals("CoordAutoDetect") ) {
    			Messenger.printMsg(Messenger.WARNING, "Spectral coordinate won't be set because it is required to be computed from mapping parameters, but mapping is not set.");
    		}				
    	}else if( priority.equals("first") ){
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : first. The unit specified'll be taken.");
    		this.scPriority = DefineType.FIRST;
    	}else if( priority.equals("last") ){
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : last. The unit specified'll be taken if there is not one already defined in the file.");
    		this.scPriority = DefineType.LAST;
    		
    	}else{
    		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The default priority is taken. Priority : first");
    		this.scPriority = DefineType.FIRST;
    	}		
    	
    	if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The mapping configuration is over.");
    	
    }
	/**
	 * @return Returns the scPriority.
	 */
	public int getScPriority() {
		return scPriority;
	}
}
  
