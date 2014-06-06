package saadadb.products;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.enums.PriorityMode;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.EnergyKWDetector;
import saadadb.products.inference.ObservableKWDetector;
import saadadb.products.inference.ObservationKWDetector;
import saadadb.products.inference.SpaceKWDetector;
import saadadb.products.inference.TimeKWDetector;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class AnyFile extends File implements DataFile {


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
	public ObservationKWDetector getObservationKWDetector(boolean entryMode)
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpaceKWDetector getSpaceKWDetector(boolean entryMode)
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnergyKWDetector getEnergyKWDetector(boolean entryMode, PriorityMode priority, String defaultUnit)
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeKWDetector getTimeKWDetector(boolean entryMode)
			throws SaadaException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ObservableKWDetector getObservableKWDetector(boolean entryMode)
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
		// TODO Auto-generated method stub
		
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


}
