package saadadb.database;

/**
 * @author laurentmichel
 * * @version $Id$

 */
class DBUser {
	
	private String name;
	private String password;
	
	public DBUser(String name, char[] password) {
		this.name = name;
		this.password = new String(password);
	}

	public DBUser(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	} 

}
