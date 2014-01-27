/**
 * 
 */
package upgrade.collection;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Collection;
import saadadb.sqltable.Table_Saada_Relation;

/**
 * @author michel
 * @version $Id$
 *
 */
public class Upgrade {
	
	public static void upgrade() throws Exception {
		Table_Saada_Relation.addStatColumn();
		Table_Saada_Class.addStatColumn();
		Table_Saada_Collection.addStatColumn();		

	}

}
