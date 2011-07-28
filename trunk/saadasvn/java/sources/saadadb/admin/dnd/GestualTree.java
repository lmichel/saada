package saadadb.admin.dnd;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import saadadb.admin.SaadaDBAdmin;

public class GestualTree extends JTree implements DragGestureListener {

	public GestualTree(DefaultMutableTreeNode top) {
		super(top);
	}


	public void dragGestureRecognized(DragGestureEvent dge) {
		SaadaDBAdmin.showSuccess(this, "coucou");
	}

}
