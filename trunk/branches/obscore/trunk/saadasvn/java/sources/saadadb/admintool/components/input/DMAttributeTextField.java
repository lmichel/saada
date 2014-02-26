package saadadb.admintool.components.input;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.TreePath;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.dnd.TreepathDropableTextField;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.query.parser.UnitHandler;
import saadadb.unit.Unit;

/** * @version $Id: DMAttributeTextField.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * @author laurentmichel
 *
 */
public class DMAttributeTextField extends TreepathDropableTextField {
	private String dm_unit;
	private Map<String, AttributeHandler> attributeHandlers;
	private AttributeHandler ah;
	private String conv_fct;
	
	public DMAttributeTextField() {
		/*
		 * Takes the second node, without extension checking
		 */
		this.setTransferHandler(new ProductTreePathTransferHandler(1));				
	}
	
	@Override
	public boolean setText(TreePath treepath) {
		this.treepath = treepath;
		this.ah = attributeHandlers.get(this.treepath.getPathComponent(1).toString().trim());
		if( valid() && this.isEditable() ) {
			setBackground(Color.WHITE);
			this.setText(this.getText() + " " + this.conv_fct );	
			return true;
		}	
		else {
			return false;
		}
	}
	
	/**
	 * @param mc
	 */
	public void setAttributesHandlers( HashMap<String, AttributeHandler> attributeHandlers) {
		this.attributeHandlers = attributeHandlers;
	}
	/**
	 * @param mc
	 * @throws FatalException 
	 */
	public void setAttributeHandlers( MetaClass mc) throws FatalException {
		this.attributeHandlers = Database.getCachemeta().getCollection(mc.getCollection_name()).getAttribute_handlers(mc.getCategory());
		this.attributeHandlers.putAll(mc.getAttributes_handlers());
	}
	/**
	 * @param mc
	 * @param category
	 * @throws FatalException
	 */
	public void setAttributeHandlers( int category) throws FatalException {
		this.attributeHandlers = MetaCollection.getAttribute_handlers(category);
	}
	/**
	 * @param mc
	 * @param category
	 * @throws FatalException
	 */
	public void setAttributeHandlers(String category) throws FatalException {
		this.attributeHandlers = MetaCollection.getAttribute_handlers(Category.getCategory(category));
	}
	
	@Override
	protected boolean valid() {
		String source_unit = this.ah.getUnit();
		if( dm_unit != null && dm_unit.length() > 0 ) {
			if( source_unit != null && source_unit.length() != 0 ) {
				try {
					(new Unit(dm_unit)).convertFromStr(new Unit(source_unit));		
					this.conv_fct = UnitHandler.getConvFunction(dm_unit, source_unit, ah.getNameattr());

					return true;
				} catch (Exception e) {
					AdminComponent.showInputError(this.getParent(), e.toString());
					return false;
				}
			} else {
				this.conv_fct = ah.getNameattr();
				return AdminComponent.showConfirmDialog(this.getParent(), "Attribute <" + ah.getNameattr() + "> has no unit: It will be taken as " + this.dm_unit + ". Do you keep it?");
			}
		} else {
			if( source_unit == null || source_unit.length() == 0 ) {
				this.conv_fct = ah.getNameattr();
				return true;
			} else {
				this.conv_fct = ah.getNameattr();
				return AdminComponent.showConfirmDialog(this.getParent(), "Data model attribute has no unit.Attribute <" 
						+ ah.getNameattr() + "> will be considered as unitless although it is declared as " + source_unit + ". Do you keep it?");
			}
		}
	}

}
