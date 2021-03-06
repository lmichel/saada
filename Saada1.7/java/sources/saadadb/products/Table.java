package saadadb.products;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.Messenger;

/**
 * This class redefines method specific in tables during their collection load.
 * 
 * @author Millan Patrick
 * @version 2.0
 * @since 2.0
 */
public class Table extends Product {
	/** * @version $Id$
 * @version $Id$
 * @version $Id$
 * @version $Id$
 * @version $Id$
vvvvv * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The identification number of this table in data base* */
	protected long oid;
	protected Entry entry;
	
	/**
	 * @param fileName
	 * @param tabArg
	 */
	public Table(DataResourcePointer file, ConfigurationDefaultHandler conf){
	
		super(file, conf);
		this.entry = new Entry(this);
	}	
	/**
	 * @return Returns the entry.
	 */
	public Entry getEntry() {
		return entry;
	}


	/*
	 * Returns the identification number of this table in data base. @return
	 * long The identification number of this table in data base.
	 */
	public long getTableOid() {
		return oid;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.Product#initProductFile(java.lang.String, saadadb.prdconfiguration.Configuration)
	 */
	public void initProductFile(ConfigurationDefaultHandler configuration) throws IgnoreException{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Init TABLE instance");
		super.initProductFile(configuration);
		/*
		 * this.entry.loadProductFile already done in loadProductFile(...)
		 */
		this.entry.mapCollectionAttributes();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.Product#loadProductFile(saadadb.prdconfiguration.ConfigurationDefaultHandler)
	 */
	public void loadProductFile(ConfigurationDefaultHandler configuration) throws IgnoreException{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "load TABLE instance without mapping");
		super.loadProductFile(configuration);
		this.entry.loadProductFile(configuration);
		this.productFile.setSpaceFrameForTable();
	}
	
	public void loadValue() throws Exception  {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load table header");
		super.loadValue();
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load entries ");
		this.entry.loadValue();
	}

	/*
	 * We prefer to use the polymorphism to inhibit methods, that allows us to keep
	 * a unique code for loadValue for all product
	 * As loadValu is a very critic part of te Saada code, having it once
	 * seems to be a god idea.
	 * There is neither astrograme nor position for tables
	 */
	/* 
	 * Does nothing for tables
	 * (non-Javadoc)
	 * @see saadadb.products.Product#setAstrofFrame()
	 */
	public void setAstrofFrame() {
	}

	/* 
	 * 	Does nothing for tables
	 * (non-Javadoc)
	 * @see saadadb.products.Product#setPositionFields(int)
	 */
	public void setPositionFields(int line) {	
	}
	
    /* (non-Javadoc)
     * @see saadadb.products.Product#mergeProductFormat(java.io.File)
     */
    public void mergeProductFormat(DataResourcePointer file_to_merge) throws FitsException, IOException, SaadaException {
    	if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge TABLE format with file <" + file_to_merge.file.getName() + ">");
    	/*
         * Store the current set of attribute handlers
         */
        LinkedHashMap<String, AttributeHandler> tableAttributeHandler_org;
        tableAttributeHandler_org = this.tableAttributeHandler;

        /*
         * Build a new set of attribute handlers from the product given as a parameter
         */
        String filename = file_to_merge.file.getName().toLowerCase();
        Product prd_to_merge = this.configuration.getNewProductInstance(file_to_merge);
        prd_to_merge.configuration = this.configuration;
        
		try {
			prd_to_merge.productFile = new FitsProduct(prd_to_merge);		
			this.typeFile = "FITS";
		}
		catch(Exception ef) {
			try {
				prd_to_merge.productFile = new VOTableProduct(prd_to_merge);
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
        Iterator<AttributeHandler> it = prd_to_merge.getTableAttributeHandler().values().iterator();
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
			Messenger.printMsg(Messenger.DEBUG, "Merge ENTRY format with file <" + file_to_merge.file.getName() + ">");
        tableAttributeHandler_org = this.getEntry().getTableAttributeHandler();
        Entry entry_to_merge = ((Table)(prd_to_merge)).getEntry();
        entry_to_merge.tableAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		prd_to_merge.productFile.setKWEntry(entry_to_merge.getTableAttributeHandler());

		it = entry_to_merge.getTableAttributeHandler().values().iterator();
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
        this.entry.setFmtsignature();   
        }
    
	/**
	 * 
	 */
	protected void mapCollectionAttributes() {
		this.mapInstanceName();
		this.mapIgnoredAndExtendedAttributes();
		this.mapCollectionCooSysAttributes();
	}
}
