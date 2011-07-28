package saadadb.prdconfiguration;

import saadadb.util.DefineType;
import saadadb.util.Messenger;

public class CoordSystem{
    private boolean autodetect = false;
    private String system="";
    private String equinox="";
    private String system_value="";
    private String equinox_value="";
	
	private int priority=DefineType.FIRST;
	/**This method confirms integrity of coordinates system and equinox in a configuration
	 * and sending message in case of no validation
	 * If the coordinates system corresponds to "FK5" or "FK4" (generaly in Fits product), or "eq_FK5" or "eq_FK4" (generaly in VO product),
	 * the equinox has to be "J1950" or "J2000".
	 * If the coordinate system corresponds to "galactic", the equinox has to be "J2000".
	 *@return boolean true or false if this coordinates system and this equinox are confirmed
	 */
	public boolean isValid(){

		if( autodetect ) {
			return true;
		}
		else if( (system == null || system.length() == 0) && (system_value == null || system_value.length() == 0) &&
				 (equinox == null || equinox.length() == 0) && (equinox_value == null || equinox_value.length() == 0) ){
				Messenger.printMsg(Messenger.ERROR, getClass().getName()+": coordinate system is not valid");
				return false;
			
		}
		return true;
	}


	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * 
	 */
	public void setAutodedect() {
		this.autodetect = true;		
	}
	/**
	 * @return
	 */
	public boolean getAutodedect() {
		return this.autodetect ;		
	}

	/**
	 * @return Returns the equinox.
	 */
	public String getEquinox() {
		return equinox;
	}

	/**
	 * @param equinox The equinox to set.
	 */
	public void setEquinox(String equinox) {
		this.equinox = equinox;
	}

	/**
	 * @return Returns the system.
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * @param system The system to set.
	 */
	public void setSystem(String system) {
		this.system = system;
	}


	/**
	 * @return Returns the equinox_value.
	 */
	public String getEquinox_value() {
		return equinox_value;
	}


	/**
	 * @param equinox_value The equinox_value to set.
	 */
	public void setEquinox_value(String equinox_value) {
		this.equinox_value = equinox_value;
	}


	/**
	 * @return Returns the system_value.
	 */
	public String getSystem_value() {
		return system_value;
	}


	/**
	 * @param system_value The system_value to set.
	 */
	public void setSystem_value(String system_value) {
		this.system_value = system_value;
	}


	/**
	 * Returns the string used to compute the configuration MD5
	 * @return
	 */
	public String getMD5() {
		return  autodetect + system + system_value + equinox + equinox_value;
	}
	
	/**
	 * Returns the string used to compute the configuration MD5
	 * @return
	 */
	public String toString() {
		return  autodetect + " sys=" + system +  " sys_val=" + system_value +  " equ=" + equinox +  " equval=s" + equinox_value;
	}
}

