package saadadb.admintool.utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Specialization of the GridBagConstraints with behavior adapted to the Saada Look&Feel
 * @author michel
 * @version $Id$
 *
 */
public class MyGBC extends GridBagConstraints {
	private static final long serialVersionUID = 1L;
	public MyGBC() {
		super();
		this.gridx = this.gridy = 0;
		this.weightx = this.weighty = 0;
		this.gridwidth = this.gridheight = 1;
		this.anchor = GridBagConstraints.CENTER;
	}
	/**
	 * @param x0 inset size
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public MyGBC(int x0, int y0, int x1, int y1) {
		reset(x0, y0, x1, y1);
	}
	
	/**
	 * Reset all parameters
	 * @param x0 inset size
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public void reset(int x0, int y0, int x1, int y1) {
		this.insets = new Insets(x0, y0, x1, y1);
		this.gridx = this.gridy = 0;
		this.weightx = this.weighty = 0;
		this.gridwidth = this.gridheight = 1;
		this.anchor = GridBagConstraints.CENTER;
	}
	
	/**
	 * Add a new cell to the current row
	 */
	public void next() {
		this.weightx = 0;
		this.gridx++;		
		this.gridwidth = this.gridheight = 1;
	}
	
	/**
	 * Make the current cell expanded to the row end
	 */
	public void rowEnd(){
		this.weightx = 1;
		this.gridx++;				
		this.gridwidth = this.gridheight = 1;
	}
	
	/**
	 * Start a new row in the grid
	 */
	public void newRow() {
		this.weightx = 0;
		this.gridx = 0;		
		this.gridy++;	
		this.gridwidth = this.gridheight = 1;

	}
	
	/**
	 *   Center the component in its cell
	 */
	public void center() {
		this.anchor = GridBagConstraints.CENTER;		
	}
	/**
	 * Push the component on the left of the cell. 
	 * @param heavy: if true the cell will take all the available room. Otherwise the room is shared
	 */
	public void left(boolean heavy) {
		this.weightx = (heavy)?1:0;
		this.anchor = GridBagConstraints.WEST;		
	}
	/**
	 * Push the component on the right of the cell. 
	 * @param heavy: if true the cell will take all the available room. Otherwise the room is shared
	 */
	public void right(boolean heavy) {
		this.weightx = (heavy)?1:0;
		this.anchor = GridBagConstraints.EAST;		
	}

}
