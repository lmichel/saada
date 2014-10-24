package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.configuration.ExtendAttributeManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

public class ThreadMangeExtAttr extends CmdThread {
	protected String command;
	protected String name = null; // name of the attribute we are working on 
	protected String newname = null;
	protected String type;
	protected String category;	
	protected String comment;
	protected String unit;
	protected String ucd;
	protected String utype;
	ArrayList<String> antParams = new ArrayList<String>();
	ArrayList<String> cmdParams = new ArrayList<String>();
	public ThreadMangeExtAttr(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.name = null;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		this.command = (String) params.get("command");
		this.category = (String) params.get("category");		
		this.name = (String) params.get("name");		
		this.newname = (String) params.get("newname");		
		this.type = (String) params.get("type");		
		this.comment = (String) params.get("comment");		
		this.unit = (String) params.get("unit");		
		this.ucd = (String) params.get("ucd");		
		this.utype = (String) params.get("utype");		
		resourceLabel = "Collection " + name;
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		antParams = new ArrayList<String>();
		cmdParams = new ArrayList<String>();
		if( category == null || category.length() == 0 ){
			AdminComponent.showFatalError(frame, "No category given");
			return false;				
		}
		antParams.add("-category=" + category);
		cmdParams.add("-category=" + category);

		if( comment != null && comment.length() != 0 ){
			antParams.add("-comment=&quot;" + comment + "&quot;");
			cmdParams.add("-comment=\"" + comment + "\"");
		}
		if( unit != null && unit.length() != 0 ){
			antParams.add("-unit=" + unit + "");
			cmdParams.add("-unit=" + unit + "");
		}
		if( ucd != null && ucd.length() != 0 ){
			antParams.add("-ucd=" + ucd + "");
			cmdParams.add("-ucd=" + ucd + "");
		}
		if( utype != null && utype.length() != 0 ){
			antParams.add("-utype=" + utype + "");
			cmdParams.add("-utype=" + utype + "");
		}
		if( command.equals("create") ) {
			if( type == null || type.length() == 0 ){
				AdminComponent.showFatalError(frame, "No type given");
				return false;				
			}
			if( name == null || name.length() == 0 ){
				AdminComponent.showFatalError(frame, "No new name given");
				return false;				
			}	
			antParams.add("-type=" + type);
			cmdParams.add("-type=" + type);
			antParams.add("-" + command + "=" + name);
		} else if( command.equals("remove") ) {
			if( name == null || name.length() == 0 ){
				AdminComponent.showFatalError(frame, "No attribute name");
				return false;				
			}
			antParams.add("-" + command + "=" + name);
			cmdParams.add("-name=" + name);			
		} else if( command.equals("rename") ){
			if( name == null || name.length() == 0 ){
				AdminComponent.showFatalError(frame, "No attribute name");
				return false;				
			}
			if( newname == null || newname.length() == 0 ){
				AdminComponent.showFatalError(frame, "No new name given");
				return false;				
			}	
			if( name.equals(newname)  ) {
				command="comment";
			}
			antParams.add("-" + command + "=" + name);
			cmdParams.add("-name=" + name);
			antParams.add("-newname=" + newname);
			cmdParams.add("-newname=" + newname);
		} else {
			AdminComponent.showFatalError(frame, "Command " + command + " not supported");
			return false;				
		}
		cmdParams.add( Messenger.getDebugParam());
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new ExtendAttributeManager(name);			

			SQLTable.beginTransaction();
			ArgsParser ap = new ArgsParser(cmdParams.toArray(new String [0]));
			if( command.equals("create") ) {
				((ExtendAttributeManager)saada_process).create(ap);
			} else if( command.equals("remove") ) {
				((ExtendAttributeManager)saada_process).remove(ap);
			} else  if( command.equals("rename") ){
				((ExtendAttributeManager)saada_process).rename(ap);
			} else {
				((ExtendAttributeManager)saada_process).comment(ap);
			}
			SQLTable.commitTransaction();
			Database.getCachemeta().reloadGraphical(frame, true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Extended attribute of " + category+ " " + command + "d");		
				}				
			});
		} catch (AbortException e) {
			AdminComponent.showFatalError(frame, e);
		}catch (Exception e) {
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, e);
		}
	}

	@Override
	public String getAntTarget() {
		return AntDesk.getAntFile(AdminComponent.MANAGE_EXTATTR
				, taskTitle
				, antParams.toArray(new String [0]));
	}

	public String toString() {
		return "Manage extended attribute " + this.name + " of " + category;
	}

}
