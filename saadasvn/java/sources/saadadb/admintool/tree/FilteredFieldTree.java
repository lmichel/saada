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

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 *
 */
public class FilteredFieldTree extends JPanel {
	private VoClassTree classTree;
	private JTextField maskField;
	private HashMap<String, AttributeHandler> attributeHandlers;

	public FilteredFieldTree(String title,int width, int height) throws Exception{
		super();
		this.setBorder(BorderFactory.createTitledBorder(title));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		classTree = new VoClassTree(this);
		classTree.setPreferredSize(new Dimension(width, height));
		this.add(classTree);
		maskField = new JTextField(15);
		this.add(maskField);
		
		maskField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent arg0) {}		
			public void keyReleased(KeyEvent arg0) {
				try {
					FilteredFieldTree.this.classTree.filterTree(".*" + FilteredFieldTree.this.maskField.getText() + ".*" );
				} catch (FatalException e) {
					Messenger.printStackTrace(e);
				}
			}		
			public void keyPressed(KeyEvent arg0) {}
		});
	}
	
	public void setAttributeHandlers(HashMap<String, AttributeHandler> attributeHandlers) throws FatalException {
		this.attributeHandlers = attributeHandlers;
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
