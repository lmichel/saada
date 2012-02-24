package saadadb.admintool.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class HelpedTextField extends JTextField { 
	public static final Color HELPCOLOR = Color.LIGHT_GRAY;
	private Runnable runnable;

	public HelpedTextField(String helpString){
		this.setText(helpString);
		this.setForeground(HELPCOLOR);
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switchToEdit();
			}
			public void keyReleased(KeyEvent e) {
				if( runnable != null ) {
					runnable.run();
				}
			}
		});
		this.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				switchToEdit();
				if( runnable != null ) {
					runnable.run();
				}
			}
		});
	}
	public HelpedTextField(String helpString, int width){
		this(helpString);
		this.setColumns(width);
	}

	public void  setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	private void switchToEdit() {
		if( this.getForeground() == HELPCOLOR) {
			super.setText("");
			this.setForeground(Color.BLACK);
		}		
	}

	public void setText(String text) {
		switchToEdit();
		super.setText(text);
	}

	public String getText() {
		switchToEdit();
		return super.getText();
	}
}
