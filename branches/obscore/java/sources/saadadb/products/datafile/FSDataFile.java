package saadadb.products.datafile;

import java.io.File;
import java.io.IOException;

/**
 * Super class of all types of DataFile being on the file system 
 * @author michel
 * @version $Id$
 */
public abstract class FSDataFile extends DataFile{
	public File file;

	/**
	 * @param parent
	 * @param child
	 */
	public FSDataFile(File parent, String child) {
		this.file = new File(parent, child);
	}
	
	/**
	 * @param fileName
	 */
	public FSDataFile(String fileName) {
		this.file = new File(fileName);
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return this.file.getCanonicalPath();
	}
	@Override
	public String getAbsolutePath() {
		return this.file.getAbsolutePath();
	}
	@Override
	public String getParent() {
		return  this.file.getParent();
	}
	@Override
	public long length() {
		return this.file.length();
	}
	@Override
	public boolean delete() {
		return this.file.delete();
	}
	@Override
	public String getName() {
		return this.file.getName();
	}
}
