package saadadb.products;

import java.io.File;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;

/**This class redefines method specific in miscs during their collection load.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public class Misc extends Product{
	/** * @version $Id: Misc.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;

						/* ######################################################
						 * 
						 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
						 * 
						 *#######################################################*/

	public Misc(File file, ProductMapping mapping){	
		super(file, mapping);
	}
	
	/**
	 * @throws SaadaException 
	 * 
	 */
	protected void mapCollectionAttributes() throws SaadaException {
		this.mapInstanceName();
		this.mapIgnoredAndExtendedAttributes();
	}
	/*
	 * We prefer to use polymorphism to inhibit methods, taht allows to keep
	 * a unique code for loadValue for all product
	 * As loadValu is a very critic part of te Saada code, having it once
	 * seems to be a god idea
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
}

