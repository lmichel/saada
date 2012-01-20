/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadLoadData;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.LoaderConfigChooser;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.dialogs.DataFileChooser;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.command.ArgsParser;
import saadadb.util.Messenger;


/**
 * @author laurentmichel
 *
 */
public class DataLoaderPanel extends TaskPanel {

	private LoaderConfigChooser configChooser;
	protected JRadioButton repCopy ;
	protected JRadioButton repMove ;
	protected JRadioButton repKeep ;
	protected JRadioButton noIndex ;


	protected JTextField directory ;
	protected JButton dirBrowser ;
	protected JLabel datafileSummary ;

	protected String typed_dir;
	protected ArrayList<String> selectedFiles;

	private String collection, category;
	protected RunTaskButton runButton;


	public DataLoaderPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, DATA_LOADER, null, ancestor);
		cmdThread = new ThreadLoadData(rootFrame);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected DataLoaderPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.cmdThread = cmdThread;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( dataTreePath != null ) {
			if( dataTreePath.isCollectionLevel() ) {
				showInputError(rootFrame, "No category (IMAGE,ENTRY....) in the selected data tree node");
				runButton.inActivate();
			}
			else {
				super.setDataTreePath(dataTreePath);
				collection = dataTreePath.collection;
				if( "ENTRY".equals(dataTreePath.category)) {
					category = "TABLE";
				}
				else {
					category = dataTreePath.category;
				}

				configChooser.setCategory(category);
				runButton.activate();
			}
			treePathLabel.setText(dataTreePath.toString());

		}
	}


	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	@Override
	protected Map<String, Object> getParamMap() {
		ArgsParser ap = configChooser.getArgsParser();
		System.out.println(ap);
		if( ap != null ) {
			String rep = "";
			if( repMove.isSelected() ) {
				rep = "move";
			}
			else if( repKeep.isSelected() ) {
				rep = "no";					
			}
			System.out.println(directory.getText());
			ap.completeArgs(collection, directory.getText(), rep, noIndex.isSelected(), Messenger.debug_mode);
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("params", ap);
			
			if( selectedFiles != null && selectedFiles.size() > 0 ) {
				map.put("filelist", selectedFiles);
			}
			System.out.println(ap);

			return map;
		}
		else {
			return null;	
		}
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadLoadData(rootFrame);
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel = this.addSubPanel("Data Loader Filter Selector");
		configChooser = new LoaderConfigChooser(this);
		tPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;c.gridy = 0;
		c.weightx = 0;
		tPanel.add(configChooser, c);

		tPanel = this.addSubPanel("Repository parameters");
		repCopy = new JRadioButton("Copy Input Files into the Repository");
		repMove = new JRadioButton("Move Input Files to the Repository");
		repKeep = new JRadioButton("Use Input Files as Repository");
		noIndex = new JRadioButton("Do not rebuild indexes after loading");
		tPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0;c.gridy = 0;
		c.weightx = 0;

		c.anchor = GridBagConstraints.WEST;
		tPanel.add(repCopy, c);
		c.gridy ++;
		tPanel.add(repKeep, c);
		c.gridy ++;
		tPanel.add(repMove, c);
		c.gridy ++;
		tPanel.add(noIndex, c);
		c.gridx = 1;c.weightx = 1;
		tPanel.add(new JLabel(""), c);


		tPanel = this.addSubPanel("Input Files Selector");
		datafileSummary = new JLabel("no file selected");
		directory = new JTextField(32);
		dirBrowser = new JButton("Browse...");
		/*
		 * Directory browser
		 */
		dirBrowser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				DataFileChooser filechooser = new DataFileChooser(DataLoaderPanel.this, selectedFiles);										
				DataLoaderPanel.this.directory.setText(filechooser.getCurrentDir());
				selectedFiles = filechooser.getSelectedDataFiles();
				if( selectedFiles != null ) {
					if(  filechooser.isFullDirectory() > 0 ) {
						datafileSummary.setText("The whole directory content: " + selectedFiles.size() + " files");
					}
					else {
						datafileSummary.setText(selectedFiles.size() + " file(s) selected.");
					}
				}
				else {
					datafileSummary.setText("No file selected");
					AdminComponent.showInputError(rootFrame, "No Selected Data Files!");
				}
			}
		});

		c = new GridBagConstraints();
		c.gridx = 0;c.gridy = 0;
		c.weightx = 0;

		c.anchor = GridBagConstraints.WEST;
		tPanel.add(directory, c);
		c.gridy ++;
		tPanel.add(dirBrowser, c);
		c.gridy ++;
		tPanel.add(datafileSummary, c);
		c.gridx = 1;c.weightx = 1;
		tPanel.add(new JLabel(""), c);

		runButton = new RunTaskButton(this);
		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});

	}



}
