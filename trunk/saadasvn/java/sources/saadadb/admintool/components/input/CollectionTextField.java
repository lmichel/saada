package saadadb.admintool.components.input;


import javax.swing.tree.TreePath;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.dnd.TreepathDropableTextField;
import saadadb.database.Database;

/**
 * @author michel
 * * @version $Id: CollectionTextField.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class CollectionTextField extends TreepathDropableTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected TreePath treepath;
	
	/**
	 * @param form
	 */
	public CollectionTextField() {
		super(24);
		/*
		 * Takes the second node, without extension checking
		 */
		this.setTransferHandler(new ProductTreePathTransferHandler(3));		
	}
	
	/**
	 * @param treepath
	 * @param num_node
	 */
	public boolean setText(TreePath treepath) {
		this.treepath = treepath;
		if( valid() && this.isEditable() ) {
			this.setText(this.treepath.getPathComponent(1) + "." + this.treepath.getPathComponent(2));
			return true;
		}	
		else {
			return false;
		}
	}

	/**
	 * @return
	 */
	protected boolean valid() {
		if( this.treepath.getPathCount() < 3 ) {
			AdminComponent.showFatalError(this.getParent(), "A collection + a category must be pasted");		
			return false;
		}
		else if( !Database.getCachemeta().collectionExists(treepath.getPathComponent(1).toString()) ) {
			AdminComponent.showFatalError(this.getParent(), "Collection <" + treepath.getPathComponent(1).toString() +"> does not exist");		
			return false;			
		}
		else {
			return true;
		}
	}

}
