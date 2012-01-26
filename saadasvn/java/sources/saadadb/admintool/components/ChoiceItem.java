package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.utils.RGBGrayFilter;

/**
 * @author laurent
 * @version $Id$
 *
 */
public class ChoiceItem extends AdminComponent {
	private String label;
	private JPanel choicePanel;
	private JPanel localPanel;
	private Runnable runnable;
	private Icon activeIcon;
	private Icon inactiveIcon;
	private JLabel iconLabel;
	private JLabel textLabel;
	private boolean active;

	public ChoiceItem(AdminTool rootFrame,  JPanel choicePanel, GridBagConstraints gbc, String label, String activeImage, Runnable runnable) {
		super(rootFrame);
		this.label = label;
		this.choicePanel = choicePanel;
		this.runnable = runnable;
		if( activeImage != null ) this.activeIcon = new ImageIcon(ClassLoader.getSystemClassLoader().getResource(activeImage));
		iconLabel = new JLabel(this.activeIcon);
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		iconLabel.setBorder(new EmptyBorder(10, 10, 1, 10));
		this.localPanel.add(iconLabel);
		makeIncativeIcon();

		textLabel = new JLabel(this.label);
		textLabel.setFont(plainFont);		
		textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		textLabel.setBorder(new EmptyBorder(1, 10, 5, 10));
		this.localPanel.add(textLabel);
		
		choicePanel.add(this.localPanel, gbc);
		this.localPanel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {	
				if( active ) {
					if( ChoiceItem.this.localPanel.getBorder() != null )   
						ChoiceItem.this.runnable.run();
					localPanel.setBorder(null);
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

	
	private void makeIncativeIcon() {
		inactiveIcon = RGBGrayFilter.getDisabledIcon(iconLabel, this.activeIcon);
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
		iconLabel.setIcon(activeIcon);
		textLabel.setForeground(Color.BLACK);
	}

	/**
	 * 
	 */
	public void inactive() {
		active = false;	
		localPanel.setBorder(null);
		iconLabel.setIcon(inactiveIcon);
		textLabel.setForeground(Color.LIGHT_GRAY);

	}

}
