package saadadb.admin.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.threads.CmdThread;
import saadadb.command.SaadaProcess;
import saadadb.util.Messenger;

public class ProgressDialog extends JDialog  {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JProgressBar progress_bar=null;
	private JTextArea task_output;
	private JOptionPane optionPane;
	private JButton pause_resume_btn = new JButton("Pause");
	private JButton abort_btn = new JButton("Abort");
	private CmdThread thread;
	
	/**
	 * @param aFrame
	 * @param title
	 * @param thread
	 * @param monitor_mode
	 */
	public ProgressDialog(Frame aFrame, String title, final CmdThread thread) {
		super(aFrame, false);
		this.setLocation(aFrame.getLocation());
		setTitle(title);
		this.thread = thread;
		task_output = new JTextArea(15, 48);
		Messenger.setGui_area_output(task_output);
		JScrollPane jsp = new JScrollPane(task_output);
		
		if( thread!= null && thread.getProcess() != null && thread.getProcess().getEndValue() >= 0) {
			progress_bar = new JProgressBar(0, thread.getProcess().getEndValue());   
			progress_bar.setStringPainted(true);

			Messenger.setProgress_bar(progress_bar);
		}
		else {
			progress_bar = new JProgressBar();
			progress_bar.setIndeterminate(true);
		}
		Object[] array = new Object[]{"", progress_bar,"", jsp};
		pause_resume_btn.addActionListener(new ActionListener() {
			public synchronized void actionPerformed(ActionEvent e) {
				if(pause_resume_btn.getText().equalsIgnoreCase("pause") ) {
					Messenger.pause();
					suspendProgressBar();				
					pause_resume_btn.setText("Resume");
				}
				else if(pause_resume_btn.getText().equalsIgnoreCase("close") ) {
					suspendProgressBar();				
					setVisible(false);
				}
				else {
					Messenger.printMsg(Messenger.TRACE, "Resume Task");
					ProgressDialog.this.resume();
					pause_resume_btn.setText("Pause");		
					resumeProgressBar();				
				}				
			}	
		});
		
		abort_btn.addActionListener(new ActionListener() {
			public synchronized void actionPerformed(ActionEvent e) {
				Messenger.pause();
				if( SaadaDBAdmin.showConfirmDialog(ProgressDialog.this.getParent(), "Are you sure?")  ) {
					ProgressDialog.this.resume();
					Messenger.abort();	
				}
				else {
					ProgressDialog.this.resume();
				}
			}	
		});
		Object[] options = {pause_resume_btn, abort_btn};
		
		optionPane = new JOptionPane(array,
				JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.YES_OPTION,
				null,
				options,
				null);
		setContentPane(optionPane);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				Messenger.unplugGui();
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});
		this.pack();
		this.setLocationRelativeTo(aFrame);
	    this.setVisible(true);	
	}

	/**
	 * Notify all saada processed to resume
	 */
	public synchronized void resume() {
		Messenger.resume();
		SaadaProcess running_process = thread.getProcess();
		if( running_process != null ) {
			synchronized (running_process) {
				/*
				 * Notify all must be invoked by the object owning the lock
				 */
				running_process.notifyAll();
				resumeProgressBar();
			}
		}
		else {
			Messenger.printMsg(Messenger.WARNING, "Null current process");			
		}
		
	}

	public void finish() {
		abort_btn.setEnabled(false);
		pause_resume_btn.setText("Close");		
		suspendProgressBar();						
	}
	  
	
	public final void suspendProgressBar(){
	       if(this.progress_bar!=null && this.progress_bar.isIndeterminate()){
	           this.progress_bar.setIndeterminate(false);
	           this.progress_bar.setMaximum(1);
	           this.progress_bar.setValue(1);
	       }
	   } 
	public final void resumeProgressBar(){
	       if(this.progress_bar!=null ){
	           this.progress_bar.setIndeterminate(true);
	           this.progress_bar.setMaximum(-1);
	           this.progress_bar.setValue(0);
	       }
	   } 
}

