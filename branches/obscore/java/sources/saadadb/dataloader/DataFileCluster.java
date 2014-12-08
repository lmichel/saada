/**
 * 
 */
package saadadb.dataloader;

import java.util.ArrayList;
import java.util.List;

import saadadb.meta.MetaClass;

/**
 * Simple container fo of set of data files having all the same format
 * @author michel
 * @version $Id$
 */
public class DataFileCluster {
	public List<String> fileList = new ArrayList<String>();
	public MetaClass classe;
	public MetaClass entryClasse;

}
