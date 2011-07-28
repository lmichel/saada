package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import saadadb.admin.SaadaDBAdmin;
import saadadb.sqltable.SQLTable;

public class RunSQLQuery extends CmdThread {
	private String query;
	
	public RunSQLQuery(Frame frame, String query) {
		super(frame);
		this.query = query;
	}
	
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			SQLTable.addQueryToTransaction(this.query);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showSuccess(frame, "Query Successfull.");		
		} catch (Exception e) {
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Query Failed.");		
		}
	}

}
