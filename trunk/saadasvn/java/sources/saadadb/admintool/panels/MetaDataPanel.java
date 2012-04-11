package saadadb.admintool.panels;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import saadadb.admin.dnd.TreePathTransferable;
import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.windows.DataTableWindow;
import saadadb.cache.CacheMeta;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.executor.Query;
import saadadb.util.Messenger;

/**
 * Display the SaadaDB data tree
 * @author michel
 *
 */
public class MetaDataPanel extends JPanel implements DragGestureListener,  DragSourceListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JTree tree;
	protected AdminTool frame;
	protected DefaultTreeModel model;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode("Collections");
	private Timer simplCLickTimer = new Timer(500, new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			processSimpleClick();
		}
	});
	private TreePath clickedTreePath ;
	;
	void processSimpleClick(){
		if( simplCLickTimer.isRunning()) {
			if( clickedTreePath != null ) {
				try {
					MetaDataPanel.this.frame.setDataTreePath(new DataTreePath(clickedTreePath));
				} catch (QueryException e1) {
					Messenger.trapQueryException(e1);
				}
			}
			tree.setSelectionPath(clickedTreePath);
			simplCLickTimer.stop();
		}
	}
	void processDoubleClick(){
		if( simplCLickTimer.isRunning()) {
			simplCLickTimer.stop();
			if( clickedTreePath.getPathCount() >= 2) {
				try {
					DataTableWindow dtw = new DataTableWindow(frame, clickedTreePath);
					dtw.open();
				} catch (QueryException e) {
					Messenger.trapQueryException(e);
				}
			}
			else {
				AdminComponent.showInputError(frame, "Only category ether class nodes can be displayed");
			}

		}
	}

	public MetaDataPanel(AdminTool frame, int largeur, int hauteur) throws SaadaException {
		this.frame = frame;
		model = new DefaultTreeModel(top);
		tree = new JTree(model){
			@Override
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				TreePath path = getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					String description = "";
					if( path.getPathCount() >= 2) {
						try {
							description += Database.getCachemeta().getCollection(path.getPathComponent(1).toString()).getDescription();
						} catch (FatalException e1) {
							Messenger.trapFatalException(e1);
						}
					}
					if( description.length() > 0 ) {
						description = "<BR>Description<BR><PRE>\n" + description + "</PRE>";
					}
					switch(path.getPathCount()) {
					case 2: tip = "<HTML>Data collection:<B>" 
						+ path.getLastPathComponent() 
						+ "</B>"+ description  + "<BR>";
					break;
					case 3: tip = "<HTML><B>" + path.getLastPathComponent() + "</B> node of data collection <B>" + path.getPathComponent(1)
					+ "</B>"+ description  + "<BR>";
					break;
					case 4: tip = "<HTML><B>" + path.getPathComponent(2) + "</B> data class <B>" +  path.getLastPathComponent()
					+ "</B> of collection <B>" + path.getPathComponent(1) 
					+ "</B>"+ description  + "<BR>";
					break;
					default: break;
					}
				}
				return tip ;
			}
		};
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setEditable(false);		
		tree.setBorder(BorderFactory.createTitledBorder("Database Map"));		
		this.createTree();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.addMouseListener(new MouseAdapter() {
			boolean memoBoutonDroit = false;
			@Override
			public void mousePressed(MouseEvent e) {
				if( !simplCLickTimer.isRunning() ) simplCLickTimer.start();

			}
			@Override
			public void mouseReleased(MouseEvent e) {
				clickedTreePath = tree.getPathForLocation(e.getX(), e.getY());
				if( e.getClickCount() == 2) {
					processDoubleClick();
				}

				tree.setSelectionPath(clickedTreePath);
			}
		});	
		/*
		 * Change the renderer to apply a specific icon on classes with instances
		 */
		FilledNodeRenderer new_tcr = new FilledNodeRenderer();
		tree.setCellRenderer(new_tcr);
		expandAll(true);
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(largeur, hauteur));
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.setDragFeatures();
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	public void createTree() throws SaadaException {
		CacheMeta cm = Database.getCachemeta();
		String[] colls = cm.getCollection_names();
		model.setRoot(null);
		top = new DefaultMutableTreeNode("Collections ");
		model.setRoot(top);
		/*
		 * Loop on collections
		 */
		for (int k = 0; k < colls.length; k++) {
			addCollectionPath(colls[k]);
		}
	}

	/**
	 * Add a collection path to the tree
	 * @param coll_name
	 * @throws SaadaException
	 */
	private void addCollectionPath(String coll_name) throws SaadaException {
		DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode(coll_name);
		model.insertNodeInto(collectionNode, top, top.getChildCount());
		/*
		 * Loop on  categories
		 */
		for( int c=1 ; c<Category.NB_CAT ; c++ ) {
			/*
			 * Cubes are not implemented yet
			 */
			if( c == Category.CUBE ) continue;
			addCategoryNode(collectionNode, coll_name, c);
		}		
		tree.scrollPathToVisible(new TreePath(collectionNode.getPath()));
	}

	/**
	 * Add a requested category node on the collection node
	 * @param collnode
	 * @param coll_name
	 * @param category
	 * @throws SaadaException
	 */
	private void addCategoryNode(DefaultMutableTreeNode collnode, String coll_name, int category) throws SaadaException {
		String[] classes = Database.getCachemeta().getClassesOfCollection(coll_name, category);
		DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(Category.explain(category));
		model.insertNodeInto(categoryNode, collnode, collnode.getChildCount());
		/*
		 * No class for flatfiles
		 */
		if( category != Category.FLATFILE ) {    	
			/*
			 * Loop on classes
			 */    	
			for( int cl=0 ; cl<classes.length ; cl++ ) {
				model.insertNodeInto(new DefaultMutableTreeNode(classes[cl]), categoryNode, categoryNode.getChildCount());
			}
		} 
	}

	/**
	 * @param collname
	 * @param category
	 * @throws SaadaException 
	 */
	public void synchronizeCategoryNodes(String collname, String category) throws SaadaException {
		int tree_nb_coll = top.getChildCount();
		/*
		 * Look for the collection node
		 */
		for( int n=0 ; n<tree_nb_coll ; n++ ) {
			DefaultMutableTreeNode coll_node = (DefaultMutableTreeNode) top.getChildAt(n);
			if( coll_node.toString().equals(collname)) {
				int tree_nb_cat = coll_node.getChildCount();
				/*
				 * Look for the category node
				 */
				for( int c=0 ; c<tree_nb_cat ; c++ ) {
					DefaultMutableTreeNode cat_node = (DefaultMutableTreeNode) coll_node.getChildAt(c);
					/*
					 * remove and rebluild the category node in order to show new classes
					 * and to invoke the correct rendering (full or empty category)
					 */
					if( cat_node.toString().equalsIgnoreCase(category)) {
						model.removeNodeFromParent(cat_node);
						addCategoryNode(coll_node, collname, Category.getCategory(category));
						if( cat_node.getChildCount() > 0 ) {
							tree.scrollPathToVisible(new TreePath(((DefaultMutableTreeNode)(cat_node.getChildAt(0))).getPath()));							
						}
						else {
							tree.scrollPathToVisible(new TreePath(cat_node.getPath()));
						}
						return;
					}
				}	
			}
		}
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	public void synchronizeCollectionNodes() throws SaadaException {
		int tree_nb_coll = top.getChildCount();
		CacheMeta cm = Database.getCachemeta();
		String[] colls = cm.getCollection_names();
		/*
		 * Insert collections being in cache meta but not in the tree
		 */
		for( int c=0 ; c<colls.length ; c++) {
			boolean found = false;
			for( int n=0 ; n<tree_nb_coll ; n++ ) {
				if( colls[c].equals(top.getChildAt(n).toString())) {
					found = true;
					break;					
				}
			}
			if( !found ) {
				addCollectionPath(colls[c]);
			}
		}
		/*
		 * remove collections being in tree meta but not in the cache meta
		 */
		for( int n=0 ; n<tree_nb_coll ; n++ ) {
			boolean found = false;
			/*
			 * In the case of the tree have been modified somewhere
			 */
			if( n >= top.getChildCount()) {
				break;
			}
			String node_str = top.getChildAt(n).toString();
			for( int c=0 ; c<colls.length ; c++) {
				if( colls[c].equals(node_str)) {
					found = true;
					break;					
				}
			}
			if( !found ) {
				model.removeNodeFromParent((MutableTreeNode) top.getChildAt(n));
			}
		}			
	}

	public DefaultMutableTreeNode addObject(Object child) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = tree.getSelectionPath();

		if (parentPath == null) {
			//There is no selection. Default to the root node.
			parentNode = top;
		} else {
			parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
		}
		return addObject(parentNode, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			Object child,
			boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode =
			new DefaultMutableTreeNode(child);

		model.insertNodeInto(childNode, parent, parent.getChildCount());

		//Make sure the user can see the lovely new node.
		if (shouldBeVisible) {
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}    

	public void expandAll(boolean expand) {
		TreeNode root = (TreeNode)tree.getModel().getRoot();
		expandAll(new TreePath(root), expand);
	}	
	private void expandAll(TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode)parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e=node.children(); e.hasMoreElements(); ) {
				TreeNode n = (TreeNode)e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				if( path.getPathCount() == 1 ) {
					expandAll(path, expand);
				}
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}	

	/*
	 * Tree node renderer in charge of marking classes having instances
	 */
	static private class FilledNodeRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static Icon filled_icon = new MetalIconFactory.FolderIcon16();

		/*
		 * This method is invoked each time a node is redrawn. The query results must be cached in the future
		 *  (non-Javadoc)
		 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			/*
			 * Leaf refers to the visible leaf not top the tree leaf.
			 * they differ when a branch is folder. This test is
			 * not sufficiant to detect that we are on a class node.
			 * We assume that class nodes are at 4th position in the tree path
			 */
			if ( leaf ) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
				try {
					TreeNode[] path = node.getPath();
					if( path.length == 3 && node.toString().equals("FLATFILE")) {
						Query q = new Query();
						/*
						 * Can be run while a transaction in another thread: LOCK error with MySQL
						 */
						//						if( q.runQuery("Select FLATFILE From * In " + node.getParent().toString()).getSize() > 0) {
						if( Database.getCachemeta().getCollection(node.getParent().toString()).hasFlatFiles()) {
							setIcon(filled_icon);
						}
					}
					/*
					 * We are on class node
					 */
					else if( path.length == 4 ) {
						String ac;
						/*
						 * Entries have no products, they are considered as having no instance. We have to
						 * get the associated class (entry's table) to detect if the class is empty 
						 */
						if( "ENTRY".equals(node.getParent().toString()) ) {
							ac = Database.getCachemeta().getClass(node.toString()).getAssociate_class();
						}
						else {
							ac = node.toString();
						}
						if( Database.getCachemeta().getClass(ac).hasInstances() ) {
							setIcon(filled_icon);
						}
					}
				} catch (SaadaException e) {
					Messenger.printStackTrace(e);
					AdminComponent.showFatalError(null, e.toString());
				}
			}
			return this;
		}
	}

	/*
	 * Drag gesture must be redefined in subclasses
	 * (non-Javadoc)
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
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
		DragSource ds = new DragSource();
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(this.tree, DnDConstants.ACTION_COPY, this);

	}

}


