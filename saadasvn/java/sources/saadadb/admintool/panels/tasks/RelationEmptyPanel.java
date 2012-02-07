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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadLoadData;
import saadadb.admintool.cmdthread.ThreadRelationDrop;
import saadadb.admintool.cmdthread.ThreadRelationEmpty;
import saadadb.admintool.cmdthread.ThreadRelationIndex;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.LoaderConfigChooser;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.dialogs.DataFileChooser;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;


/**
 * @author laurentmichel
 *
 */
public class RelationEmptyPanel extends RelationDropPanel {

	protected  RelationshipChooser configChooser;
	protected RunTaskButton runButton;


	public RelationEmptyPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_RELATION, null, ancestor);
		cmdThread = new ThreadRelationEmpty(rootFrame, EMPTY_RELATION);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected RelationEmptyPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, cmdThread, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationEmpty(rootFrame, EMPTY_RELATION);
	}

}
