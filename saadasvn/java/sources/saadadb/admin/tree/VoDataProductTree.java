package saadadb.admin.tree;


import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dnd.TreePathTransferable;
import saadadb.meta.AttributeHandler;
import saadadb.products.FitsProduct;
import saadadb.products.ProductFile;
import saadadb.products.VOProduct;
import saadadb.util.RegExp;

public class VoDataProductTree extends VoTree implements DragGestureListener,  DragSourceListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** * @version $Id$

	 * @param frame
	 * @param top_node
	 * @param filename
	 */
	public VoDataProductTree(Window frame, String top_node, String filename) {
		super(frame, top_node);
		try {
			buildTree(filename);
		} catch (Exception e) {
			flat_types = null;
			SaadaDBAdmin.showFatalError(frame, e.toString());
			return;
		}
	}

	public VoDataProductTree(Window frame, String top_node) {
		super(frame, top_node);
	}
	/**
	 * @param filename
	 * @throws Exception 
	 */
	private void buildTree(String filename) throws Exception {
		ArrayList<String> flat_array = new ArrayList<String>();
		ProductFile prd = null;
		if( filename.matches(RegExp.FITS_FILE) ) {
			prd = new FitsProduct(filename, null);
		}
		else if( filename.matches(RegExp.VOTABLE_FILE) ) {
			prd = new VOProduct(filename);				
		}
		else {
			SaadaDBAdmin.showFatalError(frame, "Type of file <" + filename + "> not recognized");
			return;
		}
		Map<String, ArrayList<AttributeHandler>> prd_map=null;
		prd_map = prd.getMap(null);
		for( String ext: prd_map.keySet()) {
			ArrayList<AttributeHandler> alah = prd_map.get(ext);
			for( AttributeHandler ah: alah ) {
				boolean set = false;
				String leaf;
				leaf = ah.getType();
				if( leaf != null && leaf.length()!= 0 ) {
					set = true;
					flat_array.add(ext + "<>" + ah.getNameorg() + "<>Type:" +leaf);						
				}
				leaf = ah.getUnit();
				if( leaf != null && leaf.length()!= 0 ) {
					set = true;
					flat_array.add(ext + "<>" + ah.getNameorg() + "<>Unit:" +leaf);						
				}
				leaf = ah.getComment();
				if( leaf != null && leaf.length()!= 0 ) {
					set = true;
					flat_array.add(ext + "<>" + ah.getNameorg() + "<>Comment:\"" +leaf+ "\"");						
				}
				leaf = ah.getUcd();
				if( leaf != null && leaf.length()!= 0 ) {
					set = true;
					flat_array.add(ext + "<>" + ah.getNameorg() + "<>UCD:" +leaf);						
				}
				leaf = ah.getUtype();
				if( leaf != null && leaf.length()!= 0 ) {
					set = true;
					flat_array.add(ext + "<>" + ah.getNameorg() + "<>Utype:" +leaf);						
				}
				leaf = ah.getValue();
				if( leaf != null && leaf.length()!= 0 ) {
					set = true;
					flat_array.add(ext + "<>" + ah.getNameorg() + "<>Value:" +leaf);						
				}
				if( ! set ) {
					flat_array.add(ext + "<>" + ah.getNameorg());												
				}
			}
		}
		flat_types = flat_array.toArray(new String[0]);
	}

	/*
	 * Drag gesture must be redifined in subclasses
	 * (non-Javadoc)
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		System.out.println("ADMIN dragGestureRecognized" );
		Point location = dge.getDragOrigin();
		TreePath dragPath = tree.getPathForLocation(location.x, location.y);            
		if (dragPath != null && tree.isPathSelected(dragPath)) {
			Transferable transferable = new TreePathTransferable(dragPath);
			dge.startDrag(DragSource.DefaultMoveDrop, transferable, this);
		}
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	protected void setDragFeatures() {
		System.out.println("setDragFeatures ADMIN");
		DragSource ds = new DragSource();
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY, this);

	}
	/* (non-Javadoc)
	 * @see gui.VOTree#getPathComponents(java.lang.String)
	 */
	protected String[] getPathComponents(String string) {
		String[] utype_tokens = string.split("<>");
		//utype_tokens[utype_tokens.length - 1] = string;
		return utype_tokens;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();		
		JTabbedPane onglets = new JTabbedPane();
		VoDataProductTree vot;
		vot = new VoDataProductTree(frame
				, "ext/keywords"
				, "/Users/laurentmichel/Desktop/DossierBoston/xmmsl1D_clean.fits.gz");
		vot.buildTree(new Dimension(300, 500));
		vot.setPreferredSize(new Dimension(300, 500));
		frame.add(vot);
		frame.pack();
		frame.setVisible(true);
	}


}
