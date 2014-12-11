package saadadb.products.datafile;

import java.io.IOException;

import saadadb.dataloader.mapping.ProductMapping;

/**
 * Super class of all types of DataFile being on the file system 
 * @author michel
 * @version $Id$
 */
public abstract class FSDataFile extends DataFile{
	public DataResourcePointer file;

	/**
	 * @param fileName
	 * @throws Exception 
	 */
	public FSDataFile(String fileName, ProductMapping productMapping) throws Exception {
		super(productMapping);
		this.file = new DataResourcePointer(fileName);
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
