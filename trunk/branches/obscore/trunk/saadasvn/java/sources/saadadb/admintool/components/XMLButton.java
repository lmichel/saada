package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.panels.AdminPanel;

public class XMLButton extends JButton{
	private final AdminPanel adminPanel;
	private Runnable runnable;
	
	public XMLButton(AdminPanel adminPanel, Runnable runnable) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/XML.png")));
		this.adminPanel =adminPanel;
		this.setPreferredSize(new Dimension(60, 40));
		this.runnable = runnable;
		if( this.runnable != null ) {
			this.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					XMLButton.this.runnable.run();
				}
			});
		}

	}

}
