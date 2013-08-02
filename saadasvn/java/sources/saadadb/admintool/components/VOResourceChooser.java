package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
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
import saadadb.util.Messenger;

public class VOResourceChooser extends JPanel 
{
	private static final long serialVersionUID = 1L;
	public static final int VO_PROTOCOL_FIELDS = 1;
	public static final int VO_PUBLISHED_RESOURCES = 2;
	
	private String[] VOResourceNames;
	private JList<String> confList = new JList<String>(new DefaultListModel<String>());
	private JXTable descriptionTable;
	private DefaultTableModel dm;
	private String selectedVOResource;
	private int lastSelectedIndex;
	private EditPanel editPanel;
	private int componentType;
	private Border selectedBorder;
	
	public VOResourceChooser(EditPanel editPanel, int componentType)
	{
		this.editPanel = editPanel;
		this.componentType = componentType;
		this.lastSelectedIndex = -1;
		this.confList.setFont(AdminComponent.plainFont);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1; c.weighty = 0.1;
		c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_START;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("List of protocols"));
		scrollPane.setPreferredSize(new Dimension(200,100));
		this.add(scrollPane, c);
		c.gridx = 1; c.gridy = 0;;
		JTextArea lbl = AdminComponent.getHelpLabel(HelpDesk.get(HelpDesk.VO_PROTOCOL_FIELDS));
		this.add(lbl, c);
		
		dm = new DefaultTableModel();
		descriptionTable = new JXTable(dm);
		descriptionTable.setHighlighters(HighlighterFactory.createSimpleStriping());
		descriptionTable.setRowSelectionAllowed(false);
		descriptionTable.setShowHorizontalLines(false);
		descriptionTable.setShowVerticalLines(false);
		descriptionTable.setColumnControlVisible(true);
		descriptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		descriptionTable.setEditable(false);
		JScrollPane jspDescription = new JScrollPane(descriptionTable);
		selectedBorder = BorderFactory.createTitledBorder("Structure of selected protocol");
		jspDescription.setBorder(selectedBorder);
		//jspDescription.setPreferredSize(new Dimension(500,400));
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
		this.loadConfList();
	}
	
	private void loadConfList()
	{
		DefaultListModel<String> model = (DefaultListModel<String>) confList.getModel();
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
	
	private void loadColumnHeaders()
	{
		String[] columnName = new String[]{"name", "ucd", "utype", "type", "asize", "hidden", "unit", "value", "desc", "reqlevel"};
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
	
	public void setDescription()
	{
		selectedVOResource = confList.getSelectedValue().toString();
		if (this.componentType==VO_PROTOCOL_FIELDS)
		{
			VOResource currentVOResource = null;
			try
			{
				currentVOResource = Database.getCachemeta().getVOResource(selectedVOResource);
				if( currentVOResource.getGroups() != null ) 
				{
					String[] groups = currentVOResource.groupNames();
					
					if (dm.getRowCount() > 0)
					    for (int i = dm.getRowCount() - 1; i > -1; i--) 
					    	dm.removeRow(i);
					
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
						}
						dm.addRow(new_row);
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
			// TODO Later
		}
	}
	
	public String getSelectedVOResource() 
	{
		return selectedVOResource;
	}
}
