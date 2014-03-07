package saadadb.products.inference;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import cds.astro.Astroframe;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;
import cds.savot.model.SavotCoosys;

/**
 * @author michel
 * @version $Id: SpaceFrame.java 939 2014-02-13 08:17:24Z laurent.mistahl $
 *
 * 03/2012 Regulr expression pushed to {@link RegExp} to be used by the VO stuff
 */
public class SpaceKWDetector extends KWDetector{
	private AttributeHandler ascension_kw;
	private AttributeHandler declination_kw;
	private AttributeHandler err_min;
	private AttributeHandler err_maj;
	private AttributeHandler err_angle;
	private AttributeHandler fov;
	private Astroframe frame = null;
	public static final int FRAME_FOUND = 1;
	public static final int POS_KW_FOUND = 2;
	private int status=0;

	
	/**
	 * @param tableAttributeHandler
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
		lookForError();
		AttributeHandler ah = searchByName(RegExp.FIST_COOSYS_KW);
		if( ah == null ) {
			ah = searchByUcd("pos.frame");
		}
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
	}

	/**
	 * @param tableAttributeHandler
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Map<String, AttributeHandler> columnsAttributeHandler) {
		super(tableAttributeHandler);
		AttributeHandler ah = searchByName(RegExp.FIST_COOSYS_KW);
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
	}

	/**
	 * @param infoCooSys
	 * @param tableAttributeHandler
	 */
	public SpaceKWDetector(SavotCoosys infoCooSys, LinkedHashMap<String, AttributeHandler> tableAttributeHandler) {
		super(tableAttributeHandler);
		lookForError();
		/* 
		 * Take first the coosys if not null and look for keywords matching that system
		 */
		if( infoCooSys != null ) {
			if( infoCooSys.getSystem().matches(RegExp.ECL_SYSTEM) ) {
				this.frame = new Ecliptic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForEclpiticKeywords();
			}
			else if( infoCooSys.getSystem().matches(RegExp.FK4_SYSTEM) ) {
				this.frame = new FK4();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForFK4Keywords();
			}
			else if( infoCooSys.getSystem().matches(RegExp.FK5_SYSTEM) ) {
				this.frame = new FK5();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForFK5Keywords();
			}
			else if( infoCooSys.getSystem().matches(RegExp.GALACTIC) ) {
				this.frame = new Galactic();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForGalacticKeywords();
			}
			else if( infoCooSys.getSystem().matches(RegExp.ICRS) ) {
				this.frame = new ICRS();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Could take <" + this.frame + "> as frame (read in coosys)");
				status |= FRAME_FOUND;
				lookForICRSKeywords();
			}
			else {
				Messenger.printMsg(Messenger.WARNING, "Unknown coordinate system <" +  infoCooSys.getSystem() + ">");
			}
			if( (status & FRAME_FOUND) > 0 && (status & POS_KW_FOUND) == 0 && Messenger.debug_mode) {
				Messenger.printMsg(Messenger.DEBUG, "No keyword matching the system <" +  infoCooSys.getSystem() + "> found. Try to find coordinate in another system");
			}
		}			
		/*
		 * If no frame has been found, look for keywords and infers the frame from them
		 */
		if( (status & FRAME_FOUND) == 0 || (status & POS_KW_FOUND) == 0) {
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
	}

	private void lookForError() {
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
	 * 
	 */
	public void detectKeywordsandInferFrame() {
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
	 * 
	 */
	private void lookForFK5Keywords() {
		/*
		 * Search by UCD first
		 */
		if( (ascension_kw = searchByUcd(RegExp.FK5_RA_MAINUCD)) != null && (declination_kw = searchByUcd(RegExp.FK5_DEC_MAINUCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an FK5 position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByUcd(RegExp.FK5_RA_UCD)) != null && (declination_kw = searchByUcd(RegExp.FK5_DEC_UCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an FK5 position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByName(RegExp.FK5_RA_KW)) != null && (declination_kw = searchByName(RegExp.FK5_DEC_KW))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an FK5 position (name)");
			status |= POS_KW_FOUND;
			return ;			
		}
	}

	/**
	 * 
	 */
	private void lookForFK4Keywords() {
		/*
		 * Search by UCD first
		 */
		if( (ascension_kw = searchByUcd(RegExp.FK4_RA_MAINUCD)) != null && (declination_kw = searchByUcd(RegExp.FK4_DEC_MAINUCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an FK4 position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByUcd(RegExp.FK4_RA_UCD)) != null && (declination_kw = searchByUcd(RegExp.FK4_DEC_UCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an FK4 position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByName(RegExp.FK4_RA_KW)) != null && (declination_kw = searchByName(RegExp.FK4_DEC_KW))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an FK4 position (name)");
			status |= POS_KW_FOUND;
			return ;			
		}
	}
	/**
	 * 
	 */
	private void lookForICRSKeywords() {
		/*
		 * Search by UCD first
		 */
		if( (ascension_kw = searchByUcd(RegExp.ICRS_RA_MAINUCD)) != null && (declination_kw = searchByUcd(RegExp.ICRS_DEC_MAINUCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an ICRS position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByUcd(RegExp.ICRS_RA_UCD)) != null && (declination_kw = searchByUcd(RegExp.ICRS_DEC_UCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an ICRS position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByName(RegExp.ICRS_RA_KW)) != null && (declination_kw = searchByName(RegExp.ICRS_DEC_KW))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an ICRS position (name)");
			status |= POS_KW_FOUND;
			return ;			
		}
	}
	/**
	 * 
	 */
	private void lookForEclpiticKeywords() {
		/*
		 * Search by UCD first
		 */
		if( (ascension_kw = searchByUcd(RegExp.ECLIPTIC_RA_MAINUCD)) != null && (declination_kw = searchByUcd(RegExp.ECLIPTIC_DEC_MAINUCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an Ecliptic position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByUcd(RegExp.ECLIPTIC_RA_UCD)) != null && (declination_kw = searchByUcd(RegExp.ECLIPTIC_DEC_UCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an Ecliptic position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByName(RegExp.ECLIPTIC_RA_KW)) != null && (declination_kw = searchByName(RegExp.ECLIPTIC_DEC_KW))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an Ecliptic position (name)");
			status |= POS_KW_FOUND;
			return ;			
		}
	}
	/**
	 * 
	 */
	private void lookForGalacticKeywords() {
		/*
		 * Search by UCD first
		 */
		if( (ascension_kw = searchByUcd(RegExp.GALACTIC_RA_MAINUCD)) != null && (declination_kw = searchByUcd(RegExp.GALACTIC_DEC_MAINUCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an Galactic position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByUcd(RegExp.GALACTIC_RA_UCD)) != null && (declination_kw = searchByUcd(RegExp.GALACTIC_DEC_UCD))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an Galactic position (ucd)");
			status |= POS_KW_FOUND;
			return ;			
		}
		if( (ascension_kw = searchByName(RegExp.GALACTIC_RA_KW)) != null && (declination_kw = searchByName(RegExp.GALACTIC_DEC_KW))  != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Keywords <" + ascension_kw.getNameorg() + "> and <" + declination_kw.getNameorg() + "> could be an Galactic position (name)");
			status |= POS_KW_FOUND;
			return ;			
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
	public AttributeHandler getAscension_kw() {
		if( (status & POS_KW_FOUND) > 0)
			return ascension_kw;
		else 
			return null;
	}

	/**
	 * @return the declination_kw
	 */
	public AttributeHandler getDeclination_kw() {
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
	
	public AttributeHandler getErrorMin(){
		return err_min;
	}
	public AttributeHandler getErrorMaj(){
		return err_maj;
	}
	public AttributeHandler getErrorAngle(){
		return err_angle;		
	}
	public AttributeHandler getfov(){
		if( this.fov == null ){
			this.fov = this.search(RegExp.FOV_UCD, RegExp.FOV_KW);
		}
		return fov;		
	}

}
