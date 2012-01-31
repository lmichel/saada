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
import saadadb.admintool.components.AntButton;
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
	private RunTaskButton runButton ;
	private SQLJTable productTable ;
	private String sqlQuery;
	private int tableClass;
	private JPanel tPanel;
	
	public MetaDataEditorPanel(AdminTool rootFrame,String ancestor) {
		super(rootFrame, MANAGE_METADATA, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadSaveClassTag(rootFrame, dataTreePath) ;
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
		tPanel = this.addSubPanel("Meta Data Display");
		tPanel.add(AdminComponent.getHelpLabel(HelpDesk.METADATA_EDITOR));
		this.setActionBar(new Component[]{runButton
				, debugButton});
	}

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
					sqlQuery= "select  name_attr, type_attr, name_origin, ucd, comment from saada_metacoll_" 
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
						productTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
								vtf.open();
							}
						});
						tPanel.add(jb, c);
						c.gridx ++; c.anchor = GridBagConstraints.LINE_START; 
						tPanel.add(getHelpLabel(HelpDesk.METADATA_EDITOR), c);
					}
					else {
						productTable.setModel(sqlQuery);
					}
				} catch (QueryException e) {
					Messenger.trapQueryException(e);
				}
				runButton.setEnabled(true);
			}
		}
	}

	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}

	public boolean hasChanged() {
		return (productTable == null)? false:  productTable.hasChanged();
	}
	
	public void resetChanges() {
		if( productTable!= null && productTable.hasChanged() )  {
			try {
				productTable.setModel(sqlQuery);
			} catch (QueryException e) {
				Messenger.trapQueryException(e);
			}
		}
	}
}
