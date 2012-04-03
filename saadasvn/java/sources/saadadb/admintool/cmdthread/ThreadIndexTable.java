package saadadb.admintool.cmdthread;

import java.util.Collection;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.tasks.SQLIndexPanel;
import saadadb.admintool.utils.AntDesk;
import saadadb.command.ManageTableIndex;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id: BuildIndex.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class ThreadIndexTable extends CmdThread {
	private String table;
	private Collection<String> columns = null;
	private boolean remove=true;
	private SQLIndexPanel panel;


	/**
	 * @param frame
	 * @param taskTitle
	 */
	public ThreadIndexTable(SQLIndexPanel panel, String taskTitle) {
		super(panel.rootFrame, taskTitle);
		this.panel = panel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		if( params != null ) {
			remove = (Boolean)(params.get("remove"));
			table = (String)(params.get("table"));
			columns = (Collection<String>)(params.get("columns"));
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( table == null ) {
			AdminComponent.showFatalError(frame, "No table columns given");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		try {
			saada_process  = new ManageTableIndex(table);
			SQLTable.beginTransaction();
			if( columns == null ) {
				if( remove ) {
					((ManageTableIndex)saada_process).dropTableIndex();
				}else {
					((ManageTableIndex)saada_process).indexTable();
				}
			} else {
				for(String c: columns) {
					if( remove ) {
						((ManageTableIndex)saada_process).dropTableColumnIndex(c);
					}else {
						((ManageTableIndex)saada_process).indexTableColumn(c);
					}
				}
			}
			SQLTable.commitTransaction();
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					try {
						panel.buildColumnTable();
					} catch (Exception e) {
						AdminComponent.showFatalError(frame, e);
					}
				}
			});
			AdminComponent.showSuccess(frame, "Index of table <" +  table + "> successfully updated");		
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			AdminComponent.showFatalError(frame, "<HTML>Index update failed for table " + table + "<BR>" + SaadaException.toHTMLString(ae));
		} catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			AdminComponent.showFatalError(frame, "<HTML>Index update failed for table " + table + "<BR>" + SaadaException.toHTMLString(e));
		}
	}

	@Override
	public String getAntTarget() {
		if( remove ) {
			return AntDesk.getAntFile(AdminComponent.SQL_INDEX
					, taskTitle
					, new String[]{"-remove=\"" + table + "\""});
		} else {
			return AntDesk.getAntFile(AdminComponent.SQL_INDEX
					, taskTitle
					, new String[]{"-create=\"" + table + "\""});		
		}
	}

}
