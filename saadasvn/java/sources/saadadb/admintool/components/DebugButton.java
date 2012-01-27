package saadadb.admintool.components;

import java.awt.Color;
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
		debugColor = Color.ORANGE;
		this.setOpaque(true);
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
