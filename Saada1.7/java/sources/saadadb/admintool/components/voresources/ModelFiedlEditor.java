/**
 * 
 */
package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.DMAttributeTextField;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.sqltable.SQLQuery;

/**
 * @author laurentmichel
 *
 */
public class ModelFiedlEditor extends JPanel {
	protected UTypeHandler uth;
	protected String mapping;
	protected JButton check_button = new JButton("Check");
	protected DMAttributeTextField mapper;
	SaadaInstance dm_impl;
	MetaClass mc;
	MetaCollection mcoll;

	ModelFiedlEditor(MetaClass mc, UTypeHandler uth) throws FatalException {
		this.setLayout(new GridBagLayout());
		this.uth = uth;
		this.mc = mc;
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 1;
		c.gridx = 0;c.gridy = 0;c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("utype: "), c);
		c.gridx++;c.anchor = GridBagConstraints.LINE_START;
		this.add(AdminComponent.getPlainLabel(uth.getUtype()), c);

		c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("ucd: "), c);
		c.gridx++;c.anchor = GridBagConstraints.LINE_START;
		this.add(AdminComponent.getPlainLabel(uth.getUcd()), c);

		c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("type: "), c);
		c.gridx++;c.anchor = GridBagConstraints.LINE_START;
		int as = uth.getArraysize();
		String size = "";
		if( as == -1 ) {
			size = " (*)";
		} else if( as != 1 ) {
			size = " (" + as+ ")";				
		}
		this.add(AdminComponent.getPlainLabel(uth.getType() + size), c);

		c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
		this.add(new JLabel("desc: "), c);
		c.gridx++;c.anchor = GridBagConstraints.LINE_START;
		this.add(AdminComponent.getPlainLabel(uth.getComment()), c);

		c.gridx = 0;c.gridy++; c.gridwidth = 1;
		this.add(check_button, c);

		this.mapper = new DMAttributeTextField();
		this.mapper.setAttributeHandlers(mc);
		this.mapper.setColumns(32);
		if( dm_impl != null ) {
			String str = dm_impl.getSQLField(uth.getUtypeOrNickname());
			if( !"'null'".equals(str))
				this.mapper.setText(str);
		} else {
			this.mapper.setText("");
		}
		c.gridx++;c.fill = GridBagConstraints.HORIZONTAL;
		this.add(mapper, c);

		c.gridx++;
		this.add(AdminComponent.getPlainLabel(uth.getUnit()), c);

		TitledBorder title;
		switch( uth.getRequ_level()) {
		case UTypeHandler.MANDATORY: 				
			title = BorderFactory.createTitledBorder(uth.getNickname() + " (MAN)");
			title.setTitleColor(Color.BLACK);
			break;
		case UTypeHandler.RECOMMENDED: 				
			title = BorderFactory.createTitledBorder(uth.getNickname() + " (REC)");
			title.setTitleColor(Color.darkGray);
			break;
		default:
			title = BorderFactory.createTitledBorder(uth.getNickname() + " (OPT)");
			title.setTitleColor(Color.gray);
		}
		this.setBorder(title);
		this.setBackground(Color.WHITE);

		check_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkContent(true);
			}
		});
	}	

	public void setMetaClass(MetaClass mc) throws FatalException {					
		mapper.setText("");
		mapper.setAttributeHandlers(mc);
		if( dm_impl != null ) {
			try {
				String str = dm_impl.getSQLField(uth.getUtypeOrNickname());
				if( !"'null'".equals(str)) {
					this.mapper.setText(str);
				}
				else {
					this.mapper.setText("");
				}
			} catch(FatalException e){}
		}
		else {
			this.mapper.setText("");
		}
	}
	/**
	 * @return
	 */
	public String getMappingText() {
		String mt = mapper.getText().trim();
		if( mt.length() == 0 ) {
			mt = "'null'";
		}
		if( mt.startsWith("'") ){
			if( uth.getType().equals("char")) {
				return mt;
			}
			/*
			 * Remove quotes for numerics
			 */
			else {
				return mt.replaceAll("'", "");														
			}
		}
		else if( mt.startsWith("\"") ){
			if( uth.getType().equals("char")) {
				return mt.replaceAll("\"", "'");		
			}
			/*
			 * Remove quotes for numerics
			 */
			else {
				return mt.replaceAll("\"", "");														
			}
		}
		else {
			String mtt = mt.trim();
			if( mtt.startsWith("(") && mtt.endsWith(")") ) {
				return mtt;
			}
			else {
				return "(" + mtt + ")";
			}
		}		
	}

	public boolean checkContent(boolean with_dialog) {
		try {
			if( mapper.getText().trim().length() == 0 ) {
				mapper.setBackground(Color.WHITE);
				return true;								
			}
			String mt = getMappingText();
			SQLQuery squery = new SQLQuery();
			if( squery.run("Select " + mt + " from " + mc.getName()+  ", " + Database.getCachemeta().getCollectionTableName(mcoll.getName(), mc.getCategory()) + "  limit 1") != null ) {
				mapper.setBackground(Color.GREEN);
				squery.close();
				return true;			
			}
			else {
				AdminComponent.showInputError(this.getParent(), "Expression <" + mapper.getText() + "> can not be computed in SQL on table " + mc.getName());
				squery.close();
				mapper.setForeground(Color.RED);
			}
		} catch (Exception e) {
			mapper.setBackground(Color.RED);
			if( with_dialog)AdminComponent.showInputError(this.getParent(), e.toString());
		}
		return false;
	}
}
