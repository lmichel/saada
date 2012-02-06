package saadadb.admintool.components.input;

import java.awt.Component;
import java.awt.Dimension;
import java.text.ParseException;

import javax.swing.JFormattedTextField;


public class NodeNameTextField extends JFormattedTextField {
	
	public NodeNameTextField(int columns, String regexp, Component componentToActive)  {
		super(new RegexFormatter(regexp, componentToActive));
		this.setColumns(columns);
	}

}
