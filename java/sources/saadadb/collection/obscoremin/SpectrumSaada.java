package saadadb.collection.obscoremin;
/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2003</p>
* <p>Company: </p>
* @author NGUYEN Ngoc Hoan
* @version 1.0
*/
import saadadb.util.SaadaConstant;

public class SpectrumSaada extends WCSSaada{
	/*
	 * Public fields are persistent
	 */
	/*
	 * Saada Axe
	 */	
    public int    x_naxis_csa = SaadaConstant.INT;
	/*
	 * Energy Axe
	 */
	public double e_min   = SaadaConstant.DOUBLE;
    public double e_max   = SaadaConstant.DOUBLE;
    public int    x_type_csa  = SaadaConstant.INT;
    public String x_unit_csa  = SaadaConstant.STRING;
    public String x_colname_csa = SaadaConstant.STRING;
    public double x_min_org_csa  = SaadaConstant.DOUBLE;
    public double x_max_org_csa  = SaadaConstant.DOUBLE;
    public String x_unit_org_csa = SaadaConstant.STRING;
}
  
