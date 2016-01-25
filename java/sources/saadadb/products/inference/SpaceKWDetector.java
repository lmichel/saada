package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.transformations.spatial.SpatialProjection;
import hecds.wcs.types.AxeType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnWcsSetter;
import saadadb.query.parser.PositionParser;
import saadadb.util.DBUtils;
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
			Messenger.printMsg(Messenger.DEBUG, "Look at WCS KW");
		if( this.projection != null && this.projection.isUsable() ){
			this.frameSetter    = new ColumnWcsSetter("astroframe", "WCS.getAstroFrame()", projection);
			this.ascension_kw   = new ColumnWcsSetter("s_ra", "WCS.getCenter(1)", projection);
			this.declination_kw = new ColumnWcsSetter("s_dec", "WCS.getCenter(2)", projection);
			this.err_maj        = new ColumnWcsSetter("s_resolution", "WCS.getWorldPixelSize()", projection);
			this.err_maj.setUnit("deg");
			this.status |= FRAME_FOUND;		
			this.status |= POS_KW_FOUND;
			this.status |= WCS_KW_FOUND;
			/*
			 * The region is stored as a string in the value field and as a List<Double> in the storedValue field
			 * That is not the standard purpose of storedValue but that avoids useless conversion String <> List
			 * There is no way to detect this behavior from the API. Just  remind it.
			 */
			this.region = new ColumnWcsSetter("s_region", "WCS.getWorldPixelRegion()", projection);
			this.fov = new ColumnWcsSetter("s_fov", "WCS.getFieldOfView()", projection);
		} else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No valid WCS");
		}
		if( (status & FRAME_FOUND) == 0 ) {
			ColumnExpressionSetter ah = search("astroframe", RegExp.FITS_COOSYS_UCD, RegExp.FITS_COOSYS_KW);
			ColumnExpressionSetter ahEq = search("equinox" , RegExp.FITS_EQUINOX_UCD, RegExp.FITS_EQUINOX_KW);
			ColumnExpressionSetter ahEp = search("epoch"   , RegExp.FITS_EPOCH_UCD, RegExp.FITS_EPOCH_KW);

			if( ah.isSet()) {
				String sframe=ah.getSingleAttributeHandler().getNameorg();
				if( ahEq.isSet()){
					sframe += "," + ahEq.getSingleAttributeHandler().getNameorg();
					if( ahEp.isSet()){
						sframe += "," + ahEp.getSingleAttributeHandler().getNameorg();
					}
				}					
				this.frameSetter = new ColumnExpressionSetter("astroframe", "getCoosys(" + sframe + ")", this.tableAttributeHandler, false);
				status |= FRAME_FOUND;
				/*
				 * coosys can be infered from equinox 2000 => FK5 e.g. 
				 */
			} else if(  ahEq.isSet() ) {
				this.frameSetter = new ColumnExpressionSetter("astroframe", "getCoosys(" + ahEq.getSingleAttributeHandler().getNameorg() + ")", this.tableAttributeHandler, false);
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
				if( exp.startsWith("FK4") || exp.startsWith("1950") || exp.startsWith("B1950")){
					this.lookForFK4Keywords();
				} else if( exp.startsWith("FK5")  || exp.startsWith("2000") || exp.startsWith("J2000")){
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
					this.frameSetter.setByValue(frame.toString(), false);
					this.frameSetter.completeDetectionMsg("Infered from the name of the position keywords");
					this.frameSetter.storedValue = frame;
				} else {
					this.frameSetter.completeDetectionMsg("Cannot guess it from the position keywords");
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
			this.ascension_kw.completeDetectionMsg(pp.getReport());
			this.declination_kw.completeDetectionMsg(pp.getReport());
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
						Messenger.printMsg(Messenger.DEBUG,  "Find position keywords " + ascension_kw.getAttNameOrg() + " " + declination_kw.getAttNameOrg()
								+ " tagged with coosys " + cooString);					this.status |= FRAME_FOUND;		
								this.frameSetter = new ColumnExpressionSetter("astroframe");
								this.frameSetter.setByValue(frame.toString(), false);
								this.frameSetter.completeDetectionMsg("infered from position keywords");
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
			retour.completeDetectionMsg(e.getMessage());
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
					this.ascension_kw.completeDetectionMsg("RA in hours (" +  ah.getComment() + "): convert in deg");
				}
			}
			return ascension_kw;
		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("s_ra");
			retour.completeDetectionMsg("No supported WCS - no simple keywords");
			return retour;
		}
			
	}

	/**
	 * @return the declination_kw
	 * @throws SaadaException 
	 */
	public ColumnExpressionSetter getDeclination() throws SaadaException {
		if( this.arePosColFound() )
			return declination_kw;
		else  {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("s_dec");
			retour.completeDetectionMsg("No supported WCS - no simple keywords");
			return retour;
		}
	}

	public boolean isFrameFound() throws SaadaException {
		this.init();
		if( (status & FRAME_FOUND) > 0  ) {
			return true;
		}
		else return false;
	}

	/**
	 * @return
	 * @throws SaadaException
	 */
	public boolean arePosColFound() throws SaadaException {
		this.init();
		if( (status & POS_KW_FOUND)> 0  ||  (status & WCS_KW_FOUND)> 0  ) {
			return true;
		}
		else return false;
	}

	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getSpatialError() throws Exception{
		ColumnExpressionSetter retour = this.err_maj;
		if( retour == null || retour.isNotSet() ){
			retour = search("s_resolution", RegExp.ERROR_MAJ_UCD, RegExp.ERROR_MAJ_KW);
		}
		if( retour == null  || retour.isNotSet()){
			retour = search("s_resolution", RegExp.ERROR_MIN_UCD, RegExp.ERROR_MIN_KW);
		}
		return retour;		
	}
		
	/**
	 * @return
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getfov() throws Exception{
		if( this.fov == null  || this.fov.isNotSet()){
			return search("s_fov", RegExp.FOV_UCD, RegExp.FOV_KW);
		}
		return fov;		
	}
	/**
	 * The value returned contain just a list of point. The coord system must be added later.
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getRegion() throws Exception{
		this.init();
		if( this.region == null  || this.region.isNotSet()){
			return search("s_region", RegExp.REGION_UCD, RegExp.REGION_KW);
		}
		return region;		
	}
	
	/**
	 * @return the CTYPE1 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSType1() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_CTYPE1", "String");
			setter.setValue(((SpatialProjection)(this.projection)).getCtype1Val());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_CTYPE1");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}

	/**
	 * @return the CTYPE2 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSType2() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_CTYPE2", "String");
			setter.setValue(((SpatialProjection)(this.projection)).getCtype1Val());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_CTYPE2");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CRPIX1 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrpix1() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_CRPIX1", "double");
			setter.setValue(((SpatialProjection)(this.projection)).getCrpix1Val());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_CRPIX1");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CRPIX2 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrpix2() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_CRPIX2", "double");
			setter.setValue(((SpatialProjection)(this.projection)).getCrpix2Val());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_CRPIX2");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CRVAL1 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrval1() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_VAL1", "double");
			setter.setValue(((SpatialProjection)(this.projection)).getCrval1Val());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_VAL1");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}

	/**
	 * @return the CRVAL2 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrval2() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_VAL2", "double");
			setter.setValue(((SpatialProjection)(this.projection)).getCrval2Val());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_VAL2");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}

	/**
	 * @return the CD11 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD11() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_D1_1", "double");
			setter.setValue(DBUtils.getSmallDouble(((SpatialProjection)(this.projection)).getCd11Val()));
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_D1_1");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CD12 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD12() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_D1_2", "double");
			setter.setValue(DBUtils.getSmallDouble(((SpatialProjection)(this.projection)).getCd12Val()));
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_D1_2");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CD22 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD21() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_D2_1", "double");
			setter.setValue(DBUtils.getSmallDouble(((SpatialProjection)(this.projection)).getCd21Val()));
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_D2_1");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CD22 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD22() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_D2_2", "double");
			setter.setValue(DBUtils.getSmallDouble(((SpatialProjection)(this.projection)).getCd22Val()));
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_D2_2");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}
	/**
	 * @return the CROTA WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCROTA() throws Exception {
		if(  (status & WCS_KW_FOUND) > 0 ) {
			ColumnExpressionSetter setter = getWCSSetter("WCS_CROTA", "double");
			setter.setValue(((SpatialProjection)(this.projection)).getRotaVal());
			setter.completeDetectionMsg("Read from WCS");
			return setter;

		} else {
			ColumnExpressionSetter  retour =  new ColumnExpressionSetter("WCS_CROTA");
			retour.completeDetectionMsg("No supported WCS");
			return retour;
		}
	}

	/**
	 * Build a column setter used to extract WCD keywords from WCS header
	 * @param colName
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private ColumnExpressionSetter getWCSSetter(String colName, String type) throws Exception{
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr(colName);
		ah.setNameorg(colName);
		ah.setType(type);
		return new ColumnExpressionSetter(colName, ah);

	}
}
