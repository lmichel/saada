package saadadb.admintool.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;


public class JBlinkingButton extends JButton {
	private Color bkgColor;
	private static Color BLKCOLOR = Color.ORANGE;
	private Timer blinker;
	public static final int BLINK_PERIOD = 500;
	public JBlinkingButton(Icon icon) {
		super(icon);
		bkgColor = this.getBackground();
	}
	
	public void  startBlinking() {
		blinker = new Timer(BLINK_PERIOD, new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	        	  if( JBlinkingButton.this.getBackground() == BLKCOLOR) {
	        		  JBlinkingButton.this.setBackground(JBlinkingButton.this.bkgColor);
	        	  }
	        	  else {
	        		  JBlinkingButton.this.setBackground(BLKCOLOR);
	        	  }
	          }
	       });
		blinker.start();
	}
	
	public void  stopBlinking(boolean onBlinkColor) {
		if( blinker != null ) {
			blinker.stop();
		}
		if( onBlinkColor) {
  		  this.setBackground(BLKCOLOR);
		}
		else {
  		  this.setBackground(JBlinkingButton.this.bkgColor);
		}
	}
}
