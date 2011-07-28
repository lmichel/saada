package saadadb.meta;

import java.sql.ResultSet;

import saadadb.util.Messenger;


/**
 * 
 */

/**
 * @author michel
 *
 */
public class MetaObject {

	protected String name;
	protected int id;
	/**
	 * @param rs
	 */
	MetaObject(ResultSet rs)  {
		try {
			this.name = rs.getString("name").trim(); 
			this.id = rs.getInt("id");
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
	}
	
	MetaObject(String name, int id) {
		this.name = name;
		this.id = id;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

}
