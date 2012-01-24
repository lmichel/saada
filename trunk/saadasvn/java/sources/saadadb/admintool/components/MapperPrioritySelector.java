package saadadb.admintool.components;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.JTextComponent;


public class MapperPrioritySelector {
	private JRadioButton nomapping;
	private JComponent[] components;

	/**
	 * @param buttons
	 * @param nomapping
	 * @param components
	 */
	public MapperPrioritySelector(JRadioButton[] buttons, JRadioButton nomapping, ButtonGroup bg, JComponent[] components, JPanel panel, GridBagConstraints ccs) {
		this.nomapping = nomapping;
		this.components = components;
		JPanel jp = new JPanel();
		jp.setBackground(AdminComponent.LIGHTBACKGROUND);
		jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));

		for( JRadioButton b: buttons) {
			if( b.getText().equalsIgnoreCase("only")) {
				b.setToolTipText("Only the rule given below will be applied");
			}
			else if( b.getText().equalsIgnoreCase("first")) {
				b.setToolTipText("The rule given below will be applied first and then, in case of failure,  an automatic detection.");
			}
			else if( b.getText().equalsIgnoreCase("last")) {
				b.setToolTipText("An automatic detection will be applied first and then, in case of failure, the rule given below.");
			}
			else if( b.getText().equalsIgnoreCase("no mapping")) {
				b.setToolTipText("Only the automatic detection will be applied ");
			}
		}
		for( JRadioButton jrb: buttons) {
			jrb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( e.getSource() == MapperPrioritySelector.this.nomapping ) {
						MapperPrioritySelector.this.setEnable(false);
					}
					else 
						MapperPrioritySelector.this.setEnable(true);
				}
			});
			if( jrb == nomapping ) {
				jrb.setSelected(true);
			}
			else {
				jrb.setSelected(false);
			}
			jp.add(jrb);	
			bg.add(jrb);
		}
		panel.add(jp, ccs);
		this.setEnable(false);
	}

	/**
	 * @param b
	 */
	protected void setEnable(boolean b) {
		for( JComponent jc: components) {
			if( jc.getClass().getName().matches(".*Text.*") ) {
				((JTextComponent)jc).setEditable(b);
			}
			jc.setEnabled(b);
		}
	}
}
