package saadadb.products;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import nom.tam.fits.FitsException;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.FooProduct;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 */
public class TableBuilder extends ProductBuilder {

	private static final long serialVersionUID = 1L;
	/** The identification number of this table in data base* */
	protected long oid;
	public EntryBuilder entryBuilder;

	/**
	 * @param fileName
	 * @param tabArg
	 * @throws FatalException 
	 */
	public TableBuilder(DataFile file, ProductMapping mapping, MetaClass metaClass) throws Exception{	
		super(file, mapping, metaClass);
		System.out.println("@@@@@@@@@@@@@ build entry");

		this.entryBuilder = new EntryBuilder(this);
//		this.entryBuilder.setProductIngestor();
//		this.entryBuilder.bindDataFile(file);
	}	
	public TableBuilder(DataFile file, ProductMapping mapping) throws Exception{	
		super(file, mapping, null);
		System.out.println("@@@@@@@@@@@@@ build entry");

		this.entryBuilder = new EntryBuilder(this);
//		this.entryBuilder.setProductIngestor();
//		this.entryBuilder.bindDataFile(file);
	}	

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getTableOid()
	 */
	@Override
	public long getTableOid() {
		return oid;
	}
//
//	/* (non-Javadoc)
//	 * @see saadadb.products.Product#initProductFile(java.lang.String, saadadb.prdconfiguration.Configuration)
//	 */
//	@Override
//	public void initProductFile() throws SaadaException{
//		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Init TABLE instance");
//		super.initProductFile();
//		this.entryBuilder.initProductFile();
//	}
//
//	/* (non-Javadoc)
//	 * @see saadadb.products.Product#loadProductFile(saadadb.prdconfiguration.ConfigurationDefaultHandler)
//	 */
//	@Override
//	public void bindDataFile(DataFile dataFile) throws Exception{
//		super.bindDataFile(dataFile);
//		/*
//		 * Case when the method is called by the superclass constructor
//		 */
//		if( this.entryBuilder != null ){
//			this.entryBuilder.bindDataFile(dataFile);
//		}
//	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#loadValue()
	 */
	@Override
	public void loadProduct() throws Exception  {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load table header");
		super.loadProduct();
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load entries ");
		this.entryBuilder.loadProduct();
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#mapDataFile(saadadb.products.datafile.DataFile)
	 */
	@Override
	public void mapDataFile(DataFile dataFile) throws Exception{
		super.mapDataFile(dataFile);
		this.entryBuilder.mapDataFile(dataFile);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#mapDataFile()
	 */
	@Override
	public void mapDataFile() throws Exception{
		super.mapDataFile();
		this.entryBuilder.mapDataFile();
	}

	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#calculateAllExpressions()
	 */
	@Override
	protected void calculateAllExpressions() throws Exception {
		super.calculateAllExpressions();
		this.entryBuilder.calculateAllExpressions();
	}

	protected void setProductIngestor() throws Exception{
		super.setProductIngestor();
		/*
		 * Case when the method is called by the superclass constructor
		 */
		if( this.entryBuilder != null ){
			this.entryBuilder.setProductIngestor();
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.Product#mergeProductFormat(java.io.File)
	 */
	@Override
	public void mergeProductFormat(DataFile file_to_merge) throws FitsException, IOException, SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge TABLE format with file <" + file_to_merge.getName() + ">");
		/*
		 * Store the current set of attribute handlers
		 */
		Map<String, AttributeHandler> tableAttributeHandler_org;
		tableAttributeHandler_org = this.productAttributeHandler;

		/*
		 * Build a new set of attribute handlers from the product given as a parameter
		 */
		ProductBuilder prd_to_merge = this.mapping.getNewProductBuilderInstance(file_to_merge, this.metaClass);
		prd_to_merge.mapping = this.mapping;

		try {
			prd_to_merge.dataFile = new FitsDataFile(prd_to_merge);		
			this.typeFile = "FITS";
		}
		catch(Exception ef) {
			try {
				prd_to_merge.dataFile = new VOTableDataFile(prd_to_merge);
				this.typeFile = "VO";
			}
			catch(Exception ev) {
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + file_to_merge + "> neither FITS nor VOTable");			
			}
		}

		//		
		//        if( filename.endsWith(".fit") || filename.endsWith(".fits") ||
		//            filename.endsWith(".fits.gz") || filename.endsWith(".fit.gz") || filename.endsWith(".ftz")) {
		//            this.typeFile = "FITS";
		//            prd_to_merge.productFile = new FitsProduct(prd_to_merge);
		//        }
		//        else if( filename.endsWith(".xml") || filename.endsWith(".vo") ||
		//                filename.endsWith(".votable") || filename.endsWith(".vot") ) {
		//            this.typeFile = "VO";
		//            prd_to_merge.productFile = new VOProduct(prd_to_merge);
		//        }
		//        else {
		//            IgnoreException.throwException("<" + filename + "> File type not recognized");
		//            return;
		//        }
		/*
		 * Merge old a new sets of attribute handlers
		 */
		Iterator<AttributeHandler> it = prd_to_merge.getProductAttributeHandler().values().iterator();
		while( it.hasNext()) {
			AttributeHandler new_att = it.next();
			AttributeHandler old_att = null;
			if( (old_att = tableAttributeHandler_org.get(new_att.getNameattr())) != null ) {
				old_att.mergeAttribute(new_att);
			}
			else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameattr() + ">");
				tableAttributeHandler_org.put(new_att.getNameattr(), new_att);
			}
		}
		this.setFmtsignature();   
		/*
		 * Merge old a new sets of entry attribute handlers
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge ENTRY format with file <" + file_to_merge.getName() + ">");
		tableAttributeHandler_org = this.entryBuilder.getProductAttributeHandler();
		EntryBuilder entry_to_merge = ((TableBuilder)(prd_to_merge)).entryBuilder;
		entry_to_merge.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		entry_to_merge.productAttributeHandler = prd_to_merge.dataFile.getEntryAttributeHandler();

		for( AttributeHandler new_att: entry_to_merge.getProductAttributeHandler().values()) {
			AttributeHandler old_att = null;
			if( (old_att = tableAttributeHandler_org.get(new_att.getNameattr())) != null ) {
				old_att.mergeAttribute(new_att);
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameattr() + ">");
				tableAttributeHandler_org.put(new_att.getNameattr(), new_att);
			}
		}
		this.entryBuilder.setFmtsignature();   
	}

}
