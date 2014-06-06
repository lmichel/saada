/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import saadadb.enums.RepositoryMode;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;


/**
 * @author laurentmichel
 *
 */
public class DataLoaderPanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	protected  LoaderConfigChooser configChooser;
	protected JRadioButton repCopy ;
	protected JRadioButton repMove ;
	protected JRadioButton repKeep ;
	protected JCheckBox noIndex ;


	protected JTextField directory ;
	protected JButton dirBrowser ;
	protected JLabel datafileSummary ;

	protected String typed_dir;
	protected ArrayList<String> selectedFiles;

	private String collection, category;
	protected RunTaskButton runButton;


	public DataLoaderPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, DATA_LOADER, null, ancestor);
		cmdThread = new ThreadLoadData(rootFrame, DATA_LOADER);
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
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) {
		if(dataTreePath.isRootOrCollectionLevel())
		{
			showInputError(rootFrame, "You must select either a category (IMAGE, SPECTRUM, ...) or a class.");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( this.isDataTreePathLocked() ){
			showInputError(rootFrame, "Can not change data treepath in this context");
		}else if( dataTreePath != null ) {
			if( dataTreePath.isCollectionLevel() ) {
				runButton.inactivate();
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

				try {
					configChooser.setCategory(category, null);
				} catch (FatalException e) {
					Messenger.trapFatalException(e);
				}
				runButton.activate();
			}
			//treePathLabel.setText(dataTreePath.toString());
			this.setTextTreePathPanel(dataTreePath);
		}
	}


	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	@Override
	protected Map<String, Object> getParamMap() {
		ArgsParser ap = configChooser.getArgsParser();
		if( ap != null ) {
			RepositoryMode rep = RepositoryMode.MOVE;
			if( repMove.isSelected() ) {
				rep = RepositoryMode.MOVE;
			}
			else if( repKeep.isSelected() ) {
				rep = RepositoryMode.KEEP;					
			}
			ap.completeArgs(collection, directory.getText(), rep, noIndex.isSelected(), Messenger.debug_mode);
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("params", ap);
			
			if( selectedFiles != null && selectedFiles.size() > 0 ) {
				map.put("filelist", selectedFiles);
			}
			return map;
		}
		else {
			return null;	
		}
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadLoadData(rootFrame, DATA_LOADER);
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel = this.addSubPanel("Filter Chooser");
		configChooser = new LoaderConfigChooser(this);
		tPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;c.gridy = 0;
		c.weightx = 0;
		tPanel.add(configChooser, c);

		tPanel = this.addSubPanel("Repository parameters");
		repCopy = new JRadioButton("Copy Input Files into the Repository");
		repCopy.setBackground(AdminComponent.LIGHTBACKGROUND);
		repMove = new JRadioButton("Move Input Files to the Repository");
		repMove.setBackground(AdminComponent.LIGHTBACKGROUND);
		repKeep = new JRadioButton("Use Input Files as Repository");
		repKeep.setBackground(AdminComponent.LIGHTBACKGROUND);
		repKeep.setSelected(true);
		ButtonGroup bg = new ButtonGroup();
		bg.add(repCopy); 
		bg.add(repMove); 
		bg.add(repKeep); 
		noIndex = new JCheckBox("Do not rebuild indexes after loading");
		noIndex.setSelected(true);
		noIndex.setBackground(AdminComponent.LIGHTBACKGROUND);
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
		tPanel.add(new JLabel("  "), c);
		c.gridy ++;
		tPanel.add(noIndex, c);
		c.gridx = 1;c.weightx = 1;
		tPanel.add(new JLabel(""), c);


		tPanel = this.addSubPanel("Data Files Selector");
		datafileSummary = new JLabel("No file selected");
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
//					datafileSummary.setText("No file selected");
//					AdminComponent.showInputError(rootFrame, "No Selected Data Files!");
				}
			}
		});

		c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.gridx = 0;c.gridy = 0;
		c.weightx = 0;

		c.anchor = GridBagConstraints.WEST; c.gridwidth = 2;
		tPanel.add(directory, c);
		c.gridx = 2; c.gridwidth = 3;
		tPanel.add(dirBrowser, c);
		c.gridy++;
		c.gridx = 0;c.weightx = 1;
		tPanel.add(datafileSummary, c);

		runButton = new RunTaskButton(this);
		JButton previewDataButton = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/preview.png")));
		previewDataButton.setToolTipText("Preview");
		previewDataButton.setEnabled(false);
		previewDataButton.setPreferredSize(new Dimension(60, 40));
		previewDataButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				//TODO Implement previewData function
				Messenger.printMsg(Messenger.DEBUG, "Not implemented yet !");
			}
		});
		this.setActionBar(new Component[]{runButton
				, previewDataButton
				, debugButton
				, (new AntButton(this))});

	}

	/**
	 * Called by the cnfig editor to set the new config
	 * @param confName
	 */
	public void setConfig(String confName) {
		try {
			configChooser.setCategory(this.category, confName);
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
	}

}
