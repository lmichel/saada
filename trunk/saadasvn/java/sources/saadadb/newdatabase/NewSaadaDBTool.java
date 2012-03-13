package saadadb.newdatabase;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import saadadb.admin.SaadaDBAdmin;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

public class NewSaadaDBTool extends JFrame {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ProgressPanel progress_panel;
	private FormPanel form_panel;
	private JPanel button_panel;
	private int screen_number=0;
	static public String saada_home;
	private JButton next_btn = new JButton("Next >>");
	private JButton prev_btn = new JButton("<< Previous");
	private String log_file ;
	/**
	 * @throws FatalException 
	 */
	public NewSaadaDBTool(ArgsParser ap, String saada_home) throws FatalException {
		super("Saada " + Database.version() + ": Database Creation Tool");
		this.saada_home = saada_home;
		this.setResizable(true);
		this.setLayout(new GridBagLayout());	
		/*
		 * Make sure to close and rename the log file when exit
		 */
		log_file = this.saada_home + Database.getSepar() + "logs" + Database.getSepar() + "newdb.log";
		if( !ap.isNolog() ) Messenger.openLog(log_file);
		Messenger.printMsg(Messenger.TRACE, "Start to build a SaadaDB from Saada instance: " + saada_home);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Messenger.closeLog();
				String new_logfile;
				if( NewSaadaDBTool.this.form_panel.saadadb_name.getText().length() > 0 ) {
					new_logfile = NewSaadaDBTool.this.saada_home + Database.getSepar() + "logs" + Database.getSepar() + "newdb." + NewSaadaDBTool.this.form_panel.saadadb_name.getText() + ".log";
				}
				else {
					new_logfile = NewSaadaDBTool.this.saada_home + Database.getSepar() + "logs" + Database.getSepar() + "newdb.aborted.log";			
				}
				(new File(NewSaadaDBTool.this.log_file)).renameTo(new File(new_logfile));
				System.out.println("Log saved in " + new_logfile);
			}
		});
		
		/*
		 * Exit after confirmation when click on window close button
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){
	        	
				if( JOptionPane.showConfirmDialog(NewSaadaDBTool.this,
						"Are you sure?",
						"Configuration Question",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) {
					System.exit(1);
				}
	        }
	      });
	    
	    GridBagConstraints c = new GridBagConstraints();
		/*
		 * meta_data tree is always visible on the left pbutton_panelart of the panel
		 */

		progress_panel = new ProgressPanel(this);
		c.gridx = 0;
		c.gridy = 0;	
		c.fill  = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;
 		this.getContentPane().add(progress_panel, c);
 		
 		form_panel = new FormPanel(this);
		c.gridx = 1;
		c.gridy = 0;		
		this.add(form_panel, c);
 		/*
 		 * Button Panel
 		 */
		button_panel = new JPanel();
		prev_btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				screen_number--;
				NewSaadaDBTool.this.next_btn.setText("Next >>");
				if( screen_number >= 0 ) {
					progress_panel.jumpToStep(screen_number);
					NewSaadaDBTool.this.form_panel.jumpToPanel(screen_number);
				}
				else {
					screen_number = 0;
				}
			}
			
		});
		button_panel.setLayout(new FlowLayout());
		button_panel.add(prev_btn);
		next_btn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if( next_btn.getText().equals("EXIT") ) {
					System.exit(0);
				}
				else if( screen_number <= 5 ) {
					if( NewSaadaDBTool.this.form_panel.validPanel(screen_number) ) {
						if( screen_number ==  5) {
							next_btn.setText("INSTALL YOUR SaadaDB");
						}
						screen_number++;
						progress_panel.jumpToStep(screen_number);
						NewSaadaDBTool.this.form_panel.jumpToPanel(screen_number);
					}
				}
				else {
					NewSaadaDBTool.this.createSaadaDB();
					progress_panel.jumpToStep(screen_number);
					next_btn.setText("EXIT");
				}
			}
			
		});
		button_panel.setLayout(new FlowLayout());
		button_panel.add(next_btn);
		c.gridx = 1;
		c.gridy = 1;		
		c.weightx = 0;
		c.weighty = 0;		
		c.fill  = GridBagConstraints.NONE;
		this.getContentPane().add(button_panel, c);
		/*
		 * and show
		 */
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("icons/saada_transp_square.png")));
		this.pack();
		this.setVisible(true);
	}
	
	/**
	 * 
	 */
	private void createSaadaDB() {
		Cursor cursor_org = this.getCursor();
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		NewSaadaDB newdb;
		try {
			this.form_panel.dbmswrapper.createDB(this.form_panel.saadadb_name.getText().trim());
			newdb = new NewSaadaDB(this.saada_home, new String(this.form_panel.dbms_admin_passwd.getPassword()));
			newdb.buildSaadaDB();
			NewWebServer.innerMain(new String[]{this.saada_home});
			JOptionPane.showMessageDialog(this,
					SaadaDBAdmin.getPlainLabel("<HTML><B>Your SaadaDB <I>" + form_panel.saadadb_name.getText() + "</I> has been created</B><BR>"
							+ "You can now exit this tool and work with the new database<BR>"
							+ "<UL><LI>Go to <I>" + form_panel.saadadb_home.getText() + "/" + form_panel.saadadb_name.getText() + "/bin</I></LI>"
							+ "<LI>Run <I>saadadmintool</I></LI>"),
					"Database Created",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			this.setCursor(cursor_org);
			JOptionPane.showMessageDialog(this,
					"SaadaDB Creation failed: " + e,
					"Creation Error",
					JOptionPane.ERROR_MESSAGE);
		}
		this.setCursor(cursor_org);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try {
			ArgsParser ap = new ArgsParser(args);
			new NewSaadaDBTool(ap, args[args.length-1]);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
		}
	}

}
