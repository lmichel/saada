package saadadb.util;

import saadadb.database.Database;
import saadadb.database.DbmsWrapper;
import saadadb.database.MysqlWrapper;
import saadadb.database.PostgresWrapper;
import saadadb.database.SQLiteWrapper;
import saadadb.exceptions.FatalException;
import saadadb.vocabulary.enums.DBType;

public class DBUtils {

	/**
	 * Encapsulate the string with "", ``, '' depending on the database type that is used
	 * Currently, "name" is only wrapped in `` for MYSQL DB to ensure that it won't be rejected when executing the query (reserved keyword)
	 * @param name
	 * @return
	 */
	public static String encapsulate(String name) {
		try {
			DbmsWrapper wrapper = Database.getWrapper();
			// MYSQL
			if (wrapper instanceof MysqlWrapper) {
				return "`" + name + "`";

			} else if (wrapper instanceof PostgresWrapper) {
				// POSTGRES
				return name;

			} else {
				// SQLite
				return name;
			}
		} catch (FatalException e) {
			return name;
		}
	}
	
	/**
	 * Get the Database Wrapper to find out which jdbc driver has been loaded
	 * @return a DBType representing the jdbc driver that has been loaded Or DBTpe.NODB in case of failure
	 */
	public static DBType getDBType() {
		try {
			DbmsWrapper wrapper = Database.getWrapper();
			
			if(wrapper instanceof MysqlWrapper) {
				return DBType.MYSQL;
			}
			if(wrapper instanceof PostgresWrapper) {
				return DBType.POSTRESQL;
			}
			if(wrapper instanceof SQLiteWrapper) {
				return DBType.SQLITE;
			}
			
			return DBType.NODB;
			
		} catch (FatalException e) {
			return DBType.NODB;
		}
	}
	
	public static String getTempoDBName() {
		try {
			return Database.getTempodbName();
		} catch (FatalException e) {
			return "";
		}
	}
}
