package saadadb.admintool.dnd;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class VoDataProductTreeDragSource implements DragGestureListener, DragSourceListener {
	private JTree tree;
	  public VoDataProductTreeDragSource(JTree tree) {
	    this.tree = tree;

	    // Use the default DragSource
	    DragSource dragSource = DragSource.getDefaultDragSource();

	    // Create a DragGestureRecognizer and
	    // register as the listener
	    dragSource.createDefaultDragGestureRecognizer(tree,
	        DnDConstants.ACTION_COPY_OR_MOVE, this);
	  }

	  // Implementation of DragGestureListener interface.
	  public void dragGestureRecognized(DragGestureEvent dge) {
		  // Get the mouse location and convert it to
	    // a location within the tree.
	    Point location = dge.getDragOrigin();
	    TreePath dragPath = tree.getPathForLocation(location.x, location.y);
	    if (dragPath != null && tree.isPathSelected(dragPath)) {
	      // Get the list of selected files and create a Transferable
	      // The list of files and the is saved for use when
	      // the drop completes.
//	      paths = tree.getSelectionPaths();
//	      if (paths != null && paths.length > 0) {
//	        dragFiles = new File[paths.length];
//	        for (int i = 0; i < paths.length; i++) {
//	          String pathName = tree.getPathName(paths[i]);
//	          dragFiles[i] = new File(pathName);
//	        }
//
//	        Transferable transferable = new FileListTransferable(dragFiles);
//	        dge.startDrag(null, transferable, this);
//	      }
	    }
	  }

	public void dragEnter(DragSourceDragEvent dsde) {
		
	}

	public void dragOver(DragSourceDragEvent dsde) {
		
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
		
	}

	public void dragExit(DragSourceEvent dse) {
		
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		
	}



}
