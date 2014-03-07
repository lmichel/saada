package saadadb.command;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ClassifierMode;
import saadadb.dataloader.mapping.PriorityMode;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.dataloader.mapping.RepositoryMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * Check the command line parameters
 * @author michel
 *
 */
public class ArgsParser implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name; // used to name the file where the parameters could be saved
	private String[] args;
	public static final Set<String> allowedArgs;
	static {
		allowedArgs = new TreeSet<String>();
		allowedArgs.add("-debug") ;
		allowedArgs.add("-classifier") ;
		allowedArgs.add("-coldef") ;
		allowedArgs.add("-classfusion") ;
		allowedArgs.add("-category") ;
		allowedArgs.add("-collection") ;
		allowedArgs.add("-relation") ;
		allowedArgs.add("-config") ;
		allowedArgs.add("-filename") ;
		allowedArgs.add("-extension") ;
		/*
		 * Observation Axe
		 */
		allowedArgs.add("-obsmaping") ; 
		allowedArgs.add("-name")    ; allowedArgs.add("-obsid") ;
		allowedArgs.add("-ename")   ; allowedArgs.add("-entry.obsid") ;
		allowedArgs.add("-obscollection") ; allowedArgs.add("-entry.obscollection") ;
		allowedArgs.add("-facility") ; allowedArgs.add("-entry.facility") ;
		allowedArgs.add("-instrument") ; allowedArgs.add("-entry.instrument") ;
		allowedArgs.add("-target")  ; allowedArgs.add("-entry.target") ;
		/*
		 * Space Axe
		 */
		allowedArgs.add("-posmapping") ;
		allowedArgs.add("-system") ;
		allowedArgs.add("-position")     ;allowedArgs.add("-entry.position") ;
		allowedArgs.add("-poserror")     ;allowedArgs.add("-entry.poserror") ;
		allowedArgs.add("-poserrorunit") ;allowedArgs.add("-entry.poserrorunit") ;
		/*
		 * Energy Axe
		 */
		allowedArgs.add("-spcmapping") ;
		allowedArgs.add("-spcunit")   ;allowedArgs.add("-entry.spcunit") ;
		allowedArgs.add("-spccolumn") ;allowedArgs.add("-entry.spccolumn") ;
		/*
		 * Time Axe
		 */
		allowedArgs.add("-timemapping") ;
		allowedArgs.add("-tmin")      ;allowedArgs.add("-entry.tmin") ;
		allowedArgs.add("-tmax")      ;allowedArgs.add("-entry.tmax") ;
		/*
		 * Other Axe
		 */
		allowedArgs.add("-ignore") ;
		allowedArgs.add("-eignore") ;
		allowedArgs.add("-ukw") ;
		allowedArgs.add("-eukw") ;
		
		allowedArgs.add("-empty") ;
		allowedArgs.add("-remove") ;
		allowedArgs.add("-index") ;
		allowedArgs.add("-populate") ;
		allowedArgs.add("-repository") ;
		allowedArgs.add("-links") ;
		allowedArgs.add("-filter") ;
		allowedArgs.add("-create") ;
		allowedArgs.add("-oids") ;
		allowedArgs.add("-force") ;
		allowedArgs.add("-debug") ;
		allowedArgs.add("-silent") ;
		allowedArgs.add("-comment") ;
		allowedArgs.add("-command") ;
		allowedArgs.add("-password") ;
		allowedArgs.add("-urlroot");
		allowedArgs.add("-basedir");
		allowedArgs.add("-repdir");
		allowedArgs.add("-rename");
		allowedArgs.add("-newname");
		allowedArgs.add("-nolog") ;
		allowedArgs.add("-noindex") ;
		allowedArgs.add("-novignette") ;
		allowedArgs.add("-query") ;
		allowedArgs.add("-build") ;
		allowedArgs.add("-all");
		allowedArgs.add("-continue");
		allowedArgs.add("-from");
		allowedArgs.add("-to");
		allowedArgs.add("-qualifiers");
		allowedArgs.add("-if") ;
		allowedArgs.add("-of") ;
		allowedArgs.add("-protocol") ;
		allowedArgs.add("-type") ;
		allowedArgs.add("-unit") ;
		allowedArgs.add("-ucd") ;
		allowedArgs.add("-utype") ;
	}
	/**
	 * @param args
	 * @throws FatalException
	 */
	public ArgsParser(String[] args) throws FatalException {
		if( args == null || args.length == 0 ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "No parameters given");
		}
		else {
			this.args = args;
			String msg="";
			for( int i=0 ; i<args.length ; i++ ) {
				String arg = args[i];
				if( arg.startsWith("-") ) {
					int pos = arg.indexOf('=');
					if( (pos == -1 && !allowedArgs.contains(arg))  || // param without =
					    (pos >= 0  && !allowedArgs.contains(arg.substring(0, pos))))  {
						msg += " <" + arg + ">";
						if( pos >=0 ) System.out.println(arg.substring(0, pos));
					}
				}
			}			
			this.setSilentMode();
			this.setDebugMode();
			this.setContinueMode();
			if( msg.length() > 0 ) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "The following parametres are not understood: " + msg);	
			}

		}

	}

	/**
	 * Return the last parameter, supposed to be the last
	 * @return
	 * @throws FatalException 
	 */
	public String getDBName() throws FatalException {
		/*
		 * DB name is supposed to be the last parameter
		 */
		return this.matches(args[args.length - 1], "database name", RegExp.DBNAME);
	}

	/**
	 * Returns the class mapping type (classifier or fusion) or throws an exception if not defined
	 * @return
	 * @throws FatalException
	 */
	public ClassifierMode getMappingType()  {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-classifier")) {
				return ClassifierMode.CLASSIFIER;
			}
			else if( args[i] .startsWith("-classfusion")) {
				return ClassifierMode.CLASS_FUSION;
			}
		}
		return ClassifierMode.NOCLASSIFICATION;
	}

	/**
	 * Returns the class name given as value of the class mapping type parameter
	 * @return
	 * @throws FatalException 
	 */
	public String getClassName() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-classifier") ||  args[i] .startsWith("-classfusion")) {
				String ret = this.matches(getArgsValue(args[i]), "-classifier/classfusion", RegExp.CLASSNAME);
				if( ret.matches(RegExp.FORBIDDEN_CLASSNAME)) {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, ret + ": Forbidden class name");					
				}
				else {
					return  ret;
				}
			}
		}
		return null;
	}

	/**
	 * returns a column definition formated like "name:type"
	 * @return
	 */
	public String[] getColdef() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-coldef")) {
				String [] retour =  getArgsValue(args[i]).split(":");
				if( retour.length == 2 ) {
					return retour;
				}
				else {
					return null;
				}
			}
		}
		return null;
	}	

	/**
	 * Returns the category specified by the arg -category=.....
	 * @return
	 * @throws FatalException
	 */
	public String getCategory() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-category")) {
				return this.matches(getArgsValue(args[i]), "-category", RegExp.CATEGORY);
			}
		}
		return null;
	}

	/**
	 * Returns the collection specified by the arg -collection=.....
	 * @return
	 * @throws FatalException
	 */
	public String getCollection() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-collection")) {
				return this.matches(getArgsValue(args[i]), "-collection", RegExp.COLLNAME);
			}
		}
		return null;
	}

	/**
	 * Returns the relation  specified by the arg -relation=.....
	 * @return
	 * @throws FatalException
	 */
	public String getRelation() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-relation")) {
				return this.matches(getArgsValue(args[i]), "-relation", RegExp.COLLNAME);
			}
		}
		return null;
	}
	/**
	 * Returns the type specified by the arg -type=.....
	 * used for adding extened attribues
	 * @return
	 * @throws FatalException
	 */
	public String getType() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-type")) {
				return this.matches(getArgsValue(args[i]), "-type", JavaTypeUtility.ATTREXTENDTYPES);
			}
		}
		return null;
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public String getUnit() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-unit")) {
				return getArgsValue(args[i]);
			}
		}
		return null;
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public String getUcd() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-ucd")) {
				return this.matches(getArgsValue(args[i]), "-ucd", RegExp.UCD);
			}
		}
		return null;
	}
	/**
	 * @return
	 * @throws FatalException
	 */
	public String getUtype() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-utype")) {
				return this.matches(getArgsValue(args[i]), "-utype", RegExp.UTYPE);
			}
		}
		return null;
	}

	/**
	 * Returns the data specified by the arg -filename=.....
	 * @return
	 */
	public String getFilename() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-filename")) {
				return getArgsValue(args[i]);
			}
		}
		return null;
	}

	/**
	 * Returns the config file specified by the arg -filename=.....
	 * @return
	 */
	public String getConfig() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-config")) {
				return getArgsValue(args[i]);
			}
		}
		return null;
	}
	/**
	 * Returns the collection specified by the arg -extension=.....
	 * Extension numbers are considered as strings here
	 * @return
	 */
	public String getExtension() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-extension")) {
				return getArgsValue(args[i]);
			}
		}
		return null;
	}

	/*
	 * OBSERVATION parameters
	 */
	public PriorityMode getObsMappingPriority() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-obsmapping")) {
				return  getPriority(args[i]);
			}
		}
		return null;		
	}
	public String[] getNameComponents(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && (args[i].startsWith("-name") || args[i].startsWith("-obsid")) ){
				return getArgsValue(args[i]).split(",");
			}
			if( entry && (args[i].startsWith("-ename") || args[i].startsWith("-entry.obsid")) ){
				return getArgsValue(args[i]).split(",");
			}
		}
		return new String[0];
	}	
	public String getObscollection(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && args[i].startsWith("-obscollection")  ){
				return getArgsValue(args[i]);
			}
			if( entry && args[i].startsWith("-entry.obscollection") ){
				return getArgsValue(args[i]);
			}
		}
		return null;
	}	
	public String getFacility(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && args[i].startsWith("-facility")  ){
				return getArgsValue(args[i]);
			}
			if( entry && args[i].startsWith("-entry.facility") ){
				return getArgsValue(args[i]);
			}
		}
		return null;
	}	
	public String getInstrument(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && args[i].startsWith("-instrument")  ){
				return getArgsValue(args[i]);
			}
			if( entry && args[i].startsWith("-entry.instrument") ){
				return getArgsValue(args[i]);
			}
		}
		return null;
	}	
	public String getTarget(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && args[i].startsWith("-target")  ){
				return getArgsValue(args[i]);
			}
			if( entry && args[i].startsWith("-entry.target") ){
				return getArgsValue(args[i]);
			}
		}
		return null;
	}	
	/*
	 * TIME
	 */
	public PriorityMode getTimeMappingPriority() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-timemapping")) {
				return  getPriority(args[i]);
			}
		}
		return null;		
	}
	public String getTmin(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && args[i].startsWith("-tmin")  ){
				return getArgsValue(args[i]);
			}
			if( entry && args[i].startsWith("-entry.tmin") ){
				return getArgsValue(args[i]);
			}
		}
		return null;
	}	
	public String getTmax(boolean entry) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( !entry && args[i].startsWith("-tmax")  ){
				return getArgsValue(args[i]);
			}
			if( entry && args[i].startsWith("-entry.tmax") ){
				return getArgsValue(args[i]);
			}
		}
		return null;
	}	

	/**
	 * returns a table with all ignored attributes -ignore=att1,att2,.....
	 * @return
	 */
	public String[] getIgnoredAttributes() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-ignore")) {
				return getArgsValue(args[i]).split(",");
			}
		}
		return new String[0];
	}

	/**
	 * returns a table with all instance ignored entry attributes  -eignore=att1,att2,.....
	 * @return
	 */
	public String[] getEntryIgnoredAttributes() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-eignore")) {
				return getArgsValue(args[i]).split(",");
			}
		}
		return new String[0];
	}

	/**
	 * Switch between entry and product parameters
	 * @param entry
	 * @return
	 */
	public String[] getIgnoredAttributes(boolean entry) {
		return (entry)?this.getEntryIgnoredAttributes(): this.getIgnoredAttributes();
	}	

	/**
	 * Returns the attribute mapping the user keyword user_kw: -ukw user_kw=attt ....
	 * @param user_kw
	 * @return
	 */
	public String getUserKeyword(String user_kw) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-ukw")) {
				if( i <= (args.length -2) && args[i+1] .startsWith(user_kw)) {
					return getArgsValue(args[i+1]);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the attribute mapping the user keyword user_kw for entries : -eukw user_kw=attt ....
	 * @param user_kw
	 * @return
	 */
	public String getEntryUserKeyword(String user_kw) {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-eukw")) {
				if( i <= (args.length -2) && args[i+1] .startsWith(user_kw)) {
					return getArgsValue(args[i+1]);
				}
			}
		}
		return null;

	}	
	/**
	 * Switch between entry and product parameters
	 * @param entry
	 * @return
	 */
	public String getUserKeyword(boolean entry, String user_kw) {
		return (entry)?this.getEntryUserKeyword(user_kw): this.getUserKeyword(user_kw);
	}	


	/**
	 * Returns the coordinate system mapping priority -sysmapping=only|last
	 * @return
	 */
	public PriorityMode getSysMappingPriority() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-sysmapping")) {
				return  getPriority(args[i]);
			}
		}
		return null;		
	}

	/**
	 * returns a table with all instance name components -system=system,equinox
	 * @return
	 */
	public String[] getCoordinateSystem() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-system")) {
				return getArgsValue(args[i]).split(",");
			}
		}
		return new String[0];
	}

	/**
	 * Returns the position mapping priority -posmapping=only|first|last
	 * @return
	 * @throws FatalException
	 */
	public PriorityMode getPositionMappingPriority() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-posmapping")) {
				return  getPriority(args[i]);
			}
		}
		return PriorityMode.LAST;		
	}

	/**
	 * returns a table with both position values (KW or values) -posmapping=KW_A,KW_D:
	 * @return
	 */
	public String[] getPositionMapping() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-position")) {
				String av = getArgsValue(args[i]) ;
				/*
				 * Object name are in '' or n ""
				 */
				if( (av.startsWith("'") && av.endsWith("'")) || (av.startsWith("\"") && av.endsWith("\"")) ) {
					//					return (new String[]{av.substring(1, av.length() -1)});
					return (new String[]{av});
				}
				else {
					/*
					 * Only one parameter which is not an object: Can only be
					 * a keywords with both position (e.g.1:2:3 +3:4:5)
					 */
					String[] ret = getArgsValue(args[i]).split(",");
					if( ret.length == 1 ) {
						return (new String[]{ret[0], ret[0]});						
					}
					/*
					 * Else return both parameters
					 */
					else {
						return ret;
					}
				}
			}
		}
		return new String[0];
	}

	/**
	 * Returns the position mapping priority -posmapping=only|first|last
	 * @return
	 * @throws FatalException
	 */
	public PriorityMode getPoserrorMappingPriority() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-poserrormapping=")) {
				return  getPriority(args[i]);
			}
		}
		return PriorityMode.LAST;		
	}
	/**
	 * returns a table with both position error values (KW or values) -poserror=KW_A,KW_D:
	 * @return
	 */
	public String[] getPoserrorMapping() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-poserror=")) {
				return getArgsValue(args[i]).split(",");
			}
		}
		return new String[0];
	}
	/**
	 * returns a table with both position error values (KW or values) -poserror=KW_A,KW_D:
	 * @return
	 */
	public String getPoserrorUnit() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-poserrorunit")) {
				return getArgsValue(args[i]);
			}
		}
		return null;
	}

	/**
	 * Returns the spectral coordinate mapping priority -spcmapping=only|first|last
	 * @return
	 * @throws FatalException
	 */
	public PriorityMode getSpectralMappingPriority() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-spcmapping")) {
				return  getPriority(args[i]);
			}
		}
		return PriorityMode.LAST;		
	}

	/**
	 * Returns the spectral coordinate unit -spcunit=....
	 * @return
	 */
	public String getSpectralUnit() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-spcunit")) {
				return getArgsValue(args[i]);
			}
		}
		return null;		
	}

	/**
	 * Returns the spectral coordinate column -spccolumn=....
	 * @return
	 */
	public String getSpectralColumn() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-spccolumn")) {
				return getArgsValue(args[i]);
			}
		}
		return null;		
	}

	/**
	 * Returns the comment -comment=....
	 * @return
	 */
	public String getComment() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-comment")) {
				return getArgsValue(args[i].replaceAll("[\\\"']", ""));
			}
		}
		return null;		
	}

	/**
	 * Returns the command -command=....
	 * @return
	 */
	public String getCommand() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-command")) {
				return getArgsValue(args[i].replaceAll("[\\\"']", ""));
			}
		}
		return null;		
	}

	/**
	 * Returns the password -password=....
	 * @return
	 */
	public String getPassword() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-password")) {
				return getArgsValue(args[i]);
			}
		}
		return null;
	}


	/**
	 * Returns the name of the entity to empty 
	 * @return
	 * @throws FatalException 
	 */
	public String getCreate() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-create")) {
				return this.matches(getArgsValue(args[i]), "-create", RegExp.COLLNAME);	
			}
		}
		return null;		
	}	

	/**
	 * Returns the name of the entity to empty 
	 * @return
	 * @throws FatalException 
	 */
	public String getEmpty() throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-empty")) {
				return this.matches(getArgsValue(args[i]), "-empty", RegExp.COLLNAME);	
			}
		}
		return null;		
	}

	/**
	 * Returns the name of the entity to remove 
	 * @return
	 */
	public String getRemove() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-remove")) {
				return getArgsValue(args[i]);	
			}
		}
		return null;		
	}

	/**
	 * Returns the name of the entity to remove 
	 * @return
	 */
	public String getIndex() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-index")) {
				return getArgsValue(args[i]);	
			}
		}
		return null;		
	}

	/**
	 * Returns the name of the entity to populate 
	 * @return
	 */
	public String getPopulate() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-populate")) {
				return getArgsValue(args[i]);	
			}
		}
		return null;		
	}

	/**
	 * Returns the repository strategy: 
	 * no: used the input file as repository entry
	 * move: Move the input file into the repository
	 * @return
	 */
	public RepositoryMode getRepository()  throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-repository")) {
				String v = getArgsValue(args[i]);
				if( v.equalsIgnoreCase("no")) {
					return RepositoryMode.KEEP;
				} else if( v.equalsIgnoreCase("copy") ) {
					return RepositoryMode.COPY;
				} else {
					return RepositoryMode.MOVE;
				}
			}
		}
		return RepositoryMode.MOVE;		
	}


	/**
	 * Return the links policy: follow: the action must be extended to attached data,
	 * ignore; it mustn't 
	 * @return
	 * @throws FatalException
	 */
	public String getLinks()  throws FatalException {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-links")) {
				return this.matches(getArgsValue(args[i]), "-links", RegExp.FOLLOWLINKS);							
			}
		}
		return null;		
	}

	/**
	 * The filter is used by the dataloader to filter the names of datafile
	 * @return
	 * @throws FatalException
	 */
	public String getFilter()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-filter=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}

	/**
	 * if is used as inpufile by some file convertors
	 * @return
	 * @throws FatalException
	 */
	public String getIf()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-if=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}

	/**
	 * if is used as output by some file convertors
	 * @return
	 * @throws FatalException
	 */
	public String getof()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-of=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}
	/**
	 * @return
	 * @throws FatalException
	 */
	public String getUrlroot() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-urlroot=")) {
				String param = getArgsValue(args[i]);
				if( param.matches(RegExp.URL)) {
					return param;
				}
				else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "url <" + param + "> badly formed");
				}
			}
		}
		return null;		
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public String getBasedir() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-basedir=")) {
				String param = getArgsValue(args[i]);
				File f = (new File(param));
				if( f.exists() && f.isDirectory() ) {
					return param;
				}
				else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot access to directory <" + param + ">");
				}
			}
		}
		return null;		
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public String getRepdir() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-repdir=")) {
				String param = getArgsValue(args[i]);
				File f = (new File(param));
				if( f.exists() && f.isDirectory() ) {
					return param;
				}
				else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Cannot access to directory <" + param + ">");
				}
			}
		}
		return null;		
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public String getRename() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-rename=")) {
				String param = getArgsValue(args[i]);
				if( param.matches(RegExp.DBNAME)) {
					return param;
				}
				else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "SaadaDB name <" + param + "> badly formed");
				}
			}
		}
		return null;		
	}
	/**
	 * @return
	 * @throws FatalException
	 */
	public String getNewname() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-newname=")) {
				String param = getArgsValue(args[i]);
				if( param.matches(RegExp.DBNAME)) {
					return param;
				}
				else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "SaadaDB name <" + param + "> badly formed");
				}
			}
		}
		return null;		
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public boolean isNolog() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-nolog")) {
				return true;
			}
		}
		return false;		
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public boolean isNoindex() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-noindex")) {
				return true;
			}
		}
		return false;		
	}

	/**
	 * @return
	 * @throws FatalException
	 */
	public boolean isNovignette() throws FatalException   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-novignette")) {
				return true;
			}
		}
		return false;		
	}

	/**
	 *  Returns the name of the entity to remove 
	 * @return
	 */
	public long[] getOids() throws Exception{
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-oids")) {
				String[] soids =  getArgsValue(args[i]).split("[,;]");
				long retour[] = new long[soids.length];
				for( int j=0 ; j<soids.length ; j++ ) {
					retour[j] = Long.parseLong(soids[j]);
				}
				return retour;
			}
		}
		return null;		
	}

	/**
	 * Switch on debug mode is the arg debug is found
	 */
	public void setDebugMode() {
		//Messenger.debug_mode = false;		
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-debug")) {
				String param = getArgsValue(args[i]);
				if( "off".equalsIgnoreCase(param) || "false".equalsIgnoreCase(param)
						|| "no".equalsIgnoreCase(param) || param == null || param.length() == 0) {
					Messenger.switchDebugOff();
				}
				else {
					Messenger.switchDebugOn();
				}
				return;
			}
		}
	}

	/**
	 * Switch on debug mode is the arg debug is found
	 */
	public void setContinueMode() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-continue")) {
				Messenger.printMsg(Messenger.TRACE, "Continue mode on");
				Messenger.ALWAYS_IGNORE = true;
				return;
			}
		}
		Messenger.ALWAYS_IGNORE = false;		
	}

	/**
	 * Switch on debug mode is the arg debug is found
	 */
	public void setSilentMode() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-silent")) {
				Messenger.debug_mode = false;
				Messenger.silent_mode = true;
				Messenger.printMsg(Messenger.TRACE, "Silent mode on");
				return;
			}
		}
		Messenger.silent_mode = false;
	}
	/**
	 * Returns the -force flag
	 */
	public boolean getDebugMode() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-debug")) {
				return true;
			}
		}
		return  false;		
	}
	/**
	 * Returns the -force flag
	 */
	public boolean getSilentMode() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-silent")) {
				return true;
			}
		}
		return  false;		
	}

	/**
	 * Returns the -force flag
	 */
	public boolean getForce() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-force")) {
				return true;
			}
		}
		return  false;		
	}
	/**
	 * Returns the -build flag
	 */
	public boolean getBuild() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-build")) {
				return true;
			}
		}
		return  false;		
	}
	/**
	 * Returns the -all flag
	 */
	public boolean getAll() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-all")) {
				return true;
			}
		}
		return  false;		
	}	
	/**
	 * Returns the -continue flag
	 */
	public boolean getContinue() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-continue")) {
				return true;
			}
		}
		return  false;		
	}	

	/**
	 * @return
	 */
	public String getQuery()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-query=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}

	/**
	 * Starting collection: collname_category
	 * @return
	 */
	public String getFrom()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-from=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}
	/**
	 * Ending collection: collname_category
	 * @return
	 */
	public String getTo()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-to=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}
	/**
	 * @return
	 */
	public String getProtocol()   {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-protocol=")) {
				return getArgsValue(args[i]);							
			}
		}
		return null;		
	}

	/**
	 * returns a list of qualifier names
	 * @return
	 */
	public String[] getQualifiers() {
		for( int i=0 ; i<args.length ; i++ ) {
			if( args[i] .startsWith("-qualifiers=")) {
				return getArgsValue(args[i]).split("[,;]");
			}
		}
		return new String[0];
	}


	/**
	 * Extract a priority from the parametee
	 * @param arg
	 * @return
	 */
	static public PriorityMode getPriority(String arg) {
		String v = getArgsValue(arg);
		if( v.equalsIgnoreCase("first")) {
			return PriorityMode.FIRST;
		} else if( v.equalsIgnoreCase("only") ) {
			return PriorityMode.ONLY;
		} else {
			return PriorityMode.LAST;
		}
	}


	/**
	 * returns the arg part after the '=' or an empty string
	 * @param arg
	 * @return
	 */
	static public String getArgsValue(String arg) {
		if( arg == null ) {
			return null;
		}
		int pos = arg.indexOf('=');
		if( pos == (arg.length() - 1) || pos == -1) {
			return "";
		}
		else {
			String retour =  arg.substring(pos+1).trim();
			if( retour.matches("\\$\\{.*\\}")) {
				Messenger.printMsg(Messenger.WARNING, "ant parameter not set: " + retour + " ignored");
				return "";
			}
			else {
				return retour;
			}
		}
	}


	/**
	 * Returns val if matches regexp, a thow a parsing exception else
	 * @param val
	 * @param param
	 * @param regexp
	 * @return
	 * @throws FatalException
	 */
	private String matches(String val, String param, String regexp) throws FatalException {
		if( val.matches(regexp) ) {
			return val;
		} else {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, val + ": Wrong value for parameter <" + param + ">");
			return "";
		}
	}
	/**
	 * Return val if it matches one of the string of authorizedValues
	 * @param val
	 * @param param
	 * @param authorizedValues
	 * @return
	 * @throws FatalException
	 */
	private String matches(String val, String param, String[] authorizedValues) throws FatalException {
		if( authorizedValues != null ){
			for( String v: authorizedValues ){
				if( v.equals(val) ) {
					return val;
				}
			}
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, val + ": Value not allowed for parameter <" + param + ">");
			return null;
		} else {
			return val;
		}
	}

	/**
	 * Returns a hasmap of type (param,values build with all parameters expect the last supposed to be the DB name
	 * @return
	 */
	public LinkedHashMap<String, String> getParamsMap() {
		LinkedHashMap<String, String> retour = new LinkedHashMap<String, String>() ;
		int cpt=0;
		for( String arg: this.args) {
			int pos = arg.indexOf('=');
			if( cpt == (this.args.length - 1) ) break;
			String pname = arg.substring(0, pos+1).trim().replaceAll("-", "");
			retour.put(pname, ArgsParser.getArgsValue(arg));
			cpt++;
		}
		return retour;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String params[] = {"-classifier=CLASSE", "dbname"};
		String params[][] = { {"-debug", "-poserrormapping=LAST", "-poserror=a,b,c", "-poserrorunit=arcsec", "-config=${config}", "AAAAA"}
		,{"-debug", "-poserrormapping=LAST", "-poserror=10.5,b,c", "-poserrorunit=arcsec", "-config={config}", "AAAAA"}};

		ArgsParser ap;
		try {
			for( int p=0 ; p<params.length ; p++) {

				ap = new ArgsParser(params[p]);
				String poserror[] = ap.getPoserrorMapping();
				System.out.print(poserror.length + " poserror params: ");
				for( int i=0 ; i<poserror.length ; i++) {
					System.out.print(poserror[i] + " " );
				}
				System.out.println();
				LinkedHashMap<String, String> pmap = ap.getParamsMap();
				for( Entry<String, String> e: pmap.entrySet() ) {
					System.out.println("Entry: " + e);
				}
			}
		} catch (FatalException e) {
			Messenger.printStackTrace(e);
		}
	}

	/**
	 * @return Returns the args.
	 */
	public String[] getArgs() {
		return args;
	}

	public String[] addDebugMode(boolean debug_mode) {
		ArrayList<String> al = new ArrayList<String>();

		if( debug_mode ) {
			al.add("-debug=on");			
		}
		else {
			al.add("-debug=off");			
		}
		for( int i=0 ; i<args.length ; i++ ) {
			al.add(args[i]);
		}
		al.add(Database.getDbname());
		return al.toArray(new String[0]);
	}

	/**
	 * @return Returns the args preceded by collection and followed by the dbname 
	 * used by the UI
	 */
	public String[] completeArgs(String collection, String filename, RepositoryMode repository, boolean noindex, boolean debug_mode) {
		ArrayList<String> al = new ArrayList<String>();
		if( collection != null && collection.length() > 0) {
			al.add("-collection="+ collection);
		}
		if( filename != null && filename.length() > 0) {
			al.add("-filename="+ filename);
		}
		if( repository != null ) {
			al.add("-repository="+ ((repository == RepositoryMode.KEEP)? "no" : "copy"));
		}
		if( debug_mode ) {
			al.add("-debug=on");			
		}
		if( noindex ) {
			al.add("-noindex");			
		}
		for( int i=0 ; i<args.length ; i++ ) {
			al.add(args[i]);
		}
		al.add(Database.getDbname());
		args = al.toArray(new String[0]);
		return args;
	}

	/**
	 * @return Returns the name.
	 */
	public  String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public  void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ProductMapping getProductMapping() throws SaadaException {
		switch( Category.getCategory(this.getCategory()) ) {
		case Category.TABLE: 
			Messenger.printMsg(Messenger.TRACE, "Build the TABLE configuration");
			return new ProductMapping("ConfigTable", this);
		case Category.IMAGE: 
			Messenger.printMsg(Messenger.TRACE, "Build the IMAGE configuration");
			return new ProductMapping("ConfigImage", this);
		case Category.SPECTRUM: 
			Messenger.printMsg(Messenger.TRACE, "Build the SPECTRUM configuration");
			return new ProductMapping("ConfigSpectrum", this);
		case Category.MISC: 
			Messenger.printMsg(Messenger.TRACE, "Build the SPECTRUM configuration");
			return new ProductMapping("ConfigMisc", this);
		case Category.FLATFILE: 
			Messenger.printMsg(Messenger.TRACE, "Build the FLATFILE configuration");
			return new ProductMapping("ConfigFlatfile", this);
		default: IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Category <" + this.getCategory() + "> not supported yet");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retour = "ArgsParser(";
		for( String p: this.args) {
			retour += p + " ";
		}
		return retour + ")";
	}

	/**
	 * @return
	 */
	public String toXML() {
		String retour = "";
		for( String p: this.args) {
			retour += "<arg value=\"" + p + "\"/>\n";
		}
		return retour;
	}

}
