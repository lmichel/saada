package saadadb.admintool.panels.editors;

import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.VOResourceChooser;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.admintool.windows.TextSaver;

public class VOPublishedResourcesPanel extends EditPanel
{
	private static final long serialVersionUID = 1L;
	private VOResourceChooser resourceChooser;
	private String selectedProtocol, selectedResource;
	private JButton jb_test;
	
	public VOPublishedResourcesPanel(AdminTool rootFrame, String ancestor)
	{
		super(rootFrame, AdminComponent.VO_PUBLISHED_RESOURCES, null, ancestor);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() 
	{
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) 
	{
		// Nothing for the moment
	}

	@Override
	protected void setActivePanel() 
	{
		JPanel tPanel = this.addSubPanel("List of published VO Resources", false);
		resourceChooser = new VOResourceChooser(this, VOResourceChooser.VO_PUBLISHED_RESOURCES);
		this.selectedProtocol = "";
		this.selectedResource = "";
		MyGBC c = new MyGBC(5,5,5,5);
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH; c.gridwidth = 2;
		tPanel.add(resourceChooser, c);
		c.gridy = 1; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.LINE_START; c.weighty = 0;
		jb_test = new JButton("Test the selected resource");
		jb_test.setEnabled(false);
		tPanel.add(jb_test, c);
		jb_test.addMouseListener(new MouseListener() 
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				(new TextSaver(rootFrame, selectedResource + " (" + selectedProtocol + ")", selectedResource + " (" + selectedProtocol + ")", "<Content>Content of the table</Content>")).open(SQLJTable.DMVIEW_PANEL);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {}
	
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
		final JTable descriptionTable = resourceChooser.getDescriptionTable();
		descriptionTable.addMouseListener(new java.awt.event.MouseAdapter() 
		{
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) 
		    {
		        int row = descriptionTable.rowAtPoint(evt.getPoint());
		        int col = descriptionTable.columnAtPoint(evt.getPoint());
		        if (row >= 0 && col >= 0) 
		        {
		        	selectedResource = descriptionTable.getModel().getValueAt(row, 0).toString();
		        	jb_test.setEnabled(true);
		        }
		        else
		        	jb_test.setEnabled(false);
		    }
		});
		final JList<String> confList = resourceChooser.getconfList();
		confList.addListSelectionListener(new ListSelectionListener() 
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				selectedProtocol = confList.getSelectedValue().toString();
				jb_test.setEnabled(false);
			}
		});
	}

	@Override
	public void active() {}
}
