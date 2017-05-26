package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;

import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.prdconfiguration.ConfigurationImage;
import saadadb.util.DefineType;
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
public class Image2D extends Product {
	boolean load_vignette = true;
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param fileName
	 */
	public Image2D(DataResourcePointer file, ConfigurationDefaultHandler conf){		
		super(file, conf);
		if( conf != null )
		this.load_vignette = conf.load_vignette;
	}
	/**
	 * Set the WCS fields for the image. Position is also set by this method.
	 * Do something in higher level in hierarchy
	 * @throws Exception 
	 */
	protected void setWcsFields() throws Exception {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set WCS keywords");
		Image2DCoordinate wcs = new Image2DCoordinate();
		ImageSaada image = (ImageSaada)(this.saadainstance);
		try{
			wcs.setImage2DCoordinate(this.tableAttributeHandler);
			Messenger.printMsg(Messenger.TRACE, "Valid WCS keywords found");
		}catch(Exception e){
			Messenger.printStackTrace(e);
			Messenger.printMsg(Messenger.WARNING, "No valid WCS keywords found: " + e.getMessage() + ": Coordinate and image size will not be set.");
			Messenger.printMsg(Messenger.WARNING, "No position found: Position will not be set for this product");
		}

		Coord coo_c = wcs.getImgCenter();
		image.setCtype1_csa(wcs.getType1());
		image.setCtype2_csa(wcs.getType2());
		double[][] cd_ij = wcs.getCD();
		image.setCd1_1_csa(cd_ij[0][0]);
		image.setCd1_2_csa(cd_ij[0][1]);
		image.setCd2_1_csa(cd_ij[1][0]);
		image.setCd2_2_csa(cd_ij[1][1]);
		image.setCrpix1_csa(wcs.getXcen());
		image.setCrpix2_csa(wcs.getXcen());
		image.setCrval1_csa(wcs.getAlphai());
		image.setCrval2_csa(wcs.getDeltai());
		image.setCrota_csa(wcs.getRota());	

		image.setNaxis1(wcs.getXnpix());
		image.setNaxis2(wcs.getYnpix());

		image.setSize_alpha_csa(wcs.getWidtha());
		image.setSize_delta_csa(wcs.getWidthd());

		/*
		 * Here we can mapp position attributes
		 * Position attribute have already be mapped by super.mapPositionAttribute
		 * If this mapping failed, we can use WCS KW with the condition not to be with the
		 * ONLY mapping pririty
		 */
		int pos_prio = this.configuration.getMapping().getPosPriority();
		if( this.astroframe != null && (this.ra_attribute == null || this.dec_attribute == null) &&
				(pos_prio == DefineType.LAST || pos_prio == DefineType.FIRST)) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No position mapping found: Use WCS keyword");
			this.ra_attribute = new AttributeHandler();
			this.ra_attribute.setNameorg("Numeric");
			this.ra_attribute.setNameattr("Numeric");
			this.ra_attribute.setValue(Double.toString(coo_c.getPOS_RA()));
			this.dec_attribute = new AttributeHandler();
			this.dec_attribute.setNameorg("Numeric");
			this.dec_attribute.setNameattr("Numeric");
			this.dec_attribute.setValue(Double.toString(coo_c.getPOS_DEC()));
			Messenger.printMsg(Messenger.TRACE, "Position extracted from WCS <value="+this.ra_attribute.getValue() 
					+" value="+this.dec_attribute.getValue() + ">");
		}
	}

	/**
	 * This method builds a SaadaInstance and stores it into the DB
	 * It is used for all data categories except for entries where the SaadaInstance step is skipped
	 * @param saada_class
	 * @throws Exception
	 */
	public void loadValue() throws Exception  {

		/*
		 * Build the Saada instance
		 */
		this.saadainstance = (SaadaInstance)  SaadaClassReloader.forGeneratedName( this.metaclass.getName()).newInstance();
		long newoid = SaadaOID.newOid(this.metaclass.getName());
		this.saadainstance.setOid(newoid);
		this.setAstrofFrame();
		this.setBusinessFields();
		this.setWcsFields();
		this.setPositionFields(0);
		this.setBasicCollectionFields();
		this.loadAttrExtends();
		/*
		 * Store the Saada instance
		 */
		this.storeCopyFileInRepository();
		this.saadainstance.store();
		try {
			this.createVignette();
		} catch( Exception e ) {
			Messenger.printMsg(Messenger.WARNING, "Can't create image vignette");
		}
		Messenger.printMsg(Messenger.TRACE, "Processing file <" + this.dataPointer.file.getName() + "> complete");
	}

	/**
	 * This method builds a SaadaInstance and stores it into the ASCII files
	 * It is used for all data categories except for entries where the SaadaInstance step is skipped
	 * @param colwriter : file where are store collection level attributes
	 * @param buswfriter: file where are store class level attributes
	 * @throws Exception
	 */
	public void loadValue(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {

		super.loadValue(colwriter, buswriter, loadedfilewriter);
		if( load_vignette ) {
			try {
				this.createVignette();
			} catch( Exception e ) {
				Messenger.printMsg(Messenger.WARNING, "Can't create image vignette");
			}
		}
		else {
			Messenger.printMsg(Messenger.TRACE, "Make no vignette");
		}
	}

	/**
	 * @throws FatalException
	 * @throws IgnoreException
	 */
	public void createVignette() throws Exception {
		String basedir = Database.getRepository() 
		+ separ + this.getConfiguration().getCollectionName() 
		+ separ + Category.explain(this.getConfiguration().getCategorySaada()) 
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
		+ separ + this.saadainstance.getRepositoryName() + ".jpg";
		ImageUtils.createImage(namefilejpeg, (FitsProduct) this.productFile, 400);
		this.saadainstance.setVignetteFile();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.Product#initProductFile(saadadb.prdconfiguration.ConfigurationDefaultHandler)
	 */
	public void initProductFile(ConfigurationDefaultHandler configuration) throws IgnoreException{

		this.loadProductFile(configuration);
		try {
			this.mapCollectionAttributes();
		} catch (Exception e) {
			IgnoreException.throwNewException(SaadaException.MAPPING_FAILURE, e);
		}	
	}


	/* (non-Javadoc)
	 * @see saadadb.products.Product#loadProductFile(saadadb.prdconfiguration.ConfigurationDefaultHandler)
	 */
	public void loadProductFile(ConfigurationDefaultHandler configuration) throws IgnoreException{
		this.configuration = configuration;
		try {
			this.productFile = new FitsProduct(this);			
		}
		catch(Exception ef) {
			ef.printStackTrace();
			String filename = this.dataPointer.file.getName();
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + filename + ">  :" + ef + ". It cannot be loaded as an image.");			
		}
		this.setFmtsignature();
		this.productFile.setSpaceFrame();
	}


	/* (non-Javadoc)
	 * @see saadadb.products.Product#main(java.lang.String[])
	 */
	public static void main(String[] args)  {
		Database.init("ThreeXMM");
		//Image2D img = new Image2D(new File("/data/MUSE/Scene_fusion_01.fits"), null);
		try {
			Image2D img = new Image2D(new DataResourcePointer("/data/3xmm/data_test/EpicObsImage/P0300520301EPX000OIMAGE8000.FIT.gz"), null);
			img.initProductFile(new ConfigurationImage("", new ArgsParser(new String[]{"-debug", "-system='Ecliptic'", "-collection=qwerty"})));
			//			img.setWcsFields();

			ImageUtils.createImage("/home/michel/Desktop/ma04979.fit.jpg", (FitsProduct) img.productFile, 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.close();
	}

}
