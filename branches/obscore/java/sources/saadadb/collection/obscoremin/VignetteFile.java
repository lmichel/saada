/**
 * 
 */
package saadadb.collection.obscoremin;

import java.io.File;
import java.io.IOException;

import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * Manage the name of the vignette file attached to one product file
 * The vignette file is searched in a directory given to the constructor. 
 * Its is supposed to have the same name as this of the product but suffixed with one of {@link saadadb.util.RegExp#PICT_FORMAT}
 * @author michel
 * @version $Id$
 *
 */
public class VignetteFile {
	private String path=null;
	private String name=null;

	
	/**
	 * Set instance fields if a vignette file is found.
	 * Fields remains null otherwise
	 * @param searchPath
	 * @param productName
	 * @throws IOException
	 */
	public VignetteFile(String searchPath, String productName) throws IOException {
		for( String suffix: RegExp.PICT_FORMAT) {
			File f = new File(searchPath + File.separator + productName + "." + suffix);
			if( f.exists() ) {
				this.name = f.getName();
				this.path = f.getCanonicalPath();
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Vignette file " + this.path + " found");
				return;
			}
		}
	}

	/**
	 * Returns true if a vignette file as been found a creation time.
	 * Does no check
	 * @return
	 */
	public boolean exists() {
		return (this.path == null || this.name == null )? false: true;
	}

	/**
	 * returns the name of the vignette file
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * returns the canonical path  of the vignette file
	 * @return
	 */
	public String getPath() {
		return this.path;
	}
}
