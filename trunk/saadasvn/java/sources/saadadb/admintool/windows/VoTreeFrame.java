package saadadb.admintool.windows;


import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import saadadb.admintool.AdminTool;
import saadadb.admintool.dnd.UCDTransferHandler;
import saadadb.admintool.dnd.UnitTransferHandler;
import saadadb.admintool.dnd.UtypeTransferHandler;
import saadadb.admintool.tree.VoCharacDMTree;
import saadadb.admintool.tree.VoSpectrumDMTree;
import saadadb.admintool.tree.VoTree;
import saadadb.admintool.tree.VoUCDTree;
import saadadb.admintool.tree.VoUnitTree;

public class VoTreeFrame extends OuterWindow {
	private static final long serialVersionUID = 1L;
	private JComponent jtable;
	private JTabbedPane onglets;
	private static VoTreeFrame vofreeframe;
	
	
	public VoTreeFrame(AdminTool rootFrame, JComponent jtable) {
		super(rootFrame);
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

		votree = new VoUnitTree(this);
		votree.buildTree(dim);
		onglets.addTab("Units", null, votree, "qqqqqqqA");
		onglets.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
		        JTabbedPane tab = (JTabbedPane)e.getSource();
		        switch(tab.getSelectedIndex()) {
		        /*
		         * first pane: UCDs
		         */
		        case 0: 
		        VoTreeFrame.this.jtable.setTransferHandler(new UCDTransferHandler());			
				break;
		        /*
		         * first pane: Units
		         */
		        case 3: 
		        VoTreeFrame.this.jtable.setTransferHandler(new UnitTransferHandler());			
				break;
		        /*
		         * other panes: Utypes
		         */
				default: 
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
		this.add(onglets);
		this.pack();
	}
	
	public static  VoTreeFrame getInstance (AdminTool rootFrame, JTable jtable) {
		if( vofreeframe == null ) {
			vofreeframe = new VoTreeFrame(rootFrame, jtable);
		}
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

	@Override
	protected void setContent() throws Exception {
		if( vofreeframe != null ) {
			vofreeframe.jtable = jtable;
			/*
			 * Be sure that the first tab (UCD) is selected and activate DnD
			 */
			vofreeframe.onglets.setSelectedIndex(0);
			vofreeframe.jtable.setTransferHandler(new UCDTransferHandler());			
		}
	}
}
