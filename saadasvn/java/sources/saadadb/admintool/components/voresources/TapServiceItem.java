package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;


public class TapServiceItem extends JPanel {
	DataTreePath dataTreePath;
	
	protected TapServiceItem(DataTreePath dataTreePath) {
		this.dataTreePath = dataTreePath;
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(new JCheckBox("aaaa"));
		this.add(new JLabel(dataTreePath.toString()));
		this.setBackground(Color.RED);
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		//this.setSize(new Dimension(100, 100));
		System.out.println(this.getSize());

	}
}
