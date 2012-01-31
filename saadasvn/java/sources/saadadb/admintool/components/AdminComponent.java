package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import saadadb.admintool.AdminTool;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.util.Messenger;


/**
 * @author laurent
 *
 */
public abstract class AdminComponent extends JPanel {
	private static final long serialVersionUID = 1L;
	public final AdminTool rootFrame;
	public static final Color IVORY = new Color(255, 255, 240);
	public static final Color LIGHTBACKGROUND = new Color(245, 245, 245);
	public static final Font plainFont = new Font("Helvetica",Font.PLAIN,12);
	public static final Font italicFont = new Font("Helvetica",Font.ITALIC,12);
	public static final Font helpFont = new Font("Helvetica",Font.ITALIC,10);
	public static final Font titleFont = new Font("Helvetica",Font.BOLD,14);
	public static final Font subtTitleFont = new Font("Helvetica",Font.ITALIC,14);

	public static int STRING_FIELD_NAME = 20;

	public static final String ROOT_PANEL  = "Root Panel";
	public static final String MANAGE_DATA = "Manage Data";
	public static final String LOAD_DATA   = "Load Data";
	public static final String VO_PUBLISH  = "VO Publishing";
	public static final String MANAGE_RELATIONS  = "Manage Relationships";
	public static final String PROCESS_PANEL     = "Process Panel";
	
	public static final String CREATE_COLLECTION   = "Create Collection";
	public static final String DROP_COLLECTION     = "Drop Collection";
	public static final String EMPTY_COLLECTION    = "Empty Collection";
	public static final String COMMENT_COLLECTION  = "Comment Collection";
	
	public static final String EMPTY_CATEGORY    = "Empty Category";
	
	public static final String DATA_LOADER = "Data Loader";
	
	public static final String MISC_MAPPER     = "KW Mapper for MISC";
	public static final String TABLE_MAPPER    = "KW Mapper for Tables";
	public static final String SPECTRUM_MAPPER = "KW Mapper for Spectra";
	public static final String IMAGE_MAPPER    = "KW Mapper for Images";
	public static final String FLATFILE_MAPPER = "KW Mapper for Flatfiles";
	
	public static final String MANAGE_METADATA = "Manage Meta Data";
	protected DataTreePath dataTreePath;
	protected String selectedResource;
	protected String currentTask;
	
	
	/**
	 * @param rootFrame
	 */
	public AdminComponent(AdminTool rootFrame) {
		super();
		this.rootFrame = rootFrame;
		this.setMainPanel();
	}

	/**
	 * 
	 */
	protected abstract void setMainPanel() ;

	/**
	 * @param frame
	 * @param message
	 */
	public static final void showInfo(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				getPlainLabel(message),
				"Information",
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * @param txt
	 * @return
	 */
	public final static void showCopiableInfo(Component frame, String message, String title) {
		JTextArea jta = new  JTextArea(message);
		jta.setEditable(false);	
		jta.setFont(plainFont);		
		jta.setBackground(LIGHTBACKGROUND);		
		/*
		 * Mettre un scroller
		 */
		JOptionPane.showMessageDialog(frame,
				jta,
				"Loader Parameters",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static final void showFatalError(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				message,
				"Fatal Internal Error",
				JOptionPane.ERROR_MESSAGE);
	}
	
	public static final void showFatalError(Component frame, Exception e) {
		Messenger.printStackTrace(e);
		JOptionPane.showMessageDialog(frame,
				e.toString(),
				"Fatal Internal Error",
				JOptionPane.ERROR_MESSAGE);
	} 

	public static final void showInputError(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				message,
				"Input Error",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @param frame
	 * @param message
	 * @return
	 */
	public static final boolean showConfirmDialog(Component frame, String message) {
		Object[] options = {"Yes", "No"};
		int n = JOptionPane.showOptionDialog(frame,
				message,
				"Need a Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);

		if( n == 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static final void showSuccess(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				message,
				"Success",
				JOptionPane.PLAIN_MESSAGE);		
	}	

	
	/**
	 * @param txt
	 * @return
	 */
	public final static JLabel getPlainLabel(String txt) {
		JLabel retour = new JLabel(txt);
		retour.setBackground(Color.white);
		retour.setFont(plainFont);		
		return retour;
	}

	/**
	 * @param txt
	 * @return
	 */
	public final static JTextArea getHelpLabel(String txt) {
		JTextArea retour = new  JTextArea(txt);
		retour.setEditable(false);	
		retour.setFont(helpFont);		
		retour.setBackground(LIGHTBACKGROUND);
		retour.setForeground(Color.GRAY);
		return retour;
	}
	
	/**
	 * @param phrases
	 * @return
	 */
	public final static JTextArea getHelpLabel(String[] phrases) {
		JTextArea retour = new  JTextArea();
		for( String str: phrases) {
			retour.append(str + "\n");
		}
		retour.setEditable(false);	
		retour.setFont(helpFont);		
		retour.setBackground(LIGHTBACKGROUND);
		retour.setForeground(Color.GRAY);
		return retour;
	}
	
	/**
	 * @param textKey
	 * @return
	 */
	public final static JTextArea getHelpLabel(int textKey) {
		return  getHelpLabel(HelpDesk.get(textKey));
	}


	/**
	 * @param txt
	 * @return
	 */
	public final static JLabel getItalicLabel(String txt) {
		JLabel retour = new JLabel(txt);
		retour.setBackground(Color.white);
		retour.setFont(italicFont);		
		return retour;
	}
	/**
	 * @param txt
	 * @return
	 */
	public final static JLabel getTitleLabel(String txt) {
		JLabel retour = new JLabel(txt);
		retour.setBackground(Color.white);
		retour.setFont(titleFont);		
		return retour;
	}
	/**
	 * @param txt
	 * @return
	 */
	public final static JLabel getSubTitleLabel(String txt) {
		JLabel retour = new JLabel(txt);
		retour.setBackground(Color.white);
		retour.setFont(subtTitleFont);		
		return retour;
	}
	
	/**
	 * 
	 */
	public  void setDataTreePath(DataTreePath dataTreePath) {
		this.dataTreePath = dataTreePath;
	}
	
	public  void setSelectedResource(String selectedResource, String explanation) {
		this.selectedResource = selectedResource;
	}

	public  void setCurrentTask(String currentTask) {
		this.currentTask = currentTask;
	}

	public AdminTool getRootFrame() {
		return rootFrame;
	}
	
	

}
