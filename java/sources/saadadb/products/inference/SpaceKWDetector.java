package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.types.AxeType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.database.Database;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnWcsSetter;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import cds.astro.Astroframe;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

/**
 * @author michel
 * @version $Id$
 *
 * 03/2014 Regular expression pushed to {@link RegExp} to be used by the VO stuff
 * 10/2014 use of the WCS modeler
 */
public class SpaceKWDetector extends KWDetector{
	private ColumnExpressionSetter ascension_kw;
	private ColumnExpressionSetter declination_kw;
	private ColumnExpressionSetter err_min = new  ColumnExpressionSetter("err_min");
	private ColumnExpressionSetter err_maj= new  ColumnExpressionSetter("s_resolution");
	private ColumnExpressionSetter err_angle= new  ColumnExpressionSetter("err_angle");
	private ColumnExpressionSetter fov;
	private ColumnExpressionSetter region;
	//private Astroframe frame = null;
	private ColumnExpressionSetter frameSetter = null;
	public static final int FRAME_FOUND = 1;
	public static final int POS_KW_FOUND = 2;
	public static final int WCS_KW_FOUND = 4;
	public static final int ERR_KW_FOUND = 8;
	private int status=0;
	//private WCSModel wcsModel;
	private boolean isInit = false;
	private boolean errorSearched = false;
	private Astroframe frame = null;


	/**
	 * @param tableAttributeHandler
	 * @throws SaadaException 
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Modeler wcsModeler, List<String> comments) throws SaadaException {
		super(tableAttributeHandler, wcsModeler.getProjection(AxeType.SPACE));
	}

	/**
	 * @param tableAttributeHandler
	 * @throws SaadaException 
	 */
	public SpaceKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> columnsAttributeHandler, Modeler wcsModeler, List<String> comments) throws SaadaException {
		super(tableAttributeHandler, columnsAttributeHandler, wcsModeler.getProjection(AxeType.SPACE));
	}

	/**
	 * Search first by WCS, then by KW and finally by equinox
	 * @throws Exception
	 */
	private void searchFrame() throws Exception {
		if( this.frameSetter != null ){
			return;
		}
		/*
		 * Search first an explicit mention of Frame
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for astro frame in KW");
		this.frameSetter = new ColumnExpressionSetter("astroframe");
		/*
		 * look first in WCS
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look in WCS KW");
		if( this.projection != null && this.projection.isUsable() ){
			this.frameSetter    = new ColumnWcsSetter("astroframe", "WCS.getAstroFrame()", projection);
			this.ascension_kw   = new ColumnWcsSetter("s_ra", "WCS.getCenter(1)", projection);
			this.declination_kw = new ColumnWcsSetter("s_dec", "WCS.getCenter(2)", projection);
			this.err_maj = new ColumnWcsSetter("s_resolution", "WCS.getWorldPixelSize()", projection);
			this.err_maj.setUnit("deg");
			this.status |= FRAME_FOUND;		
			this.status |= POS_KW_FOUND;
			/*
			 * The region is stored as a string in the value field and as a List<Double> in the storedValue field
			 * That is not the standard purpose of storedValue but that avoids useless conversion String <> List
			 * There is no way to detect this behavior from the API. Just  remind it.
			 */
			this.region = new ColumnWcsSetter("s_region", "WCS.getWorldPixelRegion()", projection);
		} else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No valid WCS");
		}
		if( (status & FRAME_FOUND) == 0 ) {
			ColumnExpressionSetter ah = search("astroframe", RegExp.FITS_COOSYS_UCD, RegExp.FITS_COOSYS_KW);
			ColumnExpressionSetter ahEq = search("equinox" , RegExp.FITS_EQUINOX_UCD, RegExp.FITS_EQUINOX_KW);
			ColumnExpressionSetter ahEp = search("epoch"   , RegExp.FITS_EPOCH_UCD, RegExp.FITS_EPOCH_KW);

			String message= "";
			if( !ah.isNotSet()) {
				String sframe=ah.getSingleAttributeHandler().getNameorg();
				if( !ahEq.isNotSet()){
					sframe += "," + ahEq.getSingleAttributeHandler().getNameorg();
					if( !ahEp.isNotSet()){
						sframe += "," + ahEp.getSingleAttributeHandler().getNameorg();
					}
				}					
				this.frameSetter = new ColumnExpressionSetter("astroframe", "getCoosys(" + sframe + ")", this.tableAttributeHandler, false);
				status |= FRAME_FOUND;
			} 


		}
		/*
		 * Then look at EQUINOX KW
		 */
		if( (status & FRAME_FOUND) == 0 ) {
			ColumnExpressionSetter ah = searchByName("astroframe", RegExp.FITS_EQUINOX);		
			String message="";
			if( !ah.isNotSet() ) {
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
	}

	/**
	 * @throws SaadaException
	 */
	private void init() throws SaadaException {
		try {
			if( this.isInit )
				return;
			this.searchFrame();
			if( (this.status & FRAME_FOUND) != 0 && (this.status & POS_KW_FOUND) == 0) {
				String exp = this.frameSetter.getExpression();
				if( exp.startsWith("FK4") ){
					this.lookForFK4Keywords();
				} else if( exp.startsWith("FK5")  ){
					this.lookForFK5Keywords();
				} if( exp.startsWith("ICRS")  ){
					this.lookForICRSKeywords();
				} if( exp.startsWith("Galactic")  ){
					this.lookForGalacticKeywords();
				} if( exp.startsWith("Ecliptic")  ){
					this.lookForEclpiticKeywords();
				}
			}
			if( (status & FRAME_FOUND) == 0 ) {
				detectKeywordsandInferFrame();				
				if( (status & POS_KW_FOUND) == 0) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW detected");
				}
			} 
//			if( (status & FRAME_FOUND) == 0 ) {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "No coosys detected");				
//			} else  if( (status & POS_KW_FOUND) == 0 ) {
//				lookForAstrometryInWCS();
//				if( (status & POS_KW_FOUND) == 0 ) {
//					detectKeywordsandInferFrame();				
//				}
//				if( (status & POS_KW_FOUND) == 0) {
//					if (Messenger.debug_mode)
//						Messenger.printMsg(Messenger.DEBUG, "No coordinate columns or KW detected");
//				}	
//			}
			if( (status & FRAME_FOUND) != 0 && (status & POS_KW_FOUND) != 0) {
				//this.lookForError();							
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
	private void lookForAstrometryInWCSXXX() throws Exception {
		if( (this.status & WCS_KW_FOUND) != 0 )
			return;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Searching spatial coordinates in WCS keywords");

		if( this.projection != null &&  this.projection.isUsable() ){

			ColumnExpressionSetter[] 	ascRange ;
			ColumnExpressionSetter[] 	decRange ;
			ColumnExpressionSetter  	resolution ;
			//			center = this.wcsModel.getRadecCenter();
			//			if( center[0].notSet() || center[1].notSet()  )  {
			//				center = this.wcsModel.getGlonlatCenter();
			//				if( center[0].notSet() || center[1].notSet()  )  {
			//					center = this.wcsModel.getElonlatCenter();
			//					if( center[0].notSet() || center[1].notSet()  )  {
			//						Messenger.printMsg(Messenger.TRACE, "WCS projection not valid: can't find the center of the image");
			//						return;
			//					} else {
			//						ascRange   = this.wcsModel.getElonRange();
			//						decRange   = this.wcsModel.getElatRange();
			//						resolution = this.wcsModel.getElonlatResolution();
			//					}
			//				} else {						
			//					ascRange   = this.wcsModel.getGlonRange();
			//					decRange   = this.wcsModel.getGlatRange();
			//					resolution = this.wcsModel.getGlonlatResolution();
			//				}	
			//			}	else {
			//				ascRange   = this.wcsModel.getRaRange();
			//				decRange   = this.wcsModel.getDecRange();					
			//				resolution = this.wcsModel.getRadecResolution();
			//			}
			ascRange = new ColumnExpressionSetter[]{
					new ColumnWcsSetter("ra_min", "WCD.getMin(1)", projection),
					new ColumnWcsSetter("ra_max", "WCD.getMax(1)", projection),
			};
			decRange = new ColumnExpressionSetter[]{
					new ColumnWcsSetter("dec_min", "WCD.getMin(2)", projection),
					new ColumnWcsSetter("dec_max", "WCD.getMax(2)", projection)
			};
			resolution = new ColumnWcsSetter("s_resol", "WCD.getResolution()", projection);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Take " + this.ascension_kw.getValue() + " " + this.declination_kw.getValue() + " as image center");				
			this.err_maj = resolution;
			this.err_min = resolution;
			this.err_angle = new ColumnExpressionSetter("err_angle");
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
				ah.setValue(fov);
				//this.fov = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);
				this.fov = new ColumnExpressionSetter("s_fov", ah);
				this.fov.completeMessage("smaller image size taken (height)");	
				this.fov.setUnit("deg");
			} else {
				ah.setValue(fov);
				//this.fov = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);
				this.fov = new ColumnExpressionSetter("s_fov", ah);
				this.fov.completeMessage("smaller image size taken (width)");														
				this.fov.setUnit("deg");
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
			//this.region = new ColumnExpressionSetter(ah, ColumnSetMode.BY_WCS);
			this.region = new ColumnExpressionSetter("s_region", ah);
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
	 * Search for the position error either in WCS or in the keyword
	 * @throws Exception
	 */
	private void searchError() throws SaadaException {
		if( this.errorSearched ) {
			return;
		}
		try{
			this.errorSearched = true;
			//this.lookForAstrometryInWCS();
			this.lookForError();	
		} catch (Exception e) {}

	}

	/**
	 * @throws Exception 
	 */
	private void lookForError() throws Exception {
		ColumnExpressionSetter eM=null, em=null, ea=null;
		if( this.err_maj != null && this.err_maj.byWcs() ) {
			eM = this.err_maj;
			em = this.err_min;
			ea = this.err_angle;
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for positional error keywords");		
		this.err_min = this.search("err_min", RegExp.ERROR_UCD, RegExp.ERROR_KW);
		if( !this.err_min .isNotSet() ) {
			this.err_maj = this.err_min;
		} else {
			this.err_maj = this.search("err_maj", RegExp.ERROR_MAJ_UCD, RegExp.ERROR_MAJ_KW);		
			this.err_min = this.search("err_min", RegExp.ERROR_MIN_UCD, RegExp.ERROR_MIN_KW);

			this.err_angle = this.search("err_angle", RegExp.ERROR_ANGLE_UCD, RegExp.ERROR_ANGLE_KW);
			this.err_angle = this.searchByUcd("err_angle", RegExp.ERROR_ANGLE_UCD);
			if( this.err_angle == null ) this.err_angle = this.searchByUcd("err_angle", RegExp.ERROR_ANGLE_KW);
			if( this.err_maj == null && this.err_min != null) this.err_maj = this.err_min;
			if( this.err_min == null && this.err_maj != null) this.err_min = this.err_maj;

			if( this.err_maj.isNotSet() && eM != null ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Keep WCS error values");
				this.err_maj = eM;
				this.err_min = em;
				if( this.err_angle.isNotSet() && ea != null ) {
					this.err_angle = ea;
				}
			}
		}
	}
	/**
	 * @throws Exception 
	 * 
	 */
	public void detectKeywordsandInferFrame() throws Exception {
		Astroframe frame = null;
		if( (status & POS_KW_FOUND) == 0 ) {
			lookForTaggedKeywords();
			if( (status & POS_KW_FOUND) != 0 ) {
				this.formatPos();
				frame = new ICRS();
			} else {
				lookForICRSKeywords();
				frame = null;
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
			}
			if((this.status & FRAME_FOUND) == 0 ) {
				if( frame != null ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Take " + frame + " infered from position keywords");
					this.status |= FRAME_FOUND;		
					this.frameSetter = new ColumnExpressionSetter("astroframe");
					this.frameSetter.setByValue("", false);
					this.frameSetter.completeMessage("Take <" + frame + "> as frame (infered from the name of the position keywords)");
					this.frameSetter.storedValue = frame;
				} else {
					this.frameSetter.completeMessage("Cannot guess the frame from the  from the name of the position keywords");
				}
			}
		}
	}

	/**
	 * Makes sure coordiantes are in decimal 
	 */
	private void formatPos() {
		try {
			/*
			 * values not set: likely a table where AH are just colmun descriptions
			 */
			if( this.ascension_kw.getValue().length() == 0 &&  this.declination_kw.getValue().length() == 0 ) {
				return;
			}
			PositionParser pp = new PositionParser(this.ascension_kw.getValue().replaceAll("[+-]", "") + " " + this.declination_kw.getValue());
			this.ascension_kw.completeMessage(pp.getReport());
			this.declination_kw.completeMessage(pp.getReport());
			this.ascension_kw.setValue(pp.getRa());
			this.declination_kw.setValue(pp.getDec());
		} catch (QueryException e) {
			Messenger.printMsg(Messenger.TRACE, "formatPos " 
					+ this.ascension_kw.getValue().replaceAll("[+-]", "") + " " + this.declination_kw.getValue() + " "  + e.getMessage());
			this.status &= ~POS_KW_FOUND;
		}
	}


	/**
	 * Look for position keywords by using UCDs. To be valid, keywords must have an associate AH named COOSYS.
	 * Both COOSYS must have the same valid value.
	 * @throws Exception 
	 */
	private void lookForTaggedKeywords() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for tagged keyword");
		Astroframe frame=null;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for tagged  keywords");
		List<ColumnExpressionSetter> posKW;
		if( (posKW =  searchByUcd("s_ra", RegExp.RA_MAINUCD,"s_dec", RegExp.DEC_MAINUCD )).size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
		} else if( (posKW =  searchByUcd("s_ra", RegExp.RA_UCD,"s_dec", RegExp.DEC_UCD)).size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
		} else if( (posKW =  searchByUcd("s_ra", RegExp.ECLIPTIC_RA_MAINUCD,"s_dec", RegExp.ECLIPTIC_DEC_MAINUCD)).size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
		} else if( (posKW =  searchByUcd("s_ra", RegExp.ECLIPTIC_RA_UCD,"s_dec", RegExp.ECLIPTIC_DEC_UCD)).size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
		}else if( (posKW =  searchByUcd("s_ra", RegExp.GALACTIC_RA_MAINUCD,"s_dec", RegExp.GALACTIC_DEC_MAINUCD)).size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
		} else if( (posKW =  searchByUcd("s_ra", RegExp.GALACTIC_RA_UCD, "s_dec", RegExp.GALACTIC_DEC_UCD)).size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
		}

		if( ascension_kw == null || declination_kw == null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "not found");
			return ;
		}
		/*
		 * Look at he associate attribute which could carry the Coossys info.
		 * The associate attribute implements the if/ref mechanism of VOTables
		 */
		AttributeHandler raCoosys;
		AttributeHandler decCoosys;
		if( (raCoosys  = ascension_kw.getAssociateAtttribute()) != null && "COOSYS".equals(raCoosys.getNameorg()) &&
				(decCoosys = declination_kw.getAssociateAtttribute()) != null && "COOSYS".equals(decCoosys.getNameorg())){
			String cooString = raCoosys.getValue();
			if( cooString.equals(decCoosys.getValue()) ) {
				if( cooString.matches(RegExp.ICRS_SYSTEM)) {
					frame = new ICRS();
					status |= FRAME_FOUND;
					status |= POS_KW_FOUND;
				} else if( cooString.matches(RegExp.FK5_SYSTEM)) {
					frame = new FK5();
					status |= FRAME_FOUND;
					status |= POS_KW_FOUND;
				} else if( cooString.matches(RegExp.FK4_SYSTEM)) {
					frame = new FK4();
					status |= FRAME_FOUND;
					status |= POS_KW_FOUND;
				} else if( cooString.matches(RegExp.GALACTIC_SYSTEM)) {
					frame = new Galactic();
					status |= FRAME_FOUND;
					status |= POS_KW_FOUND;
				} else if( cooString.matches(RegExp.ECL_SYSTEM)) {
					frame = new Ecliptic();
					status |= FRAME_FOUND;
					status |= POS_KW_FOUND;
				} else {
					Messenger.printMsg(Messenger.TRACE, "Cooo system " + cooString + " not undestood: ignored ");
				}
				if( frame != null  ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG,  "Fing position keywords " + ascension_kw.getAttNameOrg() + " " + declination_kw.getAttNameOrg()
								+ " tagged with coosys " + cooString);					this.status |= FRAME_FOUND;		
								this.frameSetter = new ColumnExpressionSetter("astroframe");
								this.frameSetter.setByValue("", false);
								this.frameSetter.completeMessage("Take <" + frame + "> as frame (referenced by position keywords)");
								this.frameSetter.storedValue = frame;
				}
			} else {
				Messenger.printMsg(Messenger.TRACE, "Position keywords " + ascension_kw.getAttNameOrg() + " " + declination_kw.getAttNameOrg()
						+ " point different coo systems " + cooString + " and " + decCoosys.getValue() + ": ignored" );
			}
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void lookForFK5Keywords() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for FK5 keywords");
		List<ColumnExpressionSetter> posKW;
		posKW =  searchByUcd("s_ra", RegExp.RA_MAINUCD, "s_dec", RegExp.DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd("s_ra",RegExp.RA_UCD,"s_dec", RegExp.DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName("s_ra", RegExp.FK5_RA_KW,"s_dec",  RegExp.FK5_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void lookForFK4Keywords() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for FK4 keywords");
		List<ColumnExpressionSetter> posKW;
		posKW =  searchByUcd("s_ra", RegExp.RA_MAINUCD, "s_dec", RegExp.DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd("s_ra", RegExp.RA_UCD,"s_dec", RegExp.DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName("s_ra", RegExp.FK4_RA_KW,"s_dec",  RegExp.FK4_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void lookForICRSKeywords() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for ICRS keywords");
		List<ColumnExpressionSetter> posKW;
		posKW =  searchByUcd("s_ra", RegExp.RA_MAINUCD,"s_dec", RegExp.DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd("s_ra",RegExp.RA_UCD,"s_dec", RegExp.DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName("s_ra", RegExp.ICRS_RA_KW, "s_dec",RegExp.ICRS_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}
	/**
	 * @throws Exception 
	 * 
	 */
	private void lookForEclpiticKeywords() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for Ecliptic keywords");
		List<ColumnExpressionSetter> posKW;
		posKW =  searchByUcd("s_ra", RegExp.ECLIPTIC_RA_MAINUCD,"s_dec",RegExp.ECLIPTIC_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd("s_ra", RegExp.ECLIPTIC_RA_UCD,"s_dec",RegExp.ECLIPTIC_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName("s_ra", RegExp.ECLIPTIC_RA_KW, "s_dec",RegExp.ECLIPTIC_DEC_KW);
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
	}
	/**
	 * @throws Exception 
	 * 
	 */
	private void lookForGalacticKeywords() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for Galactic keywords");
		List<ColumnExpressionSetter> posKW;
		posKW =  searchByUcd("s_ra", RegExp.GALACTIC_RA_MAINUCD,"s_dec",RegExp.GALACTIC_DEC_MAINUCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByUcd("s_ra",RegExp.GALACTIC_RA_UCD,"s_dec",RegExp.GALACTIC_DEC_UCD );
		if( posKW.size() == 2 ) {
			ascension_kw = posKW.get(0);
			declination_kw = posKW.get(1);
			status |= POS_KW_FOUND;
			return;
		}
		posKW =  searchByName("s_ra",RegExp.GALACTIC_RA_KW, "s_dec",RegExp.GALACTIC_DEC_KW);
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
	public ColumnExpressionSetter getFrame() throws SaadaException {
		try {
			this.searchFrame();
		} catch (Exception e) {
			ColumnExpressionSetter retour = new ColumnExpressionSetter("astroframe");
			retour.completeMessage(e.getMessage());
			return retour;
		}
		return this.frameSetter;
	}

	/**
	 * @return the ascension_kw
	 * @throws SaadaException 
	 */
	public ColumnExpressionSetter getAscension() throws Exception {
		if( this.arePosColFound() ) {
			AttributeHandler ah;
			if( ascension_kw.byKeyword() && (ah = ascension_kw.getSingleAttributeHandler()) != null ) {
				if( ah.getComment().matches(".*(?i)(hour).*")) {
					Map<String , AttributeHandler> m = new  LinkedHashMap<String, AttributeHandler>();
					m.put(ah.getNameattr(), ah);
					this.ascension_kw = new ColumnExpressionSetter("s_ra", "15*" + ah.getNameorg(), m, true);
					this.ascension_kw.completeMessage("RA in hours (" +  ah.getComment() + "): convert in deg");
				}
			}
			return ascension_kw;
		} else 
			return null;
	}

	/**
	 * @return the declination_kw
	 * @throws SaadaException 
	 */
	public ColumnExpressionSetter getDeclination() throws SaadaException {
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

	public ColumnExpressionSetter getSpatialError() throws SaadaException{
		return (this.err_maj == null)? new ColumnExpressionSetter("s_resolution"): this.err_maj;
	}
	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getfov() throws Exception{
		// fov = Diameter (bounds) of the covered region in deg
		this.init();
		if( this.fov == null ){
			this.fov = this.search("s_fov", RegExp.FOV_UCD, RegExp.FOV_KW);
		}	
		if( this.fov.isNotSet() && (this.status & POS_KW_FOUND) != 0){
			AttributeHandler ah = new AttributeHandler();
			ah.setNameattr("s_fov");
			ah.setNameorg("s_fov");
			ah.setUnit("deg");
			ah.setUtype("Char.SpatialAxis.Coverage.Bounds.Extent.diameter");
			ah.setValue("0");
			//this.fov = new ColumnExpressionSetter(ah, ColumnSetMode.BY_VALUE);
			this.fov = new ColumnExpressionSetter("s_fov", ah);
			this.fov.completeMessage("Default value");														
		}
		return fov;		
	}
	/**
	 * The value returned contain just a list of point. The coord system must be added later.
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getRegion() throws SaadaException{
		this.init();
		if( this.region == null ){
			return new ColumnExpressionSetter("s_region");
		}
		return region;		
	}

}
