package saadadb.dataloader.mapping;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;

/**
 * @author michel
 * @version $Id$
 *
 */
public class ExtendedAttMapping extends AxeMapping {

	ExtendedAttMapping(ArgsParser ap, boolean entryMode) throws SaadaException{
		super(ap,  Database.getCachemeta().getAtt_extend(Category.getCategory(ap.getCategory())).keySet().toArray(new String[0]), entryMode);		
		
		for( String s: this.attributeNames) {
			String v =  ap.getUserKeyword(entryMode, s);
			if( v != null ) {
				this.columnMapping.put(s, new ColumnMapping(ap.getUnit(), v));
			}
		}
		this.completeColumns();
	}

}
