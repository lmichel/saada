package saadadb.admintool.components.input;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Combox with textfield filtering lhe list content
 * @author michel
 * @version $Id$
 *
 */
public class FilteredComboBox extends JComboBox {
	private static final long serialVersionUID = 1L;
	private List<String> array;
	private final JTextField textfield;
	private final String welcomeLabel;

	/**
	 * @param array
	 */
	public FilteredComboBox(List<String> array) {
		super(array.toArray());
		this.welcomeLabel = "";
		this.array = array;
		this.textfield = (JTextField) this.getEditor().getEditorComponent();
		init();
		this.restoreWelcomeLabel();
	}
	/**
	 * @param welcomeLabel: displayed in background still some text is entered in the text field
	 */
	public FilteredComboBox(String welcomeLabel) {
		super();
		this.welcomeLabel = welcomeLabel;
		this.array = new ArrayList<String>();
		this.setEditable(true);
		this.textfield = (JTextField) this.getEditor().getEditorComponent();
		init();
		this.restoreWelcomeLabel();

	}

	/**
	 * Invoked by all constructors
	 */
	private void init(){
		this.setMaximumRowCount(10);
		this.setEditable(true);
		textfield.addKeyListener(new KeyAdapter() {			
			public void keyReleased(KeyEvent e) {
				if( textfield.getForeground() == Color.gray) {
					textfield.setForeground(Color.BLACK);
					textfield.setText("");
				}				
				comboFilter();		
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!isPopupVisible()) {
							showPopup();
						}
					}
				});			
			}
		});   
		this.addPopupMenuListener(new PopupMenuListener() {			
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				if( textfield.getText().equals(welcomeLabel) ) {
					//textfield.setForeground(Color.BLACK);
					//textfield.setText("");					
					comboFilter();
				}
			}			
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}	
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
	}

	private void restoreWelcomeLabel(){
		textfield.setForeground(Color.GRAY);
		textfield.setText(welcomeLabel);   
		textfield.setCaretPosition(0);
	}

	public void removeAllItems() {
		super.removeAllItems();
		this.array.clear();
		this.comboFilter();
		this.restoreWelcomeLabel();
	}
	/* (non-Javadoc)
	 * @see javax.swing.JComboBox#addItem(java.lang.Object)
	 */
	public void addItem(Object obj) {
		this.array.add(obj.toString());
		comboFilter();
	}

	/**
	 * Action on keyboard filters the popup list whereas action on mouse with
	 * the popup open takeq the selection
	 * 
	 * @param e
	 * @return
	 */
	public boolean isItemSelectEvent(ActionEvent e) {
		if(  this.isPopupVisible() && e.getModifiers() ==  ActionEvent.MOUSE_EVENT_MASK ) {
			return true;
		}
		return  false;

	}
	/**
	 * Fileter the combobox with the text
	 * Must be called after the list is set
	 */
	public void comboFilter() {
		String enteredText = textfield.getText();
		List<String> filterArray= new ArrayList<String>();
		for (int i = 0; i < array.size(); i++) {
			String s = array.get(i);
			if (textfield.getForeground() == Color.GRAY || s.length() == 0  || s.toLowerCase().contains(enteredText.toLowerCase())) {
				filterArray.add(s);
			}
		}
		if (filterArray.size() > 0) {
			DefaultComboBoxModel model = (DefaultComboBoxModel) this.getModel();
			model.removeAllElements();
			for (String s: filterArray) {
				model.addElement(s);
			}
			JTextField textfield = (JTextField) this.getEditor().getEditorComponent();
			textfield.setText(enteredText);
		}
		this.setMaximumRowCount(10);
	}



	/* Testing Codes */
	public  List<String> populateArray() {
		List<String> test = new ArrayList<String>();
		this.addItem("");
		this.addItem("Mountain Flight");
		this.addItem("Mount Climbing");
		this.addItem("Trekking");
		this.addItem("Rafting");
		this.addItem("Jungle Safari");
		this.addItem("Bungie Jumping");
		this.addItem("Para Gliding");
		return test;
	}

	public static void makeUI() {
		JFrame frame = new JFrame("Adventure in Nepal - Combo Filter Test");
		final FilteredComboBox acb = new FilteredComboBox("ESSAI");
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(acb);
		JButton jb = new JButton("Reste");
		jb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				acb.removeAllItems();	
			}
		});
		frame.getContentPane().add(jb);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		acb.populateArray();
	}

	public static void main(String[] args) throws Exception {

		//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		makeUI();
	}
}
