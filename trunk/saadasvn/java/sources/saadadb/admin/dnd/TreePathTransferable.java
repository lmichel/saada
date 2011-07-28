package saadadb.admin.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.tree.TreePath;

public class TreePathTransferable implements Transferable {

	public static DataFlavor XNODE_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");
	public DataFlavor[] flavors = {XNODE_FLAVOR};
	public TreePath treepath = null;
	
	public TreePathTransferable(TreePath path){
		treepath = path;
	}

	public synchronized DataFlavor[] getTransferDataFlavors(){
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor){
		return flavor.getRepresentationClass() == TreePath.class;
	}
	public synchronized Object getTransferData(DataFlavor flavor)throws UnsupportedFlavorException{
		if(isDataFlavorSupported(flavor)){
			return (Object)treepath;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}


}
