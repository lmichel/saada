package saadadb.collection.obscoremin;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;


/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @version SAADA 1.0
 * @author: NGUYEN Ngoc Hoan
 * E-Mail: nguyen@saadadb.u-strasbg.fr</p>
 */
public class WCS3DSaada extends WCSSaada {
	public double crpix1_csa;
	public double crpix2_csa;
	public String ctype1_csa;
	public String ctype2_csa;
	public double cd1_1_csa;
	public double cd1_2_csa;
	public double cd2_1_csa;
	public double cd2_2_csa;
	public double crota_csa;
	public double crval1_csa;
	public double crval2_csa;
	public String product_url_csa;
	
	public WCS3DSaada() {
		super();
	}
	
	public  void setCrpix1_csa(double value)
	{
		this.crpix1_csa=value;
	}
	
	public  double  getCrpix1_csa()
	{
		return this.crpix1_csa;
	}
	
	public  void setCrpix2_csa(double value)
	{
		this.crpix2_csa=value;
	}
	
	public  double  getCrpix2_csa()
	{
		return this.crpix2_csa;
	}
	
	public  void setCtype1_csa(String value)
	{
		this.ctype1_csa=value;
	}
	
	public  String getCtype1_csa()
	{
		return this.ctype1_csa;
	}
	
	public  void setCtype2_csa(String value)
	{
		this.ctype2_csa=value;
	}
	
	public  String getCtype2_csa()
	{
		return this.ctype2_csa;
	}
	
	public  void setCrval1_csa(double value)
	{
		this.crval1_csa=value;
	}
	
	public  double  getCrval1_csa()
	{
		return this.crval1_csa;
	}
	
	public  void setCrval2_csa(double value)
	{
		this.crval2_csa=value;
		
	}
	
	public  double  getCrval2_csa()
	{
		return this.crval2_csa;
	}
	
	public  void setCrota_csa(double value)
	{
		this.crota_csa=value;
	}
	
	public  double  getCrota_csa()
	{
		return this.crota_csa;
	}
	
	
	/**
	 * @return Returns the cd1_1_csa.
	 */
	public double getCd1_1_csa() {
		return cd1_1_csa;
	}
	
	
	/**
	 * @param cd1_1_csa The cd1_1_csa to set.
	 */
	public void setCd1_1_csa(double cd1_1_csa) {
		this.cd1_1_csa = cd1_1_csa;
	}
	
	
	/**
	 * @return Returns the cd1_2_csa.
	 */
	public double getCd1_2_csa() {
		return cd1_2_csa;
	}
	
	
	/**
	 * @param cd1_2_csa The cd1_2_csa to set.
	 */
	public void setCd1_2_csa(double cd1_2_csa) {
		this.cd1_2_csa = cd1_2_csa;
	}
	
	
	/**
	 * @return Returns the cd2_1_csa.
	 */
	public double getCd2_1_csa() {
		return cd2_1_csa;
	}
	
	
	/**
	 * @param cd2_1_csa The cd2_1_csa to set.
	 */
	public void setCd2_1_csa(double cd2_1_csa) {
		this.cd2_1_csa = cd2_1_csa;
	}
	
	
	/**
	 * @return Returns the cd2_2_csa.
	 */
	public double getCd2_2_csa() {
		return cd2_2_csa;
	}
	
	
	/**
	 * @param cd2_2_csa The cd2_2_csa to set.
	 */
	public void setCd2_2_csa(double cd2_2_csa) {
		this.cd2_2_csa = cd2_2_csa;
	}
	
	
	
	public void calculWCS()
	{
		
	}
	
	/* (non-Javadoc)
	 * @see saadadb.collection.Position#setProduct_url_csa(java.lang.String)
	 */
	public  void setProduct_url_csa(String nameProduct) throws AbortException
	{
    	if( nameProduct == null ) {
    		AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "product_url_csa cannot be set to null");
    	}
		this.product_url_csa=nameProduct;
	}
	
	public  String  getProduct_url_csa()
	{
		return this.product_url_csa;
	}
	

	/**
	 * @return
	 */
	public String getMimeType() {
		return getMimeType(this.product_url_csa);
	}

}

