package saadadb.sqltable;

import saadadb.database.Database;
import saadadb.exceptions.SaadaException;

public abstract  class Table_Saada_Group extends SQLTable {


	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saada_group", "id int, name " + Database.getWrapper().getIndexableTextType() , "name", false);
	}
}
