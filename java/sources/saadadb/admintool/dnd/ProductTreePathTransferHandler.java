package saadadb.admintool.dnd;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import saadadb.admintool.components.voresources.VOServiceList;
import saadadb.admintool.components.voresources.VOServiceListItem;
import saadadb.admintool.utils.DataTreePath;
import saadadb.util.Messenger;


/**
 * Handle DnD transfert of a data tree path specific target components
 * @author michel
 * @version $Id$
 *
 */
public class ProductTreePathTransferHandler extends TransferHandler {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean check_extension;
	int index;



	public ProductTreePathTransferHandler(int index) {
		// TODO Auto-generated constructor stub
		this.index = index;
	}


	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
	 */
	public boolean importData(JComponent targetComponent, Transferable transferable) {
		try {
			DataFlavor[] df = transferable.getTransferDataFlavors();
			for(int i=0 ; i<df.length ; i++ ) {
				if( df[i].getRepresentationClass().getName().equals("java.lang.String") ) {
					TreepathDropableTextField tf = (TreepathDropableTextField) targetComponent;
					tf.setText((String) transferable.getTransferData(new DataFlavor(Class.forName("java.lang.String"), "")));       		       				
					return true;
				} else if( df[i].getRepresentationClass().getName().equals("javax.swing.tree.TreePath") ) {
					TreePath treepath = (TreePath) transferable.getTransferData(new DataFlavor(Class.forName("javax.swing.tree.TreePath"), ""));
					if( targetComponent instanceof TreepathDropableTextField) {
						return ((TreepathDropableTextField) targetComponent).setText(treepath); 
					} else if( targetComponent instanceof VOServiceList) {
						return ((VOServiceList)targetComponent).addResource(new DataTreePath(treepath));
					} else if( targetComponent instanceof VOServiceListItem) {
						return ((VOServiceListItem)targetComponent).fireAddResource(new DataTreePath(treepath));
					}
				}
			}
		} catch(Exception e) {Messenger.printStackTrace(e);}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
	 */
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for (DataFlavor transferFlavor : transferFlavors) {
			if( "javax.swing.tree.TreePath".equals(transferFlavor.getRepresentationClass().getName()) ) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}


}
