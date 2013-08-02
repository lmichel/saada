package saadadb.admintool.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import saadadb.admintool.panels.EditPanel;
import saadadb.database.Database;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;

public class VOResourceChooser extends JPanel 
{
	private static final long serialVersionUID = 1L;
	public static final int VO_PROTOCOL_FIELDS = 1;
	public static final int VO_PUBLISHED_RESOURCES = 2;
	
	private Color headersDefaultColor;
	private String[] VOResourceNames;
	private JList<String> confList = new JList<String>(new DefaultListModel<String>());
	private JXTable descriptionTable;
	private DefaultTableModel dm;
	private String selectedVOResource;
	private int lastSelectedIndex;
	private EditPanel editPanel;
	private int componentType;
	
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
		c.gridx = 0; c.gridy = 0;c.weightx = 0;
		JScrollPane scrollPane = new JScrollPane(confList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("List of protocols"));
		scrollPane.setPreferredSize(new Dimension(200,100));
		this.add(scrollPane, c);
		
		descriptionTable = new JXTable(dm);
		descriptionTable.setHighlighters(HighlighterFactory.createSimpleStriping());
		descriptionTable.setRowSelectionAllowed(false);
		descriptionTable.setShowHorizontalLines(false);
		descriptionTable.setShowVerticalLines(false);
		descriptionTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		descriptionTable.setEditable(false);
		JScrollPane jspDescription = new JScrollPane(descriptionTable);
		jspDescription.setPreferredSize(new Dimension(450,500));
		jspDescription.setBorder(BorderFactory.createTitledBorder("Structure of selected protocol"));
		c.fill = GridBagConstraints.BOTH;
		c.gridx++; c.weightx = 1; c.gridheight = 2;
		this.add(jspDescription, c);
		headersDefaultColor = descriptionTable.getTableHeader().getBackground();
		descriptionTable.getTableHeader().setBackground(Color.WHITE);
		
		confList.addListSelectionListener(new ListSelectionListener() 
		{
			public void valueChanged(ListSelectionEvent arg0) 
			{
				if( confList.getSelectedValue() != null ) 
				{
					if (acceptChange()) 
					{
						setDescription();
						lastSelectedIndex = confList.getSelectedIndex();
					}
				}	
			}
		});
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
	
	/**
	 * @return
	 */
	private boolean acceptChange() 
	{
		Messenger.printMsg(Messenger.DEBUG, "test : " + confList.getSelectedIndex());
		if (confList.getSelectedIndex() != lastSelectedIndex) 
		{
			confList.setSelectedIndex(lastSelectedIndex);
			return false;
		} 
		else 
		{
			return true;
		}

	}
	
	public void setDescription()
	{
		selectedVOResource = confList.getSelectedValue().toString();
		//Messenger.printMsg(Messenger.DEBUG, "seletecVOResource:setDescription : " + selectedVOResource);
		VOResource currentVOResource = null;
		try
		{
			currentVOResource = Database.getCachemeta().getVOResource(selectedVOResource);
			if( currentVOResource.getGroups() != null ) 
			{
				String[] groups = currentVOResource.groupNames();
				for( String group: groups ) 
				{
					UTypeHandler[] uths = currentVOResource.getGroupUtypeHandlers(group);
					for( UTypeHandler uth: uths) 
					{
						String retour = " name=" + uth.getNickname()
						+ " ucd=" + uth.getUcd()
						+ " utype=" + uth.getUtype()
						+ " type=" + uth.getType()
						+ " asize=" + uth.getArraysize()
						+ " hidden=" + uth.isHidden()
						+ " unit=" + uth.getUnit()
						+ " value=" + uth.getValue()
						+ " desc=" + uth.getComment()
						+ " reqlevel=" + uth.getRequ_level() + "\n";
						
						//Messenger.printMsg(Messenger.DEBUG, "currentVOResource : " + selectedVOResource + "\n" + retour);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public String getSelectedVOResource() 
	{
		return selectedVOResource;
	}
}
