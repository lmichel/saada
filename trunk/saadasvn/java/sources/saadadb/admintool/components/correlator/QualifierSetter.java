package saadadb.admintool.components.correlator;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.util.Messenger;


public class QualifierSetter extends CollapsiblePanel {
	private RelationPopulatePanel taskPanel;
	private Map<String, QualifierEditor> qual_setter;
	public JComboBox primary_att = new JComboBox();
	public JComboBox secondary_att = new JComboBox();
	public JTextArea active_att_receiver;
	private RelationConf relationConf;

	/**
	 * @param taskPanel
	 */
	public QualifierSetter(RelationPopulatePanel taskPanel) {
		super("Qualifier Setter");
		this.taskPanel = taskPanel;
		primary_att.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( active_att_receiver != null && primary_att.getSelectedItem() != null && !primary_att.getSelectedItem().toString().startsWith("-")) {
					active_att_receiver.insert(primary_att.getSelectedItem().toString().split(" ")[0]
					                                                                               , active_att_receiver.getCaretPosition());
				}
			}
		});
		secondary_att.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( active_att_receiver != null && secondary_att.getSelectedItem() != null && !secondary_att.getSelectedItem().toString().startsWith("-")) {
					active_att_receiver.insert(secondary_att.getSelectedItem().toString().split(" ")[0]
					                                                                                 , active_att_receiver.getCaretPosition());
				}
			}
		});

	}


	/**
	 * @param relationConf
	 */
	public void load(RelationConf relationConf){
		this.relationConf = relationConf;
		String correlator = relationConf.getQuery();
		/*
		 * Remove former qualifier setters
		 */
		for( QualifierEditor qs: this.qual_setter.values()) {
			this.remove(qs.label);
		}
		qual_setter = new LinkedHashMap<String, QualifierEditor>();
		for( String qual: relationConf.getQualifier().keySet() ) {
			Pattern p = Pattern.compile("(" + qual + ")\\.Set\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
			Matcher m = p.matcher(correlator);	
			QualifierEditor qs = new QualifierEditor(qual);
			this.qual_setter.put(qual, qs);

			qs.editor.setText("");
			if( m.find() ) {
				if( m.groupCount() > 1 ) {
					qs.editor.setText(m.group(2));
				}
			}			
		}
		addQualifierSetters();
	}

	/**
	 * 
	 */
	private void addQualifierSetters() {
		MyGBC mc = new MyGBC(5,5,5,5); 
		JPanel panel = this.getContentPane();
		panel.removeAll();
		panel.setLayout(new GridBagLayout());

		int cpt=1;
		for( Entry<String, QualifierEditor> e: qual_setter.entrySet()) {
			mc.right(false);
			panel.add(e.getValue().label, mc);

			JTextArea jtf = e.getValue().editor;
			jtf.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
			jtf.setToolTipText("SQL statement computing the value of the qualifer <" + e.getKey() +">");
			jtf.addCaretListener(new CaretListener() { 
				public void caretUpdate(CaretEvent e) {
					QualifierSetter.this.activateQueryField((JTextArea) e.getSource());
				}			
			});
			mc.rowEnd();mc.left(true);
			panel.add(jtf, mc);		
			mc.newRow();
			cpt++;			
		}
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(primary_att);
		p.add(secondary_att);
		mc.gridwidth = 2;
		panel.add(p, mc);
	}

	/**
	 * @param primaryClass
	 * @param secondaryClass
	 */
	public void updateAvailableAttributes(String primaryClass, String secondaryClass) {
		String[] classes=null;;
		String text=null;
		JComboBox combo = null;
		int cat;
		if( this.relationConf == null ) {
			return;
		}
		for( String prefix: new String[]{"p.", "s."}) {
			/*
			 * Get pointers on primary or secondary widgets
			 */
			if( prefix.equalsIgnoreCase("p.") ) {
				text = primaryClass;
				combo = primary_att;
				combo.removeAllItems();
				combo.addItem("- Primary Attributes -");
				cat = this.relationConf.getColPrimary_type();
			}
			else if( prefix.equalsIgnoreCase("s.") ) {
				text = secondaryClass;
				combo = secondary_att;
				combo.removeAllItems();
				combo.addItem("- Secondary Attributes -");
				cat = this.relationConf.getColSecondary_type();
			}
			else {
				return;
			}
			/*
			 * insert collection attributes
			 */

			for( AttributeHandler ah: MetaCollection.getAttribute_handlers(cat).values() ) {
				if( !ah.getType().equals("String") && !ah.getType().equals("boolean") ) {
					combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
				}
			}
			if( text.length() > 0 ) {
				classes = text.split("\\s*,\\s*", 0);
				/*
				 * insert class attributes
				 */
				if( classes.length == 1 && !classes[0].trim().equals("*") ) {
					try {
						MetaClass mcl = Database.getCachemeta().getClass(classes[0]);
						for( AttributeHandler ah: mcl.getAttributes_handlers().values() ) {
							if( !ah.getType().equals("String") && !ah.getType().equals("boolean") ) {
								combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
							}
						}
					} catch (SaadaException e) {
						Messenger.printStackTrace(e);
						return;
					}
				}
			}
		}
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
	 * @return
	 */
	public String getCorrelator() {
		String retour = "";
		for( Entry<String, QualifierEditor> e: this.qual_setter.entrySet()) {
			if( e.getValue().editor.getText().trim().length() > 0 )
				retour += e.getKey() + ".Set{" + e.getValue().editor.getText() + "}\n";
		}
		return retour;
	}

	public void reset() {
		this.getContentPane().removeAll();
		this.qual_setter = new LinkedHashMap<String, QualifierEditor>();
		this.setVisible(false);
	}

	class QualifierEditor {
		JTextArea editor;
		JLabel    label;

		QualifierEditor(String qual_name) {
			label = SaadaDBAdmin.getPlainLabel(qual_name);
			editor = new JTextArea(1, 20);					
			editor.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));					

		}
	}


}
