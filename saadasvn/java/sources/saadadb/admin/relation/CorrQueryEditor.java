package saadadb.admin.relation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class CorrQueryEditor extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JTextField primary_classes = new JTextField(20);
	protected JComboBox primary_combo = new JComboBox();
	protected JComboBox primary_att = new JComboBox();
	protected JTextField secondary_classes = new JTextField(20);
	protected JComboBox secondary_combo = new JComboBox();
	protected JComboBox secondary_att = new JComboBox();
	protected JTextArea condition = new JTextArea(12, 48);
	protected LinkedHashMap<String, QualifierSetter> qual_setter = new LinkedHashMap<String, QualifierSetter>();
	protected JComboBox knn_mode = new JComboBox(new String[]{"None", "K-NN", "1st-NN"});
	protected JComboBox knn_unit = new JComboBox(new String[]{"degree", "arcmin", "arcsec", "mas", "sigma"});
	private JTextField knn_k = new JTextField(2);
	private JTextField knn_dist = new JTextField(5);
	private JTextArea active_att_receiver;
	private RelationConf relation;
	/**
	 * 
	 */
	public CorrQueryEditor(){
		this.setBackground(SaadaDBAdmin.beige_color);
		this.setLayout(new GridBagLayout());
		GridBagConstraints cl = new GridBagConstraints();
		
		this.setBorder(BorderFactory.createTitledBorder("Join Query"));
		cl.insets = new Insets(5,5,5,5);
		cl.gridx  = 0; cl.gridy = 0;	
		cl.gridwidth = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_END;
		this.add(SaadaDBAdmin.getPlainLabel("Primary Classes"), cl);
		cl.gridx = 1; cl.gridy = 0;	
		cl.gridwidth = 4;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		primary_classes.setToolTipText("Subset of the primary collection covered by the join query");
		this.add(primary_classes, cl);
		cl.gridx = 5; cl.gridy = 0;	
		cl.gridwidth = 2;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		primary_combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( primary_combo.getSelectedItem() == null ) {
					return;
				}
				String si = primary_combo.getSelectedItem().toString();
				String ct = primary_classes.getText().trim();
				if( !ct.equals(si) && !ct.matches(".*,\\s*" + si + ".*") && !ct.matches(".*" + si + "\\s*,.*")) {
					if( si.startsWith("ANY")) {
						primary_classes.setText("*");
					}
					else if( ct.length() == 0 || ct.equals("*")){
						primary_classes.setText(si);			
					}
					else {
						primary_classes.setText((ct + "," + si).replaceAll(",,", ","));			
					}
					updateAvailableAttributes("p.");
				}
			}
			
		});
		primary_combo.setToolTipText("List of sub-classes of the primary collection - transfered to the text field when selected");
		this.add(primary_combo, cl);
		
		cl.gridx  = 0; cl.gridy = 1;	
		cl.gridwidth = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_END;
		this.add(SaadaDBAdmin.getPlainLabel("Secondary Classes"), cl);
		cl.gridx = 1; cl.gridy = 1;	
		cl.gridwidth = 4;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		secondary_classes.setToolTipText("Subset of the secondary collection covered by the join query");
		this.add(secondary_classes, cl);
		cl.gridx = 5; cl.gridy = 1;	
		cl.gridwidth = 2;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		secondary_combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( secondary_combo.getSelectedItem() == null ) {
					return;
				}
				String si = secondary_combo.getSelectedItem().toString();
				String ct = secondary_classes.getText().trim();
				if( !ct.equals(si) && !ct.matches(".*,\\s*" + si + ".*") && !ct.matches(".*" + si + "\\s*,.*")) {
					if( si.startsWith("ANY")) {
						secondary_classes.setText("*");
					}
					else if( ct.length() == 0 || ct.equals("*")){
						secondary_classes.setText(si);			
					}
					else {
						secondary_classes.setText((ct + "," + si).replaceAll(",,", ","));			
					}
					updateAvailableAttributes("s.");
				}
			}

			
		});
		secondary_combo.setToolTipText("List of sub-classes of the secondary collection - transfered to the text field when selected");
		this.add(secondary_combo, cl);
		
		
		/*
		 * KNN editor
		 */
		cl.gridx = 0; cl.gridy = 2;	
		cl.gridwidth = 1;
		cl.gridheight = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_END;
		this.add(SaadaDBAdmin.getPlainLabel("Neighborhood Constraint"), cl);
		
		cl.gridx = 1; cl.gridy = 2;	
		cl.gridwidth = 1;
		cl.gridheight = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
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

		this.add(knn_mode, cl);	
		cl.gridx = 2;
		this.add(SaadaDBAdmin.getPlainLabel("k="), cl);	
		cl.gridx = 3;
		this.add(knn_k, cl);	
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
		cl.gridx = 4;
		this.add(SaadaDBAdmin.getPlainLabel("dist max="), cl);	
		cl.gridx = 5;
		this.add(knn_dist, cl);	
		knn_dist.setToolTipText("Max distance ofcorrelated neighbours");
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
		cl.gridx = 6;
		this.add(knn_unit, cl);	
		knn_unit.setToolTipText("Distance unit");
		knn_unit.setEnabled(false); 	
		
		
		cl.gridx  = 0; cl.gridy = 3;	
		cl.gridwidth = 1;
		cl.gridheight = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_END;
		this.add(SaadaDBAdmin.getPlainLabel("Keyword Based Condition"), cl);
		cl.gridx = 1; cl.gridy = 3;	
		cl.gridwidth = 6;
		cl.gridheight = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		condition.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		JScrollPane jsp = new JScrollPane(condition);
		Dimension dim = condition.getPreferredSize();
		dim.setSize(dim.getWidth() + 1, dim.getHeight() + 1);
		jsp.setPreferredSize(dim);
		condition.setToolTipText("Primary and secondary records are linked when the condition (SQL statement) is true.");
		condition.addCaretListener(new CaretListener() {

			public void caretUpdate(CaretEvent e) {
				JTextArea jta = (JTextArea) e.getSource();
				if( jta != CorrQueryEditor.this.active_att_receiver )  {
					CorrQueryEditor.this.activateQueryField((JTextArea) e.getSource());
				}
			}
			
		});
		this.add(jsp, cl);

		cl.gridx = 1; cl.gridy = 4;	
		cl.gridwidth = 3;
		cl.gridheight = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		primary_att.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( primary_att.getSelectedItem() != null && active_att_receiver != null 
						&& !primary_att.getSelectedItem().toString().startsWith("-")) {
					active_att_receiver.insert(primary_att.getSelectedItem().toString().split(" ")[0]
							                 , active_att_receiver.getCaretPosition());
				}
			}
		});
		primary_att.setToolTipText("Available keywords of the primary collection subset covered by the querty.");
		this.add(primary_att, cl);

		cl.gridx = 4; cl.gridy = 4;	
		cl.gridwidth = 3;
		cl.gridheight = 1;
		cl.fill   = GridBagConstraints.NONE;
		cl.anchor = GridBagConstraints.LINE_START;
		secondary_att.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( secondary_att.getSelectedItem() != null && active_att_receiver != null 
						&& !secondary_att.getSelectedItem().toString().startsWith("-")) {
					active_att_receiver.insert(secondary_att.getSelectedItem().toString().split(" ")[0]
							                 , active_att_receiver.getCaretPosition());
				}
			}
		});
		secondary_att.setToolTipText("Available keywords of the secondary collection subset covered by the querty.");
		this.add(secondary_att, cl);	
	
		this.addQualifierSetters();
		
	}

	/**
	 * 
	 */
	protected void addQualifierSetters() {
		GridBagConstraints cl = new GridBagConstraints();
		cl.insets = new Insets(5,5,5,5);
		cl.gridheight = 1;
		int cpt=1;
		for( Entry<String, QualifierSetter> e: qual_setter.entrySet()) {
			cl.gridx  = 0; cl.gridy = 4 + cpt;	
			cl.gridwidth = 1;
			cl.fill   = GridBagConstraints.NONE;
			cl.anchor = GridBagConstraints.LINE_START;
			this.add(e.getValue().label, cl);
			cl.gridx = 1; cl.gridy = 4 + cpt;	
			cl.gridwidth = 6;
			//cl.fill   = GridBagConstraints.NONE;
			cl.anchor = GridBagConstraints.LINE_START;
			JTextArea jtf = e.getValue().editor;
			jtf.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			this.add(jtf, cl);		
			jtf.setToolTipText("SQL statement computing the value of the qualifer <" + e.getKey() +">");
			jtf.addCaretListener(new CaretListener() { 
				
				public void caretUpdate(CaretEvent e) {
					CorrQueryEditor.this.activateQueryField((JTextArea) e.getSource());
				}			
			});
			cpt++;			
		}
	}

	/**
	 * @param prefix
	 */
	private void updateAvailableAttributes(String prefix) {
		String[] classes=null;;
		JTextField text=null;
		JComboBox combo = null;
		int cat;
		if( this.relation == null ) {
			return;
		}
		/*
		 * Get pointers on primary or secondary widgets
		 */
		if( prefix.equalsIgnoreCase("p.") ) {
			text = primary_classes;
			combo = primary_att;
			combo.removeAllItems();
			combo.addItem("- Primary Attributes -");
			cat = this.relation.getColPrimary_type();
		}
		else if( prefix.equalsIgnoreCase("s.") ) {
			text = secondary_classes;
			combo = secondary_att;
			combo.removeAllItems();
			combo.addItem("- Secondary Attributes -");
			cat = this.relation.getColSecondary_type();
		}
		else {
			return;
		}
		/*
		 * insert collection attributes
		 */
		classes = text.getText().split("\\s*,\\s*");
		for( AttributeHandler ah: MetaCollection.getAttribute_handlers(cat).values() ) {
			combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
		}
		/*
		 * insert class attributes
		 */
		if( classes.length == 1 && !classes[0].trim().equals("*") ) {
			try {
				MetaClass mcl = Database.getCachemeta().getClass(classes[0]);
				for( AttributeHandler ah: mcl.getAttributes_handlers().values() ) {
					combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
				}
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
				return;
			}
		}
	}
	
	/**
	 * 
	 */
	public void initMetaData() {
		if( this.relation != null ) {
			primary_combo.removeAllItems();
			try {
				primary_combo.addItem("ANY (" + Category.explain(this.relation.getColPrimary_type()) + ")" );
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
			}				
			for( String cl: Database.getCachemeta().getClassesOfCollection(this.relation.getColPrimary_name()
					                 , this.relation.getColPrimary_type()) ) {	
				primary_combo.addItem(cl);	
			}
			secondary_combo.removeAllItems();
			try {
				secondary_combo.addItem("ANY (" + Category.explain(this.relation.getColSecondary_type()) + ")" );
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
			}	
			for( String cl: Database.getCachemeta().getClassesOfCollection(this.relation.getColSecondary_name()
			         , this.relation.getColSecondary_type()) ) {
				secondary_combo.addItem(cl);	
			}
			
		}
		updateAvailableAttributes("p.");
		updateAvailableAttributes("s.");
	}

	/**
	 * @param conf
	 */
	public void load(RelationConf conf) {
		this.relation = conf;
		this.initMetaData();
		String correlator = relation.getQuery();
   		Pattern p = Pattern.compile("PrimaryFrom\\s+(.*)\\s+");
 		Matcher m = p.matcher(correlator);		
 		primary_classes.setText("");
  		if( m.find() ) {
 			primary_classes.setText(m.group(1));
 		}
   		p = Pattern.compile("SecondaryFrom\\s+(.*)\\s+");
 		m = p.matcher(correlator);		
 		secondary_classes.setText("");
 		if( m.find() ) {
 			secondary_classes.setText(m.group(1));
 		}
   		p = Pattern.compile("Condition\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
 		m = p.matcher(correlator);		
 		condition.setText("");
 		if( m.find() ) {
 			condition.setText(m.group(1));
 		}
 		/*
 		 * Parse Condition 1st neighbour 
 		 */
   		p = Pattern.compile("ConditionDist\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
 		m = p.matcher(correlator);		
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
		/*
 		 * Remove former qualifier setters
 		 */
 		for( QualifierSetter qs: this.qual_setter.values()) {
 			this.remove(qs.label);
  		}
 		qual_setter = new LinkedHashMap<String, QualifierSetter>();
 		for( String qual: relation.getQualifier().keySet() ) {
 	   		p = Pattern.compile("(" + qual + ")\\.Set\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
 	 		m = p.matcher(correlator);	
 	 		QualifierSetter qs = new QualifierSetter(qual);
 			this.qual_setter.put(qual, qs);
	 		
 	 		qs.editor.setText("");
	 		if( m.find() ) {
 	 			if( m.groupCount() > 1 ) {
 	 				qs.editor.setText(m.group(2));
 		 		}
 	 		}			
		}
 		
		this.addQualifierSetters();
		this.updateUI();
	}

	/**
	 * @return
	 */
	public String getCorrelator() {
		if( primary_classes.getText().trim().length() == 0 ) {
			primary_classes.setText("*");
		}
		if( secondary_classes.getText().trim().length() == 0 ) {
			secondary_classes.setText("*");
		}
		String retour =  "PrimaryFrom " + primary_classes.getText() + "\n"
		               + "SecondaryFrom " + secondary_classes.getText() + "\n";
		if( condition.getText().trim().length() > 0 ) {
			retour += "Condition{" + condition.getText() + "}\n";
		}
		//K-NN", "1st-NN
		if( knn_mode.getSelectedItem().toString().equals("K-NN") ) {
			if( knn_k.getText().length() == 0 ) {
				SaadaDBAdmin.showInputError(this.getParent(), "K value must be given");
				return "";
			}
			else if( knn_dist.getText().length() == 0 ) {
				SaadaDBAdmin.showInputError(this.getParent(), "A distance value must be given");
				return "";
			}
			else {
				retour += "ConditionKnn{" + knn_k.getText() + ", " + knn_dist.getText() + " [" +  knn_unit.getSelectedItem() + "]}\n";
			}
		}
		else if( knn_mode.getSelectedItem().toString().equals("1st-NN") ) {
			if( knn_dist.getText().length() == 0 ) {
				SaadaDBAdmin.showInputError(this.getParent(), "A distance value must be given");
				return "";
			}
			else {
				retour += "ConditionDist{" + knn_dist.getText() + " [" +  knn_unit.getSelectedItem() + "]}\n";
			}
		}
		for( Entry<String, QualifierSetter> e: this.qual_setter.entrySet()) {
			if( e.getValue().editor.getText().trim().length() > 0 )
				retour += e.getKey() + ".Set{" + e.getValue().editor.getText() + "}\n";
		}
		return retour;
	}

	/**
	 * 
	 */
	public void reset() {
		this.primary_classes.setText("");
		this.primary_combo.removeAllItems();
		this.primary_att.removeAllItems();
		this.secondary_classes.setText("");
		this.secondary_combo.removeAllItems();
		this.secondary_att.removeAllItems();
		this.condition.setText("");
		this.qual_setter = new LinkedHashMap<String, QualifierSetter>();
		this.active_att_receiver = null;
		this.relation = null;
		this.knn_mode.setSelectedIndex(0);
		this.knn_k.setText("");
		this.knn_dist.setText("");
	}
	
	/**
	 * 
	 */
	public void activateConditionField() {
		activateQueryField(this.condition);
	}
	
	/**
	 * @param field
	 */
	public void activateQueryField(JTextArea field) {
		if( active_att_receiver != null ) {
			active_att_receiver.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));					
		}
		if( field.isEditable() && field.isEnabled()) {
			active_att_receiver = field;
			active_att_receiver.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		}
	}
	
	/**
	 * @param active
	 */
	public void activateKNN(boolean active) {
		if( active ) {	
			this.knn_mode.setEnabled(true); 
			this.knn_unit.setEnabled(true);
			this.knn_k.setEnabled(true);
			this.knn_dist.setEnabled(true);
		}
		else {
			this.knn_mode.setEnabled(false); 
			this.knn_unit.setEnabled(false);
			this.knn_k.setEnabled(false);
			this.knn_dist.setEnabled(false);			
		}
	}
	/**
	 * @author michel
	 *
	 */
	class QualifierSetter {
		JTextArea editor;
		JLabel    label;
		
		QualifierSetter(String qual_name) {
			label = SaadaDBAdmin.getPlainLabel("Qualifier : <" + qual_name + ">");
			editor = new JTextArea(1, 20);					
			editor.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));					

		}
	}

	/**
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		primary_classes.setEditable(editable);
		primary_classes.setEnabled(editable);
		primary_combo.setEnabled(editable);
		primary_att.setEnabled(editable);
		secondary_classes.setEditable(editable);
		secondary_classes.setEnabled(editable);
		secondary_combo.setEnabled(editable);
		secondary_att.setEnabled(editable);
		condition.setEditable(editable);
		condition.setEnabled(editable);
		this.activateConditionField();
		knn_mode.setEnabled(editable);
		knn_unit.setEnabled(editable);
		knn_k.setEditable(editable);
		knn_dist.setEditable(editable);
	}
}
