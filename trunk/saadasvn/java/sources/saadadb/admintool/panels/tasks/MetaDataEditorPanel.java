/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.util.Map;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;


/**
 * @author laurentmichel
 *
 */
public class MetaDataEditorPanel extends TaskPanel {
	private RunTaskButton runButton ;
	private SQLJTable productTable ;
	private String sqlQuery;

	public MetaDataEditorPanel(AdminTool rootFrame,String ancestor) {
		super(rootFrame, MANAGE_METADATA, null, ancestor);
	}

	@Override
	public void initCmdThread() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Map<String, Object> getParamMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	protected void setActivePanel() {		
		RunTaskButton runButton = new RunTaskButton(this);
		JPanel tPanel = this.addSubPanel("Meta Data Display");

		this.setActionBar(new Component[]{runButton
			, debugButton
			, (new AntButton(this))});

	}
	
	public void setDataTreePath(DataTreePath dataTreePath) {
	super.setDataTreePath(dataTreePath);
	if( dataTreePath)
	sql = "select pk, name_attr, type_attr, name_origin, ucd, utype, queriable, unit , comment, format from saada_metaclass_" + tree_path_components[2] .toString().toLowerCase()
	+ " where  name_class = '" + tree_path_components[3] + "' order by pk";
	title = "Product meta-data for " +  tree_path_components[2] + " class  <" + tree_path_components[3] + "> of collection <" + tree_path_components[1] + ">";
	table_class = SQLJTable.CLASS_PANEL;

	this.buildSQL();
	
	}
	
	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}


}
