package saadadb.admintool.windows;

import javax.swing.tree.TreePath;

import saadadb.admintool.AdminTool;
import saadadb.exceptions.QueryException;

public class MappedTableWindow extends DataTableWindow {

	public MappedTableWindow(AdminTool rootFrame, String sqlQuery)
			throws QueryException {
		super(rootFrame);
		this.sqlQuery = sqlQuery;
	}

	protected void buidSQL() {}
}
