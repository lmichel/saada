package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.util.Messenger;


public class DebugButton extends JButton{
	private Color debugColor, noDebugColor;
	
	public DebugButton() {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/DebugMode.png")));
		noDebugColor = this.getBackground();
		debugColor = new Color(255, 233, 181);;
		this.setOpaque(true);
		this.setPreferredSize(new Dimension(60, 40));
		this.active();

		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Messenger.debug_mode = (Messenger.debug_mode)?false: true;
				if( Messenger.debug_mode ) {
					setBackground(debugColor);
				}
				else {
					setBackground(noDebugColor);
				}
			}
		});
	}

	public void active() {
		if( Messenger.debug_mode ) {
			setBackground(debugColor);
		}
		else {
			setBackground(noDebugColor);
		}

	}
}
