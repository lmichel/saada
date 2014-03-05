package saadadb.products;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/** 
 * @version $Id: FlatFile.java 826 2013-11-08 15:24:32Z laurent.mistahl $
 * 
 */
public class FlatFile extends Product {

	public FlatFile(File file, ProductMapping conf) {
		super(file, conf);
	}

	public void loadValue() throws Exception  {
		this.saadainstance = (SaadaInstance) SaadaClassReloader.forGeneratedName("FLATFILEUserColl").newInstance();
	    /*
		 * Build the Saada instance
		 */
		long newoid = SaadaOID.newFlatFileOid(this.mapping.getCollection());
		this.saadainstance.oidsaada = newoid;
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
	public void loadProductFile(ProductMapping mapping) throws IgnoreException{
		this.mapping = mapping;
		
			Messenger.printMsg(Messenger.TRACE, "Make a AnyTable instance with file <"  + this.file.getName() + ">");
			this.productFile = new AnyFile(this);
			this.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
	}
	/**
	 * @param configuration
	 * @throws FatalException 
	 * @throws FitsException
	 * @throws IOException
	 */
	public void initProductFile(ProductMapping conf) throws SaadaException{
		
		this.loadProductFile(mapping);
		this.mapCollectionAttributes();	
	}
	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionAttributes() throws SaadaException {
		this.mapInstanceName();
		this.mapIgnoredAndExtendedAttributes();
	}
	
	/**
	 * Used by FLatFileMapper to load flafiles by burst using a single instance of the this class
	 * @param si
	 * @throws AbortException 
	 */
	public void bindInstanceToFile(SaadaInstance si, File file) throws SaadaException {
		this.saadainstance = si;
		this.file = file;
		this.productFile = new AnyFile(this);
		this.setBasicCollectionFields();
		this.loadAttrExtends();
	}
	

}
