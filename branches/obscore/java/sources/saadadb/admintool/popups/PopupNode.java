package saadadb.admintool.popups;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadDeleteProduct;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.DataTreePath;
import saadadb.exceptions.SaadaException;

public class PopupNode extends JPopupMenu implements ActionListener{
	/** * @version $Id$

	 * 
	 */
	public static final String 	DELETE_PRODUCTS	= "Delete Selected Products";

	public static final String 	EDIT_UCD	= "Edit UCD of selected attribute";
	public static final String 	EDIT_UTYPE	= "Edit UTYPE of selected attribute";

	public static final String 	MAP_META	   = "Tag Metadata (UCDs, Utypes and units)";
	public static final String  SAVE_MAPPING   = "Save Metadata";
	public static final String  CANCEL_MAPPING = "Cancel Metadata Tags";


	public static final String MAPP_DM = "Map Data Model";
	public static final String EDIT_DM = "Edit a Data Model View";

	protected AdminTool rootFrame;
	/*
	 * JTable frm which the menu has been opened
	 */
	protected JTable jtable;
	protected DataTreePath dataTreePath;
	protected Map<String, Object> params;
	/**
	 * @param frame
	 * @param title
	 */
	public PopupNode(AdminTool rootFrame, DataTreePath dataTreePath, String title) {
		super();
		this.rootFrame = rootFrame;
		this.dataTreePath = dataTreePath;
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
	public PopupNode(AdminTool rootFrame, DataTreePath dataTreePath, JTable jtable, String title) {
		super();
		this.rootFrame = rootFrame;
		this.jtable = jtable;
		this.dataTreePath = dataTreePath;
		this.add(title);
		this.addSeparator();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String item = ((AbstractButton)(e.getSource())).getText();

		if( item.equals(DELETE_PRODUCTS) ) {
			try {
				ThreadDeleteProduct cdp = new ThreadDeleteProduct(rootFrame, DELETE_PRODUCTS);
				cdp.setParams(params);
				if (cdp.checkParams(true) ) {
					rootFrame.activeProcessPanel(cdp);
				}
			} catch (SaadaException e1) {
				AdminComponent.showFatalError(rootFrame, e1);
			}
		}
		else if( item.equals(MAP_META) ) {	
			try {
				ThreadDeleteProduct cdp = new ThreadDeleteProduct(rootFrame, DELETE_PRODUCTS);
				cdp.setParams(params);
				rootFrame.activeWindowProcess(cdp, dataTreePath);
			} catch (SaadaException e1) {
				AdminComponent.showFatalError(rootFrame, e1);
			}
		}
		else if( item.equals(CANCEL_MAPPING) ) {	
			//			try {
			//				((SaadaDBAdmin)(frame)).showClass(tree_path_components);
			//			} catch (QueryException e1) {
			//				Messenger.trapFatalException(e1);
			//			}
		}
		else if( item.equals(SAVE_MAPPING) ) {	
			//			ct = new CmdSaveClass(frame, tree_path_components, (SQLJTable)jtable);
			//			try {
			//				((SaadaDBAdmin)(frame)).showClass(tree_path_components);
			//			} catch (Exception e1) {
			//				// TODO Auto-generated catch block
			//				e1.printStackTrace();
			//			}
		}

		else if( item.equals(MAPP_DM)) {

			//			SelectClass sc;
			//			try {
			//				sc = new SelectClass(this.frame, Database.getCachemeta().getVOResourceNames());
			//				String c = sc.getTyped_name();		
			//				if( c!= null ) {
			//					(new ClassToDatamodelMapper(c, tree_path_components[1].toString(), Category.getCategory(tree_path_components[2].toString()))).selectClass(tree_path_components[3].toString());
			//				}
			//
			//			} catch (Exception e1) {
			//				Messenger.printStackTrace(e1);
			//			}

		}
		else if( item.equals(EDIT_DM)) {
			//			try {
			//				new DMBuilder();
			//			} catch (Exception e1) {
			//				Messenger.printStackTrace(e1);
			//			}
		}
		//		else if( item.equals(CREATE_RELATIONSHIP) ) {
		//			ct = new CmdCreateRelation(frame, tree_path_components);
		//		}
		//		else if( item.equals(REMOVE_RELATIONSHIP) ) {
		//			ct = new CmdDeleteRelation(frame, tree_path_components);
		//		}
		//		if( ct != null ) {
		//			/*
		//			 * Thread die when main console closed
		//			 */
		//			ct.setDaemon(true);
		//			ct.start();
		//			ct.setPriority(Thread.MIN_PRIORITY);
		//		}
	}
}
