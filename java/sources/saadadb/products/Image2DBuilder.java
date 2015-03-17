package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.ImageSaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.JsonDataFile;
import saadadb.products.inference.Coord;
import saadadb.products.inference.Image2DCoordinate;
import saadadb.util.ImageUtils;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.PriorityMode;

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
	/** * @version $Id$

	 * 
	 */
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
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
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
	 * @see saadadb.products.ProductBuilder#getCategory()
	 */
	public int getCategory(){
		return Category.IMAGE;
	}

	
//	/* (non-Javadoc)
//	 * @see saadadb.products.ProductBuilder#setProductIngestor()
//	 */
//	@Override
//	protected void setProductIngestor() throws Exception{
//		if( this.productIngestor == null ){
//			try{
//				this.wcs = new Image2DCoordinate();
//				this.wcs.setImage2DCoordinate(this.productAttributeHandler);
//				Messenger.printMsg(Messenger.TRACE, "Valid WCS keywords found");
//			}catch(Exception e){
//				this.wcs = null;
//				Messenger.printStackTrace(e);
//				Messenger.printMsg(Messenger.WARNING, "No valid WCS keywords found: " + e.getMessage() + ": Coordinate and image size will not be set.");
//				Messenger.printMsg(Messenger.WARNING, "No position found: Position will not be set for this product");
//			}
//			this.productIngestor = new ImageIngestor(this);
//		}		
//	}

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

}
