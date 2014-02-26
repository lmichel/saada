package saadadb.admintool.tree;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;

public class VoClassTree extends VoDataProductTree {
	private Map<String, AttributeHandler> attributeHandlers;
	
	public VoClassTree(Container frame) throws Exception {
		super(frame);
	}
	public VoClassTree(Container frame, String  classe) throws Exception {
		super(frame, Database.getCachemeta().getClass(classe).toString());
	}
	public VoClassTree(Container frame, MetaClass  mc) throws Exception {
		super(frame, mc.toString());
	}

	/**
	 * @param attributeHandlers
	 * @throws FatalException
	 */
	public  void buildTree(Map<String, AttributeHandler> attributeHandlers) throws FatalException {
		this.attributeHandlers = attributeHandlers;
		ArrayList<String> flat_array = new ArrayList<String>();
		for( String attname: this.attributeHandlers.keySet()) {
			AttributeHandler ah = attributeHandlers.get(attname);
			/*
			 * Housekeeping filed are ignored
			 */
			if( attname.equals("oidsaada") || attname.equals("oidproduct") || attname.equals("contentsignature") ||
					attname.equals("date_load") || attname.equals("access_right") || attname.equals("loaded") ||
					attname.equals("oidtable") ) {
				continue;
			}
			String leaf;
			leaf = ah.getNameorg();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname  + "<>Org Name:" +leaf);						
			}
			leaf = ah.getType();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname  + "<>Type:" +leaf);						
			}
			leaf = ah.getUnit();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname + "<>Unit:" +leaf);						
			}
			leaf = ah.getComment();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname  + "<>Comment:\"" +leaf+ "\"");						
			}
			leaf = ah.getUcd();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname + "<>UCD:" +leaf);						
			}
			leaf = ah.getUtype();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname  + "<>Utype:" +leaf);						
			}
			leaf = ah.getValue();
			if( leaf != null && leaf.length()!= 0 ) {
				flat_array.add(attname  + "<>Value:" +leaf);						
			}
		}
		flat_types = flat_array.toArray(new String[0]);
	}

	/**
	 * @param filterRegExp
	 * @throws FatalException
	 */
	protected  void filterTree(String filterRegExp) throws FatalException {
		if( tree == null ) return;
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode top = (DefaultMutableTreeNode) model.getRoot();

		while( model.getChildCount(top) > 0 ) {
			MutableTreeNode node = (MutableTreeNode) model.getChild(top, 0);
			model.removeNodeFromParent(node);		
		}
		model.reload();

		DefaultMutableTreeNode last_node=top;
		for( int i=0 ; i<flat_types.length ; i++ ) {
			/*
			 * Input data are transformed in a set of string matching the future nodes
			 */
			String[] utype_tokens = this.getPathComponents(flat_types[i]);
			/*
			 * Skip empty lines
			 */
			if(utype_tokens == null || utype_tokens.length == 0 ) {
				continue;
			}	
			else if( !utype_tokens[0].matches(filterRegExp)) {
				continue;
			}
			/*
			 * One token makes one node: no need to further computation
			 */
			else if( utype_tokens.length == 1 ) {
				last_node = new DefaultMutableTreeNode(utype_tokens[0]);
				top.add(last_node);
			}
			/*
			 * Compute the path, which can be partially merged with the previous one
			 */
			else {
				TreeNode[] tnp = last_node.getPath();
				Boolean match_found = false;
				last_node = top;
				for( int t=0 ; t<utype_tokens.length  ; t++ ) {
					/*
					 * Done while current path node matches previous path node
					 */
					if( !match_found ) {
						if( t >= (tnp.length-1)) {
							match_found = true;
						}
						else if( !utype_tokens[t].equals(tnp[t+1].toString()) ) {
							last_node = (DefaultMutableTreeNode) tnp[t];
							match_found = true;
						}
					}
					/*
					 * Current node differs from the the previous node: buid a new branch
					 */
					if( match_found ) {
						DefaultMutableTreeNode node = new DefaultMutableTreeNode(utype_tokens[t]);
						last_node.add(node);
						last_node = node;	
					}
				}		    		
			}
		}
		model.reload();
	}
}
