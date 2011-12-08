package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import saadadb.admintool.AdminTool;

public class ChoiceItem extends AdminComponent {
	private String label;
	private JPanel choicePanel;
	private JPanel localPanel;
	private Runnable runnable;
	private ImageIcon activeIcon;
	private boolean active;

	public ChoiceItem(AdminTool rootFrame,  JPanel choicePanel, GridBagConstraints gbc, String label, String activeImage, Runnable runnable) {
		super(rootFrame);
		this.label = label;
		this.choicePanel = choicePanel;
		this.runnable = runnable;
		if( activeImage != null ) this.activeIcon = new ImageIcon(activeImage);
		JLabel jLabel = new JLabel(this.activeIcon);
		jLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		jLabel.setBorder(new EmptyBorder(10, 10, 1, 10));
		this.localPanel.add(jLabel);

		jLabel = new JLabel(this.label);
		jLabel.setFont(plainFont);		
		jLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		jLabel.setBorder(new EmptyBorder(1, 10, 5, 10));

		this.localPanel.add(jLabel);
		choicePanel.add(this.localPanel, gbc);
		this.localPanel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {	
				if( active ) {
					if( ChoiceItem.this.localPanel.getBorder() != null )   
						ChoiceItem.this.runnable.run();
					localPanel.setBackground(LIGHTBACKGROUND);      
				}
			}
			public void mousePressed(MouseEvent e) {if( active ) localPanel.setBackground(Color.ORANGE); }
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {
				if( active ) localPanel.setBorder(BorderFactory.createLineBorder(Color.black));
			}
			public void mouseExited(MouseEvent e) {
				if(active ) {
					localPanel.setBorder(null);
					localPanel.setBackground(LIGHTBACKGROUND); 
				}
			}});
		this.active();
	}

	/* (non-Javadoc)
	 * @see components.AdminComponent#setMainPanel()
	 */
	protected void setMainPanel() {
		this.localPanel = new JPanel();
		this.localPanel.setBackground(LIGHTBACKGROUND);       
		this.localPanel.setLayout(new BoxLayout(localPanel, BoxLayout.PAGE_AXIS));

	}

	/**
	 * 
	 */
	public void active() {
		active = true;
		this.localPanel.setBackground(LIGHTBACKGROUND);       

	}

	/**
	 * 
	 */
	public void inactive() {
		active = false;	
		this.localPanel.setBackground(Color.LIGHT_GRAY);       
		localPanel.setBorder(null);

	}

}
