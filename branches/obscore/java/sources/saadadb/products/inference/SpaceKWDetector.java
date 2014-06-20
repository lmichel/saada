package saadadb.products.inference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import cds.astro.Astroframe;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;
import cds.savot.model.SavotCoosys;

/**
 * @author michel
 * @version $Id$
 *
 * 03/2012 Regulr expression pushed to {@link RegExp} to be used by the VO stuff
 */
public class SpaceKWDetector extends KWDetector{
	private ColumnSetter ascension_kw;
	private ColumnSetter declination_kw;
	private ColumnSetter err_min;
	private ColumnSetter err_maj;
	private ColumnSetter err_angle;
	private ColumnSetter fov;
	private ColumnSetter region;
	//private Astroframe frame = null;
	private ColumnSetter frameSetter = null;
	public static final int FRAME_FOUND = 1;
	public static final int POS_KW_FOUND = 2;
	public static final int WCS_KW_FOUND = 4;
	public static final int ERR_KW_FOUND = 8;
	private int status=0;
	private WCSModel wcsModel;
	private boolean isInit = false;


	/**
	 * @param tableAttributeHandler
	 * @throws SaadaException 
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler) throws SaadaException {
		super(tableAttributeHandler);

	}

	/**
	 * @param tableAttributeHandler
	 * @throws SaadaException 
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> columnsAttributeHandler) throws SaadaException {
		super(tableAttributeHandler);
		ColumnSetter ah = searchByName(RegExp.FITS_COOSYS_KW);
		lookForError();
		if( ah == null ) {
			ah = searchByUcd("pos.frame");
		}
		this.tableAttributeHandler = columnsAttributeHandler;
		//		if( ah != null ) {
		//			if( ah.getValue().toLowerCase().matches(RegExp.ECL_SYSTEM)) {
		//				this.frame = new Ecliptic();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in " + ah.getAttNameOrg() + ")");
		//				status |= FRAME_FOUND;
		//				lookForEclpiticKeywords();					
		//			} else if( ah.getValue().matches(RegExp.FK5_SYSTEM)) {
		//				this.frame = new FK5();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in " + ah.getAttNameOrg() + ")");
		//				status |= FRAME_FOUND;
		//				lookForFK5Keywords();					
		//			} else if( ah.getValue().matches(RegExp.FK4_SYSTEM)) {
		//				this.frame = new FK4();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in " + ah.getAttNameOrg() + ")");
		//				status |= FRAME_FOUND;
		//				lookForFK4Keywords();					
		//			} else if( ah.getValue().toLowerCase().matches(RegExp.GALACTIC_SYSTEM)) {
		//				this.frame = new Galactic();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in " + ah.getAttNameOrg() + ")");
		//				status |= FRAME_FOUND;
		//				lookForGalacticKeywords();					
		//			} else if( ah.getValue().toLowerCase().matches(RegExp.ICRS_SYSTEM)) {
		//				this.frame = new ICRS();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in " + ah.getAttNameOrg() + ")");
		//				status |= FRAME_FOUND;
		//				lookForICRSKeywords();					
		//			} else {
		//				Messenger.printMsg(Messenger.WARNING, "Unknown coordinate system <" +  ah.getValue() + ">");
		//			}
		//			if( (status & FRAME_FOUND) > 0 && (status & POS_KW_FOUND) == 0 && Messenger.debug_mode) {
		//				Messenger.printMsg(Messenger.DEBUG, "No keyword matching the system <" +  ah.getValue() + "> found. Try to find coordinate in another system");
		//			}
		//		}
		//		/*
		//		 * If no frame has been found, look for keywords and infers the frame from them
		//		 */
		//		if( (status & FRAME_FOUND) == 0 || (status & POS_KW_FOUND) == 0) {
		//			if (Messenger.debug_mode)
		//				Messenger.printMsg(Messenger.DEBUG, "No frame found, try to infer it from the position keywords");
		//			detectKeywordsandInferFrame();
		//		}
		//		if( (status & FRAME_FOUND) == 0) {
		//			if (Messenger.debug_mode)
		//				Messenger.printMsg(Messenger.DEBUG, "No coosys for an auto detection");
		//		}
		//		else if( (status & POS_KW_FOUND) == 0) {
		//			if (Messenger.debug_mode)
		//				Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW found for an auto detection");
		//		}
		//		this.lookForError();
	}

	/**
	 * @param infoCooSys
	 * @param tableAttributeHandler
	 * @throws SaadaException 
	 */
	public SpaceKWDetector(SavotCoosys infoCooSys, LinkedHashMap<String, AttributeHandler> tableAttributeHandler) throws SaadaException {
		super(tableAttributeHandler);
		this.lookForError();
		/* 
		 * Take first the coosys if not null and look for keywords matching that system
		 */
		//		if( infoCooSys != null ) {
		//			if( infoCooSys.getSystem().matches(RegExp.ECL_SYSTEM) ) {
		//				this.frame = new Ecliptic();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (Coosys Info = " + infoCooSys.getSystem() + ")");
		//				this.status |= FRAME_FOUND;
		//				this.lookForEclpiticKeywords();
		//			} else if( infoCooSys.getSystem().matches(RegExp.FK4_SYSTEM) ) {
		//				this.frame = new FK4();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (Coosys Info " +infoCooSys.getSystem() + ")");
		//				this.status |= FRAME_FOUND;
		//				this.lookForFK4Keywords();
		//			} else if( infoCooSys.getSystem().matches(RegExp.FK5_SYSTEM) ) {
		//				this.frame = new FK5();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (Coosys Info " + infoCooSys.getSystem() + ")");
		//				this.status |= FRAME_FOUND;
		//				this.lookForFK5Keywords();
		//			} else if( infoCooSys.getSystem().matches(RegExp.GALACTIC_SYSTEM) ) {
		//				this.frame = new Galactic();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (Coosys Info " + infoCooSys.getSystem() + ")");
		//				this.status |= FRAME_FOUND;
		//				this.lookForGalacticKeywords();
		//			} else if( infoCooSys.getSystem().matches(RegExp.ICRS_SYSTEM) ) {
		//				this.frame = new ICRS();
		//				if (Messenger.debug_mode)
		//					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (Coosys Info " + infoCooSys.getSystem() + ")");
		//				this.status |= FRAME_FOUND;
		//				this.lookForICRSKeywords();
		//			} else {
		//				Messenger.printMsg(Messenger.WARNING, "Unknown coordinate system <" +  infoCooSys.getSystem() + ">");
		//			}
		//			if( (this.status & FRAME_FOUND) > 0 && (status & POS_KW_FOUND) == 0 && Messenger.debug_mode) {
		//				Messenger.printMsg(Messenger.DEBUG, "No keyword matching the system <" +  infoCooSys.getSystem() + "> found. Try to find coordinate in another system");
		//			}
		//		}			
		//		/*
		//		 * If no frame has been found, look for keywords and infer the frame from them
		//		 */
		//		if( (status & FRAME_FOUND) == 0 || (status & POS_KW_FOUND) == 0) {
		//			if (Messenger.debug_mode)
		//				Messenger.printMsg(Messenger.DEBUG, "No frame found, try to infer it from the position keywords");
		//			this.detectKeywordsandInferFrame();
		//		}
		//		if( (status & FRAME_FOUND) == 0) {
		//			if (Messenger.debug_mode)
		//				Messenger.printMsg(Messenger.DEBUG, "No coosys for an auto detection");
		//		} else if( (status & POS_KW_FOUND) == 0) {
		//			if (Messenger.debug_mode)
		//				Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW found for an auto detection");
		//		}
		this.lookForError();
	}

	/**
	 * @throws Exception
	 */
	private void searchFrame() throws Exception {
		if( this.frameSetter != null ){
			return;
		}
		/*
		 * Search first an explicit mention of Frame
		 */
		this.frameSetter = new ColumnSetter();
		ColumnSetter ah = search("pos.frame", RegExp.FITS_COOSYS_KW);
		Astroframe frame = null;
		String message="";
		if( ah != null ) {
			if( ah.getValue().toLowerCase().matches(RegExp.ECL_SYSTEM)) {
				frame = new Ecliptic();
				message = "Take <" + frame + "> as frame (read in " + ah.getAttNameOrg() + ")";
				status |= FRAME_FOUND;
				this.frameSetter = ah;
			} else if( ah.getValue().matches(RegExp.FK5_SYSTEM)) {
				frame = new FK5();
				message = "Take <" + frame + "> as frame (read in " + ah.getAttNameOrg() + ")";
				status |= FRAME_FOUND;
				this.frameSetter = ah;
			} else if( ah.getValue().matches(RegExp.FK4_SYSTEM)) {
				frame = new FK4();
				message = "Take <" + frame + "> as frame (read in " + ah.getAttNameOrg() + ")";
				status |= FRAME_FOUND;
				this.frameSetter = ah;
			} else if( ah.getValue().toLowerCase().matches(RegExp.GALACTIC_SYSTEM)) {
				frame = new Galactic();
				message = "Take <" + frame + "> as frame (read in " + ah.getAttNameOrg() + ")";
				status |= FRAME_FOUND;
				this.frameSetter = ah;
			} else if( ah.getValue().toLowerCase().matches(RegExp.ICRS_SYSTEM)) {
				frame = new ICRS();
				message = "Take <" + frame + "> as frame (read in " + ah.getAttNameOrg() + ")";
				status |= FRAME_FOUND;
				this.frameSetter = ah;
			}
		}
		/*
		 * Then look at EQUINOX KW
		 */
		if( (status & FRAME_FOUND) == 0 ) {
			ah = searchByName(RegExp.FITS_EQUINOX);		
			if( ah != null ) {
				float val = Float.parseFloat(ah.getValue());
				if( val == 1950.f) {
					frame = new FK4();
					message = "Take <" + frame + "> as frame (infered from " + ah.getAttNameOrg() + ")";
					status |= FRAME_FOUND;
					this.frameSetter.setByValue("", false);
				} else if( val == 2000.f) {
					frame = new FK5();
					message = "Take <" + frame + "> as frame (infered from " + ah.getAttNameOrg() + ")";
					status |= FRAME_FOUND;
					this.frameSetter.setByValue("", false);
				}
			}
		}
		/*
		 * Then look at the WCS projection
		 */
		if( (status & FRAME_FOUND) == 0 ) {
			if( this.wcsModel == null )
				this.wcsModel = new WCSModel(tableAttributeHandler);
			if( this.wcsModel.isKwset_ok() ){
				ColumnSetter[] center;
				center = this.wcsModel.getGlonlatCenter();
				if( !center[0].notSet() && !center[1].notSet()) {
					frame = new Galactic();
					message = "Take <" + frame + "> as frame (infered from WCS)";
					status |= FRAME_FOUND;
					this.frameSetter.setByWCS("", false);
				}
				if( (status & FRAME_FOUND) == 0 ) {
					center = this.wcsModel.getElonlatCenter();
					if( !center[0].notSet() && !center[1].notSet()) {
						frame = new Ecliptic();
						message = "Take <" + frame + "> as frame (infered from WCS)";
						status |= FRAME_FOUND;
					}
					this.frameSetter.setByWCS("", false);
				}			
				if( (status & FRAME_FOUND) == 0 ) {
					center = this.wcsModel.getRadecCenter();
					if( !center[0].notSet() && !center[1].notSet()) {
						frame = new ICRS();
						message = "Take <" + frame + "> as frame (infered from WCS: RA/DEC without equinox considered as ICRS)";
						status |= FRAME_FOUND;
					}
					this.frameSetter.setByWCS("", false);
				}			
			}
		}
		if( (status & FRAME_FOUND) != 0 ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take " + frame + " found");
			this.frameSetter.completeMessage(message);
			this.frameSetter.storedValue = frame;
			this.frameSetter.byWcs();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, message);
		}	else {
			this.frameSetter.completeMessage("No frame found");
		}
	}

	/**
	 * @throws SaadaException
	 */
	private void init() throws SaadaException {
		try {
			if( this.isInit )
				return;
			this.searchFrame();
			if( (status & FRAME_FOUND) != 0 ) {
				lookForAstrometryInWCS();
				if( (status & POS_KW_FOUND) == 0 ) {
					detectKeywordsandInferFrame();				
				}
				if( (status & POS_KW_FOUND) == 0) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW detected");
				}
				this.lookForError();			
			}else  {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No coosys detected");
			}
		} catch (Exception e) {
			e.printStackTrace();
			IgnoreException.throwNewException(SaadaException.METADATA_ERROR, e);
		}
		this.isInit = true;
	}
	/**
	 * Look for the position in WCS keywords
	 * @throws Exception 
	 */
	private void lookForAstrometryInWCS() throws Exception {
		if( (this.status & WCS_KW_FOUND) != 0 )
			return;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Searching spatial coordinates in WCS keywords");

		if( this.wcsModel == null )
			this.wcsModel = new WCSModel(tableAttributeHandler);
		if( this.wcsModel.isKwset_ok() ){
			ColumnSetter[] center ;
			ColumnSetter[] 	ascRange ;
			ColumnSetter[] 	decRange ;
			ColumnSetter  	resolution ;
			center = this.wcsModel.getRadecCenter();
			if( center[0].notSet() || center[1].notSet()  )  {
				center = this.wcsModel.getGlonlatCenter();
				if( center[0].notSet() || center[1].notSet()  )  {
					center = this.wcsModel.getElonlatCenter();
					if( center[0].notSet() || center[1].notSet()  )  {
						Messenger.printMsg(Messenger.TRACE, "WCS projection not valid: can't find the center of the image");
						return;
					} else {
						ascRange   = this.wcsModel.getElonRange();
						decRange   = this.wcsModel.getElatRange();
						resolution = this.wcsModel.getElonlatResolution();
					}
				} else {						
					ascRange   = this.wcsModel.getGlonRange();
					decRange   = this.wcsModel.getGlatRange();
					resolution = this.wcsModel.getGlonlatResolution();
				}	
			}	else {
				ascRange   = this.wcsModel.getRaRange();
				decRange   = this.wcsModel.getDecRange();					
				resolution = this.wcsModel.getRadecResolution();
			}
			this.ascension_kw = center[0];
			this.declination_kw = center[1];
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take " + center[0].getValue() + " " + center[1].getValue() + " as image center");				
			this.err_maj = resolution;
			this.err_min = resolution;
			this.err_angle = new ColumnSetter();
			this.err_angle.setByWCS("0", false);
			double raMin = Double.parseDouble(ascRange[0].getValue());
			double raMax = Double.parseDouble(ascRange[1].getValue());
			double decMin = Double.parseDouble(decRange[0].getValue());
			double decMax = Double.parseDouble(decRange[1].getValue());
			AttributeHandler ah = new AttributeHandler();
			ah.setNameattr("s_fov");
			ah.setNameorg("s_fov");
			ah.setUnit("deg");
			ah.setUtype("Char.SpatialAxis.Coverage.Bounds.Extent.diameter");
			double fov = Math.abs(raMax - raMin);
			if( fov > 180 ) fov = 360 -fov;
			if( Math.abs(decMax - decMin) < fov ){
				fov = Math.abs(decMax - decMin);
				ah.setValue(Double.toString(fov));
				this.fov = new ColumnSetter(ah, ColumnSetMode.BY_WCS);
				this.fov.completeMessage("smaller image size taken (height)");							
			} else {
				ah.setValue(Double.toString(fov));
				this.fov = new ColumnSetter(ah, ColumnSetMode.BY_WCS);
				this.fov.completeMessage("smaller image size taken (width)");														
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take " +fov + " as fov");				
			ah = new AttributeHandler();
			ah.setNameattr("s_region");
			ah.setNameorg("s_region");
			ah.setUnit("deg");
			ah.setUtype("Char.SpatialAxis.Coverage.Support.Area");
			double[] pts = new double[8];
			pts[0] = raMin; pts[1] = decMax; 
			pts[2] = raMax; pts[3] = decMax; 
			pts[4] = raMax; pts[5] = decMin; 
			pts[6] = raMin; pts[7] = decMin; 
			this.region = new ColumnSetter(ah, ColumnSetMode.BY_WCS);
			this.region.completeMessage("Match the WCS rectangle");			
			this.region.storedValue = pts;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take " + (pts.length/2)  + " points for the region");
			this.status |= WCS_KW_FOUND;
			this.status |= POS_KW_FOUND;

		} else {
			Messenger.printMsg(Messenger.DEBUG, "WCS keywords not found");				
		}
	}
	/**
	 * @throws SaadaException
	 */
	private void lookForError() throws SaadaException {
		ColumnSetter eM=null, em=null, ea=null;
		if( this.err_maj != null && this.err_maj.byWcs() ) {
			eM = this.err_maj;
			em = this.err_min;
			ea = this.err_angle;
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for positional error keywords");		
		this.err_maj = this.searchByUcd(RegExp.ERROR_MAJ_UCD);
		if( this.err_maj == null ) this.err_maj = this.searchByUcd(RegExp.ERROR_MIN_KW);
		this.err_min = this.searchByUcd(RegExp.ERROR_MIN_UCD);
		if( this.err_min == null ) this.err_min = this.searchByUcd(RegExp.ERROR_MIN_KW);
		this.err_angle = this.searchByUcd(RegExp.ERROR_ANGLE_UCD);
		if( this.err_angle == null ) this.err_angle = this.searchByUcd(RegExp.ERROR_ANGLE_KW);
		if( this.err_maj == null && this.err_min != null) this.err_maj = this.err_min;
		if( this.err_min == null && this.err_maj != null) this.err_min = this.err_maj;
		
		if( this.err_maj.notSet() && eM != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keep WCS error values");
			this.err_maj = eM;
			this.err_min = em;
			if( this.err_angle.notSet() && ea != null ) {
				this.err_angle = ea;
			}

		}
	}
	/**
	 * @throws SaadaException 
	 * 
	 */
	public void detectKeywordsandInferFrame() throws SaadaException {
		if( (status & POS_KW_FOUND) == 0 ) {
			lookForICRSKeywords();
			Astroframe frame = null;
			if( (status & POS_KW_FOUND) != 0 ) {
				this.formatPos();
				frame = new ICRS();
			} else {
				lookForFK5Keywords();
				if( (status & POS_KW_FOUND) != 0 ) {
					this.formatPos();
					frame = new FK5();
				} else {
					lookForFK4Keywords();
					if( (status & POS_KW_FOUND) != 0 ) {
						this.formatPos();
						frame = new FK4();
					} else {
						lookForEclpiticKeywords();
						if( (status & POS_KW_FOUND) != 0 ) {
							this.formatPos();
							frame = new Ecliptic();
						} else {
							lookForGalacticKeywords();
							if( (status & POS_KW_FOUND) != 0 ) {
								this.formatPos();
								frame = new Galactic();
							}	
						}
					}
				}
			}

			if( frame != null ) {
				status |= FRAME_FOUND;		
				this.frameSetter = new ColumnSetter();
				this.frameSetter.setByValue("", false);
				this.frameSetter.completeMessage("Take <" + frame + "> as frame (infered from the name of the position keywords)");
				this.frameSetter.storedValue = frame;
			} else {
				this.frameSetter.completeMessage("Cannot guess the frame from the  from the name of the position keywords");
			}
		}
	}
	
	/**
	 * Makes sure coordiantes are in decimal 
	 */
	private void formatPos() {
		try {
			PositionParser pp = new PositionParser(this.ascension_kw.getValue().replaceAll("[+-]", "") + " " + this.declination_kw.getValue());
			this.ascension_kw.setValue(pp.getRa());
			this.declination_kw.setValue(pp.getDec());
		} catch (QueryException e) {
			Messenger.printMsg(Messenger.WARNING, "formatPos " 
					+ this.ascension_kw.getValue().replaceAll("[+-]", "") + " " + this.declination_kw.getValue() + " "  + e.getMessage());
		}
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	private void lookForFK5Keywords() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for FK5 keywords");
		List<ColumnSetter> posKW;
		posKW =  searchByUcd(RegExp.FK5_RA_MAINUCD,RegExp.FK5_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd(RegExp.FK5_RA_UCD,RegExp.FK5_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName(RegExp.FK5_RA_KW, RegExp.FK5_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	private void lookForFK4Keywords() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for FK4 keywords");
		List<ColumnSetter> posKW;
		posKW =  searchByUcd(RegExp.FK4_RA_MAINUCD,RegExp.FK4_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd(RegExp.FK4_RA_UCD,RegExp.FK4_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName(RegExp.FK4_RA_KW, RegExp.FK4_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}
	/**
	 * @throws SaadaException 
	 * 
	 */
	private void lookForICRSKeywords() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for ICRS keywords");
		List<ColumnSetter> posKW;
		posKW =  searchByUcd(RegExp.ICRS_RA_MAINUCD,RegExp.ICRS_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd(RegExp.ICRS_RA_UCD,RegExp.ICRS_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName(RegExp.ICRS_RA_KW, RegExp.ICRS_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}
	/**
	 * @throws SaadaException 
	 * 
	 */
	private void lookForEclpiticKeywords() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for Ecliptic keywords");
		List<ColumnSetter> posKW;
		posKW =  searchByUcd(RegExp.ECLIPTIC_RA_MAINUCD,RegExp.ECLIPTIC_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd(RegExp.ECLIPTIC_RA_UCD,RegExp.ECLIPTIC_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName(RegExp.ECLIPTIC_RA_KW, RegExp.ECLIPTIC_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}
	/**
	 * @throws SaadaException 
	 * 
	 */
	private void lookForGalacticKeywords() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for Galactic keywords");
		List<ColumnSetter> posKW;
		posKW =  searchByUcd(RegExp.GALACTIC_RA_MAINUCD,RegExp.GALACTIC_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd(RegExp.GALACTIC_RA_UCD,RegExp.GALACTIC_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName(RegExp.GALACTIC_RA_KW, RegExp.GALACTIC_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}
	/**
	 * @return the astro_frame
	 * @throws SaadaException 
	 */
	public ColumnSetter getFrame() throws SaadaException {
		try {
			this.searchFrame();
		} catch (Exception e) {
			IgnoreException.throwNewException(SaadaException.METADATA_ERROR, e);
		}
		return this.frameSetter;
	}

	/**
	 * @return the ascension_kw
	 * @throws SaadaException 
	 */
	public ColumnSetter getAscension() throws SaadaException {
		if( this.arePosColFound() )
			return ascension_kw;
		else 
			return null;
	}

	/**
	 * @return the declination_kw
	 * @throws SaadaException 
	 */
	public ColumnSetter getDeclination() throws SaadaException {
		if( this.arePosColFound() )
			return declination_kw;
		else 
			return null;
	}

	public boolean isFrameFound() throws SaadaException {
		this.init();
		if( (status & FRAME_FOUND) > 0  ) {
			return true;
		}
		else return false;
	}

	public boolean arePosColFound() throws SaadaException {
		this.init();
		if( (status & POS_KW_FOUND)> 0  ||  (status & WCS_KW_FOUND)> 0  ) {
			return true;
		}
		else return false;
	}

	public ColumnSetter getErrorMin() throws SaadaException{
		this.init();
		return err_min;
	}
	public ColumnSetter getErrorMaj() throws SaadaException{
		this.init();
		return err_maj;
	}
	public ColumnSetter getErrorAngle() throws SaadaException{
		this.init();
		return err_angle;		
	}
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnSetter getfov() throws SaadaException{
		// fov = Diameter (bounds) of the covered region in deg
		this.init();
		if( this.fov == null ){
			this.fov = this.search(RegExp.FOV_UCD, RegExp.FOV_KW);
		}
		return fov;		
	}
	/**
	 * The value returned contain just a list of point. The coord system must be added later.
	 * @return
	 * @throws SaadaException
	 */
	public ColumnSetter getRegion() throws SaadaException{
		this.init();
		if( this.region == null ){
			return new ColumnSetter();
		}
		return region;		
	}

}
