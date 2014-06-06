package saadadb.products.inference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
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
	private Astroframe frame = null;
	public static final int FRAME_FOUND = 1;
	public static final int POS_KW_FOUND = 2;
	private int status=0;
	private WCSModel wcsModel;


	/**
	 * @param tableAttributeHandler
	 * @throws FatalException 
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler) throws FatalException {
		super(tableAttributeHandler);
		lookForWCS();
		ColumnSetter ah = searchByName(RegExp.FIST_COOSYS_KW);
		if( ah == null ) {
			ah = searchByUcd("pos.frame");
		}
		if( ah != null ) {
			if( ah.getValue().toLowerCase().matches(".*ecliptic.*")) {
				this.frame = new Ecliptic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				if( (status & POS_KW_FOUND) == 0 ) lookForEclpiticKeywords();					
			} else if( ah.getValue().matches(".*FK5.*")) {
				this.frame = new FK5();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				if( (status & POS_KW_FOUND) == 0 ) lookForFK5Keywords();					
			} else if( ah.getValue().matches(".*FK4.*")) {
				this.frame = new FK4();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				if( (status & POS_KW_FOUND) == 0 ) lookForFK4Keywords();					
			} else if( ah.getValue().toLowerCase().matches(".*galactic.*")) {
				this.frame = new Galactic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				if( (status & POS_KW_FOUND) == 0 ) lookForGalacticKeywords();					
			} else if( ah.getValue().toLowerCase().matches(".*ICRS.*")) {
				this.frame = new ICRS();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				if( (status & POS_KW_FOUND) == 0 ) lookForICRSKeywords();					
			} else {
				Messenger.printMsg(Messenger.WARNING, "Unknown coordinate system <" +  ah.getValue() + ">");
			}
			if( (status & FRAME_FOUND) > 0 && (status & POS_KW_FOUND) == 0 && Messenger.debug_mode) {
				Messenger.printMsg(Messenger.DEBUG, "No keyword matching the system <" +  ah.getValue() + "> found. Try to find coordinate in another system");
			}
		}

		/*
		 * If no frame has been found, look for keywords and infers the frame from them
		 */
		if( (status & FRAME_FOUND) == 0 || (status & POS_KW_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No frame found, try to infer it from the position keywords");
			detectKeywordsandInferFrame();
		}
		if( (status & FRAME_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coosys for an auto detection");
		}
		else if( (status & POS_KW_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW found for an auto detection");
		}
		this.lookForError();
	}

	/**
	 * @param tableAttributeHandler
	 * @throws FatalException 
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> columnsAttributeHandler) throws FatalException {
		super(tableAttributeHandler);
		ColumnSetter ah = searchByName(RegExp.FIST_COOSYS_KW);
		lookForError();
		if( ah == null ) {
			ah = searchByUcd("pos.frame");
		}
		this.tableAttributeHandler = columnsAttributeHandler;
		if( ah != null ) {
			if( ah.getValue().toLowerCase().matches(".*ecliptic.*")) {
				this.frame = new Ecliptic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForEclpiticKeywords();					
			}
			else if( ah.getValue().matches(".*FK5.*")) {
				this.frame = new FK5();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForFK5Keywords();					
			}
			else if( ah.getValue().matches(".*FK4.*")) {
				this.frame = new FK4();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForFK4Keywords();					
			}
			else if( ah.getValue().toLowerCase().matches(".*galactic.*")) {
				this.frame = new Galactic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForGalacticKeywords();					
			}
			else if( ah.getValue().toLowerCase().matches(".*ICRS.*")) {
				this.frame = new ICRS();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForICRSKeywords();					
			}
			else {
				Messenger.printMsg(Messenger.WARNING, "Unknown coordinate system <" +  ah.getValue() + ">");
			}
			if( (status & FRAME_FOUND) > 0 && (status & POS_KW_FOUND) == 0 && Messenger.debug_mode) {
				Messenger.printMsg(Messenger.DEBUG, "No keyword matching the system <" +  ah.getValue() + "> found. Try to find coordinate in another system");
			}
		}

		/*
		 * If no frame has been found, look for keywords and infers the frame from them
		 */
		if( (status & FRAME_FOUND) == 0 || (status & POS_KW_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No frame found, try to infer it from the position keywords");
			detectKeywordsandInferFrame();
		}
		if( (status & FRAME_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coosys for an auto detection");
		}
		else if( (status & POS_KW_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW found for an auto detection");
		}
		this.lookForError();
	}

	/**
	 * @param infoCooSys
	 * @param tableAttributeHandler
	 * @throws FatalException 
	 */
	public SpaceKWDetector(SavotCoosys infoCooSys, LinkedHashMap<String, AttributeHandler> tableAttributeHandler) throws FatalException {
		super(tableAttributeHandler);
		this.lookForError();
		/* 
		 * Take first the coosys if not null and look for keywords matching that system
		 */
		if( infoCooSys != null ) {
			if( infoCooSys.getSystem().matches(RegExp.ECL_SYSTEM) ) {
				this.frame = new Ecliptic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				this.lookForEclpiticKeywords();
			} else if( infoCooSys.getSystem().matches(RegExp.FK4_SYSTEM) ) {
				this.frame = new FK4();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				this.lookForFK4Keywords();
			} else if( infoCooSys.getSystem().matches(RegExp.FK5_SYSTEM) ) {
				this.frame = new FK5();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				this.lookForFK5Keywords();
			} else if( infoCooSys.getSystem().matches(RegExp.GALACTIC) ) {
				this.frame = new Galactic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				this.lookForGalacticKeywords();
			} else if( infoCooSys.getSystem().matches(RegExp.ICRS) ) {
				this.frame = new ICRS();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				this.lookForICRSKeywords();
			} else {
				Messenger.printMsg(Messenger.WARNING, "Unknown coordinate system <" +  infoCooSys.getSystem() + ">");
			}
			if( (status & FRAME_FOUND) > 0 && (status & POS_KW_FOUND) == 0 && Messenger.debug_mode) {
				Messenger.printMsg(Messenger.DEBUG, "No keyword matching the system <" +  infoCooSys.getSystem() + "> found. Try to find coordinate in another system");
			}
		}			
		/*
		 * If no frame has been found, look for keywords and infer the frame from them
		 */
		if( (status & FRAME_FOUND) == 0 || (status & POS_KW_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No frame found, try to infer it from the position keywords");
			this.detectKeywordsandInferFrame();
		}
		if( (status & FRAME_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coosys for an auto detection");
		} else if( (status & POS_KW_FOUND) == 0) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW found for an auto detection");
		}
		this.lookForError();
	}

	/**
	 * Look for the position in WCS keywords
	 */
	private void lookForWCS() {
		try {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Searching spatial coordinates in WCS keywords");
			this.wcsModel = new WCSModel(tableAttributeHandler);
			this.ascension_kw = this.wcsModel.getCenterRaSetter();
			this.declination_kw = this.wcsModel.getCenterDecSetter();		
			double raMin = this.wcsModel.getRaMin();
			double raMax = this.wcsModel.getRaMax();
			double decMin = this.wcsModel.getDecMin();
			double decMax = this.wcsModel.getDecMax();
			if( this.ascension_kw.byWcs() && this.declination_kw.byWcs() ) {
					if( raMin != SaadaConstant.DOUBLE && !Double.isNaN(raMin) &&
							raMax != SaadaConstant.DOUBLE && !Double.isNaN(raMax) &&
							decMin != SaadaConstant.DOUBLE && !Double.isNaN(decMin) &&
							decMax != SaadaConstant.DOUBLE && !Double.isNaN(decMax) ) {
						double fov = Math.abs(raMax - raMin);
						AttributeHandler ah = new AttributeHandler();
						ah.setNameattr("s_fov");
						ah.setNameorg("s_fov");
						ah.setUnit("deg");
						ah.setUtype("Char.SpatialAxis.Coverage.Bounds.Extent.diameter");
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
						ah.setValue(raMin + " " + decMax + " " 
								  + raMax + " " + decMax + " "
								  + raMax + " " + decMin + " "
								  + raMin + " " + decMin
								  );
						this.region = new ColumnSetter(ah, ColumnSetMode.BY_WCS);
						this.region.completeMessage("Match the WCS rectangle");																			
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Take " +ah.getValue() + " as region");
					}
				this.status |= POS_KW_FOUND;
			} else {
				Messenger.printMsg(Messenger.DEBUG, "WCS keywords not found");				
			}
		} catch (Exception e) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "WCS keywords not valid " + e.getMessage());
		}
	}
	/**
	 * @throws FatalException
	 */
	private void lookForError() throws FatalException {
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
	}
	/**
	 * @throws FatalException 
	 * 
	 */
	public void detectKeywordsandInferFrame() throws FatalException {
		lookForICRSKeywords();
		if( (status & POS_KW_FOUND) != 0 ) {
			this.frame = new ICRS();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (auto detected)");
			status |= FRAME_FOUND;		
			return;
		}
		lookForFK5Keywords();
		if( (status & POS_KW_FOUND) != 0 ) {
			this.frame = new FK5();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (auto detected)");
			status |= FRAME_FOUND;		
			return;
		}
		lookForFK4Keywords();
		if( (status & POS_KW_FOUND) != 0 ) {
			this.frame = new FK4();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (auto detected)");
			status |= FRAME_FOUND;		
			return;
		}
		lookForEclpiticKeywords();
		if( (status & POS_KW_FOUND) != 0 ) {
			this.frame = new Ecliptic();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (auto detected)");
			status |= FRAME_FOUND;		
			return;
		}
		lookForGalacticKeywords();
		if( (status & POS_KW_FOUND) != 0 ) {
			this.frame = new Galactic();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (auto detected)");
			status |= FRAME_FOUND;		
			return;
		}		
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	private void lookForFK5Keywords() throws FatalException {
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
	 * @throws FatalException 
	 * 
	 */
	private void lookForFK4Keywords() throws FatalException {
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
	 * @throws FatalException 
	 * 
	 */
	private void lookForICRSKeywords() throws FatalException {
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
	 * @throws FatalException 
	 * 
	 */
	private void lookForEclpiticKeywords() throws FatalException {
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
	 * @throws FatalException 
	 * 
	 */
	private void lookForGalacticKeywords() throws FatalException {
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
	 */
	public Astroframe getFrame() {
		if( (status & FRAME_FOUND) > 0)
			return frame;
		else 
			return null;
	}

	/**
	 * @return the ascension_kw
	 */
	public ColumnSetter getAscension_kw() {
		if( (status & POS_KW_FOUND) > 0)
			return ascension_kw;
		else 
			return null;
	}

	/**
	 * @return the declination_kw
	 */
	public ColumnSetter getDeclination_kw() {
		if( (status & POS_KW_FOUND) > 0)
			return declination_kw;
		else 
			return null;
	}

	public boolean isFrameFound() {
		if( (status & FRAME_FOUND) > 0  ) {
			return true;
		}
		else return false;
	}

	public boolean arePosColFound() {
		if( (status & POS_KW_FOUND) > 0  ) {
			return true;
		}
		else return false;
	}

	public ColumnSetter getErrorMin(){
		return err_min;
	}
	public ColumnSetter getErrorMaj(){
		return err_maj;
	}
	public ColumnSetter getErrorAngle(){
		return err_angle;		
	}
	/**
	 * @return
	 * @throws FatalException
	 */
	public ColumnSetter getfov() throws FatalException{
		// fov = Diameter (bounds) of the covered region in deg
		if( this.fov == null ){
			this.fov = this.search(RegExp.FOV_UCD, RegExp.FOV_KW);
		}
		return fov;		
	}
	/**
	 * The value returned contain just a list of point. The coord system must be added later.
	 * @return
	 * @throws FatalException
	 */
	public ColumnSetter getRegion() throws FatalException{
		if( this.region == null ){
			return new ColumnSetter();
		}
		return region;		
	}

}
