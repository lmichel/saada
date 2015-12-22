package saadadb.products;

import saadadb.collection.obscoremin.ImageSaada;

/**
 * Ingestor specific for images. In addition to the standard Obscore params, it sets the current instance with WCS values
 * @author michel
 *
 */
public class ImageIngestor extends ProductIngestor {

	ImageIngestor(ProductBuilder product) throws Exception {
		super(product);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setSpaceFields()
	 */
	@Override
	protected void setSpaceFields() throws Exception {
		super.setSpaceFields();
		ImageSaada image = (ImageSaada)this.saadaInstance;		
		Image2DBuilder product = (Image2DBuilder)this.product;

		image.crval1_csa = product.wcs_val1Setter.getNumValue();
		image.crval2_csa = product.wcs_val2Setter.getNumValue();
		image.crpix1_csa = product.wcs_crpix1Setter.getNumValue();
		image.crpix2_csa = product.wcs_crpix2Setter.getNumValue();
		image.ctype1_csa = product.wcs_ctype1Setter.getValue();
		image.ctype2_csa = product.wcs_ctype2Setter.getValue();
		image.cd1_1_csa  = product.wcs_d1_1Setter.getNumValue();
		image.cd1_2_csa  = product.wcs_d1_2Setter.getNumValue();
		image.cd2_1_csa  = product.wcs_d2_1Setter.getNumValue();
		image.cd2_2_csa  = product.wcs_d2_2Setter.getNumValue();
		image.crota_csa  = product.wcs_crotaSetter.getNumValue();
	}

}
