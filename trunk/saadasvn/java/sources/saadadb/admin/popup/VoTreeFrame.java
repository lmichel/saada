package saadadb.admin.popup;


import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dnd.UCDTransferHandler;
import saadadb.admin.dnd.UnitTransferHandler;
import saadadb.admin.dnd.UtypeTransferHandler;
import saadadb.admin.tree.VoCharacDMTree;
import saadadb.admin.tree.VoSpectrumDMTree;
import saadadb.admin.tree.VoTree;
import saadadb.admin.tree.VoUCDTree;

public class VoTreeFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SaadaDBAdmin parent;
	private JComponent jtable;
	private JTabbedPane onglets;
	private static VoTreeFrame vofreeframe;
	
	
	public VoTreeFrame(SaadaDBAdmin parent, JComponent jtable) {
		super();
		this.parent = parent;
		this.jtable = jtable;
		Dimension dim = new Dimension(500, 500);
		onglets = new JTabbedPane();
		onglets.setPreferredSize(dim);
		
		VoTree votree = new VoUCDTree(this);
		votree.buildTree(dim);
		onglets.addTab("UCD", null, votree, "qqqqqqqA");

		votree = new VoCharacDMTree(this);
		votree.buildTree(dim);
		onglets.addTab("Charac DM", null, votree, "qqqqqqqA");
		
		votree = new VoSpectrumDMTree(this);
		votree.buildTree(dim);
		onglets.addTab("Spectrum DM", null, votree, "qqqqqqqA");

		votree = new saadadb.admin.tree.VoUnitTree(this);
		votree.buildTree(dim);
		onglets.addTab("Units", null, votree, "qqqqqqqA");
		onglets.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
		        JTabbedPane tab = (JTabbedPane)e.getSource();
		        switch(tab.getSelectedIndex()) {
		        /*
		         * first pane: UCDs
		         */
		        case 0: VoTreeFrame.this.jtable = VoTreeFrame.this.parent.getClass_table();
		        VoTreeFrame.this.jtable.setTransferHandler(new UCDTransferHandler());			
				break;
		        /*
		         * first pane: Units
		         */
		        case 3: VoTreeFrame.this.jtable = VoTreeFrame.this.parent.getClass_table();
		        VoTreeFrame.this.jtable.setTransferHandler(new UnitTransferHandler());			
				break;
		        /*
		         * other panes: Utypes
		         */
				default: VoTreeFrame.this.jtable = VoTreeFrame.this.parent.getClass_table();
				VoTreeFrame.this.jtable.setTransferHandler(new UtypeTransferHandler());			
				break;
				}
			}
			
		});
		/*
		 * Be sure that the fisrt tab (UCD) is selected and activate DnD
		 */
		onglets.setSelectedIndex(0);
		jtable.setTransferHandler(new UCDTransferHandler());			
		this.setLocationRelativeTo(this.parent);
		this.add(onglets);
		this.pack();
		this.setVisible(true);
	}
	
	public static  VoTreeFrame getInstance (SaadaDBAdmin parent, JTable jtable) {
		if( vofreeframe == null ) {
			vofreeframe = new VoTreeFrame(parent, jtable);
		}
		vofreeframe.setVisible(true);
		return vofreeframe;
	}
	
	public static void setTable(JTable jtable) {
		if( vofreeframe != null ) {
			vofreeframe.jtable = jtable;
			/*
			 * Be sure that the fisrt tab (UCD) is selected and activate DnD
			 */
			vofreeframe.onglets.setSelectedIndex(0);
			vofreeframe.jtable.setTransferHandler(new UCDTransferHandler());			
		}
		
	}
}
