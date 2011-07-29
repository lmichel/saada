package saadadb.admin.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

public class DataLoaderRunner extends DataLoaderDefaultRunner implements ActionListener,
		PropertyChangeListener {
	/**
	 *  * @version $Id$

	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private JTextField conf_name;
	private JButton conf_browser;
	private JCheckBox use_conf;
	private JLabel conf_label1 ;
	private JLabel conf_label2;
	private JLabel conf_label3;
	private JLabel show_param;
	/**
	 * @param aFrame
	 * @param title
	 * @param directory
	 * @param cat
	 * @param conf_name
	 */
	public DataLoaderRunner(SaadaDBAdmin aFrame, String title, String directory, String collection, int cat) {
		super(aFrame, title, directory, collection, cat);
	}
	
	/**
	 * @param aFrame
	 * @param title
	 * @param directory
	 * @param cat
	 * @param conf_name
	 */
	protected void init(SaadaDBAdmin aFrame, String title, String directory, int cat) {
		setTitle(title);
		this.admintool = aFrame;
		this.category = cat;
		this.directory.setText(directory);
		
		/*
		 * Set the config with that shown on the config editor panel
		 */
		this.conf_name = new JTextField(32);
		this.conf_browser = new JButton("Browse...");
		this.conf_name.setEditable(false);
		this.conf_name.setText("");
		/*
		 * Label URL like showing dataloader parameter
		 */
		show_param = SaadaDBAdmin.getPlainLabel("<HTML><A HREF=>Show Loader Parameters Matching this Configuation</A>");
		show_param.setToolTipText("Show dataloader parameters matching the current configuration.");
		show_param.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e) {
				
				try {
					String[] ca = getLoaderArgs();
					String summary = "";
					for( String a: ca ) {
						summary += a + "\n";
					}
					SaadaDBAdmin.showCopiableInfo(admintool, summary, "Loader Parameters");
				} catch (FatalException e1) {
					e1.printStackTrace();
					Messenger.trapFatalException(e1);
				}
			}
		});
		/*
		 * Browser for config files
		 */
		conf_browser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {	
				DialogConfigFileChooser dial;
				try {
					dial = new DialogConfigFileChooser(category);
					String conf_path = dial.open(admintool);
					if( conf_path.length() > 0 ) {
						admintool.getDlconf_panel().loadConfFile(conf_path);
						ArgsParser ap = admintool.getDlconf_panel().getArgsParser(Category.explain(category));	
						admintool.showConfPanel(category);
						if( ap == null ){
							SaadaDBAdmin.showFatalError(admintool, "Config file not valid");
							return;
						}
						DataLoaderRunner.this.conf_name.setText(ap.getName());
					}
				} catch (SaadaException e1) {
					SaadaDBAdmin.showFatalError(admintool, e1);
				}
			}
		});				
		/*
		 * Repository strategy buttons
		 */
		ButtonGroup bg   = new ButtonGroup();
		copy.setSelected(true);
		bg.add(copy);
		bg.add(move);
		bg.add(keep);
		use_conf = new JCheckBox("Use a customized configuration");
		/*
		 * Class creation fails (of course) for flatfiles. For this reason
		 * the use of configuration is disabled here 
		 */
		if( category == Category.FLATFILE) {
			use_conf.setEnabled(false);
		}
		conf_label1 = new JLabel("Configuration Name:");
		conf_label2 = SaadaDBAdmin.getPlainLabel("The configuration must exist prior to load data");
		conf_label3 = SaadaDBAdmin.getPlainLabel("It can be created with the Dataloader Configuration tab.");
		conf_name.setEnabled(false);
		conf_browser.setEnabled(false);				
		conf_label1.setEnabled(false);
		conf_label2.setEnabled(false);
		conf_label3.setEnabled(false);
		
		use_conf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( use_conf.isSelected() ) {
					admintool.showConfPanel(category);
					conf_name.setEnabled(true);
					conf_browser.setEnabled(true);
					conf_label1.setEnabled(true);
					conf_label2.setEnabled(true);
					conf_label3.setEnabled(true);
				}
				else {
					conf_name.setEnabled(false);
					conf_browser.setEnabled(false);				
					conf_label1.setEnabled(false);
					conf_label2.setEnabled(false);
					conf_label3.setEnabled(false);
				}
			}			
		});
		/*
		 * Pane building
		 */
		Object[] options = {btnString1, btnString2};
		Object[] array = {"", use_conf
				        , conf_label1, this.conf_name
				        , conf_label2, conf_label3, conf_browser
				        , "<html><BR>Repository Strategy", copy, "", move, "", keep
				        , "<html><BR>Indexation Strategy", no_index
					    , "<html><BR>Data Directory", this.directory
					    , "", dir_browser
					    , "", datafile_summary
				        , "", show_param
				        };
		optionPane = new JOptionPane(array
					, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION
					, null, options, options[0]);
		setContentPane(optionPane);
		/*
		 * Pan behavior setup
		 */
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window,
				 * we're going to change the JOptionPane's
				 * value property.
				 */
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});
		/*
		 * 
		 */
		if( use_conf.isSelected() ) {
			ArgsParser ap = admintool.getDlconf_panel().getArgsParser(this.category);
			if( ap!= null ) {
				if( admintool.getDlconf_panel().getMapping_panel().hasChanged() ) {
					if( SaadaDBAdmin.showConfirmDialog(admintool
							, "The configuration <" + ap.getName() + "> has been modified. Would you like to save it?") ) {
						admintool.getDlconf_panel().getMapping_panel().save();
						this.conf_name.setText(ap.getName());
					}
					
				}
				else {
					this.conf_name.setText(ap.getName());
				}
			}
		}
		optionPane.addPropertyChangeListener(this);
		this.pack();
		this.setLocationRelativeTo(admintool);
	    this.setVisible(true);		
	}

	/**
	 * 
	 */
	protected void checkParameters(){
		typed_dir = this.directory.getText().trim();
		if( use_conf.isSelected() && conf_name.getText().length() == 0 ) {
			JOptionPane.showMessageDialog(this,
					"Please select a configuration or uncheck the customized configuration mode button.",
					"",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			super.checkParameters();
		}
	}
	
	/**
	 * @return
	 * @throws FatalException
	 */
	public String[] getLoaderArgs() throws FatalException {
		boolean dm =  Messenger.debug_mode;
		String rep = "";
		if( move.isSelected() ) {
			rep = "move";
		}
		else if( keep.isSelected() ) {
			rep = "no";					
		}
	
		ArgsParser args_parser;
		if( use_conf.isSelected() ) {
			if( conf_name.getText().length() == 0 ){
				SaadaDBAdmin.showFatalError(admintool, "Edit or load a configuration please.");
				return null;
			}
			args_parser = admintool.getDlconf_panel().getArgsParser(category);
		}
		else {
			args_parser = new ArgsParser(new String[]{"-category=" + Category.explain(category)});
		}
		boolean ni = false;
		if( no_index.isSelected() ) {
			ni = true;
		}
		return  args_parser.completeArgs(DataLoaderRunner.this.collection, DataLoaderRunner.this.directory.getText(), rep, ni, dm);
	}

	public static void main(String[] args) {
		DataLoaderRunner dlr = new DataLoaderRunner(null, "QWERTY", "." , "", Category.ENTRY);
	}
}

