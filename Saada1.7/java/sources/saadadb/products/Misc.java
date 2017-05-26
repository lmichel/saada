package saadadb.products;

import java.io.File;

import saadadb.prdconfiguration.ConfigurationDefaultHandler;

/**This class redefines method specific in miscs during their collection load.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public class Misc extends Product{
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;

						/* ######################################################
						 * 
						 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
						 * 
						 *#######################################################*/

	public Misc(DataResourcePointer file, ConfigurationDefaultHandler conf){	
		super(file, conf);
	}
	
	/**
	 * 
	 */
	protected void mapCollectionAttributes() {
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

