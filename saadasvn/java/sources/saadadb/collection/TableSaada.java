package saadadb.collection;

import saadadb.util.SaadaConstant;

/**
 * <p>Title: SAADA</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-3000</p>
 * <p>Company: </p>
 * @author NGUYEN Ngoc Hoan
 * @version 1.0
 */


public class TableSaada extends SaadaInstance {
	public long group_oid_csa;
	public String product_url_csa=SaadaConstant.STRING;
	public int nb_rows_csa;
	
	public TableSaada() {
		super();
	}
	
	/**
	 * @param nameProduct
	 */
	public  void setProduct_url_csa(String nameProduct)
	{
		this.product_url_csa=nameProduct;
	}
	/**
	 * @return
	 */
	public  String  getProduct_url_csa()
	{
		return this.product_url_csa;
	}
		
	/**
	 * @return
	 */
	public int getNumberRows()
	{
		return this.nb_rows_csa;
	}
	

	/**
	 * @return
	 */
	public String getMimeType() {
		return getMimeType(this.product_url_csa);
	}
	
}


