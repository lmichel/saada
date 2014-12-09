package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoadBarButton extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static ImageIcon loadingIcon = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/loadingBar.gif"));
	private static ImageIcon OKIcon = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/OKBar.png"));
	public JButton btn;
	private JLabel statelbl;
	private boolean isLoading;

	public LoadBarButton(String t) 
	{
		super(new GridBagLayout());
		this.isLoading = false;
		GridBagConstraints mc = new GridBagConstraints();
		mc.gridheight = 1; mc.gridwidth = 1; mc.fill = GridBagConstraints.HORIZONTAL; mc.gridy = 0; 
		mc.weightx = 1; mc.gridx = 0;
		btn = new JButton(t);
		this.add(btn, mc);
		mc.weightx = 0; mc.gridx = 1;
		statelbl = new JLabel();
		this.setActiveProcess(false);
		this.add(statelbl, mc);
		
		statelbl.setPreferredSize(new Dimension(35, btn.getPreferredSize().height));
		statelbl.setBorder(BorderFactory.createLineBorder(new Color(122,138,153)));
		statelbl.setHorizontalTextPosition(JButton.CENTER);
		statelbl.setFocusable(false);
	}
	
	public void setActiveProcess(boolean isActiveProcess)
	{
		this.isLoading = isActiveProcess;
		if (this.isLoading)
		{
			statelbl.setIcon(loadingIcon);
			statelbl.setToolTipText("Working on selected action");
		}
		else
		{
			statelbl.setIcon(OKIcon);
			statelbl.setToolTipText("Ready to run a new action");
		}
	}
}


