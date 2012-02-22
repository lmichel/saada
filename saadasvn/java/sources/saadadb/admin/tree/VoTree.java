package saadadb.admin.tree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import saadadb.admin.dnd.GestualTree;

public abstract class VoTree extends JPanel {
	protected GestualTree tree;
	protected Window frame;
	protected String top_node="NoTopNode";
	
	public String[] flat_types;
	
	/** * @version $Id$

	 * @param frame
	 * @param top_node
	 */
	public VoTree(Window frame, String top_node) {
		this.frame = frame;
		this.top_node = top_node;
	}
	/**
	 * Build the tree with flat input data
	 * @param dim
	 */
	public void buildTree(Dimension dim) {
		//this.setPreferredSize(new Dimension(dim));
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(top_node);
		tree = new GestualTree(top);
		tree.setEditable(false);	
		//tree.setDragEnabled(true);
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
		/*
		 * Makes visible the first level of nodes
		 */
		for( int i=0 ; i<top.getChildCount() ; i++ ) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) top.getChildAt(i);
			tree.scrollPathToVisible(new TreePath(node.getPath()));

		}
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(dim);
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		this.setDragFeatures();
	}

	protected void setDragFeatures() {
		tree.setDragEnabled(true);
	}
	
	/**
	 * @param string
	 * @return
	 */
	abstract String[] getPathComponents(String string) ;
	
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();		
		JTabbedPane onglets = new JTabbedPane();
		onglets.setPreferredSize(new Dimension(300, 500));
		
		VoTree vot = new VoUCDTree(frame);
		vot.buildTree(new Dimension(300, 500));
		onglets.addTab("UCD", null, vot, "qqqqqqqA");

		vot = new VoCharacDMTree(frame);
		vot.buildTree(new Dimension(300, 500));
		onglets.addTab("Charac DM", null, vot, "qqqqqqqA");
		
		vot = new VoSpectrumDMTree(frame);
		vot.buildTree(new Dimension(300, 500));
		onglets.addTab("Spectrum DM", null, vot, "qqqqqqqA");

		onglets.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
		        JTabbedPane tab = (JTabbedPane)e.getSource();
		        int sIdx = tab.getSelectedIndex();
			}
			
		});

		frame.add(onglets);
		frame.pack();
		frame.setVisible(true);
		
	}
}
