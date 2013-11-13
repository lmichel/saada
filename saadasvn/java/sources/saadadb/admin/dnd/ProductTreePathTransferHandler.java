package saadadb.admin.dnd;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import saadadb.util.Messenger;


public class ProductTreePathTransferHandler extends TransferHandler {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean check_extension;
	int node_to_copy;
	
	public ProductTreePathTransferHandler(int node_to_copy) {
		this.check_extension = check_extension;
		this.node_to_copy = node_to_copy;
	}

	
    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent c, Transferable trsf) {
        	try {
       		DataFlavor[] df = trsf.getTransferDataFlavors();
       		for(int i=0 ; i<df.length ; i++ ) {
       			if( df[i].getRepresentationClass().getName().equals("java.lang.String") ) {
       				TreepathDropableTextField tf = (TreepathDropableTextField) c;
       				tf.setText((String) trsf.getTransferData(new DataFlavor(Class.forName("java.lang.String"), "")));       		       				
       				return true;
       			}
       			else if( df[i].getRepresentationClass().getName().equals("javax.swing.tree.TreePath") ) {
       	      		TreePath treepath = (TreePath) trsf.getTransferData(new DataFlavor(Class.forName("javax.swing.tree.TreePath"), ""));
       	       		TreepathDropableTextField tf = (TreepathDropableTextField) c;
       	       		return tf.setText(treepath); 				
       			}
       		}
       	} catch(Exception e) {Messenger.printStackTrace(e);}
       	return false;
    	}
    
    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      	for( int i=0 ; i<transferFlavors.length ; i++) {
    		if( "javax.swing.tree.TreePath".equals(transferFlavors[i].getRepresentationClass().getName()) ) {
    			return true;
    		}
    	}
    	return false;
    }


}