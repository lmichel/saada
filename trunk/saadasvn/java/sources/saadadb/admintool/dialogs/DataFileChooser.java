package saadadb.admintool.dialogs;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;


import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.tree.VoDataProductTree;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.RegExp;

/**
 * @author michel
 * @version $Id$
 *
 * 04/2012: include rootList in directory list
 */
public class DataFileChooser extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTextField mask = new JTextField("", 16);
	private JComboBox combo_mask = new JComboBox(new Object[]{"Any", "FITS", "VOTables", "FITS or VOTables"});
	private JButton purge = new JButton("Remove");
	private JButton keep = new JButton("Keep");
	private JButton open = new JButton("Open the Selected File");

	private JList directories = new JList();
	private JList files = new JList();
	private JButton accept = new JButton("Load Files");
	private JButton cancel = new JButton("cancel");

	private static String current_dir = null;;
	private JLabel currentDirLabel = AdminComponent.getPlainLabel(current_dir);
	private boolean full_directory = true;

	public DataFileChooser(TaskPanel taskPanel, ArrayList<String> file_list) {
		super(((taskPanel != null)?taskPanel.getRootFrame(): null), true);
		if( current_dir == null ) {
			current_dir = (new File(Database.getRoot_dir())).getParent();
			currentDirLabel = AdminComponent.getPlainLabel(current_dir);
		}
		this.setResizable(false);
		this.setLayout(new GridBagLayout());	
		this.setTitle("Input Data Files Selector");
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;	
		c.insets = new Insets(5,3,5,3);
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("New Filename Mask (Reg Exp) "), c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 2;	
		this.add(mask, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Filename Masks"),c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 2;	
		this.add(combo_mask, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Data Files Selected from the List"),c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		this.add(purge, c);
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 2;
		this.add(keep, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Data Files Preview"),c);

		c.gridx = 1;
		c.gridwidth = 2;	
		c.anchor = GridBagConstraints.LINE_START;
		this.add(open, c);

		c.gridx = 0;
		c.gridwidth = 4;	
		c.gridy = 4;
		c.anchor = GridBagConstraints.LINE_START;
		this.add(currentDirLabel, c);

		c.gridx = 0;
		c.gridy = 5;	
		c.gridwidth = 3;	
		c.fill  = GridBagConstraints.BOTH;
		directories.setBorder(BorderFactory.createTitledBorder("Directories"));
		directories.setVisibleRowCount(10);
		JScrollPane jsp1 = new JScrollPane(directories);
		files.setBorder(BorderFactory.createTitledBorder("Data Files"));
		files.setVisibleRowCount(10);
		JScrollPane jsp2 = new JScrollPane(files);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsp1, jsp2);		
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);
		this.add(splitPane, c);

		c.fill  = GridBagConstraints.NONE;
		c.gridwidth = 1;	
		c.gridx = 1;
		c.gridy = 6;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(accept, c);
		c.gridx = 2;
		c.gridy = 6;	
		c.anchor = GridBagConstraints.LINE_START;
		this.add(cancel, c);

		this.setBehavior();
		this.setFileList(file_list);
		this.pack();
		this.setLocationRelativeTo(taskPanel);
		this.setVisible(true);			

	}

	/**
	 * Activate components
	 */
	private void setBehavior() {
		getRootPane().setDefaultButton(accept);
		DefaultListModel dlm = new DefaultListModel();
		dlm.addElement("..");

		files.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		directories.setModel(dlm);
		/*
		 * Double click on a directory: update file list
		 */
		directories.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 ) {
					/*
					 * User filter has priority on pre-set filters
					 */
					if( mask.getText().trim().length() > 0 ) {
						setDirectory(directories.getSelectedValue().toString(), mask.getText());						
					}
					else {
						setDirectory(directories.getSelectedValue().toString(), combo_mask.getSelectedItem().toString());
					}
				}
			}
		});

		/*
		 * Select a mask in the combo: update the file selection
		 */
		combo_mask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mask.setText("");
				setDirectory(".", combo_mask.getSelectedItem().toString());
			}			
		});
		/*
		 * Add a new mask
		 */
		mask.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				boolean found = false;
				String text = mask.getText();
				if( text.startsWith("*")) {
					text = "." + text; 
					mask.setText(text);
				}
				for( int i=0 ; i<combo_mask.getItemCount() ; i++ ){
					if( combo_mask.getItemAt(i).equals(text)) {
						found = true;
						break;
					}
				}
				if( !found ) {	
					//reduce size of other widgets.....in some circumstance
					//					DefaultComboBoxModel dcbm = (DefaultComboBoxModel) combo_mask.getModel();
					//					dcbm.addElement(mask.getText());
					//					dcbm.setSelectedItem(mask.getText());
				}
				setDirectory(".", mask.getText());
			}

		});		
		/*
		 * Remove selected file from the list
		 */
		purge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeSet<Integer> selected = new TreeSet<Integer>();
				int[] si = files.getSelectedIndices();
				for( Integer i: si) {
					selected.add(i);
				}
				DefaultListModel old_model = (DefaultListModel) files.getModel();
				DefaultListModel new_model = new DefaultListModel();
				int size = old_model.getSize();
				int cpt = 0;
				for( int i=0 ; i<size ; i++ ) {
					if( !selected.contains(i)) {
						new_model.addElement(old_model.getElementAt(i));
						cpt++;
					}
				}
				files.setModel(new_model);
				files.setBorder(BorderFactory.createTitledBorder( cpt + " Data File(s)"));
				full_directory = false;
			}

		});
		/*
		 * Remove selected file from the list
		 */
		keep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeSet<Integer> selected = new TreeSet<Integer>();
				int[] si = files.getSelectedIndices();
				for( Integer i: si) {
					selected.add(i);
				}
				DefaultListModel old_model = (DefaultListModel) files.getModel();
				DefaultListModel new_model = new DefaultListModel();
				int size = old_model.getSize();
				int cpt = 0;
				for( int i=0 ; i<size ; i++ ) {
					if( selected.contains(i)) {
						new_model.addElement(old_model.getElementAt(i));
						cpt++;
					}
				}
				files.setModel(new_model);
				files.setBorder(BorderFactory.createTitledBorder( cpt + " Data File(s)"));
				full_directory = false;
			}

		});

		/*
		 * Open a file preview
		 */
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( files.getSelectedIndices().length != 1 ) {
					AdminComponent.showInputError(DataFileChooser.this, "Select just one file");
				}
				else {
					String filename = current_dir + Database.getSepar() + files.getSelectedValue();
					JDialog window = new JDialog(DataFileChooser.this, new File(filename).getName(), true);
					VoDataProductTree vot;					
					vot = new VoDataProductTree(window
							, "ext/keywords"
							, filename);
					if( vot.flat_types != null ) {
						vot.drawTree(new Dimension(300, 500));
						vot.setPreferredSize(new Dimension(300, 500));
						window.add(vot);
						window.pack();
						window.setLocationRelativeTo(DataFileChooser.this.getOwner());
						window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
						window.setVisible(true);
					}					
				}
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//SaadaDBAdmin.current_dir = current_dir;
				//current_dir = null;
				setVisible(false);				
			}			
		});

		accept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//SaadaDBAdmin.current_dir = current_dir;
				setVisible(false);				
			}			
		});
		setDirectory(".", ".*");
	}

	/**
	 * Update both directories and file lists with the new directory;
	 * @param new_dir
	 */
	private void setDirectory(String new_dir, String filter) {
		File dir = null;
		boolean isRoot = false;
		DefaultListModel ddlm = new DefaultListModel();
		for( File f: File.listRoots() ) {
			if( new_dir.equals(f.getAbsolutePath()) ) {
				isRoot = true;
				dir = (new File(new_dir));
			}
			else {
				ddlm.addElement(f.getAbsolutePath());
			}
		}
		//ddlm.addElement(".");
		if( !isRoot) {
			if( new_dir.equals("..")) {
				dir = (new File(current_dir)).getParentFile();
			} else if( new_dir.equals(".")) {
				dir = new File(current_dir + System.getProperty("file.separator"));
			} else {
				dir = new File(current_dir + System.getProperty("file.separator") + new_dir);				
			}
		}
		current_dir = dir.getAbsolutePath();
		isRoot = false;
		for( File f: File.listRoots() ) {
			if( current_dir.equals(f.getAbsolutePath()) ) {
				isRoot = true;
			}
		}
		if( !isRoot) {
			ddlm.addElement("..");
		}

		DefaultListModel fdlm = new DefaultListModel();
		TreeSet<String> sorted_dirs = new TreeSet<String>();
		TreeSet<String> sorted_files = new TreeSet<String>();
		int cpt = 0;
		if( dir.isDirectory()) {
			String[] dir_content = dir.list();
			for( String f: dir_content) {
				if( f.startsWith(".")) {
					continue;
				}
				File tf = new File(current_dir + System.getProperty("file.separator") + f);
				if( tf.isDirectory()) {
					sorted_dirs.add(tf.getName());
				}
				else {
					//String filter = combo_mask.getSelectedItem().toString();
					if( filter.equalsIgnoreCase("any") || filter.equals(".*")) {
						/*
						 * All files contained in the directory are considered as selected while the user didn't make its own selection.
						 */
						full_directory = true;
						sorted_files.add(f);
						cpt++;
					}
					else if( filter.equalsIgnoreCase("FITS")) {
						full_directory = false;
						if( f.matches(RegExp.FITS_FILE) ) {
							sorted_files.add(f);
							cpt++;
						}
					}
					else if( filter.equalsIgnoreCase("VOTables") ) {
						full_directory = false;
						if( f.matches(RegExp.VOTABLE_FILE) ) {
							sorted_files.add(f);
							cpt++;
						}
					}
					else if( filter.equalsIgnoreCase("FITS or VOTables") ) {
						full_directory = false;
						if( f.matches(RegExp.VOTABLE_FILE) || f.matches(RegExp.FITS_FILE)) {
							sorted_files.add(f);
							cpt++;
						}
					}
					else if( f.matches(filter) ) {
						full_directory = false;
						sorted_files.add(f);
						cpt++;
					}
				}
			}
		}
		for(String f: sorted_dirs) {
			ddlm.addElement(f);
		}
		for(String f: sorted_files) {
			fdlm.addElement(f);
		}
		directories.setModel(ddlm);
		/*
		 * Make sure to have at least one empty element otherwise all widgets crush!
		 */
		if( fdlm.getSize() == 0 ) fdlm.addElement("");
		files.setModel(fdlm);
		files.setBorder(BorderFactory.createTitledBorder( cpt + " Data File(s)"));
		//directories.setBorder(BorderFactory.createTitledBorder(current_dir));
		currentDirLabel.setText(current_dir);
		/*
		 * All files contained in the directory are considered as selected while the user didn't make its own selection.
		 */

	}

	protected void setFileList(ArrayList<String> file_list) {
		if( file_list != null && file_list.size() > 0) {
			DefaultListModel fdlm = new DefaultListModel();
			int cpt=0;
			for( String f: file_list) {
				fdlm.addElement(f);
				cpt++;
			}
			files.setModel(fdlm);
			files.setBorder(BorderFactory.createTitledBorder( cpt + " Data File(s)"));
		}
	}
	/**
	 * @return
	 */
	public String getCurrentDir() {
		return current_dir;
	}

	/**
	 * @return
	 */
	public int isFullDirectory() {
		if( full_directory ) {
			return files.getModel().getSize();
		}
		else {
			return 0;
		}
	}

	/**
	 * @return Returns the content of the file list
	 */
	public ArrayList<String> getSelectedDataFiles() {
		if( current_dir == null  ) {
			return null;
		}
		else {
			ArrayList<String>  retour = new ArrayList<String>();
			DefaultListModel dlm = ((DefaultListModel)files.getModel());
			int[] selected_indices = files.getSelectedIndices();
			/*
			 * If no file are selected, we suppose that all are to be loaded
			 */
			if( selected_indices.length == 0) {	

				int size = ((DefaultListModel)files.getModel()).getSize();
				for( int i=0 ; i<size ; i++ ) {
					String fn = dlm.get(i).toString();

					/*
					 * Remove the empty string set to avoid widget crushing
					 */
					if(fn.length() > 0 ) {
						retour.add(fn);
					}
					//					}
				}
			}
			/*
			 * Else we only keep tye selected ones
			 */
			else {
				full_directory = false;
				for( int s: selected_indices) {
					String fn = dlm.get(s).toString();
					/*
					 * Remove the empty string put to avoid widget crushing
					 */
					if(fn.length() > 0 ) {
						retour.add(fn);
					}
				}
			}
			return retour;
		}
	}

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws FatalException {
		Database.init((new ArgsParser(args)).getDBName());
		DataFileChooser dfc = new DataFileChooser(null, null);
		System.out.println(dfc.isFullDirectory() + " " + dfc.getCurrentDir() + " " + dfc.getSelectedDataFiles());
	}
}
