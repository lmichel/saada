package saadadb.admintool.components.mapper;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.dataloader.mapping.PriorityMode;

public abstract class PriorityPanel extends MappingPanel {

	protected JRadioButton onlyBtn;
	protected JRadioButton firstBtn ;
	protected JRadioButton lastBtn ;
	protected JRadioButton noBtn ;
	protected ButtonGroup buttonGroup ;

	
	public PriorityPanel(MappingKWPanel mappingPanel, String title) {
		super(mappingPanel, title);
		onlyBtn = new JRadioButton("only");
		firstBtn = new JRadioButton("first");
		lastBtn = new JRadioButton("last");
		noBtn = new JRadioButton("no mapping");
		buttonGroup = new ButtonGroup();
	}
	
	public boolean isOnly() {
		return onlyBtn.isSelected();
	}
	public boolean isFirst() {
		return firstBtn.isSelected();
	}
	public boolean isLast() {
		return lastBtn.isSelected();
	}
	public boolean isNo() {
		return noBtn.isSelected();
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
	public void setMode(PriorityMode mode) {
		onlyBtn.setSelected(false);
		firstBtn.setSelected(false);
		lastBtn.setSelected(false);
		noBtn.setSelected(false);

		switch(mode){
		case ONLY:noBtn.setSelected(true);break;
		case FIRST: firstBtn.setSelected(true); break;
		case LAST: lastBtn.setSelected(true); break;
		default: noBtn.setSelected(true);
		}
	}
	
	public boolean valid() {
		if( this.isNo() ) {
			return true;
		}
		return super.valid();
	}


}
