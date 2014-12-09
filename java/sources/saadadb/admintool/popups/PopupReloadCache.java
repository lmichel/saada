package saadadb.admintool.popups;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class PopupReloadCache extends JFrame 
{
	private static final long serialVersionUID = 1L;
	
	public PopupReloadCache(Frame base)
	{
		this.setLayout(new FlowLayout());
	    this.setSize(280, 140);
	    this.setLocationRelativeTo(base);
	    this.setUndecorated(true);
	    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    JLabel lbl = new JLabel("Please wait, reloading the cache...");
	    lbl.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/reloadCache.gif")));
	    this.add(lbl);
	    this.pack();
	}
	
	public void showPopup()
	{
		this.requestFocus();
		this.setVisible(true);
	}
	
	public void hipePopup()
	{
		this.setVisible(false);
	}
}	
