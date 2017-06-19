package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.JsonDataFile;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnSetter;
import saadadb.util.ImageUtils;
import saadadb.util.Messenger;

/**
 * This class redefines method specific in 2D images during their collection
 * load, and for the JPEG creation.
 * 
 * @author Millan Patrick
 * @version 2.0init
 * @since 2.0
 */
/**
 * @author michel
 * @version $Id$
 */
public class Image2DBuilder extends ProductBuilder {
	boolean load_vignette = true;	
	
	ColumnSetter wcs_crpix1Setter=new ColumnExpressionSetter("WCS_CRPIX1");
	ColumnSetter wcs_crpix2Setter=new ColumnExpressionSetter("WCS_CRPIX2");
	ColumnSetter wcs_ctype1Setter=new ColumnExpressionSetter("WCS_CTYPE1");
	ColumnSetter wcs_ctype2Setter=new ColumnExpressionSetter("WCS_CTYPE2");
	ColumnSetter wcs_val1Setter=new ColumnExpressionSetter("WCS_VAL1");
	ColumnSetter wcs_val2Setter=new ColumnExpressionSetter("WCS_VAL2");
	ColumnSetter wcs_crotaSetter=new ColumnExpressionSetter("WCS_CROTA");
	ColumnSetter wcs_d1_1Setter=new ColumnExpressionSetter("WCS_D1_1");
	ColumnSetter wcs_d1_2Setter=new ColumnExpressionSetter("WCS_D1_2");
	ColumnSetter wcs_d2_1Setter=new ColumnExpressionSetter("WCS_D2_1");
	ColumnSetter wcs_d2_2Setter=new ColumnExpressionSetter("WCS_D2_1");
	ColumnSetter naxis1=new ColumnExpressionSetter("NAXIS1");
	ColumnSetter naxis2=new ColumnExpressionSetter("NAXIS2");
	ColumnSetter size_alpha_csaSetter=new ColumnExpressionSetter("size_alpha_csa");
	ColumnSetter size_delta_csaSetter=new ColumnExpressionSetter("size_delta_csaSetter");

	private static final long serialVersionUID = 1L;

	/**
	 * @param productFile
	 * @param conf
	 * @param metaClass
	 * @throws SaadaException
	 */
	public Image2DBuilder(JsonDataFile productFile, ProductMapping conf, MetaClass metaClass) throws SaadaException{	
		super(productFile, conf, metaClass);
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}

	/**
	 * @param file
	 * @param mapping
	 * @param metaClass
	 * @throws SaadaException
	 */
	public Image2DBuilder(DataFile file, ProductMapping mapping, MetaClass metaClass) throws SaadaException{		
		super(file, mapping, metaClass);
		if( mapping != null )
		this.load_vignette = !mapping.noVignette();
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
	/**
	 * @param file
	 * @param mapping
	 * @throws SaadaException
	 */
	public Image2DBuilder(DataFile file, ProductMapping mapping) throws SaadaException{		
		super(file, mapping, null);
		if( mapping != null )
		this.load_vignette = !mapping.noVignette();
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
	

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#setProductIngestor()
	 */
	protected void setProductIngestor() throws Exception{
		if( this.productIngestor == null ){
			this.productIngestor = new ImageIngestor(this);
		}		
	}

	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getCategory()
	 */
	public int getCategory(){
		return Category.IMAGE;
	}

	/**
	

	/**
	 * This method builds a SaadaInstance and stores it into the DB
	 * It is used for all data categories except for entries where the SaadaInstance step is skipped
	 * @param saada_class
	 * @throws Exception
	 */
	@Override
	public void loadProduct() throws Exception  {
		super.loadProduct();
		try {
			this.createVignette();
		} catch( Exception e ) {
			Messenger.printMsg(Messenger.WARNING, "Can't create image vignette");
		}
		Messenger.printMsg(Messenger.TRACE, "Processing file <" + this.dataFile.getName() + "> complete");
	}

	/**
	 * This method builds a SaadaInstance and stores it into the ASCII files
	 * It is used for all data categories except for entries where the SaadaInstance step is skipped
	 * @param colwriter : file where are store collection level attributes
	 * @param buswfriter: file where are store class level attributes
	 * @throws Exception
	 */
	@Override
	public void loadProduct(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {

		super.loadProduct(colwriter, buswriter, loadedfilewriter);
		if( load_vignette ) {
			try {
				this.createVignette();
			} catch( Exception e ) {
				Messenger.printMsg(Messenger.WARNING, "Can't create image vignette");
			}
		} else {
			Messenger.printMsg(Messenger.TRACE, "Make no vignette");
		}
	}

	/**
	 * @throws FatalException
	 * @throws IgnoreException
	 */
	public void createVignette() throws Exception {
		String basedir = Database.getRepository() 
		+ separ + this.mapping.getCollection() 
		+ separ + Category.explain(this.mapping.getCategory()) 
		+ separ  + "JPEG" ;
		File bf = new File(basedir);
		/*
		 * JPEG dir can be removed by some bad action!
		 */
		if( !bf.exists() ) {
			Messenger.printMsg(Messenger.TRACE, "Create directory " + basedir);
			bf.mkdir();
		}
		String namefilejpeg = basedir
		+ separ + this.getName() + ".jpg";
		ImageUtils.createImage(namefilejpeg, (FitsDataFile) this.dataFile, 400);
		this.productIngestor.saadaInstance.setVignetteFile();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#mapDataFile(saadadb.products.datafile.DataFile)
	 */
	public void mapDataFile(DataFile dataFile) throws Exception{
		this.wcs_crpix1Setter.resetMessages();
		this.wcs_crpix2Setter.resetMessages();
		this.wcs_ctype1Setter.resetMessages();
		this.wcs_ctype2Setter.resetMessages();
		this.wcs_val1Setter.resetMessages();
		this.wcs_val2Setter.resetMessages();
		this.wcs_crotaSetter.resetMessages();
		this.wcs_d1_1Setter.resetMessages();
		this.wcs_d1_2Setter.resetMessages();
		this.wcs_d2_1Setter.resetMessages();
		this.wcs_d2_2Setter.resetMessages();
		this.size_alpha_csaSetter.resetMessages();
		this.size_delta_csaSetter.resetMessages();
		this.naxis1.resetMessages();
		this.naxis2.resetMessages();

		super.mapDataFile(dataFile);
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#mapCollectionPosAttributes()
	 */
	protected void mapCollectionPosAttributes() throws Exception {
		super.mapCollectionPosAttributes();
		this.wcsModeler.updateValues();

		this.wcs_crpix1Setter = this.quantityDetector.getWCSCrpix1();
		this.wcs_crpix2Setter = this.quantityDetector.getWCSCrpix2();
		this.wcs_ctype1Setter = this.quantityDetector.getWCSType1();
		this.wcs_ctype2Setter = this.quantityDetector.getWCSType2();
		this.wcs_val1Setter   = this.quantityDetector.getWCSCrval1();
		this.wcs_val2Setter   = this.quantityDetector.getWCSCrval2();
		this.wcs_crotaSetter  = this.quantityDetector.getWCSCROTA();
		this.wcs_d1_1Setter   = this.quantityDetector.getWCSCD11();
		this.wcs_d1_2Setter   = this.quantityDetector.getWCSCD12();
		this.wcs_d2_1Setter   = this.quantityDetector.getWCSCD21();
		this.wcs_d2_2Setter   = this.quantityDetector.getWCSCD22();
		this.size_alpha_csaSetter = this.quantityDetector.getSizeRA();
		this.size_delta_csaSetter = this.quantityDetector.getSizeDEC();
		this.naxis1 = this.quantityDetector.getNaxis1();
		this.naxis2 = this.quantityDetector.getNaxis2();
		
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#calculateAllExpressions()
	 */
	protected void calculateAllExpressions() throws Exception {
		Messenger.locateCode();
		super.calculateAllExpressions();
		this.wcs_crpix1Setter.calculateExpression();
		this.wcs_crpix2Setter.calculateExpression();
		this.wcs_ctype1Setter.calculateExpression();
		this.wcs_ctype2Setter.calculateExpression();
		this.wcs_val1Setter.calculateExpression();
		this.wcs_val2Setter.calculateExpression();
		this.wcs_crotaSetter.calculateExpression();
		this.wcs_d1_1Setter.calculateExpression();
		this.wcs_d1_2Setter.calculateExpression();
		this.wcs_d2_1Setter.calculateExpression();
		this.wcs_d2_2Setter.calculateExpression();		
		this.size_alpha_csaSetter.calculateExpression();
		this.size_delta_csaSetter.calculateExpression();
		this.naxis1.calculateExpression();
		this.naxis2.calculateExpression();
	}
}
