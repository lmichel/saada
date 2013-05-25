package saadadb.admintool.components.correlator;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;

public class SQLConditionHelper extends JComboBox{
	private JTextArea textEditor;
	private Frame rootFrame;
	private RelationPopulatePanel taskPanel;
	public static final Map<String, String> helpItems;

	static {
		helpItems = new LinkedHashMap<String, String>();
		helpItems.put("- Join Operator Templates -", "");
		helpItems.put("Partial comparison of names", "substr(p.namesaada, 0, 5) = substr(s.namesaada, 0, 5) ");
		helpItems.put("Row number equality"        , "(p.oidsaada >> 32) = (s.oidsaada >> 32)");
		try {
			helpItems.put("Regular expression op", "p.namesaada " + Database.getWrapper().getRegexpOp() + " 'RegExp'");
		} catch (FatalException e) {}
		helpItems.put("Same sky pixel", "p.sky_pixel_csa = s.sky_pixel_csa");
	}

	/**
	 * @param rootFrame
	 * @param textEditor
	 */
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
