package saadadb.products;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import saadadb.exceptions.IgnoreException;
import saadadb.meta.AttributeHandler;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class AnyFile extends File implements ProductFile {


	public AnyFile(File parent, String child) {
		super(parent, child);
	}

	public AnyFile(Product product) {
		super(product.file.getAbsolutePath());
	}

	public String getKWValueQuickly(String key) {
		return null;
	}

	public LinkedHashMap getTableEntry() {
		return null;
	}

	public void getKWEntry(LinkedHashMap<String, AttributeHandler> tah) throws IgnoreException {
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

	public LinkedHashMap<String, ArrayList<AttributeHandler>> getMap(String category) throws IgnoreException {
		return null;
	}

	public SpaceFrame getSpaceFrame() {
		return null;
	}

	public void setSpaceFrame() {
		
	}

	public void setSpaceFrameForTable() throws IgnoreException {
		
	}

}
