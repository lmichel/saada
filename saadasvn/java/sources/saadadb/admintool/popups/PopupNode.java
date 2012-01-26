package saadadb.admintool.popups;


import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import saadadb.admin.SQLJTable;
import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.SQLIndexPanel;
import saadadb.admin.dialogs.SelectClass;
import saadadb.admin.dmmapper.ClassToDatamodelMapper;
import saadadb.admin.dmmapper.DMBuilder;
import saadadb.admin.dmmapper.MapperDemo;
import saadadb.admin.threads.CmdCommentCollection;
import saadadb.admin.threads.CmdCreateCollection;
import saadadb.admin.threads.CmdDeleteClass;
import saadadb.admin.threads.CmdDeleteCollection;
import saadadb.admin.threads.CmdDeleteProduct;
import saadadb.admin.threads.CmdEmptyCategory;
import saadadb.admin.threads.CmdEmptyCollection;
import saadadb.admin.threads.CmdLoadData;
import saadadb.admin.threads.CmdSaveClass;
import saadadb.admin.threads.CmdThread;
import saadadb.admin.threads.DummyTask;
import saadadb.admin.threads.RelationShow;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.util.Messenger;

public class PopupNode extends JPopupMenu implements ActionListener{
	/** * @version $Id: PopupNode.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String CREATE_COLLECTION = "Create Collection";
	public static final String COMMENT_COLLECTION  = "Edit Description";
	public static final String EMPTY_COLLECTION  = "Clear Collection";
	public static final String DELETE_COLLECTION = "Remove Collection";

	public static final String EMPTY_CATEGORY  = "Clear Content";

	public static final String EMPTY_CLASS  = "Clear Class";
	public static final String DELETE_CLASS = "Remove Class";
	public static final String SHOW_CLASS = "Show Metadata";

	public static final String LOAD_DATA = "Load Data in Default Mode (no config to be set)";
	public static final String LOAD_DATA_CLASS = "Load  Data in Default Mode in This Class";
	public static final String LOAD_DATA_NEW_CONF = "Load Data";

	public static final String SHOW_CONTENT = "Show Data";

	public static final String CREATE_RELATIONSHIP = "Create a Saada Relationship";
	public static final String REMOVE_RELATIONSHIP = "Remove a Saada Relationship";
	public static final String STARTING_RELATIONSHIP = "Show Starting Relationship";
	public static final String ENDING_RELATIONSHIP   = "Show Coming Relationship";

	public static final String 	DELETE_PRODUCTS	= "Delete Selected Products";

	public static final String 	EDIT_UCD	= "Edit UCD of selected attribute";
	public static final String 	EDIT_UTYPE	= "Edit UTYPE of selected attribute";

	public static final String 	MAP_META	   = "Tag Metadata (UCDs, Utypes and units)";
	public static final String  SAVE_MAPPING   = "Save Metadata";
	public static final String  CANCEL_MAPPING = "Cancel Metadata Tags";

	public static final String BUILD_INDEX = "Manage Indexes";

	public static final String MAPP_DM = "Map Data Model";
	public static final String EDIT_DM = "Edit a Data Model View";

	public static final String DUMMY  = "Dummy Task";
	protected Frame frame;
	/*
	 * Selected tree path from which the menu has been opened.
	 */
	protected Object[] tree_path_components;
	/*
	 * JTable frm which the menu has been opened
	 */
	protected JTable jtable;
	/**
	 * @param frame
	 * @param title
	 */
	public PopupNode(Frame frame, Object[] tree_path_components, String title) {
		super();
		this.frame = frame;
		this.tree_path_components = tree_path_components;
		this.add(title);
		this.addSeparator();
	}

	/**
	 * Used when the popup is open from a JTable (product or class)
	 * @param frame
	 * @param tree_path_components
	 * @param jtable
	 * @param title
	 */
	public PopupNode(Frame frame, Object[] tree_path_components, JTable jtable, String title) {
		super();
		this.frame = frame;
		this.jtable = jtable;
		this.tree_path_components = tree_path_components;
		this.add(title);
		this.addSeparator();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String item = ((AbstractButton)(e.getSource())).getText();
		CmdThread ct=null;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "User command: " + item);
		if( item.equals(CREATE_COLLECTION) ) {
			ct = new CmdCreateCollection(frame);
		}
		else if( item.equals(COMMENT_COLLECTION) ) {
			ct = new CmdCommentCollection(frame, tree_path_components);
		}
		else if( item.equals(EMPTY_COLLECTION) ) {
			ct = new CmdEmptyCollection(frame, tree_path_components);
		}
		else if( item.equals(DELETE_COLLECTION) ) {
			ct = new CmdDeleteCollection(frame, tree_path_components);
		}
		else if( item.equals(EMPTY_CATEGORY) ) {
			ct = new CmdEmptyCategory(frame, tree_path_components);
		}
		else if( item.equals(LOAD_DATA) || item.equals(LOAD_DATA_CLASS) ) {
			ct = new CmdLoadData(frame, tree_path_components);
		}
		else if( item.equals(DELETE_CLASS) ) {
			ct = new CmdDeleteClass(frame, tree_path_components);
		}
		else if( item.equals(LOAD_DATA_NEW_CONF) ) {
			ct = new CmdLoadData(frame, tree_path_components, null);
		}
		else if( item.equals(SHOW_CONTENT) ) {
			try {
				((SaadaDBAdmin)(frame)).showProduct(tree_path_components);
			} catch (QueryException e1) {
				Messenger.trapFatalException(e1);
			}
		}
		else if( item.equals(SHOW_CLASS) ) {
			try {
				if( frame instanceof SaadaDBAdmin)
					((SaadaDBAdmin)(frame)).showClass(tree_path_components);
				else if( frame instanceof MapperDemo)
					((MapperDemo)(frame)).showClass(tree_path_components);
			} catch (QueryException e1) {
				Messenger.trapFatalException(e1);
			} 
		}
		else if( item.equals(DELETE_PRODUCTS) ) {
			ct = new CmdDeleteProduct(frame, tree_path_components, jtable);
		}
		else if( item.equals(MAP_META) ) {	
			VoTreeFrame.getInstance((SaadaDBAdmin)frame, jtable);
		}
		else if( item.equals(CANCEL_MAPPING) ) {	
			try {
				((SaadaDBAdmin)(frame)).showClass(tree_path_components);
			} catch (QueryException e1) {
				Messenger.trapFatalException(e1);
			}
		}
		else if( item.equals(SAVE_MAPPING) ) {	
			ct = new CmdSaveClass(frame, tree_path_components, (SQLJTable)jtable);
			try {
				((SaadaDBAdmin)(frame)).showClass(tree_path_components);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if( item.equals(BUILD_INDEX) ) {	
			try {
				new SQLIndexPanel(tree_path_components);
			} catch (FatalException e1) {
				Messenger.trapFatalException(e1);
			}
		}
		else if( item.equals(DUMMY) ) {	
			ct = new DummyTask(frame);
		}
		else if( item.startsWith(STARTING_RELATIONSHIP)  || item.startsWith(ENDING_RELATIONSHIP)) {
			String classe = null;
			if( tree_path_components.length == 4) {
				classe = tree_path_components[3].toString();
			}
			ct = new RelationShow(frame, item.substring(item.lastIndexOf(":")+1).trim(), classe);
		}

		else if( item.equals(MAPP_DM)) {

			SelectClass sc;
			try {
				sc = new SelectClass(this.frame, Database.getCachemeta().getVOResourceNames());
				String c = sc.getTyped_name();		
				if( c!= null ) {
					(new ClassToDatamodelMapper(c, tree_path_components[1].toString(), Category.getCategory(tree_path_components[2].toString()))).selectClass(tree_path_components[3].toString());
				}

			} catch (Exception e1) {
				Messenger.printStackTrace(e1);
			}

		}
		else if( item.equals(EDIT_DM)) {
			try {
				new DMBuilder();
			} catch (Exception e1) {
				Messenger.printStackTrace(e1);
			}
		}
		//		else if( item.equals(CREATE_RELATIONSHIP) ) {
		//			ct = new CmdCreateRelation(frame, tree_path_components);
		//		}
		//		else if( item.equals(REMOVE_RELATIONSHIP) ) {
		//			ct = new CmdDeleteRelation(frame, tree_path_components);
		//		}
		if( ct != null ) {
			/*
			 * Thread die when main console closed
			 */
			ct.setDaemon(true);
			ct.start();
			ct.setPriority(Thread.MIN_PRIORITY);
		}
	}
}
