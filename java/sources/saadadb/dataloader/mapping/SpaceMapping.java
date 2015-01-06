package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.CoordSystem;
import saadadb.products.ProductBuilder;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.MappingMode;
import saadadb.vocabulary.enums.PriorityMode;

public class SpaceMapping extends AxisMapping {
    private String errorUnit;

	/**
	 * @param ap
	 * @throws SaadaException
	 */
	SpaceMapping(ArgsParser ap, boolean entryMode) throws SaadaException {
		super(ap, new String[]{"s_ra", "s_dec","s_fov","s_region", "s_resolution", "s_resolution_unit", "system"}, entryMode);
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
		tabRa_dec = tabArg.getPositionMapping(this.entryMode);
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
				this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.NOMAPPING, "deg", Double.toString(pp.getRa()), "Can not resolve name <" + av + ">"));
				this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.NOMAPPING, "deg", Double.toString(pp.getDec()), "Can not resolve name <" + av + ">"));
			} else {
				this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getRa()), "-position param"));
				this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getDec()), "-position param"));
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
					this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getRa()), "-position param"));
					this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.VALUE, "deg", Double.toString(pp.getDec()), "-position param"));
				} else {
					this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.KEYWORD, "deg", tabRa_dec[0], "-position param"));
					this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.KEYWORD, "deg", tabRa_dec[1], "-position param"));
				}
			} catch(QueryException e) {
				this.columnMapping.put("s_ra", new ColumnMapping(MappingMode.KEYWORD, "deg", tabRa_dec[0], "-position param"));
				this.columnMapping.put("s_dec", new ColumnMapping(MappingMode.KEYWORD, "deg", tabRa_dec[1], "-position param"));
			}
		}
		String s;
		if( (s = tabArg.getSFov()) != null  ){
			ColumnMapping cm = new ColumnMapping(null, s, "-sfov param");
			cm.extractUnit();
			this.columnMapping.put("s_fov", cm);
		} 
		if( (s = tabArg.getSRegion()) != null  ){
			this.columnMapping.put("s_region", new ColumnMapping(null, s, "-sregion param"));
		} 

	}
	
	/**
	 * @param tabArg
	 * @throws FatalException
	 */
	public void mapPoserror(ArgsParser tabArg) throws FatalException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set position error mapping");
		
		String av  = tabArg.getPoserrorMapping(this.entryMode);
		this.errorUnit = tabArg.getPoserrorUnit(this.entryMode);
		if( this.errorUnit != null ){
			this.columnMapping.put("s_resolution_unit", new ColumnMapping(null, this.errorUnit, "s_resolution_unit param"));				
		}
		/*
		 * One poserror parameter: error is supposed to be a circle
		 */
		if( av != null ) {
			ColumnMapping cm = new ColumnMapping(this.errorUnit, av, "sresolution param");				
			cm.extractUnit();
			this.columnMapping.put("s_resolution", cm);				
		}
	}
	
	/**
	 * @param tabArg
	 * @throws FatalException
	 */
	private void mapCoordSystem(ArgsParser tabArg) throws FatalException {
		String s;
		if( (s = tabArg.getCoordinateSystem()) != null  ){
			ColumnMapping cm = new ColumnMapping(null, s, "-system param");
			this.columnMapping.put("system", cm);
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
