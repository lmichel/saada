package saadadb.admintool.windows;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import xmleditorkit.XMLEditorKit;

@SuppressWarnings("serial")
public class TextSaver extends OuterWindow {
	private SaveButton saveButton = new SaveButton(null);
	private JEditorPane textArea = new JEditorPane();
	private JFileChooser fileChooser;
	private static  String defaultDir;

	/**
	 * @param rootFrame
	 * @param title
	 * @param defaultDir
	 * @param defaultFile
	 * @param content
	 */
	public TextSaver(AdminTool rootFrame, String title, String defaultDir, String defaultFile, String content) {
		super(rootFrame, "Save " + title);
		textArea.setEditorKit(new XMLEditorKit());
		if(content != null ) {
			textArea.setText(content);
		}
		if( defaultDir != null ) {
			TextSaver.defaultDir = defaultDir;
		}
		this.fileChooser = new JFileChooser(TextSaver.defaultDir);
		this.fileChooser.setSelectedFile(new File(defaultFile));
		Container panel = this.getContentPane();
		panel.setLayout(new GridBagLayout());
		MyGBC gbc = new MyGBC(5,5,5,5);
		gbc.left(false);
		panel.add(saveButton, gbc);

		gbc.newRow(); gbc.left(true);gbc.weighty = 1;gbc.fill = GridBagConstraints.BOTH;
		panel.add(new JScrollPane(textArea), gbc);
		this.pack();

	}

	/**
	 * @param rootFrame
	 * @param title
	 * @param defaultFile
	 * @param content
	 */
	public TextSaver(AdminTool rootFrame, String title, String defaultFile, String content) {
		this(rootFrame, title, null, defaultFile, content);
		if( TextSaver.defaultDir == null ) {
			TextSaver.defaultDir = Database.getRoot_dir() + File.separator + "config";
		}
		this.fileChooser = new JFileChooser(TextSaver.defaultDir);
		this.fileChooser.setSelectedFile(new File(defaultFile));
	}
	
	
	@Override
	protected void setContent(int type) throws Exception {
		saveButton.setEnabled(true);
		saveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if( fileChooser.showSaveDialog(rootFrame) == JFileChooser.APPROVE_OPTION ) {
					try {
						File f = fileChooser.getSelectedFile();
						BufferedWriter bw = new BufferedWriter(new FileWriter(f));
						bw.write(textArea.getText());
						bw.close();
						AdminComponent.showSuccess(rootFrame, "Save in " + f.getAbsolutePath() + " successed");
					} catch (IOException e) {
						AdminComponent.showFatalError(rootFrame, e);
					}
				}
			}	
		});
	}
}
