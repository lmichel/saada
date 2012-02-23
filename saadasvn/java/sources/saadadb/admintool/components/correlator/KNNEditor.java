package saadadb.admintool.components.correlator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.configuration.RelationConf;


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

	/**
	 * @param conf
	 */
	public void load(RelationConf conf) {
		String correlator = conf.getQuery();
		/*
		 * Parse Condition 1st neighbour 
		 */
		Pattern p = Pattern.compile("ConditionDist\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
		Matcher m = p.matcher(correlator);		
		knn_dist.setText("");
		knn_k.setText("");
		knn_mode.setSelectedIndex(0);
		if( m.find() ) {
			knn_mode.setSelectedIndex(2);
			String con = m.group(1);
			for( int i=0 ; i<knn_unit.getItemCount() ; i++ ) {
				if( con.matches(".*" + knn_unit.getItemAt(i).toString() + ".*") ) {
					knn_unit.setSelectedIndex(i);
					break;
				}
			}
			p = Pattern.compile("([0-9]+\\.?[0-9]*)", Pattern.DOTALL);
			m = p.matcher(correlator);		
			if( m.find() ) {
				knn_dist.setText(m.group(1));
			}
		}
		/*
		 * Parse Condition K Nearest neighbours 
		 */
		p = Pattern.compile("ConditionKnn\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
		m = p.matcher(correlator);		
		if( m.find() ) {
			knn_mode.setSelectedIndex(1);
			String con = m.group(1);
			for( int i=0 ; i<knn_unit.getItemCount() ; i++ ) {
				if( con.matches(".*" + knn_unit.getItemAt(i).toString() + ".*") ) {
					knn_unit.setSelectedIndex(i);
					break;
				}
			}
			p = Pattern.compile("([0-9]+)", Pattern.DOTALL);
			m = p.matcher(correlator);		
			if( m.find() ) {
				knn_k.setText(m.group(1));
			}
			p = Pattern.compile(",\\s*([0-9]+\\.?[0-9]*)", Pattern.DOTALL);
			m = p.matcher(correlator);		
			if( m.find() ) {
				knn_dist.setText(m.group(1));
			}
		}
	}

	/**
	 * @return
	 */
	public String getCorrelator() {
		String retour = "";
		//K-NN", "1st-NN
		if( knn_mode.getSelectedItem().toString().equals("K-NN") ) {
			if( knn_k.getText().length() == 0 ) {
				AdminComponent.showInputError(this.getParent(), "K value must be given");
				return "";
			}
			else if( knn_dist.getText().length() == 0 ) {
				AdminComponent.showInputError(this.getParent(), "A distance value must be given");
				return "";
			}
			else {
				retour += "ConditionKnn{" + knn_k.getText() + ", " + knn_dist.getText() + " [" +  knn_unit.getSelectedItem() + "]}\n";
			}
		}
		else if( knn_mode.getSelectedItem().toString().equals("1st-NN") ) {
			if( knn_dist.getText().length() == 0 ) {
				AdminComponent.showInputError(this.getParent(), "A distance value must be given");
				return "";
			}
			else {
				retour += "ConditionDist{" + knn_dist.getText() + " [" +  knn_unit.getSelectedItem() + "]}\n";
			}
		}
		return retour;
	}
	
	/**
	 * 
	 */
	public void reset() {
		this.knn_mode.setSelectedIndex(0);
		this.knn_k.setText("");
		this.knn_dist.setText("");
	}

}
