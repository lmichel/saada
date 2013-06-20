/**
 * 
 */
package saadadb.newdatabase.upgrade;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.SaadaDBConnector;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;
import saadadb.util.Version;

/**
 * @author laurentmichel
 *
 */
public class UpgradeHelper extends JFrame {
	private static final long serialVersionUID = 1L;
	private String saadaDir;
	private String saadadbDir ="";
	private SaadaDBConnector connector;	
	private JTextArea saadadbField = new JTextArea(6, 64);
	private JTextArea tarField = new JTextArea(4, 64);
	private JButton tarButton = new JButton("Browse Files...");
	private RunTaskButton runButton = new RunTaskButton(null);



	public UpgradeHelper(String saadaDir) {
		super("Saada Upgrade to version " + Version.version);
		this.saadaDir = saadaDir;
		Container panel = this.getContentPane();
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);

		Messenger.setGraphicMode(this);

		panel.setLayout(new GridBagLayout());
		MyGBC mg = new MyGBC(5,5,5,5);

		mg.right(false);
		panel.add(AdminComponent.getPlainLabel("Select the SaadaDB home directory"), mg);
		mg.rowEnd(); mg.left(false);
		JButton fc = new JButton("Browse Files...");
		fc.addActionListener(new ActionListener(){	
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if( UpgradeHelper.this.saadadbDir.length() > 0 ) {
					jfc.setSelectedFile(new File(saadadbDir));
				} else {
					jfc.setSelectedFile(new File(UpgradeHelper.this.saadaDir));
				}
				if( jfc.showOpenDialog(UpgradeHelper.this) == 	JFileChooser.APPROVE_OPTION	) {
					UpgradeHelper.this.saadadbField.setText("");
					UpgradeHelper.this.tarButton.setEnabled(false);				
					UpgradeHelper.this.tarField.setText("");
					runButton.setEnabled(false);
					try {
						UpgradeHelper.this.connector = new SaadaDBConnector();
						UpgradeHelper.this.connector.ParserSaadaDBConfFile(jfc.getSelectedFile().getAbsolutePath() + File.separator + "config" + File.separator + "saadadb.xml");
						UpgradeHelper.this.saadadbDir = connector.getRoot_dir();
						UpgradeHelper.this.saadadbField.setText("SaadaDB Name: " +connector.getDbname() + "\n");
						UpgradeHelper.this.saadadbField.append("Install Dir: " + saadadbDir + "\n");
						UpgradeHelper.this.saadadbField.append("Repository Dir: " + connector.getRepository());
						UpgradeHelper.this.tarButton.setEnabled(true);
						runButton.setEnabled(true);

					} catch (FatalException e) {
						AdminComponent.showInputError(UpgradeHelper.this, "Directory does not look like a valid SaadaDB installe dir ("+ e.getMessage() + ")");
					}
				}
			}
		});
		tarButton.addActionListener(new ActionListener(){	
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc = new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				File tarDir = (new File(UpgradeHelper.this.saadaDir)).getParentFile();
				jfc.setSelectedFile((new File(tarDir.getAbsoluteFile() + File.separator + connector.getDbname() + ".tar")));
				if( jfc.showSaveDialog(UpgradeHelper.this) == 	JFileChooser.APPROVE_OPTION	) {
					File f = jfc.getSelectedFile();
					if( f.getParentFile().canWrite() ) {
						UpgradeHelper.this.tarField.setText(f.getAbsolutePath());
					}
					else {
						AdminComponent.showInputError(UpgradeHelper.this, "No write permission for directory " + f.getAbsolutePath());						
						UpgradeHelper.this.tarField.setText("");
					}
				}
			}
		});

		panel.add(fc, mg);
		mg.newRow(); mg.gridwidth = 2;
		saadadbField.setEditable(false);	
		saadadbField.setFont(AdminComponent.helpFont);		
		saadadbField.setBackground(AdminComponent.LIGHTBACKGROUND);
		saadadbField.setForeground(Color.GRAY);
		saadadbField.setText("No selected SaadaDB install");

		saadadbField.setEnabled(false);
		panel.add(new JScrollPane(saadadbField), mg);

		mg.newRow(); mg.gridwidth = 1;
		panel.add(AdminComponent.getPlainLabel("Set the backup target"), mg);
		mg.rowEnd(); mg.left(false);
		panel.add(tarButton, mg);
		mg.newRow(); mg.gridwidth = 2;
		tarField.setEnabled(false);
		tarField.setEditable(false);	
		tarField.setFont(AdminComponent.helpFont);		
		tarField.setBackground(AdminComponent.LIGHTBACKGROUND);
		tarField.setForeground(Color.GRAY);
		tarButton.setEnabled(false);
		panel.add(new JScrollPane(tarField), mg);
		
		mg.newRow(); mg.gridwidth = 2;mg.left(false);
		runButton.setEnabled(false);
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( tarField.getText().length() == 0 ) {
					if( !AdminComponent.showConfirmDialog(UpgradeHelper.this, "No backup file: are you sure to upgarde without backup?") ) {
						return;
					}
				}
				((Component) e.getSource()).setEnabled(false);
				UpgradeSaadaDB us = new UpgradeSaadaDB(UpgradeHelper.this.saadaDir, connector, tarField.getText());
				try {
					us.upgrade();
					AdminComponent.showSuccess(UpgradeHelper.this, "Database " + connector.getDbname() + " upgraded"); 
				} catch (FatalException e1) {
					AdminComponent.showInputError(UpgradeHelper.this, "ERROR " + e1.getMessage());						
				}
				((Component) e.getSource()).setEnabled(true);			
			}
		});
		panel.add(runButton, mg);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("icons/saada_transp_square.png")));
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we){	        	
				//if( SaadaDBAdmin.showConfirmDialog(SaadaDBAdmin.this, "Do you really want to exit?") ==  true ) {
				System.exit(1);
				//}
			}
		});

		this.pack();
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new UpgradeHelper(args[0]); 
		//		try {
		//			UpgradeSaadaDB upgd = new UpgradeSaadaDB(args[0]);
		//			upgd.upgrade(args[1], args[2]);
		//		} catch (FatalException e) {
		//			Messenger.trapFatalException(e);
		//		}
	}
}

