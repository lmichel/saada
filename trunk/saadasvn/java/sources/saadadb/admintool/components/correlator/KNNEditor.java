package saadadb.admintool.components.correlator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;


public class KNNEditor extends CollapsiblePanel {
	private JComboBox knn_mode;
	private JComboBox knn_unit;
	private JTextField knn_k;
	private JTextField knn_dist;
	private RelationPopulatePanel taskPanel;
	
	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public KNNEditor(RelationPopulatePanel taskPanel, Component toActive) {
		super("Neighborhood Constraint");
		this.taskPanel = taskPanel;
		
		knn_mode = new JComboBox(new String[]{"None", "K-NN", "1st-NN"});
		knn_unit = new JComboBox(new String[]{"degree", "arcmin", "arcsec", "mas", "sigma"});
		knn_k = new JTextField(2);
		knn_dist = new JTextField(5);
		knn_mode.setToolTipText("Select the mode of correlations by position");		
		knn_mode.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( knn_mode.getSelectedItem().toString().equals("None") ) {
					knn_k.setEnabled(false);
					knn_dist.setEnabled(false);
					knn_unit.setEnabled(false);
					return;
				}
				else if( knn_mode.getSelectedItem().toString().equals("K-NN") ) {
					knn_k.setEnabled(true);
					knn_dist.setEnabled(true);
					knn_unit.setEnabled(true);
					return;
				}
				else if( knn_mode.getSelectedItem().toString().equals("1st-NN") ) {
					knn_k.setEnabled(false);
					knn_dist.setEnabled(true);
					knn_unit.setEnabled(true);
					return;
				}
			}
		});
		knn_k.setToolTipText("Max number ofcorrelated neighbours");
		knn_k.setEnabled(false);
		knn_k.addKeyListener(new KeyAdapter() {
			  public void keyTyped(KeyEvent e) {
				  	/*
				  	 * k must be a integer and positive value
				  	 */
				    char c = e.getKeyChar();
				    if (!((Character.isDigit(c) ||
				      (c == KeyEvent.VK_BACK_SPACE) ||
				      (c == KeyEvent.VK_DELETE)))) {
				        getToolkit().beep();
				        e.consume();
				    }
				  }
				});
		knn_dist.setEnabled(false);
		knn_dist.addKeyListener(new KeyAdapter() {
			  public void keyTyped(KeyEvent e) {
				  	/*
				  	 * Distance must be a float and positive value
				  	 */
				    char c = e.getKeyChar();
				    String txt = knn_k.getText();
				    if( c == '.' && txt.indexOf('.') >= 0 ) {
				        getToolkit().beep();
				        e.consume();				    	
				    }
				    if (!((Character.isDigit(c) || c == '.' ||
				      (c == KeyEvent.VK_BACK_SPACE) ||
				      (c == KeyEvent.VK_DELETE)))) {
				        getToolkit().beep();
				        e.consume();
				    }
				  }
				});	

		MyGBC mc = new MyGBC(5,5,5,5); mc.anchor = GridBagConstraints.NORTH;
		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		mc.right(false);
		panel.add(knn_mode, mc);
		mc.next();mc.left(false);
		panel.add(knn_k, mc);
		mc.next();mc.left(false);
		panel.add(knn_dist, mc);
		mc.rowEnd();
		panel.add(knn_unit, mc);
	}
}
