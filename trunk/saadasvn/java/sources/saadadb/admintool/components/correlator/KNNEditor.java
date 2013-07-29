package saadadb.admintool.components.correlator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.configuration.RelationConf;
import saadadb.util.Messenger;


public class KNNEditor extends CollapsiblePanel {
	private JPanel knnModePanel, knnUnitPanel;
	private JTextArea knn_descriptionLabel;
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
				KNNEditor.this.taskPanel.notifyChange();
				KNNEditor.this.setDescriptionString();
				if( knn_mode.getSelectedItem().toString().equals("None") ) {
					knn_k.setEnabled(false);
					knn_k.setText("0");
					knn_dist.setEnabled(false);
					knn_unit.setEnabled(false);
					return;
				}
				else if( knn_mode.getSelectedItem().toString().equals("K-NN") ) {
					knn_k.setEnabled(true);
					knn_k.setText("");
					knn_dist.setEnabled(true);
					knn_unit.setEnabled(true);
					knn_k.requestFocus();
					KNNEditor.this.setDescriptionString();
					return;
				}
				else if( knn_mode.getSelectedItem().toString().equals("1st-NN") ) {
					knn_k.setEnabled(false);
					knn_k.setText("1");
					knn_dist.setEnabled(true);
					knn_unit.setEnabled(true);
					return;
				}
			}
		});
		knn_k.setToolTipText("Maximum number of correlated neighbours");
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
				} else {
					KNNEditor.this.taskPanel.notifyChange();
				}
			}
			
			public void keyReleased(KeyEvent e) 
			{
				KNNEditor.this.setDescriptionString();
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
				if( (c == '.' && txt.indexOf('.') >= 0) || (!((Character.isDigit(c) || c == '.' ||
						(c == KeyEvent.VK_BACK_SPACE) ||
						(c == KeyEvent.VK_DELETE)))) ){
					getToolkit().beep();
					e.consume();
				} else {
					KNNEditor.this.taskPanel.notifyChange();
				}
			}
			public void keyReleased(KeyEvent e) 
			{
				KNNEditor.this.setDescriptionString();
			}
		});	
		knn_unit.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				KNNEditor.this.setDescriptionString();
			}
		});
		
		knn_descriptionLabel = AdminComponent.getHelpLabel("No neighbourhood constraint");
		knn_descriptionLabel.setCursor(null);  
		knn_descriptionLabel.setOpaque(false);  
		knn_descriptionLabel.setFocusable(false);       
        knn_descriptionLabel.setWrapStyleWord(true);
        knn_descriptionLabel.setLineWrap(true);
		knn_descriptionLabel.setPreferredSize(new Dimension(250, 60));
		
		knnModePanel = new JPanel();
		knnUnitPanel = new JPanel();

		MyGBC mc = new MyGBC(5,5,5,5); mc.anchor = GridBagConstraints.NORTH;
		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		mc.right(false);
		knnModePanel.add(knn_mode);
		knnModePanel.add(knn_k);
		knnModePanel.setPreferredSize(new Dimension(180,55));
		knnModePanel.setBorder(BorderFactory.createTitledBorder("Maximum neighbours"));
		panel.add(knnModePanel, mc);
		mc.next();mc.left(false);
		knnUnitPanel.add(knn_dist);
		knnUnitPanel.add(knn_unit);
		knnUnitPanel.setPreferredSize(new Dimension(180,55));
		knnUnitPanel.setBorder(BorderFactory.createTitledBorder("Distance value"));
		panel.add(knnUnitPanel, mc);
		mc.next();mc.center();
		mc.fill = GridBagConstraints.BOTH;
		panel.add(this.knn_descriptionLabel, mc);
		mc.rowEnd();
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
		this.knn_k.setText("0");
		this.setDescriptionString();
		this.knn_dist.setText("");				
		// Just to avoid to notify change at opening time
		KNNEditor.this.taskPanel.cancelChanges();

	}
	
	private void setDescriptionString()
	{
		String res = "";
		if (knn_mode.getSelectedItem().toString().equals("None")) 
		{
			res = "No neighbourhood constraint";
		}
		else 
		{
			res = "Selects ";
			if (knn_mode.getSelectedItem().toString().equals("K-NN")) 
				res += "the " + (knn_k.getText().equals("")?"?":knn_k.getText()) + " nearest neighbour"+ ((knn_k.getText().compareTo("")!=0&&Integer.parseInt(knn_k.getText())>1)?"s":"");
			
			if (knn_mode.getSelectedItem().toString().equals("1st-NN")) 
				res += "the nearest neighbour";
			
			res += " at less than ";
			
			if (knn_dist.getText().trim().length()>0)
				res += knn_dist.getText();
			else
				res += "?";
			
			res += " " + knn_unit.getItemAt(knn_unit.getSelectedIndex());
		}
		this.knn_descriptionLabel.setText(res);
	}
}
