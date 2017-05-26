package saadadb.admintool.windows;


import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import saadadb.admintool.AdminTool;
import saadadb.admintool.dnd.SaadaTransferHandler;
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
	private ArrayList<JComponent> jtable = new ArrayList<JComponent>();
	private JTabbedPane onglets;
	private static VoTreeFrame vofreeframe;
	
	
	private VoTreeFrame(AdminTool rootFrame, JComponent receiver) {
		super(rootFrame);
		this.addReceiver(receiver);
		this.build();
	}
	private VoTreeFrame(AdminTool rootFrame, JComponent[] receivers) {
		super(rootFrame);
		this.addReceiver(receivers);
		this.build();
	}
	
	private void build() {
		Dimension dim = new Dimension(500, 500);
		onglets = new JTabbedPane();
		onglets.setPreferredSize(dim);
		
		VoTree votree = new VoUCDTree(this);
		votree.drawTree(dim);
		onglets.addTab("UCD", null, votree, "qqqqqqqA");

		votree = new VoCharacDMTree(this);
		votree.drawTree(dim);
		onglets.addTab("Charac DM", null, votree, "qqqqqqqA");
		
		votree = new VoSpectrumDMTree(this);
		votree.drawTree(dim);
		onglets.addTab("Spectrum DM", null, votree, "qqqqqqqA");

		votree = new VoUnitTree(this);
		votree.drawTree(dim);
		onglets.addTab("Units", null, votree, "qqqqqqqA");
		onglets.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
		        JTabbedPane tab = (JTabbedPane)e.getSource();
		        switch(tab.getSelectedIndex()) {
		        /*
		         * first pane: UCDs
		         */
		        case 0: 
		        VoTreeFrame.this.setTransfertHandlerForReceivers(new UCDTransferHandler());			
				break;
		        /*
		         * first pane: UnitsChAxis.AxisName  (Axis name )
		         */
		        case 3:
		        VoTreeFrame.this.setTransfertHandlerForReceivers(new UnitTransferHandler());			
				break;
		        /*
		         * other panes: Utypes
		         */
				default: 
				VoTreeFrame.this.setTransfertHandlerForReceivers(new UtypeTransferHandler());			
				break;
				}
			}
			
		});
		/*
		 * Be sure that the fisrt tab (UCD) is selected and activate DnD
		 */
		onglets.setSelectedIndex(0);
		this.setTransfertHandlerForReceivers(new UCDTransferHandler());			
		this.add(onglets);
		this.pack();
	}
	/**
	 * Return the singleton instance
	 * @param rootFrame
	 * @param jtable
	 * @return
	 */
	public static  VoTreeFrame getInstance (AdminTool rootFrame, JComponent receiver) {
		if( vofreeframe == null ) {
			vofreeframe = new VoTreeFrame(rootFrame, receiver);
		}
		return vofreeframe;
	}
	public static  VoTreeFrame getInstance (AdminTool rootFrame, JComponent[] receivers) {
		if( vofreeframe == null ) {
			vofreeframe = new VoTreeFrame(rootFrame, receivers);
		}
		return vofreeframe;
	}
	
	/**
	 * @param receiver
	 */
	public void addReceiver(JComponent receiver){
		if( receiver != null) {
			this.jtable.add(receiver);
		}
	}
	/**
	 * @param receiver
	 */
	public void addReceiver(JComponent[] receivers){
		if( receivers != null) {
			for( JComponent receiver: receivers)
			this.addReceiver(receiver);
		}
	}
	
	public  void setTransfertHandlerForReceivers(SaadaTransferHandler handler){
		for( JComponent comp: this.jtable ) {
			comp.setTransferHandler(handler);
		}
	}
	public static void setTable(JTable jtable) {
		if( vofreeframe != null ) {
			vofreeframe.addReceiver(jtable);
			/*
			 * Be sure that the fisrt tab (UCD) is selected and activate DnD
			 */
			vofreeframe.onglets.setSelectedIndex(0);
			vofreeframe.setTransferHandler(new UCDTransferHandler());			
		}
	}

	@Override
	protected void setContent(int type) throws Exception {
		if( vofreeframe != null ) {
			/*
			 * Be sure that the first tab (UCD) is selected and activate DnD
			 */
			vofreeframe.onglets.setSelectedIndex(0);
			vofreeframe.setTransferHandler(new UCDTransferHandler());			
		}
	}
}
