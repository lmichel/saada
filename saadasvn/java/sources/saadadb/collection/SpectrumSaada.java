package saadadb.collection;
/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2003</p>
* <p>Company: </p>
* @author NGUYEN Ngoc Hoan
* @version 1.0
*/
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class SpectrumSaada extends WCSSaada{

	public double x_min_csa   = SaadaConstant.DOUBLE;
    public double x_max_csa   = SaadaConstant.DOUBLE;
    public int    x_type_csa  = SaadaConstant.INT;
    public String x_unit_csa  = SaadaConstant.STRING;
    public int    x_naxis_csa = SaadaConstant.INT;
    public String x_colname_csa = SaadaConstant.STRING;
    public double x_min_org_csa  = SaadaConstant.DOUBLE;
    public double x_max_org_csa  = SaadaConstant.DOUBLE;
    public String x_unit_org_csa = SaadaConstant.STRING;

    public double y_min_csa = SaadaConstant.DOUBLE;
    public double y_max_csa = SaadaConstant.DOUBLE;
    public String y_unit_csa = SaadaConstant.STRING;  
    public String y_colname_csa = SaadaConstant.STRING;
	/**
	 * @return Returns the x_colname_csa.
	 */
	public String getX_colname_csa() {
		return x_colname_csa;
	}
	/**
	 * @param x_colname_csa The x_colname_csa to set.
	 */
	public void setX_colname_csa(String x_colname_csa) {
		this.x_colname_csa = x_colname_csa;
	}
	/**
	 * @return Returns the x_max_csa.
	 */
	public double getX_max_csa() {
		return x_max_csa;
	}
	/**
	 * @param x_max_csa The x_max_csa to set.
	 */
	public void setX_max_csa(double x_max_csa) {
		this.x_max_csa = x_max_csa;
	}
	/**
	 * @return Returns the x_max_org_csa.
	 */
	public double getX_max_org_csa() {
		return x_max_org_csa;
	}
	/**
	 * @param x_max_org_csa The x_max_org_csa to set.
	 */
	public void setX_max_org_csa(double x_max_org_csa) {
		this.x_max_org_csa = x_max_org_csa;
	}
	/**
	 * @return Returns the x_min_csa.
	 */
	public double getX_min_csa() {
		return x_min_csa;
	}
	/**
	 * @param x_min_csa The x_min_csa to set.
	 */
	public void setX_min_csa(double x_min_csa) {
		this.x_min_csa = x_min_csa;
	}
	/**
	 * @return Returns the x_min_org_csa.
	 */
	public double getX_min_org_csa() {
		return x_min_org_csa;
	}
	/**
	 * @param x_min_org_csa The x_min_org_csa to set.
	 */
	public void setX_min_org_csa(double x_min_org_csa) {
		this.x_min_org_csa = x_min_org_csa;
	}
	/**
	 * @return Returns the x_naxis_csa.
	 */
	public int getX_naxis_csa() {
		return x_naxis_csa;
	}
	/**
	 * @param x_naxis_csa The x_naxis_csa to set.
	 */
	public void setX_naxis_csa(int x_naxis_csa) {
		this.x_naxis_csa = x_naxis_csa;
	}
	/**
	 * @return Returns the x_type_csa.
	 */
	public int getX_type_csa() {
		return x_type_csa;
	}
	/**
	 * @param x_type_csa The x_type_csa to set.
	 */
	public void setX_type_csa(int x_type_csa) {
		this.x_type_csa = x_type_csa;
	}
	/**
	 * @return Returns the x_unit_csa.
	 */
	public String getX_unit_csa() {
		return x_unit_csa;
	}
	/**
	 * @param x_unit_csa The x_unit_csa to set.
	 */
	public void setX_unit_csa(String x_unit_csa) {
		this.x_unit_csa = x_unit_csa;
	}
	/**
	 * @return Returns the x_unit_org_csa.
	 */
	public String getX_unit_org_csa() {
		return x_unit_org_csa;
	}
	/**
	 * @param x_unit_org_csa The x_unit_org_csa to set.
	 */
	public void setX_unit_org_csa(String x_unit_org_csa) {
		this.x_unit_org_csa = x_unit_org_csa;
	}
	/**
	 * @return Returns the y_colname_csa.
	 */
	public String getY_colname_csa() {
		return y_colname_csa;
	}
	/**
	 * @param y_colname_csa The y_colname_csa to set.
	 */
	public void setY_colname_csa(String y_colname_csa) {
		this.y_colname_csa = y_colname_csa;
	}
	/**
	 * @return Returns the y_max_csa.
	 */
	public double getY_max_csa() {
		return y_max_csa;
	}
	/**
	 * @param y_max_csa The y_max_csa to set.
	 */
	public void setY_max_csa(double y_max_csa) {
		this.y_max_csa = y_max_csa;
	}
	/**
	 * @return Returns the y_min_csa.
	 */
	public double getY_min_csa() {
		return y_min_csa;
	}
	/**
	 * @param y_min_csa The y_min_csa to set.
	 */
	public void setY_min_csa(double y_min_csa) {
		this.y_min_csa = y_min_csa;
	}
	/**
	 * @return Returns the y_unit_csa.
	 */
	public String getY_unit_csa() {
		return y_unit_csa;
	}
	/**
	 * @param y_unit_csa The y_unit_csa to set.
	 */
	public void setY_unit_csa(String y_unit_csa) {
		this.y_unit_csa = y_unit_csa;
	}

	/**
	 * @param filename
	 * @return
	 */
	public String getMimeType(String filename) {
		if( filename == null ) {
			return "";
		}
		else if( filename.matches(RegExp.FITS_FILE)) {
			return "application/fits";						
		}
		else if( filename.matches(RegExp.VOTABLE_FILE)) {
			return "application/x-votable+xml";						
		}
		else {
			return "text/html";												
		}
	}	

    

}
  
