package saadadb.prdconfiguration;

/**
 * Simple reference to a data file header or resource (name + number)
 * @author michel
 *
 */
public class HeaderRef {
	private int number;
	/** name can be either a FITS header or a VOtable ID or a votable name */
	private String name;
	
	/**
	 * @param number
	 */
	public HeaderRef(int number) {
		this.setNumber(number);
	}
	
	public HeaderRef(String name) {
		this.setName(name);
	}
	
	/**
	 * @param name
	 * @param num
	 */
	public HeaderRef(String name, int num) {
		this.setName(name);
		this.setNumber(num);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the number.
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param name
	 * @return
	 */
	public boolean isTheGoodOne(String name) {
		return this.name.equals(name);
	}
	/**
	 * @param num
	 * @return
	 */
	public boolean isTheGoodOne(int num) {
		if( num == this.number ) {
			return true;
		}
		return false;
	}
	/**
	 * @param num
	 * @return
	 */
	public boolean isTheGoodOne(String name, int num) {
		return (this.isTheGoodOne(num) & this.isTheGoodOne(name));
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
		if( name == null ) {
			this.name = "";
		}
	}

	/**
	 * @param number The number to set.
	 */
	public void setNumber(int number) {
		this.number = number;
	}
}
