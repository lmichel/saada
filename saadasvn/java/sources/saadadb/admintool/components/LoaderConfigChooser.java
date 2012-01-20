package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import saadadb.admintool.panels.TaskPanel;
import saadadb.api.SaadaDB;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

public class LoaderConfigChooser extends JPanel {
	private JList confList = new JList(new DefaultListModel());
	private String category;
	private JTextArea description = new JTextArea(6, 24);
	private static final String CONF_DIR = SaadaDB.getRoot_dir()  + Database.getSepar() + "config";
	private TaskPanel taskPanel ;
	private ArgsParser argsParser;

	public LoaderConfigChooser(TaskPanel taskPanel) {
		this.taskPanel = taskPanel;
		this.confList.setVisibleRowCount(6);
		this.confList.setFixedCellWidth(15);

		description.setEditable(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setPreferredSize(new Dimension(100,100));

		this.add(scrollPane);
		c.gridx++;
		this.add(new JScrollPane(description));

		confList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent arg0) {
				if( confList.getSelectedValue() != null ) {
					String filterName = confList.getSelectedValue().toString();
					argsParser = null;
					if( !filterName.equals("Default") ) {
						try {
							FileInputStream fis = new FileInputStream(CONF_DIR + File.separator + category + "."  + filterName + ".config" );
							ObjectInputStream in = new ObjectInputStream(fis);
							argsParser = (ArgsParser)in.readObject();
							in.close();
						} catch(Exception ex) {
							AdminComponent.showFatalError(LoaderConfigChooser.this.taskPanel.rootFrame, ex);
							return;
						}		
					}
					else {
						try {
							argsParser = new ArgsParser(new String[]{"-category=" + category.toLowerCase()});
						} catch (FatalException e) {
							Messenger.trapFatalException(e);
						}
					}
					description.setText(argsParser.toString().replaceAll("-", "\n-"));
					LoaderConfigChooser.this.taskPanel.setSelectedResource("Filter: " + filterName, null);
				}	
			}

		});

	}

	/**
	 * @param category
	 */
	public void setCategory(String category) {
		this.category = category;
		DefaultListModel model = (DefaultListModel) confList.getModel();
		model.removeAllElements();
		model.addElement("Default");
		File base_dir = new File( CONF_DIR);
		if( base_dir.isDirectory()) {
			DataFileFilter dff = new DataFileFilter(category);
			for( File c: base_dir.listFiles() ) {
				if( dff.accept(c) ) {
					model.addElement(c.getName().split("\\.")[1]);
				}
			}
		}
		confList.setSelectedIndex(0);
	}

	/**
	 * Makes a copy of the argsparser in order to avoid an accretion process at each call
	 * @return
	 */
	public ArgsParser getArgsParser() {
		try {
			return new ArgsParser(argsParser.getArgs());
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
			return null;
		}
	}

	/**
	 * @author michel
	 *
	 */
	public class DataFileFilter extends FileFilter {
		private String category  = null;

		public DataFileFilter(String category) {
			this.category =category;
		}


		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String filename = f.getName();
			if( filename.matches(this.category + ".*\\.config") ) {
				return true;	
			} else {
				return false;
			}				
		}

		//The description of this filter
		public String getDescription() {
			return "Dataloader config files for category " + category;				

		}
	}
}
