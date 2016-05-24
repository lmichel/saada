/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadSaveClassTag;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.windows.VoTreeFrame;
import saadadb.exceptions.QueryException;
import saadadb.util.Messenger;


/**
 * @author laurentmichel
 *
 */
public class MetaDataEditorPanel extends TaskPanel {
	private RunTaskButton runButton;
	private SQLJTable productTable;
	private String sqlQuery;
	private int tableClass;
	private JPanel tPanel;
	
	public MetaDataEditorPanel(AdminTool rootFrame,String ancestor) {
		super(rootFrame, MANAGE_METADATA, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadSaveClassTag(rootFrame, MANAGE_METADATA) ;
	}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();
		retour.put("datatable", productTable);
		return retour;
	}

	@Override
	protected void setActivePanel() {		
		runButton = new RunTaskButton(this);
		tPanel = this.addSubPanel("Meta Data Display", false);
		tPanel.add(AdminComponent.getHelpLabel(HelpDesk.METADATA_EDITOR));
		this.setActionBar(new Component[]{runButton
				, debugButton});
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootOrCollectionLevel())
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
		if( productTable!= null && productTable.hasChanged() )  {
			if( !AdminComponent.showConfirmDialog(this, "Modifications not saved. Do you want to continue anyway?") ) {
				return;
			}
			try {
				productTable.setModel(sqlQuery);
			} catch (QueryException e) {
				Messenger.trapQueryException(e);
			}
			
		}
		else {
			super.setDataTreePath(dataTreePath);
			if( dataTreePath != null ) {
				if( dataTreePath.isClassLevel() ) {
					sqlQuery = "select pk, name_attr, type_attr, name_origin, ucd, utype, queriable, unit , comment, format from saada_metaclass_" 
						+ dataTreePath.category.toLowerCase()
						+ " where  name_class = '" + dataTreePath.classe + "' order by pk";
					tableClass = SQLJTable.CLASS_PANEL;
				}
				else if( dataTreePath.isCategoryLevel() ) {
					sqlQuery= "select  name_attr, type_attr, name_origin, ucd, utype, comment from saada_metacoll_" 
						+ dataTreePath.category.toLowerCase()
						+ " order by pk";
				}
				else {
					return;
				}
				try {
					if( productTable == null ) {
						productTable = new SQLJTable(rootFrame, dataTreePath, sqlQuery, tableClass);
						tPanel.removeAll();
						productTable.setBackground(AdminComponent.LIGHTBACKGROUND);
						productTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
						productTable.packAll();
						JScrollPane jsp = new JScrollPane(productTable);
						jsp.setBackground(AdminComponent.LIGHTBACKGROUND);
						GridBagConstraints c = new GridBagConstraints();
						c.gridx = 0; c.gridy = 0;
						c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH; c.gridwidth = 2;
						tPanel.add(jsp, c);
						c.weightx = 0; c.weighty = 0; c.gridy++;c.fill = GridBagConstraints.NONE;c.gridwidth = 1;
						JButton jb = new JButton("Open Meta Data panel");
						jb.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								VoTreeFrame vtf = VoTreeFrame.getInstance(rootFrame, productTable);
								vtf.open(SQLJTable.META_PANEL);
							}
						});
						tPanel.add(jb, c);
						c.gridx ++; c.anchor = GridBagConstraints.LINE_START; 
						tPanel.add(getHelpLabel(HelpDesk.METADATA_EDITOR), c);
					}
					else {
						productTable.setDataTrePath(dataTreePath);
						productTable.setModel(sqlQuery);
					}
				} catch (QueryException e) {
					Messenger.trapQueryException(e);
				}
				runButton.setEnabled(true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#hasChanged()
	 */
	public boolean hasChanged() {
		return (productTable == null)? false:  productTable.hasChanged();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#cancelChanges()
	 */
	public void cancelChanges() {
		if( productTable!= null && productTable.hasChanged() )  {
			try {
				productTable.setModel(sqlQuery);
			} catch (QueryException e) {
				Messenger.trapQueryException(e);
			}
		}
	}
}
