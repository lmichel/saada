package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;
import saadadb.exceptions.FatalException;


public class TapServiceItem extends JPanel {
	protected DataTreePath dataTreePath;
	
	protected TapServiceItem(DataTreePath dataTreePath) {
		this.dataTreePath = dataTreePath;
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(new JCheckBox("aaaa"));
		this.add(new JLabel(dataTreePath.toString()));
		this.setBackground(Color.RED);
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent arg0) {
				System.out.println(arg0.getSource());
				
			}
			
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});

	}
	
	public boolean fireAddResource(DataTreePath dataTreePath) throws FatalException {
		return ((TapServiceList)(this.getParent())).addResource(dataTreePath, this);
	}
}
