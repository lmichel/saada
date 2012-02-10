package saadadb.admintool.components.correlator;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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


public class KeywordConditionEditor extends CollapsiblePanel {
	private JComboBox primary_att = new JComboBox();
	private JComboBox secondary_att = new JComboBox();
	private JTextArea condition = new JTextArea(12, 48);
	private RelationPopulatePanel taskPanel;
	private RelationConf relationConf;

	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public KeywordConditionEditor(RelationPopulatePanel taskPanel, Component toActive) {
		super("Condition based on Keywords");
		this.taskPanel = taskPanel;
		this.condition.setToolTipText("Primary and secondary records are linked when the condition (SQL statement) is true.");
		primary_att.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( primary_att.getSelectedItem() != null && !primary_att.getSelectedItem().toString().startsWith("-")) {
					condition.insert(primary_att.getSelectedItem().toString().split(" ")[0]
					                 , condition.getCaretPosition());
				}
			}
		});
		secondary_att.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( secondary_att.getSelectedItem() != null && !secondary_att.getSelectedItem().toString().startsWith("-")) {
					condition.insert(secondary_att.getSelectedItem().toString().split(" ")[0]
					                 , condition.getCaretPosition());
				}
			}
		});

		MyGBC mc = new MyGBC(5,5,5,5); 

		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		mc.gridwidth = 6;
		panel.add(new JScrollPane(condition), mc);
		mc.newRow(); mc.gridwidth = 2;
		panel.add(primary_att, mc);
		mc.gridx = 2;mc.gridwidth = 2;
		panel.add(secondary_att, mc);
		mc.gridx = 4;mc.gridwidth = 2;
		panel.add(new SQLConditionHelper(taskPanel.rootFrame, condition), mc);
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
				combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
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
							combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
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
	 * @param conf
	 */
	public void load(RelationConf conf) {
		this.relationConf = conf;
		String correlator = conf.getQuery();
		Pattern p = Pattern.compile("Condition\\s*\\{([^\\{\\}]*)\\}", Pattern.DOTALL);
		Matcher m = p.matcher(correlator);		
		condition.setText("");
		if( m.find() ) {
			condition.setText(m.group(1));
		}
	}

	/**
	 * @return
	 */
	public String getCorrelator() {
		if( condition.getText().trim().length() > 0 ) {
			return "Condition{" + condition.getText() + "}\n";
		}
		else {
			return "";
		}
	}

	/**
	 * 
	 */
	public void reset() {
		this.primary_att.removeAllItems();
		this.secondary_att.removeAllItems();
		this.condition.setText("");
	}

}
