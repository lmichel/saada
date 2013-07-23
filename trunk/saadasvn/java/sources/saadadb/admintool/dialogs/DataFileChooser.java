package saadadb.admintool.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.metal.MetalIconFactory;

import org.jdesktop.swingx.JXTitledPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.tree.VoDataProductTree;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;
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
	private JButton cancel = new JButton("Cancel");
	
	private JXTitledPanel titleDirectory, titleFile;

	private static String current_dir = null;;
	private JDirectoryPathComboBox currentDirComboBox;
	private boolean full_directory = true;

	public DataFileChooser(TaskPanel taskPanel, ArrayList<String> file_list) {
		super(((taskPanel != null)?taskPanel.getRootFrame(): null), true);
		if( current_dir == null ) {
			current_dir = (new File(Database.getRoot_dir())).getParent();
		}
		this.setResizable(false);
		this.setLayout(new GridBagLayout());	
		this.setTitle("Input Data Files Selector");
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,3,5,3);
	
		c.gridx = 0;
		c.gridwidth = 1;	
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Current folder : "), c);
		
		c.gridx = 1;
		c.gridwidth = 6;	
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		currentDirComboBox = new JDirectoryPathComboBox(current_dir);
		currentDirComboBox.setRenderer(new DirectoryPathRenderer());	
		JScrollPane jcdc = new JScrollPane(currentDirComboBox);
		this.add(jcdc, c);

		// Shortcuts panel ands its components
		c.gridx = 0;
		c.gridy = 1;	
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		JShortcutsPanel shortcutsPanel = new JShortcutsPanel();
		JScrollPane jscp = new JScrollPane(shortcutsPanel);
		JXTitledPanel titleShortcuts = new JXTitledPanel("Shortcuts", jscp);
		this.add(titleShortcuts, c);
		
		c.gridx = 1;
		c.gridy = 1;	
		c.gridwidth = 6;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill  = GridBagConstraints.BOTH;
		directories.setVisibleRowCount(12);
		JScrollPane jsp1 = new JScrollPane(directories);
		titleDirectory = new JXTitledPanel("Directories", jsp1);
		
		files.setVisibleRowCount(12);
		JScrollPane jsp2 = new JScrollPane(files);
		titleFile = new JXTitledPanel("Data Files", jsp2);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, titleDirectory, titleFile);		
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(170);
		splitPane.setPreferredSize(new Dimension(400,220));
		this.add(splitPane, c);
		
		c.gridx = 1;
		c.gridy = 2;	
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("New Filename Mask (Reg Exp) "), c);

		c.gridx = 4;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 2;	
		this.add(mask, c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 3;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Filename Masks"),c);

		c.gridx = 4;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridwidth = 2;	
		this.add(combo_mask, c);

		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 3;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Data Files Selected from the List"),c);

		c.gridx = 4;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		this.add(purge, c);
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 5;
		this.add(keep, c);

		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 4;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("Data Files Preview"),c);

		c.gridx = 4;
		c.gridwidth = 2;	
		c.anchor = GridBagConstraints.LINE_START;
		this.add(open, c);

		c.ipady = 5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;	
		c.gridx = 6;
		c.gridy = 6;	
		c.anchor = GridBagConstraints.LINE_END;
		this.add(accept, c);
		c.ipady = 0;
		c.gridx = 6;
		c.gridy = 7;	
		c.anchor = GridBagConstraints.LINE_END;
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
						setDirectory(directories.getSelectedValue().toString(), mask.getText(), false);						
					}
					else {
						setDirectory(directories.getSelectedValue().toString(), combo_mask.getSelectedItem().toString(), false);
					}
					currentDirComboBox.updateCurrentDir(current_dir);
				}
			}
		});

		/*
		 * Select a mask in the combo: update the file selection
		 */
		combo_mask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mask.setText("");
				setDirectory(".", combo_mask.getSelectedItem().toString(), false);
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
				setDirectory(".", mask.getText(), false);
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
				titleFile.setTitle(cpt + (cpt>1?" Data Files":" Data File"));
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
				titleFile.setTitle(cpt + (cpt>1?" Data Files":" Data File"));
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
		
		currentDirComboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int selectedIndex = currentDirComboBox.getSelectedIndex();
				if (selectedIndex != -1)
				{
					String hiddenItem = currentDirComboBox.hiddenList.get(selectedIndex);
					/* The method isReallyChanged is really important and must stay here because it enables not 
					 * to trigger actionPerformed event triggered by setModel and setSelectedIndex from the 
					 * updateCurrentDir method. If removed, the behavior of the widget isn't safe.
					 */
					if (currentDirComboBox.isReallyChanged()) // only true when the user really really changes the selected value
					{
						if( mask.getText().trim().length() > 0 ) 
						{
							setDirectory(hiddenItem, mask.getText(), true);						
						}
						else {
							setDirectory(hiddenItem, combo_mask.getSelectedItem().toString(), true);
						}
						currentDirComboBox.updateCurrentDir(current_dir);
					}
					
				}
			}
		});
		setDirectory(".", ".*", false);

	}

	/**
	 * Update both directories and file lists with the new directory;
	 * @param new_dir
	 */
	private void setDirectory(String new_dir, String filter, boolean isAbsolutePath) {
		//Messenger.printMsg(Messenger.DEBUG, "setDirectory : " + new_dir + " - Current_dir : " + current_dir);
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
		if (isAbsolutePath)
		{
			dir = new File(new_dir);
		}
		else
		{
			if( !isRoot) 
			{
				if( new_dir.equals("..")) {
					dir = (new File(current_dir)).getParentFile();
				} else if( new_dir.equals(".")) {
					dir = new File(current_dir + System.getProperty("file.separator"));
				} else {
					dir = new File(current_dir + System.getProperty("file.separator") + new_dir);				
				}
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
		titleFile.setTitle(cpt + (cpt>1?" Data Files":" Data File"));

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
			titleFile.setTitle(cpt + (cpt>1?" Data Files":" Data File"));
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
	
	public class JDirectoryPathComboBox extends JComboBox
	{
		private DefaultComboBoxModel<String> cbm;
		public Vector<String> displayList, hiddenList;
		private boolean isReallyChanged = false;
		
		private JDirectoryPathComboBox()
		{
			super();
			cbm = new DefaultComboBoxModel<String>();
			displayList = new Vector<String>();
			hiddenList = new Vector<String>();
			this.setBackground(Color.WHITE);
			this.setModel(cbm);
		}
		
		public boolean isReallyChanged() 
		{
			return isReallyChanged;
		}

		public JDirectoryPathComboBox(String currentDir)
		{
			this();
			this.updateCurrentDir(currentDir);
		}
		
		public void updateCurrentDir(String currentDir)
		{
			isReallyChanged = false; // isReallyChanged is false because of setModel
			displayList = new Vector<String>();
			hiddenList = new Vector<String>();
			String tmpDisplay = "", tmpHidden = "", spaceNumber = "";
			
			String[] tabDirectories = currentDir.split(Database.getSepar());
			String test = "                                                                                                              -        ---                    ";
			if (tabDirectories.length>0)
			{
				for (int i=0 ; i<tabDirectories.length ; i++)
				{
					tmpHidden += Database.getSepar() + tabDirectories[i];
					tmpDisplay =  spaceNumber + (i==0&&tabDirectories[i].compareTo("")==0?"":Database.getSepar()) + tabDirectories[i] + Database.getSepar();
					spaceNumber += "   ";
					hiddenList.add(i, tmpHidden);
					displayList.add(i, tmpDisplay);
				}
			}
			else // Root case (at least for linux)
			{
				hiddenList.add(0, Database.getSepar()+"");
				displayList.add(0, Database.getSepar()+"");
			}
			this.removeAllItems();
			this.setModel(displayList);
		}
		
		private void setModel(Vector<String> vector)
		{
			isReallyChanged = false; // isReallyChanged is false because of setSelectedIndex
			for (String item : vector)
			{
				this.cbm.addElement(item);
			}
			this.setModel(cbm);
			if (vector.size()>0)
			{
				this.setSelectedIndex(vector.size()-1);
			}
			isReallyChanged = true; // Now we can listen to the user action
		}
	}
	
	private class DirectoryPathRenderer extends DefaultListCellRenderer 
	{
		private static final long serialVersionUID = 1L;
		
		@Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) 
		{
           super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
           
           if (index == -1) // The selected item that appears in the top cell of the component
           {
              Icon icon =  new MetalIconFactory.FolderIcon16();
              this.setIcon(icon);
        	  this.setText(" " + value.toString().trim());
              return this;
           }
           this.setText(value.toString());
           return this;
        }
	}
	
	public class JShortcutsPanel extends JPanel
	{
		public JShortcutsPanel()
		{
			super(new GridLayout(7,1));
			this.initShortcuts();
		}
		
		private void initShortcuts()
		{
			String installPath, desktopPath, documentsPath, homePath, rootPath, downloadsPath;
			File currentFiletmp = null;
			Icon currentIcontmp = null;
			FileSystemView view = FileSystemView.getFileSystemView();
			homePath = (String) System.getProperty("user.home");
			
			// Desktop path
			desktopPath = this.getRegexPath(RegExp.SHORTCUT_DESKTOP);
			currentFiletmp = new File(desktopPath);
			if (currentFiletmp.exists())
			{
				currentIcontmp = view.getSystemIcon(currentFiletmp);
				this.addJLabelToList("Desktop", currentIcontmp, desktopPath);
				Messenger.printMsg(Messenger.DEBUG, "Added desktopPath : " + desktopPath);
			}
			currentFiletmp = null; currentIcontmp = null;
			
			// Documents path
			documentsPath = this.getRegexPath(RegExp.SHORTCUT_DOCUMENTS);
			currentFiletmp = new File(documentsPath);
			if (currentFiletmp.exists())
			{
				currentIcontmp = view.getSystemIcon(currentFiletmp);
				this.addJLabelToList("Documents", currentIcontmp, documentsPath);
				Messenger.printMsg(Messenger.DEBUG, "Added documentsPath : " + documentsPath);
			}
			currentFiletmp = null; currentIcontmp = null;
			
			// Download Path
			downloadsPath = this.getRegexPath(RegExp.SHORTCUT_DOWNLOADS);
			currentFiletmp = new File(downloadsPath);
			if (currentFiletmp.exists())
			{
				currentIcontmp = view.getSystemIcon(currentFiletmp);
				this.addJLabelToList("Downloads", currentIcontmp, downloadsPath);
				Messenger.printMsg(Messenger.DEBUG, "Added downloadsPath : " + downloadsPath);
			}
			currentFiletmp = null; currentIcontmp = null;
			
			// Home Path
			currentFiletmp = new File(homePath);
			if (currentFiletmp.exists())
			{
				currentIcontmp = view.getSystemIcon(currentFiletmp);
				this.addJLabelToList("Home", currentIcontmp, homePath);
				Messenger.printMsg(Messenger.DEBUG, "Added homePath : " + homePath);
			}
			currentFiletmp = null; currentIcontmp = null;
			
			// Install folder
			String saadaRepository = Database.getRepository();
			String saadaDB = Database.getRoot_dir();
			installPath = this.getCommonFolders(saadaRepository, saadaDB);
			//Messenger.printMsg(Messenger.DEBUG, "@@@@@@@@@@@@@@@@@\nsaadaRepository : " + saadaRepository + "\nsaadaDB : " + saadaDB + "\nLastCommonFolder : " + installPath);
			currentFiletmp = new File(installPath);
			if (currentFiletmp.exists())
			{
				currentIcontmp = view.getSystemIcon(currentFiletmp);
				this.addJLabelToList("Install folder", currentIcontmp, installPath);
				Messenger.printMsg(Messenger.DEBUG, "Added : saadaPath - " + installPath);
			}
			currentFiletmp = null; currentIcontmp = null;
			
			// Root path
			currentFiletmp = File.listRoots()[0];
			rootPath = currentFiletmp.getPath();
			if (currentFiletmp.exists())
			{
				currentIcontmp = view.getSystemIcon(currentFiletmp);
				this.addJLabelToList("Root", currentIcontmp, rootPath);
				Messenger.printMsg(Messenger.DEBUG, "Added rootPath : " + rootPath);
			}
			currentFiletmp = null; currentIcontmp = null;
		}
		
		private String getCommonFolders(String path1, String path2)
		{
			String lastCommonFolder = "";
			int lastCommonIndex = -1;
			boolean stop = false;
			String[] tab1 = path1.split(Database.getSepar());
			String[] tab2 = path2.split(Database.getSepar());
			for (int i=0 ; i<tab1.length && !stop; i++)
			{
				for (int j=0 ; j<tab2.length && !stop; j++)
				{
					if (i==j) // We compare tab1[0] with tab2[0], after tab1[1]==tab2[1]..
					{
						if (tab1[i].compareTo(tab2[j])==0)
							lastCommonIndex++;
						else
							stop = true;
					}
				}
			}
			if (lastCommonIndex==-1) // There is no common folder between path1 and path2
				return lastCommonFolder;
			// We create the common folders path
			for (int i=0 ; i<=lastCommonIndex ; i++)
				lastCommonFolder += tab1[i] + Database.getSepar();
			return lastCommonFolder;
		}
		
		private String getRegexPath(String regex)
		{
			String desktopPath = "", homePath = (String) System.getProperty("user.home");
			File[] children = (new File(homePath)).listFiles();
			boolean stop = false;
			for (int i=0 ; i<children.length && !stop ; i++)
			{
				String[] folders = children[i].toString().split(Database.getSepar());
				String toMatch = folders[folders.length-1];
				if (toMatch.matches(regex))
				{
					//Messenger.printMsg(Messenger.DEBUG, "Match with : " + toMatch);
					desktopPath = children[i].toString();
					stop = true;
				}
			}
			return desktopPath;
		}
		
		private void addJLabelToList(String name, Icon icon, final String path)
		{
			final JLabel lbl = new JLabel(name + " ");
			if (icon==null) 
			{
				new MetalIconFactory();
				lbl.setIcon(MetalIconFactory.getTreeFolderIcon());
			} 
			else
				lbl.setIcon(icon);
			lbl.setToolTipText(path);
			lbl.addMouseListener(new MouseListener() 
			{
				@Override
				public void mouseReleased(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseClicked(MouseEvent e) 
				{
					lbl.requestFocus();
					if( mask.getText().trim().length() > 0 ) 
						setDirectory(path, mask.getText(), true);						
					else
						setDirectory(path, combo_mask.getSelectedItem().toString(), true);
					currentDirComboBox.updateCurrentDir(current_dir);
				}
			});
			this.add(lbl);
		}
	}

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws FatalException 
	{
		Database.init((new ArgsParser(args)).getDBName());
		DataFileChooser dfc = new DataFileChooser(null, null);
		System.out.println(dfc.isFullDirectory() + " " + dfc.getCurrentDir() + " " + dfc.getSelectedDataFiles());
		System.exit(1);
	}
}
