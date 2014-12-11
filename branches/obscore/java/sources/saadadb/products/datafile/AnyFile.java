package saadadb.products.datafile;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.IgnoreException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.ProductBuilder;

/**
 * Class modeling data files without data (flatfiles e.g.)
 * @author laurentmichel
 * @version $Id$
 */
public class AnyFile extends FSDataFile {


	/**
	 * @param fileName
	 * @throws Exception 
	 */
	public AnyFile(String fileName, ProductMapping productMapping) throws Exception {
		super(fileName,productMapping);
		this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#closeStream()
	 */
	public void closeStream() {
	}
	public String getKWValueQuickly(String key) {
		return null;
	}


	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getExtrema(java.lang.String)
	 */
	public Object[] getExtrema(String key) {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getNRows()
	 */
	public int getNRows() throws IgnoreException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#getNCols()
	 */
	public int getNCols() throws IgnoreException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#initEnumeration()
	 */
	public void initEnumeration() throws IgnoreException {
		
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public Object nextElement() {
		return null;
	}


	@Override
	public void bindBuilder(ProductBuilder builder) throws Exception {
		this.productBuilder = builder;
		this.productBuilder.productAttributeHandler = this.getAttributeHandlerCopy();	
	}

	@Override
	public List<ExtensionSetter> reportOnLoadedExtension() {
		return null;
	}


	@Override
	public void bindEntryBuilder(ProductBuilder builder) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, DataFileExtension> getProductMap() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mapAttributeHandler() throws IgnoreException {
		this.productBuilder.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		
	}

	@Override
	public void mapEntryAttributeHandler() throws IgnoreException {
		this.productBuilder.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
	}



}
