/**
 * 
 */
package saadadb.admintool.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.HelpedTextField;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 *
 */
public class FilteredFieldTree extends JPanel {
	private VoClassTree classTree;
	private HelpedTextField maskField;
	private HashMap<String, AttributeHandler> attributeHandlers;

	public FilteredFieldTree(String title,int width, int height) throws Exception{
		super();
		this.setBorder(BorderFactory.createTitledBorder(title));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		classTree = new VoClassTree(this);
		this.setPreferredSize(new Dimension(width, height));
		classTree.setPreferredSize(new Dimension(width, height));
		this.add(classTree);
		maskField = new HelpedTextField("Field Name Filter", 15);
		this.add(maskField);
		this.maskField.setRunnable(new Runnable() {			
			public void run() {
				try {
					FilteredFieldTree.this.classTree.filterTree(".*" + FilteredFieldTree.this.maskField.getText() + ".*" );
				} catch (FatalException e) {
					Messenger.printStackTrace(e);
				}
			}
		});
	}
	
	
	/**
	 * @param attributeHandlers
	 * @throws FatalException
	 */
	public void setAttributeHandlers(HashMap<String, AttributeHandler> attributeHandlers) throws FatalException {
		this.attributeHandlers = attributeHandlers;
		this.classTree.buildTree(this.attributeHandlers);
		this.classTree.drawTree(this.classTree.getPreferredSize());
	}
	
	/**
	 * @param mc
	 * @throws FatalException 
	 */
	public void setAttributeHandlers( MetaClass mc) throws FatalException {
		this.attributeHandlers = Database.getCachemeta().getCollection(mc.getCollection_name()).getAttribute_handlers(mc.getCategory());
		this.attributeHandlers.putAll(mc.getAttributes_handlers());
		this.classTree.buildTree(this.attributeHandlers);
		this.classTree.drawTree(this.classTree.getPreferredSize());
	}
	/**
	 * @param mc
	 * @param category
	 * @throws FatalException
	 */
	public void setAttributeHandlers( int category) throws FatalException {
		this.attributeHandlers = MetaCollection.getAttribute_handlers(category);
		this.classTree.buildTree(this.attributeHandlers);
		this.classTree.drawTree(this.classTree.getPreferredSize());
	}
	/**
	 * @param mc
	 * @param category
	 * @throws FatalException
	 */
	public void setAttributeHandlers( String category) throws FatalException {
		this.attributeHandlers = MetaCollection.getAttribute_handlers(Category.getCategory(category));
		this.classTree.buildTree(this.attributeHandlers);
		this.classTree.drawTree(this.classTree.getPreferredSize());
	}

	public  void buildTree(HashMap<String, AttributeHandler> attributeHandlers) throws FatalException {
			this.classTree.buildTree(attributeHandlers);
	}
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Database.init("AJAX");
		MetaClass mc = Database.getCachemeta().getClass("qqqqqqEntry");
		FilteredFieldTree fft = new FilteredFieldTree("aaaaa", 200, 300);
		fft.setAttributeHandlers(mc.getAttributes_handlers());

		JFrame f = new JFrame();
		f.getContentPane().add(fft);
		f.pack();
		f.setVisible(true);
	}
}
