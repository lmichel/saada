package saadadb.admintool.windows;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.HelpDesk;
import saadadb.exceptions.QueryException;

public class MappedTableWindow extends DataTableWindow {

	public MappedTableWindow(AdminTool rootFrame, String sqlQuery)
			throws QueryException {
		super(rootFrame);
		this.sqlQuery = sqlQuery;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.windows.DataTableWindow#buidSQL()
	 */
	protected void buidSQL() {}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.windows.DataTableWindow#addCommandComponent()
	 */
	protected Component addCommandComponent() {
		JPanel retour = new JPanel();
		retour.setLayout(new BoxLayout(retour, BoxLayout.LINE_AXIS));
		retour.setBackground(AdminComponent.LIGHTBACKGROUND);
		JButton jb = new JButton("SUBMIT");
		jb.setAlignmentX(Component.LEFT_ALIGNMENT);
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refresh(queryTextArea.getText());
			}
		});
		retour.add(jb);
		retour.add(AdminComponent.getHelpLabel(HelpDesk.MAPPING_QVIEW));
		return  retour;
	}

}
