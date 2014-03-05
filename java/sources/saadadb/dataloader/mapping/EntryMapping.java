package saadadb.dataloader.mapping;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;

/**
 * Clone of ProductMapping  but forced to be an entry
 * @author michel
 * @version $Id$
 *
 */
public class EntryMapping extends ProductMapping {

	public EntryMapping(String name, ArgsParser ap) throws SaadaException {
		super(name, ap, true);
		this.category = Category.ENTRY;
    	this.stoeMapping    = new STEOMapping(ap, true);


	}

}