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
public class ExtendedAttMapping extends AxisMapping {

	ExtendedAttMapping(ArgsParser ap, boolean entryMode) throws SaadaException{
		super(ap,  Database.getCachemeta().getAtt_extend(Category.getCategory(ap.getCategory())).keySet().toArray(new String[0]), entryMode);		
		
		for( String attName: this.attributeNames) {
			String mappingValue =  ap.getUserKeyword(entryMode, attName);
			if( mappingValue != null ) {
				this.columnMapping.put(attName, new ColumnMapping(null, mappingValue, attName));
			}
		}
		this.completeColumns();
	}

}
