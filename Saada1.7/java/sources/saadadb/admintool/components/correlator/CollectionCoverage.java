package saadadb.admintool.components.correlator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;


public class CollectionCoverage extends CollapsiblePanel {
	public JTextField primary_classes = new JTextField(20);
	public JComboBox primary_combo = new JComboBox();
	public JTextField secondary_classes = new JTextField(20);
	public JComboBox secondary_combo = new JComboBox();
	private RelationPopulatePanel taskPanel;
	// Use so as to know if something can be modified and call the notifyChange function of AdminComponent
	private boolean somethingCanChanged; 
	
	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public CollectionCoverage(RelationPopulatePanel taskPanel, Component toActive) {
		super("Limit the join to a subset of the collection end-points ");
		this.taskPanel = taskPanel;
		somethingCanChanged = true;
		primary_classes = new JTextField(20);
		primary_classes.setToolTipText("Subset of the primary collection covered by the join query");

		primary_combo = new JComboBox();
		primary_combo.setToolTipText("List of sub-classes of the primary collection - transfered to the text field when selected");
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
					} else if( ct.length() == 0 || ct.equals("*")){
						primary_classes.setText(si);			
					} else {
						primary_classes.setText((ct + "," + si).replaceAll(",,", ","));			
					}
					CollectionCoverage.this.taskPanel.updateAvailableAttributes();
					if (somethingCanChanged)
						CollectionCoverage.this.taskPanel.notifyChange();
				}
			}	
		});

		secondary_classes = new JTextField(20);
		secondary_classes.setToolTipText("Subset of the secondary collection covered by the join query");

		secondary_combo = new JComboBox();
		secondary_combo.setToolTipText("List of sub-classes of the secondary collection - transfered to the text field when selected");
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
					} else if( ct.length() == 0 || ct.equals("*")){
						secondary_classes.setText(si);			
					} else {
						secondary_classes.setText((ct + "," + si).replaceAll(",,", ","));			
					}
					CollectionCoverage.this.taskPanel.updateAvailableAttributes();
					if (somethingCanChanged)
						CollectionCoverage.this.taskPanel.notifyChange();
				}
			}	
		});

		MyGBC mc = new MyGBC(5,5,5,5); mc.anchor = GridBagConstraints.NORTH;
		JPanel panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		mc.right(false);
		panel.add(AdminComponent.getPlainLabel("Primary"), mc);
		mc.next();mc.left(false);
		mc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(primary_combo, mc);
		mc.fill = GridBagConstraints.NONE;
		mc.rowEnd();
		panel.add(primary_classes, mc);
		mc.newRow();mc.right(false);
		panel.add(AdminComponent.getPlainLabel("Secondary"), mc);
		mc.next();mc.left(false);
		mc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(secondary_combo, mc);
		mc.fill = GridBagConstraints.NONE;
		mc.rowEnd();
		panel.add(secondary_classes, mc);
		mc.newRow();mc.right(false);

	}
	
	/**
	 * @param conf
	 */
	public void load(RelationConf conf) {
		somethingCanChanged = false;
		initMetaData(conf);
		String correlator = conf.getQuery();
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
 		somethingCanChanged = true;
	}
	
	/**
	 * 
	 */
	private void initMetaData(RelationConf conf) {
		if( conf != null ) {
			primary_combo.removeAllItems();
			try {
				primary_combo.addItem("ANY (" + Category.explain(conf.getColPrimary_type()) + ")" );
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
			}				
			for( String cl: Database.getCachemeta().getClassesOfCollection(conf.getColPrimary_name()
					                 ,conf.getColPrimary_type()) ) {	
				primary_combo.addItem(cl);	
			}
			secondary_combo.removeAllItems();
			try {
				secondary_combo.addItem("ANY (" + Category.explain(conf.getColSecondary_type()) + ")" );
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
			}	
			for( String cl: Database.getCachemeta().getClassesOfCollection(conf.getColSecondary_name()
			         , conf.getColSecondary_type()) ) {
				secondary_combo.addItem(cl);	
			}
		}
	}

	/**
	 * @return
	 */
	public String getPrimaryCLasses() {
		return primary_classes.getText();
	}
	/**
	 * @return
	 */
	public String getSecondaryCLasses() {
		return secondary_classes.getText();
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
		return "PrimaryFrom " + primary_classes.getText() + "\n"
		               + "SecondaryFrom " + secondary_classes.getText() + "\n";
	}

	public void reset() {
		this.primary_classes.setText("");
		this.primary_combo.removeAllItems();
		this.secondary_classes.setText("");
		this.secondary_combo.removeAllItems();
	}
}
