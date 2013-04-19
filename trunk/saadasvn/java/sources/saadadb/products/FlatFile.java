package saadadb.products;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.Messenger;

public class FlatFile extends Product {

	public FlatFile(File file, ConfigurationDefaultHandler conf) {
		super(file, conf);
	}

	/** * @version $Id$

	 * This method builds a SaadaInstance and stores it into the DB
	 * It is used for all data categories except for entries where the SaadaInstance step is skipped
	 * @param saada_class
	 * @throws Exception
	 */
	public void loadValue() throws Exception  {
		this.saadainstance = (SaadaInstance) SaadaClassReloader.forGeneratedName("FLATFILEUserColl").newInstance();
	    /*
		 * Build the Saada instance
		 */
		long newoid = SaadaOID.newFlatFileOid(this.configuration.getCollectionName());
		this.saadainstance.setOid(newoid);
		this.setBasicCollectionFields();
		this.loadAttrExtends();
		/*
		 * Store the Saada instance
		 */
		this.storeCopyFileInRepository();
		/*
		 * No class for flatfiles => not business attribute, just the collection
		 */
		this.saadainstance.storeCollection();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.file.getName() + "> complete");
	
	}
	/**
	 * @param configuration
	 * @throws FitsException
	 * @throws IOException
	 * @throws SaadaException
	 * @throws AbortException
	 */
	public void loadProductFile(ConfigurationDefaultHandler configuration) throws IgnoreException{
		this.configuration = configuration;
		
			Messenger.printMsg(Messenger.TRACE, "Make a AnyTable instance with file <"  + this.file.getName() + ">");
			this.productFile = new AnyFile(this);
			this.tableAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
	}
	/**
	 * @param configuration
	 * @throws FitsException
	 * @throws IOException
	 */
	public void initProductFile(ConfigurationDefaultHandler configuration) throws IgnoreException{
		
		this.loadProductFile(configuration);
		this.mapCollectionAttributes();	
	}
	/**
	 * 
	 */
	protected void mapCollectionAttributes() {
		this.mapInstanceName();
		this.mapIgnoredAndExtendedAttributes();
	}
	
	/**
	 * Used by FLatFileMapper to load flafiles by burst using a single instance of the this class
	 * @param si
	 * @throws AbortException 
	 */
	public void bindInstanceToFile(SaadaInstance si, File file) throws AbortException {
		this.saadainstance = si;
		this.file = file;
		this.productFile = new AnyFile(this);
		this.setBasicCollectionFields();
		this.loadAttrExtends();
	}
	

}
