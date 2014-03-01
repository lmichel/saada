package saadadb.vo.request.formator.sapmapper;

import java.io.File;

import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.Position;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SpectrumSaada;
import saadadb.collection.WCSSaada;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * This class does a rough mapping between the native fields of the saada instances and the fields defined by S*AP protocols
 * The same instance can be used with as many Saada instance as needed
 * When the object is initialized, it extracts the most from a casted Saada instance. That avoid  multiple access to Saada fields 
 * through the reflexion
 * The value of the current field is store in a public instance of @see SapMappedValue woch is read by the caller
 * 
 * @author michel
 * @version $Id$
 *
 */
/**
 * @author michel
 * @version $Id$
 *
 */
public class SapFieldMapper {
	/**
	 * Saadainstance to be mapped
	 */
	private SaadaInstance instance;	
	/**
	 * Down casted reference the current instance @see SapFieldMapper#instance
	 * (internal use)
	 */
	private Position pos_instance;
	private WCSSaada wcs_instance;
	private SpectrumSaada spec_instance;
	private ImageSaada img_instance;
	/**
	 * Flags marking the apablities of the @see SapFieldMapper#instance
	 * (internal use)
	 */
	private boolean hasPos = false;
	private boolean hasWcs = false;
	private boolean isProduct = false;
	private boolean isImage = false;
	private boolean hasSpRange = false;
	/**
	 * Values extracted from  @see SapFieldMapper#instance
	 * Used to compute composed field vales
	 */
	private File  file;
	private String ra, dec;
	private int naxis1, naxis2;
	private String wcs_rota;
	private String wcs_cd11;
	private String wcs_cd21;
	private String wcs_cd22;
	private String wcs_cd12;
	private String wcs_val1;
	private String wcs_val2;
	private String wcs_pix1;
	private String wcs_pix2;
	private String wcs_type2;
	private String wcs_type1;
	private double x_min;
	private double x_max;
	private String x_unit;
	private double size_alpha;
	private double size_delta;
	/**
	 * Container of the result for the current field @see SapMappedValue
	 */
	public SapMappedValue value = new SapMappedValue();

	/**
	 * Reset the object and connect it to a new SaadaInstance
	 * Extract all value possibly useful according the category of the object
	 * @param instance
	 */
	public void setInstance(SaadaInstance instance) {
		this.instance = instance;
		switch( this.instance.getCategory() ) {
		case Category.FLATFILE: 
		case Category.TABLE: 
		case Category.MISC: setProduct();break;
		case Category.ENTRY: setPos();break;
		case Category.IMAGE: setProduct();setPos();setWcs();setImg();break;
		case Category.SPECTRUM: setProduct();setPos();setWcs();setSpec();break;
		}
		setNotSetValue();
	}
	/**
	 * Extract fields for a downloadable Saada instance
	 */
	private void setProduct() {
		try {
			file = new File(instance.getRepositoryPath());
			if( !file.exists()) {
				file=null;
			}	
		} catch (SaadaException e) {
			file=null;
		}
		this.isProduct=true;
	}
	/**
	 * Extract fields for a Saada instance with a sky position
	 */
	private void setPos() {
		this.pos_instance = (Position) this.instance;
		ra =  this.valToString(this.pos_instance.getPos_ra_csa());
		dec = this.valToString(this.pos_instance.getPos_dec_csa());
		this.hasPos=true;
	}
	/**
	 * Extract fields for a Saada instance with WCS keywords
	 */
	private void setWcs() {
		this.wcs_instance = (WCSSaada) this.instance;
		wcs_rota =  valToString(this.wcs_instance.getCrota_csa());
		wcs_cd11 =  valToString(this.wcs_instance.getCd1_1_csa());
		wcs_cd12 =  valToString(this.wcs_instance.getCd1_2_csa());
		wcs_cd21 =  valToString(this.wcs_instance.getCd2_1_csa());
		wcs_cd22 =  valToString(this.wcs_instance.getCd2_2_csa());
		wcs_val1 =  valToString(this.wcs_instance.getCrval1_csa());
		wcs_val2 =  valToString(this.wcs_instance.getCrval2_csa());
		wcs_pix1 =  valToString(this.wcs_instance.getCrpix1_csa());
		wcs_pix2 =  valToString(this.wcs_instance.getCrpix2_csa());
		wcs_type1 =  valToString(this.wcs_instance.getCtype1_csa());
		wcs_type2 =  valToString(this.wcs_instance.getCtype2_csa());
		this.hasWcs=true;
	}
	/**
	 * Extract fields for a Saada instance with a spectra range
	 */
	private void setSpec() {
		this.spec_instance = (SpectrumSaada) this.instance;
		x_min = this.spec_instance.getX_min_csa();
		x_max = this.spec_instance.getX_max_csa();
		x_unit = valToString(this.spec_instance.getX_unit_csa());
		this.hasSpRange=true;
	}
	/**
	 * Extract fields for a Saada image
	 */
	private void setImg() {
		this.img_instance = (ImageSaada) this.instance;
		naxis1 =this.img_instance.getNaxis1();
		naxis2 = this.img_instance.getNaxis2();
		size_alpha = this.img_instance.getSize_alpha_csa();
		size_delta = this.img_instance.getSize_delta_csa();
		this.hasSpRange=true;
	}
	/**
	 * Flag the @see {@link #value} object as not set
	 */
	private void setNotSetValue() {
		value.fieldValue = ""; 
		value.isNotSet = true;
	}
	/**
	 * Translate val as a String XML compliant
	 * @param val
	 * @return
	 */
	private String valToString( double val ) {
		return (val == SaadaConstant.DOUBLE)?"": Double.toString(val);
	}
	/**
	 * Translate val as a String XML compliant
	 * @param val
	 * @return
	 */
	private String valToString( String val ) {
		return (SaadaConstant.STRING.equals(val))?"": val;
	}
	/**
	 * Set the {@link #value} object for the field idetified by fieldIdentifier with the value computed from the Saada instance
	 * @param fieldIdentifier ucd or utype of the field to map
	 */
	public void getFieldValue(String fieldIdentifier){
		value.init(fieldIdentifier);
		value.isNotSet = false;
		try {
			if( fieldIdentifier.equals("ID_MAIN")) {
				value.fieldValue = Long.toString(instance.getOid());				
			} else if( fieldIdentifier.equals("meta.title;meta.dataset")) {
				value.fieldValue = instance.getCollection().getName() + "_" + Category.explain(instance.getCategory());				
			} else if( fieldIdentifier.equals("Target.Pos") || fieldIdentifier.equals("Char.SpatialAxis.Coverage.Location.Value")) {
				if( hasPos ) value.fieldValue = ra + " " + dec;
				else  setNotSetValue();
			} else if( fieldIdentifier.equals("Access.Reference") || fieldIdentifier.equalsIgnoreCase("VOX:Image_AccessReference") 
					|| fieldIdentifier.equalsIgnoreCase("meta.ref.url")) {
				value.fieldValue = instance.getDownloadURL(true);
			} else if( fieldIdentifier.equals("VOX:Image_FileSize") ) {
				if( file != null ) value.fieldValue = Long.toString(file.length());
				else setNotSetValue();
			} else if( fieldIdentifier.equals("VOX:STC_CoordRefFrame") ) {
				value.isCdata = true;
				value.fieldValue = Database.getCoord_sys();
			} else if( fieldIdentifier.equals("Access.Format") || fieldIdentifier.equalsIgnoreCase("VOX:Image_Format") ) {
				value.isCdata = true;
				value.fieldValue = instance.getMimeType();
			} else if( fieldIdentifier.equals("DataID.Title") || fieldIdentifier.equalsIgnoreCase("VOX:Image_Title") 
					|| fieldIdentifier.equalsIgnoreCase("meta.title")) {
				value.isCdata = true;
				value.fieldValue = instance.getNameSaada();
			} else if( fieldIdentifier.equalsIgnoreCase("POS_EQ_RA_MAIN") || fieldIdentifier.equalsIgnoreCase("pos.eq.ra") 
					|| fieldIdentifier.equalsIgnoreCase("pos.eq.ra;meta.main")){
				value.fieldValue = ra;
			} else if( fieldIdentifier.equalsIgnoreCase("POS_EQ_DEC_MAIN")|| fieldIdentifier.equalsIgnoreCase("pos.eq.dec")
					|| fieldIdentifier.equalsIgnoreCase("pos.eq.dec;meta.main") ){
				value.fieldValue = dec;
			} else if( fieldIdentifier.equalsIgnoreCase("VOX:Image_Naxes") ){
				value.fieldValue = "2";
			} else if( fieldIdentifier.equalsIgnoreCase("VOX:Image_Naxis") ){
				value.fieldValue = naxis1 + " " + naxis2;
			} else if( fieldIdentifier.equalsIgnoreCase("VOX:Image_Scale") ){
				if( size_alpha == SaadaConstant.DOUBLE || naxis1 == SaadaConstant.INT ||
					size_delta == SaadaConstant.DOUBLE || naxis2 == SaadaConstant.INT	) {
					setNotSetValue();
				} else {
					value.fieldValue = Double.toString(size_alpha / naxis1)  + " " +  Double.toString(size_delta / naxis2);
				}					 
			} else if( fieldIdentifier.equals("VOX:WCS_CoordProjection") ) {
				String val = wcs_type1;
				// RA---TAN -> TAN e.g.
				if( val != null && val.length() > 3 ) {
					val = val.substring(val.length() - 3);
				}
				value.fieldValue = val;
			} else if( fieldIdentifier.equals("VOX:WCS_CoordRefPixel") ) {
				value.fieldValue = wcs_pix1 + " " + wcs_pix2;
			} else if( fieldIdentifier.equals("VOX:WCS_CoordRefValue") ) {
				value.fieldValue = wcs_val1 + " " + wcs_val2;
			} else if( fieldIdentifier.equals("VOX:WCS_CDMatrix") ) {
				value.fieldValue = wcs_cd11 + " " + wcs_cd12 + " " + wcs_cd21 + " " + wcs_cd22;
			} else if( fieldIdentifier.equals("ssa:Char.SpectralAxis.Coverage.Location.Value") ) {
				/*
				 * Conversion must be first done because it could be non linear (e.g. Kev -> m)
				 * Hence converting the mean is no equivalent to the mean of converted values
				 */
				if( x_min == SaadaConstant.DOUBLE || x_min == SaadaConstant.DOUBLE ) {
					setNotSetValue();
				} else {
					double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", x_min);
					double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", x_max);
					value.fieldValue = (Double.toString((v1  + v2)/2));
				}
			} else if( fieldIdentifier.equals("ssa:Char.SpectralAxis.Coverage.Location.Value") ) {
				/*
				 * Conversion must be first done because it could be non linear (e.g. Kev -> m)
				 * Hence converting the mean is no equivalent to the mean of converted values
				 */
				if( x_min == SaadaConstant.DOUBLE || x_min == SaadaConstant.DOUBLE ) {
					setNotSetValue();
				} else {
					double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", x_min);
					double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", x_max);
					value.fieldValue = (Double.toString((v1  + v2)/2));
				}
			} else if( fieldIdentifier.equals("ssa:Char.SpectralAxis.Coverage.Bounds.Extent") ) {
				/*
				 * Conversion must be first done because it could be non linear (e.g. Kev -> m)
				 * Hence converting the mean is no equivalent to the mean of converted values
				 */
				if( x_min == SaadaConstant.DOUBLE || x_min == SaadaConstant.DOUBLE ) {
					setNotSetValue();
				} else {
					double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", x_min);
					double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", x_max);
					double v = v2 - v1;	
					if( v < 0 ) v *= -1.;
					value.fieldValue = Double.toString(v);
				}
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Unknown identifier " + fieldIdentifier);
				setNotSetValue();
			}
		} catch( Exception e){
			setNotSetValue();
		}
	}
}
