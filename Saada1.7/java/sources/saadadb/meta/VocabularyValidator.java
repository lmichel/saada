package saadadb.meta;

import saadadb.database.Database;
import saadadb.util.RegExp;


/**
 * Utility checking the validity of Saada entities named by user inputs
 * @author michel
 * @version $Id$
 *
 */
public class VocabularyValidator {
	
	/**
	 * Return true if newName is compliant and if it has not been used before
	 * @param newName
	 * @param message String buffer where the reason of rejection  is reported
	 * @return
	 */
	public static final boolean checkClassOrRelationName(String newName, StringBuffer message){
		if( newName.matches(RegExp.CLASSNAME) ) {
			if(newName.startsWith("saada") ){
				if( message != null ) message.append("Names beginning with saada are not allowed");
				return true;
			} else if( Database.getCachemeta().relationExists(newName)) {
				if( message != null ) message.append( "A relation named " + newName + " already exists");
				return true;
			} else if( Database.getCachemeta().classExists(newName)) {
				if( message != null ) message.append( "A data class named " + newName + " already exists");
				return true;
			} else if( Database.getCachemeta().collectionExists(newName)) {
				if( message != null ) message.append( "A data collection named " + newName + " already exists");
				return true;
			} else {
				return true;
			}
		} else {
			if( message != null ) message.append( "Valid names must match " + RegExp.CLASSNAME);
			return false;
		}
	}

}
