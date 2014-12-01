package saadadb.products.datafile;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.ProductBuilder;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.inference.SpaceKWDetector;

/**
 * Class modeling data files without data (flatfiles e.g.)
 * @author laurentmichel
 * @version $Id$
 */
public class AnyFile extends FSDataFile {


	/**
	 * @param parent
	 * @param child
	 */
	public AnyFile(File parent, String child) {
		super(parent, child);
	}
	
	/**
	 * @param fileName
	 */
	public AnyFile(String fileName) {
		super(fileName);
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#closeStream()
	 */
	public void closeStream() {
	}
	public String getKWValueQuickly(String key) {
		return null;
	}

	public Map getTableEntry() {
		return null;
	}

	public void setKWEntry(Map<String, AttributeHandler> tah) throws IgnoreException {
	}

	public Image getSpectraImage() {
		return null;
	}

	public Object[] getRow(int index) throws IgnoreException {
		return null;
	}

	public Object[] getExtrema(String key) {
		return null;
	}

	public Object[] getRow(int index, int numHDU) throws IgnoreException {
		return null;
	}

	public int getNRows() throws IgnoreException {
		return 0;
	}

	public int getNCols() throws IgnoreException {
		return 0;
	}

	public void initEnumeration() throws IgnoreException {
		
	}

	public boolean hasMoreElements() {
		return false;
	}

	public Object nextElement() {
		return null;
	}

	public LinkedHashMap<String, ArrayList<AttributeHandler>> getProductMap(String category) throws IgnoreException {
		return null;
	}

	public SpaceKWDetector getSpaceFrame() {
		return null;
	}

	public void setSpaceFrame() {
		
	}

	public void setSpaceFrameForTable() throws IgnoreException {
		
	}

	@Override
	public Map<String, AttributeHandler> getEntryAttributeHandler()
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, AttributeHandler> getAttributeHandlerCopy()
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, DataFileExtension> getProductMap() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bindBuilder(ProductBuilder builder) throws Exception {
		builder.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		
	}

	@Override
	public List<ExtensionSetter> reportOnLoadedExtension() {
		return null;
	}

	@Override
	public void mapAttributeHandler() throws IgnoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mapEntryAttributeHandler() throws IgnoreException {
		// TODO Auto-generated method stub
		
	}




	

}
