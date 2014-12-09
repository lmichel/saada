package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import saadadb.admintool.AdminTool;
import saadadb.admintool.panels.AdminPanel;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.ConfFileFilter;
import saadadb.admintool.utils.MyGBC;
import saadadb.api.SaadaDB;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * Widget presenting all loader config available for a given node 
 * It is connected to the congig editor {@link MappingKWPanel}
 * @author michel
 * @version $Id$
 *
 */
public class LoaderConfigChooser extends JPanel {
	private static final long serialVersionUID = 1L;
	private DefaultListModel listModel = new DefaultListModel();
	private JList confList = new JList(listModel);
	private JButton removeConf = new JButton("Remove Selected Filter");
	private LoaderConfigChooser activeFrame = this;
	
	private String category = null;
	private JTextArea description = new JTextArea(6, 24);
	private static final String CONF_DIR = SaadaDB.getRoot_dir()  + Database.getSepar() + "config";
	private AdminPanel taskPanel ;
	private ArgsParser argsParser;
	private JButton editConf = new JButton("Edit Selected Filter");
	private JButton newConf = new JButton("New Loader Filter");

	/**
	 * @param taskPanel
	 */
	public LoaderConfigChooser(AdminPanel taskPanel) {
		this.taskPanel = taskPanel;
		this.confList.setVisibleRowCount(15);
		this.confList.setFixedCellWidth(30);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.description.setEditable(false);
		this.setLayout(new GridBagLayout());

		MyGBC mgbc = new MyGBC(5,5,5,5);mgbc.gridheight = 2;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setPreferredSize(new Dimension(150,120));
		this.add(scrollPane, mgbc);

		mgbc.rowEnd();mgbc.gridheight = 1;mgbc.anchor = GridBagConstraints.SOUTHWEST;
		this.add(AdminComponent.getPlainLabel("Description of selected filter"), mgbc);
		mgbc.newRow();mgbc.gridx++;
		this.add(new JScrollPane(description), mgbc);

		mgbc.newRow();mgbc.gridwidth=2;
		JPanel jp = new JPanel();
		jp.setBackground(AdminComponent.LIGHTBACKGROUND);
		jp.setBorder(null);
		jp.add(newConf);
		jp.add(editConf);
		jp.add(removeConf);
		
		this.add(jp, mgbc);
		this.newConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				activeEditorPanel(true) ;
			}			
		});

		this.editConf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( confList.getSelectedValue() != null) {
					activeEditorPanel(false) ;
					if( ! confList.getSelectedValue().toString().equalsIgnoreCase("default") ){
						String filterName = confList.getSelectedValue().toString();
						AdminTool at = LoaderConfigChooser.this.taskPanel.rootFrame;
						try {
//							FileInputStream fis = new FileInputStream(CONF_DIR + File.separator + category + "."  + filterName + ".config" );
//							ObjectInputStream in = new ObjectInputStream(fis);
							argsParser = new ArgsParser(CONF_DIR + File.separator + category + "."  + filterName + ".config" );//(ArgsParser)in.readObject();
							//in.close();
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
		
		this.removeConf.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if( confList.getSelectedValue() != null)
				{
					int filterIndex = confList.getSelectedIndex();
					String filterName = confList.getSelectedValue().toString();
					String filterPath = CONF_DIR + File.separator + category + "."  + filterName + ".config";
					File selectedfile = new File(filterPath);
					if (AdminComponent.showConfirmDialog(activeFrame,"Do you really want to remove the filter " + filterName + " ?"))
					{
						selectedfile.delete();
						listModel.remove(filterIndex);
						description.setText("");
					}
				}
				else
				{
					AdminComponent.showInputError(LoaderConfigChooser.this.taskPanel.rootFrame, "Select a config please");
				}
			}
			
		});

		this.confList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent arg0) {
				if( confList.getSelectedValue() != null ) {
					String filterName = confList.getSelectedValue().toString();
					argsParser = null;
					if( !filterName.equals("Default") ) {
						try {
							argsParser = ArgsParser.load(CONF_DIR + File.separator + category + "."  + filterName + ".config");
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

	private void activeEditorPanel(boolean reset) {
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
		
		if (at.getActivePanel() instanceof MappingKWPanel)
		{
			if (reset)
			{
				((MappingKWPanel) at.getActivePanel()).reset(false);
			}
		}
		at.getActivePanel().lockDataTreePath();
		at.getActivePanel().setAncestor(taskPanel.getTitle());
	}
	/**
	 * @param category
	 * @throws FatalException 
	 */
	public void setCategory(String category, String toSelect) throws FatalException {
		this.category = category;
		DefaultListModel model = (DefaultListModel) confList.getModel();
		this.description.setText("");
		model.removeAllElements();
		if( category != null ) {
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
