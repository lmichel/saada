package saadadb.admintool.components;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;


public class NodeNameTextField extends JFormattedTextField {
	
	public NodeNameTextField(int columns, String regexp) throws ParseException {
		super(new MaskFormatter("####"));
		this.setColumns(columns);
	}

}
