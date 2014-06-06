package saadadb.products;

import saadadb.enums.ExtensionSetMode;


/**
 * This class contains the number of the loaded extension and the way it has be chosen.
 * @author michel
 * @version $Id$
 */
public class ExtensionSetter {
	protected final ExtensionSetMode setMode;
	/**
	 * Log of the mapping process
	 */
	public StringBuffer message = new StringBuffer();
	public final int goodHeaderNumber;

	/**
	 * @param goodHeaderNumber
	 * @param setMode
	 * @param message
	 */
	ExtensionSetter(int goodHeaderNumber,  ExtensionSetMode setMode, String message){
		this.goodHeaderNumber = goodHeaderNumber;
		this.setMode = setMode;
		this.completeMessage(message);
	}
	/**
	 * Default constructor
	 */
	public ExtensionSetter(){
		this.goodHeaderNumber = 0;
		this.setMode = ExtensionSetMode.NOT_SET;
		this.completeMessage("Default value");
	}
	
	/**
	 * add "message" to the mapping log
	 * @param message
	 */
	public void completeMessage(String message){
		if( this.message.length() > 0 )
			this.message.append(" ");
		this.message.append(message);
	}

	/**
	 * @return
	 */
	public int getGoodHeaderNumber() {
		return this.goodHeaderNumber;
	}
	
	/**
	 * @return
	 */
	public String getMode() {
		return (setMode == ExtensionSetMode.DETECTED)? "DETECTED" :
			   (setMode == ExtensionSetMode.GIVEN)? "GIVEN" : "NOT_SET";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Extension #" +this.goodHeaderNumber + " "
		+ this.getMode() + " "
		+ this.message;
	}

}
