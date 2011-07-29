package saadadb.admin.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import saadadb.admin.SaadaDBAdmin;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;

public class DataLoaderDefaultRunner extends JDialog implements ActionListener,
		PropertyChangeListener {
	/** * @version $Id$

	 * 
	 */
	protected static final long serialVersionUID = 1L;
	
	protected JOptionPane optionPane;
	protected SaadaDBAdmin admintool;
	protected JRadioButton copy = new JRadioButton("Copy Input Files into the Repository");
	protected JRadioButton move = new JRadioButton("Move Input Files to the Repository");
	protected JRadioButton keep = new JRadioButton("Use Input Files as Repository");
	protected JRadioButton no_index = new JRadioButton("Do not rebuild indexes after loading");
	
	protected int category;
	protected String collection;
	
	protected JTextField directory = new JTextField(32);
	protected JButton dir_browser = new JButton("Browse...");
	protected JLabel datafile_summary = new JLabel("no file selected");
	protected String btnString1 = "Run Dataloader";
	protected String btnString2 = "Cancel";

	protected String typed_dir;
	protected ArrayList<String> selected_files;
	/**
	 * @param aFrame
	 * @param title
	 * @param directory
	 * @param collection 
	 * @param cat
	 * @param conf_name
	 */
	public DataLoaderDefaultRunner(SaadaDBAdmin aFrame, String title, String directory, String collection, int cat) {
		super(aFrame, true);
		this.collection = collection;
		/*
		 * Directory browser
		 */
		dir_browser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				DataFileChooser filechooser;
				filechooser = new DataFileChooser(admintool, selected_files);										
				DataLoaderDefaultRunner.this.directory.setText(filechooser.getCurrentDir());
				selected_files = filechooser.getSelectedDataFiles();
				if( selected_files != null ) {
					int dir_content;
					if(  (dir_content = filechooser.isFullDirectory()) > 0 ) {
						datafile_summary.setText("The whole directory content: " + selected_files.size() + " files");
					}
					else {
						datafile_summary.setText(selected_files.size() + " file(s) selected.");
					}
				}
				else {
					datafile_summary.setText("No file selected");
					SaadaDBAdmin.showInputError(DataLoaderDefaultRunner.this, "No Selected Data Files!");
				}
			}		
		});
		init(aFrame, title, directory, cat);
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
		 * Repository strategy buttons
		 */
		ButtonGroup bg   = new ButtonGroup();
		copy.setSelected(true);
		bg.add(copy);
		bg.add(move);
		bg.add(keep);
		/*
		 * Indexation strategy
		 */
		/*
		 * Pan building
		 */
		Object[] options = {btnString1, btnString2};
		Object[] array = {"<html><BR>Repository Strategy", copy, "", move, "", keep
				        , "<html><BR>Indexation Strategy", no_index
					    , "<html><BR>Data Directory", this.directory
					    , "", dir_browser
					    , "", datafile_summary};
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
		optionPane.addPropertyChangeListener(this);
		this.pack();
		this.setLocationRelativeTo(admintool);
	    this.setVisible(true);		
	}

	

	/** This method handles events for the text field. */
	public void actionPerformed(ActionEvent e) {
		optionPane.setValue(btnString1);
	}
	
	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		
		if (isVisible()
				&& (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) ||
						JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();
			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				return;
			}			
			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			if (btnString1.equals(value)) {
				checkParameters();
			} else { //user closed dialog or clicked cancel
				typed_dir = null;
				clearAndHide();
			}
		}
	}
	
	/**
	 * 
	 */
	protected void checkParameters(){
		typed_dir = this.directory.getText().trim();
		if( typed_dir.length() == 0  ) {
			
			JOptionPane.showMessageDialog(DataLoaderDefaultRunner.this,
					"Please give a data directory or filename",
					"",
					JOptionPane.ERROR_MESSAGE);
		}
		else if( !(new File(typed_dir)).exists() ) {
			JOptionPane.showMessageDialog(DataLoaderDefaultRunner.this,
					"Directory or file <" + typed_dir + "> does not exist",
					"",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			String msg;
			if( selected_files.size() == 0 ) {
				msg = "You are going to to load the whole content of the directory";
			}
			else {
				msg = "Your are going to start loading " + selected_files.size() + " files";
			}
			if( !SaadaDBAdmin.showConfirmDialog(DataLoaderDefaultRunner.this, msg) ) {
				typed_dir = null;
			}
			clearAndHide();
		}
	}

	/**
	 * @return
	 */
	public int getRepositoryMode() {
		if( copy.isSelected() ) {
			return ConfigurationDefaultHandler.COPY;
		}
		else if( move.isSelected() ) {
			return ConfigurationDefaultHandler.MOVE;
		}
		else {
			return ConfigurationDefaultHandler.KEEP;			
		}
	}

	
	/**
	 * @return
	 */
	public String getTyped_dir() {
		return typed_dir ;
	}

	/**
	 * 
	 */
	public void clearAndHide() {
		setVisible(false);
	}

	/**
	 * @return Returns the selected_files.
	 */
	public ArrayList<String> getSelected_files() {
		return selected_files;
	}
}

