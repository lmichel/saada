package saadadb.admintool.dialogs;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import saadadb.admintool.components.AdminComponent;
import saadadb.exceptions.IgnoreException;
import saadadb.util.Messenger;


public class IgnoreExceptionDialog extends JDialog implements ActionListener, PropertyChangeListener {
        /**
         *  * @version $Id$

         */
        private static final long serialVersionUID = 1L;
        private JOptionPane optionPane;
        
        private String btnString1 = "Continue";
        private String btnString2 = "Abort";
        private JRadioButton remember = new JRadioButton("Remember My Decision");
        private int action = 0;
        private IgnoreException exception;
        
        
        /** Creates the reusable dialog. */
        public IgnoreExceptionDialog(Frame aFrame, IgnoreException ie) {
                super(aFrame, true);
                setTitle("Exception Dialog");
                this.exception = ie;
                
                Object[] array = {"The Following Error Occured:", AdminComponent.getPlainLabel(ie.getMessage())
                                        , "For This Reason", AdminComponent.getPlainLabel(ie.getContext())
                                        , "", remember};
                Object[] options = {btnString1, btnString2};
                optionPane = new JOptionPane(array,
                                JOptionPane.ERROR_MESSAGE,
                                JOptionPane.YES_NO_OPTION,
                                null,
                                options,
                                options[0]);
                
                setContentPane(optionPane);
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent we) {
                                /*
                                 * Instead of directly closing the window,
                                 * we're going to change the JOptionPane's
                                 * value property.
                                 */
                                optionPane.setValue(new Integer(
                                                JOptionPane.CLOSED_OPTION));
                        }
                });
                optionPane.addPropertyChangeListener(this);
                this.pack();
                this.setLocationRelativeTo(aFrame);
                this.setVisible(true);
        }
        
        /** This method handles events for the text field. */
        public void actionPerformed(ActionEvent e) {
                optionPane.setValue(btnString1);
        }
        
        /** This method reacts to state changes in the option pane. */
        public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();
                
                if (isVisible()
                                && (e.getSource() == optionPane)
                                && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
                                                JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
                        Object value = optionPane.getValue();
                        
                        if (value == JOptionPane.UNINITIALIZED_VALUE) {
                                //ignore reset
                                return;
                        }
                        
                        //Reset the JOptionPane's value.
                        //If you don't do this, then if the user
                        //presses the same button next time, no
                        //property change event will be fired.
                        optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                        
                        if (btnString1.equals(value)) {
                                if( remember.isSelected() ) {
                                        IgnoreException.mustIgnore(exception);
                                }
                                this.action = Messenger.CONTINUE;
                                clearAndHide();
                        } else { //user closed dialog or clicked cancel
                                this.action = Messenger.ABORT;
                                clearAndHide();
                        }
                }
        }
        
        /**
         * @return
         */
        public int getAction() {
                return this.action;
        }
        
        /** This method clears the dialog and hides it. */
        public void clearAndHide() {
                setVisible(false);
        }
        
        public static void main(String[] args) {
                IgnoreExceptionDialog ied = new IgnoreExceptionDialog(null, new IgnoreException("PLante", "c'est comme ca"));
                System.out.println(ied.getAction());
        }
}