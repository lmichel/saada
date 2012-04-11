package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import saadadb.admintool.AdminTool;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.ConfFileFilter;
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
	private JButton editConf = new JButton("Edit Selected Filter");
	private JButton newConf = new JButton("New Loader Filter");


	public LoaderConfigChooser(TaskPanel taskPanel) {
		this.taskPanel = taskPanel;
		this.confList.setVisibleRowCount(6);
		this.confList.setFixedCellWidth(15);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		description.setEditable(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;c.weightx = 0;
		this.add(newConf, c);
		this.newConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				activeEditorPanel() ;
			}			
		});


		c.gridy++;
		this.add(editConf, c);		
		this.editConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( confList.getSelectedValue() != null) {
					activeEditorPanel() ;
					if( ! confList.getSelectedValue().toString().equalsIgnoreCase("default") ){
						String filterName = confList.getSelectedValue().toString();
						AdminTool at = LoaderConfigChooser.this.taskPanel.rootFrame;
						try {
							FileInputStream fis = new FileInputStream(CONF_DIR + File.separator + category + "."  + filterName + ".config" );
							ObjectInputStream in = new ObjectInputStream(fis);
							argsParser = (ArgsParser)in.readObject();
							in.close();
							((MappingKWPanel)(at.getActivePanel())).loadConfig(argsParser);
						} catch (Exception e) {
							AdminComponent.showFatalError(at, e);
						}		
					}
				} else {
					AdminComponent.showInputError(LoaderConfigChooser.this.taskPanel.rootFrame, "Select a config please");					
				}
			}
		});

		c.gridx++;c.gridy = 0; c.gridheight = 2;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setPreferredSize(new Dimension(150,100));
		this.add(scrollPane, c);

		c.gridx++;		c.weightx = 1;
		this.add(new JScrollPane(description), c);

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

	private void activeEditorPanel() {
		AdminTool at = LoaderConfigChooser.this.taskPanel.rootFrame;

		if( category == null ) {
			AdminComponent.showFatalError(at, "No valid data category");
			return;
		} else if( category.equalsIgnoreCase("FLATFILE") ) {
			at.activePanel(AdminComponent.FLATFILE_MAPPER);
		} else if( category.equalsIgnoreCase("MISC")) {
			at.activePanel(AdminComponent.MISC_MAPPER);
		} else if( category.equalsIgnoreCase("IMAGE") ) {
			at.activePanel(AdminComponent.IMAGE_MAPPER);
		} else if( category.equalsIgnoreCase("SPECTRUM") ) {
			at.activePanel(AdminComponent.SPECTRUM_MAPPER);
		} else if( category .equalsIgnoreCase("ENTRY ")|| category.equalsIgnoreCase("TABLE")) {
			at.activePanel(AdminComponent.TABLE_MAPPER);
		} else {
			AdminComponent.showFatalError(at, "No valid data category");
			return;
		}
		at.getActivePanel().lockDataTreePath();
	}
	/**
	 * @param category
	 * @throws FatalException 
	 */
	public void setCategory(String category, String toSelect) throws FatalException {
		this.category = category;
		DefaultListModel model = (DefaultListModel) confList.getModel();
		model.removeAllElements();
		model.addElement("Default");
		File base_dir = new File( CONF_DIR);
		boolean nothingSelected = true;
		if( base_dir.isDirectory()) {
			ConfFileFilter dff = new ConfFileFilter(category);
			for( File c: base_dir.listFiles() ) {
				if( dff.accept(c) ) { 
					String name = c.getName().split("\\.")[1];
					model.addElement(name);
					if( toSelect != null && name.equals(toSelect)) {
						nothingSelected = false;
						confList.setSelectedValue(name, true);
					}
				}
			}
		}
		if( nothingSelected) {
		confList.setSelectedIndex(0);
		}
	}

	/**
	 * Makes a copy of the argsparser in order to avoid an accretion process at each call
	 * @return
	 */
	public ArgsParser getArgsParser() {
		try {
			return ( argsParser != null)? new ArgsParser(argsParser.getArgs()): null;
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
			return null;
		}
	}


}
