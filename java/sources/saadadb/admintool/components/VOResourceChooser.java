package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.database.Database;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capability;
import saadadb.vocabulary.enums.VoProtocol;

public class VOResourceChooser extends JPanel 
{
	private static final long serialVersionUID = 1L;
	public static final int VO_PROTOCOL_FIELDS = 1;
	public static final int VO_PUBLISHED_RESOURCES = 2;
	
	private String[] VOResourceNames;
	private JList confList = new JList(new DefaultListModel());
	private JXTable descriptionTable;
	private DefaultTableModel dm;
	private VoProtocol selectedVOResource;
	private int lastSelectedIndex;
	private int componentType;
	private Border selectedBorder;
	
	public VOResourceChooser(EditPanel editPanel, int componentType)
	{
		this.componentType = componentType;
		this.lastSelectedIndex = -1;
		this.confList.setFont(AdminComponent.plainFont);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.setLayout(new GridBagLayout());
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("List of protocols"));
		String[] txt_lbl = (this.componentType==VO_PROTOCOL_FIELDS?HelpDesk.get(HelpDesk.VO_PROTOCOL_FIELDS):HelpDesk.get(HelpDesk.VO_PUBLISHED_RESOURCES));
		JTextArea lbl = AdminComponent.getHelpLabel(txt_lbl);
		dm = new DefaultTableModel();
		descriptionTable = new JXTable(dm);
		descriptionTable.setRowSelectionAllowed(true);
		descriptionTable.setShowHorizontalLines(false);
		descriptionTable.setShowVerticalLines(false);
		descriptionTable.setColumnControlVisible(true);
		descriptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		descriptionTable.setEditable(false);
		JScrollPane jspDescription = new JScrollPane(descriptionTable);
		String titleDescriptionTable = (this.componentType==VO_PROTOCOL_FIELDS?"Structure of selected protocol":"List of published resources in this protocol");
		selectedBorder = BorderFactory.createTitledBorder(titleDescriptionTable);
		jspDescription.setBorder(selectedBorder);
		
		GridBagConstraints c = new GridBagConstraints();
		scrollPane.setPreferredSize(new Dimension(200,100));
		c.insets = new Insets(3, 3, 3, 3);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1; c.weighty = 0.1;
		c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_START;
		this.add(scrollPane, c);
		c.gridx = 1; c.gridy = 0;
		this.add(lbl, c);
		c.gridx = 0; c.gridy = 1; c.gridwidth = 2;c.weighty = 0.9;
		this.add(jspDescription, c);
		
		confList.addListSelectionListener(new ListSelectionListener() 
		{
			public void valueChanged(ListSelectionEvent arg0) 
			{
				if( confList.getSelectedValue() != null ) 
				{
					if (acceptChange()) 
					{
						VOResourceChooser.this.setDescription();
						lastSelectedIndex = confList.getSelectedIndex();
					}
				}	
			}
		});
		this.loadColumnHeaders();
		descriptionTable.setHighlighters(HighlighterFactory.createSimpleStriping());
		this.loadConfList();
	}
	
	private void loadConfList()
	{
		DefaultListModel model = (DefaultListModel) confList.getModel();
		if (this.componentType==VO_PROTOCOL_FIELDS)
		{
			try 
			{
				VOResourceNames = Database.getCachemeta().getVOResourceNames();
				for (int i=0 ; i<VOResourceNames.length ; i++)
				{
					model.addElement(VOResourceNames[i]);
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else if (this.componentType==VO_PUBLISHED_RESOURCES)
		{
			model.addElement(VoProtocol.SIA);
			model.addElement(VoProtocol.SSA);
			model.addElement(VoProtocol.ConeSearch);
			model.addElement(VoProtocol.TAP);
		}
	}
	
	private void loadColumnHeaders()
	{
		String[] columnName = null;
		if (this.componentType==VO_PROTOCOL_FIELDS)
		{
			columnName = new String[]{"name", "ucd", "utype", "type", "asize", "hidden", "unit", "value", "desc", "reqlevel"};

		}
		else if (this.componentType==VO_PUBLISHED_RESOURCES)
		{
			columnName = new String[]{"resource name", "accessURL", "description"};
		}
		for (int i=0 ; i<columnName.length ; i++)
		{
			dm.addColumn(columnName[i]);
		}
	}
	
	/**
	 * @return
	 */
	private boolean acceptChange() 
	{
		if (confList.getSelectedIndex() != lastSelectedIndex) 
			return true;
		else 
			return false;
	}
	
	public JTable getDescriptionTable()
	{
		return descriptionTable;
	}
	
	public JList getconfList()
	{
		return confList;
	}
	
	public void setDescription()
	{
		selectedVOResource = (VoProtocol) confList.getSelectedValue();
		if (dm.getRowCount() > 0)
		    for (int i = dm.getRowCount() - 1; i > -1; i--) 
		    	dm.removeRow(i);
		if (this.componentType==VO_PROTOCOL_FIELDS)
		{
			VOResource currentVOResource = null;
			try
			{
				currentVOResource = Database.getCachemeta().getVOResource(selectedVOResource.toString());
				if( currentVOResource.getGroups() != null ) 
				{
					String[] groups = currentVOResource.groupNames();
					for( String group: groups ) 
					{
						String[] new_row = new String[10];
						UTypeHandler[] uths = currentVOResource.getGroupUtypeHandlers(group);
						for( UTypeHandler uth: uths) 
						{
							new_row[0] = (uth.getNickname()!=null?uth.getNickname():"");
							new_row[1] = (uth.getUcd()!=null?uth.getUcd():"");
							new_row[2] = (uth.getUtype()!=null?uth.getUtype():"");
							new_row[3] = (uth.getType()!=null?uth.getType():"");
							new_row[4] = uth.getArraysize()+"";
							new_row[5] = (uth.isHidden()?"True":"False");
							new_row[6] = (uth.getUnit()!=null?uth.getUnit():"");
							new_row[7] = (uth.getValue()!=null?uth.getValue():"");
							new_row[8] = (uth.getComment()!=null?uth.getComment():"");
							new_row[9] = uth.getRequ_level()+"";
							dm.addRow(new_row);
						}
					}
					descriptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					descriptionTable.packAll();
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else if (this.componentType==VO_PUBLISHED_RESOURCES)
		{
			ArrayList<Capability> lc = new ArrayList<Capability>();
			VoProtocol protocol = selectedVOResource;
			try 
			{
				Table_Saada_VO_Capabilities.loadCapabilities(lc, protocol);
				for( Capability cap: lc) 
				{
					Object[] new_row = new Object[3];
					new_row[0] = cap.getDataTreePathString();
					new_row[1] = cap.getAccessURL();
					new_row[2] = cap.getDescription();
					dm.addRow(new_row);
				}
				descriptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				descriptionTable.packAll();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public VoProtocol getSelectedVOResource()  {
		return selectedVOResource;
	}
}
