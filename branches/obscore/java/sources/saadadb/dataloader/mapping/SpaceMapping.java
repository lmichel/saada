package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.CoordSystem;
import saadadb.products.ProductBuilder;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;

public class SpaceMapping extends AxisMapping {
    private CoordSystem coordSystem = new CoordSystem();
    private String errorUnit;

	/**
	 * @param ap
	 * @throws SaadaException
	 */
	SpaceMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		super(ap, new String[]{"s_ra", "s_dec","s_fov", "error_maj_csa", "error_min_csa", "error_angle_csa"}, entryMode);
		this.mapCoordSystem(ap);
		this.mapPosition(ap);
		this.mapPoserror(ap);
		this.completeColumns();
		this.priority = ap.getPositionMappingPriority();
	}
	
	/**
	 * Process the position mapping: can be tricky
	 * @param tabArg
	 * @throws SaadaException
	 */
	private void mapPosition(ArgsParser tabArg) throws SaadaException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set position mapping");
		String[] tabRa_dec;
		tabRa_dec = tabArg.getPositionMapping();
		/*
		 * One position parameter: must be an object name
		 */
		if( tabRa_dec.length == 1 ) {
			String av = tabRa_dec[0];
			/*
			 * Object name can be in '' or in ""
			 */
			if( (av.startsWith("'") && av.endsWith("'")) || (av.startsWith("\"") && av.endsWith("\"")) ) {
				av= av.substring(1, av.length() -1);
			}
			PositionParser pp = new PositionParser(av);
			if( pp.getFormat() == PositionParser.NOFORMAT ) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not resolve name <" + av + ">");
			} else {
				this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getRa()), ""));
				this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getDec()), ""));
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
					this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getRa()), ""));
					this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getDec()), ""));
				} else {
					this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.ATTRIBUTE, "deg", tabRa_dec[0], ""));
					this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.ATTRIBUTE, "deg", tabRa_dec[1], ""));
				}
			} catch(QueryException e) {
				this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.ATTRIBUTE, "deg", tabRa_dec[0], ""));
				this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.ATTRIBUTE, "deg", tabRa_dec[1], ""));
			}
		}
	}
	
	/**
	 * @param tabArg
	 * @throws FatalException
	 */
	public void mapPoserror(ArgsParser tabArg) throws FatalException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set up position error mapping");
		String[] tabRa_dec = tabArg.getPoserrorMapping();
		this.errorUnit = tabArg.getPoserrorUnit();
		/*
		 * One poserror parameter: error is supposed to be a circle
		 */
		if( tabRa_dec.length == 1 ) {
			this.columnMapping.put("error_angle_csa", new ColumnMapping(MappingMode.VALUE, "deg", "0.0", ""));
			if( tabRa_dec[0].matches("[0-9]+(\\.[0-9]+)?") ) {
				this.columnMapping.put("error_maj_csa", new ColumnMapping(MappingMode.VALUE, this.errorUnit, tabRa_dec[0], "error_maj_csa"));
				this.columnMapping.put("error_min_csa", new ColumnMapping(MappingMode.VALUE, this.errorUnit, tabRa_dec[0], "error_min_csa"));
			} else {
				this.columnMapping.put("error_maj_csa", new ColumnMapping(MappingMode.ATTRIBUTE, this.errorUnit, tabRa_dec[0], "error_maj_csa"));
				this.columnMapping.put("error_min_csa", new ColumnMapping(MappingMode.ATTRIBUTE, this.errorUnit, tabRa_dec[0], "error_min_csa"));
			}
		}
		/*
		 * 2 poserror parameters: Error is supposed to be a canonical ellipse
		 */
		else if( tabRa_dec.length == 2 ) {
			this.columnMapping.put("error_angle_csa", new ColumnMapping(MappingMode.VALUE, "deg", "0.0", "error_angle_csa"));
			if( tabRa_dec[0].matches("[0-9]+(\\.[0-9]+)?") ){
				this.columnMapping.put("error_maj_csa", new ColumnMapping(MappingMode.VALUE, this.errorUnit, tabRa_dec[0], "error_maj_csa"));
			} else {
				this.columnMapping.put("error_maj_csa", new ColumnMapping(MappingMode.ATTRIBUTE, this.errorUnit, tabRa_dec[0], "error_maj_csa"));
			}
			if( tabRa_dec[1].matches("[0-9]+(\\.[0-9]+)?") ){
				this.columnMapping.put("error_min_csa", new ColumnMapping(MappingMode.VALUE, this.errorUnit, tabRa_dec[1], "error_min_csa"));
			} else {
				this.columnMapping.put("error_min_csa", new ColumnMapping(MappingMode.ATTRIBUTE, this.errorUnit, tabRa_dec[1], "error_min_csa"));
			}
		}
		/*
		 * 3 poserror parameters: Error is supposed to be an oriented ellipse
		 * angle is taken between north and maj axis (see class Coord)
		 */
		else if( tabRa_dec.length >= 3 ) {
			if( tabRa_dec[0].matches("[0-9]+(\\.[0-9]+)?") ){
				this.columnMapping.put("error_maj_csa", new ColumnMapping(MappingMode.VALUE, this.errorUnit, tabRa_dec[0], "error_maj_csa"));
			} else {
				this.columnMapping.put("error_maj_csa", new ColumnMapping(MappingMode.ATTRIBUTE, this.errorUnit, tabRa_dec[0], "error_maj_csa"));
			}
			if( tabRa_dec[1].matches("[0-9]+(\\.[0-9]+)?") ){
				this.columnMapping.put("error_min_csa", new ColumnMapping(MappingMode.VALUE, this.errorUnit, tabRa_dec[1], "error_min_csa"));
			} else {
				this.columnMapping.put("error_min_csa", new ColumnMapping(MappingMode.ATTRIBUTE, this.errorUnit, tabRa_dec[1], "error_min_csa"));
			}
			if( tabRa_dec[2].matches("[0-9]+(\\.[0-9]+)?") ){
				this.columnMapping.put("error_angle_csa", new ColumnMapping(MappingMode.VALUE, "deg", tabRa_dec[2], "error_angle_csa"));
			} else {
				this.columnMapping.put("error_angle_csa", new ColumnMapping(MappingMode.ATTRIBUTE, "deg", tabRa_dec[2], "error_angle_csa"));
			}
		}
	}
	
	/**
	 * @param tabArg
	 * @throws FatalException
	 */
	private void mapCoordSystem(ArgsParser tabArg) throws FatalException {
		/*
		 * Coordinate system mapping
		 */
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for the coordinate SYSTEM and EQUINOX");
		
		String[] tabSys_eq ;
		tabSys_eq = tabArg.getCoordinateSystem();
		/*
		 * just a system, no equinox (galact e.g.)
		 */
		if( tabSys_eq.length == 1 ) {
			/*
			 * System can be given as a value enclosed in  "'" 
			 * or as a keyword name
			 */
			this.coordSystem.setEquinox("");
			this.coordSystem.setEquinox_value("");
			if( tabSys_eq[0].startsWith("'") ) {
				this.coordSystem.setSystem_value(tabSys_eq[0].replaceAll("'", ""));
			} else {
				this.coordSystem.setSystem(tabSys_eq[0]);
			}
		} else if( tabSys_eq.length == 2 ) {
			if( tabSys_eq[0].startsWith("'") ) {
				this.coordSystem.setSystem_value(tabSys_eq[0].replaceAll("'", ""));
			} else {
				this.coordSystem.setSystem(tabSys_eq[0]);
			}
			if( tabSys_eq[1].trim().endsWith("'") ) {
				this.coordSystem.setEquinox_value(tabSys_eq[1].replaceAll("'", ""));
			} else {
				this.coordSystem.setEquinox(tabSys_eq[1]);
			}
		} else{	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "You didn't specify a coordinate system. The loader'll try to detect them in the file.");
			this.coordSystem.setAutodedect();
		}
		
		if( priority == null ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : first. The values you've specified'll be taken.");
			this.coordSystem.setPriority(PriorityMode.FIRST);
		} else if( priority == PriorityMode.ONLY ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : only. The values you've specified'll be taken.");
			this.coordSystem.setPriority(PriorityMode.ONLY);
			if( this.coordSystem.getAutodedect() ) {
				Messenger.printMsg(Messenger.WARNING, "Coord system won't be set because it is required to be computed from mapping parameters, but mapping is not set.");
			}
		} else if(  priority == PriorityMode.FIRST ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : first. The values you've specified'll be taken first.");
			this.coordSystem.setPriority(PriorityMode.FIRST);
		} else if( priority == PriorityMode.LAST){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : last. The values you've specified'll be taken if there are not already defined in the file.");
			this.coordSystem.setPriority(PriorityMode.LAST);
		} else {			
			FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unknown coordinates system mapping priority <" + priority + ">");
		}
	}
	
	/**
	 * @return
	 */
	public String getErrorUnit(){
		return this.errorUnit;
	}
	/**
	 * Error Unit can be set from the {@link ProductBuilder#mapCollectionPoserrorAttributes}
	 * @param errorUnit
	 */
	public void setErrorUnit(String errorUnit){
		this.errorUnit = errorUnit;
	}
	/**
	 * @return
	 */
	public CoordSystem getCoordSystem() {
		return this.coordSystem;
	}
	
	
	public static void main(String[] args) throws SaadaException {
		Messenger.debug_mode = true;
		SpaceMapping om = new SpaceMapping(new ArgsParser(new String[]{"-poserror=abc,eee"}), false);
		System.out.println(om);
		om = new SpaceMapping(new ArgsParser(new String[]{"-poserror=abc,eee,0.7"}), false);
		System.out.println(om);	
		om = new SpaceMapping(new ArgsParser(new String[]{"-poserror=abc,eee,0.7", "-poserrorunit=deg" }), false);
		System.out.println(om);	
		om = new SpaceMapping(new ArgsParser(new String[]{"-poserror=1,3,0.7", "-poserrorunit=arcmin" }), false);
		System.out.println(om);	
		}

}
