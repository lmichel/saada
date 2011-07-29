package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.meta.MetaRelation;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id$
 *
 */
public class RelationShow extends RelationIndexation{
	private String classe;
	
	public RelationShow(Frame frame, String relation, String classe) {
		super(frame, relation);
		this.classe = classe;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	protected boolean getParam()  {
		return  true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.RelationIndexation#runCommand()
     */
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			MetaRelation mr = Database.getCachemeta().getRelation(relation);
			String msg = "<HTML>" + mr.getHTMLSummary(classe) + "</HTML>";
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showInfo(frame, msg);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Cannot access relation <" +relation + "> failed (see console).");
		}

	}

}
