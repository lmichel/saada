package saadadb.admintool.components.mapper;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import saadadb.admintool.panels.editors.MappingKWPanel;

public abstract class PriorityPanel extends MappingPanel {

	protected JRadioButton spec_only_btn;
	protected JRadioButton spec_first_btn ;
	protected JRadioButton spec_last_btn ;
	protected JRadioButton spec_no_btn ;
	protected ButtonGroup buttonGroup ;

	
	public PriorityPanel(MappingKWPanel mappingPanel, String title) {
		super(mappingPanel, title);
		spec_only_btn = new JRadioButton("only");
		spec_first_btn = new JRadioButton("first");
		spec_last_btn = new JRadioButton("last");
		spec_no_btn = new JRadioButton("no mapping");
		buttonGroup = new ButtonGroup();
	}
	
	public boolean isOnly() {
		return spec_only_btn.isSelected();
	}
	public boolean isFirst() {
		return spec_first_btn.isSelected();
	}
	public boolean isLast() {
		return spec_last_btn.isSelected();
	}
	public boolean isNo() {
		return spec_no_btn.isSelected();
	}
	public String getMode() {
		if( isOnly()) {
			return "only";
		}
		else if( isFirst()) {
			return "first";
		}
		else if( isLast() ) {
			return "last";
		}
		else {
			return "no";
		}
	}
	public void setMode(String mode) {
		spec_only_btn.setSelected(false);
		spec_first_btn.setSelected(false);
		spec_last_btn.setSelected(false);
		spec_no_btn.setSelected(false);

		if( mode.equals("only")) {
			spec_only_btn.setSelected(true);
		}
		else if( mode.equals("first")) {
			spec_first_btn.setSelected(true);
		}
		else if( mode.equals("last")) {
			spec_last_btn.setSelected(true);
		}
		else{
			spec_no_btn.setSelected(true);
		}
	}

}
