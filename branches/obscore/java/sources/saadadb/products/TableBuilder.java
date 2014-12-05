package saadadb.products;

import java.io.BufferedWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
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
	public TableBuilder(DataFile dataFile, ProductMapping mapping, MetaClass metaClass) throws Exception{	
		super(dataFile, mapping, metaClass);
		System.out.println("@@@@@@@@@@@@@ build entry1");

		this.entryBuilder = new EntryBuilder(this);
		this.dataFile = dataFile;

		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
			// Once the tale builder is ready, we can setup the entry builder. That is not done in the EntryBuilder 
			// constructor because we need both builder to be ready.
			// Same data file shared by both table and entry Buidlers
			this.entryBuilder.dataFile = this.dataFile;
			// Set the ENTRY AHs with the columns headers of the data file
			this.dataFile.bindEntryBuilder(this.entryBuilder);
			this.entryBuilder.setProductIngestor();
			// The entry builder uses its own Quantity detector
			this.setQuantityDetector();
			// And it does its own mapping
			this.entryBuilder.mapCollectionAttributes();
			this.entryBuilder.setFmtsignature();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}	
	public TableBuilder(DataFile dataFile, ProductMapping mapping) throws Exception{	
		super(dataFile, mapping, null);
		System.out.println("@@@@@@@@@@@@@ build entryé");

		this.entryBuilder = new EntryBuilder(this);		
		// make sure the datafile has not taken by the EntryBuilder
		this.dataFile = dataFile;
		try {
			this.bindDataFile(dataFile);
			this.setQuantityDetector();
			// Once the tale builder is ready, we can setup the entry builder. That is not done in the EntryBuilder 
			// constructor because we need both builder to be ready.
			// Same data file shared by both table and entry Buidlers
			this.entryBuilder.dataFile = this.dataFile;
			// Set the ENTRY AHs with the columns headers of the data file
			this.dataFile.bindEntryBuilder(this.entryBuilder);
			// The entry builder rus its own Quantity detector
			this.setQuantityDetector();
			// And it does its own mapping
			this.entryBuilder.mapCollectionAttributes();
			this.entryBuilder.setFmtsignature();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}	

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getTableOid()
	 */
	@Override
	public long getTableOid() {
		return oid;
	}
	
	/**
	 * @return Returns the metaclass.
	 * @param metaClass
	 * @throws IgnoreException if mc is null
	 */
	public void setMetaclass(MetaClass metaClass) throws Exception {
		super.setMetaclass(metaClass);
		this.entryBuilder.tableClass = metaClass;
		MetaClass eClass = null;
		/*
		 * The entry class can not be set at the first call of the method 
		 */
		try {
			eClass = Database.getCachemeta().getClass(metaClass.getAssociate_class());
			this.entryBuilder.setMetaclass(eClass);
		} catch(Exception e){}
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#loadProduct(java.io.BufferedWriter, java.io.BufferedWriter, java.io.BufferedWriter)
	 */
	@Override
	public void loadProduct(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {
		IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Table loading must never use loadProduct(colwriter, buswriter, loadedfilewriter) ");
	}

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
		this.entryBuilder.loadProduct(this.productIngestor.saadaInstance.oidsaada);
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
//		if( this.entryBuilder != null ){
//			this.entryBuilder.setProductIngestor();
//		}
	}

//	/* (non-Javadoc)
//	 * @see saadadb.products.Product#mergeProductFormat(java.io.File)
//	 */
//	@Override
//	public void mergeProductFormat(DataFile file_to_merge) throws Exception {
//		if (Messenger.debug_mode)
//			Messenger.printMsg(Messenger.DEBUG, "Merge TABLE format with file <" + file_to_merge.getName() + ">");
//		/*
//		 * Store the current set of attribute handlers
//		 */
//		Map<String, AttributeHandler> tableAttributeHandler_org;
//		tableAttributeHandler_org = this.productAttributeHandler;
//
//		/*
//		 * Build a new set of attribute handlers from the product given as a parameter
//		 */
//		ProductBuilder prd_to_merge = this.mapping.getNewProductBuilderInstance(file_to_merge, this.metaClass);
//		prd_to_merge.mapping = this.mapping;
//
//		try {
//			prd_to_merge.dataFile = new FitsDataFile(prd_to_merge);		
//			this.typeFile = "FITS";
//		}
//		catch(Exception ef) {
//			try {
//				prd_to_merge.dataFile = new VOTableDataFile(prd_to_merge);
//				this.typeFile = "VO";
//			}
//			catch(Exception ev) {
//				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + file_to_merge + "> neither FITS nor VOTable");			
//			}
//		}
//
//		/*
//		 * Merge old a new sets of attribute handlers
//		 */
//		Iterator<AttributeHandler> it = prd_to_merge.getProductAttributeHandler().values().iterator();
//		while( it.hasNext()) {
//			AttributeHandler new_att = it.next();
//			AttributeHandler old_att = null;
//			if( (old_att = tableAttributeHandler_org.get(new_att.getNameattr())) != null ) {
//				old_att.mergeAttribute(new_att);
//			}
//			else {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameattr() + ">");
//				tableAttributeHandler_org.put(new_att.getNameattr(), new_att);
//			}
//		}
//		this.setFmtsignature();   
//		/*
//		 * Merge old a new sets of entry attribute handlers
//		 */
//		if (Messenger.debug_mode)
//			Messenger.printMsg(Messenger.DEBUG, "Merge ENTRY format with file <" + file_to_merge.getName() + ">");
//		tableAttributeHandler_org = this.entryBuilder.getProductAttributeHandler();
//		EntryBuilder entry_to_merge = ((TableBuilder)(prd_to_merge)).entryBuilder;
//		entry_to_merge.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
//		entry_to_merge.productAttributeHandler = prd_to_merge.dataFile.getEntryAttributeHandlerCopy();
//
//		for( AttributeHandler new_att: entry_to_merge.getProductAttributeHandler().values()) {
//			AttributeHandler old_att = null;
//			if( (old_att = tableAttributeHandler_org.get(new_att.getNameattr())) != null ) {
//				old_att.mergeAttribute(new_att);
//			} else {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameattr() + ">");
//				tableAttributeHandler_org.put(new_att.getNameattr(), new_att);
//			}
//		}
//		this.entryBuilder.setFmtsignature();   
//	}

}
