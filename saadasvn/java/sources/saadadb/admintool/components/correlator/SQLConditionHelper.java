package saadadb.admintool.components.correlator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

@SuppressWarnings("rawtypes")
public class SQLConditionHelper extends JComboBox{
	private static final long serialVersionUID = -7989548267498721101L;
	private JTextArea textEditor;
	private RelationPopulatePanel taskPanel;
	private static  Map<String, String> helpItems ;

	SQLConditionHelper() {
		if( helpItems == null)	{
			try {
				helpItems = Database.getWrapper().getConditionHelp();
			} catch (FatalException e) {}
		}
	}

	/**
	 * @param rootFrame
	 * @param textEditor
	 */
	@SuppressWarnings("unchecked")
	public SQLConditionHelper(RelationPopulatePanel taskPanel, JTextArea textEditor) {
		super();
		this.textEditor = textEditor;
		this.taskPanel = taskPanel;
		for( String k: helpItems.keySet()) {
			this.addItem(k);
		}
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object it = getSelectedItem();
				if( it != null && !it.toString().startsWith("-")) {
					int caret_pos = SQLConditionHelper.this.textEditor.getCaretPosition();
					SQLConditionHelper.this.taskPanel.notifyChange();
					if( caret_pos >= (SQLConditionHelper.this.textEditor.getText().length() -1) ) {
						SQLConditionHelper.this.textEditor.append("\n AND ");
						SQLConditionHelper.this.textEditor.append(helpItems.get(it.toString()));
					} else {
						SQLConditionHelper.this.textEditor.insert(helpItems.get(it.toString())
								, SQLConditionHelper.this.textEditor.getCaretPosition());
					}
				}
			}
		});

	}
}
