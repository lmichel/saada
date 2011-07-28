package saadadb.admin.popup;

import java.awt.Frame;

import javax.swing.JMenuItem;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

public class PopupCatNode extends PopupNode {
	
	private static final long serialVersionUID = 1L;
	
	public PopupCatNode(Frame frame, Object[] tree_path_components, String title) {
		super(frame, tree_path_components, title);
		
		JMenuItem item ;
		
		item = new JMenuItem(SHOW_CLASS);
		item.addActionListener(this);
		this.add(item);
		
		item = new JMenuItem(SHOW_CONTENT);
		item.addActionListener(this);
		this.add(item);
		
		this.addSeparator();
		
		item = new JMenuItem(LOAD_DATA_NEW_CONF);
		/*
		 * Loading data makes no sens for entries
		 */
		if( tree_path_components[2].toString().equals("ENTRY") ) {
			item.setText(item.getText() + " (use TABLE category)");
			item.setEnabled(false);
		}
		this.add(item);
		item.addActionListener(this);
		
		item = new JMenuItem(EMPTY_CATEGORY);
		if( tree_path_components[2].toString().equals("ENTRY") ) {
			item.setText(item.getText() + " (use TABLE category)");
			item.setEnabled(false);
		}
		this.add(item);
		item.addActionListener(this);
		
		this.addSeparator();
		boolean rel_found = false;
		try {
			for( String rel: Database.getCachemeta().getRelationNamesStartingFromColl(tree_path_components[1].toString()
					, Category.getCategory(tree_path_components[2].toString()))) {					
				item = new JMenuItem(STARTING_RELATIONSHIP + ": " + rel);
				item.addActionListener(this);
				this.add(item);
				rel_found = true;
			}
			for( String rel: Database.getCachemeta().getRelationNamesEndingOnColl(tree_path_components[1].toString()
					, Category.getCategory(tree_path_components[2].toString()))) {					
				item = new JMenuItem(ENDING_RELATIONSHIP + ": " + rel);
				item.addActionListener(this);
				rel_found = true;
				this.add(item);
			}
		} catch (FatalException e) {
			}
		if( !rel_found ) {
			item = new JMenuItem("No Relationship");
			item.setEnabled(false);
			this.add(item);
			
		}
		this.addSeparator();
		item = new JMenuItem(BUILD_INDEX);
		item.addActionListener(this);
		this.add(item);
	}
}
