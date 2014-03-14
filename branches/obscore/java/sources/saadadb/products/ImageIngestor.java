package saadadb.products;

import cds.astro.Coo;
import saadadb.collection.obscoremin.ImageSaada;
import saadadb.products.inference.Coord;

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
		if( product.wcs != null ) {
			Coord coo_c =  product.wcs.getImgCenter();
			image.ctype1_csa =  product.wcs.getType1() ;
			image.ctype2_csa =  product.wcs.getType2();
			double[][] cd_ij =  product.wcs.getCD();
			image.cd1_1_csa = cd_ij[0][0];
			image.cd1_2_csa = cd_ij[0][1];
			image.cd2_1_csa = cd_ij[1][0];
			image.cd2_2_csa = cd_ij[1][1];
			image.crpix1_csa =  product.wcs.getXcen();
			image.crpix2_csa =  product.wcs.getXcen();
			image.crval1_csa =  product.wcs.getAlphai();
			image.crval2_csa =  product.wcs.getDeltai();
			image.crota_csa =  product.wcs.getRota();	
			image.naxis1 =  product.wcs.getXnpix();
			image.naxis2  =  product.wcs.getYnpix();
			image.s_ra = product. wcs.getAlphai();
			image.s_dec = product. wcs.getDeltai();
			image.size_alpha_csa = product. wcs.getWidtha();
			image.size_delta_csa  = product. wcs.getWidthd();
			image.s_fov = Math.sqrt((image.size_alpha_csa * image.size_alpha_csa) + (image.size_delta_csa + image.size_delta_csa))/2.;
			Coord c = new Coord();
			image.s_region = "";
			c.setXY(0, 0); product. wcs.GetCoord(c);
			image.s_region += c.getPOS_RA() + " " + c.getPOS_DEC();
			c.setXY(0, image.naxis2); product. wcs.GetCoord(c);
			image.s_region +=  " " + c.getPOS_RA() + " " + c.getPOS_DEC();
			c.setXY(image.naxis1, image.naxis2); product. wcs.GetCoord(c);
			image.s_region +=  " " + c.getPOS_RA() + " " + c.getPOS_DEC();
			c.setXY(image.naxis1, 0); product. wcs.GetCoord(c);
			image.s_region +=  " " + c.getPOS_RA() + " " + c.getPOS_DEC();
		}
	}

}
