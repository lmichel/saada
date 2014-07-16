package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.enums.DataMapLevel;
import saadadb.meta.AttributeHandler;

/**
 * Use to map the extended attributes (add by the user)
 */
public class VPOtherMappingPanel extends VPAxisPanel {

	//Contains the name of the extended attribute + its field
	private HashMap<String,JTextField> fields;


	public VPOtherMappingPanel(VPSTOEPanel mappingPanel, boolean forEntry) {
		super(mappingPanel, "Extended attributes");
		int cpt =0;
		JTextField field;
		//		lines= new ArrayList<VPKWNamedField>();
		fields=new HashMap<String,JTextField>();
		//We get the attributes in a map

		Map<String, AttributeHandler> attributes = Database.getCachemeta().getAtt_extend(Category.TABLE	/*mappingPanel.getCategory()*/);

		//For each attribute we create a line with a name and a field
		for( Entry<String, AttributeHandler> e: attributes.entrySet()){
			//lines.add(new VPKWNamedField(this, e.getKey(),new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,null)));
			gbc.right(false);
			axisPanel.add(AdminComponent.getPlainLabel(e.getKey()), gbc);
			gbc.next();gbc.left(true);
			//Here we can add a type check to choose the Field we need

			field=new ReplaceMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,null);
			field.setColumns(AdminComponent.STRING_FIELD_NAME);
			fields.put(e.getKey(), field);
			axisPanel.add(field,gbc);

			if(cpt==0)
			{
				gbc.next();
				gbc.right(true);
				axisPanel.add(setHelpLabel(HelpDesk.CLASS_MAPPING),gbc);
			}
			gbc.newRow();
			cpt++;
		}
		//		Iterator<VPKWNamedField> it = lines.iterator();

		//We display each line
		//		while (it.hasNext()) {
		//		      it.next().setComponents();
		//		      if(cpt==0)
		//		      {
		//		    	  this.gbc.next();
		//		    	  this.gbc.right(true);
		//		    	  this.setHelpLabel(HelpDesk.CLASS_MAPPING);
		//		    	  this.gbc
		//		      }
		//		      cpt++;
		//		 
		//		}


	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		for(Entry<String, JTextField> e: fields.entrySet())
		{
			if(e.getValue().getText().length()>0)
			{
				if(mappingPanel.getCategory()!=Category.ENTRY) {
					params.add("-ukw");
					params.add(e.getKey().trim()+"="+e.getValue().getText());
				}
				else  {
					params.add("-eukw");
					params.add(e.getKey().trim()+"="+e.getValue().getText());
				}

			}
		}
		return params;
	}



	@Override
	public String checkAxisParams() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void reset() {
		for(Entry<String, JTextField> e: fields.entrySet())
		{
			e.getValue().setText("");
		}
	}

}
