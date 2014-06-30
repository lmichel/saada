package saadadb.admintool.components;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.AdminTool;
import saadadb.admintool.VPSandbox.VPSTOEPanel;
import saadadb.admintool.panels.AdminPanel;
import saadadb.util.Messenger;

public class SwitchButtonWIP extends JButton {
	
	private static final long serialVersionUID = 1L;
	private AdminPanel adminPanel = null;
	
	public SwitchButtonWIP(final AdminTool rootFrame) {
		//super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/swap.png")));
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Rename.png")));

		//this.adminPanel =adminPanel;
		this.setPreferredSize(new Dimension(60, 40));
		this.setToolTipText("(dev) Swap interface");
		this.setEnabled(false);
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//if( SwitchButtonWIP.this.adminPanel != null ) {
					try {
						//SwitchButtonWIP.this.adminPanel.rename ();
						//new MappingFilterPanel(rootFrame, AdminComponent.NEW_MAPPER, null, AdminComponent.DATA_LOADER);
						new Runnable(){public void run(){
							rootFrame.activePanel(AdminComponent.NEW_MAPPER);}};
					} catch (Exception e1) {
						Messenger.printStackTrace(e1);
						AdminComponent.showFatalError(SwitchButtonWIP.this.adminPanel.rootFrame, e1);
					}
				}
		//	}
		});
	}

}
