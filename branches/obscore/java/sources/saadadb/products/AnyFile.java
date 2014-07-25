package saadadb.products;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.inference.SpaceKWDetector;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class AnyFile extends File implements DataFile {

	private List<String> comments = new ArrayList<String>();

	public AnyFile(File parent, String child) {
		super(parent, child);
	}
	
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

	public double[] getExtrema(String key) {
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
	public Map<String, AttributeHandler> getAttributeHandler()
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<AttributeHandler>> getProductMap(int category)
			throws IgnoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QuantityDetector getQuantityDetector(ProductMapping productMapping) throws SaadaException {
		return new QuantityDetector( new LinkedHashMap<String, AttributeHandler>(), this.comments, productMapping);
	}

	@Override
	public List<String> getComments() throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}


}
