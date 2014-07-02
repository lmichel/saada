package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.components.mapper.MapperPrioritySelector;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.MyGBC;
import saadadb.enums.PriorityMode;

public class VPPriorityPanel extends JPanel {

	protected JRadioButton onlyBtn;
	protected JRadioButton firstBtn ;
	protected JRadioButton lastBtn ;
	protected JRadioButton noBtn ;
	protected ButtonGroup buttonGroup ;

	public VPPriorityPanel(MappingKWPanel mappingPanel, String title, JComponent[] component){
		//super(title);
		super();
		this.setLayout(new GridBagLayout());
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		MyGBC gbc = new MyGBC();
		gbc.right(false);
		this.add(AdminComponent.getPlainLabel("Priority "), gbc);
		gbc.next();
		gbc.left(true);
		onlyBtn = new JRadioButton("only");
		firstBtn = new JRadioButton("first");
		lastBtn = new JRadioButton("last");
		noBtn = new JRadioButton("no mapping");
		buttonGroup = new ButtonGroup();
		new MapperPrioritySelector(new JRadioButton[] {onlyBtn, firstBtn, lastBtn, noBtn}, noBtn, buttonGroup
				,  component
		, this, gbc);
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
		if( mode != null ){
			switch(mode){
			case ONLY:noBtn.setSelected(true);break;
			case FIRST: firstBtn.setSelected(true); break;
			case LAST: lastBtn.setSelected(true); break;
			default: noBtn.setSelected(true);
			}
		} else {
			noBtn.setSelected(true);
		}
	}

//	public boolean valid() {
//		if( this.isNo() ) {
//			return true;
//		}
//		return super.valid();
//	}
//
//	@Override
//	public String getText() {
//		// TODO Auto-generated method stub
//		return null;
//	}


}
