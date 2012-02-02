package saadadb.admintool.utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;


public class MyGBC extends GridBagConstraints {
	MyGBC() {
		super();
	}
	public MyGBC(int x0, int y0, int x1, int y1) {
		reset(x0, y0, x1, y1);
	}
	
	public void reset(int x0, int y0, int x1, int y1) {
		this.insets = new Insets(x0, y0, x1, y1);
		this.gridx = this.gridy = 0;
		this.weightx = this.weighty = 0;
		this.gridwidth = this.gridheight = 1;
		this.anchor = GridBagConstraints.CENTER;
	}
	
	public void next() {
		this.weightx = 0;
		this.gridx++;		
	}
	
	public void rowEnd(){
		this.weightx = 1;
		this.gridx++;				
	}
	
	public void newRow() {
		this.weightx = 0;
		this.gridx = 0;		
		this.gridy++;				
	}
	
	public void center() {
		this.anchor = GridBagConstraints.CENTER;		
	}
	public void left() {
		this.anchor = GridBagConstraints.WEST;		
	}
	public void right() {
		this.anchor = GridBagConstraints.EAST;		
	}

}
